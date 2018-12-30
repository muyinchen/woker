# 04 NIO中Buffer缓冲区的实现

## Buffer 缓冲区
Java的NIO中Buffer至关重要：buffer是读写的中介，主要和NIO的通道交互。数据是通过通道读入缓冲区和从缓冲区写入通道的。

其实缓冲区buffer的本质就是一块可以读写的内存块。这块内存块被包装成NIO的Buffer对象，并提供了一组方法方便读写。

## 1 Buffer的基本用法：
使用Buffer读写数据一般是下面步骤： 

1. 写入数据到Buffer 
2. 调用flip()方法：Buffer从写模式切换到读模式。 
3. 从buffer读取数据 
4. 调用clear()方法或则compact()方法。

当向buffer写入数据时，buffer会记录下写了多少数据。一旦要读取数据，需要通过flip()方法将Buffer从写模式切换到读模式。在读模式下，可以读取之前写入到buffer的所有数据。

一旦读完了所有的数据，就需要清空缓冲区，让它可以再次被写入。有两种方式能清空缓冲区：调用clear()或compact()方法。clear()方法会清空整个缓冲区。compact()方法只会清除已经读过的数据。任何未读的数据都被移到缓冲区的起始处，新写入的数据将放到缓冲区未读数据的后面。

下面是一个简单的示例：
``` java
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();

//create buffer with capacity of 48 bytes
ByteBuffer buf = ByteBuffer.allocate(48);

int bytesRead = inChannel.read(buf); //read into buffer.
while (bytesRead != -1) {

  buf.flip();  //make buffer ready for read

  while(buf.hasRemaining()){
      System.out.print((char) buf.get()); // read 1 byte at a time
  }

  buf.clear(); //make buffer ready for writing
  bytesRead = inChannel.read(buf);
}
aFile.close();
```
## 2 Buffer中的三个重要变量capacity、position、limit
在JDK中NIO的源码中Buffer的基类Buffer有三个变量如下：
``` java
 	private int position = 0;
    private int limit;
    private int capacity;
```
缓冲区本质是一块可以读写的内存块；为了理解Buffer的工作原理，我们需要深入的理解这三个变量。

position和limit的含义取决于Buffer处在读模式还是写模式。不管Buffer处在什么模式，capacity的含义总是一样的。

