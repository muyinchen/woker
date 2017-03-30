# mybatis学习笔记(13)-延迟加载

标签： mybatis

---

**Contents**

  - [使用association实现延迟加载](#使用association实现延迟加载)
  - [延迟加载思考](#延迟加载思考)
  - [IDEA的debug小现象](#idea的debug小现象)



---


resultMap可以实现高级映射（使用`association`、`collection`实现一对一及一对多映射），`association`、`collection`具备延迟加载功能。

延迟加载：先从单表查询、需要时再从关联表去关联查询，大大提高数据库性能，因为查询单表要比关联查询多张表速度要快。


需求：

如果查询订单并且关联查询用户信息。如果先查询订单信息即可满足要求，当我们需要查询用户信息时再查询用户信息。把对用户信息的按需去查询就是延迟加载。

## 使用association实现延迟加载

- mapper.xml

需要定义两个mapper的方法对应的statement。

1.只查询订单信息

`SELECT * FROM orders`

在查询订单的statement中使用association去延迟加载（执行）下边的satatement(关联查询用户信息)

```xml
<!-- 查询订单关联查询用户，用户信息需要延迟加载 -->
<select id="findOrdersUserLazyLoading" resultMap="OrdersUserLazyLoadingResultMap">
    SELECT * FROM orders
</select>
```

2.关联查询用户信息

通过上边查询到的订单信息中user_id去关联查询用户信息,使用UserMapper.xml中的findUserById

```xml
<select id="findUserById" parameterType="int" resultType="com.iot.mybatis.po.User">
    SELECT * FROM  user  WHERE id=#{value}
</select>
```

上边先去执行findOrdersUserLazyLoading，当需要去查询用户的时候再去执行findUserById，通过resultMap的定义将延迟加载执行配置起来。


- 延迟加载resultMap

```xml
<!-- 延迟加载的resultMap -->
<resultMap type="com.iot.mybatis.po.Orders" id="OrdersUserLazyLoadingResultMap">
    <!--对订单信息进行映射配置  -->
    <id column="id" property="id"/>
    <result column="user_id" property="userId"/>
    <result column="number" property="number"/>
    <result column="createtime" property="createtime"/>
    <result column="note" property="note"/>
    <!-- 实现对用户信息进行延迟加载
    select：指定延迟加载需要执行的statement的id（是根据user_id查询用户信息的statement）
    要使用userMapper.xml中findUserById完成根据用户id(user_id)用户信息的查询，如果findUserById不在本mapper中需要前边加namespace
    column：订单信息中关联用户信息查询的列，是user_id
    关联查询的sql理解为：
    SELECT orders.*,
    (SELECT username FROM USER WHERE orders.user_id = user.id)username,
    (SELECT sex FROM USER WHERE orders.user_id = user.id)sex
     FROM orders
     -->
    <association property="user"  javaType="com.iot.mybatis.po.User"
                 select="com.iot.mybatis.mapper.UserMapper.findUserById"
                 column="user_id">
     <!-- 实现对用户信息进行延迟加载 -->

    </association>

</resultMap>
```

**与非延迟加载的主要区别就在`association`标签属性多了`select`和`column`**

```xml
<association property="user"  javaType="com.iot.mybatis.po.User"
             select="com.iot.mybatis.mapper.UserMapper.findUserById"
             column="user_id">
```

- mapper.java

```java
//查询订单关联查询用户，用户信息是延迟加载
public List<Orders> findOrdersUserLazyLoading()throws Exception;
```



- 测试思路
  - 执行上边mapper方法(`findOrdersUserLazyLoading`)，内部去调用`com.iot.mybatis.mapper.OrdersMapperCustom`中的`findOrdersUserLazyLoading`只查询orders信息（单表）。
   - 在程序中去遍历上一步骤查询出的List<Orders>，当我们调用Orders中的getUser方法时，开始进行延迟加载。
   - 延迟加载，去调用UserMapper.xml中findUserbyId这个方法获取用户信息。

- 延迟加载配置

mybatis默认没有开启延迟加载，需要在SqlMapConfig.xml中setting配置。

在mybatis核心配置文件中配置：lazyLoadingEnabled、aggressiveLazyLoading


| 设置项 | 描述 | 允许值| 默认值 |
| :--- |:----  |:--- |:---- |
|lazyLoadingEnabled|全局性设置懒加载。如果设为‘false’，则所有相关联的都会被初始化加载|true/false|false|
|aggressiveLazyLoading|	当设置为‘true’的时候，懒加载的对象可能被任何懒属性全部加载。否则，每个属性都按需加载。|true/false|true|


在SqlMapConfig.xml中配置：

```xml
<settings>
    <!-- 打开延迟加载 的开关 -->
    <setting name="lazyLoadingEnabled" value="true"/>
    <!-- 将积极加载改为消极加载即按需要加载 -->
    <setting name="aggressiveLazyLoading" value="false"/>
    <!-- 开启二级缓存 -->
   <!-- <setting name="cacheEnabled" value="true"/>-->
</settings>
```

- 测试代码

```java
// 查询订单关联查询用户，用户信息使用延迟加载
@Test
public void testFindOrdersUserLazyLoading() throws Exception {
	SqlSession sqlSession = sqlSessionFactory.openSession();// 创建代理对象
	OrdersMapperCustom ordersMapperCustom = sqlSession
			.getMapper(OrdersMapperCustom.class);
	// 查询订单信息（单表）
	List<Orders> list = ordersMapperCustom.findOrdersUserLazyLoading();

	// 遍历上边的订单列表
	for (Orders orders : list) {
		// 执行getUser()去查询用户信息，这里实现按需加载
		User user = orders.getUser();
		System.out.println(user);
	}
}
```


## 延迟加载思考

不使用mybatis提供的association及collection中的延迟加载功能，如何实现延迟加载？？

实现方法如下：

定义两个mapper方法：

- 查询订单列表
- 根据用户id查询用户信息

实现思路：

先去查询第一个mapper方法，获取订单信息列表；在程序中（service），按需去调用第二个mapper方法去查询用户信息。

总之，使用延迟加载方法，先去查询简单的sql（最好单表，也可以关联查询），再去按需要加载关联查询的其它信息。


## IDEA的debug小现象

使用的是intellij IDEA 15.0.2

先说一下结果吧，IDEA在debug和run条件下，打印结果不同

我为了验证延迟加载前的user是否为空，在Orders类中加入了

```java
public void print(){
    System.out.println("----test-print-----"+user+"   user==null: "+(user==null));
}
```

测试代码如下：

```java
// 查询订单关联查询用户，用户信息使用延迟加载
@Test
public void testFindOrdersUserLazyLoading() throws Exception {
	SqlSession sqlSession = sqlSessionFactory.openSession();// 创建代理对象
	OrdersMapperCustom ordersMapperCustom = sqlSession
			.getMapper(OrdersMapperCustom.class);
	// 查询订单信息（单表）
	List<Orders> list = ordersMapperCustom.findOrdersUserLazyLoading();

	// 遍历上边的订单列表
	for (Orders orders : list) {
		// 执行getUser()去查询用户信息，这里实现按需加载
		//User user = orders.getUser();
		//System.out.println(user);
		orders.print();
	}

}
```

然后分别run和debug

- run输出

```
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Created connection 110771485.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@69a3d1d]
DEBUG [main] - ==>  Preparing: SELECT * FROM orders 
DEBUG [main] - ==> Parameters: 
DEBUG [main] - <==      Total: 3
----test-print-----null   user==null: true
----test-print-----null   user==null: true
----test-print-----null   user==null: true
```

- debug输出

在`List<Orders> list = ordersMapperCustom.findOrdersUserLazyLoading();`打断点，运行完这句,日志输出为：

```
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Created connection 1219273867.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@48aca48b]
DEBUG [main] - ==>  Preparing: SELECT * FROM orders 
DEBUG [main] - ==> Parameters: 
DEBUG [main] - <==      Total: 3
```

但是当你点开`list`属性时，控制台又输出了，而且可以看到list里的user是有内容的

```
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 1(Integer)
DEBUG [main] - <==      Total: 1
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 10(Integer)
DEBUG [main] - <==      Total: 1
```

运行完所有程序，控制台输出为：

```
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Created connection 1219273867.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@48aca48b]
DEBUG [main] - ==>  Preparing: SELECT * FROM orders 
DEBUG [main] - ==> Parameters: 
DEBUG [main] - <==      Total: 3
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 1(Integer)
DEBUG [main] - <==      Total: 1
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE id=? 
DEBUG [main] - ==> Parameters: 10(Integer)
DEBUG [main] - <==      Total: 1
----test-print-----User [id=1, username=王五, sex=2, birthday=null, address=null]   user==null: false
----test-print-----User [id=1, username=王五, sex=2, birthday=null, address=null]   user==null: false
----test-print-----User [id=10, username=张三, sex=1, birthday=Thu Jul 10 00:00:00 CST 2014, address=北京市]   user==null: false
```


所以，我觉得应该是在debug时，查看属性的话，IDEA会自动调用get相应的方法，从而触发user的查询。延迟加载的源码实现以后我会阅读，把这个问题弄清楚。




----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)

