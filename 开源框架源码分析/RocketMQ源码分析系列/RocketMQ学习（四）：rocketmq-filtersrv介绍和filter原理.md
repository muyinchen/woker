## RocketMQ学习（四）：rocketmq-filtersrv介绍和filter原理

*摘要：* 源代码版本是3.2.6，还是直接跑源代码，启动配置参照前面写的《简介和quickstart》，启动顺序是namesrv，broker，filtersrv，filter和broker有顺序要求，如果filtersrv启动后找不到broker，则会System.exit()退出程序。

源代码版本是3.2.6，还是直接跑源代码，启动配置参照前面写的《简介和quickstart》，启动顺序是namesrv，broker，filtersrv，filter和broker有顺序要求，如果filtersrv启动后找不到broker，则会System.exit()退出程序。

## 看下启动图：

![](http://lifestack.cn/wp-content/uploads/2015/05/filter%E5%8E%9F%E7%90%86.jpg)

看rocketmq-filtersrv代码，核心processor包下的只有一个Class类且只处理2种类型的请求，即DefaultRequestProcessor.processRequest()只处理RequestCode.REGISTER_MESSAGE_FILTER_CLASS和RequestCode.PULL_MESSAGE：

REGISTER_MESSAGE_FILTER_CLASS：接收consumer端注册过来的filterClass源代码的请求。

PULL_MESSAGE：接收consumer端发出的拉消息的请求。

## 看下filter流程：



## 看代码：

```xml
<properties>

<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

<logback.version>1.0.13</logback.version>

<rocketmq.version>3.2.6</rocketmq.version>

</properties>

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

<version>${rocketmq.version}</version>

</dependency>

<dependency>

<groupId>junit</groupId>

<artifactId>junit</artifactId>

<version>4.10</version>

<scope>test</scope>

</dependency>

</dependencies>
```



## filter类：

```java
package com.zoo.quickstart.filter;

import com.alibaba.rocketmq.common.filter.MessageFilter;

import com.alibaba.rocketmq.common.message.MessageExt;

public class MessageFilterImpl implements MessageFilter {

@Override

public boolean match(MessageExt msg) {

String property = msg.getUserProperty("SequenceId");

if (property != null) {

int id = Integer.parseInt(property);

if ((id % 3) == 0 && (id > 10)) {

return true;

}

}

return false;

}

}
```



## producer类：

```java
package com.zoo.quickstart.filter;

import com.alibaba.rocketmq.client.exception.MQClientException;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;

import com.alibaba.rocketmq.client.producer.SendResult;

import com.alibaba.rocketmq.common.message.Message;

public class Producer {

public static void main(String[] args) throws MQClientException, InterruptedException {

DefaultMQProducer producer = new DefaultMQProducer("ProducerGroupName");

producer.setNamesrvAddr("192.168.0.119:9876");

producer.start();

try {

for (int i = 0; i < 6000000; i++) {

Message msg = new Message("TopicFilter7",// topic

"TagA",// tag

"OrderID001",// key

("Hello MetaQ").getBytes());// body

msg.putUserProperty("SequenceId", String.valueOf(i));

SendResult sendResult = producer.send(msg);

System.out.println(sendResult);

Thread.sleep(3000);

}

}

catch (Exception e) {

e.printStackTrace();

}

producer.shutdown();

}

}
```



## consumer类：

```java
package com.zoo.quickstart.filter;

import java.util.List;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;

import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;

import com.alibaba.rocketmq.client.exception.MQClientException;

import com.alibaba.rocketmq.common.MixAll;

import com.alibaba.rocketmq.common.message.MessageExt;

public class Consumer {

public static void main(String[] args) throws InterruptedException, MQClientException {

DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("ConsumerGroupNamecc4");

consumer.setNamesrvAddr("192.168.0.119:9876");

// 使用Java代码，在服务器做消息过滤

String filterCode = MixAll.file2String("D:\\workspace\\rocketmq-quickstart\\src\\main\\java\\com\\zoo\\quickstart\\filter\\MessageFilterImpl.java");

consumer.subscribe("TopicFilter7", "com.zoo.quickstart.filter.MessageFilterImpl",

filterCode);

consumer.registerMessageListener(new MessageListenerConcurrently() {

@Override

public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,

ConsumeConcurrentlyContext context) {

System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs);

return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

}

});

/**

* Consumer对象在使用之前必须要调用start初始化，初始化一次即可<br>

*/

consumer.start();

System.out.println("Consumer Started.");

}

}
```