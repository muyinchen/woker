# [Java 容器源码分析之 Deque 与 ArrayDeque]()



Queue 也是 Java 集合框架中定义的一种接口，直接继承自 Collection 接口。除了基本的 Collection 接口规定测操作外，Queue 接口还定义一组针对队列的特殊操作。通常来说，Queue 是按照先进先出(FIFO)的方式来管理其中的元素的，但是优先队列是一个例外。

Deque 接口继承自 Queue接口，但 Deque 支持同时从两端添加或移除元素，因此又被成为双端队列。鉴于此，Deque 接口的实现可以被当作 FIFO队列使用，也可以当作LIFO队列（栈）来使用。官方也是推荐使用 Deque 的实现来替代 Stack。

ArrayDeque 是 Deque 接口的一种具体实现，是依赖于可变数组来实现的。ArrayDeque 没有容量限制，可根据需求自动进行扩容。ArrayDeque不支持值为 null 的元素。

下面基于JDK 8中的实现对 ArrayDeque 加以分析。

### 方法概览

```java
public interface Queue<E> extends Collection<E> {
    //向队列中插入一个元素，并返回true
    //如果队列已满，抛出IllegalStateException异常
    boolean add(E e);

    //向队列中插入一个元素，并返回true
    //如果队列已满，返回false
    boolean offer(E e);

    //取出队列头部的元素，并从队列中移除
    //队列为空，抛出NoSuchElementException异常
    E remove();

    //取出队列头部的元素，并从队列中移除
    //队列为空，返回null
    E poll();

    //取出队列头部的元素，但并不移除
    //如果队列为空，抛出NoSuchElementException异常
    E element();

    //取出队列头部的元素，但并不移除
    //队列为空，返回null
    E peek();
}
```

Deque 提供了双端的插入与移除操作，如下表：

|         |                  | First Element (Head) |                  | Last Element (Tail) |
| ------- | ---------------- | -------------------- | ---------------- | ------------------- |
|         | Throws exception | Special value        | Throws exception | Special value       |
| Insert  | addFirst(e)      | offerFirst(e)        | addLast(e)       | offerLast(e)        |
| Remove  | removeFirst()    | pollFirst()          | removeLast()     | pollLast()          |
| Examine | getFirst()       | peekFirst()          | getLast()        | peekLast()          |

Deque 和 Queue 方法的的对应关系如下：

| Queue Method | Equivalent Deque Method |
| ------------ | ----------------------- |
| add(e)       | addLast(e)              |
| offer(e)     | offerLast(e)            |
| remove()     | removeFirst()           |
| poll()       | pollFirst()             |
| element()    | getFirst()              |
| peek()       | peekFirst()             |

Deque 和 Stack 方法的对应关系如下：

| Stack Method | Equivalent Deque Method |
| ------------ | ----------------------- |
| push(e)      | addFirst(e)             |
| pop()        | removeFirst()           |
| peek()       | peekFirst()             |

ArrayList 实现了 Deque 接口中的所有方法。因为 ArrayList 会根据需求自动扩充容量，因而在插入元素的时候不会抛出IllegalStateException异常。

### 底层结构

```java
//用数组存储元素
transient Object[] elements; // non-private to simplify nested class access
//头部元素的索引
transient int head;
//尾部下一个将要被加入的元素的索引
transient int tail;
//最小容量，必须为2的幂次方
private static final int MIN_INITIAL_CAPACITY = 8;
```

在 ArrayDeque 底部是使用数组存储元素，同时还使用了两个索引来表征当前数组的状态，分别是 head 和 tail。head 是头部元素的索引，但注意 tail *不是尾部元素的索引，而是尾部元素的下一位*，即下一个将要被加入的元素的索引。

### 初始化

ArrayDeque 提供了三个构造方法，分别是默认容量，指定容量及依据给定的集合中的元素进行创建。默认容量为16。

```java
public ArrayDeque() {
    elements = new Object[16];
}

public ArrayDeque(int numElements) {
    allocateElements(numElements);
}

public ArrayDeque(Collection<? extends E> c) {
    allocateElements(c.size());
    addAll(c);
}
```

ArrayDeque 对数组的大小(即队列的容量)有特殊的要求，必须是 2^n。通过 `allocateElements`方法计算初始容量：

