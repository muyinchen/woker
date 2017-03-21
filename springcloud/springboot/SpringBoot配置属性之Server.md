# SpringBoot配置属性之Server

## server配置

- server.address指定server绑定的地址
- server.compression.enabled是否开启压缩，默认为false.
- server.compression.excluded-user-agents指定不压缩的user-agent，多个以逗号分隔，默认值为:text/html,text/xml,text/plain,text/css
- server.compression.mime-types指定要压缩的MIME type，多个以逗号分隔.
- server.compression.min-response-size执行压缩的阈值，默认为2048
- server.context-parameters.[param name]设置servlet context 参数
- server.context-path设定应用的context-path.
- server.display-name设定应用的展示名称，默认: application
- server.jsp-servlet.class-name设定编译JSP用的servlet，默认: org.apache.jasper

.servlet.JspServlet)

- server.jsp-servlet.init-parameters.[param name]设置JSP servlet 初始化参数.
- server.jsp-servlet.registered设定JSP servlet是否注册到内嵌的servlet容器，默认true
- server.port设定http监听端口
- server.servlet-path设定dispatcher servlet的监听路径，默认为: /

## cookie、session配置

- server.session.cookie.comment指定session cookie的comment
- server.session.cookie.domain指定session cookie的domain
- server.session.cookie.http-only是否开启HttpOnly.
- server.session.cookie.max-age设定session cookie的最大age.
- server.session.cookie.name设定Session cookie 的名称.
- server.session.cookie.path设定session cookie的路径.
- server.session.cookie.secure设定session cookie的“Secure” flag.
- server.session.persistent重启时是否持久化session，默认false
- server.session.timeoutsession的超时时间
- server.session.tracking-modes设定Session的追踪模式(cookie, url, ssl).

## ssl配置

- server.ssl.ciphers是否支持SSL ciphers.
- server.ssl.client-auth设定client authentication是wanted 还是 needed.
- server.ssl.enabled是否开启ssl，默认: true
- server.ssl.key-alias设定key store中key的别名.
- server.ssl.key-password访问key store中key的密码.
- server.ssl.key-store设定持有SSL certificate的key store的路径，通常是一个.jks文件.
- server.ssl.key-store-password设定访问key store的密码.
- server.ssl.key-store-provider设定key store的提供者.
- server.ssl.key-store-type设定key store的类型.
- server.ssl.protocol使用的SSL协议，默认: TLS
- server.ssl.trust-store持有SSL certificates的Trust store.
- server.ssl.trust-store-password访问trust store的密码.
- server.ssl.trust-store-provider设定trust store的提供者.
- server.ssl.trust-store-type指定trust store的类型.

## tomcat

- server.tomcat.access-log-enabled是否开启access log ，默认: false)
- server.tomcat.access-log-pattern设定access logs的格式，默认: common
- server.tomcat.accesslog.directory设定log的目录，默认: logs
- server.tomcat.accesslog.enabled是否开启access log，默认: false
- server.tomcat.accesslog.pattern设定access logs的格式，默认: common
- server.tomcat.accesslog.prefix设定Log 文件的前缀，默认: access_log
- server.tomcat.accesslog.suffix设定Log 文件的后缀，默认: .log
- server.tomcat.background-processor-delay后台线程方法的Delay大小: 30
- server.tomcat.basedir设定Tomcat的base 目录，如果没有指定则使用临时目录.
- server.tomcat.internal-proxies设定信任的正则表达式，默认:“10\.\d{1,3}\.\d{1,3}\.\d{1,3}| 192\.168\.\d{1,3}\.\d{1,3}| 169\.254\.\d{1,3}\.\d{1,3}| 127\.\d{1,3}\.\d{1,3}\.\d{1,3}| 172\.1[6-9]{1}\.\d{1,3}\.\d{1,3}| 172\.2[0-9]{1}\.\d{1,3}\.\d{1,3}|172\.3[0-1]{1}\.\d{1,3}\.\d{1,3}”
- server.tomcat.max-http-header-size设定http header的最小值，默认: 0
- server.tomcat.max-threads设定tomcat的最大工作线程数，默认为: 0
- server.tomcat.port-header设定http header使用的，用来覆盖原来port的value.
- server.tomcat.protocol-header设定Header包含的协议，通常是 X-Forwarded-Proto，如果remoteIpHeader有值，则将设置为RemoteIpValve.
- server.tomcat.protocol-header-https-value设定使用SSL的header的值，默认https.
- server.tomcat.remote-ip-header设定remote IP的header，如果remoteIpHeader有值，则设置为RemoteIpValve
- server.tomcat.uri-encoding设定URI的解码字符集.

## undertow

- server.undertow.access-log-dir设定Undertow access log 的目录，默认: logs
- server.undertow.access-log-enabled是否开启access log，默认: false
- server.undertow.access-log-pattern设定access logs的格式，默认: common
- server.undertow.accesslog.dir设定access log 的目录.
- server.undertow.buffer-size设定buffer的大小.
- server.undertow.buffers-per-region设定每个region的buffer数
- server.undertow.direct-buffers设定堆外内存
- server.undertow.io-threads设定I/O线程数.
- server.undertow.worker-threads设定工作线程数