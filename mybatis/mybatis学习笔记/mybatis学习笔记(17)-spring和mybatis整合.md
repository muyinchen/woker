# mybatis学习笔记(17)-spring和mybatis整合

标签： mybatis springmvc

---

**Contents**

  - [整合思路](#整合思路)
  - [整合环境](#整合环境)
  - [sqlSessionFactory](#sqlsessionfactory)
  - [原始dao开发(和spring整合后)](#原始dao开发和spring整合后)
  - [mapper代理开发](#mapper代理开发)
  - [遇到的问题](#遇到的问题)



---

本文主要将如何将spring和mybatis整合，只是作简单的示例，没有使用Maven构建。并展示mybatis与spring整合后如何进行原始dao开发和mapper代理开发。


## 整合思路

需要spring通过单例方式管理`SqlSessionFactory`。

spring和mybatis整合生成代理对象，使用`SqlSessionFactory`创建`SqlSession`。（spring和mybatis整合自动完成）

持久层的mapper都需要由spring进行管理。


## 整合环境

创建一个新的java工程（接近实际开发的工程结构）

jar包：

- mybatis3.2.7的jar包
- spring3.2.0的jar包
- mybatis和spring的整合包：早期ibatis和spring整合是由spring官方提供，mybatis和spring整合由mybatis提供。

![mybatis与spring整合工程结构图](http://7xph6d.com1.z0.glb.clouddn.com/mybatis_%E4%B8%8Espring%E6%95%B4%E5%90%88%E5%B7%A5%E7%A8%8B%E7%BB%93%E6%9E%84%E5%9B%BE.png)

## sqlSessionFactory

在applicationContext.xml配置`sqlSessionFactory`和数据源

`sqlSessionFactory`在mybatis和spring的整合包下。

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop" xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
		http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
		http://www.springframework.org/schema/mvc
		http://www.springframework.org/schema/mvc/spring-mvc-3.2.xsd
		http://www.springframework.org/schema/context
		http://www.springframework.org/schema/context/spring-context-3.2.xsd
		http://www.springframework.org/schema/aop
		http://www.springframework.org/schema/aop/spring-aop-3.2.xsd
		http://www.springframework.org/schema/tx
		http://www.springframework.org/schema/tx/spring-tx-3.2.xsd ">


    <!-- 加载配置文件 -->
    <context:property-placeholder location="classpath:db.properties" />

    <!-- 数据源，使用dbcp -->
    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource"
          destroy-method="close">
        <property name="driverClassName" value="${jdbc.driver}" />
        <property name="url" value="${jdbc.url}" />
        <property name="username" value="${jdbc.username}" />
        <property name="password" value="${jdbc.password}" />
        <property name="maxActive" value="10" />
        <property name="maxIdle" value="5" />
    </bean>

    <!-- sqlSessinFactory -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <!-- 加载mybatis的配置文件 -->
        <property name="configLocation" value="mybatis/SqlMapConfig.xml" />
        <!-- 数据源 -->
        <property name="dataSource" ref="dataSource" />
    </bean>
</beans>
```

## 原始dao开发(和spring整合后)

- User.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- namespace 命名空间，作用就是对sql进行分类化管理,理解为sql隔离
 注意：使用mapper代理方法开发，namespace有特殊重要的作用
 -->
<mapper namespace="test">
    <!-- 在映射文件中配置很多sql语句 -->
    <!--需求:通过id查询用户表的记录 -->
    <!-- 通过select执行数据库查询
     id:标识映射文件中的sql，称为statement的id
     将sql语句封装到mappedStatement对象中，所以将id称为statement的id
     parameterType:指定输入参数的类型
     #{}标示一个占位符,
     #{value}其中value表示接收输入参数的名称，如果输入参数是简单类型，那么#{}中的值可以任意。

     resultType：指定sql输出结果的映射的java对象类型，select指定resultType表示将单条记录映射成java对象
     -->
    <select id="findUserById" parameterType="int" resultType="com.iot.ssm.po.User">
        SELECT * FROM  user  WHERE id=#{value}
    </select>


</mapper>
```

在SqlMapconfig.xml中加载User.xml

```xml
 <!-- 加载映射文件-->
<mappers>
    <mapper resource="sqlmap/User.xml"/>
</mappers>  
```

- dao(实现类继承``SqlSessionDaoSupport``)

```java
public interface UserDao {
    //根据id查询用户信息
    public User findUserById(int id) throws Exception;
}
```

dao接口实现类需要注入`SqlSessoinFactory`，通过spring进行注入。这里spring声明配置方式，配置dao的bean

**让UserDaoImpl实现类继承SqlSessionDaoSupport**

```java
public class UserDaoImpl extends SqlSessionDaoSupport implements UserDao{


    @Override
    public User findUserById(int id) throws Exception {
        //继承SqlSessionDaoSupport，通过this.getSqlSession()得到sqlSessoin
        SqlSession sqlSession = this.getSqlSession();
        User user = sqlSession.selectOne("test.findUserById",id);

        return user;
    }

}
```

- 配置dao

在applicationContext.xml中配置dao

```xml
<!-- 原始dao接口 -->
<bean id="userDao" class="com.iot.ssm.dao.UserDaoImpl">
    <property name="sqlSessionFactory" ref="sqlSessionFactory"/>
</bean>
```

- 测试程序

```java
public class UserDaoImplTest {

	private ApplicationContext applicationContext;

	//在setUp这个方法得到spring容器
	@Before
	public void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml");
	}

	@Test
	public void testFindUserById() throws Exception {
		// 创建UserDao的对象
		UserDao userDao = (UserDao)applicationContext.getBean("userDao");

		// 调用UserDao的方法
		User user = userDao.findUserById(1);
		
		System.out.println(user);
	}

}
```


## mapper代理开发


- mapper.java

```java
public interface UserMapper {
    //根据id查询用户信息
    User findUserById(int id) throws Exception;

}
```

- mapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--
 namespace 命名空间，作用就是对sql进行分类化管理,理解为sql隔离
 注意：使用mapper代理方法开发，namespace有特殊重要的作用
 -->
<mapper namespace="com.iot.ssm.mapper.UserMapper">

    <!-- 在映射文件中配置很多sql语句 -->

    <select id="findUserById" parameterType="int" resultType="user">
        SELECT * FROM  user  WHERE id=#{value}
    </select>


</mapper>
```


- 通过`MapperFactoryBean`创建代理对象

```xml
 <!-- mapper配置
    MapperFactoryBean：根据mapper接口生成代理对象
    -->

<bean id="userMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
        //mapperInterface指定mapper接口
        <property name="mapperInterface" value="com.iot.ssm.mapper.UserMapper"/>
        <property name="sqlSessionFactory" ref="sqlSessionFactory"/>
</bean>
```

此方法问题：需要针对每个mapper进行配置，麻烦。


- 通过`MapperScannerConfigurer`进行mapper扫描（建议使用）

```xml
<!-- mapper批量扫描，从mapper包中扫描出mapper接口，自动创建代理对象并且在spring容器中注册
    遵循规范：将mapper.java和mapper.xml映射文件名称保持一致，且在一个目录 中
    自动扫描出来的mapper的bean的id为mapper类名（首字母小写）
    -->
<bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <!-- 指定扫描的包名
    如果扫描多个包，每个包中间使用半角逗号分隔
    -->
    <property name="basePackage" value="com.iot.ssm.mapper"/>
    <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>

</bean>
```

- 测试代码

```java
package com.iot.mybatis.mapper;

import com.iot.ssm.mapper.UserMapper;
import com.iot.ssm.po.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserMapperTest {

	private ApplicationContext applicationContext;

	//在setUp这个方法得到spring容器
	@Before
	public void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml");
	}



	@Test
	public void testFindUserById() throws Exception {


		UserMapper userMapper = (UserMapper)applicationContext.getBean("userMapper");

		//调用userMapper的方法

		User user = userMapper.findUserById(1);

		System.out.println(user);

	}

}
```



## 遇到的问题

```
org.springframework.beans.factory.BeanDefinitionStoreException: Failed to read candidate component class: file [D:\intellij\workspace\spring-mybatis\out\production\spring-mybatis\com\iot\ssm\mapper\UserMapper.class]; nested exception is java.lang.IllegalArgumentException

	at org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider.findCandidateComponents(ClassPathScanningCandidateComponentProvider.java:281)
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.doScan(ClassPathBeanDefinitionScanner.java:242)
	at org.mybatis.spring.mapper.ClassPathMapperScanner.doScan(ClassPathMapperScanner.java:155)
	at org.springframework.context.annotation.ClassPathBeanDefinitionScanner.scan(ClassPathBeanDefinitionScanner.java:220)
	at org.mybatis.spring.mapper.MapperScannerConfigurer.postProcessBeanDefinitionRegistry(MapperScannerConfigurer.java:315)
	at org.springframework.context.support.AbstractApplicationContext.invokeBeanFactoryPostProcessors(AbstractApplicationContext.java:630)
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:461)
	at org.springframework.context.support.ClassPathXmlApplicationContext.<init>(ClassPathXmlApplicationContext.java:139)
	at org.springframework.context.support.ClassPathXmlApplicationContext.<init>(ClassPathXmlApplicationContext.java:83)
	at com.iot.mybatis.mapper.UserMapperTest.setUp(UserMapperTest.java:17)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:483)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)
	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:27)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:263)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:69)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:48)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:292)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:157)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:69)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:234)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:74)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:483)
	at com.intellij.rt.execution.application.AppMain.main(AppMain.java:144)
Caused by: java.lang.IllegalArgumentException
	at org.springframework.asm.ClassReader.<init>(Unknown Source)
	at org.springframework.asm.ClassReader.<init>(Unknown Source)
	at org.springframework.asm.ClassReader.<init>(Unknown Source)
	at org.springframework.core.type.classreading.SimpleMetadataReader.<init>(SimpleMetadataReader.java:52)
	at org.springframework.core.type.classreading.SimpleMetadataReaderFactory.getMetadataReader(SimpleMetadataReaderFactory.java:80)
	at org.springframework.core.type.classreading.CachingMetadataReaderFactory.getMetadataReader(CachingMetadataReaderFactory.java:101)
	at org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider.findCandidateComponents(ClassPathScanningCandidateComponentProvider.java:257)
	... 35 more
```


- 搜到的答案

>* [BeanDefinitionStoreException Failed to read candidate component class](http://stackoverflow.com/questions/22771826/beandefinitionstoreexception-failed-to-read-candidate-component-class)
>* [Failed to read candidate component错误](http://www.osblog.net/blog/399.html)


总结起来就是java 8 和spring 3 不能一起用，我在IDEA的`project settings`里把`project language level`换成`7`就好了。具体原因还不清楚



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)


