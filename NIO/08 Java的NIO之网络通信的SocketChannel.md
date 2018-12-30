# 08 Java的NIO之网络通信的SocketChannel

Java的NIO中的SocketChannel是一个连接到TCP网络套接字的通道(SocketChannel这个通道一般用于客户端，服务端一般使用ServerSocketChannel)。可以通过以下2种方式创建SocketChannel： 
1. 打开一个SocketChannel并连接到互联网的某台服务器上。 
2. 一个新的连接到达ServerSocketChannel时，server端会创建一个SocketChannel。

下面分节讲解一下SocketChannel的用法：

## 8.1 打开SocketChannel：
下面是SocketChanel打打开方式：
``` java
SocketChannel socketChannel = SocketChannel.open();
//这里必须填写域名才能正确返回
boolean result = socketChannel.connect(new InetSocketAddress("120.25.12.92", 80));
LogUtil.log_debug(""+result);
socketChannel.close();
///~output
// true
```
## 8.2 关闭SocketChannel
我们要养成良好的习惯，用完了通道之后一定要记得关闭，释放资源。关闭SocketChannel通过调用SocketChannel.close()实现。

## 8.3 从SocketChannel中读取数据
要从SocketChannel中读取数据，调用一个read()方法，下面是一个示例。
``` java
ByteBuffer buffer = ByteBuffer.allocate(1024);
int bytesRead = socketChannel.read(buffer);
```
上面的代码首先分配了一个Buffer，然后调用SocketChannel.read()方法从channel中读取到的数据存放进这个Buffer中。read()方法的返回值表示读了多少字节进Buffer里。如果返回的是-1，表示已经读到了流的末尾（连接关闭了）。

## 8.4 写入SocketChannel
写数据到SocketChannel用的是SocketChannel.write()方法，该方法以一个Buffer作为参数。示例如下：
``` java
String newData = "New String to write to file..." + System.currentTimeMillis();

ByteBuffer buffer = ByteBuffer.allocate(48);
buffer.clear();
buffer.put(newData.getBytes());

buffer.flip();//把buffer切换到读模式，将buffer中数据读出来然后写入通道

while(buffer.hasRemaining()) {
    channel.write(buffer);
}
```
## 8.5 非阻塞模式
可以设置 SocketChannel 为非阻塞模式（non-blocking mode）.设置之后，就可以在异步模式下调用connect(), read() 和write()了。

###  connect() 

如果SocketChannel在非阻塞模式下，此时调用connect()，该方法可能在连接建立之前就返回了。为了确定连接是否建立，可以调用finishConnect()的方法。像这样：
``` java
socketChannel.configureBlocking(false);
socketChannel.connect(new InetSocketAddress("120.25.12.92", 80));

while(! socketChannel.finishConnect() ){
    //wait, or do something else...
}
```
### write() 

非阻塞模式下，write()方法在尚未写出任何内容时可能就返回了。所以需要在循环中调用write()。前面已经有例子了，这里就不赘述了。

### read() 

非阻塞模式下,read()方法在尚未读取到任何数据时可能就返回了。所以需要关注它的int返回值，它会告诉你读取了多少字节。

## 8.6 非阻塞模式与选择器
非阻塞模式与选择器搭配会工作的更好，通过将一或多个SocketChannel注册到Selector，可以询问选择器哪个通道已经准备好了读取，写入等
