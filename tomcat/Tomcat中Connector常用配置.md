### Tomcat中Connector常用配置

   Tomcat中server.xml有些配置信息是需要我们了解的,最起码知道如何进行简单的调试.

 



`<Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000"  redirectPort="8443" />  `

​    默认情况下配置信息是这样的,在dev环境中,基本上可以用了.如下,简单的介绍Connector标签上还能挂载的其他配置项.

 

 

**1. port** 

​    tomcat作为一个网络server端,它需要暴露一个socket端口来accept客户端的链接,可以通过port指定.

**2. protocol**

​    使用的网络协议,表示tomcat使用何种方式来接受和处理client端请求,"HTTP/1.1"是默认值,等效于"org.apache.coyote.http11.Http11Protocol";还有熟悉的"AJP/1.3";关于HTTP和AJP两种方式的区别和性能优劣可以参见其他文档。【[文档](http://tomcat.apache.org/tomcat-8.0-doc/config/http.html#Connector_Comparison)】

​    在Tomcat 6.0之后,还提供了NIO的方式,可以有效的提升性能,特别是在大量长连接/数据上传+下载等web应用中.此时portocal="org.apache.coyote.http11.Http11NioProtocol".

​    tomcat目前支持：BIO、NIO、NIO2、APR四种IO模型，默认为BIO。对于互联网应用，我们应该在NIO、NIO2之间做选择，因为它能够有效的提升性能（主要是并发能力），其中NIO2即为AIO，需要JDK 1.7+、Linux 2.6+才能支持。

​    BIO：JDK 1.5+，tomcat 5.x+

​    NIO：JDK 1.6+，tomcat 6.x+

​    NIO2：JDK 1.7+，tomcat 7.x+

​    为了保守起见，我们暂且基于NIO模式。

**3. connectionTimeout**

​    当client与tomcat建立连接之后,在"connectionTimeout"时间之内,仍然没有得到client的请求数据,此时连接将会被断开.此值的设定需要考虑到网络稳定型,同时也有性能的考虑.它和tcp的配置选项中的"socket_timeout"仍有区别,connectionTimeout只会在链接建立之后,得到client发送http-request信息前有效.

​    默认值为60000，即60秒；对于互联网应用，此值我们应该设置合理，比如20000。

**4. maxHeaderCount**

​    http请求中header的最大个数,默认为100,"-1"表示不限制,通常不会关注此属性,不过在一些设计"扭曲"的web应用中,使用header传递大量参数(:post)和校验信息时,可能需要调整此值.如果请求中的header个数超过此限定值,请求将会被拒绝.（避免恶性攻击，建议此值设置为符合application的实际需要）

**5. maxParameterCount**

​    http-get请求中允许传递的查询字符串的最大个数,尽管各种http浏览器(proxy工具)都会对http-get请求的长度和查询字符串的个数有限制,你仍然可以通过tomcat再次设定合适的值.parameter个数越多,事实上对tomcat的内存开支更大,很多时候处于安全或者实用的角度考虑,maxParameterCount的值都不会太大.默认值为10000,"-1"表示无限制.如果请求中参数的个数超过限定值,请求将会被拒绝.（为了避免恶性攻击，请根据application实际需要设定此值。）

​    为了安全和规范，maxHeaderCount和maxParamterCount通常应该合理，建议设置为100等；如果请求参数再多，那么就建议使用post body发送或者拆分请求。

**6. maxPostSize**

​    http-post请求中数据(body)的最大尺寸,单位:byte,默认值为2M.这对一些表单提交(较多文本域)有影响.可以适度调整此值,大文件上传一般会在client拆分成小文件,而不是直接发送.

**7. URIEncoding**

​    http-get请求中,使用何种字符集对查询字符串进行编码,默认为"iso-8859-1".

**8. useBodyEncodingForURI**

​    是否使用"Content-type"中指定的编码方式对http-get请求中查询字符串进行编码.如果为"true",将会忽略"URIEncoding"配置项,转而使用header中"content-Type"指定的编码方式.

**9. maxThreads**

​    用于接收和处理client端请求的最大线程数,tomcat底层将采取线程池的方式来处理客户端请求,此参数标识这线程池的尺寸.maxThreads意味着tomcat能够并发执行request的个数.此值默认为200.一般情况下,在production环境中(根据物理机器配置,或者虚拟机的限制来做参考值),通常会有微调.较大的值并不能提升tomcat的负载能力,事实上"200"个线程数,已经足够大了.本人的线上环境为maxThreads=120.

​    对于NIO模式下，maxThreads参数应该由CPU核心数决定，乐观起见，此值为：cpu核数 * 2。太大的值，并不能提升NIO性能，反而会使性能下降，因为线程切换（CS）将会占据CPU的大量时间。

**10. compression**

​    是否对http相应数据启用Gzip压缩,可选值为"off"或者"on";这是一个值得商榷的参数;如果开启压缩,意味着较少的网络传输量,但是将消耗一定的CPU.如果你的应用有较高的CPU性能结余,且响应数据均是一些文本字符串,那么开启压缩,会有较大的收益.（并不是所有的浏览器都能够合理的支持gzip压缩，特别是低版本）

**11. acceptCount**

​    当tomcat请求处理线程池中的所有线程都处于忙碌状态时，此时新建的链接将会被放入到pending队列，acceptCount即是此队列的容量，如果队列已满，此后所有的建立链接的请求(accept)，都将被拒绝。默认为100。在高并发/短链接较多的环境中，可以适当增大此值；当长链接较多的场景中，可以将此值设置为0.

​    这个参数将会在创建ServerSocket时带入，为TCP底层参数。如果请求均为短连接、请求耗时较短，我们可以适当增加此值。
**12. address**

​    当物理server上绑定了多个IP地址时，可以通过“address”来指定tomcat-server需要bind的地址.默认将port关联到所有的ip上。
**13. bufferSize**

​    链接在读取stream时，buffer数据的尺寸。（非socket buffer）
**14. connectionLinger**

​    socket linger参数值。当socket即将关闭时(前)阻塞的时间，单位：秒。如果设置为-1,表示关闭linger。在BIO(Blocking IO)和AJP链接中默认为100,NIO中默认为25.
**15. keepAliveTimeout**

​    当无实际数据交互时，链接被保持的时间，单位：毫秒。在未指定此属性时，将使用connectionTimeout作为keepAliveTimeout。通常和“HTTP keepAlive”选项协调工作。对于HTTP请求，server端为了支撑较高的吞吐量，不可能无限制的keepAlive一个链接(在设计要求上，这个和TCP通讯有本质的区别)，keepAliveTimeout超时后，将会导致链接关闭。如果此tomcat设计为“长链接”服务，可以适当增加keepAliveTimeout值，否则无需设置此值。

 

​    不过我们通常在tomcat上层还有nginx等代理服务器，我们通常希望链接keepAlive的机制由代理服务器控制，比如nginx来决定链接是否需要“保持活性”（注意，与keep_alive不同），当然nginx服务器只会保留极少的长连接，几乎所有的链接都会在使用结束后主动close；因为链接复用层，将有nginx与client保持，而不再是tomcat与client保持。太多的keepAlive链接，尽管提高了链接使用效率，但是对负载均衡不利。
**16. maxKeepAliveRequests**

​    tomcat需要保持的最大请求数，即处于keepAlive状态的请求的个数，建议此值为maxThreads * 0.5,不得大于maxThreads，否则将得不到预期的效果。-1表示不限制，1表示关闭keepAlive机制。

**17、maxConnections**

​    tomcat允许接收和处理的最大链接数，对于BIO而言此值默认与maxThreads参数一样，对于NIO而言此值默认为10000。对于tomcat已经接受和正在处理的线程数达到此值，server将允许继续accept新链接但是不会处理它们，这些链接将会被阻塞直到连接数降低到此值（server将不会从这些socket中读取数据，而是将它们的句柄buffer起来）。最终server是否可以继续accept新的链接，取决于acceptCount值，因为此值是在创建ServerSocket时传递的参数，超过此值后，链接请求将会被拒绝。

   此值还受限于系统的ulimit、CPU、内存等配置。

**18、acceptorThreadCount：**默认为1，表示用于accept新链接的线程个数，如果在多核CPU架构下，此值可以设置为2，官方不建议设定超过2个的值。

**19、maxHttpHeaderSize：**http头的最大尺寸，默认为8192，单位为“字节”。

**20、minSpareThreads：**线程池中，保持活跃的线程的最小数量，默认为10。

**21、SSLEnabled：**是否开启ssl支持，默认为false；通常SSL应该在nginx等代理层，我们不应该让tomcat直接接入。

 

【如下为NIO配置】

**22、pollerThreadCount：**表示用于polling IO事件的线程个数，默认为1。在多核CPU架构下，我们可以设置为2来提高polling的能力，官方不建议设置大于2的值，因为锁的竞争会导致性能下降，事实上一个线程也足够快速。

**23、useSendfile：**是否开启sendfile特性，默认为true。对于web应用而言，通常project中还会包含一定数量的静态资源，比如图片、CSS、js、html等，sendfile在一定程度上可以提高性能。

**24、selectorTimeout：**选择器阻塞的时间，如果你进行过NIO开发，应该知道此参数的含义，默认值为1000毫秒。此值不要太大，因为selector线程本身还需要用来清理已关闭的链接等。

**25、selectorPool.maxSelectors：**NIO中选择的个数，默认值为200。NIO中我们可以使用多个selector，每个selector负责注册一定数量的NIOChannel，这样可以有效的提高selector选择效率；通常我们建议此值与maxThreads值一致，或者小于maxThreads，但是大于maxThreads其实意义并不大。

​    为了开启此特性，我们需要在catalina.sh中增加一个启动命令参数：

Java代码 

`CATALINA_OPTS="-Dorg.apache.tomcat.util.net.NioSelectorShared=false"  `

​    官方的意思是使用command-line-options="-Dorg.apache.tomcat.util.net.NioSelectorShared=false"，但是经过测试发现并不能生效。这个命令参数的意思是“不使用共享的selector，而是每个thread单独使用各自的selector。”

 

相应配置 :

``` xml
<Connector port="8080" protocol="org.apache.coyote.http11.Http11NioProtocol"  
               connectionTimeout="20000"  
               maxHeaderCount="64"  
               maxParameterCount="64"  
               maxHttpHeaderSize="8192"  
               URIEncoding="UTF-8"  
               useBodyEncodingForURI="false"  
               maxThreads="128"  
               minSpareThreads="12"  
               acceptCount="1024"  
               connectionLinger="-1"  
               keepAliveTimeout="60"  
               maxKeepAliveRequests="32"  
               maxConnections="10000"  
               acceptorThreadCount="1"  
               pollerThreadCount="2"  
               selectorTimeout="1000"  
               useSendfile="true"  
               selectorPool.maxSelectors="128"  
               redirectPort="8443" />  
```



参考：

https://tomcat.apache.org/tomcat-7.0-doc/config/http.html

https://tomcat.apache.org/tomcat-8.0-doc/config/http.html