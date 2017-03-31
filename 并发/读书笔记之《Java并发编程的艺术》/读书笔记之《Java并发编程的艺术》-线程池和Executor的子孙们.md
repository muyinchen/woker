# 读书笔记之《Java并发编程的艺术》-线程池和Executor的子孙们

本书前三章分别为

1. 并发编程的挑战，也就是并发编程的缘由所在
2. 底层的实现原理
3. java内存模型

分别从cpu x86，x64以及内存模型等概念中描述java对并发编程的实现和控制，概念较为底层和基础，读书笔记略过前三章直接从第四章应用实现及原理基础开始。

## 章节

1. 并发编程基础
2. java中的锁
3. 并发容器和框架（重点）
4. 13个操作原子类
5. java并发工具类
6. **线程池**
7. **Execurot框架**

## 内容

写到第六七节终于有点小激动了，因为到这里结束就意味着可以开开心心的去写高并发底层了，但实际上开发中用到最多的还是线程池的设计和并发Executor的使用，在下一节会对这两节的内容用一套完整的框架来实践线程池和Executor的使用案例，其实还有一本书《七周七并发模型》第一个模型几乎就把这本书完整的知识点概括完了，但是因为作者针对的面不同，并发模型这本书更多的讲述如何用好并发框架，达到最优的效果，并发编程的艺术更多的是介绍原理和概念，一并起来看收获匪浅。

好了，再来复习下线程池吧，在之前的线程池的设计中使用的是jdk5之前的设计思路，在jdk5之后使用并发框架实现的线程池将会容易的多，但是性能和效率却好得多。

### 6、线程池

先看一张ThreadPoolExecutor执行execute方法的执行示意图

![img](http://static.oschina.net/uploads/space/2015/1111/143938_HPeo_1026123.jpg)

执行方法分4种情况

1. 如果当前运行的线程少于corePoolSize，则创建新线程来执行任务（这一个过程需要获取全局锁）
2. 如果当前运行线程数等于或者多余corePoolSize，则加入到队列BlockQueue
3. 如果无法加入到BlockQueue（队列已满），则创建新的线程来处理任务，（需要获取全局锁）
4. 如果创建的线程将使得当前运行的线程数量大于MaximunPoolSize，任务将被拒绝，并调用非常重要的RejuectExecutionHandler.rejectExecution()方法

##### 线程池的创建

```java
new ThreadPoolExecutor(corePoolSize,maximunPoolSize,keepAliveTime,milliseconds,runnableTaskQueue,handler);
```

共6个参数

1. corePoolSize，线程池的基本大小

2. runnableTaskQueue：用于保存等待执行的任务的阻塞队列，可以有以下选择

3. 1. ArrayBlockingQueue
   2. LinkedBlockingQueue
   3. synchronousQueue
   4. priorityBlockingQueue

4. maximumPoolSze：线程池最大数量

5. ThreadFacotry：用于设置创建线程的工厂

6. RejectedExecutionHandler：饱和策略

7. keepAliveTime：线程保持活动的时间

8. TimeUtil：线程保持活动的单位

##### 线程池提交任务

分别为execute()/submit()

```java
threadPool.execute(new Runnable(){
    @override
    public void run(){
        //run
    }
})
```

```java
Future<Object> future = executor.submit(harReturnValuetask);
try{
 Object s = futrue.get();
}catch(InterruptedException e){
}catch(ExecutionException e){
}finally{
 //关闭线程池
 executor.shutdown();
}
```

##### 关闭线程池

shutdown()、shutdownNow()

##### 线程池的监控

taskCount、completedTaskCount、largestPoolSize、getpoolSize，getActiveCount

通过重写线程池的beforeExecutor、afterExecutor、terminated方法

### 7、Executor框架

Executor框架由3大部分组成

1. 任务：被执行任务需要实现的接口Runnable、Callable
2. 任务的进行：任务执行机制的核心接口Executor，继承Executor的ExecutorService
3. 异步计算的结果：Future和实现了Future接口的FutureTask类

Execurot是Executor框架的基础，将任务的提交和任务的执行分离开来