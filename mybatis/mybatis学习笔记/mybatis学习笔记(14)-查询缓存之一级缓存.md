# mybatis学习笔记(14)-查询缓存之一级缓存

标签： mybatis

---

**Contents**

  - [查询缓存](#查询缓存)
  - [一级缓存](#一级缓存)
    - [一级缓存工作原理](#一级缓存工作原理)
    - [一级缓存测试](#一级缓存测试)
    - [一级缓存应用](#一级缓存应用)



---

本文主要讲mybatis的一级缓存，一级缓存是SqlSession级别的缓存。


## 查询缓存

mybatis提供查询缓存，用于减轻数据压力，提高数据库性能。

mybaits提供一级缓存，和二级缓存。

![查询缓存](http://7xph6d.com1.z0.glb.clouddn.com/mybatis_%E6%9F%A5%E8%AF%A2%E7%BC%93%E5%AD%98.png)

一级缓存是SqlSession级别的缓存。在操作数据库时需要构造sqlSession对象，在对象中有一个数据结构（HashMap）用于存储缓存数据。不同的sqlSession之间的缓存数据区域（HashMap）是互相不影响的。

二级缓存是mapper级别的缓存，多个SqlSession去操作同一个Mapper的sql语句，多个SqlSession可以共用二级缓存，二级缓存是跨SqlSession的。

为什么要用缓存？

如果缓存中有数据就不用从数据库中获取，大大提高系统性能。


## 一级缓存

### 一级缓存工作原理

![一级缓存工作原理](http://7xph6d.com1.z0.glb.clouddn.com/mybatis_%E4%B8%80%E7%BA%A7%E7%BC%93%E5%AD%98%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86.png)

第一次发起查询用户id为1的用户信息，先去找缓存中是否有id为1的用户信息，如果没有，从数据库查询用户信息。得到用户信息，将用户信息存储到一级缓存中。

如果sqlSession去执行commit操作（执行插入、更新、删除），清空SqlSession中的一级缓存，这样做的目的为了让缓存中存储的是最新的信息，避免脏读。

第二次发起查询用户id为1的用户信息，先去找缓存中是否有id为1的用户信息，缓存中有，直接从缓存中获取用户信息。


### 一级缓存测试
 
mybatis默认支持一级缓存，不需要在配置文件去配置。

按照上边一级缓存原理步骤去测试。

测试代码

```java
// 一级缓存测试
@Test
public void testCache1() throws Exception {
	SqlSession sqlSession = sqlSessionFactory.openSession();// 创建代理对象
	UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

	// 下边查询使用一个SqlSession
	// 第一次发起请求，查询id为1的用户
	User user1 = userMapper.findUserById(1);
	System.out.println(user1);

	// 如果sqlSession去执行commit操作（执行插入、更新、删除），清空SqlSession中的一级缓存，这样做的目的为了让缓存中存储的是最新的信息，避免脏读。

	// 更新user1的信息
	// user1.setUsername("测试用户22");
	// userMapper.updateUser(user1);
	// //执行commit操作去清空缓存
	// sqlSession.commit();

	// 第二次发起请求，查询id为1的用户
	User user2 = userMapper.findUserById(1);
	System.out.println(user2);

	sqlSession.close();

}
```



1.不执行更新操作，输出:

```
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Created connection 110771485.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@69a3d1d]
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 1(Integer)
DEBUG [main] - <==      Total: 1
User [id=1, username=王五, sex=2, birthday=null, address=null]
User [id=1, username=王五, sex=2, birthday=null, address=null]
DEBUG [main] - Resetting autocommit to true on JDBC Connection [com.mysql.jdbc.JDBC4Connection@69a3d1d]
DEBUG [main] - Closing JDBC Connection [com.mysql.jdbc.JDBC4Connection@69a3d1d]
DEBUG [main] - Returned connection 110771485 to pool.
```

2.取消测试代码中更新的的注释，输出：

```
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Created connection 110771485.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@69a3d1d]
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 1(Integer)
DEBUG [main] - <==      Total: 1
User [id=1, username=王五, sex=2, birthday=null, address=null]
DEBUG [main] - ==>  Preparing: update user set username=?,birthday=?,sex=?,address=? where id=? 
DEBUG [main] - ==> Parameters: 测试用户22(String), null, 2(String), null, 1(Integer)
DEBUG [main] - <==    Updates: 1
DEBUG [main] - Committing JDBC Connection [com.mysql.jdbc.JDBC4Connection@69a3d1d]
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 1(Integer)
DEBUG [main] - <==      Total: 1
User [id=1, username=测试用户22, sex=2, birthday=null, address=null]
DEBUG [main] - Resetting autocommit to true on JDBC Connection [com.mysql.jdbc.JDBC4Connection@69a3d1d]
DEBUG [main] - Closing JDBC Connection [com.mysql.jdbc.JDBC4Connection@69a3d1d]
DEBUG [main] - Returned connection 110771485 to pool.
```



### 一级缓存应用

正式开发，是将mybatis和spring进行整合开发，事务控制在service中。

一个service方法中包括 很多mapper方法调用。

```
service{
	//开始执行时，开启事务，创建SqlSession对象
	//第一次调用mapper的方法findUserById(1)
	
	//第二次调用mapper的方法findUserById(1)，从一级缓存中取数据
	//方法结束，sqlSession关闭
}
```

如果是执行两次service调用查询相同的用户信息，不走一级缓存，因为session方法结束，sqlSession就关闭，一级缓存就清空。


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)

