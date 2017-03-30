# mybatis学习笔记(18)-mybatis逆向工程

标签： mybatis

---

**Contents**

  - [下载逆向工程](#下载逆向工程)
  - [使用方法](#使用方法)
    - [运行逆向工程](#运行逆向工程)
    - [生成代码配置文件](#生成代码配置文件)
    - [执行生成程序](#执行生成程序)
    - [使用生成的代码](#使用生成的代码)



---


mybaits需要程序员自己编写sql语句,mybatis官方提供逆向工程,可以针对单表自动生成mybatis执行所需要的代码（mapper.java,mapper.xml、po..）

企业实际开发中，常用的逆向工程方式：由数据库的表生成java代码。

先附上官网链接：

>* [MyBatis Generator](http://www.mybatis.org/generator/)
>* [A code generator for MyBatis and iBATIS. - GitHub](https://github.com/mybatis/generator)


## 下载逆向工程

这里其实可以添加Maven依赖的，因为跟着视频做的，所以我就建了个普通工程，直接添加了个lib文件夹，把要用的jar包直接copy进来了。

> maven中央仓库`MyBatis-Generator`下载地址：[【MyBatis Generator Core】](http://mvnrepository.com/artifact/org.mybatis.generator/mybatis-generator-core)

## 使用方法

### 运行逆向工程

根据官网说的[（Running MyBatis Generator）](http://www.mybatis.org/generator/running/running.html)：

**Running MyBatis Generator**

MyBatis Generator (MBG) can be run in the following ways:

- From the command prompt with an XML configuration
- As an Ant task with an XML configuration
- As a Maven Plugin
- From another Java program with an XML configuration
- From another Java program with a Java based configuration

还可以通过eclipse的插件生成代码

建议使用java程序方式，不依赖开发工具。


### 生成代码配置文件


```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
  PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
  "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
	<context id="testTables" targetRuntime="MyBatis3">
		<commentGenerator>
			<!-- 是否去除自动生成的注释 true：是 ： false:否 -->
			<property name="suppressAllComments" value="true" />
		</commentGenerator>
		<!--数据库连接的信息：驱动类、连接地址、用户名、密码 -->
		<jdbcConnection driverClass="com.mysql.jdbc.Driver"
			connectionURL="jdbc:mysql://120.25.162.238:3306/mybatis001?characterEncoding=utf-8" 
			userId="root"
			password="123">
		</jdbcConnection>
		<!-- <jdbcConnection driverClass="oracle.jdbc.OracleDriver"
			connectionURL="jdbc:oracle:thin:@127.0.0.1:1521:yycg" 
			userId="yycg"
			password="yycg">
		</jdbcConnection> -->

		<!-- 默认false，把JDBC DECIMAL 和 NUMERIC 类型解析为 Integer，为 true时把JDBC DECIMAL 和 
			NUMERIC 类型解析为java.math.BigDecimal -->
		<javaTypeResolver>
			<property name="forceBigDecimals" value="false" />
		</javaTypeResolver>

		<!-- targetProject:生成PO类的位置 -->
		<javaModelGenerator targetPackage="com.iot.ssm.po"
			targetProject=".\src">
			<!-- enableSubPackages:是否让schema作为包的后缀 -->
			<property name="enableSubPackages" value="false" />
			<!-- 从数据库返回的值被清理前后的空格 -->
			<property name="trimStrings" value="true" />
		</javaModelGenerator>
        <!-- targetProject:mapper映射文件生成的位置 -->
		<sqlMapGenerator targetPackage="com.iot.ssm.mapper" 
			targetProject=".\src">
			<!-- enableSubPackages:是否让schema作为包的后缀 -->
			<property name="enableSubPackages" value="false" />
		</sqlMapGenerator>
		<!-- targetPackage：mapper接口生成的位置 -->
		<javaClientGenerator type="XMLMAPPER"
			targetPackage="com.iot.ssm.mapper" 
			targetProject=".\src">
			<!-- enableSubPackages:是否让schema作为包的后缀 -->
			<property name="enableSubPackages" value="false" />
		</javaClientGenerator>
		<!-- 指定数据库表 -->
		<table tableName="items"></table>
		<table tableName="orders"></table>
		<table tableName="orderdetail"></table>
		<table tableName="user"></table>
		<!-- <table schema="" tableName="sys_user"></table>
		<table schema="" tableName="sys_role"></table>
		<table schema="" tableName="sys_permission"></table>
		<table schema="" tableName="sys_user_role"></table>
		<table schema="" tableName="sys_role_permission"></table> -->
		
		<!-- 有些表的字段需要指定java类型
		 <table schema="" tableName="">
			<columnOverride column="" javaType="" />
		</table> -->
	</context>
</generatorConfiguration>
```

需要注意的位置：

- `javaModelGenerator`,生成PO类的位置
- `sqlMapGenerator`,mapper映射文件生成的位置
- `javaClientGenerator`,mapper接口生成的位置 
- `table`,指定数据库表 


### 执行生成程序

```java
public void generator() throws Exception{

	List<String> warnings = new ArrayList<String>();
	boolean overwrite = true;
	//指定逆向工程配置文件
	File configFile = new File("generatorConfig.xml"); 
	ConfigurationParser cp = new ConfigurationParser(warnings);
	Configuration config = cp.parseConfiguration(configFile);
	DefaultShellCallback callback = new DefaultShellCallback(overwrite);
	MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config,
			callback, warnings);
	myBatisGenerator.generate(null);

} 
```

日志输出：

```
2016-02-27 16:29:46,419 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Retrieving column information for table "items"
2016-02-27 16:29:46,477 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "id", data type 4, in table "mybatis001..items"
2016-02-27 16:29:46,477 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "name", data type 12, in table "mybatis001..items"
2016-02-27 16:29:46,477 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "price", data type 7, in table "mybatis001..items"
2016-02-27 16:29:46,477 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "detail", data type -1, in table "mybatis001..items"
2016-02-27 16:29:46,477 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "pic", data type 12, in table "mybatis001..items"
2016-02-27 16:29:46,478 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "createtime", data type 93, in table "mybatis001..items"
2016-02-27 16:29:46,503 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Retrieving column information for table "orders"
2016-02-27 16:29:46,551 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "id", data type 4, in table "mybatis001..orders"
2016-02-27 16:29:46,551 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "user_id", data type 4, in table "mybatis001..orders"
2016-02-27 16:29:46,551 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "number", data type 12, in table "mybatis001..orders"
2016-02-27 16:29:46,551 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "createtime", data type 93, in table "mybatis001..orders"
2016-02-27 16:29:46,551 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "note", data type 12, in table "mybatis001..orders"
2016-02-27 16:29:46,577 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Retrieving column information for table "orderdetail"
2016-02-27 16:29:46,630 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "id", data type 4, in table "mybatis001..orderdetail"
2016-02-27 16:29:46,630 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "orders_id", data type 4, in table "mybatis001..orderdetail"
2016-02-27 16:29:46,631 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "items_id", data type 4, in table "mybatis001..orderdetail"
2016-02-27 16:29:46,631 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "items_num", data type 4, in table "mybatis001..orderdetail"
2016-02-27 16:29:46,656 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Retrieving column information for table "user"
2016-02-27 16:29:46,706 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "id", data type 4, in table "mybatis001..user"
2016-02-27 16:29:46,706 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "username", data type 12, in table "mybatis001..user"
2016-02-27 16:29:46,706 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "birthday", data type 91, in table "mybatis001..user"
2016-02-27 16:29:46,706 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "sex", data type 1, in table "mybatis001..user"
2016-02-27 16:29:46,706 [main] DEBUG [org.mybatis.generator.internal.db.DatabaseIntrospector] - Found column "address", data type 12, in table "mybatis001..user"
```

生成后的代码：

![逆向工程](http://7xph6d.com1.z0.glb.clouddn.com/mybatis_%E9%80%86%E5%90%91%E5%B7%A5%E7%A8%8B%E7%94%9F%E6%88%90%E4%BB%A3%E7%A0%81.png)

### 使用生成的代码

需要将生成工程中所生成的代码拷贝到自己的工程中。

测试ItemsMapper中的方法

```java
package com.iot.ssm.mapper;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import com.iot.ssm.po.Items;
import com.iot.ssm.po.ItemsExample;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ItemsMapperTest {

	private ApplicationContext applicationContext;
	
	private ItemsMapper itemsMapper;

	//在setUp这个方法得到spring容器
	@Before
	public void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml");
		itemsMapper = (ItemsMapper) applicationContext.getBean("itemsMapper");
	}

	//根据主键删除 
	@Test
	public void testDeleteByPrimaryKey() {
		
	}

	//插入
	@Test
	public void testInsert() {
		//构造 items对象
		Items items = new Items();
		items.setName("手机");
		items.setPrice(999f);
		items.setCreatetime(new Date());
		itemsMapper.insert(items);
	}

	//自定义条件查询
	@Test
	public void testSelectByExample() {
		ItemsExample itemsExample = new ItemsExample();
		//通过criteria构造查询条件
		ItemsExample.Criteria criteria = itemsExample.createCriteria();
		criteria.andNameEqualTo("笔记本");
		//可能返回多条记录
		List<Items> list = itemsMapper.selectByExample(itemsExample);
		
		System.out.println(list);
		
	}

	//根据主键查询
	@Test
	public void testSelectByPrimaryKey() {
		Items items = itemsMapper.selectByPrimaryKey(1);
		System.out.println(items);
	}

	//更新数据
	@Test
	public void testUpdateByPrimaryKey() {
		
		//对所有字段进行更新，需要先查询出来再更新
		Items items = itemsMapper.selectByPrimaryKey(1);
		
		items.setName("手机");
		
		itemsMapper.updateByPrimaryKey(items);
		//如果传入字段不空为才更新，在批量更新中使用此方法，不需要先查询再更新
		//itemsMapper.updateByPrimaryKeySelective(record);
		
	}

}
```

----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)







