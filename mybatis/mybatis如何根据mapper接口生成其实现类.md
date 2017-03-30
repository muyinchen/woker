# mybatis如何根据mapper接口生成其实现类

## 序

mybatis里头给sqlSession指定执行哪条sql的时候，有两种方式，一种是写mapper的xml的namespace+statementId，如下：

```java
public Student findStudentById(Integer studId) {
        logger.debug("Select Student By ID :{}", studId);
        SqlSession sqlSession = MyBatisSqlSessionFactory.getSqlSession();
        try {
            return sqlSession.selectOne("com.mybatis3.StudentMapper.findStudentById", studId);
        } finally {
            sqlSession.close();
        }
    }
```

另外一种方法是指定mapper的接口：

```java
public Student findStudentById(Integer studId) {
        logger.debug("Select Student By ID :{}", studId);
        SqlSession sqlSession = MyBatisSqlSessionFactory.getSqlSession();
        try {
            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
            return studentMapper.findStudentById(studId);
        } finally {
            sqlSession.close();
        }
    }
```

一般的话，比较推荐第二种方法，因为手工写namespace和statementId极大增加了犯错误的概率，而且也降低了开发的效率。

## 问题

### mapper的实现类如何生成

如果使用mapper接口的方式，问题来了，这个是个接口，通过sqlSession对象get出来的一定是个实现类，问题是，我们并没有手工去写实现类，那么谁去干了这件事情呢？答案是mybatis通过JDK的动态代理方式，在启动加载配置文件时，根据配置mapper的xml去生成。

### mybatis-spring帮忙做了什么

自动open和close session

## 一、mapper代理类是如何生成的

#### 启动时加载解析mapper的xml

如果不是集成spring的，会去读取<mappers>节点，去加载mapper的xml配置

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <settings>
        <setting name="cacheEnabled" value="true"/>
        <setting name="lazyLoadingEnabled" value="true"/>
        <setting name="multipleResultSetsEnabled" value="true"/>
        <setting name="useColumnLabel" value="true"/>
        <setting name="useGeneratedKeys" value="false"/>
        <setting name="defaultExecutorType" value="SIMPLE"/>
        <setting name="defaultStatementTimeout" value="2"/>
    </settings>
    <typeAliases>
        <typeAlias alias="CommentInfo" type="com.xixicat.domain.CommentInfo"/>
    </typeAliases>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://localhost:3306/demo"/>
                <property name="username" value="root"/>
                <property name="password" value=""/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="com/xixicat/dao/CommentMapper.xml"/>
    </mappers>
</configuration>
```

如果是集成spring的，会去读spring的sqlSessionFactory的xml配置中的mapperLocations，然后去解析mapper的xml

```xml
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <!-- 配置mybatis配置文件的位置 -->
        <property name="configLocation" value="classpath:mybatis-config.xml"/>
        <property name="typeAliasesPackage" value="com.xixicat.domain"/>
        <!-- 配置扫描Mapper XML的位置 -->
        <property name="mapperLocations" value="classpath:com/xixicat/dao/*.xml"/>
    </bean>
```

#### 然后绑定namespace(`XMLMapperBuilder`)

```java
private void bindMapperForNamespace() {
    String namespace = builderAssistant.getCurrentNamespace();
    if (namespace != null) {
      Class<?> boundType = null;
      try {
        boundType = Resources.classForName(namespace);
      } catch (ClassNotFoundException e) {
        //ignore, bound type is not required
      }
      if (boundType != null) {
        if (!configuration.hasMapper(boundType)) {
          // Spring may not know the real resource name so we set a flag
          // to prevent loading again this resource from the mapper interface
          // look at MapperAnnotationBuilder#loadXmlResource
          configuration.addLoadedResource("namespace:" + namespace);
          configuration.addMapper(boundType);
        }
      }
    }
  }
```

这里先去判断该namespace能不能找到对应的class，若可以则调用

```java
configuration.addMapper(boundType);
```

configuration委托给MapperRegistry:

```java
public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
  }
