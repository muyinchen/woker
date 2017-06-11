# varnish详解

# 第一章    http缓存的基础概念

------

### 1、程序运行时具有局部性特征

- 时间局部性
  缓存的数据往往被打有时间缀，具有定期失效的特征，过期后会从源服务器检验请求验证是否需要重新拉取数据，某数据被访问后，该数据往往会再次在短时间内被访问到。
- 空间局部性
  被访问数据的周边数据被访问的概率会比其它常规数据访问大很多，所以这些访问数据和其它周边有可能被访问的数据通过某种方式集中在一起，以提高数据的被访问速度，减少数据查找时长。
  完成这类功能的工具往往称为Cache
- 热区：内容具有热区效应，也就是部分数据的访问频率很高，而有些数据的访问频率相对较低，只有存在热区效应服务数据才应该会有缓存，因为如果所有资源访问量都很大，那么就应该对所有内容的访问速率提速

### 2、缓存命中率：hit/(hit+miss)

- 文档命中率：从文档个数进行衡量
- 字节命中率：从内容的大小进行衡量

### 3、缓存的生命周期：

- 重启后失效

### 4、缓存清理：

- 缓存项过期：惰性清理或其他方式清理，惰性清理是指将缓存条目标记为失效，然后其占用的空间标记为可用，这样后续的缓存项可以直接覆盖其占用的空间
- 缓存空间耗尽：LRU最近最少使用

### 5、数据能否缓存的依据，取决于数据是私有的还是公开的

- 私有数据：不可缓存，可以缓存在私有缓存上(如用户的浏览器本身的缓存就是私有缓存)
- 公共缓存：可公开的一些数据，可缓存在公共缓存上(如用户端的正向代理服务器，CDN服务器，服务器端的反向代理服务器…)

### 6、http缓存处理的步骤：

- 接受请求–>解析请求(提取请求的URL及各种首部)–>查询缓存–>判断缓存的有效性–>构建响应报文–>发送响应–>记录日志

### 7、缓存相关的http的首部信息

- expire：缓存的有效期限，是一个绝对时间，如2016-11-11 08：08：08类似

- if-modified-since：表示基于last-modified机制，缓存服务器到后端真实服务器上判断缓存有效性时，判断自从某个绝对时间点后，缓存内容是否发生过更改

- if-none-match：基于Etag机制，用于缓存服务器向后端服务器验证缓存有效性

- public：可被所有公共缓存缓存，尽可以在响应报文中

- private

- no-store：在响应报文中表示该内容不可缓存，在请求报文中表示不能从缓存中进行响应

- no-cache：

- - 在响应报文中表示可以缓存，但是客户端端请求的内容缓存命中后，不能直接用缓存直接响应，而是要先到原始服务器上进行校验缓存有效性。
  - 在请求报文中表示要先到原始服务器上进行校验有效性后才能用缓存的内容进行响应

- max-age：以秒为单位指定的相对时长(300000)，表示缓存的最大有效期，是一个相对时长，与expire类似，但是expire给明的有效期是绝对日期(2016-11-11 08：08：08类似)

- s-maxage：与max-age类似，但是仅仅作用于公共缓存

### 8、http cache解决方案：

- squid&varnish
  squid、varnish
  squid和varnish的关系类似于httpd与nginx的关系
  早期的缓存服务器的常见方案是Squid
  在中小规模的应用场景中，Varnish比Squid轻量级，但是单个Varnish所能承载的并发访问量在5000个左右
  在较大规模的应用场景中，仍然可能看到Squid
- varnish是一款专业的http cache：
  其可以接受http请求，并解析http请求，根据请求来查找缓存，如果没有缓存项，则将请求转发到后端真实服务器上，真实服务器响应后，判断响应的报文是否可缓存，如果可缓存，则缓存后，再响应给用户

## 缓存控制机制

### 1、通过过期日期来控制：

- HTTP/1.0响应报文首部中的过期日期机制：
  Expires:Mon, 25 Sep 2017 01 :00 :31 GMT 指明了具体过期的时间和日期
- HTTP/1.1响应报文首部中的过期日志机制：
  Cache-Control:max-age=31104000 max-age指明了一个相对时长，可缓存多少秒

### 2、通过Cache-Control，缓存控制相关的首部,请求报文可和响应报文都可以有该首部，其可容纳的参数不一样

- 对请求首部可接受的参数：(用于客户端向服务器端传递其接受的缓存的方式)

- - no-cache：通知缓存服务器，必须要进行验证缓存的新鲜性，否则不会接受任何缓存文档(在浏览器中用Ctrl+F5就是实现了此种功能)
  - no-strore：告知缓存服务器，必须尽快删除缓存中的文档(可能是因为其包含用户的敏感信息)
  - max-age：相对的时间，类似于Expires首部的效果一样，只是相对时长，如300000之类的数字，默认单位为秒
  - max-stale：告知缓存服务器，可以使用任意缓存文件(包括过期的文件进行响应)
  - min-fresh：


- 对响应首部可接受的参数：

- - public：可以放在公共缓存上，公共缓存是指除了客户端的自身浏览器的缓存，其他任何位置都是公共缓存
  - private：可以放在私有缓存上进行缓存
  - no-cache：表示能缓存，但是当客户端下次请求同样的资源时，不能直接用缓存的内容响应给客户端，而是要到原始服务器验证缓存的有效性
  - no-store：不可缓存，表示响应的内容不允许缓存
  - must-revalidate：必须重新校验，表示内容缓存下来后，当用户请求同样的内容时，必须要原始服务器上进行校验缓存有效性
  - max-age：可缓存的时长，相对时长，也就是从此刻开始，可以缓存多少秒
  - s-max-age：公共缓存服务器的缓存，可缓存的时长。控制公共缓存最大有效期，如果没有公共缓存的定义，也能使用私有缓存中的数据(也就是s-max-age可以不用指明，但是如果没有指明，如果公共缓存中的数据需要缓存，缓存的有效时长就取决于max-age)

### 3、新鲜度检测的机制(缓存有效性)的概述

- 有效性再验证

- - 如果原始内容未改变，则仅响应首部(不需要附带body部分);响应码为304(not modified)
  - 如果原始内容发生了改变，则用新的内容正常响应，响应码为200
  - 如果原始内容消失，则响应码为404，此时缓存中的缓存项也应该被删除

- 条件式请求首部

