# 02 Java的NIO之Channel通道



## 1.Channel 通道的简介

java的NIO的通道类似流，但是又有一些不同： 

- 既可以从Channel中读数据也可以往Channel里面写数据；但是流的读写一般是单向的。 
- Channel可以异步的读写； 
- Channel的读写是通过Buffer这个中介实现的。数据总是要先读到一个Buffer，或者总是要从一个Buffer中写入。如下图所示：

![](https://github.com/muyinchen/woker/blob/master/mypics/overview-channels-buffers.png?raw=true)

引用一段关于描述Channel的文字：

> 其中Channel对应以前的流，Buffer不是什么新东西，Selector是因为NIO可以使用异步的非堵塞模式才加入的东西。
>
> 以前的流总是堵塞的，一个线程只要对它进行操作，其它操作就会被堵塞，也就相当于水管没有阀门，你伸手接水的时候，不管水到了没有，你就都只能耗在接水（流）上。
>
> NIO的Channel的加入，相当于增加了水龙头（有阀门），虽然一个时刻也只能接一个水管的水，但依赖轮换策略，在水量不大的时候，各个水管里流出来的水，都可以得到妥善接纳，这个关键之处就是增加了一个接水工，也就是Selector，他负责协调，也就是看哪根水管有水了的话，在当前水管的水接到一定程度的时候，就切换一下：临时关上当前水龙头，试着打开另一个水龙头（看看有没有水）。
>
> 当其他人需要用水的时候，不是直接去接水，而是事前提了一个水桶给接水工，这个水桶就是Buffer。也就是，其他人虽然也可能要等，但不会在现场等，而是回家等，可以做其它事去，水接满了，接水工会通知他们。 
> 这其实也是非常接近当前社会分工细化的现实，也是统分利用现有资源达到并发效果的一种很经济的手段，而不是动不动就来个并行处理，虽然那样是最简单的，但也是最浪费资源的方式。 
> 上面的比方还是相当清晰的描述了Channel、Buffer和Selector的作用。

## 2.Channel的最重要的几个实现以及使用场景

下面是Java的NIO的最重要的几个channel的实现： 

1. FileChannel 主要是用于文件的读写 

2. DatagramChannel 主要用于UDP读写网络中的数据 

3. SocketChannel 通过TCP读写网络中的数据。 

4. ServerSocketChannel 主要用于服务端：可以监听新进来的TCP连接，像Web服务器那样。对每一个新进来的连接都会创建一个SocketChannel。



## 3. 一个FileChannel的基本实例；

``` java
  RandomAccessFile accessFile = new RandomAccessFile("data.txt", "rw");
  FileChannel inchannel = accessFile.getChannel();//获取 FileChannel

ByteBuffer buffer = ByteBuffer.allocate(48);//申请48字节的缓冲区

int bytesRead = inchannel.read(buffer);// 读取数据放入缓冲区

while(bytesRead != -1){
    System.out.println("read " + bytesRead);
    Thread.sleep(500);
    buffer.flip();
    bytesRead=-1;
}
while(buffer.hasRemaining()){
    System.out.print((char) buffer.get());
}
buffer.clear();
bytesRead = inchannel.read(buffer);
accessFile.close();
```
