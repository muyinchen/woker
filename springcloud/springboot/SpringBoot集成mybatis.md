# SpringBoot集成mybatis

## 一、使用[mybatis-spring-boot-starter](https://github.com/mybatis/mybatis-spring-boot/tree/master/mybatis-spring-boot-samples)

### 1、添加依赖

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2、启动时导入指定的sql(`application.properties`)

```properties
spring.datasource.schema=import.sql
```

### 3、annotation形式

```java
@SpringBootApplication
@MapperScan("sample.mybatis.mapper")
public class SampleMybatisApplication implements CommandLineRunner {

    @Autowired
    private CityMapper cityMapper;

    public static void main(String[] args) {
        SpringApplication.run(SampleMybatisApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(this.cityMapper.findByState("CA"));
    }

}
```

### 4、xml方式

#### mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <typeAliases>
        <package name="sample.mybatis.domain"/>
    </typeAliases>
    <mappers>
        <mapper resource="sample/mybatis/mapper/CityMapper.xml"/>
    </mappers>
</configuration>
```

#### application.properties

```properties
spring.datasource.schema=import.sql
mybatis.config=mybatis-config.xml
```

#### mapper

```java
@Component
public class CityMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public City selectCityById(long id) {
        return this.sqlSessionTemplate.selectOne("selectCityById", id);
    }

}
```

## 二、手工集成

### 1、annotation方式

```java
@Configuration
@MapperScan("com.xixicat.modules.dao")
@PropertySources({ @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true), @PropertySource(value = "file:./application.properties", ignoreResourceNotFound = true) })
public class MybatisConfig {
    
    @Value("${name:}")
    private String name;

    @Value("${database.driverClassName}")
    private String driverClass;

    @Value("${database.url}")
    private String jdbcUrl;

    @Value("${database.username}")
    private String dbUser;

    @Value("${database.password}")
    private String dbPwd;

    @Value("${pool.minPoolSize}")
    private int minPoolSize;

    @Value("${pool.maxPoolSize}")
    private int maxPoolSize;


    @Bean
    public Filter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }

    @Bean(destroyMethod = "close")
    public DataSource dataSource(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClass);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(dbUser);
        hikariConfig.setPassword(dbPwd);
        hikariConfig.setPoolName("springHikariCP");
        hikariConfig.setAutoCommit(false);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
          
        hikariConfig.setMinimumIdle(minPoolSize);
        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setConnectionInitSql("SELECT 1");
        
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        return dataSource;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setFailFast(true);
        sessionFactory.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        return sessionFactory.getObject();
    }
}
```

#### 点评

这种方式有点别扭，而且[配置不了拦截式事务拦截](http://stackoverflow.com/questions/14068525/javaconfig-replacing-aopadvisor-and-txadvice)，只能采用注解声明，有些冗余

### 2、xml方式

#### 数据源

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
    http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.0.xsd
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd
    http://www.springframework.org/schema/tx
    http://www.springframework.org/schema/tx/spring-tx-4.0.xsd">

    <context:property-placeholder ignore-unresolvable="true" />

    <bean id="hikariConfig" class="com.zaxxer.hikari.HikariConfig">
        <property name="poolName" value="springHikariCP" />
        <property name="connectionTestQuery" value="SELECT 1" />
        <property name="dataSourceClassName" value="${database.dataSourceClassName}" />
        <property name="maximumPoolSize" value="${pool.maxPoolSize}" />
        <property name="idleTimeout" value="${pool.idleTimeout}" />

        <property name="dataSourceProperties">
            <props>
                <prop key="url">${database.url}</prop>
                <prop key="user">${database.username}</prop>
                <prop key="password">${database.password}</prop>
            </props>
        </property>
    </bean>

    <!-- HikariCP configuration -->
    <bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <constructor-arg ref="hikariConfig" />
    </bean>

    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <!-- 配置mybatis配置文件的位置 -->
        <property name="configLocation" value="classpath:mybatis-config.xml"/>
        <property name="typeAliasesPackage" value="com.xixicat.domain"/>
        <!-- 配置扫描Mapper XML的位置 -->
        <property name="mapperLocations" value="classpath:com/xixicat/modules/dao/*.xml"/>


    </bean>

    <!-- 配置扫描Mapper接口的包路径 -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
        <property name="basePackage" value="com.xixicat.repository.mapper"/>
    </bean>

    <bean id="sqlSessionTemplate" class="org.mybatis.spring.SqlSessionTemplate">
        <constructor-arg ref="sqlSessionFactory"/>
    </bean>

    <bean id="transactionManager"
          class="org.springframework.jdbc.datasource.DataSourceTransactionManager"
          p:dataSource-ref="dataSource"/>
    <aop:aspectj-autoproxy expose-proxy="true" proxy-target-class="true" />

    <tx:advice id="txAdvice" transaction-manager="transactionManager" >
        <tx:attributes>
            <tx:method name="start*" propagation="REQUIRED"/>
            <tx:method name="submit*" propagation="REQUIRED"/>
            <tx:method name="clear*" propagation="REQUIRED"/>
            <tx:method name="create*" propagation="REQUIRED"/>
            <tx:method name="activate*" propagation="REQUIRED"/>
            <tx:method name="save*" propagation="REQUIRED"/>
            <tx:method name="insert*" propagation="REQUIRED"/>
            <tx:method name="add*" propagation="REQUIRED"/>
            <tx:method name="update*" propagation="REQUIRED"/>
            <tx:method name="delete*" propagation="REQUIRED"/>
            <tx:method name="remove*" propagation="REQUIRED"/>
            <tx:method name="execute*" propagation="REQUIRED"/>
            <tx:method name="del*" propagation="REQUIRED"/>
            <tx:method name="*" read-only="true"/>
        </tx:attributes>
    </tx:advice>
    <aop:config proxy-target-class="true" expose-proxy="true">
        <aop:pointcut id="pt" expression="execution(public * com.xixicat.service.*.*(..))" />
        <aop:advisor order="200" pointcut-ref="pt" advice-ref="txAdvice"/>
    </aop:config>
</beans>
```

#### aop依赖

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

#### mybatis-spring等依赖

```xml
<!-- boot dependency mybatis -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.3.0</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>1.2.2</version>
            <scope>compile</scope>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.6</version>
        </dependency>

        <!--<dependency>-->
            <!--<groupId>org.hsqldb</groupId>-->
            <!--<artifactId>hsqldb</artifactId>-->
            <!--<scope>runtime</scope>-->
        <!--</dependency>-->

        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP-java6</artifactId>
            <version>2.3.8</version>
        </dependency>
```

#### 指定xml配置文件

```java
@Configuration
@ComponentScan( basePackages = {"com.xixicat"} )
@ImportResource("classpath:applicationContext-mybatis.xml")
@EnableAutoConfiguration
public class AppMain {

    // 用于处理编码问题
    @Bean
    public Filter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }

    //文件下载
    @Bean
    public HttpMessageConverters restFileDownloadSupport() {
        ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        return new HttpMessageConverters(arrayHttpMessageConverter);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AppMain.class, args);
    }

}
```

#### 点评

跟传统的方式集成最为直接，而且事务配置也比较容易上手