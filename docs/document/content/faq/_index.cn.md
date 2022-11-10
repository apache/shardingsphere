+++
pre = "<b>8. </b>"
title = "FAQ"
weight = 8
chapter = true
+++

## JDBC

### [JDBC] 为什么配置了某个数据连接池的 spring-boot-starter（比如 druid）和 shardingsphere-jdbc-spring-boot-starter 时，系统启动会报错？

回答：

1. 因为数据连接池的 starter（比如 druid）可能会先加载并且其创建一个默认数据源，这将会使得 ShardingSphere-JDBC 创建数据源时发生冲突。
2. 解决办法为，去掉数据连接池的 starter 即可，ShardingSphere-JDBC 自己会创建数据连接池。

### [JDBC] 使用 Spring 命名空间时找不到 xsd?

回答：

Spring 命名空间使用规范并未强制要求将 xsd 文件部署至公网地址，但考虑到部分用户的需求，我们也将相关 xsd 文件部署至 ShardingSphere 官网。
实际上 shardingsphere-jdbc-spring-namespace 的 jar 包中 META-INF\spring.schemas 配置了 xsd 文件的位置：
META-INF\namespace\sharding.xsd 和 META-INF\namespace\readwrite-splitting.xsd，只需确保 jar 包中该文件存在即可。

### [JDBC] 引入 `shardingsphere-transaction-xa-core` 后，如何避免 spring-boot 自动加载默认的 JtaTransactionManager？

回答:

1. 需要在 spring-boot 的引导类中添加 `@SpringBootApplication(exclude = JtaAutoConfiguration.class)`。
### [JDBC] Oracle 表名、字段名配置大小写在加载 `metadata` 元数据时结果不正确？
回答：
需要注意，Oracle 表名和字段名，默认元数据都是大写，除非建表语句中带双引号，如 `CREATE TABLE "TableName"("Id" number)` 元数据为双引号中内容，可参考以下SQL查看元数据的具体情况：
```
SELECT OWNER, TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLUMNS WHERE TABLE_NAME IN ('TableName') 
```
ShardingSphere 使用 `OracleTableMetaDataLoader` 对 Oracle 元数据进行加载，配置时需确保表名、字段名的大小写配置与数据库中的一致。
ShardingSphere 查询元数据关键SQL:
```
    private String getTableMetaDataSQL(final Collection<String> tables, final DatabaseMetaData metaData) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder(28);
        if (versionContainsIdentityColumn(metaData)) {
            stringBuilder.append(", IDENTITY_COLUMN");
        }
        if (versionContainsCollation(metaData)) {
            stringBuilder.append(", COLLATION");
        }
        String collation = stringBuilder.toString();
        return tables.isEmpty() ? String.format(TABLE_META_DATA_SQL, collation)
                : String.format(TABLE_META_DATA_SQL_IN_TABLES, collation, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
```

## Proxy

### [Proxy] Windows 环境下，运行 ShardingSphere-Proxy，找不到或无法加载主类 org.apache.shardingsphere.proxy.Bootstrap，如何解决？

回答：

某些解压缩工具在解压 ShardingSphere-Proxy 二进制包时可能将文件名截断，导致找不到某些类。
解决方案：
打开 cmd.exe 并执行下面的命令：
```
tar zxvf apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-proxy-bin.tar.gz
``` 

### [Proxy] 在使用 ShardingSphere-Proxy 的时候，如何动态在添加新的逻辑库？

回答：

使用 ShardingSphere-Proxy 时，可以通过 `DistSQL` 动态的创建或移除逻辑库，语法如下：
```sql
CREATE DATABASE  [IF NOT EXISTS] databaseName;
DROP DATABASE  [IF EXISTS] databaseName;
```
例：
```sql
CREATE DATABASE sharding_db;
DROP DATABASE sharding_db;
```

### [Proxy] 在使用 ShardingSphere-Proxy 时，怎么使用合适的工具连接到 ShardingSphere-Proxy？

回答：

1. ShardingSphere-Proxy 可以看做是一个 database server，所以首选支持 SQL 命令连接和操作。
2. 如果使用其他第三方数据库工具，可能由于不同工具的特定实现导致出现异常。
3. 目前已测试的第三方数据库工具如下：
   - Navicat：11.1.13、15.0.20。
   - DataGrip：2020.1、2021.1（使用 IDEA/DataGrip 时打开 `introspect using JDBC metadata` 选项）。
   - WorkBench：8.0.25。

