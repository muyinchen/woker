# hsweb-easy-orm 开发实践

#### 1 sql模板对象接口,提供需要执行sql的模板以及参数等信息

`public interface SQL`
获取sql语句模板:`String getSql();`
获取预编译参数:`Object getParams();`
获取关联查询的sql:` List<BindSQL> getBinds();`

`public class BindSQL`:
包含字段:
```java
	private SQL sql;

    private String toField;
```
#### 2 sql拼接

`public class SqlAppender extends LinkedList<String>`

```java
	/**
     * 接入sql语句，并自动加入空格
     *
     * @param str
     * @return
     */
    public SqlAppender addSpc(Object... str) {
        for (Object s : str) {
            this.add(s);
            this.add(" ");
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String str : this) {
            builder.append(str);
        }
        return builder.toString();
    }
```
#### 3 设置对象包装器

作用:在执行查询时，通过包装器对查询结果进行初始化
创建对象实例:
`T newInstance();`
向实例中填充一个属性值:
`void wrapper(T instance/*实例对象*/, int index/*当前实例的索引*/, String attr/*属性名称*/, Object value/*属性值*/);`
当一个实例被填充完成后调用，已进行其他操作:
`void done(T instance);`

#### 4 设置SQL执行器

```java
public interface SqlExecutor {

    /**
     * 传入SQL对象和对象包装器执行查询,将查询结果通过对象包装器进行包装后返回
     *
     * @param sql     sql对象
     * @param wrapper 执行结果对象包装器
     * @param <T>     查询结果类型泛型
     * @return 查询结果
     * @throws Exception 执行查询异常
     */
    <T> List<T> list(SQL sql, ObjectWrapper<T> wrapper) throws SQLException;

    /**
     * 传入SQL对象和对象包装器执行查询,将查询结果通过对象包装器进行包装后返回
     * 只返回单个结果,如果sql结果存在多个值,则返回首个值
     *
     * @param sql     sql对象
     * @param wrapper 对象包装其
     * @param <T>     查询结果类型泛型
     * @return 查询结果
     * @throws Exception 执行查询异常
     */
    <T> T single(SQL sql, ObjectWrapper<T> wrapper) throws SQLException;

    /**
     * 执行sql
     *
     * @param sql sql对象
     * @throws Exception 执行异常
     */
    void exec(SQL sql) throws SQLException;

    /**
     * 执行update
     *
     * @param sql sql对象
     * @return 执行sql后影响的行数
     * @throws Exception 执行异常
     */
    int update(SQL sql) throws SQLException;

    /**
     * 执行delete
     *
     * @param sql sql对象
     * @return 执行sql后影响的行数
     * @throws Exception 执行异常
     */
    int delete(SQL sql) throws SQLException;

    /**
     * 执行insert
     *
     * @param sql sql对象
     * @return 执行sql后影响的行数
     * @throws Exception 执行异常
     */
    int insert(SQL sql) throws SQLException;

}
```
#### 5 设置查询条件

`public class QueryParam`
将查询参数里的一些通用部分抽取出来整理成`SqlParam`
`public class SqlParam<R extends SqlParam>`
通用参数里包含sql语句常见的，`条件`,`指定要处理的字段`,`指定不处理的字段`
然后就是对一些具体条件的封装，如`or`,`and`,`nest`,`orNest`,`includes`,`excludes`,`where`
对`执行条件`进行POJO化封装(只展示部分代码):
对条件类型进行enum封装:`public enum TermType`

```java
public class Term {

    /**
     * 字段
     */
    private String field;

    /**
     * 值
     */
    private Object value;

    /**
     * 链接类型
     */
    private Type type = Type.and;

    /**
     * 条件类型
     */
    private TermType termType = TermType.eq;

    /**
     * 嵌套的条件
     */
    private List<Term> terms = new LinkedList<>();
```
最后，查询参数这里还剩下关于分页排序的东西
关于排序的封装:`public class Sort`

#### 6 设置修改条件

