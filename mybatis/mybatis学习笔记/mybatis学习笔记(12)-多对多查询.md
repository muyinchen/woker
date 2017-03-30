# mybatis学习笔记(12)-多对多查询

标签： mybatis

---

**Contents**

  - [示例](#示例)
  - [多对多查询总结](#多对多查询总结)
  - [resultMap总结](#resultmap总结)



---


本文实现多对多查询，查询用户及用户购买商品信息。

## 示例

查询主表是：用户表

关联表：由于用户和商品没有直接关联，通过订单和订单明细进行关联，所以关联表：orders、orderdetail、items


- sql

```sql
SELECT 
  orders.*,
  user.username,
  user.sex,
  user.address,
  orderdetail.id orderdetail_id,
  orderdetail.items_id,
  orderdetail.items_num,
  orderdetail.orders_id,
  items.name items_name,
  items.detail items_detail,
  items.price items_price
FROM
  orders,
  user,
  orderdetail,
  items
WHERE orders.user_id = user.id AND orderdetail.orders_id=orders.id AND orderdetail.items_id = items.id
```


- 映射思路

将用户信息映射到user中。

在user类中添加订单列表属性`List<Orders> orderslist`，将用户创建的订单映射到orderslist

在Orders中添加订单明细列表属性`List<OrderDetail>orderdetials`，将订单的明细映射到orderdetials

在OrderDetail中添加`Items`属性，将订单明细所对应的商品映射到Items


- mapper.xml

```xml
<!-- 查询用户及购买的商品信息，使用resultmap -->
<select id="findUserAndItemsResultMap" resultMap="UserAndItemsResultMap">
   SELECT
      orders.*,
      user.username,
      user.sex,
      user.address,
      orderdetail.id orderdetail_id,
      orderdetail.items_id,
      orderdetail.items_num,
      orderdetail.orders_id,
      items.name items_name,
      items.detail items_detail,
      items.price items_price
    FROM
      orders,
      user,
      orderdetail,
      items
    WHERE orders.user_id = user.id AND orderdetail.orders_id=orders.id AND orderdetail.items_id = items.id
</select>
```

- resultMap

```xml
<!-- 查询用户及购买的商品 -->
<resultMap type="com.iot.mybatis.po.User" id="UserAndItemsResultMap">
    <!-- 用户信息 -->
    <id column="user_id" property="id"/>
    <result column="username" property="username"/>
    <result column="sex" property="sex"/>
    <result column="address" property="address"/>

    <!-- 订单信息
    一个用户对应多个订单，使用collection映射
     -->
    <collection property="ordersList" ofType="com.iot.mybatis.po.Orders">
        <id column="id" property="id"/>
        <result column="user_id" property="userId"/>
        <result column="number" property="number"/>
        <result column="createtime" property="createtime"/>
        <result column="note" property="note"/>

        <!-- 订单明细
         一个订单包括 多个明细
         -->
        <collection property="orderdetails" ofType="com.iot.mybatis.po.Orderdetail">
            <id column="orderdetail_id" property="id"/>
            <result column="items_id" property="itemsId"/>
            <result column="items_num" property="itemsNum"/>
            <result column="orders_id" property="ordersId"/>

            <!-- 商品信息
             一个订单明细对应一个商品
             -->
            <association property="items" javaType="com.iot.mybatis.po.Items">
                <id column="items_id" property="id"/>
                <result column="items_name" property="name"/>
                <result column="items_detail" property="detail"/>
                <result column="items_price" property="price"/>
            </association>

        </collection>

    </collection>
</resultMap>
```

- mapper.java

```java
//查询用户购买商品信息
public List<User>  findUserAndItemsResultMap()throws Exception;
```

## 多对多查询总结

将查询用户购买的商品信息明细清单，（用户名、用户地址、购买商品名称、购买商品时间、购买商品数量）

针对上边的需求就使用resultType将查询到的记录映射到一个扩展的pojo中，很简单实现明细清单的功能。

一对多是多对多的特例，如下需求：

查询用户购买的商品信息，用户和商品的关系是多对多关系。

- 需求1：

查询字段：用户账号、用户名称、用户性别、商品名称、商品价格(最常见)

企业开发中常见明细列表，用户购买商品明细列表，

使用resultType将上边查询列映射到pojo输出。

- 需求2：

查询字段：用户账号、用户名称、购买商品数量、商品明细（鼠标移上显示明细）

使用resultMap将用户购买的商品明细列表映射到user对象中。

总结：

使用resultMap是针对那些对查询结果映射有特殊要求的功能，比如特殊要求映射成list中包括多个list。



## resultMap总结

- resultType
   - 作用：将查询结果按照sql列名pojo属性名一致性映射到pojo中。
   - 场合：常见一些明细记录的展示，比如用户购买商品明细，将关联查询信息全部展示在页面时，此时可直接使用resultType将每一条记录映射到pojo中，在前端页面遍历list（list中是pojo）即可。

- resultMap
	
使用association和collection完成一对一和一对多高级映射（对结果有特殊的映射要求）。

association：

- 作用：将关联查询信息映射到一个pojo对象中。
- 场合：为了方便查询关联信息可以使用association将关联订单信息映射为用户对象的pojo属性中，比如：查询订单及关联用户信息。

使用resultType无法将查询结果映射到pojo对象的pojo属性中，根据对结果集查询遍历的需要选择使用resultType还是resultMap。
	
collection：

- 作用：将关联查询信息映射到一个list集合中。
- 场合：为了方便查询遍历关联信息可以使用collection将关联信息映射到list集合中，比如：查询用户权限范围模块及模块下的菜单，可使用collection将模块映射到模块list中，将菜单列表映射到模块对象的菜单list属性中，这样的作的目的也是方便对查询结果集进行遍历查询。如果使用resultType无法将查询结果映射到list集合中。


----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)

