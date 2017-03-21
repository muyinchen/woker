# SpringBoot配置属性之NOSQL

## cache

- spring.cache.cache-names指定要创建的缓存的名称，逗号分隔(若该缓存实现支持的话)
- spring.cache.ehcache.config指定初始化EhCache时使用的配置文件的位置指定.
- spring.cache.guava.spec指定创建缓存要使用的spec，具体详见CacheBuilderSpec.
- spring.cache.hazelcast.config指定初始化Hazelcast时的配置文件位置
- spring.cache.infinispan.config指定初始化Infinispan时的配置文件位置.
- spring.cache.jcache.config指定jcache的配置文件.
- spring.cache.jcache.provider指定CachingProvider实现类的全限定名.
- spring.cache.type指定缓存类型

## mongodb

- spring.mongodb.embedded.features指定要开启的特性，逗号分隔.
- spring.mongodb.embedded.version指定要使用的版本，默认: 2.6.10

## redis

- spring.redis.database指定连接工厂使用的Database index，默认为: 0
- spring.redis.host指定Redis server host，默认为: localhost
- spring.redis.password指定Redis server的密码
- spring.redis.pool.max-active指定连接池最大的活跃连接数，-1表示无限，默认为8
- spring.redis.pool.max-idle指定连接池最大的空闲连接数，-1表示无限，默认为8
- spring.redis.pool.max-wait指定当连接池耗尽时，新获取连接需要等待的最大时间，以毫秒单位，-1表示无限等待
- spring.redis.pool.min-idle指定连接池中空闲连接的最小数量，默认为0
- spring.redis.port指定redis服务端端口，默认: 6379
- spring.redis.sentinel.master指定redis server的名称
- spring.redis.sentinel.nodes指定sentinel节点，逗号分隔，格式为host:port.
- spring.redis.timeout指定连接超时时间，毫秒单位，默认为0

## springdata

- spring.data.elasticsearch.cluster-name指定es集群名称，默认: elasticsearch
- spring.data.elasticsearch.cluster-nodes指定es的集群，逗号分隔，不指定的话，则启动client node.
- spring.data.elasticsearch.properties指定要配置的es属性.
- spring.data.elasticsearch.repositories.enabled是否开启es存储，默认为: true
- spring.data.jpa.repositories.enabled是否开启JPA支持，默认为: true
- spring.data.mongodb.authentication-database指定鉴权的数据库名
- spring.data.mongodb.database指定mongodb数据库名
- spring.data.mongodb.field-naming-strategy指定要使用的FieldNamingStrategy.
- spring.data.mongodb.grid-fs-database指定GridFS database的名称.
- spring.data.mongodb.host指定Mongo server host.
- spring.data.mongodb.password指定Mongo server的密码.
- spring.data.mongodb.port指定Mongo server port.
- spring.data.mongodb.repositories.enabled是否开启mongodb存储，默认为true
- spring.data.mongodb.uri指定Mongo database URI.默认:mongodb://localhost/test
- spring.data.mongodb.username指定登陆mongodb的用户名.
- spring.data.rest.base-path指定暴露资源的基准路径.
- spring.data.rest.default-page-size指定每页的大小，默认为: 20
- spring.data.rest.limit-param-name指定limit的参数名，默认为: size
- spring.data.rest.max-page-size指定最大的页数，默认为1000
- spring.data.rest.page-param-name指定分页的参数名，默认为: page
- spring.data.rest.return-body-on-create当创建完实体之后，是否返回body，默认为false
- spring.data.rest.return-body-on-update在更新完实体后，是否返回body，默认为false
- spring.data.rest.sort-param-name指定排序使用的key，默认为: sort
- spring.data.solr.host
  指定Solr host，如果有指定了zk的host的话，则忽略。默认为: [http://127.0.0.1](http://127.0.0.1/):8983/solr
- spring.data.solr.repositories.enabled是否开启Solr repositories，默认为: true
- spring.data.solr.zk-host指定zk的地址，格式为HOST:PORT.