```java
public class UpdateParam<T> extends SqlParam<UpdateParam<T>> {
    private T data;

    public UpdateParam() {
    }

    public UpdateParam(T data) {
        this.data = data;
    }

    public UpdateParam<T> set(T data) {
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> UpdateParam<T> build(T data) {
        return new UpdateParam<>(data);
    }

    public static <T> UpdateParam<T> build(T data, String condition, Object value) {
        return new UpdateParam<>(data).where(condition, value);
    }
}
```
#### 7 设置插入条件(其实就是(update)

```java
public class InsertParam<T> {
    private T data;

    public InsertParam() {
    }

    public InsertParam(T data) {
        this.data = data;
    }

    public InsertParam<T> value(T data) {
        this.data = data;
        return this;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> UpdateParam<T> build(T data) {
        return new UpdateParam<>(data);
    }
}
```
#### 8 对更新或者插入时data是个map的类型时的封装:

```java
public class UpdateMapParam extends UpdateParam<Map<String, Object>> {
    public UpdateMapParam() {
        this(new HashMap<>());
    }

    public UpdateMapParam(Map<String, Object> data) {
        setData(data);
    }

    public UpdateMapParam set(String key, Object value) {
        this.getData().put(key, value);
        return this;
    }
}
```



#### 9 表结构定义实体

根据数据库中表来设计一个表结构的`POJO`:包括 表名称，表别名，备注，主键，表字段，数据库定义的实体类，关联关系，触发器

```java
public class TableMetaData implements Serializable {
    private boolean locked = false;
    //表名称
    private String name;
    //表别名,如果指定了别名,查询结果将使用别名进行封装
    private String alias;
    //备注
    private String comment;
    //主键
    private Set<String> primaryKeys = new HashSet<>();
    //表字段
    private Map<String, FieldMetaData> fieldMetaDataMap = new LinkedHashMap<>();
    private Map<String, FieldMetaData> aliasFieldMetaDataMap = new LinkedHashMap<>();
    //数据库定义实体
    private DatabaseMetaData databaseMetaData;
    private Validator validator;
    private Set<Correlation> correlations = new LinkedHashSet<>();
    private Map<String, Object> properties = new HashMap<>();
    private Map<String, Trigger> triggerBase = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        checkWrite();
        this.name = name;
    }

    public void on(String triggerName, Trigger trigger) {
        checkWrite();
        triggerBase.put(triggerName, trigger);
    }

    public void on(String triggerName, Map<String, Object> context) throws TriggerException {
        if (triggerIsSupport(triggerName)) {
            Trigger trigger = triggerBase.get(triggerName);
            trigger.execute(context);
        }
    }

    public boolean triggerIsSupport(String name) {
        return triggerBase.containsKey(name);
    }

    public PropertyWrapper getProperty(String name) {
        return new SimplePropertyWrapper(properties.get(name));
    }

    public PropertyWrapper getProperty(String name, Object defaultValue) {
        return new SimplePropertyWrapper(properties.getOrDefault(name, defaultValue));
    }

    public <T> T setProperty(String property, T value) {
        properties.put(property, value);
        return value;
    }

    public String getAlias() {
        if (alias == null) alias = name;
        return alias;
    }

    public FieldMetaData findFieldByName(String name) {
        if (name == null) return null;
        if (name.contains(".")) {
            String[] tmp = name.split("[.]");
            TableMetaData metaData = databaseMetaData.getTable(tmp[0]);
            if (metaData == null) {
                Correlation correlation = getCorrelation(tmp[0]);
                if (correlation != null) {
                    metaData = databaseMetaData.getTable(correlation.getTargetTable());
                }
            }
            if (metaData != null) return metaData.findFieldByName(tmp[1]);
            return null;
        }
        FieldMetaData metaData = fieldMetaDataMap.get(name);
        if (metaData == null)
            metaData = aliasFieldMetaDataMap.get(name);
        return metaData;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<FieldMetaData> getFields() {
        return new LinkedHashSet<>(fieldMetaDataMap.values());
    }


    public Correlation getCorrelation(String target) {
        for (Correlation correlation : correlations) {
            if (correlation.getAlias().equals(target))
                return correlation;
        }
        for (Correlation correlation : correlations) {
            if (correlation.getTargetTable().equals(target))
                return correlation;
        }
        return null;
    }

    public DatabaseMetaData getDatabaseMetaData() {
        return databaseMetaData;
    }

    public void setDatabaseMetaData(DatabaseMetaData databaseMetaData) {
        checkWrite();
        this.databaseMetaData = databaseMetaData;
    }

    public Correlation addCorrelation(Correlation correlation) {
        checkWrite();
        correlations.add(correlation);
        return correlation;
    }

    public Set<Correlation> getCorrelations() {
        return correlations;
    }

    public void setCorrelations(Set<Correlation> correlations) {
        this.correlations = correlations;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, FieldMetaData> getAliasFieldMetaDataMap() {
        return aliasFieldMetaDataMap;
    }

    public void setAliasFieldMetaDataMap(Map<String, FieldMetaData> aliasFieldMetaDataMap) {
        checkWrite();
        this.aliasFieldMetaDataMap = aliasFieldMetaDataMap;
    }

    public Map<String, FieldMetaData> getFieldMetaDataMap() {
        return fieldMetaDataMap;
    }

    public void setFieldMetaDataMap(Map<String, FieldMetaData> fieldMetaDataMap) {
        checkWrite();
        this.fieldMetaDataMap = fieldMetaDataMap;
    }

    public Set<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(Set<String> primaryKeys) {
        checkWrite();
        this.primaryKeys = primaryKeys;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        checkWrite();
        this.validator = validator;
    }

    public TableMetaData addField(FieldMetaData fieldMetaData) {
        fieldMetaData.setTableMetaData(this);
        fieldMetaDataMap.put(fieldMetaData.getName(), fieldMetaData);
        if (!fieldMetaData.getName().equals(fieldMetaData.getAlias()))
            aliasFieldMetaDataMap.put(fieldMetaData.getAlias(), fieldMetaData);
        return this;
    }

    public void lock() {
        locked = true;
    }

    public void unloc() {
        locked = false;
    }

    public void checkWrite() {
        if (locked) throw new UnsupportedOperationException("表定义已锁定,禁止修改操作");
    }

}
```

