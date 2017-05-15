## RocketMQ学习（五）：Pull和Push

*摘要：* 源代码版本是3.2.6。在rocketmq里，consumer被分为2类：MQPullConsumer和MQPushConsumer，其实本质都是拉模式（pull），即consumer轮询从broker拉取消息。

源代码版本是3.2.6。在rocketmq里，consumer被分为2类：MQPullConsumer和MQPushConsumer，其实本质都是拉模式（pull），即consumer轮询从broker拉取消息。

区别是：

push方式里，consumer把轮询过程封装了，并注册MessageListener监听器，取到消息后，唤醒MessageListener的consumeMessage()来消费，对用户而言，感觉消息是被推送过来的。

pull方式里，取消息的过程需要用户自己写，首先通过打算消费的Topic拿到MessageQueue的集合，遍历MessageQueue集合，然后针对每个MessageQueue批量取消息，一次取完后，记录该队列下一次要取的开始offset，直到取完了，再换另一个MessageQueue。

文字描述可能不是很清楚，前面的文章都是push方式的，所以这里只上pull方式的，贴代码：

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

Producer：

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

producer.setNamesrvAddr("192.168.0.104:9876");

producer.start();

for (int i = 0; i < 5; i++) {

try {

Message msg = new Message("TopicTest",// topic

"TagA",// tag

("Hello RocketMQ " + i).getBytes()// body

);

SendResult sendResult = producer.send(msg);

System.out.println(sendResult);

Thread.sleep(6000);

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

Consumer:

```java
package com.zoo.quickstart.pull;

import java.util.HashMap;

import java.util.Map;

import java.util.Set;

import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;

import com.alibaba.rocketmq.client.consumer.PullResult;

import com.alibaba.rocketmq.client.exception.MQClientException;

import com.alibaba.rocketmq.common.message.MessageQueue;

/**

* PullConsumer，订阅消息

*/

public class PullConsumer {

private static final Map<MessageQueue, Long> offseTable = new HashMap<MessageQueue, Long>();

public static void main(String[] args) throws MQClientException {

DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("please_rename_unique_group_name_5");

consumer.setNamesrvAddr("192.168.0.104:9876");

consumer.start();

Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues("TopicTest");

for (MessageQueue mq : mqs) {

System.out.println("Consume from the queue: " + mq);

SINGLE_MQ: while (true) {

try {

PullResult pullResult =

consumer.pullBlockIfNotFound(mq, null, getMessageQueueOffset(mq), 32);

System.out.println(pullResult);

putMessageQueueOffset(mq, pullResult.getNextBeginOffset());

switch (pullResult.getPullStatus()) {

case FOUND:

// TODO

break;

case NO_MATCHED_MSG:

break;

case NO_NEW_MSG:

break SINGLE_MQ;

case OFFSET_ILLEGAL:

break;

default:

break;

}

}

catch (Exception e) {

e.printStackTrace();

}

}

}

consumer.shutdown();

}

private static void putMessageQueueOffset(MessageQueue mq, long offset) {

offseTable.put(mq, offset);

}

private static long getMessageQueueOffset(MessageQueue mq) {

Long offset = offseTable.get(mq);

if (offset != null)

return offset;

return 0;

}

}
```

还有一种定时的Consumer：

```java
package com.zoo.quickstart.pull;

import com.alibaba.rocketmq.client.consumer.MQPullConsumer;

import com.alibaba.rocketmq.client.consumer.MQPullConsumerScheduleService;

import com.alibaba.rocketmq.client.consumer.PullResult;

import com.alibaba.rocketmq.client.consumer.PullTaskCallback;

import com.alibaba.rocketmq.client.consumer.PullTaskContext;

import com.alibaba.rocketmq.client.exception.MQClientException;

import com.alibaba.rocketmq.common.message.MessageQueue;

import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;

public class PullScheduleService {

public static void main(String[] args) throws MQClientException {

final MQPullConsumerScheduleService scheduleService = new MQPullConsumerScheduleService("GroupName1");

scheduleService.getDefaultMQPullConsumer().setNamesrvAddr("192.168.0.104:9876");

scheduleService.setMessageModel(MessageModel.CLUSTERING);

scheduleService.registerPullTaskCallback("TopicTest", new PullTaskCallback() {

@Override

public void doPullTask(MessageQueue mq, PullTaskContext context) {

MQPullConsumer consumer = context.getPullConsumer();

try {

// 获取从哪里拉取

long offset = consumer.fetchConsumeOffset(mq, false);

if (offset < 0)

offset = 0;

PullResult pullResult = consumer.pull(mq, "*", offset, 32);

System.out.println(offset + "\t" + mq + "\t" + pullResult);

switch (pullResult.getPullStatus()) {

case FOUND:

break;

case NO_MATCHED_MSG:

break;

case NO_NEW_MSG:

case OFFSET_ILLEGAL:

break;

default:

break;

}

// 存储Offset，客户端每隔5s会定时刷新到Broker

consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());

// 设置再过100ms后重新拉取

context.setPullNextDelayTimeMillis(100);

}

catch (Exception e) {

e.printStackTrace();

}

}

});

scheduleService.start();

}

}


```