```

#### 生成该mapper的代理工厂(`MapperRegistry`)

```java
public <T> void addMapper(Class<T> type) {
    if (type.isInterface()) {
      if (hasMapper(type)) {
        throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try {
        knownMappers.put(type, new MapperProxyFactory<T>(type));
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser. If the type is already known, it won't try.
        MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }
```

这里的重点就是MapperProxyFactory类：

```java
public class MapperProxyFactory<T> {

  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

  public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  public Class<T> getMapperInterface() {
    return mapperInterface;
  }

  public Map<Method, MapperMethod> getMethodCache() {
    return methodCache;
  }

  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }

}
```

#### getMapper的时候生成mapper代理类

```java
@SuppressWarnings("unchecked")
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
    if (mapperProxyFactory == null) {
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    }
    try {
      return mapperProxyFactory.newInstance(sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }
```

#### new出来MapperProxy

```java
  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }
@SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }
```

这里给代理类注入了sqlSession

#### MapperProxy实现InvocationHandler接口进行拦截代理

```java
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private final SqlSession sqlSession;
  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache;

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class.equals(method.getDeclaringClass())) {
      try {
        return method.invoke(this, args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }

  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
      mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }
}
```

这里的代理拦截，主要是寻找到MapperMethod，通过它去执行SQL。

#### MapperMethod委托给SqlSession去执行sql

```java
public Object execute(SqlSession sqlSession, Object[] args) {
    Object result;
    if (SqlCommandType.INSERT == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.insert(command.getName(), param));
    } else if (SqlCommandType.UPDATE == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.update(command.getName(), param));
    } else if (SqlCommandType.DELETE == command.getType()) {
      Object param = method.convertArgsToSqlCommandParam(args);
      result = rowCountResult(sqlSession.delete(command.getName(), param));
    } else if (SqlCommandType.SELECT == command.getType()) {
      if (method.returnsVoid() && method.hasResultHandler()) {
        executeWithResultHandler(sqlSession, args);
        result = null;
      } else if (method.returnsMany()) {
        result = executeForMany(sqlSession, args);
      } else if (method.returnsMap()) {
        result = executeForMap(sqlSession, args);
      } else {
        Object param = method.convertArgsToSqlCommandParam(args);
        result = sqlSession.selectOne(command.getName(), param);
      }
    } else if (SqlCommandType.FLUSH == command.getType()) {
        result = sqlSession.flushStatements();
    } else {
      throw new BindingException("Unknown execution method for: " + command.getName());
    }
    if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
      throw new BindingException("Mapper method '" + command.getName() 
          + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }
```

其实这里就回到了第一种模式，该模式是直接指定了statement的Id（这里是command.getName()），而通过mapper的接口方式，则多了这么步骤，最后通过MapperMethod，给sqlSession传入statement的id。

sqlSession其实自己也不执行sql，它只是mybatis对外公布的一个api入口，具体它委托给了executor去执行sql。

#### 什么时候去getMapper

- 手工get，比如

  ```java
  public void createStudent(Student student) {
          SqlSession sqlSession = MyBatisSqlSessionFactory.getSqlSession();
          try {
              StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
              studentMapper.insertStudent(student);
              sqlSession.commit();
          } finally {
              sqlSession.close();
          }
      }
  ```

- 集成spring的话
  在spring容器给指定的bean注入mapper的时候get出来(`见MapperFactoryBean的getObject方法`)

## 二、mybatis-spring帮忙做了什么

#### 通过MapperScannerConfigurer将mapper适配成spring bean

```xml
<!-- 配置扫描Mapper接口的包路径 -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
        <property name="basePackage" value="com.xixicat.dao"/>
    </bean>
