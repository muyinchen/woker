# InterruptibleChannel 与可中断 IO



Java传统IO是不支持中断的，所以如果代码在read/write等操作阻塞的话，是无法被中断的。这就无法和Thead的interrupt模型配合使用了。JavaNIO众多的升级点中就包含了IO操作对中断的支持。InterruptiableChannel表示支持中断的Channel。我们常用的FileChannel，SocketChannel，DatagramChannel都实现了这个接口。

## **InterruptibleChannel接口**

```java
 public interface InterruptibleChannel extends Channel

 {
 	/**
      * 关闭当前Channel
      *     
      * 任何当前阻塞在当前channel执行的IO操作上的线程，都会收到一个AsynchronousCloseException异常
      */

     public void close() throws IOException;

 }
```

InterruptibleChannel接口没有定义任何方法，其中的close方法是父接口就有的，这里只是添加了额外的注释。

AbstractInterruptibleChannel实现了InterruptibleChannel接口，并提供了实现可中断IO机制的重要的方法，比如begin()，end()。

在解读这些方法的代码前，先了解一下NIO中，支持中断的Channel代码是如何编写的。

第一个要求是要正确使用begin()和end()方法：

```
 boolean completed = false;

 try {
     begin();

     completed = ...;    // 执行阻塞IO操作

     return ...;         // 返回结果

 } finally {

     end(completed);

 }
```

NIO规定了，在阻塞IO的语句前后，需要调用begin()和end()方法，为了保证end()方法一定被调用，要求放在finally语句块中。

第二个要求是Channel需要实现java.nio.channels.spi.AbstractInterruptibleChannel#implCloseChannel这个方法。AbstractInterruptibleChannel在处理中断时，会调用这个方法，使用Channel的具体实现来关闭Channel。

接下来我们具体看一下begin()和end()方法是如何实现的。

## **begin方法**

```java
 // 保存中断处理对象实例
 private Interruptible interruptor;
 // 保存被中断线程实例
 private volatile Thread interrupted;
 protected final void begin() {

     // 初始化中断处理对象，中断处理对象提供了中断处理回调
     // 中断处理回调登记被中断的线程，然后调用implCloseChannel方法，关闭Channel
     if (interruptor == null) {
         interruptor = new Interruptible() {
             public void interrupt(Thread target) {
                 synchronized (closeLock) {
                     // 如果当前Channel已经关闭，则直接返回
                    if (!open) // 在JDK11中已经换为closed来做这个状态维护了
                         return;
                    // 设置标志位，同时登记被中断的线程
                    open = false;
                     interrupted = target;
                     try {
                        // 调用具体的Channel实现关闭Channel
                         AbstractInterruptibleChannel.this.implCloseChannel();
                    } catch (IOException x) { }
                 }
            }};

     }
     // 登记中断处理对象到当前线程

     blockedOn(interruptor);
     // 判断当前线程是否已经被中断，如果已经被中断，可能登记的中断处理对象没有被执行，这里手动执行一下
     Thread me = Thread.currentThread();
     if (me.isInterrupted())
         interruptor.interrupt(me);
 }
```

从begin()方法中，我们可以看出NIO实现可中断IO操作的思路，是在Thread的中断逻辑中，挂载自定义的中断处理对象，这样Thread对象在被中断时，会执行中断处理对象中的回调，这个回调中，执行关闭Channel的操作。这样就实现了Channel对线程中断的响应了。

接下来重点就是研究“Thread添加中断处理逻辑”这个机制是如何实现的了，是通过blockedOn方法实现的：

```java
 static void blockedOn(Interruptible intr) {         // package-private
     sun.misc.SharedSecrets.getJavaLangAccess().blockedOn(Thread.currentThread(),intr);
 }
```

blockedOn方法使用的是JavaLangAccess的blockedOn方法。

SharedSecrets是一个神奇而糟糕的类，为啥说是糟糕呢，因为这个方法的存在，就是为了访问JDK类库中一些因为类作用域限制而外部无法访问的类或者方法。JDK很多类与方法是私有或者包级别私有的，外部是无法访问的，但是JDK在本身实现的时候又存在互相依赖的情况，所以为了外部可以不依赖反射访问这些类或者方法，在sun包下，存在这么一个类，提供了各种超越限制的方法。

