# mybatis学习笔记(8)-动态sql

标签： mybatis

---

**Contents**

  - [if判断](#if判断)
  - [sql片段(重点)](#sql片段重点)
  - [foreach标签](#foreach标签)



---


mybatis核心,对sql语句进行灵活操作，通过表达式进行判断，对sql进行灵活拼接、组装。

## if判断

- mapper.xml

```xml
<!-- 用户信息综合查询
    #{userCustom.sex}:取出pojo包装对象中性别值
    ${userCustom.username}：取出pojo包装对象中用户名称
 -->
<select id="findUserList" parameterType="com.iot.mybatis.po.UserQueryVo"
        resultType="com.iot.mybatis.po.UserCustom">
    SELECT * FROM user
    <!--  where 可以自动去掉条件中的第一个and -->
    <where>
        <if test="userCustom!=null">
            <if test="userCustom.sex!=null and userCustom.sex != '' ">
               AND user.sex=#{userCustom.sex}
            </if>
            <if test="userCustom.username!=null and userCustom.username != '' ">
               AND user.username LIKE '%${userCustom.username}%'
            </if>
        </if>
    </where>


</select>

<!-- 用户信息综合查询总数
    parameterType：指定输入类型和findUserList一样
    resultType：输出结果类型
-->
<select id="findUserCount" parameterType="com.iot.mybatis.po.UserQueryVo" resultType="int">
    SELECT count(*) FROM user
    <where>
        <if test="userCustom!=null">
            <if test="userCustom.sex!=null and userCustom.sex != '' ">
                AND user.sex=#{userCustom.sex}
            </if>
            <if test="userCustom.username!=null and userCustom.username != '' ">
                AND user.username LIKE '%${userCustom.username}%'
            </if>
        </if>
    </where>
</select>

```

- 测试结果

1.注释掉`testFindUserList()`方法中的`userCustom.setUsername("张三");`


```java
//由于这里使用动态sql，如果不设置某个值，条件不会拼接在sql中
userCustom.setSex("1");
//userCustom.setUsername("张三");
userQueryVo.setUserCustom(userCustom);
```

输出

```
DEBUG [main] - Checking to see if class com.iot.mybatis.mapper.UserMapper matches criteria [is assignable to Object]
DEBUG [main] - Checking to see if class com.iot.mybatis.mapper.UserMapperTest matches criteria [is assignable to Object]
DEBUG [main] - Opening JDBC Connection
DEBUG [main] - Created connection 352359770.
DEBUG [main] - Setting autocommit to false on JDBC Connection [com.mysql.jdbc.JDBC4Connection@1500955a]
DEBUG [main] - ==>  Preparing: SELECT * FROM user WHERE user.sex=? 
DEBUG [main] - ==> Parameters: 1(String)
DEBUG [main] - <==      Total: 6
[User [id=10, username=张三, sex=1, birthday=Thu Jul 10 00:00:00 CST 2014, address=北京市], User [id=16, username=张小明, sex=1, birthday=null, address=河南郑州], User [id=22, username=陈小明, sex=1, birthday=null, address=河南郑州], User [id=24, username=张三丰, sex=1, birthday=null, address=河南郑州], User [id=25, username=陈小明, sex=1, birthday=null, address=河南郑州], User [id=28, username=王小军, sex=1, birthday=Tue Feb 23 00:00:00 CST 2016, address=河南郑州]]
```

可以看到sql语句为`reparing: SELECT * FROM user WHERE user.sex=? `，没有username的部分


2.`userQueryVo`设为null,则`userCustom`为null

```java
//List<UserCustom> list = userMapper.findUserList(userQueryVo);
List<UserCustom> list = userMapper.findUserList(null);
```

输出

```
DEBUG [main] - ==>  Preparing: SELECT * FROM user 
DEBUG [main] - ==> Parameters: 
DEBUG [main] - <==      Total: 9
[User [id=1, username=王五, sex=2, birthday=null, address=null], User [id=10, username=张三, sex=1, birthday=Thu Jul 10 00:00:00 CST 2014, address=北京市], User [id=16, username=张小明, sex=1, birthday=null, address=河南郑州], User [id=22, username=陈小明, sex=1, birthday=null, address=河南郑州], User [id=24, username=张三丰, sex=1, birthday=null, address=河南郑州], User [id=25, username=陈小明, sex=1, birthday=null, address=河南郑州], User [id=26, username=王五, sex=null, birthday=null, address=null], User [id=27, username=王大军, sex=2, birthday=Tue Feb 23 00:00:00 CST 2016, address=河南郑州], User [id=28, username=王小军, sex=1, birthday=Tue Feb 23 00:00:00 CST 2016, address=河南郑州]]

```

可以看到sql语句变为了`SELECT * FROM user`


## sql片段(重点)

将上边实现的动态sql判断代码块抽取出来，组成一个sql片段。其它的statement中就可以引用sql片段。


- 定义sql片段

```xml
<!-- 定义sql片段
id：sql片段的唯 一标识

经验：是基于单表来定义sql片段，这样话这个sql片段可重用性才高
在sql片段中不要包括 where
 -->
<sql id="query_user_where">
    <if test="userCustom!=null">
        <if test="userCustom.sex!=null and userCustom.sex!=''">
            AND user.sex = #{userCustom.sex}
        </if>
        <if test="userCustom.username!=null and userCustom.username!=''">
            AND user.username LIKE '%${userCustom.username}%'
        </if>
    </if>
</sql>
```

- 引用sql片段

```xml
<!-- 用户信息综合查询
    #{userCustom.sex}:取出pojo包装对象中性别值
    ${userCustom.username}：取出pojo包装对象中用户名称
 -->
<select id="findUserList" parameterType="com.iot.mybatis.po.UserQueryVo"
        resultType="com.iot.mybatis.po.UserCustom">
    SELECT * FROM user
    <!--  where 可以自动去掉条件中的第一个and -->
    <where>
        <!-- 引用sql片段 的id，如果refid指定的id不在本mapper文件中，需要前边加namespace -->
        <include refid="query_user_where"></include>
        <!-- 在这里还要引用其它的sql片段  -->
    </where>
</select>
```

## foreach标签

向sql传递数组或List，mybatis使用foreach解析

在用户查询列表和查询总数的statement中增加多个id输入查询。两种方法，sql语句如下：

- `SELECT * FROM USER WHERE id=1 OR id=10 OR id=16`
- `SELECT * FROM USER WHERE id IN(1,10,16)`

一个使用OR,一个使用IN


- 在输入参数类型中添加`List<Integer> ids`传入多个id

```java
public class UserQueryVo {

    //传入多个id
    private List<Integer> ids;
    
    getter、setter方法
    。。。
}
```

- 修改mapper.xml

```xml
<if test="ids!=null">
    <!-- 使用 foreach遍历传入ids
    collection：指定输入 对象中集合属性
    item：每个遍历生成对象中
    open：开始遍历时拼接的串
    close：结束遍历时拼接的串
    separator：遍历的两个对象中需要拼接的串
     -->
    <!-- 使用实现下边的sql拼接：
     AND (id=1 OR id=10 OR id=16)
     -->
    <foreach collection="ids" item="user_id" open="AND (" close=")" separator="or">
        <!-- 每个遍历需要拼接的串 -->
        id=#{user_id}
    </foreach>
    
    <!-- 实现  “ and id IN(1,10,16)”拼接 -->
    <!-- <foreach collection="ids" item="user_id" open="and id IN(" close=")" separator=",">
        每个遍历需要拼接的串
        #{user_id}
    </foreach> -->

</if>
```


- 测试代码

在`testFindUserList`中加入

```java
//传入多个id
List<Integer> ids = new ArrayList<Integer>();
ids.add(1);
ids.add(10);
ids.add(16);
//将ids通过userQueryVo传入statement中
userQueryVo.setIds(ids);
```




----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)

