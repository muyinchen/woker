# 读书笔记之《Java并发编程的艺术》-java中的锁



本书前三章分别为

1. 并发编程的挑战，也就是并发编程的缘由所在
2. 底层的实现原理
3. java内存模型

分别从cpu x86，x64以及内存模型等概念中描述java对并发编程的实现和控制，概念较为底层和基础，读书笔记略过前三章直接从第四章应用实现及原理基础开始。

## 章节

1. 并发编程基础
2. **java中的锁**
3. 并发容器和框架（重点）
4. 13个操作原子类
5. java并发工具类
6. 线程池
7. Execurot框架

## 内容

### java中的锁

#### Lock接口

Lock接口出现之前，java是通过synchronized关键字实现的锁功能，javase5之后，并发包新增了Lock接口

Lock使用方式，和分布式锁的构造很像。

```java
Lock lock = new ReentrantLock
lock.lock();
try{
}finally{
 lock.unlock();
}
```

Lock接口提供了Synchronized关键字不具备的特性

| 尝试非阻塞地获取锁 | 当前线程尝试获取锁，没有其他线程获取锁，则成功 |
| --------- | ----------------------- |
| 能被中断的获取锁  |                         |
| 超时获取锁     | 在指定的时间内获取锁              |

Lock接口的API

| void lock()                              |                                          |
| ---------------------------------------- | ---------------------------------------- |
| void lockInterruptibly() throws InterruptedException |                                          |
| boolean tryLock()                        |                                          |
| boolean tryLock(long time,TimeUtil unit) throws InterruptedException |                                          |
| void unlock()                            |                                          |
| Condition newCondition                   | 获取等待通知组件，该组件和当前的锁绑定，当前线程只有获取了锁，才能调用该组件的wait（）方法，而调用后，当前线程将会释放锁 |

#### 队列同步器

锁的实现基于队列同步器完成，AbstractQueuedSynchronized（简称同步器），使用一个int成员变量表示同步状态，通过内置的FIFO队列来完成资源获取线程的排队工作

```java
public class ReentrantLock implements Lock, java.io.Serializable {
    private static final long serialVersionUID = 7373984872572414699L;
    /** Synchronizer providing all implementation mechanics */
    private final Sync sync;

    /**
     * Base of synchronization control for this lock. Subclassed
     * into fair and nonfair versions below. Uses AQS state to
     * represent the number of holds on the lock.
     在这里！！！
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * Performs {@link Lock#lock}. The main reason for subclassing
         * is to allow fast path for nonfair version.
         */
        abstract void lock();

        /**
         * Performs non-fair tryLock.  tryAcquire is
         * implemented in subclasses, but both need nonfair
         * try for trylock method.
         */
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        // Methods relayed from outer class

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        final boolean isLocked() {
            return getState() != 0;
        }

        /**
         * Reconstitutes this lock instance from a stream.
         * @param s the stream
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }
    }

    /**
     * Sync object for non-fair locks
     非公平锁
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * Performs lock.  Try immediate barge, backing up to normal
         * acquire on failure.
         */
        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * Sync object for fair locks
     公平锁
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
```

#### 重入锁 ReetrentLock

支持重进入的锁，能够支持一个线程对资源的重复加锁，代码见上面。

#### 读写锁 ReetrentReadWriteLock

特性

| 公平性选择 |      |
| ----- | ---- |
| 重进入   |      |
| 锁降级   |      |

接口示例

| int getReadLockCount()  | 读锁被或许的次数    |
| ----------------------- | ----------- |
| int getReadHoldCount()  | 当前线程或许读锁的次数 |
| int getWriteLockCount() |             |
| int getWriteHoldCount() |             |

通过Cache来解释读写锁，HashMap是非线程安全的，通过读写锁实现Cache的线程安全

```java
public class Cache {
    static Map<String,Object> map = new HashMap<String,Object>();
    static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    static Lock r = rwl.readLock();
    static Lock w = rwl.writeLock();

    public static final Object get(String key){
        r.lock();
        try {
            return map.get(key);
        }finally {
            r.unlock();
        }
    }

    public static final Object put(String key,Object value){
        w.lock();
        try {
            return map.put(key,value);
        }finally {
            w.unlock();
        }
    }

    public static final void clear() {
        w.lock();
        try {
            map.clear();
        }finally {
            w.unlock();
        }
    }
    
}
```

#### Condition接口和示例

```java
public class ConditionUseCase {
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public static void main(String[] args){
        
    }

    public void conditionWait() throws InterruptedException {
        lock.lock();
        try {
            condition.await();
        }finally {
            lock.unlock();
        }
    }
    public void conditionSignal(){
        lock.lock();
        try {
            condition.signal();
        }finally {
            lock.unlock();
        }
    }
}
```

部分方法描述

| void await()                       | 当前线程进入等待状态，直到被通知或中断                      |
| ---------------------------------- | ---------------------------------------- |
| void awaitUninterruptibly()        | 当前线程进入等待状态，对中断不敏感                        |
| long awaitNanos(long nanosTimeout) | 当前线程进入等待状态，直到被通知，中断或者超时，返回值表示剩余的时间，返回值如果是0或者负数，那么可以认定已经超时了 |
| boolean awaitUntil(Date deadline)  | 当前线程进入等待状态，直到被通知、中断或者到某个时间，如果没有到指定时间，返回true，否则到了指定时间，返回false |
| void signal()                      | 唤醒一个等待在condition中的线程，该线程从等待方法返回前必须获取与Condition相关联的锁 |
| void signlAll()                    | 唤醒所有等待的condition中的线程，能够从等待方法返回的线程必须获得与condition相关联的锁 |

有界队列BoundedQueue解释Condition

```java
public class BoundedQueue<T> {
    private Object[] items;
    private int addIndex,removeIndex,count;
    private Lock lock = new ReentrantLock();
    private Condition notEmpty = lock.newCondition();
    private Condition notFull = lock.newCondition();

    public BoundedQueue(int size){
        items = new Object[size];
    }

    public void add(T t) throws InterruptedException {
        lock.lock();
        try {
            while(count == items.length)
                notFull.await();
            items[addIndex] = t;
            if(++addIndex == items.length)
                addIndex = 0;
            ++count;
            notEmpty.signal();
        }finally {
            lock.unlock();
        }
    }

    public T remove() throws InterruptedException {
        lock.lock();
        try {
            while(count == 0)
                notEmpty.await();
            Object x = items[removeIndex];
            if(++removeIndex == items.length)
                removeIndex = 0;
            --count;
            notFull.signal();
            return (T) x;
        }finally {
            lock.unlock();
        }

    }
}
```