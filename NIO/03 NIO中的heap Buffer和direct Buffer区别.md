# 03 NIO中的heap Buffer和direct Buffer区别

## heap buffer 和 direct buffer区别

在Java的NIO中，我们一般采用ByteBuffer缓冲区来传输数据，一般情况下我们创建Buffer对象是通过ByteBuffer的两个静态方法：

```java

ByteBuffer.allocate(int capacity);
ByteBuffer.wrap(byte[] array);

```

查看JDK的NIO的源代码关于这两个部分：

```java
/**allocate()函数的源码**/
public static ByteBuffer allocate(int capacity) {
if (capacity < 0)
    throw new IllegalArgumentException();
return new HeapByteBuffer(capacity, capacity);
}

/**wrap()函数的源码**/
public static ByteBuffer wrap(byte[] array) {
   return wrap(array, 0, array.length);
}
//
public static ByteBuffer wrap(byte[] array,
                                int offset, int length)
{
    try {
        return new HeapByteBuffer(array, offset, length);
    } catch (IllegalArgumentException x) {
        throw new IndexOutOfBoundsException();
    }
}

```
我们可以很清楚的发现，这两个方法都是实例化HeapByteBuffer来创建的ByteBuffer对象，也就是heap buffer. 其实除了heap buffer以外还有一种buffer，叫做direct buffer。我们也可以创建这一种buffer，通过ByteBuffer.allocateDirect(int capacity)方法，查看JDK源码如下：

``` java
public static ByteBuffer allocateDirect(int capacity) {
    return new DirectByteBuffer(capacity);
}
```
我们发现该函数调用的是DirectByteBuffer(capacity)这个类，这个类就是创建了direct buffer。

## 那么heap buffer和direct buffer有什么区别呢？

首先解释一下两者的区别：

heap buffer这种缓冲区是分配在堆上面的，直接由Java虚拟机负责垃圾回收，可以直接想象成一个字节数组的包装类。

direct buffer则是通过JNI在Java的虚拟机外的内存中分配了一块缓冲区(所以即使在运行时通过-Xmx指定了Java虚拟机的最大堆内存，还是可以实例化超出该大小的Direct ByteBuffer),该块并不直接由Java虚拟机负责垃圾回收收集，但是在direct buffer包装类被回收时，会通过Java Reference机制来释放该内存块。(但Direct Buffer的JAVA对象是归GC管理的，只要GC回收了它的JAVA对象，操作系统才会释放Direct Buffer所申请的空间)

两者各有优劣势:direct buffer对比 heap buffer:

劣势：创建和释放Direct Buffer的代价比Heap Buffer得要高；

优势：当我们把一个Direct Buffer写入Channel的时候，就好比是“内核缓冲区”的内容直接写入了Channel，这样显然快了，减少了数据拷贝（因为我们平时的read/write都是需要在I/O设备与应用程序空间之间的“内核缓冲区”中转一下的）。而当我们把一个Heap Buffer写入Channel的时候，实际上底层实现会先构建一个临时的Direct Buffer，然后把Heap Buffer的内容复制到这个临时的Direct Buffer上，再把这个Direct Buffer写出去。当然，如果我们多次调用write方法，把一个Heap Buffer写入Channel，底层实现可以重复使用临时的Direct Buffer，这样不至于因为频繁地创建和销毁Direct Buffer影响性能。

结论：Direct Buffer创建和销毁的代价很高，所以要用在尽可能重用的地方。 比如周期长传输文件大采用direct buffer，不然一般情况下就直接用heap buffer 就好。
