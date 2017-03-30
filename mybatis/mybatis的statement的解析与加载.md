# mybatis的statement的解析与加载

## 问题

mybatis的xml中的sql语句是启动时生成JDK代理类的时候就生成一次么

## 调用顺序链

- 解析xml配置

  ```java
  Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
          sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
  ```

- 调用SqlSessionFactoryBuilder的方法

  ```java
  public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
      try {
        XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
        return build(parser.parse());
      } catch (Exception e) {
        throw ExceptionFactory.wrapException("Error building SqlSession.", e);
      } finally {
        ErrorContext.instance().reset();
        try {
          reader.close();
        } catch (IOException e) {
          // Intentionally ignore. Prefer previous error.
        }
      }
    }
  ```

- 调用XMLConfigBuilder.parser

  ```java
  public Configuration parse() {
      if (parsed) {
        throw new BuilderException("Each XMLConfigBuilder can only be used once.");
      }
      parsed = true;
      parseConfiguration(parser.evalNode("/configuration"));
      return configuration;
    }
  ```

- sqlSessionFactory.getConfiguration().addMapper(BookMapper.class);触发MapperRegistry.addMapper

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

- 触发MapperAnnotationBuilder.parse方法

  ```java
  public void parse() {
      String resource = type.toString();
      if (!configuration.isResourceLoaded(resource)) {
        loadXmlResource();
        configuration.addLoadedResource(resource);
        assistant.setCurrentNamespace(type.getName());
        parseCache();
        parseCacheRef();
        Method[] methods = type.getMethods();
        for (Method method : methods) {
          try {
            // issue #237
            if (!method.isBridge()) {
              parseStatement(method);
            }
          } catch (IncompleteElementException e) {
            configuration.addIncompleteMethod(new MethodResolver(this, method));
          }
        }
      }
      parsePendingMethods();
    }
  ```

- 触发触发MapperAnnotationBuilder.loadXmlResource

  ```java
  private void loadXmlResource() {
      // Spring may not know the real resource name so we check a flag
      // to prevent loading again a resource twice
      // this flag is set at XMLMapperBuilder#bindMapperForNamespace
      if (!configuration.isResourceLoaded("namespace:" + type.getName())) {
        String xmlResource = type.getName().replace('.', '/') + ".xml";
        InputStream inputStream = null;
        try {
          inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource);
        } catch (IOException e) {
          // ignore, resource is not required
        }
        if (inputStream != null) {
          XMLMapperBuilder xmlParser = new XMLMapperBuilder(inputStream, assistant.getConfiguration(), xmlResource, configuration.getSqlFragments(), type.getName());
          xmlParser.parse();
        }
      }
    }
  ```

  - 触发XMLMapperBuilder.parse

    ```java
      public void parse() {
        if (!configuration.isResourceLoaded(resource)) {
          configurationElement(parser.evalNode("/mapper"));
          configuration.addLoadedResource(resource);
          bindMapperForNamespace();
        }
        parsePendingResultMaps();
        parsePendingChacheRefs();
        parsePendingStatements();
      }
    ```

- 触发XMLMapperBuilder.configurationElement

  ```java
  private void configurationElement(XNode context) {
      try {
        String namespace = context.getStringAttribute("namespace");
        if (namespace == null || namespace.equals("")) {
          throw new BuilderException("Mapper's namespace cannot be empty");
        }
        builderAssistant.setCurrentNamespace(namespace);
        cacheRefElement(context.evalNode("cache-ref"));
        cacheElement(context.evalNode("cache"));
        parameterMapElement(context.evalNodes("/mapper/parameterMap"));
        resultMapElements(context.evalNodes("/mapper/resultMap"));
        sqlElement(context.evalNodes("/mapper/sql"));
        buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
      } catch (Exception e) {
        throw new BuilderException("Error parsing Mapper XML. Cause: " + e, e);
      }
    }
  ```