### [Proxy] 使用 Navicat 等第三方数据库工具连接 ShardingSphere-Proxy 时，如果 ShardingSphere-Proxy 没有创建 Database 或者没有添加 Storage Unit，连接失败？

回答：

1. 第三方数据库工具在连接 ShardingSphere-Proxy 时会发送一些 SQL 查询元数据，当 ShardingSphere-Proxy 没有创建 `database` 或者没有添加 `storage unit` 时，ShardingSphere-Proxy 无法执行 SQL。
2. 推荐先创建 `database` 和 `storage unit` 之后再使用第三方数据库工具连接。
3. 有关 `storage unit` 的详情请参考。[相关介绍](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)

## 分片

### [分片] Cloud not resolve placeholder ... in string value ... 异常的解决方法?

回答：

行表达式标识符可以使用 `${...}` 或 `$->{...}`，但前者与 Spring 本身的属性文件占位符冲突，因此在 Spring 环境中使用行表达式标识符建议使用 `$->{...}`。

### [分片] inline 表达式返回结果为何出现浮点数？

回答：

Java的整数相除结果是整数，但是对于 inline 表达式中的 Groovy 语法则不同，整数相除结果是浮点数。
想获得除法整数结果需要将 A/B 改为 A.intdiv(B)。

### [分片] 如果只有部分数据库分库分表，是否需要将不分库分表的表也配置在分片规则中？

回答：

不需要，ShardingSphere 会自动识别。

### [分片] 指定了泛型为 Long 的 `SingleKeyTableShardingAlgorithm`，遇到 `ClassCastException: Integer can not cast to Long`?

回答：

必须确保数据库表中该字段和分片算法该字段类型一致，如：数据库中该字段类型为 int(11)，泛型所对应的分片类型应为 Integer，如果需要配置为 Long 类型，请确保数据库中该字段类型为 bigint。

### [分片、PROXY] 实现 `StandardShardingAlgorithm` 自定义算法时，指定了 `Comparable` 的具体类型为 Long, 且数据库表中字段类型为 bigint，出现 `ClassCastException: Integer can not cast to Long` 异常。

回答：

实现 `doSharding` 方法时，不建议指定方法声明中 `Comparable` 具体的类型，而是在 `doSharding` 方法实现中对类型进行转换，可以参考 `ModShardingAlgorithm#doSharding` 方法

### [分片]  ShardingSphere 提供的默认分布式自增主键策略为什么是不连续的，且尾数大多为偶数？

回答：

ShardingSphere 采用 snowflake 算法作为默认的分布式自增主键策略，用于保证分布式的情况下可以无中心化的生成不重复的自增序列。因此自增主键可以保证递增，但无法保证连续。
而 snowflake 算法的最后 4 位是在同一毫秒内的访问递增值。因此，如果毫秒内并发度不高，最后 4 位为零的几率则很大。因此并发度不高的应用生成偶数主键的几率会更高。
在 3.1.0 版本中，尾数大多为偶数的问题已彻底解决，参见：https://github.com/apache/shardingsphere/issues/1617

### [分片] 如何在 inline 分表策略时，允许执行范围查询操作（BETWEEN AND、\>、\<、\>=、\<=）？

回答：

1. 需要使用 4.1.0 或更高版本。
2. 调整以下配置项（需要注意的是，此时所有的范围查询将会使用广播的方式查询每一个分表）：
- 4.x 版本：`allow.range.query.with.inline.sharding` 设置为 true 即可（默认为 false）。
- 5.x 版本：在 InlineShardingStrategy 中将 `allow-range-query-with-inline-sharding` 设置为 true 即可（默认为 false）。

### [分片] 为什么我实现了 `KeyGenerateAlgorithm` 接口，也配置了 Type，但是自定义的分布式主键依然不生效？

回答：

[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) 是一种为了被第三方实现或扩展的 API，除了实现接口外，还需要在 META-INF/services 中创建对应文件来指定 SPI 的实现类，JVM 才会加载这些服务。
具体的 SPI 使用方式，请大家自行搜索。
与分布式主键 `KeyGenerateAlgorithm` 接口相同，其他 ShardingSphere 的[扩展功能](/cn/concepts/pluggable/)也需要用相同的方式注入才能生效。

