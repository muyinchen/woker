# 读书笔记之《Java并发编程的艺术》-并发编程基础

本书前三章分别为

1. 并发编程的挑战，也就是并发编程的缘由所在
2. 底层的实现原理
3. java内存模型

分别从cpu x86，x64以及内存模型等概念中描述java对并发编程的实现和控制，概念较为底层和基础，读书笔记略过前三章直接从第四章应用实现及原理基础开始。

## 章节

1. **并发编程基础**
2. java中的锁
3. 并发容器和框架（重点）
4. 13个操作原子类
5. java并发工具类
6. 线程池
7. Execurot框架

## 内容

### 并发编程基础

#### 多线程

先看一段main方法

```java
public class MultThread {
    public static void main(String[] args){
        //获取Java线程管理MXBean

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false,false);
        for(ThreadInfo threadInfo:threadInfos){
            System.out.println("[" + threadInfo.getThreadId() + " ] " + threadInfo.getLockName());
        }
    }
}
```
输出

```java
[9 ] Monitor Ctrl-Break
[5 ] Attach Listener
[4 ] Signal Dispatcher
[3 ] Finalizer
[2 ] Reference Handler
[1 ] main
```
解释

java程序运行的不仅仅是main方法的运行，而是main线程和多个其他线程的同时运行

java天生就是多线程程序

​

#### 线程优先级

​    线程的运行不能依赖于线程优先级

​



 #### 线程的状态

    线程在一个时刻，只能处于一种状态

    | NEW          | 初始状态，线程构建还没有start   |
    | ------------ | ------------------- |
    | RUNNABLE     | 运行状态，就绪+运行          |
    | BLOCKED      | 阻塞状态，阻塞于锁           |
    | WAITING      | 等待状态，需要等待其他线程做出一些动作 |
    | TIME_WAITING | 超时等待，可以再指定的时间自行返回   |
    | TERMINATED   | 终止状态，执行完毕           |

 ```java
    public class ThreadState {
        public static void main(String[] args){
            new Thread(new TimeWaiting(),"TimeWaiting ").start();
            new Thread(new Waiting(),"Waiting").start();
            new Thread(new Blocked(),"Block-1").start();
            new Thread(new Blocked(),"Block-2").start();
        }

        static class TimeWaiting implements Runnable{

            @Override
            public void run() {
                while(true){
                    SleepUtils.second(100);
                }
            }
        }

        static class Waiting implements Runnable{

            @Override
            public void run() {
                while (true){
                    synchronized (Waiting.class){
                        try {
                            Waiting.class.wait();
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        static class Blocked implements Runnable{

            @Override
            public void run() {
                synchronized (Blocked.class){
                    while (true){
                        SleepUtils.second(100);
                    }
                }
            }
        }

        static class SleepUtils{
            public static final void second(long seconds){
                try {
                    TimeUnit.SECONDS.sleep(seconds);
                }catch (InterruptedException e){

                }
            }
        }
    }
 ```

cmd：jps

