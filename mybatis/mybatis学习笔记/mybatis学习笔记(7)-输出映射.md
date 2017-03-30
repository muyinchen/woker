# mybatis学习笔记(7)-输出映射

标签： mybatis

---

**Contents**

  - [resultType](#resulttype)
    - [输出简单类型](#输出简单类型)
    - [输出pojo对象和pojo列表](#输出pojo对象和pojo列表)
  - [resultMap](#resultmap)
    - [resultMap使用方法](#resultmap使用方法)
    - [小结](#小结)



---

本文主要讲解mybatis的输出映射。


输出映射有两种方式

- `resultType`
- `resultMap`


## resultType

- 使用`resultType`进行输出映射，只有查询出来的列名和pojo中的属性名一致，该列才可以映射成功。
- 如果查询出来的列名和pojo中的属性名全部不一致，没有创建pojo对象。
- 只要查询出来的列名和pojo中的属性有一个一致，就会创建pojo对象。

### 输出简单类型


- mapper.xml

```xml
 <!-- 用户信息综合查询总数
        parameterType：指定输入类型和findUserList一样
        resultType：输出结果类型
    -->
    <select id="findUserCount" parameterType="com.iot.mybatis.po.UserQueryVo" resultType="int">
        SELECT count(*) FROM user WHERE user.sex=#{userCustom.sex} AND user.username LIKE '%${userCustom.username}%'
    </select>
```

- mapper.java

```java
    //用户信息综合查询总数
	@Test
	public void testFindUserCount() throws Exception {

		SqlSession sqlSession = sqlSessionFactory.openSession();

		//创建UserMapper对象，mybatis自动生成mapper代理对象
		UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

		//创建包装对象，设置查询条件
		UserQueryVo userQueryVo = new UserQueryVo();
		UserCustom userCustom = new UserCustom();
		//由于这里使用动态sql，如果不设置某个值，条件不会拼接在sql中
		userCustom.setSex("1");
		userCustom.setUsername("小");
		userQueryVo.setUserCustom(userCustom);
		//调用userMapper的方法

		int count = userMapper.findUserCount(userQueryVo);

		System.out.println(count);


	}
```


- 小结

查询出来的结果集只有一行且一列，可以使用简单类型进行输出映射。


###	输出pojo对象和pojo列表

**不管是输出的pojo单个对象还是一个列表（list中包括pojo），在mapper.xml中`resultType`指定的类型是一样的。**

在mapper.java指定的方法返回值类型不一样：

- 输出单个pojo对象，方法返回值是单个对象类型

```java
//根据id查询用户信息
public User findUserById(int id) throws Exception;
```

- 输出pojo对象list，方法返回值是List<Pojo>

```java
//根据用户名列查询用户列表
public List<User> findUserByName(String name) throws Exception;
```


**生成的动态代理对象中是根据mapper方法的返回值类型确定是调用`selectOne`(返回单个对象调用)还是`selectList` （返回集合对象调用 ）.**



## resultMap

mybatis中使用resultMap完成高级输出结果映射。(一对多，多对多)


###	resultMap使用方法 

如果查询出来的列名和pojo的属性名不一致，通过定义一个resultMap对列名和pojo属性名之间作一个映射关系。

1.定义resultMap

2.使用resultMap作为statement的输出映射类型

- 定义reusltMap

```xml
<!-- 定义resultMap
	将SELECT id id_,username username_ FROM USER 和User类中的属性作一个映射关系
	
	type：resultMap最终映射的java对象类型,可以使用别名
	id：对resultMap的唯一标识
	 -->
	 <resultMap type="user" id="userResultMap">
	 	<!-- id表示查询结果集中唯一标识 
	 	column：查询出来的列名
	 	property：type指定的pojo类型中的属性名
	 	最终resultMap对column和property作一个映射关系 （对应关系）
	 	-->
	 	<id column="id_" property="id"/>
	 	<!-- 
	 	result：对普通名映射定义
	 	column：查询出来的列名
	 	property：type指定的pojo类型中的属性名
	 	最终resultMap对column和property作一个映射关系 （对应关系）
	 	 -->
	 	<result column="username_" property="username"/>
	 
	 </resultMap>
```

- 使用resultMap作为statement的输出映射类型

```xml
<!-- 使用resultMap进行输出映射
        resultMap：指定定义的resultMap的id，如果这个resultMap在其它的mapper文件，前边需要加namespace
        -->
    <select id="findUserByIdResultMap" parameterType="int" resultMap="userResultMap">
        SELECT id id_,username username_ FROM USER WHERE id=#{value}
    </select>

```

- mapper.java

```java
//根据id查询用户信息，使用resultMap输出
public User findUserByIdResultMap(int id) throws Exception;
```

- 测试代码

```java
@Test
public void testFindUserByIdResultMap() throws Exception {

	SqlSession sqlSession = sqlSessionFactory.openSession();

	//创建UserMapper对象，mybatis自动生成mapper代理对象
	UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

	//调用userMapper的方法

	User user = userMapper.findUserByIdResultMap(1);

	System.out.println(user);


}
```


### 小结 

使用resultType进行输出映射，只有查询出来的列名和pojo中的属性名一致，该列才可以映射成功。

如果查询出来的列名和pojo的属性名不一致，通过定义一个resultMap对列名和pojo属性名之间作一个映射关系。




----

> 作者[@brianway](http://brianway.github.io/)更多文章：[个人网站](http://brianway.github.io/) | [CSDN](http://blog.csdn.net/h3243212/) | [oschina](http://my.oschina.net/brianway)

