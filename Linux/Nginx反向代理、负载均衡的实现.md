# Nginx反向代理、负载均衡的实现

## 概述：

​    上篇介绍了Nginx作为web服务器的一些常用配置的说明，但是在实际生产环境中，Nginx更多是作为前端的负载均衡器，反代前端用户请求到后端真实的web服务器上，完成LNAMP的组合的方式存在。本篇就介绍一些Nginx作为http的反向代理和前端负载均衡调度器的一些常用配置，具体包括：

​    1、ngx_http_proxy_module模块实现反代HTTP请求的配置

​    2、ngx_http_headers_module模块实现nginx响应报文中的首部定义

​    3、ngx_http_upstream_module模块实现nginx反代HTTP请求时的负载均衡

​    4、ngx_stream_core_module模块实现nginx反代TCP/UDP协议请求

​                   

## 第一章    ngx_http_proxy_module模块实现反代HTTP请求的配置

​    

###     1、nginx作为反向代理http请求时的相关配置选项

​    nginx作为前端接受用户请求的服务器，接收到用户请求后，nginx自己构建请求报文，向后端真实服务器进行请求，后端服务器响应内容是响应给nginx，nginx再将接受到的响应报文根据需要重新封装后响应给真实用户

​    nginx可以在反代用户请求到后端服务器时，将服务器响应的文件内容缓存到本地，下次用户再访问同样页面时，不用反代到后端服务器，而是直接利用本地的缓存进行响应，提升其效率

​    ![proxy.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477915962945690.png)

​    

###     2、nginx反向代理示例

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477922455917504.png)

​    <1> 配置好实验环境，在nginx反代服务器上安装nginx，在web1上安装LAMP，在web2上安装httpd

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477918519312556.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477918329185852.png)    

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477918436452448.png)

​    ![1477919031518126.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477922687713248.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477919537227640.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477919320473742.png)

​    <2> 在web1和web2上提供测试页面，启动web服务，验证服务是否正常

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477920939186900.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477920999621494.png)

​    <3> 在nginx主机上配置反代

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477921416716516.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477921510465182.png)

​    <4> 在客户端上测试访问

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477921568871476.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477922375604680.png)

​              

###     3、nginx反向代理时传递客户端IP给后端web服务器，实现日志中记录真实客户端IP的示例

```
   当通过nginx代理用户请求到httpd服务器时，httpd上访问日志记录的客户端IP全部是nginx的IP，这样不利于对日志进行
   分析统计，此时，我们就可以利用proxy_set_header指令，在nginx向后端发送请求报文时，在请求首部中添加进去一个真
   正客户端IP的首部
```

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477923025927243.png)

​    在nginx主机上设置nginx请求后端web服务器的请求报文首部

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477923327597345.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477923382216398.png)

​    在后端httpd服务器上，修改日志记录的格式，让其记录请求首部中我们定义的首部，从而实现记录真实客户端IP

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477923720963209.png)

​    测试访问，查看日志信息

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477923852465665.png)

​          

###     4、nginx反向代理http请求时，缓存后端响应内容的示例

```
    nginx的可以在反代用户请求到后端服务器时，将服务器响应的文件内容缓存到本地，下次用户再访问同样页面时，
    不用反代到后端服务器，而直接利用本地的缓存进行响应，提升其效率，缓存时key-value方式存储在内存中，key
    是可以指定的，一般是用户请求的URI，value值是相应URI对应的网页文件的特征码(如MD5码)，具体的执行结果将
    保存在磁盘上的某个文件，该文件的文件名为文件内容的特征码
    
    在上述实验的基础上，实现将后端的资源缓存到nginx服务上
```

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477924710445649.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477924863363847.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477925183182985.png)

​      

## 第二章    ngx_http_headers_module模块实现nginx响应报文中的首部定义

###     1、http_headers模块的相关配置指令介绍

​    http_headers模块可实现响应给客户端的报文中，添加自定义首部，或修改指定首部的值

​    ![headers.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477916383356650.png)    

###     2、添加nginx响应报文首部的的示例

```
    在上述实验的环境中，实现向nginx响应报文中添加首部，查看缓存是否命中
```

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477959994939932.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477960648317846.png)

​    

​      

## 第三章    ngx_http_upstream_module模块实现nginx反代HTTP请求时的负载均衡

###     1、upstream模块相关配置指令

​    该模块可以实现将多台服务器定义为一个服务器组，然后定义一定的调度算法和属性，当nginx作为http反代服务器，或者fastcgi反代，uwsgi反代，scgi反代，memcached反代时，利用各个反代的指令，如：proxy_pass, fastcgi_pass, uwsgi_pass, scgi_pass, and memcached_pass指令，将请求代理到定义的服务器组上，实现负载均衡调取器的作用

​    ![upstream.png](http://www.178linux.com/ueditor/php/upload/image/20161031/1477916473406995.png)

###     2、nginx实现反代用户请求到后端多台服务器时的负载均衡示例

```
    在上述实验的基础上，实验将请求负载均衡到后端的两台服务器
```

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477962271602774.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477962342995456.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477962363391188.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477962488183317.png)    

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477962532151407.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477964804691075.png)   

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477964855247030.png)

## 第四章    ngx_stream_core_module模块实现nginx反代TCP/UDP协议请求

###     1、stream模块的相关配置指令

​    该模块可以实现nginx基于tcp或udp协议进行反代，相当于可以反向代理tcp或udp传输层相关应用，因此可以将nginx模拟成为传输层的反代的调度器(类似LVS)

​    ![stream.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477964083345836.png)

###     2、nginx配置成为后端服务器ssh服务的反向代理示例

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477963822354889.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477963594412018.png)

​    ![blob.png](http://www.178linux.com/ueditor/php/upload/image/20161101/1477963644301344.png)