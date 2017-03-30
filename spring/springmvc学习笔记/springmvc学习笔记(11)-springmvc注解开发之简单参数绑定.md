# springmvc学习笔记(11)-springmvc注解开发之简单参数绑定

标签： springmvc

---

**Contents**

  - [spring参数绑定过程](#spring参数绑定过程)
  - [默认支持的类型](#默认支持的类型)
  - [简单类型](#简单类型)
  - [pojo绑定](#pojo绑定)
  - [自定义参数绑定实现日期类型绑定](#自定义参数绑定实现日期类型绑定)
  - [springmvc和struts2的区别](#springmvc和struts2的区别)



---


本文主要介绍注解开发的简单参数绑定，包括简单类型、简单pojo以及自定义绑定实现类型转换

## spring参数绑定过程

从客户端请求key/value数据，经过参数绑定，将key/value数据绑定到controller方法的形参上。

springmvc中，接收页面提交的数据是通过方法形参来接收。而不是在controller类定义成员变更接收！！！！

![参数绑定过程](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_%E5%8F%82%E6%95%B0%E7%BB%91%E5%AE%9A%E8%BF%87%E7%A8%8B.png)


## 默认支持的类型

直接在controller方法形参上定义下边类型的对象，就可以使用这些对象。在参数绑定过程中，如果遇到下边类型直接进行绑定。

- `HttpServletRequest`：通过request对象获取请求信息
- `HttpServletResponse`：通过response处理响应信息
- `HttpSession`：通过session对象得到session中存放的对象
- `Model/ModelMap`：model是一个接口，modelMap是一个接口实现。作用：将model数据填充到request域。


## 简单类型

通过`@RequestParam`对简单类型的参数进行绑定。如果不使用`@RequestParam`，要求request传入参数名称和controller方法的形参名称一致，方可绑定成功。

如果使用`@RequestParam`，不用限制request传入参数名称和controller方法的形参名称一致。

通过required属性指定参数是否必须要传入，如果设置为true，没有传入参数，报下边错误：

![指定传入参数未传入报错](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_%E6%8C%87%E5%AE%9A%E4%BC%A0%E5%85%A5%E5%8F%82%E6%95%B0%E6%9C%AA%E4%BC%A0%E5%85%A5%E6%8A%A5%E9%94%99.png)


```java
@RequestMapping(value="/editItems",method={RequestMethod.POST,RequestMethod.GET})
//@RequestParam里边指定request传入参数名称和形参进行绑定。
//通过required属性指定参数是否必须要传入
//通过defaultValue可以设置默认值，如果id参数没有传入，将默认值和形参绑定。
public String editItems(Model model,@RequestParam(value="id",required=true) Integer items_id)throws Exception {

```


## pojo绑定

页面中input的name和controller的pojo形参中的属性名称一致，将页面中数据绑定到pojo。

注意:这里只是要求name和形参的**属性名**一致，而不是要求和形参的**名称**一致，这点不要混淆了，框架会进入形参内部自动匹配pojo类的属性名。(我没看源码，但应该是用反射实现的)



页面定义：

```jsp
<table width="100%" border=1>
<tr>
	<td>商品名称</td>
	<td><input type="text" name="name" value="${itemsCustom.name }"/></td>
</tr>
<tr>
	<td>商品价格</td>
	<td><input type="text" name="price" value="${itemsCustom.price }"/></td>
</tr>
```

controller的pojo形参的定义：

```java
public class Items {
    private Integer id;

    private String name;

    private Float price;

    private String pic;

    private Date createtime;

    private String detail;
```


## 自定义参数绑定实现日期类型绑定

对于controller形参中pojo对象，如果属性中有日期类型，需要自定义参数绑定。

将请求日期数据串传成日期类型，要转换的日期类型和pojo中日期属性的类型保持一致。本文示例中，自定义参数绑定将日期串转成java.util.Date类型。

需要向处理器适配器中注入自定义的参数绑定组件。


- 自定义日期类型绑定

```java
public class CustomDateConverter implements Converter<String,Date>{
    public Date convert(String s) {
        //实现 将日期串转成日期类型(格式是yyyy-MM-dd HH:mm:ss)

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            //转成直接返回
            return simpleDateFormat.parse(s);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //如果参数绑定失败返回null
        return null;

    }
}
```


- 配置方式

```xml
<mvc:annotation-driven conversion-service="conversionService"></mvc:annotation-driven>
```

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


## springmvc和struts2的区别 

- 1.springmvc基于方法开发的，struts2基于类开发的。

springmvc将url和controller方法映射。映射成功后springmvc生成一个Handler对象，对象中只包括了一个method。方法执行结束，形参数据销毁。springmvc的controller开发类似service开发。

- 2.springmvc可以进行单例开发，并且建议使用单例开发，struts2通过类的成员变量接收参数，无法使用单例，只能使用多例。

- 3.经过实际测试，struts2速度慢，在于使用struts标签，如果使用struts建议使用jstl。


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
