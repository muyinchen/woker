# 01 Java的NIO概述



## NIO的核心组成部分

1. Channel
2. Buffers
3. Selectors

## Channel和Buffer

基本上，所有的IO在NIO中都从一个Channel开始。Channel有点象流。数据可以从Channel读到Buffer中，也可以从Buffer写到Channel中。这里有个图示： 

![](https://github.com/muyinchen/woker/blob/master/mypics/overview-channels-buffers.png?raw=true)


Channel和Buffer有好几种类型。下面是JAVA NIO中的一些主要Channel的实现： 
- FileChannel 
- DatagramChannel 
- SocketChannel 
- ServerSocketChannel

正如你所看到的，这些通道涵盖了UDP 和 TCP 网络IO，以及文件IO。 


以下是Java NIO里关键的Buffer实现：

- ByteBuffer
- CharBuffer
- DoubleBuffer
- FloatBuffer
- IntBuffer
- LongBuffer
- ShortBuffer

这些Buffer覆盖了你能通过IO发送的基本数据类型：byte, short, int, long, float, double 和 char。

Java NIO 还有个 MappedByteBuffer，用于表示内存映射文件.

## Selector
Selector允许单线程处理多个 Channel。如果你的应用打开了多个连接（通道），但每个连接的流量都很低，使用Selector就会很方便。例如，在一个聊天服务器中。

这是在一个单线程中使用一个Selector处理3个Channel的图示： 

![](https://github.com/muyinchen/woker/blob/master/mypics/overview-selectors.png?raw=true)


要使用Selector，得向Selector注册Channel，然后调用它的select()方法。这个方法会一直阻塞到某个注册的通道有事件就绪。一旦这个方法返回，线程就可以处理这些事件，事件的例子有如新连接进来，数据接收等。

