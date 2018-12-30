# 09 Java的NIO之服务端的ServerSocketChannel的使用

## ServerSocketChannel
Java的NIO中的ServerSocketChannel是一个可以监听新进来的TCP连接的通道, 就像标准IO中的ServerSocket一样。所以不难想象ServerSocketChannel是运行在服务端的。ServerSocketChannel类在java.nio.channels包中。

下面有个简单的例子：
``` java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.socket().bind(new InetSocketAddress(9999));
while(true){
    SocketChannel socketChannel =
            serverSocketChannel.accept();
    //do something with socketChannel...
}
```
## 9.1 打开ServerSocketChannel
通过调用ServerSocketChannel.open()方法来打开ServerSocketChannel.如：
``` java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
```
## 9.2 关闭ServerSocketChannel
通过调用ServerSocketChannel.close() 方法来关闭ServerSocketChannel.

## 9.3 监听新进来的连接
通过ServerSocketChannel.accept()方法监听新进来的连接。当 accept()方法返回的时候,它返回一个包含新进来的连接的SocketChannel。因此, accept()方法会一直阻塞到有新连接到达。

通常不会仅仅只监听一个连接,在while循环中调用 accept()方法. 如下面的例子：
``` java
while(true){
    SocketChannel socketChannel =
            serverSocketChannel.accept();
    //do something with socketChannel...

}
```
当然,也可以在while循环中使用除了true以外的其它退出准则。

## 9.4 非阻塞工作模式：
ServerSocketChannel可以设置成非阻塞模式。在非阻塞模式下，accept() 方法会立刻返回，如果还没有新进来的连接,返回的将是null。 因此，需要检查返回的SocketChannel是否是null.如：
``` java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

serverSocketChannel.socket().bind(new InetSocketAddress(8080));
serverSocketChannel.configureBlocking(false);

while(true){
    SocketChannel socketChannel =
            serverSocketChannel.accept();

    if(socketChannel != null){
        //do something with socketChannel...
    }
}
```