#### 10 表中字段元素的定义

```java
public class FieldMetaData implements Serializable {
    private static final DefaultValueConverter DEFAULT_VALUE_CONVERTER = new DefaultValueConverter();
    private String name;

    private String alias;

    private String comment;

    private String dataType;

    private JDBCType jdbcType;

    private Class javaType;

    private TableMetaData tableMetaData;

    private OptionConverter optionalMapper;

    private ValueConverter valueConverter = DEFAULT_VALUE_CONVERTER;

    private Set<String> validator;

    private Map<String, Object> properties = new HashMap<>();
```

#### 11 定义下数据库的方言接口

`public interface Dialect`

```java
 	//引号开始位置
	String getQuoteStart();

    String getQuoteEnd();

    String wrapperWhere(String wherePrefix, Term term, FieldMetaData fieldMetaData, String tableAlias);

    String doPaging(String sql, int pageIndex, int pageSize);
```

#### 12 定义SQL的操作类型

```java
public enum SQLType {
    SELECT, DELETE, UPDATE, INSERT, CREATE_TABLE, ALTER_TABLE, DROP_TABLE
}
```

#### 13  where条件语句的创建的简单实现

通过`SqlAppender`,因为是链表的实现，所以将

判断执行条件是否为空，为空直接返回

不为空进行的逻辑请看代码注释

一些代码逻辑请看图:

![](http://og0sybnix.bkt.clouddn.com/sp170509_200938.png)

```java
public abstract class SimpleWhereSqlBuilder {

    protected String getTableAlias(TableMetaData metaData, String field) {
        if (field.contains("."))
            field = field.split("[.]")[0];
        else return metaData.getAlias();
        Correlation correlation = metaData.getCorrelation(field);
        if (correlation != null) return correlation.getAlias();
        return metaData.getAlias();
    }

    public void buildWhere(TableMetaData metaData, String prefix,
                           List<Term> terms, SqlAppender appender,
                           Set<String> needSelectTable) {
        if (terms == null || terms.isEmpty()) return;
        int index = -1;
        String prefixTmp = StringUtils.concat(prefix, StringUtils.isNullOrEmpty(prefix) ? "" : ".");
        for (Term term : terms) {
            index++;
            boolean nullTerm = StringUtils.isNullOrEmpty(term.getField());
            FieldMetaData field = metaData.findFieldByName(term.getField());
            //不是空条件 也不是可选字段
            if (!nullTerm && field == null && term.getTermType() != TermType.func) continue;
            //不是空条件，值为空
            if (!nullTerm && StringUtils.isNullOrEmpty(term.getValue())) continue;
            //是空条件，但是无嵌套
            if (nullTerm && term.getTerms().isEmpty()) continue;
            String tableAlias = null;
            if (field != null) {
                tableAlias = getTableAlias(metaData, term.getField());
                needSelectTable.add(tableAlias);
                //转换参数的值
                term.setValue(transformationValue(field.getJdbcType(), term.getValue(), term.getTermType()));
            }
            //用于sql预编译的参数名
            prefix = StringUtils.concat(prefixTmp, "terms[", index, "]");
            //添加类型，and 或者 or
            appender.add(StringUtils.concat(" ", term.getType().toString().toUpperCase(), " "));
            if (!term.getTerms().isEmpty()) {
                //构建嵌套的条件
                SqlAppender nest = new SqlAppender();
                buildWhere(metaData, prefix, term.getTerms(), nest, needSelectTable);
                //如果嵌套结果为空,
                if (nest.isEmpty()) {
                    appender.removeLast();//删除最后一个（and 或者 or）
                    continue;
                }
                if (nullTerm) {
                    //删除 第一个（and 或者 or）
                    nest.removeFirst();
                }
                appender.add("(");
                if (!nullTerm)
                    appender.add(getDialect().wrapperWhere(prefix, term, field, tableAlias));
                appender.addAll(nest);
                appender.add(")");
            } else {
                if (!nullTerm)
                    appender.add(getDialect().wrapperWhere(prefix, term, field, tableAlias));
            }
        }
    }

    protected Object transformationValue(JDBCType type, Object value, TermType termType) {
        if (type == null) return value;
        switch (type) {
            case INTEGER:
            case NUMERIC:
                if (StringUtils.isInt(type)) return StringUtils.toInt(value);
                if (StringUtils.isDouble(type)) return StringUtils.toDouble(value);
                break;
            case TIMESTAMP:
            case TIME:
            case DATE:
                if (!(value instanceof Date)) {
                    String strValue = String.valueOf(value);
                    Date date = DateTimeUtils.formatUnknownString2Date(strValue);
                    if (date != null) return date;
                }
                break;
        }
        return value;
    }

    public abstract Dialect getDialect();
}
```

#### 14 拼接查询总数语句的简单实现

`SimpleSelectTotalSqlRender` 难点逻辑已在13步解释过,亮点是通过内部类来完成逻辑

```java
public class SimpleSelectTotalSqlRender extends CommonSqlRender<QueryParam> {

    private Dialect dialect;

    public SimpleSelectTotalSqlRender(Dialect dialect) {
        this.dialect = dialect;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    class SimpleSelectSqlRenderProcess extends SimpleWhereSqlBuilder {
        private TableMetaData metaData;
        private QueryParam param;
        private SqlAppender whereSql = new SqlAppender();
        private Set<String> needSelectTable = new LinkedHashSet<>();

        public SimpleSelectSqlRenderProcess(TableMetaData metaData, QueryParam param) {
            this.metaData = metaData;
            this.param = param;
            //解析要查询的字段
            //解析查询条件
            buildWhere(metaData, "", param.getTerms(), whereSql, needSelectTable);
            if (!whereSql.isEmpty()) whereSql.removeFirst();
        }

        public SQL process() {
            SqlAppender appender = new SqlAppender();
            appender.add("SELECT count(0) as ", dialect.getQuoteStart(), "total", dialect.getQuoteEnd());
            appender.add(" FROM ", metaData.getName(), " ", metaData.getAlias());
            //生成join
            needSelectTable.forEach(table -> {
                if (table.equals(metaData.getName())) return;
                Correlation correlation = metaData.getCorrelation(table);
                if (correlation != null) {
                    appender.add(" ", correlation.getJoin(), " "
                            , correlation.getTargetTable(), " ", correlation.getAlias()
                            , " ON ");
                    SqlAppender joinOn = new SqlAppender();
                    buildWhere(metaData.getDatabaseMetaData().getTable(correlation.getTargetTable()),
                            "", correlation.getTerms(), joinOn, new HashSet());
                    if (!joinOn.isEmpty()) joinOn.removeFirst();
                    appender.addAll(joinOn);
                }
            });
            if (!whereSql.isEmpty())
                appender.add(" WHERE ", "").addAll(whereSql);
            String sql = appender.toString();
            SimpleSQL simpleSQL = new SimpleSQL(metaData, sql, param);
            return simpleSQL;
        }

        @Override
        public Dialect getDialect() {
            return dialect;
        }
    }

    @Override
    public SQL render(TableMetaData metaData, QueryParam param) {
        return new SimpleSelectSqlRenderProcess(metaData, param).process();
    }
}
```

#### 15 拼接查询语句的简单实现

`SimpleSelectSqlRender` 代码基本和上面的没太多出入，可以参考源码实现

#### 16 拼接Insert语句的简单实现

```java
public class SimpleInsertSqlRender implements SqlRender<InsertParam> {

    @Override
    public SQL render(TableMetaData metaData, InsertParam param) {
        SqlAppender appender = new SqlAppender();
        appender.add("INSERT INTO ", metaData.getName(), " ");
        appender.add("(");
        Object data = param.getData();
        if (data == null) throw new NullPointerException("不能插入为null的数据!");
        metaData.getFields().forEach(fieldMetaData -> {
            appender.add(fieldMetaData.getName(), ",");
        });
        appender.removeLast();
        appender.add(")values");
        List<Object> list = new ArrayList<>();
        if (data instanceof Collection) {
            list.addAll(((Collection) data));
        } else {
            list.add(data);
        }
        param.setData(list);
        Object o = list.get(0);
        for (int i = 0; i < list.size(); i++) {
            int index = i;
            if (index > 0) appender.add(",");
            appender.add("(");
            metaData.getFields().forEach(fieldMetaData -> {
                String dataProperty = fieldMetaData.getAlias();
                Object value = null;
                try {
                    if (!fieldMetaData.getAlias().equals(fieldMetaData.getName())) {
                        value = BeanUtils.getProperty(o, fieldMetaData.getAlias());
                        if (value == null) {
                            value = BeanUtils.getProperty(o, fieldMetaData.getName());
                            if (value != null) dataProperty = fieldMetaData.getName();
                        }
                    }
                } catch (Exception ignored) {
                }
                appender.add("#{data[", index, "].", dataProperty, "}", ",");
            });
            appender.removeLast();
            appender.add(")");
        }
        InsertParam paramProxy = new InsertParam();
        paramProxy.setData(list);
        return new SimpleSQL(metaData, appender.toString(), paramProxy);
    }
}
```

#### 16 添加update接口

```java
public interface Update<T> {
    Update<T> set(T data);

    Update<T> set(String property,Object value);

    Update<T> where(String condition, Object value);

    Update<T> setParam(UpdateParam<T> param);

    int exec() throws SQLException;
}
```

#### 17 添加Query接口

```java
public interface Query<T> {
    Query setParam(QueryParam param);

    Query select(String... fields);

    Query selectExcludes(String... fields);

    Query where(String condition, Object value);

    Query and(String condition, Object value);

    Query or(String condition, Object value);

    Term nest();

    Term nest(String condition, Object value);

    Term orNest(String condition, Object value);

    Query orderByAsc(String field);

    Query orderByDesc(String field);

    Query noPaging();

    List<T> list() throws SQLException;

    List<T> list(int pageIndex, int pageSize) throws SQLException;

    T single() throws SQLException;

    int total() throws SQLException;
}
```

#### 18 添加Delete接口

```java
public interface Delete {
    Delete where(String condition, Object value);

    Delete setParam(SqlParam param);

    int exec() throws SQLException;
}
```

#### 19 Table接口

```java
public interface Table<T> {
    TableMetaData getMeta();

    Query<T> createQuery();

    Update<T> createUpdate();

    Insert<T> createInsert();

    Delete createDelete();
}

```

#### 20 数据库操作接口

```java
public interface Database {

    /**
     * 获取数据库定义对象
     *
     * @return 数据库定义对象
     */
    DatabaseMetaData getMeta();

    /**
     * 获取一个表操作接口,如果数据库定义对象里未找到表结构定义,则尝试使用{@link TableMetaParser#parse(String)}进行解析
     *
     * @param name 表名
     * @param <T>  表数据泛型
     * @return 表操作接口
     */
    <T> Table<T> getTable(String name);

    /**
     * 创建表,在数据库中创建表,如果表已存在,将不进行任何操作
     *
     * @param tableMetaData 表结构定义
     * @param <T>           表数据泛型
     * @return 表操作接口
     * @throws SQLException 创建异常信息
     */
    <T> Table<T> createTable(TableMetaData tableMetaData) throws SQLException;

    /**
     * 重新载入结构定义,此操作不会对数据库表结构进行任何操作
     *
     * @param tableMetaData 表结构定义
     * @param <T>           表数据泛型
     * @return 表操作接口
     */
    <T> Table<T> reloadTable(TableMetaData tableMetaData);

    <T> Table<T> alterTable(TableMetaData tableMetaData);

    /**
     * 删除表,此操作只会删除结构定义,不会删除物理数据库中的表
     *
     * @param name 表名
     * @return
     */
    boolean removeTable(String name);

}
```

#### 21 SimpleDatabase简单实现

```java
public class SimpleDatabase implements Database {
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private DatabaseMetaData metaData;
    private SqlExecutor sqlExecutor;
    private TableMetaParser tableMetaParser;

    public SimpleDatabase(DatabaseMetaData metaData, SqlExecutor sqlExecutor) {
        this.metaData = metaData;
        this.sqlExecutor = sqlExecutor;
    }

    protected Map<String, Table> cache = new HashMap<>();

    @Override
    public DatabaseMetaData getMeta() {
        return metaData;
    }

    @Override
    public <T> Table<T> getTable(String name) {
        Table table;
        TableMetaData tableMetaData = metaData.getTable(name);
        if (tableMetaData == null) {
            if (tableMetaParser != null)
                tableMetaData = tableMetaParser.parse(name);
            if (tableMetaData != null) {
                metaData.putTable(tableMetaData);
            } else
                throw new NullPointerException("表不存在!");
        }
        try {
            readWriteLock.readLock().lock();
            table = cache.get(name);
            if (null != table) return table;
        } finally {
            readWriteLock.readLock().unlock();
        }
        if (table == null) {
            try {
                readWriteLock.writeLock().lock();
                ObjectWrapper wrapper;
                ObjectWrapperFactory factory = metaData.getObjectWrapperFactory();
                if (factory != null) {
                    wrapper = factory.createObjectWrapper(tableMetaData);
                } else {
                    wrapper = new AdvancedMapWrapper(tableMetaData);
                }
                table = new SimpleTable(tableMetaData, this, sqlExecutor, wrapper);
                cache.put(name, table);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
        return table;
    }

    @Override
    public <T> Table<T> createTable(TableMetaData tableMetaData) throws SQLException {
        SimpleQuery.TotalWrapper wrapper = new SimpleQuery.TotalWrapper();
        try {
            sqlExecutor.single(new SimpleSQL(tableMetaData, "select count(0) from " + tableMetaData.getName() + " where 1=2", new Object()), wrapper);
        } catch (Exception e) {
            SqlRender render = metaData.getRenderer(SqlRender.TYPE.META_CREATE);
            SQL sql = render.render(tableMetaData, new Object());
            sqlExecutor.exec(sql);
            metaData.putTable(tableMetaData);
        }
        return getTable(tableMetaData.getName());
    }

    @Override
    public <T> Table<T> reloadTable(TableMetaData tableMetaData) {
        try {
            readWriteLock.writeLock().lock();
            cache.remove(tableMetaData.getName());
            cache.remove(tableMetaData.getAlias());
            metaData.putTable(tableMetaData);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return getTable(tableMetaData.getAlias());
    }

    @Override
    public <T> Table<T> alterTable(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("开发进行中");
    }

    @Override
    public boolean removeTable(String name) {
        try {
            readWriteLock.writeLock().lock();
            return metaData.remove(name) != null;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void setTableMetaParser(TableMetaParser tableMetaParser) {
        this.tableMetaParser = tableMetaParser;
    }

    public Map<String, Object> getTriggerContextRoot() {
        return new HashMap<>();
    }
}
```

#### 22 SimpleTable简单实现

```java
class SimpleTable<T> implements Table<T> {
    private TableMetaData metaData;

    private SqlExecutor sqlExecutor;

    private ObjectWrapper objectWrapper;

    private SimpleDatabase database;

    public SimpleTable(TableMetaData metaData, SimpleDatabase database, SqlExecutor sqlExecutor, ObjectWrapper objectWrapper) {
        this.metaData = metaData;
        this.sqlExecutor = sqlExecutor;
        this.objectWrapper = objectWrapper;
        this.database = database;
    }

    @Override
    public TableMetaData getMeta() {
        return metaData;
    }

    @Override
    public Query createQuery() {
        return new SimpleQuery<>(this, sqlExecutor, objectWrapper);
    }

    @Override
    public Update createUpdate() {
        return new SimpleUpdate<>(this, sqlExecutor);
    }

    @Override
    public Delete createDelete() {
        return new SimpleDelete(this, sqlExecutor);
    }

    @Override
    public Insert<T> createInsert() {
        return new SimpleInsert<>(this, sqlExecutor);
    }

    public SimpleDatabase getDatabase() {
        return database;
    }
}
```

#### 23 SimpleMapWrapper对ObjectWrapper<Map<String, Object>>的简单实现

```java
public class SimpleMapWrapper implements ObjectWrapper<Map<String, Object>> {
    @Override
    public Map<String, Object> newInstance() {
        return new LinkedHashMap<>();
    }

    @Override
    public void wrapper(Map<String, Object> instance, int index, String attr, Object value) {
        if ("ROWNUM_".equals(attr.toUpperCase())) return;
        putValue(instance, attr, value);
    }

    @Override
    public void done(Map<String, Object> instance) {

    }

    public void putValue(Map<String, Object> instance, String attr, Object value) {
        if (attr.contains(".")) {
            String[] attrs = StringUtils.splitFirst(attr, "[.]");
            String attr_ob_name = attrs[0];
            String attr_ob_attr = attrs[1];
            Object object = instance.get(attr_ob_name);
            if (object == null) {
                object = newInstance();
                instance.put(attr_ob_name, object);
            }
            if (object instanceof Map) {
                Map<String, Object> objectMap = (Map) object;
                putValue(objectMap, attr_ob_attr, value);
            }
        } else {
            instance.put(attr, value);
        }
    }
}
```





#### 24  PropertyWrapper接口

```java
public interface PropertyWrapper extends Serializable {
    <T> T getValue();

    String toString();

    int toInt();

    double toDouble();

    boolean isTrue();

    Date toDate();

    Date toDate(String format);

    Map<String, Object> toMap();

    List<Map> toList();

    <T> T toBean(Class<T> type);

    <T> List<T> toBeanList(Class<T> type);

    boolean isNullOrEmpty();

    boolean valueTypeOf(Class<?> type);
}

```

#### 25 SimplePropertyWrapper简单实现

```java
public class SimplePropertyWrapper implements PropertyWrapper {

    private Object value;

    public SimplePropertyWrapper(Object value) {
        this.value = value;
    }

    @Override
    public <T> T getValue() {
        return (T) value;
    }

    @Override
    public int toInt() {
        return StringUtils.toInt(value);
    }

    @Override
    public double toDouble() {
        return StringUtils.toDouble(value);
    }

    @Override
    public boolean isTrue() {
        return StringUtils.isTrue(value);
    }

    @Override
    public Date toDate() {
        if (value instanceof Date) return ((Date) value);
        return DateTimeUtils.formatUnknownString2Date(toString());
    }

    @Override
    public Date toDate(String format) {
        if (value instanceof Date) return ((Date) value);
        return DateTimeUtils.formatDateString(toString(), format);
    }

    @Override
    public <T> T toBean(Class<T> type) {
        if (valueTypeOf(type)) return ((T) getValue());
        return JSON.parseObject(toString(), type);
    }

    @Override
    public List<Map> toList() {
        return toBeanList(Map.class);
    }

    @Override
    public Map<String, Object> toMap() {
        return toBean(Map.class);
    }

    @Override
    public <T> List<T> toBeanList(Class<T> type) {
        if (getValue() instanceof List) return ((List) getValue());
        return JSON.parseArray(toString(), type);
    }

    @Override
    public boolean isNullOrEmpty() {
        return StringUtils.isNullOrEmpty(value);
    }

    @Override
    public boolean valueTypeOf(Class<?> type) {
        if (value == null) return false;
        return ClassUtils.instanceOf(value.getClass(), type);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
```

#### 26 Converter接口

```java
public interface Converter {
    Object getValue(Object value);
}

```

#### 27 ValueConverter接口

```java
public interface ValueConverter {
    Object getData(Object value);

    Object getValue(Object data);
}
```

#### 28 DefaultValueConverter默认实现

```java
public class DefaultValueConverter implements ValueConverter {
    @Override
    public Object getData(Object value) {
        return value;
    }

    @Override
    public Object getValue(Object data) {
        return data;
    }
}
```

#### 29 AdvancedMapWrapper对简单实现的增强

```java
public class AdvancedMapWrapper extends SimpleMapWrapper {
    private TableMetaData tableMetaData;

    public AdvancedMapWrapper(TableMetaData tableMetaData) {
        this.tableMetaData = tableMetaData;
    }

    @Override
    public void wrapper(Map<String, Object> instance, int index, String attr, Object value) {
        FieldMetaData metaData = tableMetaData.findFieldByName(attr);
        if (null != metaData) {
            ValueConverter valueConverter = metaData.getValueConverter();
            super.wrapper(instance, index, attr, valueConverter.getValue(value));
            ValueConverter converter = metaData.getValueConverter();
            value = converter.getValue(value);
            OptionConverter optionConverter = metaData.getOptionalMapper();
            if (optionConverter != null) {
                Object value1 = optionConverter.converterValue(value);
                putValue(instance, optionConverter.getFieldName(), value1);
            }
        } else {
            super.wrapper(instance, index, attr, value);
        }
    }
}
```
#### 30 验证器接口Validator

```java
public interface Validator {
    boolean validate(Object data) throws ValidationException;
}
```

#### 31 验证器工厂接口ValidatorFactory

```java
public interface ValidatorFactory {
    Validator createValidator(TableMetaData tableMetaData);
}
```

#### 32 关联关系定义类

```java
public class Correlation {

    public Correlation() {
    }

    public Correlation(String target, String alias, String condition) {
        this.targetTable = target;
        this.alias = alias;
        terms = new ArrayList<>();
        Term term = new Term();
        term.setTermType(TermType.func);
        term.setField(condition);
        term.setValue(condition);
        terms.add(term);
    }

    private String targetTable;

    private String alias;

    private boolean one2one = true;

    private List<Term> terms;

    private JOIN join = JOIN.LEFT;

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getAlias() {
        if (alias == null) alias = targetTable;
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isOne2one() {
        return one2one;
    }

    public void setOne2one(boolean one2one) {
        this.one2one = one2one;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public JOIN getJoin() {
        return join;
    }

    public void setJoin(JOIN join) {
        this.join = join;
    }

    public Correlation leftJoin() {
        this.join = JOIN.LEFT;
        return this;
    }

    public Correlation rightJoin() {
        this.join = JOIN.RIGHT;
        return this;
    }

    public Correlation InnerJoin() {
        this.join = JOIN.INNER;
        return this;
    }

    public Correlation FullJoin() {
        this.join = JOIN.FULL;
        return this;
    }

    public enum JOIN {
        LEFT {
            @Override
            public String toString() {
                return "LEFT JOIN";
            }
        }, RIGHT {
            @Override
            public String toString() {
                return "RIGHT JOIN";
            }
        }, FULL {
            @Override
            public String toString() {
                return "FULL JOIN";
            }
        }, INNER {
            @Override
            public String toString() {
                return "JOIN";
            }
        }
    }

}
```

