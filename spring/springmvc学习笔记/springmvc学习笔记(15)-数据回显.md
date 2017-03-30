# springmvc学习笔记(15)-数据回显

标签： springmvc

---

**Contents**

  - [pojo数据回显方法](#pojo数据回显方法)
  - [简单类型数据回显](#简单类型数据回显)



---



本文介绍springmvc中数据回显的几种实现方法


数据回显：提交后，如果出现错误，将刚才提交的数据回显到刚才的提交页面。


## pojo数据回显方法

1.springmvc默认对pojo数据进行回显。

**pojo数据传入controller方法后，springmvc自动将pojo数据放到request域，key等于pojo类型（首字母小写）**

使用`@ModelAttribute`指定pojo回显到页面在request中的key

2.`@ModelAttribute`还可以将方法的返回值传到页面

在商品查询列表页面，通过商品类型查询商品信息。在controller中定义商品类型查询方法，最终将商品类型传到页面。

```java
 // 商品分类
//itemtypes表示最终将方法返回值放在request中的key
@ModelAttribute("itemtypes")
public Map<String, String> getItemTypes() {

    Map<String, String> itemTypes = new HashMap<String, String>();
    itemTypes.put("101", "数码");
    itemTypes.put("102", "母婴");

    return itemTypes;
}
```

页面上可以得到itemTypes数据。


```jsp
<td>
    商品名称：<input name="itemsCustom.name" />
    商品类型：
    <select name="itemtype">
        <c:forEach items="${itemtypes}" var="itemtype">
            <option value="${itemtype.key }">${itemtype.value }</option>
        </c:forEach>
    </select>
</td>
```

3.使用最简单方法使用model，可以不用`@ModelAttribute`

```java
//可以直接使用model将提交pojo回显到页面
//model.addAttribute("items", itemsCustom);
```


## 简单类型数据回显

使用最简单方法使用model

`model.addAttribute("id", id);`




----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
