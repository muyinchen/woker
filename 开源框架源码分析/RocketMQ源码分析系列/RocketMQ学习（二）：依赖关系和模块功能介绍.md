## RocketMQ学习（二）：依赖关系和模块功能介绍



*摘要：* 现在看的代码版本还是3.2.2 develop。先看张内部结构代码图： 从依赖层次再来看，越是被依赖的，越在底层： rocketmq包含9个子模块： rocketmq-common：通用的常量枚举、基类方法或者数据结构，按描述的目标来分包通俗易懂。

现在看的代码版本还是3.2.2 develop。先看张内部结构代码图：

![rocketmq内部依赖图](http://lifestack.cn/wp-content/uploads/2015/04/rocketmq%E5%86%85%E9%83%A8%E4%BE%9D%E8%B5%96%E5%9B%BE.jpg)

从依赖层次再来看，越是被依赖的，越在底层：

![层次结构](http://lifestack.cn/wp-content/uploads/2015/04/%E5%B1%82%E6%AC%A1%E7%BB%93%E6%9E%84.jpg)

rocketmq包含9个子模块：

rocketmq-common：通用的常量枚举、基类方法或者数据结构，按描述的目标来分包通俗易懂。包名有：admin，consumer，filter，hook，message等。

rocketmq-remoting：用Netty4写的客户端和服务端，fastjson做的序列化，自定义二进制协议。

rocketmq-srvutil：只有一个ServerUtil类，类注解是，只提供Server程序依赖，目的为了拆解客户端依赖，尽可能减少客户端的依赖。

rocketmq-store：存储服务，消息存储，索引存储，commitLog存储。

rocketmq-client:客户端，包含producer端和consumer端，发送消息和接收消息的过程。

rocketmq-filtersrv:消息过滤器server，现在rocketmq的wiki上有示例代码及说明，https://github.com/alibaba/RocketMQ/wiki/filter_server_guide，以后会专门对每个模块做分析，到时出个完整的demo以及流程图。

rocketmq-broker：对consumer和producer来说是服务端，接收producer发来的消息并存储，同时consumer来这里拉取消息。

rocketmq-tools：命令行工具。

rocketmq-namesrv：NameServer，类似SOA服务的注册中心，这里保存着消息的TopicName，队列等运行时的meta信息。一般系统分dataNode和nameNode，这里是nameNode。

这里可能说的比较粗糙，后面将会一一介绍。