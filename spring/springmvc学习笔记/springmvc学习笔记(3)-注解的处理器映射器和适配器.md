# springmvc学习笔记(3)-注解的处理器映射器和适配器

标签： springmvc

---

**Contents**

  - [默认加载](#默认加载)
  - [注解的处理器映射器和适配器](#注解的处理器映射器和适配器)
  - [开发注解Handler](#开发注解handler)
  - [在spring容器中加载Handler](#在spring容器中加载handler)
  - [参考资料](#参考资料)



---


本文主要介绍注解的处理器映射器和适配器相关配置


## 默认加载

前端控制器从`\org\springframework\web\servlet\DispatcherServlet.properties`件中加载处理器映射器、适配器、视图解析器等组件，如果不在springmvc.xml中配置，则使用默认加载的

注解的处理器映射器和适配器

- 在spring3.1之前使用`org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping`注解映射器。
- 在spring3.1之后使用`org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping`注解映射器。
- 在spring3.1之前使用`org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter`注解适配器。
- 在spring3.1之后使用`org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter`注解适配器


## 注解的处理器映射器和适配器

```xml
<!-- 注解的映射器 -->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping"/>

<!-- 注解的适配器 -->
    <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter"/>
    
```

或者

```xml
    <!-- 使用mvc:annotation-driven代替上面两个注解映射器和注解适配的配置
     mvc:annotation-driven默认加载很多的参数绑定方法，
     比如json转换解析器默认加载了，如果使用mvc:annotation-driven则不用配置上面的RequestMappingHandlerMapping和RequestMappingHandlerAdapter
     实际开发时使用mvc:annotation-driven
     -->
    <mvc:annotation-driven></mvc:annotation-driven>

```

## 开发注解Handler

使用注解的映射器和注解的适配器。(使用注解的映射器和注解的适配器必须配对使用)

```java
//使用@Controller来标识它是一个控制器
@Controller
public class ItemsController3 {

    //商品查询列表
    @RequestMapping("/queryItems")
    //实现 对queryItems方法和url进行映射，一个方法对应一个url
    //一般建议将url和方法写成一样
    public ModelAndView queryItems() throws Exception{
        //调用service查找数据库，查询商品列表，这里使用静态数据模拟
        List<Items> itemsList = new ArrayList<Items>();

        //向list中填充静态数据
        Items items_1 = new Items();
        items_1.setName("联想笔记本");
        items_1.setPrice(6000f);
        items_1.setDetail("ThinkPad T430 c3 联想笔记本电脑！");

        Items items_2 = new Items();
        items_2.setName("苹果手机");
        items_2.setPrice(5000f);
        items_2.setDetail("iphone6苹果手机！");

        itemsList.add(items_1);
        itemsList.add(items_2);

        //返回ModelAndView
        ModelAndView modelAndView = new ModelAndView();
        //相当于request的setAttribute方法,在jsp页面中通过itemsList取数据
        modelAndView.addObject("itemsList",itemsList);

        //指定视图
        modelAndView.setViewName("/WEB-INF/jsp/items/itemsList.jsp");
        return modelAndView;
    }
}
```

## 在spring容器中加载Handler

```xml
<!-- 对于注解的Handler 可以单个配置
    实际开发中加你使用组件扫描
    -->
    <!--  <bean  class="com.iot.ssm.controller.ItemsController3"/> -->
    <!-- 可以扫描controller、service、...
	这里让扫描controller，指定controller的包
	 -->
    <context:component-scan base-package="com.iot.ssm.controller"></context:component-scan>
```



## 参考资料

>* [SpringMVC框架】注解的处理器映射器和适配器配置](http://blog.csdn.net/acmman/article/details/46980427)



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
