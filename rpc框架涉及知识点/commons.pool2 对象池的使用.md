# commons.pool2 对象池的使用

> 摘要: `commons.pool2` 对象池的使用

`commons.pool2` 对象池的使用

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.3</version>
</dependency>
```

# 池对象工厂 PooledObjectFactory和池对象 DefaultPooledObject

先了解个概念：

**\*池对象工厂(`PooledObjectFactory`接口)***:用来创建池对象, 将不用的池对象进行钝化(`passivateObject`), 对要使用的池对象进行激活(`activeObject`), 对池对象进行验证(`validateObject`), 对有问题的池对象进行销毁(`destroyObject`)等工作

`PooledObjectFactory`是一个池化对象工厂接口，定义了生成对象、激活对象、钝化对象、销毁对象的方法，如下

```java
public interface PooledObjectFactory<T> {
    PooledObject<T> makeObject() throws Exception;

    void destroyObject(PooledObject<T> var1) throws Exception;

    boolean validateObject(PooledObject<T> var1);

    void activateObject(PooledObject<T> var1) throws Exception;

    void passivateObject(PooledObject<T> var1) throws Exception;
}
```

如果需要使用Commons-Pool，那么你就需要提供一个`PooledObjectFactory`接口的具体实现。一个比较简单的办法就是，继承`BasePooledObjectFactory`这个抽象类。而继承这个抽象类，只需要实现两个方法:create()和wrap(T obj)。

实现create()方法很简单，而实现wrap(T obj)也有捷径，可以使用类**\*`DefaultPooledObject`***，代码可以参考如下：

```java
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by liyanxin on 2015/3/25.
 */
public class PooledStringBufferFactory extends BasePooledObjectFactory<StringBuffer> {

    @Override
    public StringBuffer create() throws Exception {
        return new StringBuffer(16);
    }

    @Override
    public PooledObject<StringBuffer> wrap(StringBuffer stringBuffer) {
        return new DefaultPooledObject<StringBuffer>(stringBuffer);
    }
}
```

当然还有其他的池对象工厂，如`KeyedPooledObjectFactory`，和`PooledObjectFactory`接口类似，只是在相关的方法中多了Key参数，如下，

```java
public interface KeyedPooledObjectFactory<K,V> {
   
    PooledObject<V> makeObject(K key) throws Exception;
  
    void destroyObject(K key, PooledObject<V> p) throws Exception;

    boolean validateObject(K key, PooledObject<V> p);
   
    void activateObject(K key, PooledObject<V> p) throws Exception;

    void passivateObject(K key, PooledObject<V> p) throws Exception;
}
```

# 创建对象池 `GenericObjectPool`

在`org.apache.commons.pool2.impl`中预设了三个可以直接使用的对象池：`GenericObjectPool`、`GenericKeyedObjectPool`和`SoftReferenceObjectPool`。

**通常使用`GenericObjectPool`来创建对象池，如果是对象池是Keyed的，那么可以使用`GenericKeyedObjectPool`来创建对象池。**这两个类都提供了丰富的配置选项。这两个对象池的**\*特点是可以设置对象池中的对象特征，包括LIFO（后进先出）方式、最大空闲数、最小空闲数、是否有效性检查等等。***两者的区别如前面所述，后者支持Keyed。

而`SoftReferenceObjectPool`对象池，它利用一个`java.util.ArrayList`对象来保存对象池里的对象。不过它并不在对象池里直接保存对象本身，而是保存它们的“软引用”（Soft Reference）。这种对象池的特色是：可以保存任意多个对象，不会有容量已满的情况发生；在对象池已空的时候，调用它的`borrowObject`方法，会自动返回新创建的实例;可以在初始化同时，在池内预先创建一定量的对象;当内存不足的时候，池中的对象可以被Java虚拟机回收。

举个例子:

```java
new GenericObjectPool<StringBuffer>(new PooledStringBufferFactory());
```

我们也可以使用`GenericObjectPoolConfig`来对上面创建的对象池进行一些参数配置，创建的`Config`参数，可以使用`setConfig`方法传给对象池，也可以在对象池的构造方法中作为参数传入。

举个例子：

```java
GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
conf.setMaxTotal(20);
conf.setMaxIdle(10);
...
GenericObjectPool<StringBuffer> pool = new GenericObjectPool<StringBuffer>(new PooledStringBufferFactory(), conf);
```

# 使用对象池

对象池使用起来很方便，简单一点就是使用`borrowObject`和`returnObject`两个方法，直接给参考代码吧：

`borrowObject`部分代码，具体请看源码：**\*借出池对象，通过新建create()或从idleObjects双端队列中返回池对象。*****\*idleObjects是一个双端队列，保存返回对象池的对象，下次用的时候按照LIFO的原则（或其他原则）借出对象。***

```java
public T borrowObject(long borrowMaxWaitMillis) throws Exception {
    PooledObject<T> p = null;

    while (p == null) {
        create = false;
        if (blockWhenExhausted) {
            p = idleObjects.pollFirst();
            if (p == null) {
                p = create();
                if (p != null) {
                    create = true;
                }
            }
            if (p == null) {
                if (borrowMaxWaitMillis < 0) {
                    p = idleObjects.takeFirst();
                } else {
                    p = idleObjects.pollFirst(borrowMaxWaitMillis,
                            TimeUnit.MILLISECONDS);
                }
            }
            if (p == null) {
                throw new NoSuchElementException(
                        "Timeout waiting for idle object");
            }
            if (!p.allocate()) {
                p = null;
            }
        } else {
            p = idleObjects.pollFirst();
            if (p == null) {
                p = create();
                if (p != null) {
                    create = true;
                }
            }
        }
    }
    return p.getObject();
}
```

`returnObject`方法，部分代码，具体请看源码：**\*返回池对象，放入idleObjects双端队列保存。***

关键代码：

```java
public void returnObject(T obj) {
    PooledObject<T> p = allObjects.get(obj);

    int maxIdleSave = getMaxIdle();
    if (isClosed() || maxIdleSave > -1 && maxIdleSave <= idleObjects.size()) {
        try {
            destroy(p);
        } catch (Exception e) {
            swallowException(e);
        }
    } else {
        if (getLifo()) {
            idleObjects.addFirst(p);
        } else {
            idleObjects.addLast(p);
        }
    }
    updateStatsReturn(activeTime);
}
```

这只是大体上的逻辑，还有更多的细节逻辑控制。比如什么时候销毁，什么时候创建等等。

==============END==============