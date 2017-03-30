# mybatis学习笔记(4)-开发dao方法

标签： mybatis

---

**Contents**

  - [SqlSession使用范围](#sqlsession使用范围)
  - [原始dao开发方法](#原始dao开发方法)
    - [dao接口](#dao接口)
    - [dao接口实现类](#dao接口实现类)
    - [测试代码](#测试代码)
    - [总结原始dao开发问题](#总结原始dao开发问题)
  - [mapper代理方法](#mapper代理方法)
    - [开发规范](#开发规范)
    - [代码](#代码)
    - [一些问题总结](#一些问题总结)



---


本文讲解SqlSession，并对两种方法(原始dao开发和mapper代理开发)分别做简单展示


## SqlSession使用范围

- SqlSessionFactoryBuilder

通过`SqlSessionFactoryBuilder`创建会话工厂`SqlSessionFactory`将`SqlSessionFactoryBuilder`当成一个工具类使用即可，不需要使用单例管理`SqlSessionFactoryBuilder`。在需要创建`SqlSessionFactory`时候，只需要new一次`SqlSessionFactoryBuilder`即可。


- `SqlSessionFactory`

通过`SqlSessionFactory`创建`SqlSession`，使用单例模式管理`sqlSessionFactory`（工厂一旦创建，使用一个实例）。将来mybatis和spring整合后，使用单例模式管理`sqlSessionFactory`。


- `SqlSession`

`SqlSession`是一个面向用户（程序员）的接口。SqlSession中提供了很多操作数据库的方法：如：`selectOne`(返回单个对象)、`selectList`（返回单个或多个对象）。

`SqlSession`是线程不安全的，在`SqlSesion`实现类中除了有接口中的方法（操作数据库的方法）还有数据域属性。

`SqlSession`最佳应用场合在方法体内，定义成局部变量使用。


## 原始dao开发方法

程序员需要写dao接口和dao实现类

### dao接口

```java
public interface UserDao {
    //根据id查询用户信息
    public User findUserById(int id) throws Exception;

    //根据用户名列查询用户列表
    public List<User> findUserByName(String name) throws Exception;

    //添加用户信息
    public void insertUser(User user) throws Exception;

    //删除用户信息
    public void deleteUser(int id) throws Exception;
}
```

### dao接口实现类

```java
package com.iot.mybatis.dao;

import com.iot.mybatis.po.User;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * Created by Brian on 2016/2/24.
 */
public class UserDaoImpl implements UserDao{
    // 需要向dao实现类中注入SqlSessionFactory
    // 这里通过构造方法注入
    private SqlSessionFactory sqlSessionFactory;

    public UserDaoImpl(SqlSessionFactory sqlSessionFactory){
        this.sqlSessionFactory = sqlSessionFactory;
    }



    @Override
    public User findUserById(int id) throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        User user = sqlSession.selectOne("test.findUserById",id);
        //释放资源
        sqlSession.close();
        return user;
    }

    @Override
    public List<User> findUserByName(String name) throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        List<User> list = sqlSession.selectList("test.findUserByName", name);

        // 释放资源
        sqlSession.close();

        return list;
    }

    @Override
    public void insertUser(User user) throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        //执行插入操作
        sqlSession.insert("test.insertUser", user);

        // 提交事务
        sqlSession.commit();

        // 释放资源
        sqlSession.close();
    }

    @Override
    public void deleteUser(int id) throws Exception {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        //执行插入操作
        sqlSession.delete("test.deleteUser", id);

        // 提交事务
        sqlSession.commit();

        // 释放资源
        sqlSession.close();
    }
}
```

### 测试代码

```java
package com.iot.mybatis.dao;

import java.io.InputStream;

import com.iot.mybatis.po.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;



public class UserDaoImplTest {

	private SqlSessionFactory sqlSessionFactory;

	// 此方法是在执行testFindUserById之前执行
	@Before
	public void setUp() throws Exception {
		// 创建sqlSessionFactory

		// mybatis配置文件
		String resource = "SqlMapConfig.xml";
		// 得到配置文件流
		InputStream inputStream = Resources.getResourceAsStream(resource);

		// 创建会话工厂，传入mybatis的配置文件信息
		sqlSessionFactory = new SqlSessionFactoryBuilder()
				.build(inputStream);
	}

	@Test
	public void testFindUserById() throws Exception {
		// 创建UserDao的对象
		UserDao userDao = new UserDaoImpl(sqlSessionFactory);

		// 调用UserDao的方法
		User user = userDao.findUserById(1);
		
		System.out.println(user);
	}

}

```

### 总结原始dao开发问题

1.dao接口实现类方法中存在大量模板方法，设想能否将这些代码提取出来，大大减轻程序员的工作量。

2.调用sqlsession方法时将statement的id硬编码了

3.调用sqlsession方法时传入的变量，由于sqlsession方法使用泛型，即使变量类型传入错误，在编译阶段也不报错，不利于程序员开发。


## mapper代理方法

程序员只需要mapper接口（相当 于dao接口）

程序员还需要编写mapper.xml映射文件

程序员编写mapper接口需要遵循一些开发规范，mybatis可以自动生成mapper接口实现类代理对象。

### 开发规范

- 在mapper.xml中namespace等于mapper接口地址

```xml
<!--
 namespace 命名空间，作用就是对sql进行分类化管理,理解为sql隔离
 注意：使用mapper代理方法开发，namespace有特殊重要的作用,namespace等于mapper接口地址
 -->
<mapper namespace="com.iot.mybatis.mapper.UserMapper">
```

- mapper.java接口中的方法名和mapper.xml中statement的id一致

- mapper.java接口中的方法输入参数类型和mapper.xml中statement的parameterType指定的类型一致。

- mapper.java接口中的方法返回值类型和mapper.xml中statement的resultType指定的类型一致。

```xml
<select id="findUserById" parameterType="int" resultType="com.iot.mybatis.po.User">
    SELECT * FROM  user  WHERE id=#{value}
</select>
```

```java
//根据id查询用户信息
public User findUserById(int id) throws Exception;
```

总结：以上开发规范主要是对下边的代码进行统一生成：

```java
User user = sqlSession.selectOne("test.findUserById", id);
sqlSession.insert("test.insertUser", user);
```


### 代码

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
<mapper namespace="com.iot.mybatis.mapper.UserMapper">
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
    <select id="findUserById" parameterType="int" resultType="com.iot.mybatis.po.User">
        SELECT * FROM  user  WHERE id=#{value}
    </select>

    <!-- 根据用户名称模糊查询用户信息，可能返回多条
	resultType：指定就是单条记录所映射的java对象类型
	${}:表示拼接sql串，将接收到参数的内容不加任何修饰拼接在sql中。
	使用${}拼接sql，引起 sql注入
	${value}：接收输入参数的内容，如果传入类型是简单类型，${}中只能使用value
	 -->
    <select id="findUserByName" parameterType="java.lang.String" resultType="com.iot.mybatis.po.User">
        SELECT * FROM user WHERE username LIKE '%${value}%'
    </select>


    <!-- 添加用户
        parameterType：指定输入 参数类型是pojo（包括 用户信息）
        #{}中指定pojo的属性名，接收到pojo对象的属性值，mybatis通过OGNL获取对象的属性值
        -->
    <insert id="insertUser" parameterType="com.iot.mybatis.po.User">
        <!--
         将插入数据的主键返回，返回到user对象中

         SELECT LAST_INSERT_ID()：得到刚insert进去记录的主键值，只适用与自增主键

         keyProperty：将查询到主键值设置到parameterType指定的对象的哪个属性
         order：SELECT LAST_INSERT_ID()执行顺序，相对于insert语句来说它的执行顺序
         resultType：指定SELECT LAST_INSERT_ID()的结果类型
          -->
        <selectKey keyProperty="id" order="AFTER" resultType="java.lang.Integer">
          SELECT LAST_INSERT_ID()
        </selectKey>
        INSERT INTO user (username,birthday,sex,address)values (#{username},#{birthday},#{sex},#{address})
        <!--
            使用mysql的uuid（）生成主键
            执行过程：
            首先通过uuid()得到主键，将主键设置到user对象的id属性中
            其次在insert执行时，从user对象中取出id属性值
             -->
        <!--  <selectKey keyProperty="id" order="BEFORE" resultType="java.lang.String">
            SELECT uuid()
        </selectKey>
        insert into user(id,username,birthday,sex,address) value(#{id},#{username},#{birthday},#{sex},#{address}) -->

    </insert>

    <!-- 删除 用户
        根据id删除用户，需要输入 id值
         -->
    <delete id="deleteUser" parameterType="java.lang.Integer">
        delete from user where id=#{id}
    </delete>

    <!-- 根据id更新用户
    分析：
    需要传入用户的id
    需要传入用户的更新信息
    parameterType指定user对象，包括 id和更新信息，注意：id必须存在
    #{id}：从输入 user对象中获取id属性值
     -->
    <update id="updateUser" parameterType="com.iot.mybatis.po.User">
        update user set username=#{username},birthday=#{birthday},sex=#{sex},address=#{address}
        where id=#{id}
    </update>
    
</mapper>
```

- 在SqlMapConfig.xml中加载映射文件

```xml
<mappers>  
    <mapper resource="mapper/UserMapper.xml"/>  
</mappers>  
```

- UserMapper.java

```java
public interface UserMapper {
    //根据id查询用户信息
    public User findUserById(int id) throws Exception;

    //根据用户名列查询用户列表
    public List<User> findUserByName(String name) throws Exception;

    //添加用户信息
    public void insertUser(User user) throws Exception;

    //删除用户信息
    public void deleteUser(int id) throws Exception;

    //更新用户
    public void updateUser(User user)throws Exception;
}
```


- UserMapperTest/java

```java
public class UserMapperTest {  
  
  
    private SqlSessionFactory sqlSessionFactory;  
      
    //注解Before是在执行本类所有测试方法之前先调用这个方法  
    @Before  
    public void setup() throws Exception{  
        //创建SqlSessionFactory  
        String resource="SqlMapConfig.xml";  
          
        //将配置文件加载成流  
        InputStream inputStream = Resources.getResourceAsStream(resource);  
        //创建会话工厂，传入mybatis配置文件的信息  
        sqlSessionFactory=new SqlSessionFactoryBuilder().build(inputStream);  
    }  
      
    @Test  
    public void testFindUserById() throws Exception{  
          
        SqlSession sqlSession=sqlSessionFactory.openSession();  
          
        //创建UserMapper代理对象  
        UserMapper userMapper=sqlSession.getMapper(UserMapper.class);  
          
        //调用userMapper的方法  
        User user=userMapper.findUserById(1);  
          
        System.out.println(user.getUsername());  
    }  
}  
```


### 一些问题总结

- 代理对象内部调用`selectOne`或`selectList`
   - 如果mapper方法返回单个pojo对象（非集合对象），代理对象内部通过selectOne查询数据库。
   - 如果mapper方法返回集合对象，代理对象内部通过selectList查询数据库。


- mapper接口方法参数只能有一个是否影响系统开发

mapper接口方法参数只能有一个，系统是否不利于扩展维护?系统框架中，dao层的代码是被业务层公用的。即使mapper接口只有一个参数，可以使用包装类型的pojo满足不同的业务方法的需求。

注意：持久层方法的参数可以包装类型、map...等，service方法中建议不要使用包装类型（不利于业务层的可扩展）。



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)


