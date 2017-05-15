## RocketMQ学习（三）：rocketmq-namesrv介绍



*摘要：* 刚刚拉了最新的代码，版本是3.2.6，直接NamesrvStartup类的main()方法启动，不需要带启动参数，启动序列图如下： 当broker，producer，consumer都运行后，namesrv一共有8类线程： 1.ServerHouseKeepingService：守护线程，本质是ChannelEventListener，监听broker的channel变化来更新本地的RouteInfo。

刚刚拉了最新的代码，版本是3.2.6，直接NamesrvStartup类的main()方法启动，不需要带启动参数，启动序列图如下：

![namesrv_start](http://lifestack.cn/wp-content/uploads/2015/04/namesrv_start.jpg)

当broker，producer，consumer都运行后，namesrv一共有8类线程：

1.ServerHouseKeepingService：守护线程，本质是ChannelEventListener，监听broker的channel变化来更新本地的RouteInfo。

2.NSScheduledThread1：定时任务线程，定时跑2个任务，第一个是，每隔10分钟扫描出不活动的broker，然后从routeInfo中删除，第二个是，每个10分钟定时打印configTable的信息。

3.NettyBossSelector_1:Netty的boss线程（Accept线程），这里只有一根线程。

4.NettyEventExecuter:一个单独的线程，监听NettyChannel状态变化来通知ChannelEventListener做响应的动作。

5.DestroyJavaVM:java虚拟机析构钩子，一般是当虚拟机关闭时用来清理或者释放资源。

6.NettyServerSelector_x_x:Netty的Work线程（IO线程），这里可能有多根线程。

7.NettyServerWorkerThread_x:执行ChannelHandler方法的线程，ChannelHandler运行在该线程上，这里可能有多根线程。

8.RemotingExecutorThread_x:服务端逻辑线程，这里可能有多根线程。

rocketmq-namesrv扮演着nameNode角色，记录运行时消息相关的meta信息以及broker和filtersrv运行时信息，可以部署集群。