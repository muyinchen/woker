# springmvc学习笔记(12)-springmvc注解开发之包装类型参数绑定

标签： springmvc

---

**Contents**

  - [需求](#需求)
  - [实现方法](#实现方法)
  - [页面参数和controller方法形参定义](#页面参数和controller方法形参定义)



---

本文主要介绍注解开发的介绍包装类型的参数绑定


## 需求

商品查询controller方法中实现商品查询条件传入。

## 实现方法

- 第一种方法：在形参中添加`HttpServletRequest request`参数，通过request接收查询条件参数。
- 第二种方法：在形参中让包装类型的pojo接收查询条件参数。

分析：

页面传参数的特点：复杂，多样性。条件包括：用户账号、商品编号、订单信息。。。

如果将用户账号、商品编号、订单信息等放在简单pojo（属性是简单类型）中，pojo类属性比较多，比较乱。建议使用包装类型的pojo，pojo中属性是pojo。

## 页面参数和controller方法形参定义

- 页面参数：

商品名称：`<input name="itemsCustom.name" />`

**注意：itemsCustom和包装pojo中的属性名一致即可。**


- controller方法形参：

`public ModelAndView queryItems(HttpServletRequest request, ItemsQueryVo itemsQueryVo) throws Exception`

- 包装类ItemsQueryVo中部分属性：

```java
public class ItemsQueryVo {

    //商品信息
    private Items items;

    //为了系统 可扩展性，对原始生成的po进行扩展
    private ItemsCustom itemsCustom;
```

可见，`ItemsQueryVo`中属性`itemsCustom`和页面参数中一致



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)




