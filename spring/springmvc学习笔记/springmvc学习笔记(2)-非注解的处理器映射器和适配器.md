# springmvc学习笔记(2)-非注解的处理器映射器和适配器

标签： springmvc

---

**Contents**

  - [非注解的处理器映射器](#非注解的处理器映射器)
  - [非注解的处理器适配器](#非注解的处理器适配器)



---

本文主要介绍非注解的处理器映射器和适配器配置


## 非注解的处理器映射器

```xml
 <!-- 配置Handler -->
<bean id="itemsController" name="/queryItems.action" class="com.iot.ssm.controller.ItemsController"/>

<!-- 处理器映射器
将bean的name作为url进行查找，需要在配置Handler时指定beanname(就是url)
 -->
<bean class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"/>

<!-- 简单url映射-->
<bean class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
    <property name="mappings">
        <props>
            <!-- 对 itemsController进行url映射-->
            <prop key="/queryItems1.action">itemsController</prop>
            <prop key="/queryItems2.action">itemsController</prop>
        </props>
    </property>
</bean>
```

多个映射器可并存，前端控制器判断url能让哪些映射器处理就让正确的映射器处理


## 非注解的处理器适配器

```
 <!-- 处理器适配器
     所有处理器适配器都实现了HandlerAdapter接口
     -->
    <bean class="org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter"/>
```

要求编写的Handler实现`Controller`接口

`<bean class="org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter"/>`

要求编写的Handler实现`HttpRequestHandler`接口

```java
package com.iot.ssm.controller;

import com.iot.ssm.po.Items;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brian on 2016/2/19.
 */
public class ItemsController2 implements HttpRequestHandler{
    public void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        //调用service查找数据库，查询商品列表，这里使用静态数据模拟
        List<Items> itemsList = new ArrayList<Items>();

        //向list中填充静态数据
        Items items_1 = new Items();
        items_1.setName("联想笔记本");
        items_1.setPrice(6000f);
        items_1.setDetail("ThinkPad T430 联想笔记本电脑！");

        Items items_2 = new Items();
        items_2.setName("苹果手机");
        items_2.setPrice(5000f);
        items_2.setDetail("iphone6苹果手机！");

        itemsList.add(items_1);
        itemsList.add(items_2);

        //设置模型数据
        httpServletRequest.setAttribute("itemsList",itemsList);

        //设置转发的视图
        httpServletRequest.getRequestDispatcher("/WEB-INF/jsp/items/itemsList.jsp").forward(httpServletRequest,httpServletResponse);

    }
}
```


`HttpRequestHandler`适配器的`handleRequest`方法返回为`void`,没有返回`ModelAndView`，可通过response修改响应内容,比如返回json数据：

```java
response.setCharacterEncoding("utf-8");
response.setContentType("application/json;charset=utf-8");
response.getWriter().write("json串");
```


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