```java
D:\IdealProjects\j360-jdk>jps
11476 Launcher
13520
7628 AppMain
4480 Jps
```
```java
D:\IdealProjects\j360-jdk>jstack 7628
2015-11-10 11:20:55
Full thread dump Java HotSpot(TM) 64-Bit Server VM (24.75-b04 mixed mode):

"DestroyJavaVM" prio=6 tid=0x0000000000e6d800 nid=0x11b8 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Block-2" prio=6 tid=0x0000000011429000 nid=0x373c waiting for monitor entry [0x0000000011c9f000]
   java.lang.Thread.State: BLOCKED (on object monitor)
        at me.j360.jdk.concurrent._1_thread.ThreadState$Blocked.run(ThreadState.java:52)
        - waiting to lock <0x00000007ac3e23f0> (a java.lang.Class for me.j360.jdk.concurrent._1_thread.ThreadState$Blocked)
        at java.lang.Thread.run(Thread.java:745)

"Block-1" prio=6 tid=0x0000000011420000 nid=0x35a0 waiting on condition [0x0000000011b9e000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
        at java.lang.Thread.sleep(Native Method)
        at java.lang.Thread.sleep(Thread.java:340)
        at java.util.concurrent.TimeUnit.sleep(TimeUnit.java:360)
        at me.j360.jdk.concurrent._1_thread.ThreadState$SleepUtils.second(ThreadState.java:61)
        at me.j360.jdk.concurrent._1_thread.ThreadState$Blocked.run(ThreadState.java:52)
        - locked <0x00000007ac3e23f0> (a java.lang.Class for me.j360.jdk.concurrent._1_thread.ThreadState$Blocked)
        at java.lang.Thread.run(Thread.java:745)

"Waiting" prio=6 tid=0x000000001141d800 nid=0x2318 in Object.wait() [0x0000000011a9f000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x00000007ac3df4e8> (a java.lang.Class for me.j360.jdk.concurrent._1_thread.ThreadState$Waiting)
        at java.lang.Object.wait(Object.java:503)
        at me.j360.jdk.concurrent._1_thread.ThreadState$Waiting.run(ThreadState.java:37)
        - locked <0x00000007ac3df4e8> (a java.lang.Class for me.j360.jdk.concurrent._1_thread.ThreadState$Waiting)
        at java.lang.Thread.run(Thread.java:745)

"TimeWaiting " prio=6 tid=0x000000001141a800 nid=0x1124 waiting on condition [0x000000001199f000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
        at java.lang.Thread.sleep(Native Method)
        at java.lang.Thread.sleep(Thread.java:340)
        at java.util.concurrent.TimeUnit.sleep(TimeUnit.java:360)
        at me.j360.jdk.concurrent._1_thread.ThreadState$SleepUtils.second(ThreadState.java:61)
        at me.j360.jdk.concurrent._1_thread.ThreadState$TimeWaiting.run(ThreadState.java:25)
        at java.lang.Thread.run(Thread.java:745)

"Monitor Ctrl-Break" daemon prio=6 tid=0x00000000113a7000 nid=0x3338 runnable [0x000000001180f000]
   java.lang.Thread.State: RUNNABLE
        at java.net.DualStackPlainSocketImpl.accept0(Native Method)
        at java.net.DualStackPlainSocketImpl.socketAccept(DualStackPlainSocketImpl.java:131)
        at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:398)
        at java.net.PlainSocketImpl.accept(PlainSocketImpl.java:199)
        - locked <0x00000007ac444038> (a java.net.SocksSocketImpl)
        at java.net.ServerSocket.implAccept(ServerSocket.java:530)
        at java.net.ServerSocket.accept(ServerSocket.java:498)
        at com.intellij.rt.execution.application.AppMain$1.run(AppMain.java:90)
        at java.lang.Thread.run(Thread.java:745)
```
线程的状态会随着代码的执行在不同的状态间切换

​

 #### 线程中断

线程的一个标识位属性，运行中的线程被其他线程进行了中断操作，调用interrupt()方法，可以通过isinterrupt()方法判断是否被中断

标记位清除的两个场景：Thread.interrupt()复位、InterruptException抛出异常

​

 #### 安全终止线程

利用boolean变量来控制线程

```java
public class Shutdown {
    public static void main(String[] args) throws InterruptedException {
        Runner one  = new Runner();
        Thread thread1 = new Thread(one,"CountThread");
        thread1.start();
        TimeUnit.SECONDS.sleep(1);
        thread1.interrupt();
        Runner two = new Runner();
        Thread thread2 = new Thread(two,"CountThread");
        thread2.start();
        TimeUnit.SECONDS.sleep(1);
        thread2.interrupt();
        two.cancel();
    }

    private static class Runner implements Runnable{

        private long i;
        private volatile boolean on = true;
        @Override
        public void run() {
            while(on && !Thread.currentThread().isInterrupted()){
                i++;
            }
            System.out.println("Count i = " + i);
        }

        public void cancel(){
            on = false;
        }
    }
}
```
```java
Count i = 365764392
Count i = 226360860
```
​

#### 线程间通信、等待/通知机制

