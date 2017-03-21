# SpringBoot配置属性之Migration

## 序

SpringBoot支持了两种数据库迁移工具，一个是flyway，一个是liquibase。其本身也支持sql script，在初始化数据源之后执行指定的脚本。

## flyway

- flyway.baseline-description对执行迁移时基准版本的描述.
- flyway.baseline-on-migrate当迁移时发现目标schema非空，而且带有没有元数据的表时，是否自动执行基准迁移，默认false.
- flyway.baseline-version开始执行基准迁移时对现有的schema的版本打标签，默认值为1.
- flyway.check-location检查迁移脚本的位置是否存在，默认false.
- flyway.clean-on-validation-error当发现校验错误时是否自动调用clean，默认false.
- flyway.enabled是否开启flywary，默认true.
- flyway.encoding设置迁移时的编码，默认UTF-8.
- flyway.ignore-failed-future-migration当读取元数据表时是否忽略错误的迁移，默认false.
- flyway.init-sqls当初始化好连接时要执行的SQL.
- flyway.locations迁移脚本的位置，默认db/migration.
- flyway.out-of-order是否允许无序的迁移，默认false.
- flyway.password目标数据库的密码.
- flyway.placeholder-prefix设置每个placeholder的前缀，默认${.
- flyway.placeholder-replacementplaceholders是否要被替换，默认true.
- flyway.placeholder-suffix设置每个placeholder的后缀，默认}.
- flyway.placeholders.[placeholder name]设置placeholder的value
- flyway.schemas设定需要flywary迁移的schema，大小写敏感，默认为连接默认的schema.
- flyway.sql-migration-prefix迁移文件的前缀，默认为V.
- flyway.sql-migration-separator迁移脚本的文件名分隔符，默认__
- flyway.sql-migration-suffix迁移脚本的后缀，默认为.sql
- flyway.tableflyway使用的元数据表名，默认为schema_version
- flyway.target迁移时使用的目标版本，默认为latest version
- flyway.url迁移时使用的JDBC URL，如果没有指定的话，将使用配置的主数据源
- flyway.user迁移数据库的用户名
- flyway.validate-on-migrate迁移时是否校验，默认为true.

## liquibase

- liquibase.change-logChange log 配置文件的路径，默认值为classpath:/db/changelog/db.changelog-master.yaml
- liquibase.check-change-log-location是否坚持change log的位置是否存在，默认为true.
- liquibase.contexts逗号分隔的运行时context列表.
- liquibase.default-schema默认的schema.
- liquibase.drop-first是否首先drop schema，默认为false
- liquibase.enabled是否开启liquibase，默认为true.
- liquibase.password目标数据库密码
- liquibase.url要迁移的JDBC URL，如果没有指定的话，将使用配置的主数据源.
- liquibase.user目标数据用户名