- 触发XMLMapperBuilder.buildStatementFromContext

  ```java
  private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
      for (XNode context : list) {
        final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
        try {
          statementParser.parseStatementNode();
        } catch (IncompleteElementException e) {
          configuration.addIncompleteStatement(statementParser);
        }
      }
    }
  ```

- 触发XMLStatementBuilder.parseStatementNode

  ```java
  public void parseStatementNode() {
      String id = context.getStringAttribute("id");
      String databaseId = context.getStringAttribute("databaseId");
      if (!databaseIdMatchesCurrent(id, databaseId, this.requiredDatabaseId)) {
        return;
      }
      Integer fetchSize = context.getIntAttribute("fetchSize");
      Integer timeout = context.getIntAttribute("timeout");
      String parameterMap = context.getStringAttribute("parameterMap");
      String parameterType = context.getStringAttribute("parameterType");
      Class<?> parameterTypeClass = resolveClass(parameterType);
      String resultMap = context.getStringAttribute("resultMap");
      String resultType = context.getStringAttribute("resultType");
      String lang = context.getStringAttribute("lang");
      LanguageDriver langDriver = getLanguageDriver(lang);
      Class<?> resultTypeClass = resolveClass(resultType);
      String resultSetType = context.getStringAttribute("resultSetType");
      StatementType statementType = StatementType.valueOf(context.getStringAttribute("statementType", StatementType.PREPARED.toString()));
      ResultSetType resultSetTypeEnum = resolveResultSetType(resultSetType);
      String nodeName = context.getNode().getNodeName();
      SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
      boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
      boolean flushCache = context.getBooleanAttribute("flushCache", !isSelect);
      boolean useCache = context.getBooleanAttribute("useCache", isSelect);
      boolean resultOrdered = context.getBooleanAttribute("resultOrdered", false);
      // Include Fragments before parsing
      XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
      includeParser.applyIncludes(context.getNode());
      // Parse selectKey after includes and remove them.
      processSelectKeyNodes(id, parameterTypeClass, langDriver);
      // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
      SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
      String resultSets = context.getStringAttribute("resultSets");
      String keyProperty = context.getStringAttribute("keyProperty");
      String keyColumn = context.getStringAttribute("keyColumn");
      KeyGenerator keyGenerator;
      String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
      keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
      if (configuration.hasKeyGenerator(keyStatementId)) {
        keyGenerator = configuration.getKeyGenerator(keyStatementId);
      } else {
        keyGenerator = context.getBooleanAttribute("useGeneratedKeys",
            configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType))
            ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
      }
      builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
          fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
          resultSetTypeEnum, flushCache, useCache, resultOrdered, 
          keyGenerator, keyProperty, keyColumn, databaseId, langDriver, resultSets);
    }
  ```

- 触发MapperBuilderAssistant.addMappedStatement将解析好的statement放到configuration的map中

  ```java
  public void addMappedStatement(MappedStatement ms) {
      mappedStatements.put(ms.getId(), ms);
    }
  ```

## sql的组装

- MappedStatement.getBoundSql

  ```java
  public BoundSql getBoundSql(Object parameterObject) {
      BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
      List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
      if (parameterMappings == null || parameterMappings.isEmpty()) {
        boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
      }
      // check for nested result maps in parameter mappings (issue #30)
      for (ParameterMapping pm : boundSql.getParameterMappings()) {
        String rmId = pm.getResultMapId();
        if (rmId != null) {
          ResultMap rm = configuration.getResultMap(rmId);
          if (rm != null) {
            hasNestedResultMaps |= rm.hasNestedResultMaps();
          }
        }
      }
      return boundSql;
    }
  ```

  里头主要是把sql语句和参数组成BoundSql对象

- 调用BaseExecutor的query方法

  ```java
  @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
      BoundSql boundSql = ms.getBoundSql(parameter);
      CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
      return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
   }
  ```

