# springmvc学习笔记(16)-异常处理器

标签： springmvc

---

**Contents**

  - [异常处理思路](#异常处理思路)
  - [自定义异常类](#自定义异常类)
  - [全局异常处理器](#全局异常处理器)
  - [错误页面](#错误页面)
  - [在springmvc.xml配置全局异常处理器](#在springmvcxml配置全局异常处理器)
  - [异常测试](#异常测试)



---


本文主要介绍springmvc中异常处理的思路，并展示如何自定义异常处理类以及全局异常处理器的配置


## 异常处理思路

系统中异常包括两类：

- 预期异常
- 运行时异常RuntimeException

前者通过捕获异常从而获取异常信息，后者主要通过规范代码开发、测试通过手段减少运行时异常的发生。

系统的dao、service、controller出现都通过throws Exception向上抛出，最后由springmvc前端控制器交由异常处理器进行异常处理，如下图：

![springmvc异常处理](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_%E5%BC%82%E5%B8%B8%E5%A4%84%E7%90%86.png)

springmvc提供全局异常处理器（一个系统只有一个异常处理器）进行统一异常处理。


## 自定义异常类

对不同的异常类型定义异常类，继承Exception。

```java
package com.iot.learnssm.firstssm.exception;

/**
 * Created by brian on 2016/3/7.
 *
 * 系统 自定义异常类，针对预期的异常，需要在程序中抛出此类的异常
 */
public class CustomException  extends  Exception{
    //异常信息
    public String message;

    public CustomException(String message){
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
```

## 全局异常处理器

思路：

系统遇到异常，在程序中手动抛出，dao抛给service、service给controller、controller抛给前端控制器，前端控制器调用全局异常处理器。

全局异常处理器处理思路：

解析出异常类型

- 如果该异常类型是系统自定义的异常，直接取出异常信息，在错误页面展示
- 如果该异常类型不是系统自定义的异常，构造一个自定义的异常类型（信息为“未知错误”）

springmvc提供一个`HandlerExceptionResolver`接口


```java
   public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //handler就是处理器适配器要执行Handler对象（只有method）
        //解析出异常类型
        //如果该 异常类型是系统 自定义的异常，直接取出异常信息，在错误页面展示
        //String message = null;
        //if(ex instanceof CustomException){
			//message = ((CustomException)ex).getMessage();
        //}else{
			////如果该 异常类型不是系统 自定义的异常，构造一个自定义的异常类型（信息为“未知错误”）
			//message="未知错误";
        //}

        //上边代码变为
        CustomException customException;
        if(ex instanceof CustomException){
            customException = (CustomException)ex;
        }else{
            customException = new CustomException("未知错误");
        }

        //错误信息
        String message = customException.getMessage();

        ModelAndView modelAndView = new ModelAndView();

        //将错误信息传到页面
        modelAndView.addObject("message", message);

        //指向错误页面
        modelAndView.setViewName("error");

        return modelAndView;

    }
}
```

## 错误页面

```jsp
<%--
  Created by IntelliJ IDEA.
  User: Brian
  Date: 2016/3/4
  Time: 10:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>错误提示</title>
</head>
<body>
${message}
</body>
</html>
```

## 在springmvc.xml配置全局异常处理器

```xml
<!-- 全局异常处理器
只要实现HandlerExceptionResolver接口就是全局异常处理器
-->
<bean class="com.iot.learnssm.firstssm.exception.CustomExceptionResolver"></bean>
```

全局异常处理器只有一个，配置多个也没用。



## 异常测试

在controller、service、dao中任意一处需要手动抛出异常。如果是程序中手动抛出的异常，在错误页面中显示自定义的异常信息，如果不是手动抛出异常说明是一个运行时异常，在错误页面只显示“未知错误”。

- 在商品修改的controller方法中抛出异常 .

```java
public String editItems(Model model,@RequestParam(value="id",required=true) Integer items_id)throws Exception {

    //调用service根据商品id查询商品信息
    ItemsCustom itemsCustom = itemsService.findItemsById(items_id);

    //判断商品是否为空，根据id没有查询到商品，抛出异常，提示用户商品信息不存在
    if(itemsCustom == null){
		throw new CustomException("修改的商品信息不存在!");
    }

    //通过形参中的model将model数据传到页面
    //相当于modelAndView.addObject方法
    model.addAttribute("items", itemsCustom);

    return "items/editItems";
}
```

- 在service接口中抛出异常：

```java
public ItemsCustom findItemsById(Integer id) throws Exception {
    Items items = itemsMapper.selectByPrimaryKey(id);
    if(items==null){
        throw new CustomException("修改的商品信息不存在!");
    }
    //中间对商品信息进行业务处理
    //....
    //返回ItemsCustom
    ItemsCustom itemsCustom = null;
    //将items的属性值拷贝到itemsCustom
    if(items!=null){
        itemsCustom = new ItemsCustom();
        BeanUtils.copyProperties(items, itemsCustom);
    }

    return itemsCustom;
}
```


- 如果与业务功能相关的异常，建议在service中抛出异常。
- 与业务功能没有关系的异常，建议在controller中抛出。

上边的功能，建议在service中抛出异常。



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
