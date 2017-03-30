# mybatis学习笔记(6)-输入映射

标签： mybatis

---

**Contents**

  - [传递pojo的包装对象](#传递pojo的包装对象)
    - [Error querying database.  Cause: org.apache.ibatis.reflection.ReflectionException: There is no getter for property named 'UserCustom' in 'class com.iot.mybatis.po.UserQueryVo'](#error-querying-database--cause-orgapacheibatisreflectionreflectionexception-there-is-no-getter-for-property-named-usercustom-in-class-comiotmybatispouserqueryvo)
    - [Cause: org.apache.ibatis.reflection.ReflectionException: There is no getter for property named 'UserCustom' in 'class com.iot.mybatis.po.UserQueryVo'](#cause-orgapacheibatisreflectionreflectionexception-there-is-no-getter-for-property-named-usercustom-in-class-comiotmybatispouserqueryvo)



---

本文主要讲解mybatis的输入映射。


通过parameterType指定输入参数的类型，类型可以是

- 简单类型
- hashmap
- pojo的包装类型

## 传递pojo的包装对象

- 定义包装类型pojo

```java
package com.iot.mybatis.po;

/**
 * Created by Brian on 2016/2/24.
 */
public class UserQueryVo {

    //在这里包装所需要的查询条件

    //用户查询条件
    private UserCustom userCustom;

    public UserCustom getUserCustom() {
        return userCustom;
    }

    public void setUserCustom(UserCustom userCustom) {
        this.userCustom = userCustom;
    }

    //可以包装其它的查询条件，订单、商品
    //....

}
```

其中，UserCustom类继承User

```java
public class UserCustom extends User{
}
```

- mapper.xml

在UserMapper.xml中定义用户信息综合查询（查询条件复杂，通过高级查询进行复杂关联查询）。

```xml
    <!-- 用户信息综合查询
        #{userCustom.sex}:取出pojo包装对象中性别值
        ${userCustom.username}：取出pojo包装对象中用户名称
     -->
    <select id="findUserList" parameterType="com.iot.mybatis.po.UserQueryVo"
            resultType="com.iot.mybatis.po.UserCustom">
        SELECT * FROM user WHERE user.sex=#{userCustom.sex} AND user.username LIKE '%${userCustom.username}%'
    </select>
```

注意不要将`#{userCustom.sex}`中的`userCustom`写成`UserCustom`,前者指属性名(由于使用IDE提示自动补全，所以只是把类型名首字母小写了)，后者指类型名，这里是`UserQueryVo`类中的`userCustom`属性，是**属性名**。写错会报如下异常：

```
org.apache.ibatis.exceptions.PersistenceException: 
### Error querying database.  Cause: org.apache.ibatis.reflection.ReflectionException: There is no getter for property named 'UserCustom' in 'class com.iot.mybatis.po.UserQueryVo'
### Cause: org.apache.ibatis.reflection.ReflectionException: There is no getter for property named 'UserCustom' in 'class com.iot.mybatis.po.UserQueryVo'
```

- mapper.java

```java
//用户信息综合查询
public List<UserCustom> findUserList(UserQueryVo userQueryVo) throws Exception;
```

- 测试代码

```java
//用户信息的综合 查询
	@Test
	public void testFindUserList() throws Exception {

		SqlSession sqlSession = sqlSessionFactory.openSession();

		//创建UserMapper对象，mybatis自动生成mapper代理对象
		UserMapper userMapper  sqlSession.getMapper(UserMapper.class);

		//创建包装对象，设置查询条件
		UserQueryVo userQueryVo = new UserQueryVo();
		UserCustom userCustom = new UserCustom();
		//由于这里使用动态sql，如果不设置某个值，条件不会拼接在sql中
		userCustom.setSex("1");
		userCustom.setUsername("张三");
		userQueryVo.setUserCustom(userCustom);
		//调用userMapper的方法

		List<UserCustom> list = userMapper.findUserList(userQueryVo);

		System.out.println(list);


	}
```



----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)