SharedSecrets.getJavaLangAccess()方法返回JavaLangAccess对象。JavaLangAccess对象就和名称所说的一样，提供了java.lang包下一些非公开的方法的访问。这个类在System初始化时被构造：

```java
 // java.lang.System#setJavaLangAccess

 private static void setJavaLangAccess() {
    sun.misc.SharedSecrets.setJavaLangAccess(new sun.misc.JavaLangAccess(){
         public void blockedOn(Thread t, Interruptible b) {
             t.blockedOn(b);
        }

        //...
     });
 }
```

可以看出，sun.misc.JavaLangAccess#blockedOn保证的就是java.lang.Thread#blockedOn这个包级别私有的方法：

```java
 /* The object in which this thread is blocked in an interruptible I/O
  * operation, if any.  The blocker's interrupt method should be invoked
  * after setting this thread's interrupt status.
  */

 private volatile Interruptible blocker;
 private final Object blockerLock = new Object();

 /* Set the blocker field; invoked via sun.misc.SharedSecrets from java.nio code
  */
 void blockedOn(Interruptible b) {
     // 串行化blocker相关操作
     synchronized (blockerLock) {
         blocker = b;
     }
 }
```

而这个方法也非常简单，就是设置java.lang.Thread#blocker变量为之前提到的中断处理对象。而且从注释中可以看出，这个方法就是专门为NIO设计的，注释都非常直白的提到了，NIO的代码会通过sun.misc.SharedSecrets调用到这个方法。。

接下来就是重头戏了，看一下Thread在中断时，如何调用NIO注册的中断处理器：

``` java
 public void interrupt() {

     if (this != Thread.currentThread())

         checkAccess();

     synchronized (blockerLock) {

         Interruptible b = blocker;

         // 如果NIO设置了中断处理器，则只需Thread本身的中断逻辑后，调用中断处理器的回调函数
         if (b != null) {

             interrupt0();           // 这一步会设置interrupt标志位

             b.interrupt(this);
             return;

         }

     }

     // 如果没有的话，就走普通流程
     interrupt0();

 }
```