| notify()       | 通知一个等待的线程，从wait方法返回，前提是线程获取了对象的锁     |
| -------------- | ------------------------------------ |
| notifyAll()    | 通知所有在该对象上的线程                         |
| wait()         | 调用该方法进入WAITING状态，只有等待其他线程通知才会返回，会释放锁 |
| wait(long)     | 超时等待通知                               |
| wait(long,int) | 更精确的超时等待通知，精确到纳秒                     |

​

 #### Thread.join()的使用

等待线程终止后才用thread.join返回

每个线程的终止是前驱线程的终止

```java
public class Join {
    public static void main(String[] args){
        Thread previous = Thread.currentThread();
        for(int i = 0;i<10;i++){
            Thread thread = new Thread(new Domino(previous),String.valueOf(i));
            thread.start();
            previous = thread;
        }
    }

    static class Domino implements Runnable{

        public Domino(Thread thread){
            this.thread = thread;
        }
        private Thread thread;
        @Override
        public void run() {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " terminate");
        }
    }
}
0 terminate
1 terminate
2 terminate
3 terminate
4 terminate
5 terminate
6 terminate
7 terminate
8 terminate
9 terminate
```
#### 

## 案例





#### 基于等待模式数据库连接池

```java
   public class ConnectionPool {
       private LinkedList<Connection> pool = new LinkedList<Connection>();

       public ConnectionPool(int initialSize){
           if(initialSize > 0){
               for(int i=1;i<initialSize;i++){
                   pool.addLast(ConnectionDrive.createConnection());
               }
           }
       }

       public void releaseConnection(Connection connection){
           if(connection != null){
               synchronized (pool){
                   pool.addLast(connection);
                   pool.notifyAll();
               }
           }
       }

       public Connection fetchConnection(long mills) throws InterruptedException {
           synchronized (pool){
               if(mills <= 0){
                   while (pool.isEmpty()){
                       pool.wait();
                   }
                   return pool.removeFirst();
               }else{
                   long future = System.currentTimeMillis() + mills;
                   long remainning = mills;
                   while(pool.isEmpty() && remainning > 0){
                       pool.wait();
                       remainning = future - System.currentTimeMillis();
                   }
                   Connection result = null;
                   if(! pool.isEmpty()){
                       result = pool.removeFirst();
                   }
                   return result;
               }
           }
       }
   }
```

 ```java
   public class ConnectionDrive {
       static class ConnectionHandler implements InvocationHandler{

           @Override
           public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
               if(method.getName().equals("commit")){
                   TimeUnit.MILLISECONDS.sleep(100);
               }
               return null;
           }
       }

       public static final Connection createConnection(){
           return (Connection) Proxy.newProxyInstance(ConnectionDrive.class.getClassLoader(),new Class[]{ Connection.class} ,new ConnectionHandler());
       }
    }
 ```




```java

   public class ConnectionTest {
       static ConnectionPool pool = new ConnectionPool(10);
       static CountDownLatch start = new CountDownLatch(1);

       static CountDownLatch end;

       public static void main(String[] args) throws InterruptedException {
           int threadCount = 30;
           end = new CountDownLatch(threadCount);
           int count = 20;
           AtomicInteger got = new AtomicInteger();
           AtomicInteger notGot = new AtomicInteger();
           for(int i=0;i<threadCount;i++){
               Thread thread = new Thread(new ConnectionRunner(count,got,notGot),"Thread");
               thread.start();
           }
           start.countDown();
           end.await();
           System.out.println("total invoke:" + (threadCount *count));
           System.out.println("got " + got);
           System.out.println("notGot " + notGot);
       }

       static class ConnectionRunner implements Runnable{
           int count;
           AtomicInteger got;
           AtomicInteger notGot;
           public ConnectionRunner(int count,AtomicInteger got,AtomicInteger notGot){
               this.count = count;
               this.got = got;
               this.notGot = notGot;
           }

           @Override
           public void run() {
               try {
                   start.await();
               }catch (Exception e){

               }
               while (count > 0){
                   try {
                       Connection connection = pool.fetchConnection(1000);
                       if(connection != null){
                           try {
                               connection.createStatement();
                               connection.commit();
                           }finally {
                               pool.releaseConnection(connection);
                               got.incrementAndGet();
                           }
                       }else{
                           notGot.incrementAndGet();
                       }
                   }catch (Exception ex){

                   }finally {
                       count--;
                   }
               }
               end.countDown();
           }
       }
   }
```

