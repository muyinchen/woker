## BIO与NIO区别以及例子

**简介：**

BIO：同步阻塞式IO，服务器实现模式为一个连接一个线程，即客户端有连接请求时服务器端就需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的线程开销，当然可以通过线程池机制改善。 
NIO：同步非阻塞式IO，服务器实现模式为一个请求一个线程，即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有I/O请求时才启动一个线程进行处理。 
AIO(NIO.2)：异步非阻塞式IO，服务器实现模式为一个有效请求一个线程，客户端的I/O请求都是由OS先完成了再通知服务器应用去启动线程进行处理。 

**BIO** 
同步阻塞式IO，相信每一个学习过操作系统网络编程或者任何语言的网络编程的人都很熟悉，在while循环中服务端会调用accept方法等待接收客户端的连接请求，一旦接收到一个连接请求，就可以建立通信套接字在这个通信套接字上进行读写操作，此时不能再接收其他客户端连接请求，只能等待同当前连接的客户端的操作执行完成。 
如果BIO要能够同时处理多个客户端请求，就必须使用多线程，即每次accept阻塞等待来自客户端请求，一旦受到连接请求就建立通信套接字同时开启一个新的线程来处理这个套接字的数据读写请求，然后立刻又继续accept等待其他客户端连接请求，即为每一个客户端连接请求都创建一个线程来单独处理，大概原理图就像这样： 
![img](http://static.oschina.net/uploads/img/201510/23094528_ZQyy.jpg) 

虽然此时服务器具备了高并发能力，即能够同时处理多个客户端请求了，但是却带来了一个问题，随着开启的线程数目增多，将会消耗过多的内存资源，导致服务器变慢甚至崩溃，NIO可以一定程度解决这个问题。


**NIO** 
同步非阻塞式IO，关键是采用了事件驱动的思想来实现了一个多路转换器。 
NIO与BIO最大的区别就是只需要开启一个线程就可以处理来自多个客户端的IO事件，这是怎么做到的呢？ 
就是多路复用器，可以监听来自多个客户端的IO事件： 
A. 若服务端监听到客户端连接请求，便为其建立通信套接字(java中就是通道)，然后返回继续监听，若同时有多个客户端连接请求到来也可以全部收到，依次为它们都建立通信套接字。 
B. 若服务端监听到来自已经创建了通信套接字的客户端发送来的数据，就会调用对应接口处理接收到的数据，若同时有多个客户端发来数据也可以依次进行处理。 
C. 监听多个客户端的连接请求和接收数据请求同时还能监听自己时候有数据要发送。 
![img](http://static.oschina.net/uploads/img/201510/23094528_OF9c.jpg) 

总之就是在一个线程中就可以调用多路复用接口（java中是select）阻塞同时监听来自多个客户端的IO请求，一旦有收到IO请求就调用对应函数处理。 

**各自应用场景** :

到这里你也许已经发现，一旦有请求到来(不管是几个同时到还是只有一个到)，都会调用对应IO处理函数处理，所以：

（1）NIO适合处理连接数目特别多，但是连接比较短（轻操作）的场景，Jetty，Mina，ZooKeeper等都是基于java nio实现。

（2）BIO方式适用于连接数目比较小且固定的场景，这种方式对服务器资源要求比较高，并发局限于应用中。

**NIO例子**:

**服务器端**

```java
package cn.com;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NServer
{

	// 用于检测所有的Channel状态的selector
	// (在一个线程中就可以调用多路复用接口（java中是select）阻塞同时监听来自多个客户端的IO请求，
	// 一旦有收到IO请求就调用对应函数处理)
	private Selector selector = null;
	static final int PORT = 30000;
	// 定义实现编码、解码的字符串集对象
	private Charset charse = Charset.forName("GBK");

	public void init() throws IOException
	{
		//获得一个通道管理器   
		selector = Selector.open();
		//通过open方法来获得一个ServerSocket通道  
		ServerSocketChannel server = ServerSocketChannel.open();
		//设置serverSocket已非阻塞方式工作
		server.configureBlocking(false);
		
		InetSocketAddress isa = new InetSocketAddress("127.0.0.1", PORT);
		//将该通道对应的ServerSocket绑定到port端口   
		server.socket().bind(isa);
		
		//将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，    
		//当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。   
		server.register(selector, SelectionKey.OP_ACCEPT);
		
		//采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理 
		//轮询访问selector
		//当注册的事件到达时，方法返回；否则,该方法会一直阻塞 
		while(selector.select() > 0)  
		{
		    // 遍历selector中选中的项，选中的项为注册的事件 
			for(SelectionKey selectionKey : selector.selectedKeys())
			{
				// 从selector上已选择的Key集合中删除正在处理的SelectionKey, 删除已选的key,以防重复处理    
				selector.selectedKeys().remove(selectionKey);
				
				// 如果sk对应的Channel包含客户端的连接请求事件
				if(selectionKey.isAcceptable())
				{
					// 调用accept方法接收连接，产生服务器段的SocketChennal
					SocketChannel socketChannel = server.accept();
					// 设置采用非阻塞模式
					socketChannel.configureBlocking(false);
					// 将该SocketChannel注册到selector
					socketChannel.register(selector, SelectionKey.OP_READ);
				}
				// 如果sk对应的Channel可读事件
				if(selectionKey.isReadable())
				{
					// 获取该SelectionKey对应的Channel，该Channel中有可读的数据
					SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
					// 定义备注执行读取数据源的ByteBuffer
					ByteBuffer buff = ByteBuffer.allocate(1024);
					String content = "";
					// 开始读取数据
					try
					{
						while(socketChannel.read(buff) > 0)
						{
							buff.flip();
							content += charse.decode(buff);
						}
						System.out.println("读取的数据：" + content);
						// 将sk对应的Channel设置成准备下一次读取
						selectionKey.interestOps(SelectionKey.OP_READ);
					}
					// 如果捕获到该sk对银行的Channel出现了异常，表明
					// Channel对应的Client出现了问题，所以从Selector中取消
					catch(IOException io)
					{
						// 从Selector中删除指定的SelectionKey
						selectionKey.cancel();
						if(selectionKey.channel() != null)
						{
							selectionKey.channel().close();
						}
					}
					// 如果content的长度大于0,则连天信息不为空
					if(content.length() > 0)
					{
						// 遍历selector里注册的所有SelectionKey
						for(SelectionKey key : selector.keys())
						{
							// 获取该key对应的Channel
							Channel targerChannel = key.channel();
							// 如果该Channel是SocketChannel对象
							if(targerChannel instanceof SocketChannel)
							{
								// 将读取到的内容写入该Channel中
								SocketChannel dest = (SocketChannel) targerChannel;
								dest.write(charse.encode(content));
							}
						}
					}
				}
			}
		}

	}

	public static void main(String[] args) throws IOException
	{
		new NServer().init();
	}

}
```

**客户端：**

```java
package cn.com;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NClient
{

	// 定义检测Sockethannel的Selector对象
	private Selector selector = null;
	static final int PORT = 30000;
	// 定义处理编码的字符集
	private Charset charset = Charset.forName("GBK");
	// 客户端SocketChannel
	private SocketChannel socketChannel = null;

	public void init() throws IOException
	{
		// 获得一个通道管理器 
		selector = Selector.open();
		InetSocketAddress isa = new InetSocketAddress("127.0.0.1", PORT);
		// 获得一个Socket通道 
		socketChannel = SocketChannel.open(isa);
		 // 设置通道为非阻塞
		socketChannel.configureBlocking(false);
		// 将SocketChannel对象注册到指定的Selector
		socketChannel.register(selector, SelectionKey.OP_READ);
		// 启动读取服务器数据端的线程
		new ClientThread().start();
		// 创建键盘输入流
		Scanner scan = new Scanner(System.in);
		while(scan.hasNextLine())
		{
			// 读取键盘的输入
			String line = scan.nextLine();
			// 将键盘的内容输出到SocketChanenel中
			socketChannel.write(charset.encode(line));
		}
	}

	// 定义读取服务器端的数据的线程
	private class ClientThread extends Thread
	{

		@Override
		public void run()
		{
			try
			{
				while(selector.select() > 0)
				{
					// 遍历每个有可能的IO操作的Channel对银行的SelectionKey
					for(SelectionKey tmpSelectionKey : selector.selectedKeys())
					{
						// 删除正在处理的SelectionKey
						selector.selectedKeys().remove(tmpSelectionKey);
						// 如果该SelectionKey对应的Channel中有可读的数据
						if(tmpSelectionKey.isReadable())
						{
							// 使用NIO读取Channel中的数据
							SocketChannel socketChannel = (SocketChannel) tmpSelectionKey.channel();
							String content = "";
							ByteBuffer bff = ByteBuffer.allocate(1024);
							while(socketChannel.read(bff) > 0)
							{
								socketChannel.read(bff);
								bff.flip();
								content += charset.decode(bff);
							}
							// 打印读取的内容
							System.out.println("聊天信息:" + content);
							tmpSelectionKey.interestOps(SelectionKey.OP_READ);

						}
					}
				}

			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
		}

	}

	public static void main(String[] args) throws IOException
	{
		new NClient().init();
	}
}
```





#  





# 以Java的视角来聊聊BIO、NIO与AIO的区别？





# BIO（Blocking I/O）同步阻塞I/O

这是最基本与简单的I/O操作方式，其根本特性是做完一件事再去做另一件事，一件事一定要等前一件事做完，这很符合程序员传统的顺序来开发思想，因此BIO模型程序开发起来较为简单，易于把握。

但是BIO如果需要同时做很多事情（例如同时读很多文件，处理很多tcp请求等），就需要系统创建很多线程来完成对应的工作，因为BIO模型下一个线程同时只能做一个工作，如果线程在执行过程中依赖于需要等待的资源，那么该线程会长期处于阻塞状态，我们知道在整个操作系统中，线程是系统执行的基本单位，在BIO模型下的线程 阻塞就会导致系统线程的切换，从而对整个系统性能造成一定的影响。当然如果我们只需要创建少量可控的线程，那么采用BIO模型也是很好的选择，但如果在需要考虑高并发的web或者tcp服务器中采用BIO模型就无法应对了，如果系统开辟成千上万的线程，那么CPU的执行时机都会浪费在线程的切换中，使得线程的执行效率大大降低。此外，关于线程这里说一句题外话，在系统开发中线程的生命周期一定要准确控制，在需要一定规模并发的情形下，尽量使用线程池来确保线程创建数目在一个合理的范围之内，切莫编写线程数量创建上限的代码。





# NIO (New I/O) 同步非阻塞I/O

关于NIO，国内有很多技术博客将英文翻译成No-Blocking I/O，非阻塞I/O模型 ，当然这样就与BIO形成了鲜明的特性对比。NIO本身是基于事件驱动的思想来实现的，其目的就是解决BIO的大并发问题，在BIO模型中，如果需要并发处理多个I/O请求，那就需要多线程来支持，NIO使用了多路复用器机制，以socket使用来说，多路复用器通过不断轮询各个连接的状态，只有在socket有流可读或者可写时，应用程序才需要去处理它，在线程的使用上，就不需要一个连接就必须使用一个处理线程了，而是只是有效请求时（确实需要进行I/O处理时），才会使用一个线程去处理，这样就避免了BIO模型下大量线程处于阻塞等待状态的情景。

相对于BIO的流，NIO抽象出了新的通道（Channel）作为输入输出的通道，并且提供了缓存（Buffer）的支持，在进行读操作时，需要使用Buffer分配空间，然后将数据从Channel中读入Buffer中，对于Channel的写操作，也需要现将数据写入Buffer，然后将Buffer写入Channel中。

如下是NIO方式进行文件拷贝操作的示例，见下图：

![以Java的视角来聊聊BIO、NIO与AIO的区别？](https://static.oschina.net/uploads/img/201708/15225835_zZ5l.jpg)

通过比较New IO的使用方式我们可以发现，新的IO操作不再面向 Stream来进行操作了，改为了通道Channel，并且使用了更加灵活的缓存区类Buffer，Buffer只是缓存区定义接口， 根据需要，我们可以选择对应类型的缓存区实现类。在java NIO编程中，我们需要理解以下3个对象Channel、Buffer和Selector。

- **Channel**

首先说一下Channel，国内大多翻译成“通道”。Channel和IO中的Stream(流)是差不多一个等级的。只不过Stream是单向的，譬如：InputStream, OutputStream。而Channel是双向的，既可以用来进行读操作，又可以用来进行写操作，NIO中的Channel的主要实现有：FileChannel、DatagramChannel、SocketChannel、ServerSocketChannel；通过看名字就可以猜出个所以然来：分别可以对应文件IO、UDP和TCP（Server和Client）。

- **Buffer**

NIO中的关键Buffer实现有：ByteBuffer、CharBuffer、DoubleBuffer、 FloatBuffer、IntBuffer、 LongBuffer,、ShortBuffer，分别对应基本数据类型: byte、char、double、 float、int、 long、 short。当然NIO中还有MappedByteBuffer, HeapByteBuffer, DirectByteBuffer等这里先不具体陈述其用法细节。

说一下 DirectByteBuffer 与 HeapByteBuffer 的区别？

它们 ByteBuffer 分配内存的两种方式。HeapByteBuffer 顾名思义其内存空间在 JVM 的 heap（堆）上分配，可以看做是 jdk 对于 byte[] 数组的封装；而 DirectByteBuffer 则直接利用了系统接口进行内存申请，其内存分配在c heap 中，这样就减少了内存之间的拷贝操作，如此一来，在使用 DirectByteBuffer 时，系统就可以直接从内存将数据写入到 Channel 中，而无需进行 Java 堆的内存申请，复制等操作，提高了性能。既然如此，为什么不直接使用 DirectByteBuffer，还要来个 HeapByteBuffer？原因在于， DirectByteBuffer 是通过full gc来回收内存的，DirectByteBuffer会自己检测情况而调用 system.gc()，但是如果参数中使用了 DisableExplicitGC 那么就无法回收该快内存了，-XX:+DisableExplicitGC标志自动将 System.gc() 调用转换成一个空操作，就是应用中调用 System.gc() 会变成一个空操作，那么如果设置了就需要我们手动来回收内存了，所以DirectByteBuffer使用起来相对于完全托管于 java 内存管理的Heap ByteBuffer 来说更复杂一些，如果用不好可能会引起OOM。Direct ByteBuffer 的内存大小受 -XX:MaxDirectMemorySize JVM 参数控制（默认大小64M），在 DirectByteBuffer 申请内存空间达到该设置大小后，会触发 Full GC。

- **Selector**

Selector 是NIO相对于BIO实现多路复用的基础，Selector 运行单线程处理多个 Channel，如果你的应用打开了多个通道，但每个连接的流量都很低，使用 Selector 就会很方便。例如在一个聊天服务器中。要使用 Selector , 得向 Selector 注册 Channel，然后调用它的 select() 方法。这个方法会一直阻塞到某个注册的通道有事件就绪。一旦这个方法返回，线程就可以处理这些事件，事件的例子有如新的连接进来、数据接收等。

这里我们再来看一个NIO模型下的TCP服务器的实现，我们可以看到Selector 正是NIO模型下 TCP Server 实现IO复用的关键，请仔细理解下段代码while循环中的逻辑，见下图：

![以Java的视角来聊聊BIO、NIO与AIO的区别？](https://static.oschina.net/uploads/img/201708/15225835_ZY5a.jpg)





# AIO (Asynchronous I/O) 异步非阻塞I/O

Java AIO就是Java作为对异步IO提供支持的NIO.2 ，Java NIO2 (JSR 203)定义了更多的 New I/O APIs， 提案2003提出，直到2011年才发布， 最终在JDK 7中才实现。JSR 203除了提供更多的文件系统操作API(包括可插拔的自定义的文件系统)， 还提供了对socket和文件的异步 I/O操作。 同时实现了JSR-51提案中的socket channel全部功能,包括对绑定， option配置的支持以及多播multicast的实现。

从编程模式上来看AIO相对于NIO的区别在于，NIO需要使用者线程不停的轮询IO对象，来确定是否有数据准备好可以读了，而AIO则是在数据准备好之后，才会通知数据使用者，这样使用者就不需要不停地轮询了。当然AIO的异步特性并不是Java实现的伪异步，而是使用了系统底层API的支持，在Unix系统下，采用了epoll IO模型，而windows便是使用了IOCP模型。关于Java AIO，本篇只做一个抛砖引玉的介绍，如果你在实际工作中用到了，那么可以参考Netty在高并发下使用AIO的相关技术。





# 总 结

IO实质上与线程没有太多的关系，但是不同的IO模型改变了应用程序使用线程的方式，NIO与BIO的出现解决了很多BIO无法解决的并发问题，当然任何技术抛开适用场景都是耍流氓，复杂的技术往往是为了解决简单技术无法解决的问题而设计的，在系统开发中能用常规技术解决的问题，绝不用复杂技术，否则大大增加系统代码的维护难度，学习IT技术不是为了炫技，而是要实实在在解决问题。

攻破JAVA NIO技术壁垒： http://www.importnew.com/19816.html

 

 

https://www.zhihu.com/question/39851782

read的文档说明大致是：如果因已到达流末尾而没有可用的字节，则返回值 -1。在输入数据可用、检测到流的末尾或者抛出异常前，此方法一直阻塞。
socket和文件不一样，从文件中读，读到末尾就到达流的结尾了，所以会返回-1或null，循环结束，但是socket是连接两个主机的桥梁，一端无法知道另一端到底还有没有数据要传输。
socket如果不关闭的话，read之类的阻塞函数会一直等待它发送数据，就是所谓的阻塞。
如果发送的东西非常多必须要用循环读的话，可以有一下解决方案：

- 调用socket的shutdownOutput方法关闭输出流，该方法的文档说明为，将此套接字的输出流置于“流的末尾”，这样另一端的输入流上的read操作就会返回-1。
- 约定结束标志，当读到该结束标志时退出不再read。
- 设置超时，会在设置的超时时间到达后抛出SocketTimeoutException异常而不再阻塞。
- 双方定义好通信协议，在协议头部约定好数据的长度。当读取到的长度等于这个长度时就不再继续调用read方法。

```java
package cn.com.paic.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketService
{
	public void oneServer() throws IOException
	{
		ServerSocket serviceSocket = new ServerSocket(5209);

		// 获取socket。这个方法是阻塞式的

		Socket socket = serviceSocket.accept();

		InputStream inputStream = socket.getInputStream();

		byte buf[] = new byte[1024];

		int len = 0;

		// 向客户端生成响应

		OutputStream outputStream = socket.getOutputStream();

		int i = 0;
		while((len = inputStream.read(buf)) > 0)
		{
			String recvStr = new String(buf, 0, len);
			System.out.println(recvStr);
			if(i == 10 && recvStr.equals("#end#"))
			{
				break;
			}
			i++;
		}

		outputStream.write("收到".getBytes());

	}

	// 搭建服务器端
	public static void main(String[] args) throws IOException
	{
		ServerSocketService socketService = new ServerSocketService();
		// 1、a)创建一个服务器端Socket，即SocketService
		socketService.oneServer();
	}
}
package cn.com.paic.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

public class SocketClient
{
	// 搭建客户端
	public static void main(String[] args) throws IOException
	{
		try
		{
			// 1、创建客户端Socket，指定服务器地址和端口
			// Socket socket=new Socket("127.0.0.1",5200);
			Socket socket = new Socket("127.0.0.1", 5209);
			System.out.println("客户端启动成功");

			/*
			 * InputStream inputStream = socket.getInputStream();
			 * 
			 * byte buf[] = new byte[1024];
			 * 
			 * int len = 0;
			 * 
			 * len = inputStream.read(buf);
			 */

			// 打印客户端的消息

			// System.out.println(new String(buf, 0, len));

			// 向客户端生成响应

			OutputStream outputStream = socket.getOutputStream();

			int i = 0;

			while(true)
			{
				String str = "客户端发送" + i + "消息啦==" + UUID.randomUUID().toString();
				outputStream.write(str.getBytes());
				outputStream.write("#end#".getBytes());
				Thread.currentThread().sleep(500);
				System.out.println("客户端第" + i + "次发送消息！！！");
				i++;
			}

		}
		catch(Exception e)
		{
			System.out.println("can not listen to:" + e);// 出错，打印出错信息
		}
	}

}
```

 