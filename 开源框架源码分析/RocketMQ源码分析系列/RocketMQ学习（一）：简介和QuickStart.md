## RocketMQ学习（一）：简介和QuickStart

*摘要：* RocketMQ是什么？ 引用官方描述： RocketMQ是一款分布式、队列模型的消息中间件，具有以下特点： 支持严格的消息顺序 支持Topic与Queue两种模式 亿级消息堆积能力 比较友好的分布式特性 同时支持Push与Pull方式消费消息 历经多次天猫双十一海量消息考验 RocketMQ是纯java编写，基于通信框架Netty。

RocketMQ是什么？

引用官方描述：
RocketMQ是一款分布式、队列模型的消息中间件，具有以下特点：

支持严格的消息顺序
支持Topic与Queue两种模式
亿级消息堆积能力
比较友好的分布式特性
同时支持Push与Pull方式消费消息
历经多次天猫双十一海量消息考验

RocketMQ是纯java编写，基于通信框架Netty。

代码地址：https://github.com/alibaba/RocketMQ，目前分支是3.2.2 develop。

下载完代码后，将各个模块导入eclipse，本地尝试启动看看。

1.启动nameServer，运行rocketmq-namesrv的NamesrvStartup，运行之前需设置环境变量ROCKETMQ_HOME为RocketMQ项目的根目录，这样有一个作用是，指向logback的配置文件路径，保证在nameServer启动时，logback的正常初始化。我本机设置的是：ROCKETMQ_HOME=C:\Users\Administrator\git\RocketMQ。
The Name Server boot success. 表示启动成功。

2.启动brokerServer，运行rocketmq-broker的BrokerStartup，同样，运行之前需设置环境变量ROCKETMQ_HOME，然后启动参数需要带上【-n “192.168.0.109:9876″】，我本机的ip是192.168.0.109。如果不带-n的参数，那么broker会去访问http://jmenv.tbsite.net:8080/rocketmq/nsaddr获取nameServer的地址，这个地址不是我们自己的nameServer。
The broker[LENOVO-PC, 192.168.0.109:10911] boot success. and name server is 192.168.0.109:9876表示成功。

3.这个非必选项，不运行也可以。还可以启动rocketmq-srvutil的FiltersrvStartup，这是Consumer使用Java代码，在服务器做消息过滤。启动方式和broker一样，具体的过滤原理以后再详细的说。

到此就可以运行demo了。
pom.xml依赖：

```xml
<dependencies>

<dependency>

<groupId>ch.qos.logback</groupId>

<artifactId>logback-classic</artifactId>

<version>1.0.13</version>

</dependency>

<dependency>

<groupId>ch.qos.logback</groupId>

<artifactId>logback-core</artifactId>

<version>1.0.13</version>

</dependency>

<dependency>

<groupId>com.alibaba.rocketmq</groupId>

<artifactId>rocketmq-client</artifactId>

<version>3.2.2</version>

</dependency>

<dependency>

<groupId>junit</groupId>

<artifactId>junit</artifactId>

<version>4.10</version>

<scope>test</scope>

</dependency>

</dependencies>
```

如果依赖包下载不下来，再给个仓库地址，开源中国的：

```xml
<repositories>

<repository>

<id>nexus</id>

<name>Nexus</name>

<url>http://maven.oschina.net/content/groups/public/</url>

<releases>

<enabled>true</enabled>

</releases>

<snapshots>

<enabled>true</enabled>

</snapshots>

</repository>

</repositories>
```

贴代码：
Producer

```java
package com.zoo.quickstart;

import com.alibaba.rocketmq.client.exception.MQClientException;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;

import com.alibaba.rocketmq.client.producer.SendResult;

import com.alibaba.rocketmq.common.message.Message;

/**

* Producer，发送消息

*

*/

public class Producer {

public static void main(String[] args) throws MQClientException, InterruptedException {

DefaultMQProducer producer = new DefaultMQProducer("please_rename_unique_group_name");

producer.setNamesrvAddr("192.168.0.109:9876");

producer.start();

for (int i = 0; i < 1000; i++) {

try {

Message msg = new Message("TopicTest",// topic

"TagA",// tag

("Hello RocketMQ " + i).getBytes()// body

);

SendResult sendResult = producer.send(msg);

System.out.println(sendResult);

Thread.sleep(3000);

}

catch (Exception e) {

e.printStackTrace();

Thread.sleep(3000);

}

}

producer.shutdown();

}

}
```

Consumer

```java
package com.zoo.quickstart;

import java.util.List;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;

import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;

import com.alibaba.rocketmq.client.exception.MQClientException;

import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;

import com.alibaba.rocketmq.common.message.MessageExt;

/**

* Consumer，订阅消息

*/

public class Consumer {

public static void main(String[] args) throws InterruptedException, MQClientException {

DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("please_rename_unique_group_name_4");

consumer.setNamesrvAddr("192.168.0.109:9876");

/**

* 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费<br>

* 如果非第一次启动，那么按照上次消费的位置继续消费

*/

consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

consumer.subscribe("TopicTest", "*");

consumer.registerMessageListener(new MessageListenerConcurrently() {

@Override

public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,

ConsumeConcurrentlyContext context) {

System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs);

System.out.println(" Receive Message Size: " + msgs.size());

return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

}

});

consumer.start();

System.out.println("Consumer Started.");

}

}
```

因为demo代码来自于rocketmq-example，所以没有上传Github。

ps：以前rocketmq在Github开源的时候没有学习，后来突然有一天发现Github上404了，心里后悔莫急，这次rocketmq重新开源出来，一定不能错过了。