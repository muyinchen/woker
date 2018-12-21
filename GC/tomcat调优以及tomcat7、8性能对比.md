# [tomcat调优以及tomcat7、8性能对比]()



文章来源 ： https://www.cnblogs.com/jiangxinlingdu/p/7580104.html



因为每个链路都会对其性能造成影响，应该是**全链路的修改压测**（ak大神经常说全链路!）。本次基本就是局域网，所以并没有怎么优化，其实也应该考虑进去的。

## Linux系统参数层面的修改：

1. **修改可打开文件数和用户最多可开发进程数**
   命令：

```
  ulimit -n 655350
  ulimit –u 655350
```

可以通过ulimit –a查看参数设置，不设置时默认为1024，默认情况下，你会发现请求数到到一定数值后，再也上不去了。

1. **操作系统内核优化**

   > net.ipv4.tcp_max_tw_buckets = 6000
   > timewait 的数量，默认是180000。
   > net.ipv4.ip_local_port_range = 1024 65000
   > 允许系统打开的端口范围。
   > net.ipv4.tcp_tw_recycle = 1
   > 启用timewait 快速回收。
   > net.ipv4.tcp_tw_reuse = 1
   > 开启重用。允许将TIME-WAIT sockets 重新用于新的TCP 连接。
   > net.ipv4.tcp_syncookies = 1
   > 开启SYN Cookies，当出现SYN等待队列溢出时，启用cookies来处理。
   > net.core.somaxconn = 262144
   > web 应用中listen函数的backlog默认会给我们内核参数的net.core.somaxconn限制到128，而nginx定义的NGX_LISTEN_BACKLOG默认为511，所以有必要调整这个值。
   > net.core.netdev_max_backlog = 262144
   > 每个网络接口接收数据包的速率比内核处理这些包的速率快时，允许送到队列的数据包的最大数目。
   > net.ipv4.tcp_max_orphans = 262144
   > 系统中最多有多少个TCP套接字不被关联到任何一个用户文件句柄上。如果超过这个数字，故而连接将即刻被复位并打印出警告信息。这个限制仅仅是为了防止简单的DoS攻击，不能过分依靠它或者人为地减小这个值，更应该增加这个值(如果增加了内存之后)。
   > net.ipv4.tcp_max_syn_backlog = 262144
   > 记录的那些尚未收到客户端确认信息的连接请求的最大值。对于有128M内存的系统而言，缺省值是1024，小内存的系统则是128。
   > net.ipv4.tcp_timestamps = 0
   > 时间戳可以避免序列号的卷绕。一个1Gbps的链路肯定会遇到以前用过的序列号。时间戳能够让内核接受这种“异常”的数据包。这里需要将其关掉。
   > net.ipv4.tcp_synack_retries = 1
   > 为了打开对端的连接，内核需要发送一个SYN 并附带一个回应前面一个SYN的ACK。也就是所谓三次握手中的第二次握手。这个设置决定了内核放弃连接之前发送SYN+ACK包的数量。
   > net.ipv4.tcp_syn_retries = 1
   > 在内核放弃建立连接之前发送SYN 包的数量。
   > net.ipv4.tcp_fin_timeout = 1
   > 如果套接字由本端要求关闭，这个参数决定了它保持在FIN-WAIT-2状态的时间。对端可以出错并永远不关闭连接，甚至意外当机。缺省值是60秒。2.2内核的通常值是180秒，3你可以按这个设置，但要记住的是，即使你的机器是一个轻载的WEB服务器，也有因为大量的死套接字而内存溢出的风险，FIN-WAIT-2的危险性比FIN-WAIT-1要小，因为它最多只能吃掉1.5K内存，但是它们的生存期长些。
   > net.ipv4.tcp_keepalive_time = 30
   > 当keepalive 起用的时候，TCP发送keepalive消息的频度。缺省是2小时。
   > 内核参数优化设置在/etc/sysctl.conf文件中。

上面2个都调整一样的情况下，开始准备测试*tomcat7*（*jdk7*）与*tomcat8*（*jdk8*）的一些性能测试了。
由于各各原因复杂服务没法测试，先仅仅是测试静态页面。

------

## jvm层面优化：

**Jdk7：**

```
-Xms2G
-Xmx2G
-Xmn512m
-XX:PermSize=512M 
-XX:MaxPermSize=512M 
-XX:+UseConcMarkSweepGC 
-XX:+CMSClassUnloadingEnabled 
-XX:+HeapDumpOnOutOfMemoryError 
-verbose:gc 
-XX:+PrintGCDetails 
-XX:+PrintGCTimeStamps 
-XX:+PrintGCDateStamps 
-Xloggc:/appl/gc.log 
-XX:CMSInitiatingOccupancyFraction=75 
-XX:+UseCMSInitiatingOccupancyOnly  
```

