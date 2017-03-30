# mybatis学习笔记(11)-一对多查询

标签： mybatis

---

**Contents**

  - [示例](#示例)
  - [小结](#小结)



---


本文实现一对多查询，查询订单及订单明细的信息

## 示例

- sql

确定主查询表：订单表
确定关联查询表：订单明细表
在一对一查询基础上添加订单明细表关联即可。

```sql
SELECT 
  orders.*,
  user.username,
  user.sex,
  user.address,
  orderdetail.id orderdetail_id,
  orderdetail.items_id,
  orderdetail.items_num,
  orderdetail.orders_id
FROM
  orders,
  user,
  orderdetail
WHERE orders.user_id = user.id AND orderdetail.orders_id=orders.id
```

**注意上面的`orderdetail.id (AS) orderdetail_id`,这里需要取别名，否则由于orders表也有id字段，在后面映射时会冲突**

- 映射思路

使用resultType将上边的查询结果映射到pojo中，订单信息的就是重复。

对orders映射不能出现重复记录。

在orders.java类中添加`List<orderDetail> orderDetails`属性。
最终会将订单信息映射到orders中，订单所对应的订单明细映射到orders中的orderDetails属性中。

映射成的orders记录数为两条（orders信息不重复）,每个orders中的orderDetails属性存储了该订单所对应的订单明细。


- 在orders中添加list订单明细属性

```java
//订单明细
private List<Orderdetail> orderdetails;
```

- mapper.xml

```xml
<!-- 查询订单关联查询用户及订单明细，使用resultmap -->
<select id="findOrdersAndOrderDetailResultMap" resultMap="OrdersAndOrderDetailResultMap">
   SELECT
      orders.*,
      user.username,
      user.sex,
      user.address,
      orderdetail.id orderdetail_id,
      orderdetail.items_id,
      orderdetail.items_num,
      orderdetail.orders_id
    FROM
      orders,
      user,
      orderdetail
    WHERE orders.user_id = user.id AND orderdetail.orders_id=orders.id
</select>
```

- resultMap定义

```xml
<!-- 订单及订单明细的resultMap
使用extends继承，不用在中配置订单信息和用户信息的映射
 -->
<resultMap type="com.iot.mybatis.po.Orders" id="OrdersAndOrderDetailResultMap" extends="OrdersUserResultMap">
    <!-- 订单信息 -->
    <!-- 用户信息 -->
    <!-- 使用extends继承，不用在中配置订单信息和用户信息的映射 -->


    <!-- 订单明细信息
    一个订单关联查询出了多条明细，要使用collection进行映射
    collection：对关联查询到多条记录映射到集合对象中
    property：将关联查询到多条记录映射到com.iot.mybatis.po.Orders哪个属性
    ofType：指定映射到list集合属性中pojo的类型
     -->
    <collection property="orderdetails" ofType="com.iot.mybatis.po.Orderdetail">
        <!-- id：订单明细唯 一标识
        property:要将订单明细的唯 一标识 映射到com.iot.mybatis.po.Orderdetail的哪个属性
          -->
        <id column="orderdetail_id" property="id"/>
        <result column="items_id" property="itemsId"/>
        <result column="items_num" property="itemsNum"/>
        <result column="orders_id" property="ordersId"/>
    </collection>

</resultMap>
```


- mapper.java

```java
//查询订单(关联用户)及订单明细
public List<Orders>  findOrdersAndOrderDetailResultMap()throws Exception;
```


## 小结

mybatis使用resultMap的collection对关联查询的多条记录映射到一个list集合属性中。

使用resultType实现：将订单明细映射到orders中的orderdetails中，需要自己处理，使用双重循环遍历，去掉重复记录，将订单明细放在orderdetails中。


另外，下面这篇文章对一对多的resultMap机制解释的很清楚：

> [MyBatis：一对多表关系详解(从案例中解析)](http://blog.csdn.net/xzm_rainbow/article/details/15336933)

----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)


