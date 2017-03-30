# springmvc学习笔记(18)-json数据交互

标签： springmvc

---

**Contents**

  - [springmvc进行json交互](#springmvc进行json交互)
  - [环境准备](#环境准备)
    - [添加json转换的依赖](#添加json转换的依赖)
    - [配置json转换器](#配置json转换器)
  - [json交互测试](#json交互测试)
    - [输入json串，输出是json串](#输入json串，输出是json串)
    - [输入key/value，输出是json串](#输入keyvalue，输出是json串)



---



本文主要介绍如何在springmvc中进行json数据的交互，先是环境准备和配置，然后分别展示了“输入json串，输出是json串”和“输入key/value，输出是json串”两种情况下的交互


## springmvc进行json交互

json数据格式在接口调用中、html页面中较常用，json格式比较简单，解析还比较方便。

比如：webservice接口，传输json数据.

![json交互](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_json%E4%BA%A4%E4%BA%92.png)

- 请求json、输出json，要求请求的是json串，所以在前端页面中需要将请求的内容转成json，不太方便。
- 请求key/value、输出json。此方法比较常用。

## 环境准备

### 添加json转换的依赖

最开始我少了`jackson-databind`依赖，程序各种报错。

```xml

<!-- json 转换-->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.7.2</version>
</dependency>

<dependency>
    <groupId>org.codehaus.jackson</groupId>
    <artifactId>jackson-mapper-asl</artifactId>
    <version>1.9.13</version>
</dependency>
```

查看依赖树

```
[INFO] +- com.fasterxml.jackson.core:jackson-databind:jar:2.7.2:compile
[INFO] |  +- com.fasterxml.jackson.core:jackson-annotations:jar:2.7.0:compile
[INFO] |  \- com.fasterxml.jackson.core:jackson-core:jar:2.7.2:compile
[INFO] \- org.codehaus.jackson:jackson-mapper-asl:jar:1.9.13:compile
[INFO]    \- org.codehaus.jackson:jackson-core-asl:jar:1.9.13:compile
```


### 配置json转换器

在注解适配器中加入`messageConverters`

```xml

<!--注解适配器 -->
<bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
	<property name="messageConverters">
	<list>
	<bean class="org.springframework.http.converter.json.MappingJacksonHttpMessageConverter"></bean>
	</list>
	</property>
</bean>
```

**注意：如果使用`<mvc:annotation-driven />`则不用定义上边的内容。**

## json交互测试

显示两个按钮分别测试

- jsp页面

```jsp
<%--
  Created by IntelliJ IDEA.
  User: brian
  Date: 2016/3/7
  Time: 20:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>json交互测试</title>
    <script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-1.4.4.min.js"></script>
    <script type="text/javascript">
        //请求json，输出是json
        function requestJson(){     省略    }
        //请求key/value，输出是json
        function responseJson(){    省略    }
    </script>
</head>
<body>
<input type="button" onclick="requestJson()" value="请求json，输出是json"/>
<input type="button" onclick="responseJson()" value="请求key/value，输出是json"/>
</body>

```

- controller


```java
@Controller
public class JsonTest {
    省略
}
```

- 测试结果


### 输入json串，输出是json串

使用jquery的ajax提交json串，对输出的json结果进行解析。

- jsp页面

```jsp
//请求json，输出是json
function requestJson(){

    $.ajax({
        type:'post',
        url:'${pageContext.request.contextPath }/requestJson.action',
        contentType:'application/json;charset=utf-8',
        //数据格式是json串，商品信息
        data:'{"name":"手机","price":999}',
        success:function(data){//返回json结果
            alert(data);
        }

    });

}
```

- controller

```java
 //请求json串(商品信息)，输出json(商品信息)
//@RequestBody将请求的商品信息的json串转成itemsCustom对象
//@ResponseBody将itemsCustom转成json输出
@RequestMapping("/requestJson")
public @ResponseBody ItemsCustom requestJson(@RequestBody ItemsCustom itemsCustom){

    //@ResponseBody将itemsCustom转成json输出
    return itemsCustom;
}
```

- 测试结果

![请求json，返回json](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_json-request-json-1.png)

可以看到，request和response的HTTP头的Content-Type都是`application/json;charset=utf-8`

![请求json，返回json,response的body](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_json-request-json-2.png)


### 输入key/value，输出是json串

使用jquery的ajax提交key/value串，对输出的json结果进行解析

- jsp页面

```jsp
//请求key/value，输出是json
function responseJson(){

    $.ajax({
        type:'post',
        url:'${pageContext.request.contextPath }/responseJson.action',
        //请求是key/value这里不需要指定contentType，因为默认就 是key/value类型
        //contentType:'application/json;charset=utf-8',
        //数据格式是json串，商品信息
        data:'name=手机&price=999',
        success:function(data){//返回json结果
            alert(data.name);
        }

    });

}
```

- controller


```java
 //请求key/value，输出json
@RequestMapping("/responseJson")
public @ResponseBody ItemsCustom responseJson(ItemsCustom itemsCustom){

    //@ResponseBody将itemsCustom转成json输出
    return itemsCustom;
}
```

- 测试结果

![请求key/value,返回json](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_json-request-keyvalue-1.png)


可以看到，key/value键值对的默认Content-Type是`application/x-www-form-urlencoded`,同时，我们收到了响应“手机”



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)



