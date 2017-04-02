# CopyOnWriteArrayList的详解



# 一 CopyOnWriteArrayList与JMM

说明：本文代码均以JDK1.8的源码为准。

## 1 什么是CopyOnWriteArrayList

关于CopyOnWriteArrayList是什么以及基本用法，在这里不多说，网上可以搜到大量这方面的文章。在这里只做简要说明：CopyOnWriteArrayList相当于线程安全的ArrayList，是一个可变数组。它具有如下特性：

- 是线程安全的
- 写操作会复制整个基础数组，因此写操作开销很大
- 适用于如下情况：数组大小较小，并且读操作比写操作多很多的情形

## 2 CopyOnWriteArrayList的设计原理与JMM

下面我们分析CopyOnWriteArrayList的设计原理，结合JMM的基础知识，分析CopyOnWriteArrayList是如何保证线程安全的。

首先看用来实际保存数据的数组：

```java
/** The array, accessed only via getArray/setArray. */
private transient volatile Object[] array;
```

可以看到array数组前面使用了volatile变量来修饰。volatile主要用来解决内存可见性问题。关于volatile的详细实现原理可以参考《[深入理解java内存模型.pdf](http://o8sltkx20.bkt.clouddn.com/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3Java%E5%86%85%E5%AD%98%E6%A8%A1%E5%9E%8B.pdf)》以及[Java并发编程：volatile关键字解析-博客园-海子](http://www.cnblogs.com/dolphin0520/p/3920373.html)。

### 2.1 CopyOnWriteArrayList的读方法

读方法比较简单，直接从array中获取对应索引的值。

```java
/**
 * {@inheritDoc}
 *
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E get(int index) {
    return get(getArray(), index);
}

@SuppressWarnings("unchecked")
private E get(Object[] a, int index) {
    return (E) a[index];
}

/**
 * Gets the array.  Non-private so as to also be accessible
 * from CopyOnWriteArraySet class.
 */
final Object[] getArray() {
    return array;
}
```

### 2.2 CopyOnWriteArrayList的写方法

- set方法
  源码如下：

```java
/**
 * Replaces the element at the specified position in this list with the
 * specified element.
 *
 * @throws IndexOutOfBoundsException {@inheritDoc}
 */
public E set(int index, E element) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        E oldValue = get(elements, index);

        if (oldValue != element) {
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len);
            newElements[index] = element;
            setArray(newElements);
        } else {
            // Not quite a no-op; ensures volatile write semantics
            setArray(elements);
        }
        return oldValue;
    } finally {
        lock.unlock();
    }
}

/**
 * Sets the array.
 */
final void setArray(Object[] a) {
    array = a;
}
```

set方法的功能是将对应索引的元素置为一个新值。执行流程：
（1）加锁
（2）获取对应索引已有的值
（3）比较已有的值和新值，如果不相等，转4，否则转5
（4）创建新的数组，复制原数组的元素，并将对应索引置为新值。然后将新数组赋给array（setArray）
（5）setArray-将array赋给array

这里有一个比较奇怪的点，为什么已有的值和新值相等的时候，还要执行setArray呢？本质上setArray也没有做什么事情。

这段代码混合使用了锁以及volatile。锁的用法比较容易理解，它在使用同一个锁的不同线程之间保证内存顺序性，代码结尾的释放锁的操作提供了本线程和其他欲获取相同的锁的线程之间的happens-before语义。但是CopyOnWriteArrayList类中其他代码，不一定会使用到这把锁，因此，前面所述的锁带来的内存模型含义对这部分代码执行是不适用的。

其他没用到这把锁的代码，读写是volatile读和volatile写（因为array前面使用volatile关键字修饰）。由volatile来保证happens-before语义。

------

**volatile的特性及原理**

volatile 变量自身具有下列特性:
（1）可见性。对一个volatile 变量的读,总是能看到(任意线程)对这个volatile变量最后的写入。
（2）原子性:对任意单个volatile 变量的读/写具有原子性,但类似于volatile++这种复合操作不具有原子性。

volatile 写和锁的释放有相同的内存语义。

为了实现 volatile 的内存语义,编译器在生成字节码时,会在指令序列中插入内存屏障来禁止特定类型的处理器重排序。

------

这里调用setArray的原因是，确保set方法对array执行的永远是volatile写。这就和其他对array执行volatile读的线程之间建立了happens-before语义。非常重要的一点：volatile读/写语义针对的是读写操作，而不是使用volatile修饰的变量本身。这样说更直白一点：在一个volatile写操作之前的对其他非volatile变量的写，happens-before于同一个volatile变量读操作之后的对其他变量的读。这句话比较绕，看下面一个例子就比较易懂了。

```java
// initial conditions
int nonVolatileField = 0;
CopyOnWriteArrayList<String> list = /* a single String */

// Thread 1
nonVolatileField = 1;                 // (1)
list.set(0, "x");                     // (2)

// Thread 2
String s = list.get(0);               // (3)
if (s == "x") {
    int localVar = nonVolatileField;  // (4)
}
```

现在假设原始数组中无元素“x”，这样(2)成功设置了元素”x”，(3)处可以成功获取到元素”x”。这种情况下，(4)一定会读取到(1)处设置的值1.因为(2)处的volatile写以及在此之前的任何写操作都happens-before(3)处的读以及之后的所有读。

但是，假设一开始数组中就有了元素”x”，如果else不调用setArray，那么(2)处的写就不是volatile写，(4)处的读就不一定能读到(1)处设置的值！

很显然我们不想让内存可见性依赖于list中已有的值，为了确保任何情况下的内存可见性，set方法必须永远都是一个volatile写，这就是为何要在else代码块中调用setArray的原因。

其他写方法（add、remove）比较易懂，在此不详述。

## 3 参考资料

- [Java多线程系列–“JUC集合”02之 CopyOnWriteArrayList](http://www.cnblogs.com/skywang12345/p/3498483.html)
- [Why setArray() method call required in CopyOnWriteArrayList](http://stackoverflow.com/questions/28772539/why-setarray-method-call-required-in-copyonwritearraylist#)
- [深入理解java内存模型.pdf](http://o8sltkx20.bkt.clouddn.com/%E6%B7%B1%E5%85%A5%E7%90%86%E8%A7%A3Java%E5%86%85%E5%AD%98%E6%A8%A1%E5%9E%8B.pdf)
- [Java并发编程：volatile关键字解析-博客园-海子](http://www.cnblogs.com/dolphin0520/p/3920373.html)





# 二 为什么Java HashMap、CopyOnWriteArrayList等集合自己实现readObject和writeObject方法

PS:本文源码参考的是JDK 1.8.

## 1 readObject、writeObject方法是什么？作用是什么？

当一个class实现了Serializable接口，那么意味着这个类可以被序列化。如果类不实现readObject、writeObject方法，那么会执行默认的序列化和反序列化逻辑，否则执行自定义的序列化和反序列化逻辑，即readObject、writeObject方法的逻辑。

JDK提供的对于Java对象序列化操作的类是ObjectOutputStream，反序列化的类是ObjectInputStream。下面我们来看序列化的实现（ObjectOutputStream.writeObject）。

```
/**
 * Write the specified object to the ObjectOutputStream.  The class of the
 * object, the signature of the class, and the values of the non-transient
 * and non-static fields of the class and all of its supertypes are
 * written.  Default serialization for a class can be overridden using the
 * writeObject and the readObject methods.  Objects referenced by this
 * object are written transitively so that a complete equivalent graph of
 * objects can be reconstructed by an ObjectInputStream.
 *
 * <p>Exceptions are thrown for problems with the OutputStream and for
 * classes that should not be serialized.  All exceptions are fatal to the
 * OutputStream, which is left in an indeterminate state, and it is up to
 * the caller to ignore or recover the stream state.
 *
 * @throws  InvalidClassException Something is wrong with a class used by
 *          serialization.
 * @throws  NotSerializableException Some object to be serialized does not
 *          implement the java.io.Serializable interface.
 * @throws  IOException Any exception thrown by the underlying
 *          OutputStream.
 */
public final void writeObject(Object obj) throws IOException {
    if (enableOverride) {
        writeObjectOverride(obj);
        return;
    }
    try {
        writeObject0(obj, false);
    } catch (IOException ex) {
        if (depth == 0) {
            writeFatalException(ex);
        }
        throw ex;
    }
}
```

从方法注释可以看到，此方法正是执行了将对象序列化的操作。并且默认的序列化机制可以通过重写readObject、writeObject方法实现。实际调用的方法writeObject0最终会调到writeSerialData：

```
/**
 * Writes instance data for each serializable class of given object, from
 * superclass to subclass.
 */
private void writeSerialData(Object obj, ObjectStreamClass desc)
    throws IOException
{
    ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
    for (int i = 0; i < slots.length; i++) {
        ObjectStreamClass slotDesc = slots[i].desc;
        //如果类重写了writeObject方法
        if (slotDesc.hasWriteObjectMethod()) {
            PutFieldImpl oldPut = curPut;
            curPut = null;
            SerialCallbackContext oldContext = curContext;

            if (extendedDebugInfo) {
                debugInfoStack.push(
                    "custom writeObject data (class \"" +
                    slotDesc.getName() + "\")");
            }
            try {
                curContext = new SerialCallbackContext(obj, slotDesc);
                bout.setBlockDataMode(true);
                //调用实现类自己的writeobject方法
                slotDesc.invokeWriteObject(obj, this);
                bout.setBlockDataMode(false);
                bout.writeByte(TC_ENDBLOCKDATA);
            } finally {
                curContext.setUsed();
                curContext = oldContext;
                if (extendedDebugInfo) {
                    debugInfoStack.pop();
                }
            }

            curPut = oldPut;
        } else {
            defaultWriteFields(obj, slotDesc);
        }
    }
}
```

## 2 为什么是private方法？

javadoc上没有明确说明声明为private的原因，一个可能的原因是，除了子类以外没有其他类会使用它，这样不会被滥用。

另一个原因是，不希望这些方法被子类override。每个类都可以有自己的writeObject方法，序列化引擎会逐一调用。readObject相同。

## 3 HashMap中对readObject、writeObject方法的实现

### 3.1 为什么HashMap要自定义序列化逻辑

下文是摘自《Effective Java》：

*For example, consider the case of a hash table. The physical representation is a sequence of hash buckets containing key-value entries. The bucket that an entry resides in is a function of the hash code of its key, which is not, in general, guaranteed to be the same from JVM implementation to JVM implementation. In fact, it isn’t even guaranteed to be the same from run to run. Therefore, accepting the default serialized form for a hash table would constitute a serious bug. Serializing and deserializing the hash table could yield an object whose invariants were seriously corrupt.*

大概意思是：对于同一个key，在不同的JVM平台上计算出来的hash值可能不同，导致的结果就是，同一个hashmap反序列化之后和序列化之前不同，导致同一个key取出来的值不同。

### 3.2 HashMap是如何解决的

- 将可能造成数据不一致的元素使用transient修饰，在序列化的时候忽略这些元素：
  *Entry[] tablesizemodCount*
- HashMap中对writeObject的实现：

```java
/**
 * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
 * serialize it).
 *
 * @serialData The <i>capacity</i> of the HashMap (the length of the
 *             bucket array) is emitted (int), followed by the
 *             <i>size</i> (an int, the number of key-value
 *             mappings), followed by the key (Object) and value (Object)
 *             for each key-value mapping.  The key-value mappings are
 *             emitted in no particular order.
 */
private void writeObject(java.io.ObjectOutputStream s)
    throws IOException {
    int buckets = capacity();
    // Write out the threshold, loadfactor, and any hidden stuff
    s.defaultWriteObject();
    s.writeInt(buckets);
    s.writeInt(size);
    internalWriteEntries(s);
}

// Called only from writeObject, to ensure compatible ordering.
void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
    Node<K,V>[] tab;
    if (size > 0 && (tab = table) != null) {
        for (int i = 0; i < tab.length; ++i) {
            for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                s.writeObject(e.key);
                s.writeObject(e.value);
            }
        }
    }
}
```

HashMap不会将保存数据的数组序列化，而是将元素个数以及每个元素的key、value序列化。而在反序列化的时候，重新计算，填充hashmap：

readObject的实现：

```java
/**
 * Reconstitute the {@code HashMap} instance from a stream (i.e.,
 * deserialize it).
 */
private void readObject(java.io.ObjectInputStream s)
    throws IOException, ClassNotFoundException {
    // Read in the threshold (ignored), loadfactor, and any hidden stuff
    s.defaultReadObject();
    reinitialize();
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new InvalidObjectException("Illegal load factor: " +
                                         loadFactor);
    s.readInt();                // Read and ignore number of buckets
    int mappings = s.readInt(); // Read number of mappings (size)
    if (mappings < 0)
        throw new InvalidObjectException("Illegal mappings count: " +
                                         mappings);
    else if (mappings > 0) { // (if zero, use defaults)
        // Size the table using given load factor only if within
        // range of 0.25...4.0
        float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
        float fc = (float)mappings / lf + 1.0f;
        int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                   DEFAULT_INITIAL_CAPACITY :
                   (fc >= MAXIMUM_CAPACITY) ?
                   MAXIMUM_CAPACITY :
                   tableSizeFor((int)fc));
        float ft = (float)cap * lf;
        threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                     (int)ft : Integer.MAX_VALUE);
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
        table = tab;

        // Read the keys and values, and put the mappings in the HashMap
        for (int i = 0; i < mappings; i++) {
            @SuppressWarnings("unchecked")
                K key = (K) s.readObject();
            @SuppressWarnings("unchecked")
                V value = (V) s.readObject();
            putVal(hash(key), key, value, false, false);
        }
    }
}
```

这样就避免了反序列化之后根据Key获取到的元素与序列化之前获取到的元素不同。

## 4 为什么CopyOnWriteArrayList也需要自定义序列化逻辑？

writeObject、readObject实现：

```java
/**
  * Saves this list to a stream (that is, serializes it).
  *
  * @param s the stream
  * @throws java.io.IOException if an I/O error occurs
  * @serialData The length of the array backing the list is emitted
  *               (int), followed by all of its elements (each an Object)
  *               in the proper order.
  */
 private void writeObject(java.io.ObjectOutputStream s)
     throws java.io.IOException {

     s.defaultWriteObject();

     Object[] elements = getArray();
     // Write out array length
     s.writeInt(elements.length);

     // Write out all elements in the proper order.
     for (Object element : elements)
         s.writeObject(element);
 }

 /**
  * Reconstitutes this list from a stream (that is, deserializes it).
  * @param s the stream
  * @throws ClassNotFoundException if the class of a serialized object
  *         could not be found
  * @throws java.io.IOException if an I/O error occurs
  */
 private void readObject(java.io.ObjectInputStream s)
     throws java.io.IOException, ClassNotFoundException {

     s.defaultReadObject();

     // bind to new lock
     resetLock();

     // Read in array length and allocate array
     int len = s.readInt();
     Object[] elements = new Object[len];

     // Read in all elements in the proper order.
     for (int i = 0; i < len; i++)
         elements[i] = s.readObject();
     setArray(elements);
 }
```

而数组被声明为transient：

```java
/** The array, accessed only via getArray/setArray. */
private transient volatile Object[] array;
```

可以看出其逻辑和ArrayList相同：是将数组长度以及所有元素序列化，在反序列化的时候新建数组，填充元素。

如果采用默认的序列化机制会有如下问题：存储数据的数组实际上是动态数组，每次在放满以后自动增长设定的长度值，如果数组自动增长长度设为100，而实际只放了一个元素，那就会序列化很多null元素，所以ArrayList把元素数组设置为transient。

## 5 参考资料

- [Why are readObject and writeObject private, and why would I write transient variables explicitly?](http://stackoverflow.com/questions/7467313/why-are-readobject-and-writeobject-private-and-why-would-i-write-transient-vari)
- [http://www.a-site.cn/article/140346.html](http://www.a-site.cn/article/140346.html)