- - if-Modified-Since：自从某时间后是否发生过改变，基于原始内容的最近一个修改的时间戳进行有效性再验证;
  - if-Unmodified-Since：自动某时间后是否没有发生过改变
  - if-none-match/if-match:基于Etag机制的条件式请求的首部

　

#### 4、如何保证缓存的新鲜度的(新鲜度验证的详细说明)：取决于缓存的管理机制

- **(1)文档过期机制：**
  在有效期内，利用的都是缓存的内容，此种方式有可能造成，当缓存有效，但实际服务器上的数据已经发生变化，而用户看到的内容依然是缓存的内容

- - 在HTTP/1.0版本中，有个首部叫Expires(过期时间)，使用的是绝对时间，如2016-10-18 18 :28 :58类似这种指明具体的绝对的过期时间
  - 在HTTP/1.1版本中，有个首部叫Cache-Control字段中有一个叫max-age=XX的字段用来控制，使用的是相对时长，如可以使用2000秒

- **(2)条件式请求：**

- - 基于时间的条件式请求：mtime: if-modified-since|if-Unmodified-Since
    相当于客户端发送请求，缓存服务器收到请求后，如果缓存服务器缓存有请求的资源，不会直接使用该资源响应用户请求，而是缓存服务器去向原始服务器询问，是否有该资源，如果没有资源，则证明缓存的已经没用了，就返回给客户端没有对应的资源;如果实际服务器上有对应的资源，缓存服务就将自己缓存的资源的时间戳提供给原始服务器，询问原始服务器上资源在此时间戳之后有没有发生过修改，如果没有发生过修改，原始服务器则返回304，Not Modified给缓存服务器，证明缓存服务器上的资源与原始服务器的资源是一致的，如果在时间戳之后发生过修改，则原始服务器将修改后的资源重新发送给缓存服务器，缓存服务器重新缓存，然后发送给客户端
  - 基于扩展标签的条件式请求 :ETag : if-none-match|if-Match
    想象此类情况：当请求的资源的时间戳发生了改变，但是文件内容却没有发生改变(如touch现有的文件);或者有些文件的内容修改的速度非常快，在毫秒级别，但是文件的时间戳只能精确到秒级别，此时就会发现文件的时间戳没变，但是文件内容已经发生改变
    此种情况下，我们给每个文件一个版本号(专业称呼为：扩展标记ETag)，一旦文件内容发生改变，扩展标记就会发生改变，内容不变，扩展标记也不变，当缓存服务器与后端服务器进行比对时，就是比对扩展标记，如果扩展标记不一致，就重新缓存，这样就实现了基于文件内容的判断机制

- **(3)文件有效期机制和条件式请求机制的结合使用：**

- - 在文档有效期内，用缓存的内容去响应，但是当文档有效期到期后，不是立即到后端服务器上请求新内容来覆盖缓存服务器上的原有缓存内容，而是通过条件式请求机制(if-modified-since或if-none-match)去向后端真实服务器，判断当前缓存服务器上的缓存与后端服务器上的真实数据是否一致，如果一致，则继续使用

> 注意：文档过期机制(Expires和Cache-Control:max-age=)一般是在响应首部中，而条件式请求机制(if-modified-since或if-none-match)一般出现在请求首部中

## http相关首部的回顾

### 1、通用首部：响应报文和请求报文都可用到的一些首部

- Connection：close|keep-alive 表示使用短连接机制或长连接机制，HTTP1.1默认是keep-alive，HTTP1.0默认是close
- Date：日期时间，请求(响应)报文产生时的时间点
- Host：请求的主机信息
- Pagram： no-cache 如果在请求报文中，则表示客户端请求时，如果中间有缓存服务器，则必须要到原始服务器上做新鲜度验证。如果在响应报文中，则表示缓存服务器可以缓存该报文内容，但不能直接用该缓存去响应客户端请求，而是要在响应客户端请求时先到后端服务器上验证数据的新鲜性
- Via: 请求或响应消息在客户端与服务器之间传递时，所经过的代理
- Transfer-Encoding：消息主体的传输编码方式，用于保障数据在客户端和服务器端传递时的安全机制，常用值为chunked，表示采用块编码的方式

### 2、请求首部：只能用在请求报文部分的首部

- if-modified-since：向后端服务器验证所缓存的数据是否是最新(基于修改时间戳的验证方式)
- if-none-match：向后端服务器验证所缓存的数据是否是最新的(基于验证资源的ETag扩展标记)
- Referer：通过点击那个页面的跳转链接跳转过来的
- User-Agent：用户代理，也就是用户浏览器类型
- Host：请求的主机，必要时包括端口号
- Accept-Encoding：表示接受的编码方式，是否支持压缩等
- Accept-Language：接受的自然语言，中文，英文等
- Accept-Charset：支持的字符集编码
- Autorization：服务端发送www-authenticate时，客户端通过此首部提供认证信息

### 3、响应首部：只能用在响应报文部分的首部

- ETag：内容的扩展标签，提供给前端缓存，用于验证缓存的内容与实际服务器上的内容是否一致
- Location：重定向后的新的资源位置
- Server：服务器软件信息版本号等
- WWW-Authenticate：要求对客户端进行验证，通常此时的状态码为401

### 4、实体首部：也就是响应报文BODY部分用到的相关首部

- Content-Encoding：内容编码
- Content-Language：内容语言
- Content-Length：响应内容本身的长度
- Content-Type：当前内容的MIME类型
- Expires：内容的过期期限
- Last-Modified：最后一次的修改时间
- Cache-Control相关的定义

------

# 第二章    varnish基础理论简介

------

### 1、varnish的程序结构

- varnish主要运行两个进程：Management进程和Child进程(也叫Cache进程)。

- Management进程主要实现应用新的配置、编译VCL、监控varnish、初始化varnish以及提供一个命令行接口等。Management进程会每隔几秒钟探测一下Child进程以判断其是否正常运行，如果在指定的时长内未得到Child进程的回应，Management将会重启此Child进程。

- Child进程包含多种类型的线程，常见的如：

  > Varnish依赖“工作区(workspace)”以降低线程在申请或修改内存时出现竞争的可能性。在varnish内部有多种不同的工作区，其中最关键的当属用于管理会话数据的session工作区。

- - Acceptor线程：接收新的连接请求并响应；
  - Worker线程：child进程会为每个会话启动一个worker线程，此worker线程真正来管理缓存，构建响应报文，因此，在高并发的场景中可能会出现数百个worker线程甚至更多；
  - Expiry线程：从缓存中清理过期内容；
    ![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345351143715.png)

