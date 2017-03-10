# jvm参数陷阱

## 序

本文主要记录一些jvm参数的使用陷阱。

## -XX:MaxTenuringThreshold

-XX:MaxTenuringThreshold只对串行回收器和ParNew有效，对ParallGC无效。存活次数在串行和ParNew方式中可通过-XX:MaxTenuringThreshold来设置，ParallelScavenge则根据运行状态来决定。

## -XX:PretenureSizeThreshold

-XX:PretenureSizeThreshold，设置大对象直接进入年老代的阈值。-XX:PretenureSizeThreshold只对串行回收器和ParNew有效，对ParallGC无效。默认该值为0，即不指定最大的晋升大小，一切由运行情况决定。

## -verbose:gc与-XX:+PrintGC

-XX:+PrintGC 与 -verbose:gc 是一样的，可以认为-verbose:gc 是 -XX:+PrintGC的别名.-XX:+PrintGCDetails 在启动脚本可以自动开启-XX:+PrintGC , 如果在命令行使用jinfo开启的话，不会自动开启-XX:+PrintGC

## -XX:ConcGCThreads并发线程数

CMS默认启动的并发线程数是（ParallelGCThreads+3）/4。

当有4个并行线程时，有1个并发线程；当有5~8个并行线程时，有2个并发线程。

ParallelGCThreads表示的是GC并行时使用的线程数，如果新生代使用ParNew，那么ParallelGCThreads也就是新生代GC线程数。默认情况下，当CPU数量小于8时，ParallelGCThreads的值就是CPU的数量，当CPU数量大于8时，ParallelGCThreads的值等于3+5*cpuCount/8。

ParallelGCThreads = (ncpus <= 8) ? ncpus : 3 + ((ncpus * 5) / 8)

可以通过-XX:ConcGCThreads或者-XX:ParallelCMSThreads来指定。

并发是指垃圾收集器和应用程序交替执行，并行是指应用程序停止，同时由多个线程一起执行GC。因此并行回收器不是并发的。因为并行回收器执行时，应用程序完全挂起，不存在交替执行的步骤。

## -XX:+UseCMSInitiatingOccupancyOnly

始终基于设定的阈值，不根据运行情况进行调整。

如果没有 -XX:+UseCMSInitiatingOccupancyOnly 这个参数, 只有第一次会使用CMSInitiatingPermOccupancyFraction=65 这个值. 后面的情况会自动调整。

我们用-XX+UseCMSInitiatingOccupancyOnly标志来命令JVM不基于运行时收集的数据来启动CMS垃圾收集周期。而是，当该标志被开启时，JVM通过CMSInitiatingOccupancyFraction的值进行每一次CMS收集，而不仅仅是第一次。然而，请记住大多数情况下，JVM比我们自己能作出更好的垃圾收集决策。因此，只有当我们充足的理由(比如测试)并且对应用程序产生的对象的生命周期有深刻的认知时，才应该使用该标志。

## -XX:CMSInitiatingOccupancyFraction

由于CMS收集器不是独占式的回收器，在CMS回收过程中，应用程序仍然在不停地工作。在应用程序工作过程中，又会不断产生垃圾。这些新垃圾在当前CMS回收过程中是无法清除的。同时，因为应用程序没有中断，所以在CMS回收过程中，还应该确保应用程序由足够的内存可用。因此，CMS回收器不会等待堆内存饱和时才进行垃圾回收，而是当堆内存使用率达到某一阈值时便开始进行回收，以确保应用程序在CMS工作中仍然有足够的空间支持应用程序运行。

-XX:CMSInitiatingOccupancyFraction，默认为68，即当年老代的空间使用率达到68%时，会执行一次CMS回收。如果应用程序的内存使用率增长很快，在CMS的执行过程中，已经出现了内存不足，此时，CMS回收就会失败，虚拟机将启动SerialOld串行收集器进行垃圾回收。如果这样，应用程序将完全中断，直到垃圾回收完成，这时，应用程序的停顿时间可能会较长。

因此，根据应用特点，可以对该值进行调优，如果内存增长缓慢，则可以设置一个稍大的值，大的阈值可以有效降低CMS的触发频率，减少年老代回收的次数可以较为明显地改善应用程序性能。

反之，如果应用程序内存使用率增长很快，则应该降低这个阈值，以避免频繁触发年老代串行收集器。

## CMS相关

-XX:+UseCMSCompactAtFullCollection：在CMS垃圾收集后，进行一次内存碎片整理。
-XX:CMSFullGCsBeforeCompaction：在进行多少次CMS回收后，进行一次内存压缩。
-XX:+CMSClassUnloadingEnabled，使用CMS回收Perm区

## 参考

- [[HotSpot VM\] JVM调优的"标准参数"的各种陷阱](http://hllvm.group.iteye.com/group/topic/27945)