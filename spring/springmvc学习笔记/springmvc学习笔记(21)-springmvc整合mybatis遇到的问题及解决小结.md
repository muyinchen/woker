# springmvc学习笔记(21)-springmvc整合mybatis遇到的问题及解决小结

标签： springmvc mybatis

---

**Contents**

  - [遇到的问题](#遇到的问题)
    - [在web.xml中`<listener-class>`标签报红](#在webxml中listener-class标签报红)
    - [BeanCreationException](#beancreationexception)
    - [mybatis绑定错误](#mybatis绑定错误)
    - [请求参数乱码问题](#请求参数乱码问题)
    - [请求参数类型转换问题](#请求参数类型转换问题)
    - [maven平台编码问题](#maven平台编码问题)
    - [json格式数据问题](#json格式数据问题)
  - [还没弄懂但不影响运行的问题](#还没弄懂但不影响运行的问题)
    - [加载spring容器报红](#加载spring容器报红)
    - [参数绑定配置问题](#参数绑定配置问题)
    - [maven依赖分析问题](#maven依赖分析问题)



---

本文主要记录springmvc整合mybatis整合过程中遇到的各种问题和解决方法


## 遇到的问题

### 在web.xml中`<listener-class>`标签报红


参考：

> [web.xml listener-class is not allowed here](http://q.cnblogs.com/q/74982/)

解决：改用2.5的版本

答案节选：

> Servlet3.0是J2EE6.0规范的一部分，跟随J2EE6.0一起发布，并且Tomcat7.0已经完全支持Servlet3.0 ；
平时，我们一般使用tomcat6.0，是不能够使用servelt3.0的，tomcat6.0还不能支持那些规范；
至于说，为毛线不能使用lintener-class，是因为在web-app_3_0.xsd结构定义文件中，根本就不提倡这些配置，因为Servlet3.0已经支持注解形式；

当时解决了报红的问题。但后来我其他部分调试好了后，改回3.0也没报错。


### BeanCreationException

```
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSource' defined in file [D:\intellij\workspace\learnssm-firstssm\target\learnssm-firstssm-1.0-SNAPSHOT\WEB-INF\classes\spring\applicationContext-dao.xml]: BeanPostProcessor before instantiation of bean failed; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor#0': Initialization of bean failed; nested exception is java.lang.NoClassDefFoundError: org/aspectj/weaver/reflect/ReflectionWorld$ReflectionWorldException
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:478)
	at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:306)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:302)
    ....省略
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor#0': Initialization of bean failed; nested exception is java.lang.NoClassDefFoundError: org/aspectj/weaver/reflect/ReflectionWorld$ReflectionWorldException
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:553)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:482)
	at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:306)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230)
	  ....省略
```

是少依赖的问题，输入`mvn dependency:tree`打依赖树：

```
D:\intellij\workspace\learnssm-firstssm>mvn dependency:tree
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building learnssm-firstssm 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ learnssm-firstssm ---
[INFO] com.iot.learnssm:learnssm-firstssm:war:1.0-SNAPSHOT
[INFO] +- org.springframework:spring-core:jar:4.2.4.RELEASE:compile
[INFO] |  \- commons-logging:commons-logging:jar:1.2:compile
[INFO] +- org.springframework:spring-webmvc:jar:4.2.4.RELEASE:compile
[INFO] |  +- org.springframework:spring-beans:jar:4.2.4.RELEASE:compile
[INFO] |  +- org.springframework:spring-context:jar:4.2.4.RELEASE:compile
[INFO] |  |  \- org.springframework:spring-aop:jar:4.2.4.RELEASE:compile
[INFO] |  |     \- aopalliance:aopalliance:jar:1.0:compile
[INFO] |  +- org.springframework:spring-expression:jar:4.2.4.RELEASE:compile
[INFO] |  \- org.springframework:spring-web:jar:4.2.4.RELEASE:compile
[INFO] +- org.springframework:spring-jdbc:jar:4.2.4.RELEASE:compile
[INFO] |  \- org.springframework:spring-tx:jar:4.2.4.RELEASE:compile
[INFO] +- mysql:mysql-connector-java:jar:5.1.38:compile
[INFO] +- org.mybatis:mybatis:jar:3.3.1:compile
[INFO] +- org.mybatis:mybatis-spring:jar:1.2.4:compile
[INFO] +- log4j:log4j:jar:1.2.17:compile
[INFO] +- org.slf4j:slf4j-api:jar:1.7.18:compile
[INFO] +- commons-dbcp:commons-dbcp:jar:1.4:compile
[INFO] |  \- commons-pool:commons-pool:jar:1.5.4:compile
[INFO] +- javax.servlet:jstl:jar:1.2:compile
[INFO] \- taglibs:standard:jar:1.1.2:compile
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 7.956 s
[INFO] Finished at: 2016-03-03T20:06:00+08:00
[INFO] Final Memory: 11M/126M
[INFO] ------------------------------------------------------------------------
```

少了spring-aspects,spring-core等依赖，加上


```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>${spring.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-orm</artifactId>
    <version>${spring.version}</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
    <version>${spring.version}</version>
</dependency>

 <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <version>${spring.version}</version>
</dependency>
```

spring版本统一设置

```xml
<properties>
    <!-- jar 版本设置 -->
    <spring.version>4.2.4.RELEASE</spring.version>
</properties>
```


### mybatis绑定错误

错误：`org.apache.ibatis.binding.BindingException: Invalid bound statement`

使用了下面的方法检查，都没有解决。排除了包名不同等低级错误。

>* [mybatis绑定错误- softwarehe的专栏- 博客频道- CSDN.NET](http://blog.csdn.net/softwarehe/article/details/8889206)
>* [Mybatis绑定错误的原因](http://ljhzzyx.blog.163.com/blog/static/38380312201412453629988/)

又找到了oschina一个人的帖子，有个回答感觉靠谱

>* [java spring4+mybatis整合报错BindingException Invalid bound statement](http://www.oschina.net/question/113302_228910)

> 是的，是没有在pom.xml配置build包含 xml，导致target目录下没有userMapper.xml
      -by 唐小明生

我一看自己的target目录,发现还真是少mapper.xml文件

![target少mapper的xml文件](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_bug_target%E5%B0%91xml%E6%96%87%E4%BB%B6.png)
  
  
我想到了两种解决方案：

- 方案一：自定义一个插件，绑定某个生命周期，比如compile，然后插件目标的功能是将源码包下的xml文件copy到相应的输出目录。(现有插件是否有已有这个功能，通过简单的配置就能完成？我还不清楚)
- 方案二：在maven工程的`src/main/resource`目录下建和mapper接口类相应的包，将每个mapper.xml存在这里

这里我插件玩的不熟，所以没办法，只能手动在resources目录下建包，把每个mapper.xml手动粘贴进去

解决后如图：

![mapper.xml存在resources下面](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_solve_mapper%E7%9A%84xml%E6%96%87%E4%BB%B6%E5%AD%98%E5%9C%A8resources.png)

### 请求参数乱码问题

![springmcv_post请求参数乱码](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_post%E8%AF%B7%E6%B1%82%E5%8F%82%E6%95%B0%E4%B9%B1%E7%A0%81.png)


在web.xml添加post乱码filter

```xml
<!-- post乱码过虑器 -->
<filter>
    <filter-name>CharacterEncodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>utf-8</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>CharacterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

以上可以解决post请求乱码问题。解决后调试如图

![springmcv_post请求参数乱码解决](http://7xph6d.com1.z0.glb.clouddn.com/springmcv_post%E8%AF%B7%E6%B1%82%E5%8F%82%E6%95%B0%E4%B9%B1%E7%A0%81%E8%A7%A3%E5%86%B3.png)

对于get请求中文参数出现乱码解决方法有两个：

修改tomcat配置文件添加编码与工程编码一致，如下：

`<Connector URIEncoding="utf-8" connectionTimeout="20000" port="8080" protocol="HTTP/1.1" redirectPort="8443"/>`

另外一种方法对参数进行重新编码：

```java
String userName = new 
String(request.getParamter("userName").getBytes("ISO8859-1"),"utf-8")
```

ISO8859-1是tomcat默认编码，需要将tomcat编码后的内容按utf-8编码




### 请求参数类型转换问题

编写对应的转换类才行,具体参考前面参数绑定的博文[《 springmvc学习笔记(11)-springmvc注解开发之简单参数绑定》](http://blog.csdn.net/h3243212/article/details/50854748#自定义参数绑定实现日期类型绑定)


### maven平台编码问题

`[WARNING] Using platform encoding (GBK actually) to copy filtered resources, i.e. build is platform dependent!`

参考: 

>* [CSDN博客](http://blog.csdn.net/jinguangliu/article/details/43373203) 
>* [Maven官网在FAQ](http://maven.apache.org/general.html#encoding-warning)

解决:

在pom.xml文件的设置编码即可

```
<properties>  
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>  
</properties>  
```


### json格式数据问题

- 1.请求是json格式

debug窗里报下面的错误：

`org.springframework.web.HttpMediaTypeNotSupportedException: Content type 'application/json;charset=UTF-8' not supported`

浏览器报下面的错误：

`HTTP Status 415 -`and`description The server refused this request because the request entity is in a format not supported by the requested resource for the requested method.`


2.请求是key/value格式

debug窗里报下面的错误：

`java.lang.IllegalArgumentException: No converter found for return value of type: class com.iot.learnssm.firstssm.po.ItemsCustom`

参考stackoverflow的这个链接:

> [How to return JSON data from spring Controller using @ResponseBody](http://stackoverflow.com/questions/32905917/how-to-return-json-data-from-spring-controller-using-responsebody)

多加一个依赖`jackson-databind`（之前只加了`jackson-mapper-asl`的依赖， 间接依赖`jackson-core-asl`，但还不够。

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.7.2</version>
</dependency>
```


## 还没弄懂但不影响运行的问题

### 加载spring容器报红

- web.xml节选

```xml
 <!-- 加载spring容器 -->
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>WEB-INF/classes/spring/applicationContext-*.xml</param-value>
    <!--  <param-value>classpath:spring/applicationContext-*.xml</param-value>-->
 </context-param>
```

![加载spring容器报红](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_bug_%E5%AE%B9%E5%99%A8%E8%B7%AF%E5%BE%84%E6%8A%A5%E7%BA%A2.png)


`/classes/spring/applicationContext-*.xml`这部分字会报红，但运行起来没问题。我使用下面的那句` <param-value>classpath:spring/applicationContext-*.xml</param-value>`不报红.原因不清楚。

这里两种方式都能跑通，但是引用的路径不同:一个是引用的的输出的target目录的classes下的，一个是引用输出的`target/learnssm-firstssm-1.0-SNAPSHOT`目录(相当于部署的WEBROOT或者叫做webapp)，所以我觉得还是用WEB-INF下面那个更好

![容器加载文件的选用](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_solve_%E5%AE%B9%E5%99%A8%E5%8A%A0%E8%BD%BD%E6%96%87%E4%BB%B6%E7%9A%84%E9%80%89%E7%94%A8.png)


### 参数绑定配置问题

在自定义参数绑定时，spring.xml的配置如下：

```xml
<!-- 自定义参数绑定 -->
    <bean id="conversionService" class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <!-- 转换器 -->
        <property name="converters">
            <list>
                <!-- 日期类型转换 -->
                <bean class="com.iot.learnssm.firstssm.controller.converter.CustomDateConverter"/>
           </list>
        </property>
    </bean>
```

其中`<list>`标签会报红，但不影响运行。去掉`<list>`标签，也可以运行成功。原因我还不知道，以后阅读源码会研究下这个问题。

![转换器list报红](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_bug_%E8%BD%AC%E6%8D%A2%E5%99%A8list%E6%8A%A5%E7%BA%A2.png)

### maven依赖分析问题

输入`mvn  dependency:analyze`进行依赖分析

```
[INFO]
[INFO] --- maven-dependency-plugin:2.8:analyze (default-cli) @ learnssm-firstssm ---
[WARNING] Used undeclared dependencies found:
[WARNING]    org.springframework:spring-context:jar:4.2.4.RELEASE:compile
[WARNING]    org.springframework:spring-web:jar:4.2.4.RELEASE:compile
[WARNING]    org.springframework:spring-beans:jar:4.2.4.RELEASE:compile
[WARNING] Unused declared dependencies found:
[WARNING]    org.springframework:spring-orm:jar:4.2.4.RELEASE:compile
[WARNING]    org.springframework:spring-aspects:jar:4.2.4.RELEASE:compile
[WARNING]    org.springframework:spring-test:jar:4.2.4.RELEASE:compile
[WARNING]    org.springframework:spring-jdbc:jar:4.2.4.RELEASE:compile
[WARNING]    mysql:mysql-connector-java:jar:5.1.38:compile
[WARNING]    org.mybatis:mybatis-spring:jar:1.2.4:compile
[WARNING]    log4j:log4j:jar:1.2.17:compile
[WARNING]    org.slf4j:slf4j-api:jar:1.7.18:compile
[WARNING]    commons-dbcp:commons-dbcp:jar:1.4:compile
[WARNING]    javax.servlet:jstl:jar:1.2:compile
[WARNING]    taglibs:standard:jar:1.1.2:compile
[WARNING]    org.hibernate:hibernate-validator:jar:5.2.4.Final:compile
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.294 s
[INFO] Finished at: 2016-03-06T16:35:23+08:00
[INFO] Final Memory: 16M/164M
[INFO] ------------------------------------------------------------------------

```

可以看到里面有:

- `Used undeclared dependencies found:`
- `Unused declared dependencies found:`

当时为了解决缺包的问题，看到相关的spring-xxx包就加进去了，具体相互之间的依赖关系也没搞清楚，等我以后阅读spring源码再慢慢改好了。反正多引比少引好点，起码不会报错，顶多工程冗余点。


至于说使用了未声明的包就不知道为啥了，比如

```
[WARNING]    org.springframework:spring-context:jar:4.2.4.RELEASE:compile
[WARNING]    org.springframework:spring-web:jar:4.2.4.RELEASE:compile
[WARNING]    org.springframework:spring-beans:jar:4.2.4.RELEASE:compile
```

都被`org.springframework:spring-webmvc:jar:4.2.4.RELEASE:compile`依赖，这点可以从依赖树看到

```
[INFO] --- maven-dependency-plugin:2.8:tree (default-cli) @ learnssm-firstssm ---
[INFO] com.iot.learnssm:learnssm-firstssm:war:1.0-SNAPSHOT
[INFO] +- org.springframework:spring-webmvc:jar:4.2.4.RELEASE:compile
[INFO] |  +- org.springframework:spring-beans:jar:4.2.4.RELEASE:compile
[INFO] |  +- org.springframework:spring-context:jar:4.2.4.RELEASE:compile
[INFO] |  |  \- org.springframework:spring-aop:jar:4.2.4.RELEASE:compile
[INFO] |  |     \- aopalliance:aopalliance:jar:1.0:compile
[INFO] |  +- org.springframework:spring-expression:jar:4.2.4.RELEASE:compile
[INFO] |  \- org.springframework:spring-web:jar:4.2.4.RELEASE:compile
```

总之，上面未解决的问题，我会留意，如果有大神指导原因，请不吝赐教。


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)