total invoke:600

got 569

notGot 31

资源一定的情况下，客户端出现超时无法获取连接的比例不断升高，超时按时返回告知客户端获取连接出现问题，是系统自我保护的机制。

​

​

 #### 线程池技术

```java
public interface ThreadPool<Job extends Runnable> {
    void execute(Job job);
    void shutdown();
    void addWorkers(int num);
    void removeWorker(int num);
    int getJobSize();
}
```
```java
package me.j360.jdk.concurrent._1_thread.simplepool;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with j360-jdk -> me.j360.jdk.concurrent._1_thread.simplepool.
 * User: min_xu
 * Date: 2015/11/10
 * Time: 13:45
 * 说明：
 */
public class DefaultThreadPool<Job extends Runnable> implements ThreadPool<Job> {

    private static final int MAX_WORKER_NUMBERS = 10;
    private static final int DEFAULT_WORRER_NUMBERS = 5;
    private static final int MIN_WORKER_NUMBERS = 1;
    private final LinkedList<Job> jobs = new LinkedList<Job>();
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());

    private int workerNum = DEFAULT_WORRER_NUMBERS;
    private AtomicLong threadNum = new AtomicLong();

    public DefaultThreadPool(){
        initializerWorkers(DEFAULT_WORRER_NUMBERS);
    }
    public DefaultThreadPool(int num){
        workerNum = num  > MAX_WORKER_NUMBERS?MAX_WORKER_NUMBERS:num < MIN_WORKER_NUMBERS?MIN_WORKER_NUMBERS:num;
        initializerWorkers(workerNum);
    }
    @Override
    public void execute(Job job) {
        if(job != null){
            synchronized (jobs){
                jobs.addLast(job);
                jobs.notify();
            }
        }
    }

    @Override
    public void shutdown() {
        for(Worker worker:workers){
            worker.shutdown();
        }
    }

    @Override
    public void addWorkers(int num) {
        synchronized (jobs){
            if(num+this.workerNum >MAX_WORKER_NUMBERS){
                num = MAX_WORKER_NUMBERS - this.workerNum;
            }
            initializerWorkers(num);
            this.workerNum = num;
        }
    }

    @Override
    public void removeWorker(int num) {
        synchronized(jobs){
            if(num>this.workerNum){
                throw new IllegalArgumentException("beyond worknum");
            }
            int count = 0;
            while(count < num){
                Worker worker = workers.get(count);
                if(workers.remove(worker)){
                    worker.shutdown();
                    count++;
                }
            }
            this.workerNum -= count;
        }
    }

    @Override
    public int getJobSize() {
        return jobs.size();
    }

    //初始化线程工作
    private void initializerWorkers(int num){
        for(int i = 0;i<num;i++){
            Worker worker = new Worker();
            Thread thread = new Thread(worker,"ThreadPool-Worker-" + threadNum.incrementAndGet());
            thread.start();
        }
    }

    class Worker implements Runnable{

        private volatile boolean running = true;
        @Override
        public void run() {
            while (running){
                Job job = null;
                synchronized (jobs){
                    while (jobs.isEmpty()){
                        try {
                            jobs.wait();
                        }catch (InterruptedException e){
                            Thread.currentThread().interrupt();
                            return;
                        }

                    }
                    job = jobs.removeFirst();
                }
                if(job != null){
                    try {
                        job.run();
                    }catch (Exception ex){

                    }
                }
            }
        }

        public void shutdown(){
            running = false;
        }
    }

    static class JobJob implements Runnable{

        @Override
        public void run() {
            System.out.println("JobJob");
        }
    }
    public static void main(String[] args){
        DefaultThreadPool<JobJob> defaultThreadPool = new DefaultThreadPool<JobJob>(4);
        for(int i=0;i<40;i++){
            JobJob jobJob = new JobJob();
            defaultThreadPool.execute(jobJob);

            System.out.println(defaultThreadPool.getJobSize());
        }

        defaultThreadPool.shutdown();
    }
}
```
