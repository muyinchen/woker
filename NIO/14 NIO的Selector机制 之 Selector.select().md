# 14 NIO的Selector机制 之 Selector.select()
我们先来回顾下上篇博文中所介绍的内容。

**Selector selector = Selector.open();这行代码简单来说：实例化了一个WindowSelectorImpl类的对象。并且在windows下通过两个链接的socketChannel实现了Pipe。**

知道上面一点知识就更加方便的来理解了。下面开始详细的介绍

## Selector.select() 介绍：
前面已经介绍过了Selector的open函数以及channel的register函数，现在分析最后一个函数：select()函数。

selector.select()在Selector类中此方法是一个抽象的。如下： 
public abstract int select() throws IOException; 
函数功能：选择一些I/O操作已经准备好的channel。每个channel对应着一个key。这个方法是一个阻塞的选择操作。当至少有一个通道被选择时才返回。当这个方法被执行时，当前线程是允许被中断的。

除了这个方法之外，还有两个重载方法： 
1. public abstract int select(long timeout)throws IOException; 

2. public abstract int selectNow() throws IOException;

####  select(long timeout) 
   select(long timeout)和select()一样，除了最长会阻塞timeout毫秒(参数)。 
   这个方法并不能提供精确时间的保证，和当执行wait(long timeout)方法时并不能保证会延时timeout道理一样。

这里的timeout说明如下：

- 如果 timeout为正，则select(long timeout)在等待有通道被选择时至多会阻塞timeout毫秒
- 如果timeout为零，则永远阻塞直到有至少一个通道准备就绪。
- timeout不能为负数

#### selectNow() 
这个方法与select()的区别在于，是非阻塞的，即当前操作即使没有通道准备好也是立即返回。只是返回的是0. 
**值得注意的是：调用这个方法会清除所有之前执行了wakeup方法的作用。**

## select()函数的具体实现

在上篇文章中我们通过源码的角度知道Selector selector = Selector.open()；代码中的selector实际是指向的是其子类WindowsSelectorImpl 的对象实例。

因此在 我们执行 selector.select()方法时，实际上时调用的是 WindowsSelectorImpl 中的select()方法

在找这个函数的时候，觉得有必要说下Selector， WindowsSelectorImpl 之间的继承关系：

WindowsSelectorImpl 的直接父类是 SelectorImpl(select方法在这个里面实现)；
SelectorImpl 的直接父类是 AbstractSelector
AbstractSelector 的直接父类是 Selector.
以上就是他们的继承关系，其中select()方法是在 SelectorImpl类中进行实现的。


首先我们来看一下这三个方法的实现：是在SelectorImpl这个类里面：

``` java
public int select(long var1) throws IOException {
        if(var1 < 0L) {
            throw new IllegalArgumentException("Negative timeout");
        } else {
            return this.lockAndDoSelect(var1 == 0L?-1L:var1);
        }
}

public int select() throws IOException {
    return this.select(0L);
}

public int selectNow() throws IOException {
    return this.lockAndDoSelect(0L);
}
```
我们可以发现，这三个方法最终都是调用了 
lockAndDoSelect(0L); 
这个函数，这个函数也是在SelectorImpl这个类里面实现的，源码如下：
``` java
private int lockAndDoSelect(long var1) throws IOException {
    synchronized(this) {
        //坚持selector是否已经打开了
        if(!this.isOpen()) {
            throw new ClosedSelectorException();
        } else {
            Set var4 = this.publicKeys;
            int var10000;
            //这里用了双重锁来实现同步访问，双重锁可能引起死锁。
            synchronized(this.publicKeys) {
                Set var5 = this.publicSelectedKeys;
                synchronized(this.publicSelectedKeys) {
                    var10000 = this.doSelect(var1);
                }
            }
            return var10000;
        }
    }
}
```
这里先看下这个isOpen()函数的具体实现，这个函数是在 AbstractSelector 中实现的。
``` java
public final boolean isOpen() {
    return selectorOpen.get();
}
```
函数的功能：检查这个Selector是否打开； 
在isOpen()方法中的selectorOpen变量在AbstractSelector类中定义如下：

