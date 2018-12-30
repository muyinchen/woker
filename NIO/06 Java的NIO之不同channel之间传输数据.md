# 06 Java的NIO之不同channel之间传输数据

## 不同通道channel之间传输数据
在Java的NIO中，如果两个通道中有一个是FileChannel，那么我们可以直接将数据从一个channel传输到另外一个channel中。两个通道之间传输数据的方式有两种，分别是： 
- transferFrom() 
- transferTo()

## 6.1 transferFrom()
FileChannel 的transferFrom()方法可以将数据从源通道传输到FileChannel中（这个方法在JDK文档中解释为将字节从给定的可读取字节通道传输到此通道的文件中）。下面是一个简单的例子：
``` java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel fromChannel = fromFile.getChannel();//获取source的通道;

RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
FileChannel toChannel = toFile.getChannel();//获取dest的通道;

long position = 0;
long count = fromChannel.size();

long result = toChannel.transferFrom(fromChannel, position, count);

LogUtil.log_debug(""+result);
```
方法的输入参数 
- position表示从position处开始向目标文件写入数据， 
- count表示最多传输的字节数。如果源通道的剩余空间小于count个字节，则所传输的字节数要小于请求的字节数。

此外要注意，在SoketChannel的实现中，SocketChannel只会传输此刻准备好的数据（可能不足count字节）。因此，SocketChannel可能不会将请求的所有数据(count个字节)全部传输到FileChannel中。

## 6.2 transferTo()方法
transferTo()方法将数据从FileChannel传输到其他的channel中。下面是一个简单的例子：
``` java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel fromChannel = fromFile.getChannel();//获取source的通道;

RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
FileChannel toChannel = toFile.getChannel();//获取dest的通道;

long position = 0;
long count = fromChannel.size();
long result = fromChannel.transferTo(position, count, toChannel);
LogUtil.log_debug(""+result);
```
观察一下就可以发现，其实这两个例子其实非常的相似，如果仅仅从文件IO的角度来看，就是两个文件之间的数据复制；

上面所说的关于SocketChannel的问题在transferTo()方法中同样存在。SocketChannel会一直传输数据直到目标buffer被填满。
