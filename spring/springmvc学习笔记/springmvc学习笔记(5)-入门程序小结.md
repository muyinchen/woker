# springmvc学习笔记(5)-入门程序小结

标签： springmvc

---

**Contents**

  - [入门程序配置小结](#入门程序配置小结)
  - [非注解的完整的配置文件](#非注解的完整的配置文件)
  - [注解的完整配置文件](#注解的完整配置文件)



---


通过入门程序理解springmvc前端控制器、处理器映射器、处理器适配器、视图解析器用法。并附上入门程序的非注解的完整的配置文件，注解的完整配置文件。

## 入门程序配置小结

前端控制器配置：

- 第一种：`*.action`，访问以`.action`结尾 由`DispatcherServlet`进行解析
- 第二种：`/`，所以访问的地址都由`DispatcherServlet`进行解析，对于静态文件的解析需要配置不让`DispatcherServlet`进行解析,使用此种方式可以实现RESTful风格的url

处理器映射器：

- 非注解处理器映射器（了解）
- 注解的处理器映射器（掌握）

对标记`@Controller`类中标识有`@RequestMapping`的方法进行映射。在`@RequestMapping`里边定义映射的url。使用注解的映射器不用在xml中配置url和Handler的映射关系。

处理器适配器：

非注解处理器适配器（了解）
注解的处理器适配器（掌握）
注解处理器适配器和注解的处理器映射器是**配对使用**。理解为不能使用非注解映射器进行映射。

```xml
<mvc:annotation-driven></mvc:annotation-driven> 
```

可以代替下边的配置：

```xml
<!--注解映射器 -->  
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>  
    <!--注解适配器 -->  
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter"/>  
```



## 非注解的完整的配置文件

`src/main/resources/springmvc.xml`

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
    http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-4.0.xsd
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd">

    <!-- 配置Handler -->
    <bean name="/queryItems.action" class="com.iot.ssm.controller.ItemsController"/>

    <!-- 处理器映射器
    将bean的name作为url进行查找，需要在配置Handler时指定beanname(就是url)
     -->
    <bean class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"/>

    <!-- 处理器适配器
     所有处理器适配器都实现了HandlerAdapter接口
     -->
    <bean class="org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter"/>

    <!-- 视图解析器
    解析jsp,默认使用jstl,classpath下要有jstl的包
    -->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver"/>

</beans>
```


## 注解的完整配置文件

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
    http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-4.0.xsd
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd">

   <!-- 对于注解的Handler 可以单个配置
    实际开发中加你使用组件扫描
    -->
    <!--  <bean  class="com.iot.ssm.controller.ItemsController3"/> -->
    <!-- 可以扫描controller、service、...
	这里让扫描controller，指定controller的包
	 -->
    <context:component-scan base-package="com.iot.ssm.controller"></context:component-scan>




    <!-- 注解的映射器 -->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>

    <!-- 注解的适配器 -->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter"/>

    <!-- 使用mvc:annotation-driven代替上面两个注解映射器和注解适配的配置
     mvc:annotation-driven默认加载很多的参数绑定方法，
     比如json转换解析器默认加载了，如果使用mvc:annotation-driven则不用配置上面的RequestMappingHandlerMapping和RequestMappingHandlerAdapter
     实际开发时使用mvc:annotation-driven
     -->
    <mvc:annotation-driven></mvc:annotation-driven>

    <!-- 视图解析器
    解析jsp,默认使用jstl,classpath下要有jstl的包
    -->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <!-- 配置jsp路径的前缀 -->
        <property name="prefix" value="/WEB-INF/jsp/"/>
        <!-- 配置jsp路径的后缀 -->
        <property name="suffix" value=".jsp"/>
    </bean>

</beans>
```


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)

