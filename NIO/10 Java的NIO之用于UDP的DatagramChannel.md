# 10 Java的NIO之用于UDP的DatagramChannel
Java NIO中的DatagramChannel是一个能 收发 UDP包的通道。因为UDP是无连接的网络协议，所以不能像其它通道那样读取和写入。它发送和接收的是数据包。

## 10.1 打开 DatagramChannel
下面是打开DatagramChannel的方式：
``` java
DatagramChannel channel = DatagramChannel.open();
channel.socket().bind(new InetSocketAddress(8000));
```
这个例子打开的 DatagramChannel可以在UDP端口8000上接收数据包。

## 10.2 接收数据
通过receive()方法从DatagramChannel接收数据，如：
``` java
ByteBuffer buffer = ByteBuffer.allocate(100);
buffer.clear();
channel.receive(buffer);
```
receive()方法会将接收到的数据包内容复制到指定的Buffer. 如果Buffer容不下收到的数据，多出的数据将被丢弃。

## 10.3 发送数据
通过send()方法从DatagramChannel发送数据，如:
``` java
String newData = "New String to write to file..." + System.currentTimeMillis();

ByteBuffer buffer = ByteBuffer.allocate(100);
buffer.clear();
buffer.put(newData.getBytes());
buffer.flip();//将buffer切换到读模式

int bytesSent = channel.send(buffer, new InetSocketAddress("120.25.12.92", 80));
```
这个例子发送一串字符到”120.25.12.92”服务器的UDP端口80。 因为服务端并没有监控这个端口，所以什么也不会发生。也不会通知你发出的数据包是否已收到，因为UDP在数据传送方面没有任何保证。

## 10.4 将DatagramChannel通道连接到特定的地址
可以将DatagramChannel“连接”到网络中的特定地址的。由于UDP是无连接的，连接到特定地址并不会像TCP通道那样创建一个真正的连接。而是锁住DatagramChannel ，让其只能从特定地址收发数据。

下面是个示例：
``` java
channel.connect(new InetSocketAddress("120.25.12.92", 80));
```
当连接后，也可以使用read()和write()方法，就像在用传统的通道一样。只是在数据传送方面没有任何保证。这里有几个例子：
``` java
int bytesRead = channel.read(buffer);
int bytesWritten = channel.write(but);
```