```java
private AtomicBoolean selectorOpen = new AtomicBoolean(true); 
```

即selectorOpen是一个原子性的变量；如果在执行selector.select()方法之前执行了Selector selector = Selector.open();则selectorOpen就进行了初始化，为true。否则为false。

最后分析一下lockAndDoSelect()中的核心函数： 
doSelect(var1); 
传参数是var1是一个long型整数，表示阻塞等待的时间。 
doselect()方法也是一个abstract方法。他有两个实现： 

![](https://github.com/muyinchen/woker/blob/master/mypics/doselect()%E5%AE%9E%E7%8E%B0.png?raw=true)

由于我是在Mac下，所以默认调用的是KQueueSelectorImpl这个类里面的实现，源码如下：
``` java
protected int doSelect(long var1) throws IOException {
    boolean var3 = false;
    //判断当前selector是否是关闭的，
    if(this.closed) {
        throw new ClosedSelectorException();
    } else {
        this.processDeregisterQueue();

        int var7;
        try {
            this.begin();
            var7 = this.kqueueWrapper.poll(var1);
        } finally {
            this.end();
        }

        this.processDeregisterQueue();
        return this.updateSelectedKeys(var7);
    }
}
```
这部分代码还是比较复杂的，这里我只分析核心的地方： 

var7 = this.kqueueWrapper.poll(var1); 

这个函数的内部会调用系统的poll函数，轮询kqueueWrapper里面保存的fd,内部会调用native方法。这里会监听fd中是否有数据进出，这回造成IO阻塞，直到有数据读写事件发生。

比如，由于pollWrapper中保存的也有ServerSocketChannel的FD(在上篇博文中提到)，所以只要ClientSocket发一份数据到ServerSocket,那么poll0（）就会返回； 
又由于pollWrapper中保存的也有pipe的write端的FD，所以只要pipe的write端向FD发一份数据，也会造成poll0（）返回； 
如果这两种情况都没有发生，那么poll0（）就一直阻塞，也就是selector.select()会一直阻塞；如果有任何一种情况发生，那么selector.select()就会返回， 
所有在OperationServer的run()里要用while (true) {，这样就可以保证在selector接收到数据并处理完后继续监听poll();

## WindowsSelectorImpl.wakeup()

看完了select()方法的内部实现思路，最后来看下：WindowsSelectorImpl.wakeup()的具体实现.

wakeup()方法源码如下：
``` java
    public Selector wakeup() {
        synchronized (interruptLock) {
            if (!interruptTriggered) {
                setWakeupSocket();
                interruptTriggered = true;
            }
        }
        return this;
    }
    // Sets Windows wakeup socket to a signaled state.
    private void setWakeupSocket() {
        setWakeupSocket0(wakeupSinkFd);
    }
    private native void setWakeupSocket0(int wakeupSinkFd);
```
native实现摘要：

——-WindowsSelectorImpl.c —-

``` c
    Java_sun_nio_ch_WindowsSelectorImpl_setWakeupSocket0(JNIEnv *env, jclass this,
                                                    jint scoutFd)
    {
        /* Write one byte into the pipe */
        send(scoutFd, (char*)&POLLIN, 1, 0);
    }
```
这里完成了向最开始建立的pipe的sink端写入了一个字节，source文件描述符就会处于就绪状态，poll方法会返回，从而导致select方法返回。（原来自己建立一个socket链着自己另外一个socket就是为了干这事）

sun.nio.ch包下面所有的类库可以在这里看到：http://www.docjar.com/docs/api/sun/nio/ch/package-index.html

小结

关于selector.select()方法中的脉络就这样的顺了一遍，还是有很多的细节自己由于水平的原因没有理解清楚，如有错误，请批评指正。

由于目前自己从源码的角度只看了Selector.open()方法和selector.select()方法的内部实现。还有以下几块内容有待自己去理解：

1、Channel 类中的 register()方法，其中涉及到ServerSocketChannel 类和SocketChannel类。

2、selector.selectedKey()方法返回的Set集合中的值是何时添加进去的，以及SelectionKey类相关的一些操作的具体实现。

这就是两块比较重要内容，这对理解整个的Java NIO有很大帮助。
