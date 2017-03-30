# mybatis学习笔记(15)-查询缓存之二级缓存

标签： mybatis

---

**Contents**

  - [二级缓存原理](#二级缓存原理)
  - [开启二级缓存](#开启二级缓存)
  - [调用pojo类实现序列化接口](#调用pojo类实现序列化接口)
  - [测试方法](#测试方法)
  - [useCache配置](#usecache配置)
  - [刷新缓存（就是清空缓存）](#刷新缓存（就是清空缓存）)
  - [应用场景和局限性](#应用场景和局限性)



---

本文主要讲mybatis的二级缓存，二级缓存是mapper级别的缓存，多个SqlSession去操作同一个Mapper的sql语句，多个SqlSession可以共用二级缓存，二级缓存是跨SqlSession的。


## 二级缓存原理


![二级缓存原理](http://7xph6d.com1.z0.glb.clouddn.com/mybatis_%E4%BA%8C%E7%BA%A7%E7%BC%93%E5%AD%98%E5%8E%9F%E7%90%86%E5%9B%BE.png)

首先开启mybatis的二级缓存.

sqlSession1去查询用户id为1的用户信息，查询到用户信息会将查询数据存储到二级缓存中。

如果SqlSession3去执行相同mapper下sql，执行commit提交，清空该mapper下的二级缓存区域的数据。

sqlSession2去查询用户id为1的用户信息，去缓存中找是否存在数据，如果存在直接从缓存中取出数据。

二级缓存与一级缓存区别，**二级缓存的范围更大，多个sqlSession可以共享一个UserMapper的二级缓存区域**。

UserMapper有一个二级缓存区域（按namespace分），其它mapper也有自己的二级缓存区域（按namespace分）。每一个namespace的mapper都有一个二缓存区域，两个mapper的namespace如果相同，这两个mapper执行sql查询到数据将存在相同的二级缓存区域中。


## 开启二级缓存

mybaits的二级缓存是mapper范围级别，除了在SqlMapConfig.xml设置二级缓存的总开关，还要在具体的mapper.xml中开启二级缓存。

在核心配置文件SqlMapConfig.xml中加入`<setting name="cacheEnabled" value="true"/>`

|设置项|描述|允许值|默认值|
|:---|:---|:---|:---|
|cacheEnabled|对在此配置文件下的所有cache 进行全局性开/关设置。|	true/false |	true|

```xml
<!-- 开启二级缓存 -->
<setting name="cacheEnabled" value="true"/>
```

在UserMapper.xml中开启二缓存，UserMapper.xml下的sql执行完成会存储到它的缓存区域（HashMap）。

```xml
<mapper namespace="com.iot.mybatis.mapper.UserMapper">
<!-- 开启本mapper的namespace下的二级缓存-->
<cache />

...

</mapper>
```


## 调用pojo类实现序列化接口

```java
public class User implements Serializable{
    ....
}
```

为了将缓存数据取出执行反序列化操作，因为二级缓存数据存储介质多种多样，不一定在内存。

## 测试方法

```java
// 二级缓存测试
@Test
public void testCache2() throws Exception {
	SqlSession sqlSession1 = sqlSessionFactory.openSession();
	SqlSession sqlSession2 = sqlSessionFactory.openSession();
	SqlSession sqlSession3 = sqlSessionFactory.openSession();
	// 创建代理对象
	UserMapper userMapper1 = sqlSession1.getMapper(UserMapper.class);
	// 第一次发起请求，查询id为1的用户
	User user1 = userMapper1.findUserById(1);
	System.out.println(user1);

	//这里执行关闭操作，将sqlsession中的数据写到二级缓存区域
	sqlSession1.close();


//		//使用sqlSession3执行commit()操作
//		UserMapper userMapper3 = sqlSession3.getMapper(UserMapper.class);
//		User user  = userMapper3.findUserById(1);
//		user.setUsername("张明明");
//		userMapper3.updateUser(user);
//		//执行提交，清空UserMapper下边的二级缓存
//		sqlSession3.commit();
//		sqlSession3.close();



	UserMapper userMapper2 = sqlSession2.getMapper(UserMapper.class);
	// 第二次发起请求，查询id为1的用户
	User user2 = userMapper2.findUserById(1);
	System.out.println(user2);

	sqlSession2.close();
}
```



1.无更新，输出

```
DEBUG [main] - Cache Hit Ratio [com.iot.mybatis.mapper.UserMapper]: 0.0
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Created connection 103887628.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 1(Integer)
DEBUG [main] - <==      Total: 1
User [id=1, username=测试用户22, sex=2, birthday=null, address=null]
DEBUG [main] - Resetting autocommit to true on JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Closing JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Returned connection 103887628 to pool.
DEBUG [main] - Cache Hit Ratio [com.iot.mybatis.mapper.UserMapper]: 0.5
User [id=1, username=测试用户22, sex=2, birthday=null, address=null]
```

2.有更新，输出


```
DEBUG [main] - Cache Hit Ratio [com.iot.mybatis.mapper.UserMapper]: 0.0
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Created connection 103887628.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 1(Integer)
DEBUG [main] - <==      Total: 1
User [id=1, username=测试用户22, sex=2, birthday=null, address=null]
DEBUG [main] - Resetting autocommit to true on JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Closing JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Returned connection 103887628 to pool.
DEBUG [main] - Cache Hit Ratio [com.iot.mybatis.mapper.UserMapper]: 0.5
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Checked out connection 103887628 from pool.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - ==>  Preparing: update user set username=?,birthday=?,sex=?,address=? where id=? 
DEBUG [main] - ==> Parameters: 张明明(String), null, 2(String), null, 1(Integer)
DEBUG [main] - <==    Updates: 1
DEBUG [main] - Committing JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Resetting autocommit to true on JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Closing JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Returned connection 103887628 to pool.
DEBUG [main] - Cache Hit Ratio [com.iot.mybatis.mapper.UserMapper]: 0.3333333333333333
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Checked out connection 103887628 from pool.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 1(Integer)
DEBUG [main] - <==      Total: 1
User [id=1, username=张明明, sex=2, birthday=null, address=null]
DEBUG [main] - Resetting autocommit to true on JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Closing JDBC Connection [com.mysql.jdbc.JDBC4Connection@631330c]
DEBUG [main] - Returned connection 103887628 to pool.
```


## useCache配置

在statement中设置`useCache=false`可以禁用当前select语句的二级缓存，即每次查询都会发出sql去查询，默认情况是true，即该sql使用二级缓存。

`<select id="findOrderListResultMap" resultMap="ordersUserMap" useCache="false">`

总结：针对每次查询都需要最新的数据sql，要设置成useCache=false，禁用二级缓存。



## 刷新缓存（就是清空缓存）

刷新缓存就是清空缓存。在mapper的同一个namespace中，如果有其它insert、update、delete操作数据后需要刷新缓存，如果不执行刷新缓存会出现脏读。

 设置statement配置中的`flushCache="true"`属性，默认情况下为true即刷新缓存，如果改成false则不会刷新。使用缓存时如果手动修改数据库表中的查询数据会出现脏读。如下：
 
`<insert id="insertUser" parameterType="cn.itcast.mybatis.po.User" flushCache="true">`

总结：一般下执行完commit操作都需要刷新缓存，`flushCache=true`表示刷新缓存，这样可以避免数据库脏读。

## 应用场景和局限性

- 应用场景

对于访问多的查询请求且用户对查询结果实时性要求不高，此时可采用mybatis二级缓存技术降低数据库访问量，提高访问速度，业务场景比如：耗时较高的统计分析sql、电话账单查询sql等。

实现方法如下：通过设置刷新间隔时间，由mybatis每隔一段时间自动清空缓存，根据数据变化频率设置缓存刷新间隔flushInterval，比如设置为30分钟、60分钟、24小时等，根据需求而定。


- 局限性

mybatis二级缓存对细粒度的数据级别的缓存实现不好，比如如下需求：对商品信息进行缓存，由于商品信息查询访问量大，但是要求用户每次都能查询最新的商品信息，此时如果使用mybatis的二级缓存就无法实现当一个商品变化时只刷新该商品的缓存信息而不刷新其它商品的信息，因为mybaits的二级缓存区域以mapper为单位划分，当一个商品信息变化会将所有商品信息的缓存数据全部清空。解决此类问题需要在业务层根据需求对数据有针对性缓存。




----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)

