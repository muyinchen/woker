# springmvc学习笔记(8)-springmvc整合mybatis之service

标签： springmvc mybatis

---

**Contents**

  - [定义service接口](#定义service接口)
  - [在spring容器配置service](#在spring容器配置service)
  - [事务控制](#事务控制)



---


本文记录如何整合service,包括定义spring接口，在spring容器配置service以及事务控制。让spring管理service接口。


## 定义service接口

```java
public interface ItemsService {
    //商品查询列表
    List<ItemsCustom> findItemsList(ItemsQueryVo itemsQueryVo) throws Exception;

}
```

```java
public class ItemsServiceImpl implements ItemsService {

    @Autowired
    private ItemsMapperCustom itemsMapperCustom;

    public List<ItemsCustom> findItemsList(ItemsQueryVo itemsQueryVo) throws Exception {
        return itemsMapperCustom.findItemsList(itemsQueryVo);
    }
}
```

## 在spring容器配置service


在`resources/spring`下创建applicationContext-service.xml，文件中配置service。

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd">

    <!-- 商品管理的service -->
    <bean id="itemsService" class="com.iot.learnssm.firstssm.service.impl.ItemsServiceImpl"/>

</beans>
```

## 事务控制

在`resources/spring`下创建applicationContext-transaction.xml，在applicationContext-transaction.xml中使用spring声明式事务控制方法。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd
        http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">


<!-- 事务管理器
        对mybatis操作数据库事务控制，spring使用jdbc的事务控制类
    -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <!-- 数据源
        dataSource在applicationContext-dao.xml中配置了
         -->
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!-- 通知 -->
    <tx:advice id="txAdvice" transaction-manager="transactionManager">
        <tx:attributes>
            <!-- 传播行为 -->
            <tx:method name="save*" propagation="REQUIRED"/>
            <tx:method name="delete*" propagation="REQUIRED"/>
            <tx:method name="insert*" propagation="REQUIRED"/>
            <tx:method name="update*" propagation="REQUIRED"/>
            <tx:method name="find*" propagation="SUPPORTS" read-only="true"/>
            <tx:method name="get*" propagation="SUPPORTS" read-only="true"/>
            <tx:method name="select*" propagation="SUPPORTS" read-only="true"/>
        </tx:attributes>
    </tx:advice>
    <!-- aop -->
    <aop:config>
        <aop:advisor advice-ref="txAdvice" pointcut="execution(* com.iot.learnssm.firstssm.service.impl.*.*(..))"/>
    </aop:config>
</beans>
```



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) `|` [CSDN](http://blog.csdn.net/h3243212/) `|` [oschina](http://my.oschina.net/brianway)