```

这里使用 MapperFactoryBean将Mapper接口配置成 Spring bean 实体同时注入sqlSessionFactory。

#### MapperScannerConfigurer给每个mapper生成对应的MapperFactoryBean

```java
public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    if (this.processPropertyPlaceHolders) {
      processPropertyPlaceHolders();
    }

    ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
    scanner.setAddToConfig(this.addToConfig);
    scanner.setAnnotationClass(this.annotationClass);
    scanner.setMarkerInterface(this.markerInterface);
    scanner.setSqlSessionFactory(this.sqlSessionFactory);
    scanner.setSqlSessionTemplate(this.sqlSessionTemplate);
    scanner.setSqlSessionFactoryBeanName(this.sqlSessionFactoryBeanName);
    scanner.setSqlSessionTemplateBeanName(this.sqlSessionTemplateBeanName);
    scanner.setResourceLoader(this.applicationContext);
    scanner.setBeanNameGenerator(this.nameGenerator);
    scanner.registerFilters();
    scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
  }
```

#### 委托给ClassPathMapperScanner去scan

```java
public Set<BeanDefinitionHolder> doScan(String... basePackages) {
    Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

    if (beanDefinitions.isEmpty()) {
      logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
    } else {
      for (BeanDefinitionHolder holder : beanDefinitions) {
        GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();

        if (logger.isDebugEnabled()) {
          logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName() 
              + "' and '" + definition.getBeanClassName() + "' mapperInterface");
        }

        // the mapper interface is the original class of the bean
        // but, the actual class of the bean is MapperFactoryBean
        definition.getPropertyValues().add("mapperInterface", definition.getBeanClassName());
        definition.setBeanClass(MapperFactoryBean.class);

        definition.getPropertyValues().add("addToConfig", this.addToConfig);

        boolean explicitFactoryUsed = false;
        if (StringUtils.hasText(this.sqlSessionFactoryBeanName)) {
          definition.getPropertyValues().add("sqlSessionFactory", new RuntimeBeanReference(this.sqlSessionFactoryBeanName));
          explicitFactoryUsed = true;
        } else if (this.sqlSessionFactory != null) {
          definition.getPropertyValues().add("sqlSessionFactory", this.sqlSessionFactory);
          explicitFactoryUsed = true;
        }

        if (StringUtils.hasText(this.sqlSessionTemplateBeanName)) {
          if (explicitFactoryUsed) {
            logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
          }
          definition.getPropertyValues().add("sqlSessionTemplate", new RuntimeBeanReference(this.sqlSessionTemplateBeanName));
          explicitFactoryUsed = true;
        } else if (this.sqlSessionTemplate != null) {
          if (explicitFactoryUsed) {
            logger.warn("Cannot use both: sqlSessionTemplate and sqlSessionFactory together. sqlSessionFactory is ignored.");
          }
          definition.getPropertyValues().add("sqlSessionTemplate", this.sqlSessionTemplate);
          explicitFactoryUsed = true;
        }

        if (!explicitFactoryUsed) {
          if (logger.isDebugEnabled()) {
            logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
          }
          definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
      }
    }

    return beanDefinitions;
  }