### [分片] ShardingSphere 除了支持自带的分布式自增主键之外，还能否支持原生的自增主键？

回答：

是的，可以支持。但原生自增主键有使用限制，即不能将原生自增主键同时作为分片键使用。
由于 ShardingSphere 并不知晓数据库的表结构，而原生自增主键是不包含在原始 SQL 中内的，因此 ShardingSphere 无法将该字段解析为分片字段。如自增主键非分片键，则无需关注，可正常返回；若自增主键同时作为分片键使用，ShardingSphere 无法解析其分片值，导致 SQL 路由至多张表，从而影响应用的正确性。
而原生自增主键返回的前提条件是 INSERT SQL 必须最终路由至一张表，因此，面对返回多表的 INSERT SQL，自增主键则会返回零。

## 数据加密

### [数据加密] JPA 和 数据加密无法一起使用，如何解决？

回答：

由于数据加密的 DDL 尚未开发完成，因此对于自动生成 DDL 语句的 JPA 与 数据加密一起使用时，会导致 JPA 的实体类（Entity）无法同时满足 DDL 和 DML 的情况。
解决方案如下：
1. 以需要加密的逻辑列名编写 JPA 的实体类（Entity）。
2. 关闭 JPA 的 auto-ddl，如 auto-ddl=none。
3. 手动建表，建表时应使用数据加密配置的 `cipherColumn`,`plainColumn` 和 `assistedQueryColumn` 代替逻辑列。

## DistSQL

### [DistSQL] 使用 DistSQL 添加数据源时，如何设置自定义的 JDBC 连接参数或连接池属性？

回答：

1. 如需自定义 JDBC 参数，请使用 `urlSource` 的方式定义 `dataSource`。
2. ShardingSphere 预置了必要的连接池参数，如 `maxPoolSize`、`idleTimeout` 等。如需增加或覆盖参数配置，请在 `dataSource` 中通过 `PROPERTIES` 指定。
3. 以上规则请参考 [相关介绍](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)。

### [DistSQL] 使用 `DistSQL` 删除资源时，出现 `Resource [xxx] is still used by [SingleTableRule]`。

回答：

1. 被规则引用的资源将无法被删除。
2. 若资源只被 single table rule 引用，且用户确认可以忽略该限制，则可以添加可选参数 ignore single tables 进行强制删除。

### [DistSQL] 使用 `DistSQL` 添加资源时，出现 `Failed to get driver instance for jdbcURL=xxx`。

回答：

ShardingSphere-Proxy 在部署过程中没有添加 jdbc 驱动，需要将 jdbc 驱动放入 ShardingSphere-Proxy 解压后的 ext-lib 目录，例如：`mysql-connector`。

## 其他

### [其他] 如果 SQL 在 ShardingSphere 中执行不正确，该如何调试？

回答：

在 ShardingSphere-Proxy 以及 ShardingSphere-JDBC 1.5.0 版本之后提供了 `sql.show` 的配置，可以将解析上下文和改写后的 SQL 以及最终路由至的数据源的细节信息全部打印至 info 日志。
`sql.show` 配置默认关闭，如果需要请通过配置开启。
> 注意：5.x版本以后，`sql.show` 参数调整为 `sql-show`。

### [其他] 阅读源码时为什么会出现编译错误? IDEA 不索引生成的代码？

回答：

