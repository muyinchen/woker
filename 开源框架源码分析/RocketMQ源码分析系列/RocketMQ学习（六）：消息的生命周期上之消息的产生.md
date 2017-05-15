## RocketMQ学习（六）：消息的生命周期上之消息的产生

*摘要：* 源代码版本是3.2.6。消息的生命周期包括2部分，消息的产生和消息的消费，这篇先说下前者。消息的产生详细一点可以分为: a.消息产生后由Producer发送至Broker。 b.Broker接收到消息做持久化。

源代码版本是3.2.6。消息的生命周期包括2部分，消息的产生和消息的消费，这篇先说下前者。消息的产生详细一点可以分为:

a.消息产生后由Producer发送至Broker。

b.Broker接收到消息做持久化。

调试代码得到这样的过程，

1.DefaultMQProducer.send()发出消息。

2.DefaultMQProducerImpl.sendDefaultImpl()发出消息。

3.DefaultMQProducerImpl.tryToFindTopicPublishInfo()，即向Namesrv发出GET_ROUTEINTO_BY_TOPIC的请求，来更新
MQProducerInner的topicPublishInfoTable和MQConsumerInner的topicSubscribeInfoTable。

4.调用topicPublishInfo.selectOneMessageQueue()，从发布的topic中轮询取出一个MessageQueue。默认一个topic对应4个MessageQueue。

5.调用mQClientFactory.findBrokerAddressInPublish(mq.getBrokerName())，获取brokerAddr（broker的地址）。

6.调用this.mQClientFactory.getMQClientAPIImpl().sendMessage(
brokerAddr,// 1
mq.getBrokerName(),// 2
msg,// 3
requestHeader,// 4
timeout,// 5
communicationMode,// 6
sendCallback// 7
)发送。

7.调用MQClientAPIIImpl.sendMessageSync(addr, brokerName, msg, timeoutMillis, request)发送。

8.调用NettyRemotingClient.invokeSyncImpl()发送。

\######到此Producer端发消息结束######

———我是分割线———-

\######接着Request走到Broker######

9.SendMessageProcessor.processRequest(),接收到消息，封装requestHeader成broker内部的消息MessageExtBrokerInner，然后DefaultMessageStore.putMessage(msgInner)，调用CommitLog.putMessage(msg)。

10.调用MapedFileQueue.getLastMapedFile()获取将要写入消息的文件，mapedFile.appendMessage(msg,this.appendMessageCallback)写入消息。

11.AppendMessageCallback.doAppend(fileFromOffset, byteBuffer,maxBlank,Object msg),用回调方法存储msg。

12.MessageDecoder.createMessageId(this.msgIdMemory, msgInner.getStoreHostBytes(),wroteOffset)，用存储消息的节点ip和端口，加上准备写的偏移量（就是在前面获取的文件中）生成msgId。

13.以（topic-queueId）为key从topicQueueTable取queueOffset,queueOffset如果为null则设为0，存入topicQueueTable。

14.调用MessageSysFlag.getTransactionValue(msgInner.getSysFlag())获取tranType来判断该消息是否是事务消息，如果是TransactionPreparedType或者TransactionRollbackType，则queueOffset=0，这2种类型的消息是不会被消费的。见16，17。

15.调用byteBuffer.put(this.msgStoreItemMemory.array(), 0, msgLen)写入文件。

16.构造DispatchRequest，然后DispatchMessageService.putRequest(dispatchRequest)，异步DispatchMessageService.doDispatch()，分发消息位置信息到ConsumeQueue。如果是TransactionPreparedType或者TransactionRollbackType，则不处理，如果是TransactionNotType或者TransactionCommitType，则调用DefaultMessageStore.this.putMessagePostionInfo()。

17.调用ConsumeQueue.putMessagePostionInfo()，20个字节大小的buffer在内存里，offset即消息对应的在CommitLog的offset，size即消息在CommitLog存储中的大小，tagsCode即计算出来的长整数，写入buffer，this.mapedFileQueue.getLastMapedFile(expectLogicOffset)获取mapedFile，最后mapedFile.appendMessage(this.byteBufferIndex.array())写入文件，作逻辑队列持久化。

说明：当Broker接收到从Consumer发来的拉取消息的请求时，根据请求的Topic和queueId获取对应的ConsumerQueue，由于消息的类型是预备消息或者回滚消息时，不作持久化（即没有把消息体本身存储在CommitLog中的offset保存到ConsumerQueue中），那么自然也找不到该消息的逻辑存储单元（也就是前面的20个字节，根据这20个字节可以在CommitLog中定位到一条消息），最终Consumer也取不到该消息。

打个比喻，CommitLog是书的正文，消息体存在于CommitLog中，相当于是书正文中的一个章节，那么ConsumerQueue就是书的目录，记录着章节和页数的对应关系，如果是预备类型或者回滚类型的章节，目录中没有记录，即使在书的正文中存在，但是我们查找章节时都是通过目录来查找的，目录里没有，就找不到该章节。

18.DefaultMessageStore.this.indexService.putRequest(this.requestsRead.toArray())，新建索引。