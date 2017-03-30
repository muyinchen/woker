# springmvc学习笔记(7)-springmvc整合mybatis之mapper

标签： springmvc mybatis

---

**Contents**

  - [整合dao](#整合dao)
- [Global logging configuration](#global-logging-configuration)
- [Console output...](#console-output)
    - [sqlMapConfig.xml](#sqlmapconfigxml)
    - [applicationContext-dao.xml](#applicationcontext-daoxml)
    - [逆向工程生成po类及mapper(单表增删改查)](#逆向工程生成po类及mapper单表增删改查)
    - [手动定义商品查询mapper](#手动定义商品查询mapper)



---

本文记录springmvc整合dao的配置

## 整合dao

首先在resource文件夹下添加两个文件：数据库配置文件和日志配置文件

- 数据库配置文件db.properties

```
jdbc.driver=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://120.25.162.238:3306/mybatis001?characterEncoding=utf-8
jdbc.username=root
jdbc.password=123
```

- 日志配置文件log4j.properties

```
# Global logging configuration
log4j.rootLogger=DEBUG, stdout
# Console output...
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%5p [%t] - %m%n
```

### sqlMapConfig.xml

mybatis自己的配置文件

在resources目录下新建mybatis文件夹，并新建sqlMapConfig.xml文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <!-- 全局setting配置，根据需要添加 -->

    <!-- 配置别名 -->
    <typeAliases>
        <!-- 批量扫描别名 -->
        <package name="com.iot.learnssm.firstssm.po"/>
    </typeAliases>

    <!-- 配置mapper
    由于使用spring和mybatis的整合包进行mapper扫描，这里不需要配置了。
    必须遵循：mapper.xml和mapper.java文件同名且在一个目录
     -->

    <!-- <mappers>

    </mappers> -->
</configuration>
```


### applicationContext-dao.xml

在resources目录下新建spring文件夹，并新建applicationContext-dao.xml文件

配置：

- 数据源
- SqlSessionFactory
- mapper扫描器



```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
    http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-4.0.xsd
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd">

    <!-- 加载db.properties文件中的内容，db.properties文件中key命名要有一定的特殊规则 -->
    <context:property-placeholder location="classpath:db.properties" />
    <!-- 配置数据源 ，dbcp -->

    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="${jdbc.driver}"/>
        <property name="url" value="${jdbc.url}"/>
        <property name="username" value="${jdbc.username}" />
        <property name="password" value="${jdbc.password}" />
        <property name="maxActive" value="30" />
        <property name="maxIdle" value="5" />
    </bean>

    <!-- 从整合包里找，org.mybatis:mybatis-spring:1.2.4 -->
    <!-- sqlSessionFactory -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <!-- 数据库连接池 -->
        <property name="dataSource" ref="dataSource" />
        <!-- 加载mybatis的全局配置文件 -->
        <property name="configLocation" value="classpath:mybatis/sqlMapConfig.xml" />
    </bean>
    <!-- mapper扫描器 -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!-- 扫描包路径，如果需要扫描多个包，中间使用半角逗号隔开 -->
        <property name="basePackage" value="com.iot.learnssm.firstssm.mapper"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory" />
       <!-- <property name="sqlSessionFactory" ref="sqlSessionFactory" />
       会导致数据源配置不管用，数据库连接不上。
       且spring 4弃用
       -->
    </bean>

</beans>

```


### 逆向工程生成po类及mapper(单表增删改查)

方法参见[《mybatis学习笔记(18)-mybatis逆向工程》](http://blog.csdn.net/h3243212/article/details/50778937)


### 手动定义商品查询mapper

针对综合查询mapper，一般情况会有关联查询，建议自定义mapper

- ItemsMapperCustom.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.iot.learnssm.firstssm.mapper.ItemsMapperCustom" >

   <!-- 定义商品查询的sql片段，就是商品查询条件 -->
   <sql id="query_items_where">
   	<!-- 使用动态sql，通过if判断，满足条件进行sql拼接 -->
   	<!-- 商品查询条件通过ItemsQueryVo包装对象 中itemsCustom属性传递 -->
   		<if test="itemsCustom!=null">
   			<if test="itemsCustom.name!=null and itemsCustom.name!=''">
   				items.name LIKE '%${itemsCustom.name}%'
   			</if>
   		</if>
	
   </sql>
  	
  	<!-- 商品列表查询 -->
  	<!-- parameterType传入包装对象(包装了查询条件)
  		resultType建议使用扩展对象
  	 -->
  	<select id="findItemsList" parameterType="com.iot.learnssm.firstssm.po.ItemsQueryVo"
  		 resultType="com.iot.learnssm.firstssm.po.ItemsCustom">
  		SELECT items.* FROM items  
  		<where>
  			<include refid="query_items_where"></include>
  		</where>
  	</select>
  	
</mapper>
```

- ItemsMapperCustom.java

```java
public interface ItemsMapperCustom {
    //商品查询列表
    List<ItemsCustom> findItemsList(ItemsQueryVo itemsQueryVo)throws Exception;
}
```

- po类`ItemsCustom`

```java
package com.iot.learnssm.firstssm.po;

/**
 * Created by Brian on 2016/3/2.
 * 商品信息的扩展类
 */
public class ItemsCustom extends Items{
    //添加商品信息的扩展属性
}
```

- 输入pojo的包装类

```java
package com.iot.learnssm.firstssm.po;

/**
 * Created by Brian on 2016/3/2.
 */
public class ItemsQueryVo {

    //商品信息
    private Items items;

    //为了系统 可扩展性，对原始生成的po进行扩展
    private ItemsCustom itemsCustom;

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    public ItemsCustom getItemsCustom() {
        return itemsCustom;
    }

    public void setItemsCustom(ItemsCustom itemsCustom) {
        this.itemsCustom = itemsCustom;
    }
}
```


整合好dao后的工程目录如图

![springmvc_整合工程-2](http://7xph6d.com1.z0.glb.clouddn.com/springmvc_%E6%95%B4%E5%90%88%E5%B7%A5%E7%A8%8B-2.png)


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)