**Jdk8:**

```
-Xms2G
-Xmx2G
-Xmn512m 
-XX:MetaspaceSize=512M 
-XX:MaxMetaspaceSize=512M 
-XX:+UseConcMarkSweepGC 
-XX:+CMSClassUnloadingEnabled 
-XX:+HeapDumpOnOutOfMemoryError 
-verbose:gc 
-XX:+PrintGCDetails 
-XX:+PrintGCTimeStamps 
-XX:+PrintGCDateStamps 
-Xloggc:/appl/gc.log 
-XX:CMSInitiatingOccupancyFraction=75 
-XX:+UseCMSInitiatingOccupancyOnly   
```

**需要特别说明下：**

> 元数据空间，专门用来存元数据的，它是jdk8里特有的数据结构用来替代perm。

**Jdk7：**
出现了多次Full GC了。![img](https://upload-images.jianshu.io/upload_images/7849276-6520a009e419813b?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)其中，CMS-initial-mark和CMS-remark会stop-the-world。
![img](https://upload-images.jianshu.io/upload_images/7849276-2d6c83b51fe2a16d?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)所以选择cms垃圾回收器，用jstat 相关命令看到的FGC每次都是加2的变化情况。
**Jdk8：**
一次Full GC也没有发生。从这里也可以看出tomcat8的实现机制比tomcat7的要好些（相同条件没有产生多余对象从而导致Full GC问题）。

**需要特别说明下：**
年轻代的gc日志7和8略有不同
![img](https://upload-images.jianshu.io/upload_images/7849276-09e1b033ec2d0d04?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)jdk8把日志打得更全了 ，jdk8的gc日志与jdk7的有所不同，听大佬们说各各jdk的日志都有所不同，其实这里8的这个和7的意思一样，只是7没有表达出来而已。

通过压力测试结果来看，jdk7每隔一段时间会出现tps大的下降，就是俗话说的卡顿。
而jdk8没有啥卡顿现象
![img](https://upload-images.jianshu.io/upload_images/7849276-c091052fac2e4104?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
而jdk7的波动就特别明显
![img](https://upload-images.jianshu.io/upload_images/7849276-300962ba9d321c95?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
该效果8比7的效果请求要好。

由于jdk7 gc日志，
![img](https://upload-images.jianshu.io/upload_images/7849276-30f9ce5859f09f22?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)CMS开始回收tenured generation collection。这阶段是CMS初始化标记的阶段，从垃圾回收的“根对象”开始，且只扫描直接与“根对象”直接关联的对象，并做标记，在此期间，其他线程都会停止。

tenured generation的空间是1572864K，在容量为1205558K时开始执行初始标记。
说明-XX:CMSInitiatingOccupancyFraction=75已经达到触发（Background ）CMS GC的条件。

应该扩大堆空间大小，在此修改仅仅是修改了堆其他不变，其他参数还是原来上面的参数
**Jdk7，jdk8：**

```
-Xms4G -Xmx4G -Xmn1365m  
```

查看gc日志，发现的确都没有FGC了，但是ygc差距很大，在此表示tomcat8（jdk8）比tomcat7（jdk7）好好像。
Jdk7 ygc时间过长：
![img](https://upload-images.jianshu.io/upload_images/7849276-5de8016af7eef0f2?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
Jdk8 ygc非常好：
![img](https://upload-images.jianshu.io/upload_images/7849276-874109315de11cb7?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
其实对于ygc的分享特别复杂，jvm的参数调整算是小调，最关键的应该在产生对象的地方，即应用本事，采用合理的架构，合理的数据结构结合一些技巧来达到等。
由于测试的是静态页面，那么只有tomcat代码了，表示8的实现比7的实现方面的确要好（有空去准备去读读tomcat源码到时候在分享分享）。

通过日志查看jdk7的老年代使用率很低，准备在此进行调整，在堆大小不变的情况下调整年轻代的大小。
**Jdk7，jdk8都进行调整其他参数还保持上面不变。**

```
-Xms4G -Xmx4G -Xmn3g   
```

效果有所改善（tps也张了200多），但是还是不如jdk8的，可能是tomcat内部实现8就是比7好。
次中间还尝试过更大堆以及年轻代的调整 如6G 8G 10G等都没有太大变化有些还不如4G点这个好，**所以并不是堆空间设置越大越好。**
Jvm目前只能调到这块了，后续如果有啥发现或者大佬们的建议在调整。

------

## Tomcat本身这块的调优

Tomcat 7/8 的优化参数有点不一样，最好按下面的方式看一下官网这个文档是否还保留着这个参数
启动tomcat，访问该地址，下面要讲解的一些配置信息，在该文档下都有说明的：
文档：http://127.0.0.1:8080/docs/config
你也可以直接看网络版本：
Tomcat 7 文档：https://tomcat.apache.org/tomcat-7.0-doc/config/
Tomcat 8 文档：https://tomcat.apache.org/tomcat-8.0-doc/config/
如果你需要查看 Tomcat 的运行状态可以配置tomcat管理员账户，然后登陆Tomcat后台进行查看。

在修改jvm参数之后tps怎么都上不去的情况下面通过查看线程dump![img](https://upload-images.jianshu.io/upload_images/7849276-4fc0d49b2c1e51c5?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![img](https://upload-images.jianshu.io/upload_images/7849276-2af6f4362c6cdb93?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)![img](https://upload-images.jianshu.io/upload_images/7849276-8ffc0f119c4ec1e8?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Tomcat7、tomcat8情况一样，发现很多都堵塞在这块了。
这块涉及到代码这块了（tomcat的源码这块了）
通过查看源码发现这块是涉及到了tomcat线程池这块了，稍微会详细说明下，先看看一些简单配置。
![img](https://upload-images.jianshu.io/upload_images/7849276-7fc1c08875ae5858?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
默认配置，可以配置写那些值呢？![img](https://upload-images.jianshu.io/upload_images/7849276-41481561b4468d34?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
**Tomcat8多了一个nio2**
![img](https://upload-images.jianshu.io/upload_images/7849276-1a616a072bcadd9d?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)这个也比较重要
![img](https://upload-images.jianshu.io/upload_images/7849276-e53b55cd215b82d1?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)这个就是关于池的配置了，有那些参数和怎么实现的呢？
![img](https://upload-images.jianshu.io/upload_images/7849276-6c57e2880dd60ee1?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
**Tomcat的实现在org.apache.catalina.core.StandardThreadExecutor**
里面的参数有:
![img](https://upload-images.jianshu.io/upload_images/7849276-a45c1a4014685f4e?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**简单理解就是：**

> maxThreads - Tomcat线程池最多能起的线程数
> maxConnections - Tomcat最多能并发处理的请求（连接）
> acceptCount - Tomcat维护最大的对列数
> minSpareThreads - Tomcat初始化的线程池大小或者说Tomcat线程池最少会有这么多线程。
> 比较容易弄混的是maxThreads和maxConnections这两个参数：

maxThreads是指Tomcat线程池做多能起的线程数
maxConnections则是Tomcat一瞬间做多能够处理的并发连接数。比如maxThreads=1000，maxConnections=800，假设某一瞬间的并发时1000，那么最终Tomcat的线程数将会是800，即同时处理800个请求，剩余200进入队列“排队”，如果acceptCount=100，那么有100个请求会被拒掉。

**注意：**根据前面所说，只是并发那一瞬间Tomcat会起800个线程处理请求，但是稳定后，某一瞬间可能只有很少的线程处于RUNNABLE状态，大部分线程是TIMED_WAITING，如果你的应用处理时间够快的话。所以真正决定Tomcat最大可能达到的线程数是maxConnections这个参数和并发数，当并发数超过这个参数则请求会排队，这时响应的快慢就看你的程序性能了。

这些仅仅是告诉我们，如果需要了解细节还需要阅读下源码。有些读了源码可能参数的理解更清楚了。
![img](https://upload-images.jianshu.io/upload_images/7849276-de93721a563c8fbd?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

```java
public class StandardThreadExecutor extends LifecycleMBeanBase  
        implements Executor, ResizableExecutor {  
    //默认线程的优先级  
    protected int threadPriority = Thread.NORM_PRIORITY;  
    //守护线程  
    protected boolean daemon = true;  
    //线程名称的前缀  
    protected String namePrefix = "tomcat-exec-";  
    //最大线程数默认200个  
    protected int maxThreads = 200;  
    //最小空闲线程25个  
    protected int minSpareThreads = 25;  
    //超时时间为6000  
    protected int maxIdleTime = 60000;  
    //线程池容器  
    protected ThreadPoolExecutor executor = null;  
    //线程池的名称  
    protected String name;  
     //是否提前启动线程  
    protected boolean prestartminSpareThreads = false;  
    //队列最大大小  
    protected int maxQueueSize = Integer.MAX_VALUE;  
    //为了避免在上下文停止之后，所有的线程在同一时间段被更新，所以进行线程的延迟操作  
    protected long threadRenewalDelay = 1000L;  
    //任务队列  
    private TaskQueue taskqueue = null;  
  
    //容器启动时进行,具体可参考org.apache.catalina.util.LifecycleBase#startInternal()  
    @Override  
    protected void startInternal() throws LifecycleException {  
        //实例化任务队列  
        taskqueue = new TaskQueue(maxQueueSize);  
        //自定义的线程工厂类,实现了JDK的ThreadFactory接口  
        TaskThreadFactory tf = new TaskThreadFactory(namePrefix,daemon,getThreadPriority());  
        //这里的ThreadPoolExecutor是tomcat自定义的,不是JDK的ThreadPoolExecutor  
        executor = new ThreadPoolExecutor(getMinSpareThreads(), getMaxThreads(), maxIdleTime, TimeUnit.MILLISECONDS,taskqueue, tf);  
        executor.setThreadRenewalDelay(threadRenewalDelay);  
        //是否提前启动线程，如果为true，则提前初始化minSpareThreads个的线程，放入线程池内  
        if (prestartminSpareThreads) {  
            executor.prestartAllCoreThreads();  
        }  
        //设置任务容器的父级线程池对象  
        taskqueue.setParent(executor);  
        //设置容器启动状态  
        setState(LifecycleState.STARTING);  
    }  
  
  //容器停止时的生命周期方法,进行关闭线程池和资源清理  
    @Override  
    protected void stopInternal() throws LifecycleException {  
  
        setState(LifecycleState.STOPPING);  
        if ( executor != null ) executor.shutdownNow();  
        executor = null;  
        taskqueue = null;  
    }  
  
    //这个执行线程方法有超时的操作，参考org.apache.catalina.Executor接口  
    @Override  
    public void execute(Runnable command, long timeout, TimeUnit unit) {  
        if ( executor != null ) {  
            executor.execute(command,timeout,unit);  
        } else {   
            throw new IllegalStateException("StandardThreadExecutor not started.");  
        }  
    }  
  
    //JDK默认操作线程的方法,参考java.util.concurrent.Executor接口  
    @Override  
    public void execute(Runnable command) {  
        if ( executor != null ) {  
            try {  
                executor.execute(command);  
            } catch (RejectedExecutionException rx) {  
                //there could have been contention around the queue  
                if ( !( (TaskQueue) executor.getQueue()).force(command) ) throw new RejectedExecutionException("Work queue full.");  
            }  
        } else throw new IllegalStateException("StandardThreadPool not started.");  
    }  
  
    //由于继承了org.apache.tomcat.util.threads.ResizableExecutor接口，所以可以重新定义线程池的大小  
    @Override  
    public boolean resizePool(int corePoolSize, int maximumPoolSize) {  
        if (executor == null)  
            return false;  
  
        executor.setCorePoolSize(corePoolSize);  
        executor.setMaximumPoolSize(maximumPoolSize);  
        return true;  
    }  
}  
```

Tomcat的线程池的名字也叫作ThreadPoolExecutor，刚开始看源代码的时候还以为是使用了JDK的ThreadPoolExecutor了呢，后面仔细查看才知道是Tomcat自己实现的一个ThreadPoolExecutor，不过基本上都差不多。![img](https://upload-images.jianshu.io/upload_images/7849276-ac836010f2cb26a8?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
看到这里以为tomcat线程池的原理和jdk的线程池原理一样了，其实不是的。
问题的关键在这里
![img](https://upload-images.jianshu.io/upload_images/7849276-ebca3d67ba3888b7?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
TaskQueue这个任务队列是专门为线程池而设计的。优化任务队列以适当地利用线程池执行器内的线程。
Jdk的execute执行策略： 优先offer到queue，queue满后再扩充线程到maxThread，如果已经到了maxThread就reject
Tomcat的execute执行策略： 优先扩充线程到maxThread，再offer到queue，如果满了就reject比较适合于业务处理需要远程资源的场景

修改为：

```xml
<Executor name="tomcatThreadPool" namePrefix="catalina-exec-"  
       maxThreads="350" minSpareThreads="20" prestartminSpareThreads="true"/>  
<Connector executor="tomcatThreadPool" acceptCount="300000"  
               port="8080" protocol="HTTP/1.1"  
               connectionTimeout="20000"  
               redirectPort="8443" />  
```

由于可能是静态页面返回很快，设置500 800 1000线程效果都不怎么明显，如果是加项目应该会有所区别，所以线程池也并不是越多越好。
Tomcat7和tomcat8性能都有所提升，所以池很重要，但是8和7的tps在都提高了2000左右。在修改线程池之后，查看jvm gc情况都良好，所以并没有在此调整jvm参数了。
经过这么多分析也了解到了tomcat该如何调优了，以及tomcat7、tomcat8的一些性能区别了。
由于测试的是静态页面，很多有些问题还没有涉及到，后续如果测试服务估计需要修改，调试排查的问题更多，到时候继续查看后续文章！！