- getBoundSql调用RawSqlSource调用StaticSqlSource的getBoundSql

  ```java
  public BoundSql getBoundSql(Object parameterObject) {
      return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
  ```

  这里的sql就是mapper里头写得select * from book where id = ? limit 1

- BaseExecutor.queryFromDatabase

  ```java
  private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
      List<E> list;
      localCache.putObject(key, EXECUTION_PLACEHOLDER);
      try {
        list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
      } finally {
        localCache.removeObject(key);
      }
      localCache.putObject(key, list);
      if (ms.getStatementType() == StatementType.CALLABLE) {
        localOutputParameterCache.putObject(key, parameter);
      }
      return list;
    }
  ```

- 调用SimpleExecutor.doQuery生成JDBC的statement

  ```java
  public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
      Statement stmt = null;
      try {
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
        stmt = prepareStatement(handler, ms.getStatementLog());
        return handler.<E>query(stmt, resultHandler);
      } finally {
        closeStatement(stmt);
      }
    }
  ```

- 调用StatementHandler来具体处理,这里是RoutingStatementHandler委托PreparedStatementHandler来处理sql的生成

  ```java
  public Statement prepare(Connection connection) throws SQLException {
      return delegate.prepare(connection);
    }
  ```

- 回调BaseStatementHandler的prepare方法

  ```java
  public Statement prepare(Connection connection) throws SQLException {
      ErrorContext.instance().sql(boundSql.getSql());
      Statement statement = null;
      try {
        statement = instantiateStatement(connection);
        setStatementTimeout(statement);
        setFetchSize(statement);
        return statement;
      } catch (SQLException e) {
        closeStatement(statement);
        throw e;
      } catch (Exception e) {
        closeStatement(statement);
        throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
      }
    }
  ```

- 在调用PreparedStatementHandler的instantiateStatement

  ```java
  protected Statement instantiateStatement(Connection connection) throws SQLException {
      String sql = boundSql.getSql();
      if (mappedStatement.getKeyGenerator() instanceof Jdbc3KeyGenerator) {
        String[] keyColumnNames = mappedStatement.getKeyColumns();
        if (keyColumnNames == null) {
          return connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        } else {
          return connection.prepareStatement(sql, keyColumnNames);
        }
      } else if (mappedStatement.getResultSetType() != null) {
        return connection.prepareStatement(sql, mappedStatement.getResultSetType().getValue(), ResultSet.CONCUR_READ_ONLY);
      } else {
        return connection.prepareStatement(sql);
      }
    }
  ```

- 组装statement之后，填充参数

  ```java
  public void parameterize(Statement statement) throws SQLException {
      parameterHandler.setParameters((PreparedStatement) statement);
    }
  ```

- 调用DefaultParameterHandler.setParameters方法，自此完成sql的拼装

  ```java
  public void setParameters(PreparedStatement ps) {
      ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());
      List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
      if (parameterMappings != null) {
        for (int i = 0; i < parameterMappings.size(); i++) {
          ParameterMapping parameterMapping = parameterMappings.get(i);
          if (parameterMapping.getMode() != ParameterMode.OUT) {
            Object value;
            String propertyName = parameterMapping.getProperty();
            if (boundSql.hasAdditionalParameter(propertyName)) { // issue #448 ask first for additional params
              value = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
              value = null;
            } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
              value = parameterObject;
            } else {
              MetaObject metaObject = configuration.newMetaObject(parameterObject);
              value = metaObject.getValue(propertyName);
            }
            TypeHandler typeHandler = parameterMapping.getTypeHandler();
            JdbcType jdbcType = parameterMapping.getJdbcType();
            if (value == null && jdbcType == null) {
              jdbcType = configuration.getJdbcTypeForNull();
            }
            try {
              typeHandler.setParameter(ps, i + 1, value, jdbcType);
            } catch (TypeException e) {
              throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
            } catch (SQLException e) {
              throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
            }
          }
        }
      }
    }
  ```