ShardingSphere 使用 lombok 实现极简代码。关于更多使用和安装细节，请参考 [lombok官网](https://projectlombok.org/download.html)。
`org.apache.shardingsphere.sql.parser.autogen` 包下的代码由 ANTLR 生成，可以执行以下命令快速生成：
```bash
./mvnw -Dcheckstyle.skip=true -Drat.skip=true -Dmaven.javadoc.skip=true -Djacoco.skip=true -DskipITs -DskipTests install -T1C 
```
生成的代码例如 `org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser` 等 Java 文件由于较大，默认配置的 IDEA 可能不会索引该文件。
可以调整 IDEA 的属性：`idea.max.intellisense.filesize=10000`。

### [其他] 使用 SQLSever 和 PostgreSQL 时，聚合列不加别名会抛异常？

回答：

SQLServer 和 PostgreSQL 获取不加别名的聚合列会改名。例如，如下 SQL：
```sql
SELECT SUM(num), SUM(num2) FROM tablexxx;
```
SQLServer 获取到的列为空字符串和(2)，PostgreSQL 获取到的列为空 sum 和 sum(2)。这将导致 ShardingSphere 在结果归并时无法找到相应的列而出错。
正确的 SQL 写法应为：
```sql
SELECT SUM(num) AS sum_num, SUM(num2) AS sum_num2 FROM tablexxx;
```

### [其他] Oracle 数据库使用 Timestamp 类型的 Order By 语句抛出异常提示 “Order by value must implements Comparable”?

回答：

针对上面问题解决方式有两种：
1. 配置启动 JVM 参数 “-oracle.jdbc.J2EE13Compliant=true”
2. 通过代码在项目初始化时设置 System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");
原因如下:
`org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByValue#getOrderValues()` 方法如下:
```java
    private List<Comparable<?>> getOrderValues() throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(orderByItems.size());
        for (OrderItem each : orderByItems) {
            Object value = resultSet.getObject(each.getIndex());
            Preconditions.checkState(null == value || value instanceof Comparable, "Order by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
```
使用了 resultSet.getObject(int index) 方法，针对 TimeStamp oracle 会根据 oracle.jdbc.J2EE13Compliant 属性判断返回 java.sql.TimeStamp 还是自定义 oralce.sql.TIMESTAMP
详见 ojdbc 源码 oracle.jdbc.driver.TimestampAccessor#getObject(int var1) 方法:
```java
    Object getObject(int var1) throws SQLException {
        Object var2 = null;
        if(this.rowSpaceIndicator == null) {
            DatabaseError.throwSqlException(21);
        }
        if(this.rowSpaceIndicator[this.indicatorIndex + var1] != -1) {
            if(this.externalType != 0) {
                switch(this.externalType) {
                case 93:
                    return this.getTimestamp(var1);
                default:
                    DatabaseError.throwSqlException(4);
                    return null;
                }
            }
            if(this.statement.connection.j2ee13Compliant) {
                var2 = this.getTimestamp(var1);
            } else {
                var2 = this.getTIMESTAMP(var1);
            }
        }
        return var2;
    }
```

### [其他] Windows 环境下，通过 Git 克隆 ShardingSphere 源码时为什么提示文件名过长，如何解决？

回答：

为保证源码的可读性，ShardingSphere 编码规范要求类、方法和变量的命名要做到顾名思义，避免使用缩写，因此可能导致部分源码文件命名较长。由于 Windows 版本的 Git 是使用 msys 编译的，它使用了旧版本的 Windows Api，限制文件名不能超过 260 个字符。
解决方案如下：
打开 cmd.exe（你需要将 git 添加到环境变量中）并执行下面的命令，可以让 git 支持长文件名：
```
git config --global core.longpaths true
```
如果是 Windows 10，还需要通过注册表或组策略，解除操作系统的文件名长度限制（需要重启）：
> 在注册表编辑器中创建 `HKLM\SYSTEM\CurrentControlSet\Control\FileSystem LongPathsEnabled`， 类型为 `REG_DWORD`，并设置为1。
> 或者从系统菜单点击设置图标，输入“编辑组策略”， 然后在打开的窗口依次进入“计算机管理” > “管理模板” > “系统” > “文件系统”，在右侧双击“启用 win32 长路径”。
参考资料：
https://docs.microsoft.com/zh-cn/windows/desktop/FileIO/naming-a-file
https://ourcodeworld.com/articles/read/109/how-to-solve-filename-too-long-error-in-git-powershell-and-github-application-for-windows

### [其他] Type is required 异常的解决方法?

回答：

ShardingSphere 中很多功能实现类的加载方式是通过 [SPI](/cn/concepts/pluggable/) 注入的方式完成的，如分布式主键，注册中心等；这些功能通过配置中 type 类型来寻找对应的 SPI 实现，因此必须在配置文件中指定类型。

### [其他] 服务启动时如何加快 `metadata` 加载速度？

回答：

1. 升级到 `4.0.1` 以上的版本，以提高 metadata 的加载速度。
2. 参照你采用的连接池，将：
- 配置项 `max.connections.size.per.query`（默认值为1）调高（版本 >= 3.0.0.M3 且低于 5.0.0）。
- 配置项 `max-connections-size-per-query`（默认值为1）调高（版本 >= 5.0.0）。

### [其他] ANTLR 插件在 src 同级目录下生成代码，容易误提交，如何避免？

回答：

进入 [Settings -> Languages & Frameworks -> ANTLR v4 default project settings](jetbrains://idea/settings?name=Languages+%26+Frameworks--ANTLR+v4+default+project+settings) 配置生成代码的输出目录为 `target/gen`，如图：
![Configure ANTLR plugin](https://shardingsphere.apache.org/document/current/img/faq/configure-antlr-plugin.png)

### [其他] 使用 `Proxool` 时分库结果不正确？

回答：

使用 Proxool 配置多个数据源时，应该为每个数据源设置 alias，因为 Proxool 在获取连接时会判断连接池中是否包含已存在的 alias，不配置 alias 会造成每次都只从一个数据源中获取连接。
以下是 Proxool 源码中 ProxoolDataSource 类 getConnection 方法的关键代码：
```java
    if(!ConnectionPoolManager.getInstance().isPoolExists(this.alias)) {
        this.registerPool();
    }
```
更多关于 alias 使用方法请参考 [Proxool官网](http://proxool.sourceforge.net/configure.html)。
PS：sourceforge 网站需要翻墙访问。

### [其他] 使用 Spring Boot 2.x 集成 ShardingSphere 时，配置文件中的属性设置不生效？

回答：

需要特别注意，Spring Boot 2.x 环境下配置文件的属性名称约束为仅允许小写字母、数字和短横线，即 `[a-z][0-9]` 和 `-`。
原因如下:
Spring Boot 2.x 环境下，ShardingSphere 通过 Binder 来绑定配置文件，属性名称不规范（如：驼峰或下划线等）会导致属性设置不生效从而校验属性值时抛出 `NullPointerException` 异常。参考以下错误示例：
下划线示例：database_inline
```
spring.shardingsphere.rules.sharding.sharding-algorithms.database_inline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.database_inline.props.algorithm-expression=ds-$->{user_id % 2}
```
```
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'database_inline': Initialization of bean failed; nested exception is java.lang.NullPointerException: Inline sharding algorithm expression cannot be null.
        ... 
Caused by: java.lang.NullPointerException: Inline sharding algorithm expression cannot be null.
        at com.google.common.base.Preconditions.checkNotNull(Preconditions.java:897)
        at org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm.getAlgorithmExpression(InlineShardingAlgorithm.java:58)
        at org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm.init(InlineShardingAlgorithm.java:52)
        at org.apache.shardingsphere.spring.boot.registry.AbstractAlgorithmProvidedBeanRegistry.postProcessAfterInitialization(AbstractAlgorithmProvidedBeanRegistry.java:98)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.applyBeanPostProcessorsAfterInitialization(AbstractAutowireCapableBeanFactory.java:431)
        ... 
```
驼峰示例：databaseInline
```
spring.shardingsphere.rules.sharding.sharding-algorithms.databaseInline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.databaseInline.props.algorithm-expression=ds-$->{user_id % 2}
```
```
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'databaseInline': Initialization of bean failed; nested exception is java.lang.NullPointerException: Inline sharding algorithm expression cannot be null.
        ... 
Caused by: java.lang.NullPointerException: Inline sharding algorithm expression cannot be null.
        at com.google.common.base.Preconditions.checkNotNull(Preconditions.java:897)
        at org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm.getAlgorithmExpression(InlineShardingAlgorithm.java:58)
        at org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm.init(InlineShardingAlgorithm.java:52)
        at org.apache.shardingsphere.spring.boot.registry.AbstractAlgorithmProvidedBeanRegistry.postProcessAfterInitialization(AbstractAlgorithmProvidedBeanRegistry.java:98)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.applyBeanPostProcessorsAfterInitialization(AbstractAutowireCapableBeanFactory.java:431)
        ... 
```
从异常堆栈中分析可知： `AbstractAlgorithmProvidedBeanRegistry.registerBean` 方法调用 `PropertyUtil.containPropertyPrefix(environment, prefix)` 方法判断指定前缀 `prefix` 的配置是否存在，而 `PropertyUtil.containPropertyPrefix(environment, prefix)` 方法，在 Spring Boot 2.x 环境下使用了 Binder，不规范的属性名称（如：驼峰或下划线等）会导致属性设置不生效。
