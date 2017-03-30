# mybatis的mapper返回map结果集

## 通过MapKey指定map的key值

```java
@MapKey("id")
Map<Long, UserInfo> getUserInfoMap();

@MapKey("id")
Map<Long, Map<String,Object>> getUserValueMap();
```

## map的value为java类

```xml
<resultMap id="UserResultMap" type="com.xixicat.domain.UserInfo">
        <result property="id" column="id" />
        <result property="username" column="username" />
        <result property="sex" column="sex" />
    </resultMap>
<select id="getUserInfoMap" resultMap="UserResultMap">
   select id,username,sex from user_info
</select>
```

## map的value为map

```xml
<select id="getUserValueMap" resultType="map" >
        select id,username,sex from user_info
        from user_info
</select>
```