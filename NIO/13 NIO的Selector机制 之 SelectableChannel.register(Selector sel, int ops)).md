# 13 NIO的Selector机制 之 SelectableChannel.register(Selector sel, int ops))
通过上一篇文章，我们知道了Selector机制中的open()函数做了什么,其实也就是创建了一个管道,并把pipe的读写文件描述符放入pollArray中,这个pollArray是Selector的枢纽。下面我们抓取源码看一下channel在selector中注册时做了什么？ 
SelectableChannel.register(Selector sel, int ops)

## SelectableChannel.register(Selector sel, int ops))函数分析

register()函数是在SelectableChannel这个类中实现的，所以只有继承自这个类的channel才能通过selector管理，这个要点在前面文章中已经解释过了。那么问题在于这个register()函数做了什么呢？我们先看看JavaDoc上面的解释：

``` java
public final SelectionKey register(Selector sel, int ops)throws ClosedChannelException
{
    return register(sel, ops, null);
}
```
javaDoc上面解释：向给定的选择器注册此通道，返回一个选择键。该方法调用一个多态的register(sel, ops, null); 
我们进入这个多态的函数如下：

``` java
public abstract SelectionKey register(Selector sel, int ops, Object att)
        throws ClosedChannelException;
```
发现这是一个抽象方法，进入实现方法：在进入实现方法之前，我先给出ServerSocketServer的类继承图： 

![](https://github.com/muyinchen/woker/blob/master/mypics/ServerSocketServer.jpg?raw=true)

所以我们可以知道AbstractSelectableChannel是继承自SelectableChannel类的，并且在AbstractSelectableChannel中实现了register(Selector sel, int ops, Object att)方法。源码如下：
``` java
public final SelectionKey register(Selector sel, int ops,                            Object att)throws ClosedChannelException
{
    synchronized (regLock) {
        if (!isOpen())
            throw new ClosedChannelException();
        if ((ops & ~validOps()) != 0)
            throw new IllegalArgumentException();
        if (blocking)
            throw new IllegalBlockingModeException();
        SelectionKey k = findKey(sel);
        if (k != null) {
            k.interestOps(ops);
            k.attach(att);
        }
        if (k == null) {
            // New registration
            synchronized (keyLock) {
                if (!isOpen())
                    throw new ClosedChannelException();
                k = ((AbstractSelector)sel).register(this, ops, att);
                addKey(k);
            }
        }
        return k;
    }
}
```
首先还是看JavaDoc，看这个函数主要是干嘛的：该方法实现体内部就是一个同步代码段。 
JavaDoc：向给定的选择器注册此通道，返回一个选择键。

``` java
if (!isOpen())
    throw new ClosedChannelException();
if ((ops & ~validOps()) != 0)
    throw new IllegalArgumentException();
if (blocking)
    throw new IllegalBlockingModeException();
```
从上面的代码可知：该方法首先判断该通道是否是打开的，以及给定的初始相关操作集是否有效。此外该通道也必须设置成非阻塞的通道。如果不满要求均会抛出异常。 
再看后面的代码：
``` java
SelectionKey k = findKey(sel);
if (k != null) {
    k.interestOps(ops);
    k.attach(att);
}
```
首先分析findKey(sel);这个方法,传入selector参数。这是个类私有的方法，源码如下：
``` java
private SelectionKey findKey(Selector sel) {
    synchronized (keyLock) {
      if (keys == null)
          return null;
      for (int i = 0; i < keys.length; i++)
          if ((keys[i] != null) && (keys[i].selector() == sel))
              return keys[i];
      return null;
    }
}
```
该方法传参当前的selector。函数中用到了一个十分重要的变量keys，我们先看看这个变量的定义： 
private SelectionKey[] keys = null; 
源码中国给的注释是：这个变量保存的是已经注册在某个selector中的通道的selectionKey，如果某个通道被closed了，那么相应的key也必须被注销。这个属性被keyLock这个对象锁保护。

这个函数内部首先判断keys是否为空，如果为空，说明当前通道肯定是没有被注册的，所以返回null.如果keys非空，就遍历keys，获取每个selectionKey所注册的selector的selector，如果和给定的selector相同，就说明此通道已经注册在该selector上了，
``` java
if (k != null) {
    k.interestOps(ops);
    k.attach(att);
}
```
这段代码说明该通道已经注册在了该selector上了，在将其相关操作集设置为给定值后，返回表示该注册的选择键。
``` java
if (k == null) {
    // New registration
    synchronized (keyLock) {
        if (!isOpen())
            throw new ClosedChannelException();
        k = ((AbstractSelector)sel).register(this, ops, att);
        addKey(k);
    }
    }
return k;
```
这里的判断当k为空时，说明该通道没有注册在该selector上，所以我们需要执行注册操作。在保持适当锁定的同时调用选择器的 register 方法。返回前将得到的键添加到此通道的键集中。这里注册又调用的函数是：
``` java
k = ((AbstractSelector)sel).register(this, ops, att);
addKey(k);
```
这两行代码注册当前通道channel到selector中，并且获得相应的SelectionKey，然后将SelectionKey添加到SelectionKey[] keys数组中。 
上面的k = ((AbstractSelector)sel).register(this, ops, att); 
调用的实际上是SelectorImpl的register()函数。这个SelectorImpl是AbstractSelector的子类。我们看一下这个SelectorImpl.register()的实现;
``` java
protected final SelectionKey register(AbstractSelectableChannel var1, int var2, Object var3) {
    if(!(var1 instanceof SelChImpl)) {
        throw new IllegalSelectorException();
    } else {
        SelectionKeyImpl var4 = new SelectionKeyImpl((SelChImpl)var1, this);
        var4.attach(var3);
        Set var5 = this.publicKeys;
        synchronized(this.publicKeys) {
            this.implRegister(var4);
        }

        var4.interestOps(var2);
        return var4;
    }
}
```
这里的var1就是一个channel，var2是一个“interest集合”，意思是在通过Selector监听Channel时对什么事件感兴趣。var3是一个附加对象。 
这里核心的就是做了两件事：

1.把var1也就是当前channel包装成SelectionKeyImpl，也就是var4；

2.调用 this.implRegister(var4);实现注册。

继续往下面分析，implRegister()是一个abstract方法，有两个实现类， 

![](https://github.com/muyinchen/woker/blob/master/mypics/implRegister().jpg?raw=true)

由于我是在Mac下，所以选择的是KQueueSelectorImpl这个类里面的实现：这里就是register最核心的实现地方：
``` java
protected void implRegister(SelectionKeyImpl var1) {
    if(this.closed) {
        throw new ClosedSelectorException();
    } else {
        //1. 获取当前通道的文件描述符var2
        int var2 = IOUtil.fdVal(var1.channel.getFD());
        //2. 将文件描述符和对应的SelectionKey添加到fdMap里面
        this.fdMap.put(Integer.valueOf(var2), new KQueueSelectorImpl.MapEntry(var1));
        ++this.totalChannels;
        //把selectionKey也添加到keys中。
        this.keys.add(var1);
    }
}
```
这里var1是当前channel的selectionKey。这里面最重要的变量也就是： 
fdMap这个变量和keys变量。 


