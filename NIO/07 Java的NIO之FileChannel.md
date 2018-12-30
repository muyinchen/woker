# 07 Java的NIO之FileChannel

## FileChannel文件通道
Java的NIO中的FileChannel是一个连接到文件的通道，文件可以通过这个通道来进行读写操作。

值得注意的是：FileChannel无法设置成非阻塞模式，它总是工作在阻塞模式下。

下面介绍一下FileChannel的用法：

## 7.1 打开一个FileChannel
在使用FileChannel之前我们必须先打开一个FileChannel；但是我们无法直接打开一个FileChannel，必须通过使用一个InputStream、OutputStream或则是RandomAccessFile来获取到FileChannel实例。我们在前面的内容中,我们已经多次用到了通过RandomAccessFile对象来获取FileChannel实例，下面再次重复的给出一个示例，加强理解;
``` java
RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
FileChannel fromChannel = fromFile.getChannel();//获取source的通道;
```
## 7.2 从FileChannel通道中读取数据
通过在channel上面调用read()方法，就可以从file channel中读取数据，比如如下例子所示：
``` java
ByteBuffer buffer = ByteBuffer.allocate(100);
int bytesRead = inChannel.read(buffer);
```
上面的代码：首先分配一个100个字节的buffer，然后调用read()方法从channel中读取数据放入buffer中。read()方法将返回的int值表示有多少个字节被读入到了Buffer中。如果返回-1就表示到了文件末尾。

## 7.3 向FileChannel中写入数据：
使用FileChannel的write()方法向FileChannel中写数据，该方法的参数也是一个buffer。
``` java
String newData = "New String to write to file..." + System.currentTimeMillis();

ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());

buf.flip();

while(buf.hasRemaining()) {
    channel.write(buf);
}
```
注意FileChannel.write()是在while循环中调用的。因为无法保证write()方法一次能向FileChannel写入多少字节，因此需要重复调用write()方法，直到Buffer中已经没有尚未写入通道的字节。

## 7.4 关闭FileChannel
用完了FileChannel后必须将其关闭：
``` java
channel.close();
```
## 7.5 FileChannel的position方法：
有时候我们可能需要对FileChannle中特定的位置进行读写操作，这时我们首先要获取FileChannel的位置，position()函数可以返回FileChannel的当前位置：此外，我们也可以调用position(long pos)来设置FileChannel的当前位置。

下面通过两个例子来说明：
``` java
long pos = channel.position();
channel.position(pos +123);
```
这里如果将position设置在了文件结束符之后，然后从文件通道里面读数据，read()方法将返回-1，标志文件结束；

这里如果将position设置在了文件结束符之后，然后向文件通道写数据，文件将撑大到当前位置并写入数据。这可能导致“文件空洞”，磁盘上物理文件中写入的数据间有空隙。

## 7.6 FileChannel的size()方法
FileChannel实例的size()方法将返回该实例所关联文件的大小。使用如下例：
``` java
long fileSize = channel.size();
```
## 7.7 FileChannel的truncate方法截取文件：
可以使用FileChannel.truncate()方法截取一个文件。截取文件时，文件指定长度后面的部分将被删除。如：
``` java
channel.truncate(1024);
```
这个例子截取文件的前1024个字节。

## 7.8 FileChannel的force方法
FileChannel.force()方法将通道里尚未写入磁盘的数据强制写到磁盘上。出于性能方面的考虑，操作系统会将数据缓存在内存中，所以无法保证写入到FileChannel里的数据一定会即时写到磁盘上。要保证这一点，需要调用force()方法。

force()方法有一个boolean类型的参数，指明是否同时将文件元数据（权限信息等）写到磁盘上。

下面的例子同时将文件数据和元数据强制写到磁盘上：
``` java
channel.force(true);
```