# mybatis学习笔记(10)-一对一查询

标签： mybatis

---

**Contents**

  - [resultType实现](#resulttype实现)
  - [resultMap实现](#resultmap实现)
  - [resultType和resultMap实现一对一查询小结](#resulttype和resultmap实现一对一查询小结)



---


本文使用两种方式(resultType和resultMap)实现一对一查询，查询订单信息，关联查询创建订单的用户信息


## resultType实现

- sql语句


确定查询的主表：订单表

确定查询的关联表：用户表

关联查询使用内连接？还是外连接？

由于orders表中有一个外键（user_id），通过外键关联查询用户表只能查询出一条记录，可以使用内连接。

```sql
SELECT 
  orders.*,
  USER.username,
  USER.sex,
  USER.address 
FROM
  orders,
  USER 
WHERE orders.user_id = user.id
```

- 创建pojo

将上边sql查询的结果映射到pojo中，pojo中必须包括所有查询列名。

原始的Orders.java不能映射全部字段，需要新创建的pojo。

创建一个pojo继承包括查询字段较多的po类。

对应数据表的几个pojo类(Items,Orderdetail,Orders)就是把该类的属性名设为和数据表列字段名相同，并为这些属性添加getter和setter，在这里就不贴代码了，只贴出对应于关联查询的自定义pojo类`OrdersCustom`的代码

```java
package com.iot.mybatis.po;

/**
 * 
 * <p>Title: OrdersCustom</p>
 * <p>Description: 订单的扩展类</p>
 */
//通过此类映射订单和用户查询的结果，让此类继承包括 字段较多的pojo类
public class OrdersCustom extends Orders{
	
	//添加用户属性
	/*USER.username,
	  USER.sex,
	  USER.address */
	
	private String username;
	private String sex;
	private String address;


	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}

}

```



- mapper.xml

```xml
 <!-- 查询订单关联查询用户信息 -->
<select id="findOrdersUser"  resultType="com.iot.mybatis.po.OrdersCustom">
  SELECT
      orders.*,
      user.username,
      user.sex,
      user.address
    FROM
      orders,
      user
    WHERE orders.user_id = user.id
</select>
```


- mapper.java

```java
//查询订单关联查询用户信息
public List<OrdersCustom> findOrdersUser()throws Exception;
}
```



## resultMap实现

使用resultMap将查询结果中的订单信息映射到Orders对象中，在orders类中添加User属性，将关联查询出来的用户信息映射到orders对象中的user属性中。

- 定义resultMap


```xml
<!-- 订单查询关联用户的resultMap
将整个查询的结果映射到com.iot.mybatis.po.Orders中
 -->
<resultMap type="com.iot.mybatis.po.Orders" id="OrdersUserResultMap">
    <!-- 配置映射的订单信息 -->
    <!-- id：指定查询列中的唯一标识，订单信息的中的唯 一标识，如果有多个列组成唯一标识，配置多个id
        column：订单信息的唯一标识列
        property：订单信息的唯一标识列所映射到Orders中哪个属性
      -->
    <id column="id" property="id"/>
    <result column="user_id" property="userId"/>
    <result column="number" property="number"/>
    <result column="createtime" property="createtime"/>
    <result column="note" property="note"/>

    <!-- 配置映射的关联的用户信息 -->
    <!-- association：用于映射关联查询单个对象的信息
    property：要将关联查询的用户信息映射到Orders中哪个属性
     -->
    <association property="user"  javaType="com.iot.mybatis.po.User">
        <!-- id：关联查询用户的唯 一标识
        column：指定唯 一标识用户信息的列
        javaType：映射到user的哪个属性
         -->
        <id column="user_id" property="id"/>
        <result column="username" property="username"/>
        <result column="sex" property="sex"/>
        <result column="address" property="address"/>
    </association>
</resultMap>

```


- statement定义


```xml
<!-- 查询订单关联查询用户信息 -->
<select id="findOrdersUserResultMap" resultMap="OrdersUserResultMap">
    SELECT
    orders.*,
    user.username,
    user.sex,
    user.address
    FROM
    orders,
    user
    WHERE orders.user_id = user.id
</select>
```

- mapper.java

```java
//查询订单关联查询用户使用resultMap
public List<Orders> findOrdersUserResultMap()throws Exception;
```

- 测试代码

```java
@Test
public void testFindOrdersUserResultMap() throws Exception {

	SqlSession sqlSession = sqlSessionFactory.openSession();
	// 创建代理对象
	OrdersMapperCustom ordersMapperCustom = sqlSession
			.getMapper(OrdersMapperCustom.class);

	// 调用maper的方法
	List<Orders> list = ordersMapperCustom.findOrdersUserResultMap();

	System.out.println(list);

	sqlSession.close();
}
```

## resultType和resultMap实现一对一查询小结

实现一对一查询：

- resultType：使用resultType实现较为简单，如果pojo中没有包括查询出来的列名，需要增加列名对应的属性，即可完成映射。如果没有查询结果的特殊要求建议使用resultType。
- resultMap：需要单独定义resultMap，实现有点麻烦，如果对查询结果有特殊的要求，使用resultMap可以完成将关联查询映射pojo的属性中。
- resultMap可以实现延迟加载，resultType无法实现延迟加载。



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)