```

这里出现了MapperFactoryBean的身影，然后判断配置文件是指定注入sqlSessionFactory，还是sqlTemplate(二者不能同时指定，这里是指定了sqlSessionFactory)。这里通过sqlSessionFactoryBeanName暂时先注入引用，因为此时还在给spring托管的bean进行create，不确定sqlSessionFactory是否已经被创建。

### 关于MapperFactoryBean

```java
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {

  private Class<T> mapperInterface;

  private boolean addToConfig = true;

  /**
   * Sets the mapper interface of the MyBatis mapper
   *
   * @param mapperInterface class of the interface
   */
  public void setMapperInterface(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  /**
   * If addToConfig is false the mapper will not be added to MyBatis. This means
   * it must have been included in mybatis-config.xml.
   * <p>
   * If it is true, the mapper will be added to MyBatis in the case it is not already
   * registered.
   * <p>
   * By default addToCofig is true.
   *
   * @param addToConfig
   */
  public void setAddToConfig(boolean addToConfig) {
    this.addToConfig = addToConfig;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void checkDaoConfig() {
    super.checkDaoConfig();

    notNull(this.mapperInterface, "Property 'mapperInterface' is required");

    Configuration configuration = getSqlSession().getConfiguration();
    if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
      try {
        configuration.addMapper(this.mapperInterface);
      } catch (Throwable t) {
        logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", t);
        throw new IllegalArgumentException(t);
      } finally {
        ErrorContext.instance().reset();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public T getObject() throws Exception {
    return getSqlSession().getMapper(this.mapperInterface);
  }

  /**
   * {@inheritDoc}
   */
  public Class<T> getObjectType() {
    return this.mapperInterface;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isSingleton() {
    return true;
  }

}
```

注意这里继承了SqlSessionDaoSupport，在spring把sqlSessionFactory创建出来后，会去把之前注入的引用改为真的实例，调用SqlSessionDaoSupport的setSqlSessionFactory方法。

```java
public abstract class SqlSessionDaoSupport extends DaoSupport {

  private SqlSession sqlSession;

  private boolean externalSqlSession;

  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    if (!this.externalSqlSession) {
      this.sqlSession = new SqlSessionTemplate(sqlSessionFactory);
    }
  }

  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSession = sqlSessionTemplate;
    this.externalSqlSession = true;
  }

  /**
   * Users should use this method to get a SqlSession to call its statement methods
   * This is SqlSession is managed by spring. Users should not commit/rollback/close it
   * because it will be automatically done.
   *
   * @return Spring managed thread safe SqlSession
   */
  public SqlSession getSqlSession() {
    return this.sqlSession;
  }

  /**
   * {@inheritDoc}
   */
  protected void checkDaoConfig() {
    notNull(this.sqlSession, "Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required");
  }

}
```

这里值得注意的是setSqlSessionFactory方法new了一个SqlSessionTemplate。

### SqlSessionTemplate

它的一个重要的构造器为

```java
public SqlSessionTemplate(SqlSessionFactory sqlSessionFactory, ExecutorType executorType,
      PersistenceExceptionTranslator exceptionTranslator) {

    notNull(sqlSessionFactory, "Property 'sqlSessionFactory' is required");
    notNull(executorType, "Property 'executorType' is required");

    this.sqlSessionFactory = sqlSessionFactory;
    this.executorType = executorType;
    this.exceptionTranslator = exceptionTranslator;
    this.sqlSessionProxy = (SqlSession) newProxyInstance(
        SqlSessionFactory.class.getClassLoader(),
        new Class[] { SqlSession.class },
        new SqlSessionInterceptor());
  }
```

mybatis-srping比传统mybatis方法多做的事情就在于此，生成了一个sqlSessionProxy。这里static import了java.lang.reflect.Proxy.newProxyInstance;也就是使用使用jdk代理进行了SqlSessionInterceptor拦截。

### SqlSessionInterceptor

```java
private class SqlSessionInterceptor implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      SqlSession sqlSession = getSqlSession(
          SqlSessionTemplate.this.sqlSessionFactory,
          SqlSessionTemplate.this.executorType,
          SqlSessionTemplate.this.exceptionTranslator);
      try {
        Object result = method.invoke(sqlSession, args);
        if (!isSqlSessionTransactional(sqlSession, SqlSessionTemplate.this.sqlSessionFactory)) {
          // force commit even on non-dirty sessions because some databases require
          // a commit/rollback before calling close()
          sqlSession.commit(true);
        }
        return result;
      } catch (Throwable t) {
        Throwable unwrapped = unwrapThrowable(t);
        if (SqlSessionTemplate.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
          // release the connection to avoid a deadlock if the translator is no loaded. See issue #22
          closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
          sqlSession = null;
          Throwable translated = SqlSessionTemplate.this.exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
          if (translated != null) {
            unwrapped = translated;
          }
        }
        throw unwrapped;
      } finally {
        if (sqlSession != null) {
          closeSqlSession(sqlSession, SqlSessionTemplate.this.sqlSessionFactory);
        }
      }
    }
  }
```

到了这里就明白了mybatis-spring帮忙做了session的open和close。

## 关于一级缓存

最后留个问题给大家，如果是使用mybatis-spring，那么mybatis的一级缓存默认开启的话，那么有达到一级缓存的设计初衷的效果么？