### 2、varnish日志

- varnish通过可以基于文件系统接口进行访问的共享内存区域来记录日志，为了与系统的其它部分进行交互，Child进程使用了可以通过文件系统接口进行访问的共享内存日志(shared memory log)，因此，如果某线程需要记录信息，其仅需要持有一个锁，而后向共享内存中的某内存区域写入数据，再释放持有的锁即可。而为了减少竞争，每个worker线程都使用了日志数据缓存。
- 共享内存日志大小一般为90M，其分为两部分，前一部分为计数器，后半部分为客户端请求的数据。varnish提供了多个不同的工具如varnishlog、varnishncsa或varnishstat等来分析共享内存日志中的信息并能够以指定的方式进行显示。
- 当日志区域超过90M后，默认情况下前面的日志将会被后面的日志覆盖，如果希望保存超出90M空间限制的日志，可以开启varnishncsa服务

### 3、varnish的缓存存储机制(也就是缓存存储在哪)

- varnish支持多种不同类型的后端存储，这可以在varnishd启动时使用-s选项指定。后端存储的类型包括：

- - <1>file：自管理的文件系统，使用特定的一个文件存储全部的缓存数据，并通过操作系统的mmap()系统调用将整个缓存文件映射至内存区域(如果内存大小条件允许)；varnish重启时，所有缓存对象都将被清除
  - <2>malloc：使用malloc()库调用在varnish启动时向操作系统申请指定大小的内存空间以存储缓存对象；varnish重启时，所有缓存对象都将被清除
  - <3>persistent：与file的功能相同，但可以持久存储数据(即重启varnish数据时不会被清除)；仍处于测试期；


- varnish无法追踪某缓存对象是否存入了缓存文件，从而也就无从得知磁盘上的缓存文件是否可用，因此，file存储方法在varnish停止或重启时会清除数据。而persistent方法的出现对此有了一个弥补，但persistent仍处于测试阶段，例如目前尚无法有效处理要缓存对象总体大小超出缓存空间的情况，所以，其仅适用于有着巨大缓存空间的场景。

- 选择使用合适的存储方式有助于提升系统性，从经验的角度来看，建议在内存空间足以存储所有的缓存对象时使用malloc的方法，反之，file存储将有着更好的性能的表现。然而，需要注意的是，varnishd实际上使用的空间比使用-s选项指定的缓存空间更大，一般说来，其需要为每个缓存对象多使用差不多1K左右的存储空间，这意味着，对于100万个缓存对象的场景来说，其使用的缓存空间将超出指定大小1G左右。另外，为了保存数据结构等，varnish自身也会占去不小的内存空间。

- 各种存储方式的参数格式：

  > file中的granularity用于设定缓存空间分配单位，也就是当我们size假设设置为20G，这20G的空间不是一次性分配的，而是初始时比方说分配1G，用完后，逐步增加，一次增加的多大(此即为granularity)，默认单位是字节，一次次的增加，直到达到size大小

- - malloc[,size]
  - file[,path[,size[,granularity]]]
  - persistent,path,size

　

## vcl简介

> varnish的缓存策略相关配置，需要借助vcl来实现，系统上默认的vcl的配置文件为/etc/varnish/default.vcl

### 1、VCL是什么

- VCL，Varnish Configuration Language 是varnish配置缓存策略的工具，它是一种基于“域”(domain specific，可想象与iptables的几个链，也就是类似钩子函数)的简单编程语言，它支持有限的算术运算和逻辑运算操作、允许使用正则表达式进行字符串匹配、允许用户使用set自定义变量、支持if判断语句，也有内置的函数和变量等。
- 使用VCL编写的缓存策略通常保存至.vcl文件中，其需要编译成二进制的格式后才能由varnish调用。事实上，整个缓存策略就是由几个特定的子例程如vcl_recv、vcl_hash等组成，它们分别在不同的位置(或时间)执行，如果没有事先为某个位置自定义子例程，varnish将会执行默认的定义。
- VCL策略在启用前，会由management进程将其转换为C代码，而后再由gcc编译器将C代码编译成二进制程序。编译完成后，management负责将其连接至varnish实例，即child进程。正是由于编译工作在child进程之外完成，它避免了装载错误格式VCL的风险。因此，varnish修改配置的开销非常小，其可以同时保有几份尚在引用的旧版本配置，也能够让新的配置即刻生效。编译后的旧版本配置通常在varnish重启时才会被丢弃，如果需要手动清理，则可以使用varnishadm的vcl.discard命令完成。

### 2、vcl的状态引擎

