# springmvc学习笔记(6)-springmvc整合mybatis(IDEA中通过maven构建)

标签： springmvc mybatis

---

**Contents**

  - [整合思路](#整合思路)
  - [工程结构](#工程结构)
    - [添加依赖](#添加依赖)
    - [建包](#建包)



---


本文主要展示如何在intellij IDEA中通过maven构建springmvc+mybatis框架的开发环境。



需求：使用springmvc和mybatis完成商品列表查询

## 整合思路

![springmvc_整合mybatis思路](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_%E6%95%B4%E5%90%88mybatis%E6%80%9D%E8%B7%AF.png)


- 第一步：整合dao层
  - mybatis和spring整合，通过spring管理mapper接口。
  - 使用mapper的扫描器自动扫描mapper接口在spring中进行注册。

- 第二步：整合service层
  - 通过spring管理service接口。
  - 使用配置方式将service接口配置在spring配置文件中。
  - 实现事务控制。

- 第三步：整合springmvc
   - 由于springmvc是spring的模块，不需要整合。



## 工程结构

不同于[《mybatis学习笔记(17)-spring和mybatis整合》](http://blog.csdn.net/h3243212/article/details/50778934)中的示例demo,**本文的整合采用maven构建**。

如何创建使用maven构建的web应用可以参考前面的一篇[《springmvc学习笔记(1)-框架原理和入门配置》](http://blog.csdn.net/h3243212/article/details/50828141#环境搭建)

`new->project->maven`，建一个裸的maven工程，手动建webapp的目录

在`src/main`下新建文件夹`webapp`

### 添加依赖

pom.xml文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.iot.learnssm</groupId>
    <artifactId>learnssm-firstssm</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>war</packaging>
    
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <!-- jar 版本设置 -->
        <spring.version>4.2.4.RELEASE</spring.version>
    </properties>


    <dependencies>

        <!-- spring框架-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aspects</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>${spring.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>${spring.version}</version>
        </dependency>


        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.38</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.3.1</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>1.2.4</version>
        </dependency>
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>
        
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.18</version>
        </dependency>

        <dependency>
            <groupId>commons-dbcp</groupId>
            <artifactId>commons-dbcp</artifactId>
            <version>1.4</version>
        </dependency>

        <!-- JSP tag -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>
        <dependency>
            <groupId>taglibs</groupId>
            <artifactId>standard</artifactId>
            <version>1.1.2</version>
        </dependency>
    </dependencies>
    
</project>
```

这里添加的依赖可能有多的,但总比少包好，我开始就是引少了依赖(springframework的依赖只引用了spring-mvc,连spring-core都没引)，导致报错,以后会出一篇博客专门讲这个系列笔记中debug相关问题。


### 建包

在java目录下建各个package,按照maven的命名习惯:

`com.公司名.项目名.模块名`

这里我的包为：

`com.iot.learnssm.firstssm`

包含几个子包：

- controller
- mapper
- po
- service
  - impl 

项目结构如图：
  
![springmvc_整合工程-1](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_%E6%95%B4%E5%90%88%E5%B7%A5%E7%A8%8B-1.png)


后面几篇笔记会依次记录mapper,service,controller各个部分的整合


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)



