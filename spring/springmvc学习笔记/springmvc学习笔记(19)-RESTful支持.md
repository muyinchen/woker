# springmvc学习笔记(19)-RESTful支持

标签： springmvc

---

**Contents**

  - [概念](#概念)
  - [REST的例子](#rest的例子)
    - [controller](#controller)
    - [REST方法的前端控制器配置](#rest方法的前端控制器配置)
  - [对静态资源的解析](#对静态资源的解析)



---


本文介绍RESTful的概念，并通过一个小例子展示如何编写RESTful风格的controller和配置前端控制器，最后展示静态资源的解析


## 概念

首先附上两篇博客链接

>* [理解RESTful架构 - 阮一峰的网络日志](http://zqpythonic.qiniucdn.com/data/20110912210739/index.html)
>* [RESTful API 设计指南- 阮一峰的网络日志](http://www.ruanyifeng.com/blog/2014/05/restful_api.html)


RESTful架构，就是目前最流行的一种互联网软件架构。它结构清晰、符合标准、易于理解、扩展方便，所以正得到越来越多网站的采用。

RESTful（即Representational State Transfer的缩写）其实是一个开发理念，是对http的很好的诠释。

1.对url进行规范，写RESTful格式的url

- 非REST的url：`http://...../queryItems.action?id=001&type=T01`
- REST的url风格：`http://..../items/001`

特点：url简洁，将参数通过url传到服务端

2.http的方法规范

不管是删除、添加、更新，等等。使用url是一致的，如果进行删除，需要设置http的方法为delete，其他同理。

后台controller方法：判断http方法，如果是delete执行删除，如果是post执行添加。

3.对http的contentType规范

请求时指定contentType，要json数据，设置成json格式的type。


## REST的例子

查询商品信息，返回json数据。

### controller

定义方法，进行url映射使用REST风格的url，将查询商品信息的id传入controller .

输出json使用`@ResponseBody`将java对象输出json。

```java
//查询商品信息，输出json
//itemsView/{id}里边的{id}表示占位符，通过@PathVariable获取占位符中的参数，
//@PathVariable中名称要和占位符一致，形参名无需和其一致
//如果占位符中的名称和形参名一致，在@PathVariable可以不指定名称
@RequestMapping("/itemsView/{id}")
public @ResponseBody ItemsCustom itemsView(@PathVariable("id") Integer items_id)throws Exception{

    //调用service查询商品信息
    ItemsCustom itemsCustom = itemsService.findItemsById(items_id);

    return itemsCustom;

}
```

`@RequestMapping(value="/ itemsView/{id}")`：`{×××}`占位符，请求的URL可以是`/viewItems/1`或`/viewItems/2`，通过在方法中使用`@PathVariable`获取{×××}中的×××变量。`@PathVariable`用于将请求URL中的模板变量映射到功能处理方法的参数上。

如果`@RequestMapping`中表示为`/itemsView/{id}`，id和形参名称一致，`@PathVariable`不用指定名称。


### REST方法的前端控制器配置

```xml
<!-- springmvc前端控制器，rest配置 -->
<servlet>
    <servlet-name>springmvc_rest</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!-- contextConfigLocation配置springmvc加载的配置文件（配置处理器映射器、适配器等等） 如果不配置contextConfigLocation，默认加载的是/WEB-INF/servlet名称-serlvet.xml（springmvc-servlet.xml） -->
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:spring/springmvc.xml</param-value>
    </init-param>
</servlet>

<servlet-mapping>
    <servlet-name>springmvc_rest</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```


访问结果如图：

![RESTful格式访问](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_RESTful%E6%A0%BC%E5%BC%8F%E8%AE%BF%E9%97%AE.png)


## 对静态资源的解析

配置前端控制器的url-parttern中指定`/`，对静态资源的解析会出现问题，报404错误。


在springmvc.xml中添加静态资源解析方法。

```xml
<!-- 静态资源解析
    包括 ：js、css、img、..
     -->
<mvc:resources location="/js/" mapping="/js/**"/>
```

这时访问`http://localhost:8080/ssm1/js/jquery-1.4.4.min.js`，可以在浏览器中看到js的内容


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)












