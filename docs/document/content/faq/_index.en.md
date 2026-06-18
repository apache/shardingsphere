+++
pre = "<b>8. </b>"
title = "FAQ"
weight = 8
chapter = true
+++

## JDBC

### Found a JtaTransactionManager in spring boot project when integrating with XAtransaction.

Answer:

1. `shardingsphere-transaction-xa-core` include atomikos, it will trigger auto-configuration mechanism in spring-boot, add `@SpringBootApplication(exclude = JtaAutoConfiguration.class)` will solve it.

### The tableName and columnName configured in yaml or properties leading incorrect result when loading Oracle metadata？

Answer:

Note that, in Oracle's metadata, the tableName and columnName is default UPPERCASE, while double-quoted such as `CREATE TABLE "TableName"("Id" number)` the tableName and columnName is the actual content double-quoted, refer to the following SQL for the reality in metadata:
```sql
SELECT OWNER, TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLUMNS WHERE TABLE_NAME IN ('TableName') 
```
ShardingSphere uses the `OracleTableMetaDataLoader` to load the metadata, keep the tableName and columnName in the yaml or properties consistent with the metadata.
ShardingSphere assembled the SQL using the following code:
```java
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

### `SQLException: Unable to unwrap to interface com.mysql.jdbc.Connection` exception thrown when using MySQL XA transaction

Answer:

Incompatibility between multiple MySQL drivers. Because the MySQL5 version of the driver class under the class path is loaded first, when trying to call the `unwrap` method in the MySQL8 driver, the type conversion exception occurs.

The solutions:
Check whether there are both MySQL5 and MySQL8 drivers in the class path, and only keep one driver package of the corresponding version.

The exception stack is as follows:
```
Caused by: java.sql.SQLException: Unable to unwrap to interface com.mysql.jdbc.Connection
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:129)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:97)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:89)
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(SQLError.java:63)
	at com.mysql.cj.jdbc.ConnectionImpl.unwrap(ConnectionImpl.java:2650)
	at com.zaxxer.hikari.pool.ProxyConnection.unwrap(ProxyConnection.java:481)
	at org.apache.shardingsphere.transaction.xa.jta.connection.dialect.MySQLXAConnectionWrapper.wrap(MySQLXAConnectionWrapper.java:46)
	at org.apache.shardingsphere.transaction.xa.jta.datasource.XATransactionDataSource.getConnection(XATransactionDataSource.java:89)
	at org.apache.shardingsphere.transaction.xa.XAShardingSphereTransactionManager.getConnection(XAShardingSphereTransactionManager.java:96
```

## Proxy

### In Windows environment, could not find or load main class org.apache.shardingsphere.proxy.Bootstrap, how to solve it?

Answer:

Some decompression tools may truncate the file name when decompressing the ShardingSphere-Proxy binary package, resulting in some classes not being found.
The solutions:
Open cmd.exe and execute the following command:
```
tar zxvf apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-proxy-bin.tar.gz
```

### How to add a new logic database dynamically when use ShardingSphere-Proxy?

Answer:

When using ShardingSphere-Proxy, users can dynamically create or drop logic database through `DistSQL`, the syntax is as follows:
```sql
CREATE DATABASE [IF NOT EXISTS] databaseName;
DROP DATABASE [IF EXISTS] databaseName;
```
Example:
```sql
CREATE DATABASE sharding_db;
DROP DATABASE sharding_db;
```

### How to use suitable database tools connecting ShardingSphere-Proxy?

Answer:

1. ShardingSphere-Proxy could be considered as a MySQL server, so we recommend using MySQL command line tool to connect to and operate it.
2. If users would like to use a third-party database tool, there may be some errors cause of the certain implementation/options.
3. The currently tested third-party database tools are as follows:
   - DataGrip: 2020.1, 2021.1 (turn on "introspect using jdbc metadata" in idea or datagrip).
   - MySQLWorkBench: 8.0.25.

### When using a client to connect to ShardingSphere-Proxy, if ShardingSphere-Proxy does not create a database or does not register a storage unit, the client connection will fail?

Answer:

1. Third-party database tools will send some SQL query metadata when connecting to ShardingSphere-Proxy. When ShardingSphere-Proxy does not create a `Database` or does not register a `Storage Unit`, ShardingSphere-Proxy cannot execute SQL.
2. It is recommended to create `database` and register `storage unit` first, and then use third-party database tools to connect.
3. Please refer to [Related introduction](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/) the details about `storage unit`.

## Sharding

### How to solve `Cloud not resolve placeholder … in string value …` error?

Answer:

`${...}` or `$->{...}` can be used in inline expression identifiers using the default implementation of the 
`InlineExpressionParser` SPI, but the former one clashes with place holders in Spring property files, so `$->{...}` is recommended to be used in Spring as inline expression identifiers.

### Why does float number appear in the return result of inline expression?

Answer:

The division result of Java integers is also integer, but in Groovy syntax of inline expression, the division result of integers is float number. 
To obtain integer division result, A/B needs to be modified as A.intdiv(B).

### If sharding database is partial, should tables without sharding database & table configured in sharding rules?

Answer:

A table that does not use sharding is called single table in ShardingSphere, and you can use [LOAD statements](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/single-table/load-single-table/) or [SINGLE rule](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/yaml-config/rules/single/) to configure the single table that needs to be loaded.

### Why are binding tables rejected when my table names already end with digits?

Answer:

Binding-table validation assumes that digits at the end of an actual table name are sharding suffixes derived from the logical table (for example `t_order_0`). When the business table name itself ends with digits (for example `AllPatientsV1`), those digits are interpreted as suffixes, so ShardingSphere cannot align the binding tables and will report `Invalid binding table configuration`. To avoid this, reserve the trailing digits exclusively for sharding suffixes (add a separator such as `_` before the suffix) or skip binding-table configuration for tables whose names inherently contain digits.

### When generic Long type `SingleKeyTableShardingAlgorithm` is used, why does the `ClassCastException: Integer can not cast to Long` exception appear?

Answer:

You must make sure the field in the database table is consistent with that in the sharding algorithms. For example, the field type in database is int(11) and the sharding type corresponds to genetic type is Integer. If you want to configure Long type, please make sure the field type in the database is bigint.

### [Sharding\PROXY] When implementing the `StandardShardingAlgorithm` custom algorithm, the specific type of `Comparable` is specified as Long, and the field type in the database table is bigint, a `ClassCastException: Integer can not cast to Long` exception occurs.

Answer：

When implementing the `doSharding` method, it is not recommended to specify the specific type of `Comparable` in the method declaration, but to convert the type in the implementation of the `doSharding` method. You can refer to the `ModShardingAlgorithm#doSharding` method.

### Why is the default distributed auto-augment key strategy provided by ShardingSphere not continuous and most of them end with even numbers?

Answer:

ShardingSphere uses snowflake algorithms as the default distributed auto-augment key strategy to make sure unrepeated and decentralized auto-augment sequence is generated under the distributed situations. Therefore, auto-augment keys can be incremental but not continuous.
But the last four numbers of snowflake algorithm are incremental value within one millisecond. Thus, if concurrency degree in one millisecond is not high,  the last four numbers are likely to be zero, which explains why the rate of even end number is higher.
In 3.1.0 version, the problem of ending with even numbers has been totally solved, please refer to: https://github.com/apache/shardingsphere/issues/1617

### How to allow range query with using inline sharding strategy (BETWEEN AND, \>, \<, \>=, \<=)?

Answer:

1. Update to 4.1.0 above.
2. Configure(A tip here: then each range query will be broadcast to every sharding table):
- Version 4.x: `allow.range.query.with.inline.sharding` to `true` (Default value is `false`).
- Version 5.x: `allow-range-query-with-inline-sharding` to `true` in InlineShardingStrategy (Default value is `false`).

### Why does my custom distributed primary key do not work after implementing `KeyGenerateAlgorithm` interface and configuring `type` property?

Answer:

[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) is a kind of API for the third party to implement or expand. Except implementing interface, you also need to create a corresponding file in `META-INF/services` to make the JVM load these SPI implementations.
More detail for SPI usage, please search by yourself.
Other ShardingSphere functionality implementation will take effect in the same way.

### In addition to internal distributed primary key, does ShardingSphere support other native auto-increment keys?

Answer:

Yes. But there is restriction to the use of native auto-increment keys, which means they cannot be used as sharding keys at the same time.
Since ShardingSphere does not have the database table structure and native auto-increment key is not included in original SQL, it cannot parse that field to the sharding field. If the auto-increment key is not sharding key, it can be returned normally and is needless to be cared. But if the auto-increment key is also used as sharding key, ShardingSphere cannot parse its sharding value, which will make SQL routed to multiple tables and influence the rightness of the application.
The premise for returning native auto-increment key is that INSERT SQL is eventually routed to one table. Therefore, auto-increment key will return zero when INSERT SQL returns multiple tables.

## Single table

### Table or view `%s` does not exist. How to solve the exception?

Answer:

In versions before ShardingSphere 5.4.0, single tables used automatic loading. This way has many problems in actual use:

1. After a large number of data sources are registered in the logical database, too many automatically loaded single tables will cause ShardingSphere-Proxy/JDBC to start slowly;
2. When users use DistSQL, they will operate in the order of: **Register storage unit -> Create sharding, encryption, read-write separation and other rules -> Create table**. Due to the existence of the single-table automatic loading mechanism, the database will be accessed multiple times for loading during the operation, and when multiple rules are mixed and used, the single-table metadata will be confused;
3. Automatically load single tables from all data sources. Users cannot exclude single tables or abandoned tables that they do not want to be managed by ShardingSphere.

In order to solve the above problems, starting from ShardingSphere 5.4.0 version, the loading method of single tables has been adjusted. Users need to manually load a single table in the database through YAML configuration or DistSQL.
It should be noted that when using the DistSQL LOAD statement to load a single table, you need to ensure that all data sources are registered. Therefore, after the rules are created, the single table LOAD operation is performed based on the logical data source (if there is no logical data source, use the physical data source).

* YAML loading single table example:

```yaml
rules:
   - !SINGLE
     tables:
       - "*.*"
   - !READWRITE_SPLITTING
      dataSourceGroups:
       readwrite_ds:
         writeDataSourceName: write_ds
         readDataSourceNames:
           - read_ds_0
           - read_ds_1
         loadBalancerName: random
     loadBalancers:
       random:
         type: RANDOM
```

For more YAML configuration of loading single table, please refer to [Single](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/single/).

* DistSQL loading single table example:

```sql
LOAD SINGLE TABLE *.*;
```

For more LOAD single table DistSQL, please refer to [Load Single Table](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/single-table/load-single-table/).

## DistSQL

### How to set custom JDBC connection properties or connection pool properties when adding a data source using DistSQL?

Answer:

1. If you need to customize JDBC connection properties, please take the `urlSource` way to define `dataSource`.
2. ShardingSphere presets necessary connection pool properties, such as `maxPoolSize`, `idleTimeout`, etc. If you need to add or overwrite the properties, please specify it with `PROPERTIES` in the `dataSource`.
3. Please refer to [Related introduction](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/) for above rules.

### How to solve ` Storage unit [xxx] is still used by [SingleRule].` exception when dropping a data source using DistSQL?

Answer：

1. Storage units referenced by rules cannot be deleted
2. If the storage unit is only referenced by single rule, and the user confirms that the restriction can be ignored, the optional parameter ignore single tables can be added to perform forced deletion
```
UNREGISTER STORAGE UNIT storageUnitName [, storageUnitName] ... [ignore single tables]
```

### How to solve ` Failed to get driver instance for jdbcURL=xxx.` exception when adding a data source using DistSQL?

Answer：

ShardingSphere Proxy do not have jdbc driver during deployment. Some example of this include `mysql-connector`. To use it otherwise following syntax can be used:
```
REGISTER STORAGE UNIT storageUnit [..., storageUnit]
```

## Other

### How to debug when SQL can not be executed rightly in ShardingSphere?

Answer:

`sql.show` configuration is provided in ShardingSphere-Proxy and post-1.5.0 version of ShardingSphere-JDBC, enabling the context parsing, rewritten SQL and the routed data source printed to info log. `sql.show` configuration is off in default, and users can turn it on in configurations.
A Tip: Property `sql.show` has changed to `sql-show` in version 5.x.

### Why do some compiling errors appear? Why did not the IDEA index the generated codes?

Answer:

ShardingSphere uses lombok to enable minimal coding. For more details about using and installment, please refer to the official website of [lombok](https://projectlombok.org/download.html).
The codes under the package `org.apache.shardingsphere.sql.parser.autogen` are generated by ANTLR. You may execute the following command to generate codes:
```bash
./mvnw -DskipITs -DskipTests install -T1C
```
The generated codes such as `org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser` may be too large to be indexed by the IDEA.
You may configure the IDEA's property `idea.max.intellisense.filesize=10000`.

### In SQLSever and PostgreSQL, why does the aggregation column without alias throw exception?

Answer:

SQLServer and PostgreSQL will rename aggregation columns acquired without alias, such as the following SQL:
```sql
SELECT SUM(num), SUM(num2) FROM tablexxx;
```
Columns acquired by SQLServer are empty string and (2); columns acquired by PostgreSQL are empty sum and sum(2). It will cause error because ShardingSphere is unable to find the corresponding column.
The right SQL should be written as:
```sql
SELECT SUM(num) AS sum_num, SUM(num2) AS sum_num2 FROM tablexxx;
```

### Why does Oracle database throw “Order by value must implements Comparable” exception when using Timestamp Order By?

Answer:

There are two solutions for the above problem: 1. Configure JVM parameter “-oracle.jdbc.J2EE13Compliant=true” 2. Set System.getProperties().setProperty(“oracle.jdbc.J2EE13Compliant”, “true”) codes in the initialization of the project.
Reasons:
`org.apache.shardingsphere.sharding.merge.dql.orderby.OrderByValue#getOrderValues()`:
```java
    private List<Comparable<?>> getOrderValues() throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(orderByItems.size());
        for (OrderByItem each : orderByItems) {
            Object value = queryResult.getValue(each.getIndex(), Object.class);
            Preconditions.checkState(null == value || value instanceof Comparable, "Order by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
```
After using resultSet.getObject(int index), for TimeStamp oracle, the system will decide whether to return java.sql.TimeStamp or define oracle.sql.TIMESTAMP according to the property of  oracle.jdbc.J2EE13Compliant. See oracle.jdbc.driver.TimestampAccessor#getObject(int var1) method in ojdbc codes for more detail:
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

### In Windows environment,when cloning ShardingSphere source code through Git, why prompt filename too long and how to solve it?

Answer:

To ensure the readability of source code,the ShardingSphere Coding Specification requires that the naming of classes,methods and variables be literal and avoid abbreviations,which may result in   some source files have long names. 
Since the Git version of Windows is compiled using msys,it uses the old version of Windows Api,limiting the file name to no more than 260 characters. 
The solutions are as follows: 
Open cmd.exe (you need to add git to environment variables) and execute the following command to allow git supporting log paths: 
```
git config --global core.longpaths true
```
If we use windows 10, also need enable win32 log paths in registry editor or group strategy(need reboot):
> Create the registry key `HKLM\SYSTEM\CurrentControlSet\Control\FileSystem LongPathsEnabled` (Type: REG_DWORD) in registry editor, and be set to 1.
> Or click "setting" button in system menu, print "Group Policy" to open a new window "Edit Group Policy", and then click 'Computer Configuration' > 'Administrative Templates' > 'System' > 'Filesystem', and then turn on 'Enable Win32 long paths' option.
Reference material:
https://docs.microsoft.com/zh-cn/windows/desktop/FileIO/naming-a-file
https://ourcodeworld.com/articles/read/109/how-to-solve-filename-too-long-error-in-git-powershell-and-github-application-for-windows

### How to solve `Type is required` error?

Answer:

In Apache ShardingSphere, many functionality implementation are uploaded through SPI, such as Distributed Primary Key. These functions load SPI implementation by configuring the `type`, so the `type` must be specified in the configuration file.

### How to speed up the metadata loading when service starts up?

Answer:

1. Update to 4.0.1 above, which helps speed up the process of loading table metadata.
2. Configure:
- `max.connections.size.per.query`(Default value is 1) higher referring to connection pool you adopt(Version >= 3.0.0.M3 & Version < 5.0.0).
- `max-connections-size-per-query`(Default value is 1) higher referring to connection pool you adopt(Version >= 5.0.0).

### The ANTLR plugin generates codes in the same level directory as src, which is easy to commit by mistake. How to avoid it?

Answer:

Goto [Settings -> Languages & Frameworks -> ANTLR v4 default project settings](jetbrains://idea/settings?name=Languages+%26+Frameworks--ANTLR+v4+default+project+settings) and configure the output directory of the generated code as `target/gen` as shown:
![Configure ANTLR plugin](https://shardingsphere.apache.org/document/current/img/faq/configure-antlr-plugin.png)

### Why is the database sharding result not correct when using `Proxool`?

Answer:

When using `Proxool` to configure multiple data sources, each one of them should be configured with alias. It is because `Proxool` would check whether existing alias is included in the connection pool or not when acquiring connections, so without alias, each connection will be acquired from the same data source.
The followings are core codes from ProxoolDataSource getConnection method in `Proxool`:
```java
    if(!ConnectionPoolManager.getInstance().isPoolExists(this.alias)) {
        this.registerPool();
    }
```
For more alias usages, please refer to [Proxool](http://proxool.sourceforge.net/configure.html) official website.
