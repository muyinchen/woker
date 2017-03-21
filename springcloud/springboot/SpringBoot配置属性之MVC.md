# SpringBoot配置属性之MVC

## 序

主要是mvc相关的一些配置

## mvc

- spring.mvc.async.request-timeout设定async请求的超时时间，以毫秒为单位，如果没有设置的话，以具体实现的超时时间为准，比如tomcat的servlet3的话是10秒.
- spring.mvc.date-format设定日期的格式，比如dd/MM/yyyy.
- spring.mvc.favicon.enabled是否支持favicon.ico，默认为: true
- spring.mvc.ignore-default-model-on-redirect在重定向时是否忽略默认model的内容，默认为true
- spring.mvc.locale指定使用的Locale.
- spring.mvc.message-codes-resolver-format指定message codes的格式化策略(PREFIX_ERROR_CODE,POSTFIX_ERROR_CODE).
- spring.mvc.view.prefix指定mvc视图的前缀.
- spring.mvc.view.suffix指定mvc视图的后缀.

## messages

- spring.messages.basename指定message的basename，多个以逗号分隔，如果不加包名的话，默认从classpath路径开始，默认: messages
- spring.messages.cache-seconds设定加载的资源文件缓存失效时间，-1的话为永不过期，默认为-1
- spring.messages.encoding设定Message bundles的编码，默认: UTF-8

## mobile

- spring.mobile.devicedelegatingviewresolver.enable-fallback是否支持fallback的解决方案，默认false
- spring.mobile.devicedelegatingviewresolver.enabled是否开始device view resolver，默认为: false
- spring.mobile.devicedelegatingviewresolver.mobile-prefix设定mobile端视图的前缀，默认为:mobile/
- spring.mobile.devicedelegatingviewresolver.mobile-suffix设定mobile视图的后缀
- spring.mobile.devicedelegatingviewresolver.normal-prefix设定普通设备的视图前缀
- spring.mobile.devicedelegatingviewresolver.normal-suffix设定普通设备视图的后缀
- spring.mobile.devicedelegatingviewresolver.tablet-prefix设定平板设备视图前缀，默认:tablet/
- spring.mobile.devicedelegatingviewresolver.tablet-suffix设定平板设备视图后缀.
- spring.mobile.sitepreference.enabled是否启用SitePreferenceHandler，默认为: true

## view

- spring.view.prefix设定mvc视图的前缀.
- spring.view.suffix设定mvc视图的后缀.

## resource

