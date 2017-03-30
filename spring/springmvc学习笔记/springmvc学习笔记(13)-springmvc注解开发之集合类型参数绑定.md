# springmvc学习笔记(13)-springmvc注解开发之集合类型参数绑定

标签： springmvc

---

**Contents**

  - [数组绑定](#数组绑定)
    - [需求](#需求)
    - [表现层实现](#表现层实现)
  - [list绑定](#list绑定)
    - [需求](#需求)
    - [表现层实现](#表现层实现)
  - [map绑定](#map绑定)



---


本文主要介绍注解开发的集合类型参数绑定，包括数组绑定，list绑定以及map绑定

## 数组绑定

### 需求

商品批量删除，用户在页面选择多个商品，批量删除。

### 表现层实现

关键：将页面选择(多选)的商品id，传到controller方法的形参，方法形参使用数组接收页面请求的多个商品id。

- controller方法定义：

```java
// 批量删除 商品信息
@RequestMapping("/deleteItems")
public String deleteItems(Integer[] items_id) throws Exception
```

- 页面定义：

```jsp
<c:forEach items="${itemsList }" var="item">
<tr>
    <td><input type="checkbox" name="items_id" value="${item.id}"/></td>
	<td>${item.name }</td>
	<td>${item.price }</td>
	<td><fmt:formatDate value="${item.createtime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
	<td>${item.detail }</td>
	
	<td><a href="${pageContext.request.contextPath }/items/editItems.action?id=${item.id}">修改</a></td>

</tr>
</c:forEach>
```



## list绑定

### 需求

通常在需要批量提交数据时，将提交的数据绑定到`list<pojo>`中，比如：成绩录入（录入多门课成绩，批量提交），

本例子需求：批量商品修改，在页面输入多个商品信息，将多个商品信息提交到controller方法中。

### 表现层实现

- controller方法定义：
   - 1、进入批量商品修改页面(页面样式参考商品列表实现)
   - 2、批量修改商品提交

使用List接收页面提交的批量数据，通过包装pojo接收，在包装pojo中定义`list<pojo>`属性

```java
public class ItemsQueryVo {

    //商品信息
    private Items items;

    //为了系统 可扩展性，对原始生成的po进行扩展
    private ItemsCustom itemsCustom;

    //批量商品信息
    private List<ItemsCustom> itemsList;
```


```java
// 批量修改商品提交
// 通过ItemsQueryVo接收批量提交的商品信息，将商品信息存储到itemsQueryVo中itemsList属性中。
@RequestMapping("/editItemsAllSubmit")
public String editItemsAllSubmit(ItemsQueryVo itemsQueryVo) throws Exception {

    return "success";
}
```

- 页面定义：

```jsp
<c:forEach items="${itemsList }" var="item" varStatus="status">
    <tr>

        <td><input name="itemsList[${status.index }].name" value="${item.name }"/></td>
        <td><input name="itemsList[${status.index }].price" value="${item.price }"/></td>
        <td><input name="itemsList[${status.index }].createtime" value="<fmt:formatDate value="${item.createtime}" pattern="yyyy-MM-dd HH:mm:ss"/>"/></td>
        <td><input name="itemsList[${status.index }].detail" value="${item.detail }"/></td>

    </tr>
</c:forEach>
```

name的格式：

**`对应包装pojo中的list类型属性名`[`下标(从0开始)`].`包装pojo中List类型的属性中pojo的属性名`**

例子：

`"name="itemsList[${status.index }].price"`


*可以和包装类型的参数绑定归纳对比一下，其实就是在包装类的pojo基础上多了个下标。只不过包装类参数绑定时，要和包装pojo中的pojo类性的属性名一致，而list参数绑定时，要和包装pojo中的list类型的属性名一致。*


## map绑定

也通过在包装pojo中定义map类型属性。

在包装类中定义Map对象，并添加get/set方法，action使用包装对象接收。

- 包装类中定义Map对象如下：

```java
Public class QueryVo {
private Map<String, Object> itemInfo = new HashMap<String, Object>();
  //get/set方法..
}
```

- 页面定义如下：

```java
<tr>
<td>学生信息：</td>
<td>
姓名：<inputtype="text"name="itemInfo['name']"/>
年龄：<inputtype="text"name="itemInfo['price']"/>
.. .. ..
</td>
</tr>
```



- Contrller方法定义如下：

```java
public String useraddsubmit(Model model,QueryVo queryVo)throws Exception{
System.out.println(queryVo.getStudentinfo());
}
```



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