![640?wx_fmt=png](https://github.com/muyinchen/woker/blob/master/mypics/sd%E5%8F%AF%E4%B8%AD%E6%96%ADIO%E5%9C%BA%E6%99%AF.jpg?raw=true)

## **end方法**

begin()方法负责添加Channel的中断处理器到当前线程。end()是在IO操作执行完/中断完后的操作，负责判断中断是否发生，如果发生判断是当前线程发生还是别的线程中断把当前操作的Channel给关闭了，对于不同的情况，抛出不同的异常。

``` java
 protected final void end(boolean completed) throws AsynchronousCloseException
 {
     // 清空线程的中断处理器引用，避免线程一直存活导致中断处理器无法被回收
     blockedOn(null);
     Thread interrupted = this.interrupted;
     if (interrupted != null && interrupted == Thread.currentThread()) {
         interrupted = null;
        throw new ClosedByInterruptException();
    }
     // 如果这次没有读取到数据，并且Channel被另外一个线程关闭了，则排除Channel被异步关闭的异常
     if (!completed && !open)
         throw new AsynchronousCloseException();
 }
```

通过代码可以看出，如果是当前线程被中断，则抛出ClosedByInterruptException异常，表示Channel因为线程中断而被关闭了，IO操作也随之中断了。

如果是当前线程发现Channel被关闭了，并且是读取还未执行完毕的情况，则抛出AsynchronousCloseException异常，表示Channel被异步关闭了。

end()逻辑的活动图如下：

![640?wx_fmt=png](https://github.com/muyinchen/woker/blob/master/mypics/act%20end()%E9%80%BB%E8%BE%91.jpg?raw=true)

## **场景分析**

并发的场景分析起来就是复杂，上面的代码不多，但是场景很多，我们以sun.nio.ch.FileChannelImpl#read(java.nio.ByteBuffer)为例分析一下可能的场景：

1. A线程read，B线程中断A线程：A线程抛出ClosedByInterruptException异常
2. A，B线程read，C线程中断A线程

- A被中断时，B刚刚进入read方法：A线程抛出ClosedByInterruptException异常，B线程ensureOpen方法抛出ClosedChannelException异常
- A被中断时，B阻塞在底层read方法中：A线程抛出ClosedByInterruptException异常，B线程底层方法抛出异常返回，end方法中抛出AsynchronousCloseException异常
- A被中断时，B已经读取到数据：A线程抛出ClosedByInterruptException异常，B线程正常返回

sun.nio.ch.FileChannelImpl#read(java.nio.ByteBuffer)代码如下：

``` java
 public int read(ByteBuffer dst) throws IOException {

     ensureOpen();  // 1

     if (!readable) // 2

         throw new NonReadableChannelException();

     synchronized (positionLock) {

         int n = 0;

         int ti = -1;

         try {            

             begin();
             
             ti = threads.add();

             if (!isOpen())

                 return 0; // 3

             do {
                 n = IOUtil.read(fd, dst, -1, nd); // 4

             } while ((n == IOStatus.INTERRUPTED) && isOpen());

             return IOStatus.normalize(n);

         } finally {

             threads.remove(ti);

             end(n > 0);

             assert IOStatus.check(n);

          }

      }
  }
```



同样，对于`sun.nio.ch.ServerSocketChannelImpl`也不例外。

```java
/**
* Marks the beginning of an I/O operation that might block.
*
* @throws ClosedChannelException if the channel is closed
* @throws NotYetBoundException if the channel's socket has not been bound yet
*/
private void begin(boolean blocking) throws ClosedChannelException {
    if (blocking)
        begin();  // set blocker to close channel if interrupted
    synchronized (stateLock) {
        ensureOpen();
        if (localAddress == null)
            throw new NotYetBoundException();
        if (blocking)
            thread = NativeThread.current();
    }
}

/**
* Marks the end of an I/O operation that may have blocked.
*
* @throws AsynchronousCloseException if the channel was closed due to this
* thread being interrupted on a blocking I/O operation.
*/
private void end(boolean blocking, boolean completed)
    throws AsynchronousCloseException
    {
        if (blocking) {
            synchronized (stateLock) {
                thread = 0;
                // notify any thread waiting in implCloseSelectableChannel
                if (state == ST_CLOSING) {
                    stateLock.notifyAll();
                }
            }
            end(completed);
        }
    }


//sun.nio.ch.ServerSocketChannelImpl#accept()
@Override
public SocketChannel accept() throws IOException {
    acceptLock.lock();
    try {
        int n = 0;
        FileDescriptor newfd = new FileDescriptor();
        InetSocketAddress[] isaa = new InetSocketAddress[1];

        boolean blocking = isBlocking();
        try {
            begin(blocking);
            do {
                n = accept(this.fd, newfd, isaa);
            } while (n == IOStatus.INTERRUPTED && isOpen());
        } finally {
            end(blocking, n > 0);
            assert IOStatus.check(n);
        }

        if (n < 1)
            return null;

        // newly accepted socket is initially in blocking mode
        IOUtil.configureBlocking(newfd, true);

        InetSocketAddress isa = isaa[0];
        SocketChannel sc = new SocketChannelImpl(provider(), newfd, isa);

        // check permitted to accept connections from the remote address
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkAccept(isa.getAddress().getHostAddress(), isa.getPort());
            } catch (SecurityException x) {
                sc.close();
                throw x;
            }
        }
        return sc;

    } finally {
        acceptLock.unlock();
    }
}
```



## 总结

在JavaIO时期，人们为了中断IO操作想了不少方法，核心操作就是关闭流，促使IO操作抛出异常，达到中断IO的效果。NIO中，将这个操作植入了java.lang.Thread#interrupt方法，免去用户自己编码特定代码的麻烦。使IO操作可以像其他可中断方法一样，在中断时抛出ClosedByInterruptException异常，业务程序捕获该异常即可对IO中断做出响应。

## **参考资料**

- java – What does JavaLangAccess.blockedOn(Thread t, Interruptible b) do? – Stack Overflow

  https://stackoverflow.com/questions/8544891/what-does-javalangaccess-blockedonthread-t-interruptible-b-do

- Java NIO 那些躲在角落的细节

  https://www.oschina.net/question/138146_26027



本文来源: http://imushan.com/2018/08/01/java/language/JDK%E6%BA%90%E7%A0%81%E9%98%85%E8%AF%BB-InterruptibleChannel%E4%B8%8E%E5%8F%AF%E4%B8%AD%E6%96%ADIO/

其中的部分错误，本人已经修正