```java
private void allocateElements(int numElements) {
    int initialCapacity = MIN_INITIAL_CAPACITY;
    // Find the best power of two to hold elements.
    // Tests "<=" because arrays aren't kept full.
    if (numElements >= initialCapacity) {
        initialCapacity = numElements;
        initialCapacity |= (initialCapacity >>>  1);
        initialCapacity |= (initialCapacity >>>  2);
        initialCapacity |= (initialCapacity >>>  4);
        initialCapacity |= (initialCapacity >>>  8);
        initialCapacity |= (initialCapacity >>> 16);
        initialCapacity++;

        if (initialCapacity < 0)   // Too many elements, must back off
            initialCapacity >>>= 1;// Good luck allocating 2 ^ 30 elements
    }
    elements = new Object[initialCapacity];
}
```

`>>>`是无符号右移操作，`|`是位或操作，经过五次右移和位或操作可以保证得到大小为2^k-1的数。看一下这个例子：

```java
0 0 0 0 1 ? ? ? ? ?     //n
0 0 0 0 1 1 ? ? ? ?     //n |= n >>> 1;
0 0 0 0 1 1 1 1 ? ?     //n |= n >>> 2;
0 0 0 0 1 1 1 1 1 1     //n |= n >>> 4;
```

在进行5次位移操作和位或操作后就可以得到2^k-1，最后加1即可。这个实现还是很巧妙的。

### 添加元素

向末尾添加元素：

```java
public void addLast(E e) {
        if (e == null)
            throw new NullPointerException();
        //tail 中保存的是即将加入末尾的元素的索引
        elements[tail] = e;
        //tail 向后移动一位
        //把数组当作环形的，越界后到0索引
        if ( (tail = (tail + 1) & (elements.length - 1)) == head)
            //tail 和 head相遇，空间用尽，需要扩容
            doubleCapacity();
    }
```

这段代码中，`(tail = (tail + 1) & (elements.length - 1)) == head`这句有点难以理解。其实，在 ArrayDeque 中数组是当作**环形**来使用的，索引0看作是紧挨着索引(length-1)之后的。参考下面的图片：