这里有一个关于capacity，position和limit在读写模式中的说明，详细的解释在下面的插图后面： 
![](https://github.com/muyinchen/woker/blob/master/mypics/buffers-modes.png?raw=true)



### capacity 

也就是缓冲区的容量大小。我们只能往里面写capacity个byte、long，char等类型。一旦Buffer满了，需要将其清空（通过读数据或者清除数据）才能继续写数据往里写数据。

### position 

（1）当我们写数据到Buffer中时，position表示当前的位置。初始的position值为0.当一个byte、long等数据写到Buffer后， position会向前移动到下一个可插入数据的Buffer单元。position最大可为capacity – 1.

（2）当读取数据时，也是从某个特定位置读。当将Buffer从写模式切换到读模式，position会被重置为0. 当从Buffer的position处读取数据时，position向前移动到下一个可读的位置。

### limit 

（1）在写模式下，Buffer的limit表示你最多能往Buffer里写多少数据。 写模式下，limit等于Buffer的capacity。

（2）读模式时，limit表示你最多能读到多少数据。因此，当切换Buffer到读模式时，limit会被设置成写模式下的position值。换句话说，你能读到之前写入的所有数据（limit被设置成已写数据的数量，这个值在写模式下就是position）
### 解析JDK源码 

此外，我们以IntBuffer为例，讲解：查看源码： 
先看构造函数：
``` java
public static IntBuffer allocate(int var0) {
    if(var0 < 0) {
        throw new IllegalArgumentException();
    } else {
        return new HeapIntBuffer(var0, var0);
    }
}
```
看到上面你的源码可知：构造函数是包级别的，而且HeapIntBuffer是继承自IntBuffer，在HeapIntBuffer构造函数中是实例化父类对象。我们查看IntBuffer这个类的源码，发现：
``` java
final int[] hb;
final int offset;
boolean isReadOnly;
```
从上面的源码可知，上面的buffer是基于数组实现的。

## 3 Buffer的类型：
Java的NIO中有以下一些Buffer类型： 
- ByteBuffer 
- CharBuffer 
- DoubleBuffer 
- FloatBuffer 
- IntBuffer 
- LongBuffer 
- ShortBuffer

从上我们可以猜出，不同的Buffer表示存储不同的数据类型，也就是说可以通过char、short、int….double等类型来操作缓冲区。

## 4 Buffer的分配
要想获得一个Buffer对象首先应该进行分配，每个Buffer对象都有一个静态方法allocate()，该方法分配对应buffer对象的缓冲区大小。

下面是一个分配 100个字节 的capacity的ByteBuffer的示例：
``` Java
ByteBuffer buffer = ByteBuffer.allocate(100);
```
下面示例2是一个分配 1024个字符 的CharBuffer的示例：
``` Java
CharBuffer buffer = CharBuffer.allocate(1024);
```
## 5 向Buffer中写入数据：
写数据到Buffer有两种： 
1. 从Channel中获取数据写入Buffer 
2. 通过Buffer的put()方法写到Buffer中

下面通过示例来解释这两种方法： 
通过channel写数据到buffer中：

``` Java
int bytesRead = inChannel.read(buffer); //读取通道里面的数据写入buffer中,并返回写入的数据对的字节数
```

通过put()方法来写入Buffer的例子：


``` Java
buffer.put(127);
```
put方法有很多版本，允许你以不同的方式把数据写入到Buffer中。例如， 写到一个指定的位置，或者把一个字节数组写到Buffer。

## 6 flip()方法
flip方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值。

换句话说，position现在用于标记读的位置，limit表示之前写进了多少个byte、char等，现在也只能读取多少个byte、char等。

## 7 从Buffer中读取数据
从Buffer中读取数据也是有两种： 

1. 从Buffer读取数据到Channel。 
2. 使用get()方法从Buffer中读取数据。

下面通过示例来解释这两种方法： 
从Buffer读取数据到Channel：

```java
int bytesWritten = inChannel.write(buffer);
```
使用get()方法从Buffer中读取数据的例子：

``` Java
byte aByte = buffer.get(127);
```
get方法有很多版本，允许你以不同的方式从Buffer中读取数据。例如，从指定position读取，或者从Buffer中读取数据到字节数组。
``` Java
int bytesWritten = inChannel.write(buffer);
```
使用get()方法从Buffer中读取数据的例子：

## 8 rewind()方法
Buffer.rewind()将position设回0，所以你可以重读Buffer中的所有数据。limit保持不变，仍然表示能从Buffer中读取多少个元素（byte、char等）。

## 9 clear()与compact()方法
一旦读完Buffer中的数据(例如把buffer中的数据传入通道中)，需要让Buffer准备好再次被写入。可以通过clear()或compact()方法来完成。

如果调用的是clear()方法，position将被设回0，limit被设置成capacity的值。换句话说，Buffer 被清空了。实际上Buffer中的数据并未清除，只是这些标记告诉我们可以从哪里开始往Buffer里写数据。

如果Buffer中有一些未读的数据，调用clear()方法，数据将“被遗忘”，意味着不再有任何标记会告诉你哪些数据被读过，哪些还没有。

如果Buffer中仍有未读的数据，且后续还需要这些数据，但是此时想要先先写些数据，那么使用compact()方法。

compact()方法将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。limit属性依然像clear()方法一样，设置成capacity。现在Buffer准备好写数据了，但是不会覆盖未读的数据。

## 10 mark()与reset()方法；这两个方法成对使用；
通过调用Buffer.mark()方法，可以标记Buffer中的一个特定position。之后可以通过调用Buffer.reset()方法恢复到这个position。例如：
``` Java
buffer.mark();
//call buffer.get() a couple of times, e.g. during parsing.
buffer.reset();  //set position back to mark.
```