![img](http://book.varnish-software.com/4.0/_images/simplified_fsm.svg)

- vcl_recv：接受用户请求进varnish的入口的引擎，接受到结果之后，利用return(lookup)，将请求转交给vcl_hash引擎进行处理
- vcl_hash：接受到用户请求后，对用户请求的URL进行hash计算，根据请求的首部信息，以及hash结果进行下一步处理的引擎
- vcl_hit：经过vcl_hash引擎处理后，发现用户请求的资源本地有缓存，则vcl_hash引擎通过return(hit)将请求交给vcl_hit引擎进行处理，vcl_hit引擎处理后将请求交给vcl_deliver引擎，vcl_deliver引擎构建响应报文，响应给用户
- vcl_miss：经过vcl_hash引擎处理后，发现用户请求的资源本地没有缓存，则vcl_hash引擎通过return(miss)将请求交给vcl_miss引擎进行处理
- vcl_purge：经过vcl_hash引擎处理后，发现请求是对缓存的内容进行修剪时，则通过return(purge)交给vcl_purge引擎进行处理，vcl_purge引擎处理后，利用vcl_synth引擎将处理的结果告知给用户
- vcl_pipe：经过vcl_hash引擎处理后，发现用户请求的报文varnish无法理解，则通过return(pipe)，将请求交给vcl_pipe引擎，pipe引擎直接将请求交给后端真实服务器
- vcl_pass：当请求经过vcl_hash处理后，发现请求报文不让从缓存中进行响应或其他原因没办法查询缓存，则由return(pass)或return(hit-for-pass)交由vcl_pass引擎进行处理
- vcl_backend_fetch：当发现缓存未命中或由vcl_pass传递过来的某些不能查询缓存的请求，交由vcl_backend_fetch引擎处理，vcl_backend_fetch引擎会向后端真实web服务器发送请求报文，请求对应的资源
- vcl_backend_response：当后端发送响应报文到varnish后，会由vcl_backend_resonse引擎进行处理，如：判断响应的内容是否可缓存，如果能缓存，则缓存下来后，交给vcl_deliver引擎，如果不能缓存，则直接交给vcl_deliver引擎，vcl_deliver引擎构建响应报文给客户端


- varnish4.0版本的两个特殊的引擎

- - vcl_init：在处理任何请求之前要执行的vcl的代码，主要用于初始化VMODS，可用在后端主机有多台时，借助此引擎完成多台主机的负载均衡效果
  - vcl_fini：所有的请求都已经结束，在vcl配置被丢弃时调用;主要用于清理VMODS

> **因此，常见的状态引擎之间的处理流程为：**
>
> - 如果缓存命中：
>   用户请求–>vcl_recv–>vcl_hash–>vcl_hit–>vcl_deliver–>响应给用户
> - 如果缓存未命中：
>   用户请求–>vcl_recv–>vcl_hash–>vcl_miss–>vcl_backend_fetch–>后端服务器接受请求发送响应报文–>vcl_backend_response–>vcl_deliver
>   或：
>   用户请求–>vcl_recv–>vcl_hash–>vcl_miss–>vcl_pass–>vcl_backend_fetch–>后端服务器接受请求发送响应报文–>vcl_backend_response–>vcl_deliver–>响应给用户
> - 如果不能从缓存中进行响应
>   用户请求–>vcl_recv–>vcl_hash–>vcl_pass–>vcl_backend_fetch–>后端服务器接受请求发送响应报文–>vcl_backend_response–>vcl_deliver–>响应给用户
> - 如果进行缓存修剪
>   用户请求–>vcl_recv–>vcl_hash–>vcl_purge–>vcl_synth–>返回给用户
> - 如果请求报文无法理解
>   用户请求–>vcl_recv–>vcl_hash–>vcl_pipe–>交给后端服务器

### 3、vcl语法格式

- <1>配置文件第一个非注释行必须是vcl 4.0，标明此vcl配置文件是基于vcl4.0版本
- <2>//、#或/* comment */用于单行或多行注释
- <3>sub $NAME 定义函数，子例程
- <4>不支持循环，支持条件判断，有内置变量
- <5>使用终止语句return(XXX)，没有返回值，仅仅是标明下一步交给哪个状态引擎
- <6>域专用，语句用{ }括起来，用sub声明，指明为哪一段的专用代码，如：sub vcl_recv{…}，可理解为一个配置段
- <7> 每个语句必须以;分号结尾
- <8> 每个变量有其能使用的引擎的位置，可理解为变量由其可用的配置段
- <9>操作符：=(赋值)、==(等值比较)、~(模式匹配)、!(取反)、&&(逻辑与)、||(逻辑或)、>(大于)、>=(大于等于)、<(小于)、<=(小于等于)

> - 注意，各个状态引擎有其默认的配置，无论各个vcl引擎内的配置如何修改，该状态引擎的默认的配置都会自动附加在修改的配置之后生效
> - 查看默认的配置可以用：varnishadm -S /etc/varnish/secret -T 127.0.0.1:6082 –> vcl.show -v 配置名称

### 4、vcl常见内建的函数

VCL提供了几个函数来实现字符串的修改，添加bans，重启VCL状态引擎以及将控制权转回Varnish等。

- regsub(str,regex,sub)和regsuball(str,regex,sub)：
  这两个用于基于正则表达式搜索指定的字符串并将其替换为指定的字符串；但regsuball()可以将str中能够被regex匹配到的字符串统统替换为sub，regsub()只替换一次；
- ban(expression)：清除能被表达式匹配的所有缓存对象
- ban_url(regex)：清除所有其URL能够由regex匹配的缓存对象；
- hash_data(str)：对指定的字符串做hash计算后的结果
- return()：
  当某VCL域运行结束时将控制权返回给Varnish，并指示Varnish如何进行后续的动作；其可以返回的指令包括：lookup、hash、hit、miss、pass、pipe、hit_for_pass、purge等；但某特定域可能仅能返回某些特定的指令，而非前面列出的全部指令；
- return(restart)：
  重新运行整个VCL，即重新从vcl_recv开始进行处理；每一次重启都会增加req.restarts变量中的值，而max_restarts参数则用于限定最大重启次数。

> VCL的函数不接受参数并且没有返回值，因此，其并非真正意义上的函数，这也限定了VCL内部的数据传递只能隐藏在HTTP首部内部进行。
> VCL的return语句用于将控制权从VCL状态引擎返回给Varnish进程，而非默认函数，这就是为什么VCL只有终止语句而没有返回值的原因。同时，对于每个“域”来说，可以定义一个或多个终止语句，以告诉Varnish下一步采取何种操作，如查询缓存或不查询缓存等。

### 5、vcl的内建变量的分类

- req.*：req.开头的变量，由客户端发来的http请求相关的变量

  ```
    如：
    req.method  表示客户端的请求方法
    req.url  表示客户端请求的url
    req.http.host  表示客户端请求报文的主机
    req.http.*  *可以是http请求报文的任意首部的名称，代表引用http的某个请求首部
  ```

- bereq.* ：bereq.开头的变量，varnish主机在向后端真实服务器发送http请求报文时的相关变量

  ```
    如：
    可以将真实的客户端地址传递给后端真实web服务器，以便于后端真实服务器记录客户端的真实IP，而不是varnish的IP
  ```

- - breq.http.* 代表varnish发往后端的真实的web服务器的相关的请求报文中的首部


- beresp.*：beresp.开头的变量，由后端真实服务器发来的http响应报文中的某些首部信息相关的变量，一般是在vcl_backend_response或vcl_backend_fenth引擎中调用


- resp.*：resp.开头的变量，由varnish响应给客户端的响应报文相关的变量

- - 一般用在vcl_deliver引擎中进行调用，因为deliver引擎是用于给客户端构建响应报文，发送响应报文
  - resp.http.*： *可以是响应报文中的任意首部的名称，代表引用某个响应报文的值，可用于设定、添加、修改响应给客户端的响应报文中的响应首部的值


- obj.* ：obj.开头的变量，对存储在缓存空间中的缓存对象属性的引用变量。obj开头的变量都是只读的

  ```
    如：
    obj.hits： 某个缓存对象的缓存的命中次数
  ```


- client.*，server.*，storage.*：可用在所有面向客户端一侧的引擎中，也就是vcl_recv、vcl_pipe、vcl_hash、vcl_pass、vcl_purge、vcl_miss、vcl_hit、vcl_deliver、vcl_synth中

  ```
    如：
    client.ip  代表客户端的IP地址
    server.ip  代表当前varnish的IP地址
    client.port  代表客户端的端口
    server.port  代表当前varnish的端口
  ```


- 用户自定义变量

- - 可用set，来设定某个用户自定义变量或现有变量的值
  - 可用unset，来取消某个用户自定义变量，或删除现有变量

### 6、vcl内建变量的可用在哪些vcl引擎配置段和相应的读写权限

![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345406992051.png)

### 7、vcl常见的内置变量的说明

- bereq.*

- - bereq.http.HEADERS: 表示varnish发往后端真实web服务器的请求报文中的某个首部
  - bereq.request: 表示varnish发往后端真实web服务器的请求报文的请求方法(4.0版本的varnish改为了bereq.method)
  - bereq.url：表示varnish发往后端真实web服务器的请求报文的请求的url
  - bereq.proto：表示varnish发往后端真实web服务器的请求报文的http协议的协议版本
  - bereq.backend：表示要varnish发送请求到后端真实web服务器时，后端服务器不止一台时，所调用的后端主机


- beresp.*

- - beresp.http.HEADERS：表示后端真实web服务器发给varnish的http响应报文的某个首部的信息
  - beresp.proto：表示后端真实web服务器发给varnish的http响应报文的http协议版本
  - beresp.status：表示后端真实web服务器发给varnish的http响应报文的响应状态码
  - beresp.backend.name：表示后端真实web服务器发给varnish的http响应报文的后端主机的名称
  - beresp.ttl：后端服务器响应中的内容的余下的生存时长


- obj.*

- - obj.hit 此对象在缓存中命中的次数
  - obj.ttl 此对象的ttl值，也就是其缓存时长


- server.*

- - server.ip 当前varnish的IP
  - server.hostname 当前varnish的主机名


- req.*

- - req.http.HEADERS: 表示客户端发送给varnish的请求报文中的某个首部
  - req.request: 表示客户端发送给varnish的请求报文的请求方法(4.0版本的varnish改为了req.method)
  - req.url：表示客户端发送给varnish的请求报文的请求的url
  - req.proto：表示客户端发送给varnish的请求报文的http协议的协议版本


- resp.*

- - resp.http.HEADERS：表示varnish发送给客户端的响应报文的某个首部的信息
  - resp.proto：表示varnish发送给客户端的http响应报文的http协议版本
  - resp.status：表示varnish发送给客户端的http响应报文的响应状态码


- 自定义变量：可用set 变量名= 值 来设定变量

  ```
    如：set resp.http.X-Cache = "HIT"
    表示设定响应给客户端的响应报文中设定X-Cache首部的值为HIT

    如：set resp.http.IS-Cache = "YES"+" "server.ip
    表示设定响应给客户端的响应报文中的IS-Cache首部的值为"YES varnish服务器IP"，多个元素之间要用+加号连接，如果要输出空格，需要用""引号引起来
  ```

- 取消某变量：用unset 变量名

  ```
    如：
    unset req.http.cookie
    表示取消客户端请求报文中http的cookie首部信息
    unset beresp.http.Set-cookie
    表示取消后端服务器发送到varnish上的响应报文http首部中的Set-cookie首部
  ```

------

# 第三章    varnish的安装配置

------

### 1、安装：

```bash
varnish已被收录进EPEL源，故可直接利用EPEL源进行yum安装
yum install -y varnish
```

### 2、安装生成的程序环境

```properties
监听的端口为：
    服务监听的端口默认为6081
    管理接口默认监听的端口为6082

/etc/varnish/varnish.params： 
    配置varnish服务进程的工作特性，官方提供的rpm包安装的程序，其对应的程序自身配置文件在/etc/sysconfig/varnishd，例如监听的地址和端口，缓存机制；

/etc/varnish/default.vcl：
    配置各Child/Cache线程的工作属性；

主程序：
    /usr/sbin/varnishd

命令行管理工具程序：
    /usr/bin/varnishadm

Shared Memory Log交互工具：
    /usr/bin/varnishhist
    /usr/bin/varnishlog
    /usr/bin/varnishncsa
    /usr/bin/varnishstat
    /usr/bin/varnishtop    

测试工具程序：
    /usr/bin/varnishtest

VCL配置文件重载程序：
    /usr/sbin/varnish_reload_vcl

Systemd Unit File：
    /usr/lib/systemd/system/varnish.service
        varnish服务
    /usr/lib/systemd/system/varnishlog.service
    /usr/lib/systemd/system/varnishncsa.service    
        日志持久的服务；
```

![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345453174601.png)

### 3、varnish程序自身的配置文件介绍

> 自身的配置文件为：/etc/varnish/varnish.params
> 有的版本的rpm包安装的可能是/etc/sysconfig/varnishd

```properties
RELOAD_VCL=1
    设置为1表示当使用systemctl reload varnish时，会自动重新装载vcl的配置文件，也就是能够让新的配置生效
VARNISH_VCL_CONF=/etc/varnish/default.vcl
    加载的缓存策略的配置文件路径
#VARNISH_LISTEN_ADDRESS=
    varnish服务监听的地址，默认是监听在本机所有可用的地址上
VARNISH_LISTEN_PORT=6081
    varnish监听的端口，因为varnish要作为web服务器的反代进行工作时，才能将http的内容缓存，因此，一般要将其改为80端口，但是实际生产环境中，varnish一般是处于前端调度器的后面，所以可以在前端调度器上将调度的端口改为此处的端口也可以
VARNISH_ADMIN_LISTEN_ADDRESS=127.0.0.1
    varnish管理接口监听的地址，监听在127.0.0.1表示只允许从本机登录进行管理
VARNISH_ADMIN_LISTEN_PORT=6082
    varnish管理接口监听的端口
VARNISH_SECRET_FILE=/etc/varnish/secret
    varnish管理时的秘钥文件
VARNISH_STORAGE="file,/var/lib/varnish/varnish_storage.bin,1G"
    varnish缓存时，使用哪种存储方式对缓存内容进行存储，本处是指使用file文件方式，存在/var/lib/varnish/varnish_storage.bin文件中，总共使用1G大小的空间
    如果要使用内存缓存，则可以定义为："malloc,400M"
    在很多生产环境还是使用file,但是将文件放在固态硬盘，如果希望性能更好点，放在PCI-E的固态硬盘
VARNISH_TTL=120
    如果后端服务器没有指明缓存内容的TTL时间，则varnish自身为缓存定义的TTL时间
VARNISH_USER=varnish
VARNISH_GROUP=varnish

以上选项，实际是varnishd运行时调用时读取的变量，实际还有很多参数要指明，如varnish线程池的数量，以及每个线程池的线程数，此时要在varnish程序自身的配置文件中，利用
DAEMON_OPTS="-p thread_polls=2 -p thread_pool_min=100 -p thread_pool_max=5000 -p thread_pool_timeout=300"
    用-p指明参数和对应的值，其指明的选项有很多，具体可以参照man varnishd手册
```

### 4、varnishadm命令行管理工具的使用

varnishadm的工作模式有两种

- 一种是直接shell命令行模式下进行：

  ```bash
    varnishadm [-t TIMEOUT] [-S SECRET_FILE] -T [ADDRESS]:PORT COMMAND...
        -t 指明连接varnish管理的超时时间，一般无需指定
        -S 指明与varnish通行时用到的秘钥文件路径，也就是我们在/etc/varnish/varnish.params中定义的VARNISH_SECRET_FILE的值所指定的秘钥文件的路径
        -T [ADDRESS]:PORT   指明varnish管理的地址和端口，也就是我们在/etc/varnish/varnish.params中定义的VARNISH_ADMIN_LISTEN_ADDRESS和VARNISH_ADMIN_LISTEN_PORT，如果地址不给定，则默认为本地
        COMMAND  指明要运行的varnish命令
  ```

- 另一种是varnishadm的交互式模式

  ```bash
    varnishadm [-t TIMEOUT] [-S SECRET_FILE] -T [ADDRESS]:PORT
    运行此命令后，即可进入交互式命令模式
  ```

- 交互式命令模式下的varnish命令介绍
  ![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345528487628.png)
  ![img](http://i.imgur.com/ZywO54k.png)
  ![img](http://i.imgur.com/ItyAH16.png)
  ![img](http://i.imgur.com/rIXm1b9.png)
  ![img](http://i.imgur.com/SPohp3C.png)
  ![img](http://i.imgur.com/t1uT10O.png)
  ![img](http://i.imgur.com/91iDxUm.png)

### 5、vcl_recv状态引擎的默认配置介绍

> 进入vcl的交互式界面，用vcl.show -v 编译后的配置名称
> 即可显示各个状态引擎默认的配置，各个状态引擎的默认配置会自动附加在各个状态引擎自定义的配置段的后面生效

![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345601918782.png)

------

# 第四章    vcl配置示例

------

> **以具体配置，说明varnish的常见配置**
> 配置环境说明：
> ![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345718195312.png)

### 1、配置各个节点的网络环境，安装各个节点所需的服务器过程

- nginx调度器的配置
  ![img](http://i.imgur.com/XwNoMPH.png)
  ![img](http://i.imgur.com/dK2xisc.png)
- 静态httpd的web服务器1的配置(httpd2.2的配置，如果是httpd2.4则对应修改下配置文件，显示授权网页目录即可)
  ![img](http://i.imgur.com/n3gvtgT.png)
  ![img](http://i.imgur.com/aqMm7iE.png)
- 静态httpd的web服务器2的配置
  ![img](http://i.imgur.com/dpODtjL.png)
  ![img](http://i.imgur.com/AUipQDz.png)

### 2、配置varnish服务器，将请求反代到后端的一台主机上

![img](http://i.imgur.com/GPzQogT.png)
![img](http://i.imgur.com/NFSPLAQ.png)
![img](http://i.imgur.com/y2mG03B.png)

### 3、如何验证缓存是否命中

> 可通过在vcl_deliver状态引擎上加上一个判断机制，然后设定给客户端的响应报文的某个首部的值来实现

![img](http://i.imgur.com/J7hXwNB.png)
![img](http://i.imgur.com/lOtVfLV.png)
![img](http://i.imgur.com/UCPXjcP.png)
![img](http://i.imgur.com/XqDtOwu.png)

### 4、如何获知是从哪个varnish服务器命中的缓存

> 可通过在上个实验的基础上，在响应给客户端的报文的首部的值中，添加上当前varnish服务器的IP来实现

![img](http://i.imgur.com/jwYPyAO.png)
![img](http://i.imgur.com/b4nlF6r.png)
![img](http://i.imgur.com/g443p4I.png)

### 5、限定客户端的某些请求不进行缓存

> 当某些情况下，我们希望客户端访问的某些网页路径不能缓存时，如访问的是相管管理页面时，不进行缓存，则可以在vcl_recv引擎中针对用户请求的报文的URL进行过滤，因为是根据客户端的请求报文的相关信息进行条件匹配，因此应该在vcl_recv状态引擎中定义相关策略
> **通过此种方式，可以对客户端请求报文中的任意首部信息进行过滤，满足或不满足一定的条件时，我们可以定义其可不可以缓存**

![img](http://i.imgur.com/MlHmGzv.png)
![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345890427127.png)

![2.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345905840744.png)

![3.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479345924380868.png)

### 6、删除客户端的请求报文中的某些首部(如cookie信息)，或后端服务器响应的某些首部(如设定cookie的首部信息)，强制进行缓存,提高缓存命中率

> - 当请求报文首部中有cookie信息时，是很难命中缓存的，因为一旦有cookie信息，varnish中缓存的键值信息中键的值就是请求的url和cookie信息组成，然后hash计算出来的结果，这样就非常难以命中缓存，所以，对于一些不涉及敏感信息的cookie信息，我们应该在首部中进行清除之后，再进行缓存比匹配
> - **注意：为了提高缓存命中率，应该将请求报文中的无用的cookie信息移除，在响应报文中，无效的cookie信息也将其移除，以保证缓存命中率;要移除用户请求报文中的cookie信息，要在vcl_recv中进行，要移除后端服务器响应报文中的cookie信息，要在vcl_backend_response中进行**

- 示例1：假设，我们定义除了login和admin相关的cookie信息，其他页面的cookie信息都取消

  ```bash
    编辑vcl配置文件/etc/vanish/default.vcl配置文件，在对应的引擎段加入
    sub vcl_recv {
        if(!(req.url ~ "(?i)^/(login|admin)")) {
            unset req.http.cookie
        }
    }
    //表示针对客户端的请求报文中，如果其请求的url不是以/login或/admin开头的，则取消其请求报文首部中的cookie首部的信息，从而实现了提高缓存命中率的效果，因为请求首部中如果有cookie信息，是很难命中缓存的

    sub vcl_backend_response {
        if(!(bereq.url ~ "(?i)^/(login|admin)")) {
            unset beresp.http.Set-cookie
        }
    }
    //表示针对后端服务器的响应报文中，如果响应的请求的url不是以/login或/admin开始的，则取消其响应报文中的Set-cookie的首部，表示不对此类信息设定cookie，从而提高了缓存命中率
  ```

- 示例2：示例：对于特定类型的资源，如公开的图片等，取消其私有标识，并强行设定其可以由varnish缓存的时长

  ```bash
    sub vcl_backend_response {
        if (beresp.http.cache-control !~ "s-maxage") {
            if (bereq.url ~ "(?i)\.(jpg|pbg|gif|jpeg)$") {
                unset beresp.Set-Cookie;
                set beresp.ttl = 7200s;
            }
            if (bereq.url ~ "(?i)\.(css|js)$") {
                unset beresp.Set-Cookie;
                set beresp.ttl = 3600s;    
            }
        }

    }
    //表示当后端服务器的响应报文中，cache-control首部的值不匹配s-maxage，也就是没有设定公共缓存的时长的响应报文，如果请求到后端主机的请求报文中的url是以.jpg结尾("(?i)表示不区分大小写")，就设定其响应报文的缓存的ttl时长为7200秒，然后取消响应报文中的Set-Cookie的首部，进行缓存
    //请求到后端主机的请求报文中的url是以.css结尾("(?i)表示不区分大小写")，就设定其响应报文的缓存的ttl时长为7200秒，然后取消响应报文中Set-Cookie的首部，进行缓存
  ```

### 7、基于purge方式对缓存对象进行修剪

> - 提高缓存命中率的最有效途径之一是增加缓存对象的生存时间(TTL)，但是这也可能会带来副作用，比如缓存的内容在到达为其指定的有效期之间已经失效。因此，有时手动让缓存的内容失效，是必要的
> - purge方法：是对单个缓存项进行清理
> - ban方法：对符合ban提供的表达式的所有缓存项一起清理
> - 本例中使用purge方式对缓存进行修剪
> - 先要定义vcl_purge状态引擎、再针对客户端的请求报文的首部进行匹配，匹配到某个特定信息时，就调用定的purge引擎进行处理

![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346059903369.png)

![2.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346076648167.png)

![3.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346092760946.png)

![4.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346114469878.png)

### 8、vcl的acl访问控制机制，也就是基于IP的访问控制

> - 上个实验中，针对PURGE方法，如果其他人知道了利用此请求方法可以对缓存项进行修剪，而使用此方法请求页面数据，会造成很大的隐患，因此应该对其进行访问控制，也就是只允许来自哪些IP的访问请求使用PURGE方法
> - **根据此思路，可以实现基于客户端请求报文来源IP的访问控制机制**

![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346337195404.png)

![2.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346350235259.png)

![3.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346393627167.png)

![5.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346465174397.png)

![5.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346465174397.png)

![7.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346495627514.png)

![7.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346495627514.png)

- 基于此acl方法，还可以实现基于IP的访问控制，如：

  ```bash
    假设要拒绝某些IP的访问，则可以：
    acl testpurgers {
        "10.1.32.0"/24;
    }    

    sub vcl_recv {
        if (client.ip ~ testpurgers) {
             return(synth(405,"NO ACCESS"));
        }
    }
  ```

### 9、当varnish后端有多台服务器时(没台服务器提供不同的内容)，实现请求不同的资源，到不同的后端服务器上去请求

> 实际是先定义多个后端主机，然后判断请求报文的uri，利用设定req.backend_hint首部，让其对应到相关的后端服务器上

如：请求图片时，让varnish到后端的web2服务器上的第一个虚拟主机(也就是10.1.32.69的虚拟主机)上请求，如果请求的是文本资源(.txt结尾的资源)时，让其到web1服务器上的第二个虚拟主机上(10.1.32.168)请求，其他的资源都到web1服务器上的第一个虚拟主机(10.1.32.68)上请求
![2.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346822173717.png)

![2.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346822173717.png)

![3.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346830369837.png)

![4.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346842369811.png)

### 10、当后端同一类资源有多台主机时，对后端多台主机的负载均衡请求

> 实际就是将后端的多台主机定义为一个主机组，然后当满足一定条件时，将请求调度到该主机组，而不是调度到单台主机，通过引入vcl_init状态引擎，new一个主机组，然后想该主机组内添加定义的主机，调度时，向该主机组内进行调度即可

- 定义示例格式：

```bash
import directors;    # 需要导入directors功能

backend server1 {
    .host = 
    .port = 
}
backend server2 {
    .host = 
    .port = 
}

sub vcl_init {
    new GROUP_NAME = directors.round_robin();
    GROUP_NAME.add_backend(server1);
    GROUP_NAME.add_backend(server2);
}

sub vcl_recv {
    # send all traffic to the bar director:
    set req.backend_hint = GROUP_NAME.backend();
}
```

如：请求.txt内资源时，让varnish到后端的web2服务器上的第一个虚拟主机(也就是10.1.32.69的虚拟主机)上请求，和web1服务器上的第二个虚拟主机(10.1.32.168)上请求；如果请求的是.html资源时，让其到web1服务器上的第一个虚拟主机上(10.1.32.68)和web2服务器上的第二个虚拟主机(10.1.32.169)上请求

- 注意：此实验要想明显看到请求被调度到不同的后端主机上，需要请求的资源不能被缓存，因为一旦缓存了后，资源就直接从varnish缓存响应了，再次请求就不会看到被调度到其他后端主机的效果

![img](http://i.imgur.com/gkUh7iK.png)
![img](http://i.imgur.com/rdu4KQk.png)
![img](http://i.imgur.com/BMaq7dZ.png)
![img](http://i.imgur.com/ACjWylv.png)
![img](http://i.imgur.com/EOGGg6I.gif)

### 11、探测后端主机的健康性，当后端主机故障时，不往该主机上调度请求，当后端主机恢复后，又自动向该主机调度请求

> 在定义backend时，用.probe定义该主机对应的健康状况检测机制，当探测到某个主机不在线时，会自动将该后端主机移除
> 也可以单独定义健康状况检测的probe配置段，然后在各个后端主机上进行引用

```bash
backend BE_NAME {
    .host =  
    .port = 
    .probe = {
        .url= 
        .timeout= 
        .interval= 
        .window=
        .threshhold=
    }
}

.probe：定义健康状态检测方法；
    .url：检测时请求的URL，默认为”/"; 
    .request：发出的具体请求；(用于不使用.url检测时)
        .request = 
            "GET /.healthtest.html HTTP/1.1"
            "Host: www.nwc.com"
            "Connection: close"
    .window：基于最近的多少次检查来判断其健康状态； 
    .threshhold：最近.window中定义的这么次检查中至有.threshhold定义的次数是成功的；
    .interval：检测频度； 
    .timeout：超时时长；
    .expected_response：期望的响应码，默认为200；用于基于.request机制时定义响应机制

健康状态检测的配置方式：
    (1) probe PB_NAME = { }
         backend NAME = {
        .probe = PB_NAME;
        ...
         }

    (2) backend NAME  {
        .probe = {
            ...
        }
    }
```

- 示例：为上述实验的后端主机定义健康性检测机制

![img](http://i.imgur.com/oEfbGUn.png)
![img](http://i.imgur.com/2TH86II.png)
![img](http://i.imgur.com/y1inn5i.png)
![img](http://i.imgur.com/yRMgo8G.png)
![img](http://i.imgur.com/AWISTO7.png)
![img](http://i.imgur.com/cmPibnI.png)

------

# 第五章    varnish日志管理工具的使用

------

### 1、varnishstat命令

> varnish的缓存统计数据，类似htop命令的风格动态显示统计格式，会显示当前的数据，正在变化的而数据，平均数据，10分钟平均数据，100分钟平均数据，1000分钟平均数据

![img](http://i.imgur.com/HGyd07H.png)
![img](http://i.imgur.com/6fCMZeZ.png)
![img](http://i.imgur.com/IkyxBYV.png)
![img](http://i.imgur.com/unmmD8E.png)

### 2、varnishtop命令

> 可以类似系统上top命令一样，动态显示varnish的日志统计信息，按序排列，可以对其进行指定由哪个字段进行排序

```
只想显示一次，而不是动态刷新显示，则可以：
    varnishtop -1 
只想显示某个字段的值：
    varnishtop -i 字段名1,字段名2,...
只想显示1次某字段的值，而不是动态刷新显示：
    varnishtop -1 -i 字段名1,字段名2,...
除了某字段，都显示：
    varnishtop -x 字段名
除了某字段，都显示，但只显示刷新一次的结果：
    varnishtop -1 -x 字段名
```

![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346951644258.png)

![1.png](http://www.178linux.com/ueditor/php/upload/image/20161117/1479346951644258.png)

### 3、varnishlog命令

> 交互式显示varnish的日志，只是显示的格式是原始格式，也可以将varnishlog服务打开，让其脱离日志90M的限制，但是varnishlog服务和varnishncsa服务建议只开启一个，一般开启varnishnasa

![img](http://i.imgur.com/zxdrevq.png)

### 4、varnishncsa命令

> 交互式显示类型httpd的combined格式的日志
> varnish本身只有90M的空间来存储日志，超过90M的部分，新的日志会覆盖旧的日志，如果要记录更多的日志信息，则可以将varnishnasa服务打开，让其记录日志，日志格式为combined格式

![img](http://i.imgur.com/5GeCJLJ.png)
![img](http://i.imgur.com/SZU5n5T.png)

------

# 第六章    varnish性能优化

------

### 1、varnish的相关运行时参数

- thread_pool_max 5000 [threads]
  每个线程池最大启动线程池个数，最大不建议超过5000
- thread_pool_min 100 [threads]
  每个线程池最小启动的线程个数(额外的意义为，最大的空闲线程数)
- thread_pool_stack 48k [bytes]
  每个线程的栈的空间大小
- thread_pool_timeout 300.000 [seconds]
  线程池的超时时间，也就是一个线程空闲多长时间就会被关闭，因为线程池有最大和最小数量，线程个数动态在最大和最小之间动态调整
- thread_pools [pools]
  线程池个数，，默认为2个，最好小于或等于CPU核心数量
- thread_queue_limit 20 requests
  每个线程池最大允许的等待队列的长度
- thread_stats_rate 10 [requests]
  线程最大允许处理多少个请求后一次性将日志信息写入日志区域
- workspace_thread 2k [bytes]
  每个线程额外提供多大空间作为其请求处理的专用工作空间
- thread_pool_add_delay
  创建线程时的延迟时间，也就是，需要创建线程时，不是立即创建，而是延迟一段时间，说不定在此时间内有线程空闲下来，从而不用创建新的线程
- thread_pool_destroy_delay
  销毁线程时的延迟时间，也就是当需要销毁线程时，不是立即就销毁，而是等一段时间再销毁，以免有新的请求进来而导致需要新建新线程处理

> 最大的并发连接数=thread_pools*thread_pool_max

### 2、调整参数的方式一：交互式命令行模式

> varnish以及运行，在运行时进行调整其相关参数，让其动态生效，但是重启varnish服务后失效
> **注意：重启varnish会导致缓存全部失效，造成严重后果，慎重**

![img](http://i.imgur.com/Lhq5QSp.png)
![img](http://i.imgur.com/A8BU5ZH.png)
![img](http://i.imgur.com/gBjOewV.png)

### 3、调整参数方法二：配置文件模式

> - 在其配置文件/etc/varnish/varnish.params文件中，用DAEMON_OPTS="-p ARGS1=VALUE1 -p ARGS2=VALUE2 …"来设置，然后启动varnish
> - 此种方式适合于尚未启动varnish，直接在配置文件中修改其运行时参数，永久有效
> - 但是如果varnish已经启动，则如果重启varnish会导致缓存全部丢失，慎重
> - 如果已经启动，又想永久有效，可以在命令行下调整后，将相关的参数写到配置文件

![img](http://i.imgur.com/UAv0O5V.png)
![img](http://i.imgur.com/p4FP9KC.png)