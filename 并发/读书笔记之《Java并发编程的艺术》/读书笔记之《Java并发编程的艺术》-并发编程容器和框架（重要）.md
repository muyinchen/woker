# 读书笔记之《Java并发编程的艺术》-并发编程容器和框架（重要）



本书前三章分别为

1. 并发编程的挑战，也就是并发编程的缘由所在
2. 底层的实现原理
3. java内存模型

分别从cpu x86，x64以及内存模型等概念中描述java对并发编程的实现和控制，概念较为底层和基础，读书笔记略过前三章直接从第四章应用实现及原理基础开始。

## 章节

1. 并发编程基础
2. java中的锁
3. **并发容器和框架（重点）**
4. **13个操作原子类**
5. **java并发工具类**
6. 线程池
7. Execurot框架

## 内容

本章节开始是我认为的重点，这里是平常开发中打交道最多的并发框架，在了解到并发编程的基础已经实现原理后，平常很少去使用，然后作为j2se5之后加入的并发框架，让从事java开发的咱们相比其他语言的程序员幸福的多，这一切皆因为Doug Lea大师不溃余力地为java开发者提供了非常多的java并发容器和框架

### 3节：并发容器和框架

### ConcurrentHashMap

使用的锁分段技术，将数据一段一段地存储，给一段配一把锁

#### 初始化

初始化方法通过initialCapacity、loadFactor、concurrencyLevel等几个参数来初始化segment数组、段偏移量segmentShift、段掩码segmentMask和每个segment里的HashEntity数组来实现的

#### 操作

get操作，get操作的高效之处在于整个get过程不需要加锁，除非读到的值是空才会加锁重读。

```java
public V get(Object key){
    int hash = hash(key.hashCode));
    return segmentFor(hash).get(key,hash);
}
```

put操作，需要对共享变量进行写入操作，为了线程安全

### ConcurrentLinkedQueue

基于链接节点的无界线程安全队列，采用先进先出FIFO的规则对节点进行排序

### Java中的阻塞队列

jdk7提供了7个阻塞队列

ArrayBlockingQueue

LinkedBlockingQueue

PriorityBlockingQueue

DelayQueue

SynchronusQueue

LinkedTransferQueue

LinkedBlockingDeque

### Fork/join框架

jdk7提供的用于并行执行任务的框架

框架设计

步骤1：分隔任务

步骤2：执行任务并合并结果

Fork.join使用两个类来完成以上两件事情

1、ForkJoinTask

​    RecursiveAction：用于没有返回结果的任务

​    RecursiveTask：用于有返回结果的任务

2、ForkJoinPool

​    ForkJonkTask需要通过ForkJoinPool来执行

使用Fork/join框架：计算1+2+3+4的结果

```java
public class CountTask extends RecursiveTask<Integer> {

    private static final int THRESHOLD = 2; //阈值
    private int start;
    private int end;

    public CountTask(int start,int end){
        this.start = start;
        this.end = end;
    }
    @Override
    protected Integer compute() {
        int sum = 0;
        boolean canCompute = (end-start) <= THRESHOLD;
        if(canCompute){
            for(int i=start;i<=end;i++){
                sum += i;
            }
        }else{
            int middle = (start + end )/2;
            CountTask leftTask = new CountTask(start,middle);
            CountTask rightTask = new CountTask(middle,end);

            leftTask.fork();
            rightTask.fork();

            int leftResult = leftTask.join();
            int rightResult = rightTask.join();

            sum = leftResult + rightResult;

        }
        return sum;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        CountTask task = new CountTask(1,4);
        Future<Integer> result = forkJoinPool.submit(task);
        try {
            System.out.println(result.get());
        }catch (Exception e){

        }
    }
}
```

### 4节：13个操作原子类

concurrent包下面atomic包提供了13个类，属于4种类型的原子更新方式

1. 原子更新基本类型
2. 原子更新数组
3. 原子更新引用
4. 原子更新属性

基本都是使用Unsafe实现的包装类

### 5节：并发工具类

#### 等待多线程完成的CountDownLatch

允许一个或者多个线程等待其他线程完成操作

#### 同步屏障CyclickBarrier

让一组线程到达一个屏障后阻塞，直到最后一个线程到达屏障才会开门

#### 控制并发线程数的Semaphore

控制同时访问特定资源的线程数量

#### 线程间交换数据Exchanger

提供一个同步点，在这个同步点，两个线程之间可以交换数据