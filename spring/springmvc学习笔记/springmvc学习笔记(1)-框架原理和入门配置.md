# springmvc学习笔记(1)-框架原理和入门配置

标签： springmvc

---

**Contents**

  - [springmvc框架原理](#springmvc框架原理)
  - [springmvc入门程序](#springmvc入门程序)
    - [环境搭建](#环境搭建)
    - [配置文件](#配置文件)
    - [部署调试](#部署调试)
  - [参考链接](#参考链接)



---

本文主要介绍springmvc的框架原理，并通过一个入门程序展示环境搭建，配置以及部署调试。


springmvc是spring框架的一个模块，springmvc和spring无需通过中间整合层进行整合。


## springmvc框架原理

给个官网示意图

![The request processing workflow in Spring Web MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/images/mvc.png)

组件及其作用

- 前端控制器(DispatcherServlet)：接收请求，响应结果，相当于转发器，中央处理器。减少了其他组件之间的耦合度
- 处理器映射器(HandlerMapping)：根据请求的url查找Handler
- **Handler处理器**：按照HandlerAdapter的要求编写
- 处理器适配器(HandlerAdapter)：按照特定规则(HandlerAdapter要求的规则)执行Handler。
- 视图解析器(ViewResolver)：进行视图解析，根据逻辑视图解析成真正的视图(View)
- **视图(View)**：View是一个接口实现类试吃不同的View类型（jsp,pdf等等）

*注：其中加粗的为需要程序员开发的，没加粗的为不需要程序员开发的*

文末参考链接中《跟开涛学SpringMVC》里面有张图挺好的，感觉很详细.

![springmvc_核心架构图](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_%E6%A0%B8%E5%BF%83%E6%9E%B6%E6%9E%84%E5%9B%BE.jpg)


步骤：

- 1.发起请求到前端控制器(`DispatcherServlet`)
- 2.前端控制器请求处理器映射器(`HandlerMapping`)查找`Handler`(可根据xml配置、注解进行查找)
- 3.处理器映射器(`HandlerMapping`)向前端控制器返回`Handler`
- 4.前端控制器调用处理器适配器(`HandlerAdapter`)执行`Handler`
- 5.处理器适配器(HandlerAdapter)去执行Handler
- 6.Handler执行完，给适配器返回ModelAndView(Springmvc框架的一个底层对象)
- 7.处理器适配器(`HandlerAdapter`)向前端控制器返回`ModelAndView`
- 8.前端控制器(`DispatcherServlet`)请求视图解析器(`ViewResolver`)进行视图解析，根据逻辑视图名解析成真正的视图(jsp)
- 9.视图解析器(ViewResolver)向前端控制器(`DispatcherServlet`)返回View
- 10.前端控制器进行视图渲染，即将模型数据(在`ModelAndView`对象中)填充到request域
- 11.前端控制器向用户响应结果

## springmvc入门程序

一个展示商品列表的小页面

### 环境搭建

intellij IDEA 15.0.2

- A方法(有待商榷)

`new->project->maven->勾选create from archetype->选中webapp` 

在`src/main`下新建`java`文件夹，标记为`Sources Root`

这样建出来的工程感觉有问题，点开一些xml文件一片红，心里不是很踏实，所以放弃这个方法了


- B方法

`new->project->maven`，建一个裸的maven工程，手动建webapp的目录

在`src/main`下新建文件夹`webapp`

pom.xml文件

添加依赖

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>4.2.4.RELEASE</version>
</dependency>
```

加上下面的标签会生成Artifacts

```xml
<packaging>war</packaging>
```

build标签的finalName要和Artifacts的output directory一致

```xml
<build>
    <finalName>springmvc-2nd-1.0-SNAPSHOT</finalName>
</build>
```


### 配置文件

- 配置前端控制器

web.xml

```xml
<servlet>
    <servlet-name>springmvc</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!-- contextConfigLocation配置springmvc加载的配置文件(配置处理器映射器、适配器等等)
      若不配置，默认加载WEB-INF/servlet名称-servlet(springmvc-servlet.xml)
    -->
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:springmvc.xml</param-value>
    </init-param>
</servlet>
```

```xml
<servlet-mapping>
    <servlet-name>springmvc</servlet-name>
    <!--
    第一种:*.action,访问以.action结尾，由DispatcherServlet进行解析
    第二种:/,所有访问的地址由DispatcherServlet进行解析，对静态文件的解析需要配置不让DispatcherServlet进行解析，
            使用此种方式和实现RESTful风格的url
    第三种:/*,这样配置不对，使用这种配置，最终要转发到一个jsp页面时，仍然会由DispatcherServlet解析jsp地址，
            不能根据jsp页面找到handler，会报错
    -->
    <url-pattern>*.action</url-pattern>
</servlet-mapping>
```

- 配置Handler

将编写Handler在spring容器加载

```xml
<!-- 配置Handler -->
<bean name="/queryItems.action" class="com.iot.ssm.controller.ItemsController"/>

```

- 配置处理器映射器

在classpath下的springmvc.xml中配置处理器映射器

```xml
<!-- 处理器映射器
    将bean的name作为url进行查找，需要在配置Handler时指定beanname(就是url)
-->
<bean class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"/>
```


- 配置处理器适配器

所有处理器适配器都实现了`HandlerAdapter`接口

`<bean class="org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter">`


源码

```java
public boolean supports(Object handler) {
        return handler instanceof Controller;
}
```

此适配器能执行实现`Controller`接口的Handler



- 配置视图解析器

需要配置解析jsp的视图解析器

```xml
 <!-- 视图解析器
    解析jsp,默认使用jstl,classpath下要有jstl的包
    -->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver"/>
```


在springmvc.xml中视图解析器配置前缀和后缀：

```xml
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <!-- 配置jsp路径的前缀 -->
        <property name="prefix" value="/WEB-INF/jsp/"/>
        <!-- 配置jsp路径的后缀 -->
        <property name="suffix" value=".jsp"/>
</bean>
```

程序中不用指定前缀和后缀：

```java
//指定视图
//下边的路径，如果在视图解析器中配置jsp的路径前缀和后缀，修改为items/itemsList
//modelAndView.setViewName("/WEB-INF/jsp/items/itemsList.jsp");

//下边的路径配置就可以不在程序中指定jsp路径的前缀和后缀
modelAndView.setViewName("items/itemsList");
```



### 部署调试

`HTTP Status 404 -`
处理器映射器根据url找不到Handler,说明url错误

`HTTP Status 404 -/springmvc/WEB-INF/jsp/items/itemsLists.jsp`
处理器映射器根据url找到了Handler，转发的jsp页面找不到





## 参考链接

>* [第二章 Spring MVC入门 —— 跟开涛学SpringMVC](http://sishuok.com/forum/blogPost/list/5160.html)
>* [Spring MVC Framework Tutorial - TutorialsPoint](http://www.tutorialspoint.com/spring/spring_web_mvc_framework.htm)
>* [Web MVC framework](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html)


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
