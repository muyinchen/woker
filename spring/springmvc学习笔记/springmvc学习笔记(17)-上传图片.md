# springmvc学习笔记(17)-上传图片

标签： springmvc

---

**Contents**

  - [springmvc中对多部件类型解析](#springmvc中对多部件类型解析)
  - [加入上传图片的jar](#加入上传图片的jar)
  - [创建图片虚拟目录存储图片](#创建图片虚拟目录存储图片)
  - [上传图片代码](#上传图片代码)



---


本文展示如何在springmvc中上传图片


## springmvc中对多部件类型解析

在修改商品页面，添加上传商品图片功能。

在页面form中提交`enctype="multipart/form-data"`的数据时，需要springmvc对multipart类型的数据进行解析。

在springmvc.xml中配置multipart类型解析器。

```xml
<!-- 文件上传 -->
<bean id="multipartResolver"
      class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
    <!-- 设置上传文件的最大尺寸为5MB -->
    <property name="maxUploadSize">
        <value>5242880</value>
    </property>
</bean>
```

## 加入上传图片的jar

添加依赖

```xml
<!-- 文件上传 -->
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.3.1</version>
</dependency>
```

依赖树

```
[INFO] \- commons-fileupload:commons-fileupload:jar:1.3.1:compile
[INFO]    \- commons-io:commons-io:jar:2.2:compile
```

可以看到，其实还间接依赖了`commons-io:commons-io:jar`


## 创建图片虚拟目录存储图片

参考我之前的博文

> [在intellij IDEA中为web应用创建图片虚拟目录(详细截图)](http://blog.csdn.net/h3243212/article/details/50819218)


也可以直接修改tomcat的配置,在conf/server.xml文件，添加虚拟目录.

注意：在图片虚拟目录中，一定将图片目录分级创建（提高i/o性能），一般我们采用按日期(年、月、日)进行分级创建。

## 上传图片代码

- 页面

```jsp
<tr>
	<td>商品图片</td>
	<td>
		<c:if test="${items.pic !=null}">
			<img src="/pic/${items.pic}" width=100 height=100/>
			<br/>
		</c:if>
		<input type="file"  name="items_pic"/>
	</td>
</tr>
```

- controller方法

修改：商品修改controller方法：

```java
@RequestMapping("/editItemsSubmit")
    public String editItemsSubmit(
            Model model,
            HttpServletRequest request,
            Integer id,
            @ModelAttribute("items")
            @Validated(value = ValidGroup1.class)ItemsCustom itemsCustom,
            BindingResult bindingResult,
            MultipartFile items_pic
    )throws Exception {
```

```java
 //原始名称
String originalFilename = items_pic.getOriginalFilename();
//上传图片
if(items_pic!=null && originalFilename!=null && originalFilename.length()>0){

    //存储图片的物理路径
    String pic_path = "D:\\tmp\\";


    //新的图片名称
    String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
    //新图片
    File newFile = new File(pic_path+newFileName);

    //将内存中的数据写入磁盘
    items_pic.transferTo(newFile);

    //将新图片名称写到itemsCustom中
    itemsCustom.setPic(newFileName);

}
```



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)


