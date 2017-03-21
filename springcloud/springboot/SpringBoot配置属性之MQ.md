# SpringBoot配置属性之MQ

## activemq

- spring.activemq.broker-url指定ActiveMQ broker的URL，默认自动生成.
- spring.activemq.in-memory是否是内存模式，默认为true.
- spring.activemq.password指定broker的密码.
- spring.activemq.pooled是否创建PooledConnectionFactory，而非ConnectionFactory，默认false
- spring.activemq.user指定broker的用户.

## artemis(`HornetQ捐献给apache后的版本`)

- spring.artemis.embedded.cluster-password指定集群的密码，默认是启动时随机生成.
- spring.artemis.embedded.data-directory指定Journal文件的目录.如果不开始持久化则不必要指定.
- spring.artemis.embedded.enabled是否开启内嵌模式，默认true
- spring.artemis.embedded.persistent是否开启persistent store，默认false.
- spring.artemis.embedded.queues指定启动时创建的队列，多个用逗号分隔，默认: []
- spring.artemis.embedded.server-id指定Server ID. 默认是一个自增的数字，从0开始.
- spring.artemis.embedded.topics指定启动时创建的topic，多个的话逗号分隔，默认: []
- spring.artemis.host指定Artemis broker 的host. 默认: localhost
- spring.artemis.mode指定Artemis 的部署模式, 默认为auto-detected(也可以为native or embedded).
- spring.artemis.port指定Artemis broker 的端口，默认为: 61616

## rabbitmq

- spring.rabbitmq.addresses指定client连接到的server的地址，多个以逗号分隔.
- spring.rabbitmq.dynamic是否创建AmqpAdmin bean. 默认为: true)
- spring.rabbitmq.host指定RabbitMQ host.默认为: localhost)
- spring.rabbitmq.listener.acknowledge-mode指定Acknowledge的模式.
- spring.rabbitmq.listener.auto-startup是否在启动时就启动mq，默认: true)
- spring.rabbitmq.listener.concurrency指定最小的消费者数量.
- spring.rabbitmq.listener.max-concurrency指定最大的消费者数量.
- spring.rabbitmq.listener.prefetch指定一个请求能处理多少个消息，如果有事务的话，必须大于等于transaction数量.
- spring.rabbitmq.listener.transaction-size指定一个事务处理的消息数量，最好是小于等于prefetch的数量.
- spring.rabbitmq.password指定broker的密码.
- spring.rabbitmq.port指定RabbitMQ 的端口，默认: 5672)
- spring.rabbitmq.requested-heartbeat指定心跳超时，0为不指定.
- spring.rabbitmq.ssl.enabled是否开始SSL，默认: false)
- spring.rabbitmq.ssl.key-store指定持有SSL certificate的key store的路径
- spring.rabbitmq.ssl.key-store-password指定访问key store的密码.
- spring.rabbitmq.ssl.trust-store指定持有SSL certificates的Trust store.
- spring.rabbitmq.ssl.trust-store-password指定访问trust store的密码.
- spring.rabbitmq.username指定登陆broker的用户名.
- spring.rabbitmq.virtual-host指定连接到broker的Virtual host.

## hornetq

- spring.hornetq.embedded.cluster-password指定集群的密码，默认启动时随机生成.
- spring.hornetq.embedded.data-directory指定Journal file 的目录. 如果不开启持久化则不必指定.
- spring.hornetq.embedded.enabled是否开启内嵌模式，默认:true
- spring.hornetq.embedded.persistent是否开启persistent store，默认: false
- spring.hornetq.embedded.queues指定启动是创建的queue，多个以逗号分隔，默认: []
- spring.hornetq.embedded.server-id指定Server ID. 默认使用自增数字，从0开始.
- spring.hornetq.embedded.topics指定启动时创建的topic，多个以逗号分隔，默认: []
- spring.hornetq.host指定HornetQ broker 的host，默认: localhost
- spring.hornetq.mode指定HornetQ 的部署模式，默认是auto-detected，也可以指定native 或者 embedded.
- spring.hornetq.port指定HornetQ broker 端口，默认: 5445

## jms

- spring.jms.jndi-name指定Connection factory JNDI 名称.
- spring.jms.listener.acknowledge-mode指定ack模式，默认自动ack.
- spring.jms.listener.auto-startup是否启动时自动启动jms，默认为: true
- spring.jms.listener.concurrency指定最小的并发消费者数量.
- spring.jms.listener.max-concurrency指定最大的并发消费者数量.
- spring.jms.pub-sub-domain是否使用默认的destination type来支持 publish/subscribe，默认: false