- spring.resources.add-mappings是否开启默认的资源处理，默认为true
- spring.resources.cache-period设定资源的缓存时效，以秒为单位.
- spring.resources.chain.cache是否开启缓存，默认为: true
- spring.resources.chain.enabled是否开启资源 handling chain，默认为false
- spring.resources.chain.html-application-cache是否开启h5应用的cache manifest重写，默认为: false
- spring.resources.chain.strategy.content.enabled是否开启内容版本策略，默认为false
- spring.resources.chain.strategy.content.paths指定要应用的版本的路径，多个以逗号分隔，默认为:[/**]
- spring.resources.chain.strategy.fixed.enabled是否开启固定的版本策略，默认为false
- spring.resources.chain.strategy.fixed.paths指定要应用版本策略的路径，多个以逗号分隔
- spring.resources.chain.strategy.fixed.version指定版本策略使用的版本号
- spring.resources.static-locations指定静态资源路径，默认为classpath:[/META-INF/resources/,/resources/, /static/, /public/]以及context:/

## multipart

- multipart.enabled是否开启文件上传支持，默认为true
- multipart.file-size-threshold设定文件写入磁盘的阈值，单位为MB或KB，默认为0
- multipart.location指定文件上传路径.
- multipart.max-file-size指定文件大小最大值，默认1MB
- multipart.max-request-size指定每次请求的最大值，默认为10MB

## freemarker

- spring.freemarker.allow-request-override指定HttpServletRequest的属性是否可以覆盖controller的model的同名项
- spring.freemarker.allow-session-override指定HttpSession的属性是否可以覆盖controller的model的同名项
- spring.freemarker.cache是否开启template caching.
- spring.freemarker.charset设定Template的编码.
- spring.freemarker.check-template-location是否检查templates路径是否存在.
- spring.freemarker.content-type设定Content-Type.
- spring.freemarker.enabled是否允许mvc使用freemarker.
- spring.freemarker.expose-request-attributes设定所有request的属性在merge到模板的时候，是否要都添加到model中.
- spring.freemarker.expose-session-attributes设定所有HttpSession的属性在merge到模板的时候，是否要都添加到model中.
- spring.freemarker.expose-spring-macro-helpers设定是否以springMacroRequestContext的形式暴露RequestContext给Spring’s macro library使用
- spring.freemarker.prefer-file-system-access是否优先从文件系统加载template，以支持热加载，默认为true
- spring.freemarker.prefix设定freemarker模板的前缀.
- spring.freemarker.request-context-attribute指定RequestContext属性的名.
- spring.freemarker.settings设定FreeMarker keys.
- spring.freemarker.suffix设定模板的后缀.
- spring.freemarker.template-loader-path设定模板的加载路径，多个以逗号分隔，默认: ["classpath:/templates/"]
- spring.freemarker.view-names指定使用模板的视图列表.

## velocity

- spring.velocity.allow-request-override指定HttpServletRequest的属性是否可以覆盖controller的model的同名项
- spring.velocity.allow-session-override指定HttpSession的属性是否可以覆盖controller的model的同名项
- spring.velocity.cache是否开启模板缓存
- spring.velocity.charset设定模板编码
- spring.velocity.check-template-location是否检查模板路径是否存在.
- spring.velocity.content-type设定ContentType的值
- spring.velocity.date-tool-attribute设定暴露给velocity上下文使用的DateTool的名
- spring.velocity.enabled设定是否允许mvc使用velocity
- spring.velocity.expose-request-attributes是否在merge模板的时候，将request属性都添加到model中
- spring.velocity.expose-session-attributes是否在merge模板的时候，将HttpSession属性都添加到model中
- spring.velocity.expose-spring-macro-helpers设定是否以springMacroRequestContext的名来暴露RequestContext给Spring’s macro类库使用
- spring.velocity.number-tool-attribute设定暴露给velocity上下文的NumberTool的名
- spring.velocity.prefer-file-system-access是否优先从文件系统加载模板以支持热加载，默认为true
- spring.velocity.prefix设定velocity模板的前缀.
- spring.velocity.properties设置velocity的额外属性.
- spring.velocity.request-context-attribute设定RequestContext attribute的名.
- spring.velocity.resource-loader-path设定模板路径，默认为: classpath:/templates/
- spring.velocity.suffix设定velocity模板的后缀.
- spring.velocity.toolbox-config-location设定Velocity Toolbox配置文件的路径，比如 /WEB-INF/toolbox.xml.
- spring.velocity.view-names设定需要解析的视图名称.

## thymeleaf

- spring.thymeleaf.cache是否开启模板缓存，默认true
- spring.thymeleaf.check-template-location是否检查模板路径是否存在，默认true
- spring.thymeleaf.content-type指定Content-Type，默认为: text/html
- spring.thymeleaf.enabled是否允许MVC使用Thymeleaf，默认为: true
- spring.thymeleaf.encoding指定模板的编码，默认为: UTF-8
- spring.thymeleaf.excluded-view-names指定不使用模板的视图名称，多个以逗号分隔.
- spring.thymeleaf.mode指定模板的模式，具体查看StandardTemplateModeHandlers，默认为: HTML5
- spring.thymeleaf.prefix指定模板的前缀，默认为:classpath:/templates/
- spring.thymeleaf.suffix指定模板的后缀，默认为:.html
- spring.thymeleaf.template-resolver-order指定模板的解析顺序，默认为第一个.
- spring.thymeleaf.view-names指定使用模板的视图名，多个以逗号分隔.

## mustcache

- spring.mustache.cache是否Enable template caching.
- spring.mustache.charset指定Template的编码.
- spring.mustache.check-template-location是否检查默认的路径是否存在.
- spring.mustache.content-type指定Content-Type.
- spring.mustache.enabled是否开启mustcache的模板支持.
- spring.mustache.prefix指定模板的前缀，默认: classpath:/templates/
- spring.mustache.suffix指定模板的后缀，默认: .html
- spring.mustache.view-names指定要使用模板的视图名.

## groovy模板

- spring.groovy.template.allow-request-override指定HttpServletRequest的属性是否可以覆盖controller的model的同名项
- spring.groovy.template.allow-session-override指定HttpSession的属性是否可以覆盖controller的model的同名项
- spring.groovy.template.cache是否开启模板缓存.
- spring.groovy.template.charset指定Template编码.
- spring.groovy.template.check-template-location是否检查模板的路径是否存在.
- spring.groovy.template.configuration.auto-escape是否在渲染模板时自动排查model的变量，默认为: false
- spring.groovy.template.configuration.auto-indent是否在渲染模板时自动缩进，默认为false
- spring.groovy.template.configuration.auto-indent-string如果自动缩进启用的话，是使用SPACES还是TAB，默认为: SPACES
- spring.groovy.template.configuration.auto-new-line渲染模板时是否要输出换行，默认为false
- spring.groovy.template.configuration.base-template-class指定template base class.
- spring.groovy.template.configuration.cache-templates是否要缓存模板，默认为true
- spring.groovy.template.configuration.declaration-encoding在写入declaration header时使用的编码
- spring.groovy.template.configuration.expand-empty-elements
  是使用`<br/>`这种形式，还是`<br></br>`这种展开模式，默认为: false)
- spring.groovy.template.configuration.locale指定template locale.
- spring.groovy.template.configuration.new-line-string当启用自动换行时，换行的输出，默认为系统的line.separator属性的值
- spring.groovy.template.configuration.resource-loader-path指定groovy的模板路径，默认为classpath:/templates/
- spring.groovy.template.configuration.use-double-quotes指定属性要使用双引号还是单引号，默认为false
- spring.groovy.template.content-type指定Content-Type.
- spring.groovy.template.enabled是否开启groovy模板的支持.
- spring.groovy.template.expose-request-attributes设定所有request的属性在merge到模板的时候，是否要都添加到model中.
- spring.groovy.template.expose-session-attributes设定所有request的属性在merge到模板的时候，是否要都添加到model中.
- spring.groovy.template.expose-spring-macro-helpers设定是否以springMacroRequestContext的形式暴露RequestContext给Spring’s macro library使用
- spring.groovy.template.prefix指定模板的前缀.
- spring.groovy.template.request-context-attribute指定RequestContext属性的名.
- spring.groovy.template.resource-loader-path指定模板的路径，默认为: classpath:/templates/
- spring.groovy.template.suffix指定模板的后缀
- spring.groovy.template.view-names指定要使用模板的视图名称.

## http

- spring.hateoas.apply-to-primary-object-mapper设定是否对object mapper也支持HATEOAS，默认为: true
- spring.http.converters.preferred-json-mapper是否优先使用JSON mapper来转换.
- spring.http.encoding.charset指定http请求和相应的Charset，默认: UTF-8
- spring.http.encoding.enabled是否开启http的编码支持，默认为true
- spring.http.encoding.force是否强制对http请求和响应进行编码，默认为true

## json

- spring.jackson.date-format指定日期格式，比如yyyy-MM-dd HH:mm:ss，或者具体的格式化类的全限定名
- spring.jackson.deserialization是否开启Jackson的反序列化
- spring.jackson.generator是否开启json的generators.
- spring.jackson.joda-date-time-format指定Joda date/time的格式，比如yyyy-MM-dd HH:mm:ss). 如果没有配置的话，dateformat会作为backup
- spring.jackson.locale指定json使用的Locale.
- spring.jackson.mapper是否开启Jackson通用的特性.
- spring.jackson.parser是否开启jackson的parser特性.
- spring.jackson.property-naming-strategy指定PropertyNamingStrategy (CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES)或者指定PropertyNamingStrategy子类的全限定类名.
- spring.jackson.serialization是否开启jackson的序列化.
- spring.jackson.serialization-inclusion指定序列化时属性的inclusion方式，具体查看JsonInclude.Include枚举.
- spring.jackson.time-zone指定日期格式化时区，比如America/Los_Angeles或者GMT+10.

## jersey

- spring.jersey.filter.order指定Jersey filter的order，默认为: 0
- spring.jersey.init指定传递给Jersey的初始化参数.
- spring.jersey.type指定Jersey的集成类型，可以是servlet或者filter.