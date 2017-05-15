## RocketMQ学习（八）：事务消息



*摘要：* 源代码版本是3.2.6，还是直接跑源代码。rocketmq事务消息是发生在Producer和Broker之间，是二阶段提交。 二阶段提交过程看图： 第一阶段是：步骤1，2，3。 第二阶段是：步骤4，5。

源代码版本是3.2.6，还是直接跑源代码。rocketmq事务消息是发生在Producer和Broker之间，是二阶段提交。

二阶段提交过程看图：

![](http://lifestack.cn/wp-content/uploads/2015/09/%E4%BA%8B%E5%8A%A1%E9%80%BB%E8%BE%91.jpg)

第一阶段是：步骤1，2，3。
第二阶段是：步骤4，5。

具体说明：

只有在消息发送成功，并且本地操作执行成功时，才发送提交事务消息，做事务提交。

其他的情况，例如消息发送失败，直接发送回滚消息，进行回滚，或者发送消息成功，但是执行本地操作失败，也是发送回滚消息，进行回滚。

事务消息原理实现过程：

一阶段：
Producer向Broker发送1条类型为TransactionPreparedType的消息，Broker接收消息保存在CommitLog中，然后返回消息的queueOffset和MessageId到Producer，MessageId包含有commitLogOffset（即消息在CommitLog中的偏移量，通过该变量可以直接定位到消息本身），由于该类型的消息在保存的时候，commitLogOffset没有被保存到consumerQueue中，此时客户端通过consumerQueue取不到commitLogOffset，所以该类型的消息无法被取到，导致不会被消费。

一阶段的过程中，Broker保存了1条消息。

二阶段：
Producer端的TransactionExecuterImpl执行本地操作，返回本地事务的状态，然后发送一条类型为TransactionCommitType或者TransactionRollbackType的消息到Broker确认提交或者回滚，Broker通过Request中的commitLogOffset，获取到上面状态为TransactionPreparedType的消息（简称消息A），然后重新构造一条与消息A内容相同的消息B，设置状态为TransactionCommitType或者TransactionRollbackType，然后保存。其中TransactionCommitType类型的，会放commitLogOffset到consumerQueue中，TransactionRollbackType类型的，消息体设置为空，不会放commitLogOffset到consumerQueue中。

二阶段的过程中，Broker也保存了1条消息。

总结：事务消息过程中，broker一共保存2条消息。

贴代码：

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

TransactionCheckListenerImpl.java

```java
package com.zoo.quickstart.transaction;

import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.rocketmq.client.producer.LocalTransactionState;

import com.alibaba.rocketmq.client.producer.TransactionCheckListener;

import com.alibaba.rocketmq.common.message.MessageExt;

/**

* 未决事务，服务器回查客户端，broker端发起请求代码没有被调用，所以此处代码可能没用。

*/

public class TransactionCheckListenerImpl implements TransactionCheckListener {

private AtomicInteger transactionIndex = new AtomicInteger(0);

@Override

public LocalTransactionState checkLocalTransactionState(MessageExt msg) {

System.out.println("server checking TrMsg " + msg.toString());

int value = transactionIndex.getAndIncrement();

if ((value % 6) == 0) {

throw new RuntimeException("Could not find db");

}

else if ((value % 5) == 0) {

return LocalTransactionState.ROLLBACK_MESSAGE;

}

else if ((value % 4) == 0) {

return LocalTransactionState.COMMIT_MESSAGE;

}

return LocalTransactionState.UNKNOW;

}

}
```



本地操作类TransactionExecuterImpl.java

```java
package com.zoo.quickstart.transaction;

import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.rocketmq.client.producer.LocalTransactionExecuter;

import com.alibaba.rocketmq.client.producer.LocalTransactionState;

import com.alibaba.rocketmq.common.message.Message;

/**

* 执行本地事务

*/

public class TransactionExecuterImpl implements LocalTransactionExecuter {

private AtomicInteger transactionIndex = new AtomicInteger(1);

@Override

public LocalTransactionState executeLocalTransactionBranch(final Message msg, final Object arg) {

int value = transactionIndex.getAndIncrement();

if (value == 0) {

throw new RuntimeException("Could not find db");

}

else if ((value % 5) == 0) {

return LocalTransactionState.ROLLBACK_MESSAGE;

}

else if ((value % 4) == 0) {

return LocalTransactionState.COMMIT_MESSAGE;

}

return LocalTransactionState.UNKNOW;

}

}
```

Producer类：TransactionProducer.java

```java
package com.zoo.quickstart.transaction;

import com.alibaba.rocketmq.client.exception.MQClientException;

import com.alibaba.rocketmq.client.producer.SendResult;

import com.alibaba.rocketmq.client.producer.TransactionCheckListener;

import com.alibaba.rocketmq.client.producer.TransactionMQProducer;

import com.alibaba.rocketmq.common.message.Message;

/**

* 发送事务消息例子

*

*/

public class TransactionProducer {

public static void main(String[] args) throws MQClientException, InterruptedException {

TransactionCheckListener transactionCheckListener = new TransactionCheckListenerImpl();

TransactionMQProducer producer = new TransactionMQProducer("please_rename_unique_group_name");

// 事务回查最小并发数

producer.setCheckThreadPoolMinSize(2);

// 事务回查最大并发数

producer.setCheckThreadPoolMaxSize(2);

// 队列数

producer.setCheckRequestHoldMax(2000);

producer.setTransactionCheckListener(transactionCheckListener);

producer.setNamesrvAddr("192.168.0.104:9876");

producer.start();

String[] tags = new String[] { "TagA", "TagB", "TagC", "TagD", "TagE" };

TransactionExecuterImpl tranExecuter = new TransactionExecuterImpl();

for (int i = 0; i < 1; i++) {

try {

Message msg =

new Message("TopicTest", tags[i % tags.length], "KEY" + i,

("Hello RocketMQ " + i).getBytes());

SendResult sendResult = producer.sendMessageInTransaction(msg, tranExecuter, null);

System.out.println(sendResult);

Thread.sleep(10);

}

catch (MQClientException e) {

e.printStackTrace();

}

}

for (int i = 0; i < 100000; i++) {

Thread.sleep(1000);

}

producer.shutdown();

}

}
```