[![array-cycle.png](https://ooo.0o0.ooo/2016/03/16/56e97c2195747.png)](https://ooo.0o0.ooo/2016/03/16/56e97c2195747.png)

那么为什么`(tail + 1) & (elements.length - 1)`就能保证按照环形取得正确的下一个索引值呢？这就和前面说到的 ArrayDeque 对容量的特殊要求有关了。下面对其正确性加以验证：

```java
length = 2^n，二进制表示为: 第 n 位为1，低位 (n-1位) 全为0 
length - 1 = 2^n-1，二进制表示为：低位(n-1位)全为1

如果 tail + 1 <= length - 1，则位与后低 (n-1) 位保持不变，高位全为0
如果 tail + 1 = length，则位与后低 n 全为0，高位也全为0，结果为 0
```

可见，在容量保证为 2^n 的情况下，仅仅通过位与操作就可以完成*环形*索引的计算，而不需要进行边界的判断，在实现上更为高效。

向头部添加元素的代码如下：

```java
public void addFirst(E e) {
    if (e == null) //不支持值为null的元素
        throw new NullPointerException();
    elements[head = (head - 1) & (elements.length - 1)] = e;
    if (head == tail)
        doubleCapacity();
}
```

其它的诸如add，offer，offerFirst，offerLast等方法都是基于上面这两个方法实现的，不再赘述。

### 扩容

在每次添加元素后，如果头索引和尾部索引相遇，则说明数组空间已满，需要进行扩容操作。 ArrayDeque 每次扩容都会在原有的容量上翻倍，这也是对容量必须是2的幂次方的保证。

[![array-cycle-copy.PNG](https://ooo.0o0.ooo/2016/03/16/56e97f8c98931.png)](https://ooo.0o0.ooo/2016/03/16/56e97f8c98931.png)

```java
private void doubleCapacity() {
    assert head == tail; //扩容时头部索引和尾部索引肯定相等
    int p = head;
    int n = elements.length;
    //头部索引到数组末端(length-1处)共有多少元素
    int r = n - p; // number of elements to the right of p
    //容量翻倍
    int newCapacity = n << 1;
    //容量过大，溢出了
    if (newCapacity < 0)
        throw new IllegalStateException("Sorry, deque too big");
    //分配新空间
    Object[] a = new Object[newCapacity];
    //复制头部索引到数组末端的元素到新数组的头部
    System.arraycopy(elements, p, a, 0, r);
    //复制其余元素
    System.arraycopy(elements, 0, a, r, p);
    elements = a;
    //重置头尾索引
    head = 0;
    tail = n;
}

```

### 移除元素

ArrayDeque支持从头尾两端移除元素，remove方法是通过poll来实现的。因为是基于数组的，在了解了环的原理后这段代码就比较容易理解了。

```java
public E pollFirst() {
    int h = head;
    @SuppressWarnings("unchecked")
    E result = (E) elements[h];
    // Element is null if deque empty
    if (result == null)
        return null;
    elements[h] = null;     // Must null out slot
    head = (h + 1) & (elements.length - 1);
    return result;
}

public E pollLast() {
    int t = (tail - 1) & (elements.length - 1);
    @SuppressWarnings("unchecked")
    E result = (E) elements[t];
    if (result == null)
        return null;
    elements[t] = null;
    tail = t;
    return result;
}

```

### 获取队头和队尾的元素

```java
@SuppressWarnings("unchecked")
public E peekFirst() {
    // elements[head] is null if deque empty
    return (E) elements[head];
}

@SuppressWarnings("unchecked")
public E peekLast() {
    return (E) elements[(tail - 1) & (elements.length - 1)];
}
```

### 迭代器

ArrayDeque 在迭代是检查并发修改并没有使用类似于 ArrayList 等容器中使用的 modCount，而是通过尾部索引的来确定的。具体参考 next 方法中的注释。但是这样不一定能保证检测到所有的并发修改情况，加入先移除了尾部元素，又添加了一个尾部元素，这种情况下迭代器是没法检测出来的。

```java
private class DeqIterator implements Iterator<E> {
    /**
     * Index of element to be returned by subsequent call to next.
     */
    private int cursor = head;

    /**
     * Tail recorded at construction (also in remove), to stop
     * iterator and also to check for comodification.
     */
    private int fence = tail;

    /**
     * Index of element returned by most recent call to next.
     * Reset to -1 if element is deleted by a call to remove.
     */
    private int lastRet = -1;

    public boolean hasNext() {
        return cursor != fence;
    }

    public E next() {
        if (cursor == fence)
            throw new NoSuchElementException();
        @SuppressWarnings("unchecked")
        E result = (E) elements[cursor];
        // This check doesn't catch all possible comodifications,
        // but does catch the ones that corrupt traversal
        // 如果移除了尾部元素，会导致tail != fence
        // 如果移除了头部元素，会导致 result == null
        if (tail != fence || result == null)
            throw new ConcurrentModificationException();
        lastRet = cursor;
        cursor = (cursor + 1) & (elements.length - 1);
        return result;
    }

    public void remove() {
        if (lastRet < 0)
            throw new IllegalStateException();
        if (delete(lastRet)) { // if left-shifted, undo increment in next()
            cursor = (cursor - 1) & (elements.length - 1);
            fence = tail;
        }
        lastRet = -1;
    }

    public void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        Object[] a = elements;
        int m = a.length - 1, f = fence, i = cursor;
        cursor = f;
        while (i != f) {
            @SuppressWarnings("unchecked") E e = (E)a[i];
            i = (i + 1) & m;
            if (e == null)
                throw new ConcurrentModificationException();
            action.accept(e);
        }
    }
}
```

除了 DeqIterator，还有一个反向的迭代器 DescendingIterator，顺序和 DeqIterator 相反。

### 小结

ArrayDeque 是 Deque 接口的一种具体实现，是依赖于可变数组来实现的。ArrayDeque 没有容量限制，可根据需求自动进行扩容。ArrayDeque 可以作为栈来使用，效率要高于 Stack；ArrayDeque 也可以作为队列来使用，效率相较于基于双向链表的 LinkedList 也要更好一些。注意，ArrayDeque 不支持为 null 的元素。



文章来源:https://www.cnblogs.com/wxd0108/p/7366234.html