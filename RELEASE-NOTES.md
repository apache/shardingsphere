## 3.1.0

### API Changes

1. Adjust persist structure for orchestration's registry center.
1. Adjust Sharding-JDBC configuration API.

### New Features

1. XA Transaction available.
1. All SQL 100% compatible if route to single data node (MySQL Only).
1. DISTINCT SQL syntax available.
1. Broadcast table available.
1. Solve data lean problem when using DefaultKeyGenerator with low TPS.

###  Change Logs
1. [MILESTONE #3](https://github.com/sharding-sphere/sharding-sphere/milestone/3)
1. [MILESTONE #4](https://github.com/sharding-sphere/sharding-sphere/milestone/4)

## 3.0.0

### Milestones

1. Sharding-Proxy launch. Support the use of ShardingSphere in the form of database to support for MySQL CLI and GUI client

### New Features

#### Core

1. [ISSUE #290](https://github.com/sharding-sphere/sharding-sphere/issues/290) Support batch INSERT
1. [ISSUE #501](https://github.com/sharding-sphere/sharding-sphere/issues/501) Support OR
1. [ISSUE #980](https://github.com/sharding-sphere/sharding-sphere/issues/980) Support DCL
1. [ISSUE #1111](https://github.com/sharding-sphere/sharding-sphere/issues/1111) Support MySQL DAL

#### Sharding-Proxy

1. [ISSUE #902](https://github.com/sharding-sphere/sharding-sphere/issues/902) Support XA transaction
1. [ISSUE #916](https://github.com/sharding-sphere/sharding-sphere/issues/916) Support authorization
1. [ISSUE #936](https://github.com/sharding-sphere/sharding-sphere/issues/936) Support registry center
1. [ISSUE #1046](https://github.com/sharding-sphere/sharding-sphere/issues/1046) Support multiple logic databases

### Enhancements

#### Core

1. [ISSUE #373](https://github.com/sharding-sphere/sharding-sphere/issues/373) Support `order by ?`
1. [ISSUE #610](https://github.com/sharding-sphere/sharding-sphere/issues/610) Route unicast for DQL without table
1. [ISSUE #701](https://github.com/sharding-sphere/sharding-sphere/issues/701) Caching parsed results to improve performance
1. [ISSUE #773](https://github.com/sharding-sphere/sharding-sphere/issues/773) Support sharding and autoincrement key of INSERT without column names
1. [ISSUE #935](https://github.com/sharding-sphere/sharding-sphere/issues/935) Use `YAML` instead of `JSON` to store configurations in registry center
1. [ISSUE #1004](https://github.com/sharding-sphere/sharding-sphere/issues/1004) Properties can configure for Sharding and Master-slave independent
1. [ISSUE #1205](https://github.com/sharding-sphere/sharding-sphere/issues/1205) Execute engine enhancement

#### Sharding-JDBC

1. [ISSUE #652](https://github.com/sharding-sphere/sharding-sphere/issues/652) Support `Spring Boot Starter` 2.X
1. [ISSUE #702](https://github.com/sharding-sphere/sharding-sphere/issues/702) Support `$->{..}` for inline expression 
1. [ISSUE #719](https://github.com/sharding-sphere/sharding-sphere/issues/719) Support inject key generator objects by spring namespace
1. [ISSUE #720](https://github.com/sharding-sphere/sharding-sphere/issues/720) Support inject sharding algorithm objects by spring namespace

#### Sharding-Opentracing

1. [ISSUE #1172](https://github.com/sharding-sphere/sharding-sphere/issues/1172) Opentracing enhancement

### API changes

1. [ISSUE #1153](https://github.com/sharding-sphere/sharding-sphere/issues/1153) Adjust the maven artifactId for Orchestration module
1. [ISSUE #1203](https://github.com/sharding-sphere/sharding-sphere/issues/1203) Adjust Spring namespace xsd for Sharding and Master-slave
1. [ISSUE #1289](https://github.com/sharding-sphere/sharding-sphere/issues/1289) Adjust Hint API
1. [ISSUE #1302](https://github.com/sharding-sphere/sharding-sphere/issues/1302) Refine package structure
1. [ISSUE #1305](https://github.com/sharding-sphere/sharding-sphere/issues/1305) Deprecated and remove sharding-jdbc-transaction-parent module
1. [ISSUE #1382](https://github.com/sharding-sphere/sharding-sphere/issues/1328) Remove type configuration in Orchestration module

### Bug Fixes

#### Core

1. [ISSUE #569](https://github.com/sharding-sphere/sharding-sphere/issues/569) Failed to parse SQL for Oracle when ROWNUM is not at end
1. [ISSUE #628](https://github.com/sharding-sphere/sharding-sphere/issues/628) Support data type jsonb for PostgreSQL
1. [ISSUE #646](https://github.com/sharding-sphere/sharding-sphere/issues/646) When aliases in `SELECT ITEMS` correspond to the real column names of `GROUP BY` or `ORDER BY`, there is no need to generate derived columns
1. [ISSUE #806](https://github.com/sharding-sphere/sharding-sphere/issues/806) `NOT IN` parse exception
1. [ISSUE #827](https://github.com/sharding-sphere/sharding-sphere/issues/827) Endless loop for bad SQL like `SELECT * FROM table WHERE id IN ()`
1. [ISSUE #919](https://github.com/sharding-sphere/sharding-sphere/issues/919) Inline expression with groovy may cause memory leak
1. [ISSUE #993](https://github.com/sharding-sphere/sharding-sphere/issues/993) Fail to parsing PostgreSQL due to the quotation
1. [ISSUE #1015](https://github.com/sharding-sphere/sharding-sphere/issues/1015) Support SQL like `SELECT id, COUNT(*) FROM table GROUP BY 1,2`
1. [ISSUE #1120](https://github.com/sharding-sphere/sharding-sphere/issues/1120) Derived columns of `GROUP BY / ORDER BY` appear in query result
1. [ISSUE #1186](https://github.com/sharding-sphere/sharding-sphere/issues/1186) Dead lock may occur on MEMORY_STRICTLY mode when get connection on concurrency environment
1. [ISSUE #1265](https://github.com/sharding-sphere/sharding-sphere/issues/1265) RoundRobinMasterSlaveLoadBalanceAlgorithm throw an ArrayIndexOutOfBoundsException when AtomicInteger overflow

#### Sharding-JDBC

1. [ISSUE #372](https://github.com/sharding-sphere/sharding-sphere/issues/372) Reuse PreparedStatement cause cache of route result do not clean
1. [ISSUE #629](https://github.com/sharding-sphere/sharding-sphere/issues/629) Support transaction isolation on JDBC
1. [ISSUE #735](https://github.com/sharding-sphere/sharding-sphere/issues/735) Unexpected slave datasource routing result when using `Round-robin` load-balance algorithm in Mybatis
1. [ISSUE #1011](https://github.com/sharding-sphere/sharding-sphere/issues/1011) Can't resolve placeholder in `Spring Boot YAML` configuration

## 2.0.3

### New Features

#### Core

1. [ISSUE #600](https://github.com/sharding-sphere/sharding-sphere/issues/600) Support TCL

### Bug Fixes

#### Core

1. [ISSUE #540](https://github.com/sharding-sphere/sharding-sphere/issues/540) Support SQL that alias is the keyword
1. [ISSUE #577](https://github.com/sharding-sphere/sharding-sphere/issues/577) Support new line for `YAML` configuration

#### Sharding-JDBC

1. [ISSUE #522](https://github.com/sharding-sphere/sharding-sphere/issues/522) Slave database does not need to execute the DDL for read-write splitting


## 2.0.2

### Enhancements

#### Core

1. [ISSUE #475](https://github.com/sharding-sphere/sharding-sphere/issues/475) Support `CREATE INDEX`
1. [ISSUE #525](https://github.com/sharding-sphere/sharding-sphere/issues/525) Support `DROP INDEX`

### Bug Fixes

#### Core

1. [ISSUE #521](https://github.com/sharding-sphere/sharding-sphere/issues/521) `ShardingProperties` is invalid in `YAML` configuration
1. [ISSUE #529](https://github.com/sharding-sphere/sharding-sphere/issues/529) Table name capitalization cannot be queried
1. [ISSUE #541](https://github.com/sharding-sphere/sharding-sphere/issues/541) `IS NOT NULL` parse error
1. [ISSUE #557](https://github.com/sharding-sphere/sharding-sphere/issues/557) When `GROUP BY` and `ORDER BY` aliases are inconsistent, stream merging should be used
1. [ISSUE #559](https://github.com/sharding-sphere/sharding-sphere/issues/559) Support parsing numbers beginning with minus and decimal (e.g. `-.12`)
1. [ISSUE #567](https://github.com/sharding-sphere/sharding-sphere/issues/567) Add escape char for derived columns or alias when using MySQL

#### Sharding-JDBC

1. [ISSUE #520](https://github.com/sharding-sphere/sharding-sphere/issues/520) Exception is no longer `DuplicateKeyException` when the unique key conflict


## 2.0.1

### Enhancements

#### Core

1. [ISSUE #490](https://github.com/sharding-sphere/sharding-sphere/issues/490) Using `rownum` greater than or equal to or less than or equal to the result of paging is incorrect in Oracle
1. [ISSUE #496](https://github.com/sharding-sphere/sharding-sphere/issues/496) Logical table names in sharding configuration can be case sensitive
1. [ISSUE #497](https://github.com/sharding-sphere/sharding-sphere/issues/497) Close registry center gracefully

### Bug Fixes

#### Sharding-JDBC

1. [ISSUE #489](https://github.com/sharding-sphere/sharding-sphere/issues/489) Uses `RuntimeBeanReference` to prevent the creation of `InnerBean` on spring namespace
1. [ISSUE #491](https://github.com/sharding-sphere/sharding-sphere/issues/491) Can't close connection by `ResultSet.getStatement().getConnection().close()`


## 2.0.0

### Milestones

1. API adjust. Brand new groupId and artifactId for `Maven`, package name and spring namespace name. Simplify and enhance API configuration, inline expression fully configuration support
1. Support `spring-boot-starter` of `Sharding-JDBC`
1. Dynamic configuration. `ZooKeeper` and `etcd` can be used as registry to dynamically modify data sources and sharding configurations
1. Database orchestration. Fusing database access procedures to access databases and disable access to slave databases
1. ConfigMap support. Predefined metadata can be obtained in the sharding and read-write separation strategy
1. Tracking system support. You can view the invocation chain of `Sharding-JDBC` through `sky-walking` and other `Opentracing` based APM systems

### Enhancements

#### Core

1. [ISSUE #386](https://github.com/sharding-sphere/sharding-sphere/issues/386) Support SQL that does not contain table names, such as `SELECT 1`

#### Sharding-JDBC

1. [ISSUE #407](https://github.com/sharding-sphere/sharding-sphere/issues/407) Support Hyphen properties for `sharding-jdbc-spring-boot-starter`
1. [ISSUE #424](https://github.com/sharding-sphere/sharding-sphere/issues/424) Providing SQL overall execution events

### Bug Fixes

#### Core

1. [ISSUE #387](https://github.com/sharding-sphere/sharding-sphere/issues/387) Prevent errors from keywords process when '`' exists in function + column name
1. [ISSUE #419](https://github.com/sharding-sphere/sharding-sphere/issues/419) When SQL is rewritten, it does not determine whether alias is a keyword without the escape character, which results in SQL exception
1. [ISSUE #464](https://github.com/sharding-sphere/sharding-sphere/issues/464) SQL if the varchar type is not closed due to the absence of matching single quotes, and the next varchar in SQL is the wrong SQL of Chinese characters, it will lead to higher use of CPU

#### Sharding-JDBC

1. [ISSUE #394](https://github.com/sharding-sphere/sharding-sphere/issues/394) Can't only close statement
1. [ISSUE #398](https://github.com/sharding-sphere/sharding-sphere/issues/398) Use Hint routing to shield case sensitivity 
1. [ISSUE #404](https://github.com/sharding-sphere/sharding-sphere/issues/404) Sharding-jdbc's spring-boot-starter does not support HikariDataSource
1. [ISSUE #436](https://github.com/sharding-sphere/sharding-sphere/issues/436) Read-write splitting, when the RoundRobin algorithm is configured from the database and MyBatis is used, it can only be routed to the same slave library
1. [ISSUE #452](https://github.com/sharding-sphere/sharding-sphere/issues/452) Sharding of DDL statements to more than one table causes a connection leak
1. [ISSUE #472](https://github.com/sharding-sphere/sharding-sphere/issues/472) Before Connection executes createStatement, it calls getMetaData first and then setAutoCommit can not take effective connection to the database that was created later

## 1.5.4.1

### Bug Fixes

1. [ISSUE #382](https://github.com/sharding-sphere/sharding-sphere/issues/382) The query cannot be completed without sharding rule

## 1.5.4

### Bug Fixes

1. [ISSUE #356](https://github.com/sharding-sphere/sharding-sphere/issues/356) In the Where condition of SQL, the REGEXP operator is compatible with non sharding columns
1. [ISSUE #362](https://github.com/sharding-sphere/sharding-sphere/issues/362) Read-write separation using PreparedStatement does not invoke the setParameter method to cause errors
1. [ISSUE #370](https://github.com/sharding-sphere/sharding-sphere/issues/370) Error in calling getGeneratedKeys using native self increment primary key
1. [ISSUE #375](https://github.com/sharding-sphere/sharding-sphere/issues/375) Data can not be obtained after paging second pages route to a single node
1. [ISSUE #379](https://github.com/sharding-sphere/sharding-sphere/issues/379) When Mybatis is used to call Connection.getMetaData (), the connection is not close correct

## 1.5.3

### Enhancements

1. [ISSUE #98](https://github.com/sharding-sphere/sharding-sphere/issues/98) Read-write separation load balancing strategy support configuration
1. [ISSUE #196](https://github.com/sharding-sphere/sharding-sphere/issues/196) Read-write separation and sharding configuration independence

### Bug Fixes

1. [ISSUE #349](https://github.com/sharding-sphere/sharding-sphere/issues/349) Incorrect function of ResultSet.wasNull causes null numeric type in DB to zero
1. [ISSUE #351](https://github.com/sharding-sphere/sharding-sphere/issues/351) Tables that are included in the default data source but not in TableRule configuration are not properly executed
1. [ISSUE #353](https://github.com/sharding-sphere/sharding-sphere/issues/353) In the Where condition of SQL, it is compatible with non sharding columns !=, !> and !< operator
1. [ISSUE #354](https://github.com/sharding-sphere/sharding-sphere/issues/354) In the Where condition of SQL, NOT operators are compatible with non-sharding columns

## 1.5.2

### Milestones

1. The test engine of quality assurance, each SQL can run 60 test cases of different dimensions

### Enhancements

1. [ISSUE #335](https://github.com/sharding-sphere/sharding-sphere/issues/335) Support the GROUP BY + custom function SQL
1. [ISSUE #341](https://github.com/sharding-sphere/sharding-sphere/issues/341) Support ORDER BY xxx NULLS FIRST | LAST statement of Oracle

### Bug Fixes

1. [ISSUE #334](https://github.com/sharding-sphere/sharding-sphere/issues/334) Parsing ORDER BY with functions will resolve the following ASC and DESC to the name attribute of OrderItem
1. [ISSUE #335](https://github.com/sharding-sphere/sharding-sphere/issues/339) JOIN parsing is incorrect using the full name association of the table
1. [ISSUE #346](https://github.com/sharding-sphere/sharding-sphere/issues/346) Parsing table name error of DDL statement DROP TABLE IF EXISTS USER

## 1.5.1

### New Features

1. [ISSUE #314](https://github.com/sharding-sphere/sharding-sphere/issues/314) Support DDL type SQL

### Changes

1. [ISSUE #327](https://github.com/sharding-sphere/sharding-sphere/issues/327) Close sql.show configuration by default

### Bug Fixes

1. [ISSUE #308](https://github.com/sharding-sphere/sharding-sphere/issues/308) Invalid return of database native GeneratedKey
1. [ISSUE #309](https://github.com/sharding-sphere/sharding-sphere/issues/310) ORDER BY and GROUP BY in sub-queries are not included in the analytic context
1. [ISSUE #313](https://github.com/sharding-sphere/sharding-sphere/issues/313) Support <> operator
1. [ISSUE #317](https://github.com/sharding-sphere/sharding-sphere/issues/317) Parameter of LIMIT can not be type of Long
1. [ISSUE #320](https://github.com/sharding-sphere/sharding-sphere/issues/320) SQL rewriting error of GROUP BY + LIMIT
1. [ISSUE #323](https://github.com/sharding-sphere/sharding-sphere/issues/323) Parsing ORDER BY + Aggregation Expression error

## 1.5.0

### Milestones

1. The new SQL parsing module removes the dependence on Druid. We only need to parse the sharding context, and adopt a "semi understanding" concept for SQL to further improve performance and compatibility, and reduce code complexity
1. The new SQL rewrite module adds an optimized rewrite module
1. The new SQL merge module is rebuilt into 3 merging engines: streaming, memory and decorator

### New Features

1. Support Oracle, SQLServer and PostgreSQL
1. Non functional sub-query support

### Enhancements

1. [ISSUE #256](https://github.com/sharding-sphere/sharding-sphere/issues/256) Configurable display sharding execute to SQL log
1. [ISSUE #291](https://github.com/sharding-sphere/sharding-sphere/issues/291) Processing SQL use stream mode that contains only GroupBy

### Changes

1. Simplify the distributed self increasing sequence. Each table is supported by simplifying a multiple self increasing sequence to a single table supporting only a single distributed self increasing sequence, and no longer supporting workerID settings through environment variables
1. Remove support for OR

### Bug Fixes

1. [ISSUE #239](https://github.com/sharding-sphere/sharding-sphere/issues/239) LIMIT routes to multiple query result sets. If there is only one result set that is not empty, the result of paging is incorrect
1. [ISSUE #263](https://github.com/sharding-sphere/sharding-sphere/issues/263) Sharding and logical table configuration can be case insensitive
1. [ISSUE #292](https://github.com/sharding-sphere/sharding-sphere/issues/292) When the memory mode handles GROUP BY statement, if there is paging information, it needs to be rewritten
1. [ISSUE #295](https://github.com/sharding-sphere/sharding-sphere/issues/295) LIMIT 0 does not filter the result set according to paging restrictions

## 1.4.2

### Enhancements

1. [ISSUE #219](https://github.com/sharding-sphere/sharding-sphere/issues/219) Thread performance optimization
1. [ISSUE #215](https://github.com/sharding-sphere/sharding-sphere/issues/215) Aggregated result set of stream sort StreamingOrderByReducerResultSet performance optimization
1. [ISSUE #161](https://github.com/sharding-sphere/sharding-sphere/issues/161) When the result sets are merged, heap sort can be used to improve performance

### Bug Fixes

1. [ISSUE #212](https://github.com/sharding-sphere/sharding-sphere/issues/212) More meaningful hints for missing data source rules
1. [ISSUE #214](https://github.com/sharding-sphere/sharding-sphere/issues/214) table_name.column_name in (?,?) in WHERE can't parser expression
1. [ISSUE #180](https://github.com/sharding-sphere/sharding-sphere/issues/180) Batch execution of Update return inaccuracy
1. [ISSUE #225](https://github.com/sharding-sphere/sharding-sphere/issues/225) The last character of automatic generation of Id is not 0

## 1.4.1

### Enhancements

1. [ISSUE #191](https://github.com/sharding-sphere/sharding-sphere/issues/191) Generating KeyGenerator of workerId based on IP of host
1. [ISSUE #192](https://github.com/sharding-sphere/sharding-sphere/issues/192) Get workerId's KeyGenerator based on HOSTNAME's digital suffix
1. [ISSUE #210](https://github.com/sharding-sphere/sharding-sphere/issues/210) Routing to single library and single table to remove supplementary SQL statement fragments

### Bug Fixes

1. [ISSUE #194](https://github.com/sharding-sphere/sharding-sphere/issues/194) Some component exceptions in the close method of Connection, Statement, ResultSet and other interfaces cause the close method of another component to be not invoked
1. [ISSUE #199](https://github.com/sharding-sphere/sharding-sphere/issues/199) Sharding and reuse PreparedStatement object cause route error
1. [ISSUE #201](https://github.com/sharding-sphere/sharding-sphere/issues/201) Event transmission missing before batch operation execution 
1. [ISSUE #203](https://github.com/sharding-sphere/sharding-sphere/issues/203) Merge events sent by the batch operation
1. [ISSUE #209](https://github.com/sharding-sphere/sharding-sphere/issues/209) Parallel execution of multiple limit queries leads to IndexOutOfBoundsException

## 1.4.0

### Enhancements

Automatic generation key implementation, including

1. [ISSUE #162](https://github.com/sharding-sphere/sharding-sphere/issues/162) Implementation of distributed primary key algorithm
1. [ISSUE #163](https://github.com/sharding-sphere/sharding-sphere/issues/163) Acquisition of a self increasing sequence of JDBC interfaces
1. [ISSUE #171](https://github.com/sharding-sphere/sharding-sphere/issues/171) Sharding-jdbc-core coordination automatic generation sequence transformation
1. [ISSUE #172](https://github.com/sharding-sphere/sharding-sphere/issues/172) The configuration of YAML and Spring supports the self increasing sequence

### Bug Fixes

1. [ISSUE #176](https://github.com/sharding-sphere/sharding-sphere/issues/176) The wasNull flag of AbstractMemoryResultSet is not reset in time

## 1.3.3

### Enhancements

1. [ISSUE #59](https://github.com/sharding-sphere/sharding-sphere/issues/59) PreparedStatement can call the correct underlying set method according to the parameter type when setting parameters

### Bug Fixes

1. [ISSUE #149](https://github.com/sharding-sphere/sharding-sphere/issues/149) When INSERT IGNORE INTO, if the data is duplicated, the value returned to -1 when ignored, and it should be returned to 0
1. [ISSUE #118](https://github.com/sharding-sphere/sharding-sphere/issues/118) In the same thread, DQL is executed first, then DML is executed, and DML operation is executed from the slave database
1. [ISSUE #122](https://github.com/sharding-sphere/sharding-sphere/issues/122) In cases where connections are not available (such as network interruption), transactions should be interrupted rather than retry
1. [ISSUE #152](https://github.com/sharding-sphere/sharding-sphere/issues/152) PreparedStatement's cache causes an array out of bound
1. [ISSUE #150](https://github.com/sharding-sphere/sharding-sphere/issues/150) With the latest SQLServer jdbc driver compatibility problem, Product Name should be changed from SQLServer to Microsoft SQL Server
1. [ISSUE #166](https://github.com/sharding-sphere/sharding-sphere/issues/166) Druid data source stat filter multi-thread error reporting should be added to database connection level synchronization

## 1.3.2

### Enhancements

1. [ISSUE #79](https://github.com/sharding-sphere/sharding-sphere/issues/79) Optimizes limit for only one target table, does not modify limit offset

### Bug Fixes

1. [ISSUE #36](https://github.com/sharding-sphere/sharding-sphere/issues/36) ShardingPreparedStatement cannot set parameters repeatedly
1. [ISSUE #114](https://github.com/sharding-sphere/sharding-sphere/issues/114) When ShardingPreparedStatement performs batch tasks, it repeatedly analyzes SQL and leads to OOM
1. [ISSUE #33](https://github.com/sharding-sphere/sharding-sphere/issues/33) According to the MySQL document, queries similar to limit 100 and -1 format are not supported
1. [ISSUE #124](https://github.com/sharding-sphere/sharding-sphere/issues/124) The return value of com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractStatementAdapter.getUpdateCount does not conform to the JDBC specification
1. [ISSUE #141](https://github.com/sharding-sphere/sharding-sphere/issues/141) Multi-thread executor parameter setting failure


## 1.3.1

### Enhancements

1. [ISSUE #91](https://github.com/sharding-sphere/sharding-sphere/issues/91) Open support for Statement.getGeneratedKeys can return the original database self increase primary key
1. [ISSUE #92](https://github.com/sharding-sphere/sharding-sphere/issues/92) Query DQL statement event sending

### Bug Fixes

1. [ISSUE #89](https://github.com/sharding-sphere/sharding-sphere/issues/89) Use read-write separation with sharding hint leads to conflict
1. [ISSUE #95](https://github.com/sharding-sphere/sharding-sphere/issues/95) Write operations in the same thread are read from the master database changed to the same thread and within the same connection

## 1.3.0

### New Features

1. [ISSUE #85](https://github.com/sharding-sphere/sharding-sphere/issues/85) Read-write separation

### Enhancements

1. [ISSUE #82](https://github.com/sharding-sphere/sharding-sphere/issues/82) TableRule can import the dataSourceName attribute to specify the data source corresponding to the TableRule
1. [ISSUE #88](https://github.com/sharding-sphere/sharding-sphere/issues/88) Release restrictions on other databases, support standard SQL, do not support personalized paging statements

### Bug Fixes

1. [ISSUE #81](https://github.com/sharding-sphere/sharding-sphere/issues/81) Associative table query uses OR query condition to resolve the result exceptions

## 1.2.1

### Refactor

1. [ISSUE #60](https://github.com/sharding-sphere/sharding-sphere/issues/60) API adjust, remove ShardingDataSource, use factory instead
1. [ISSUE #76](https://github.com/sharding-sphere/sharding-sphere/issues/76) ShardingRule and TableRule change to Builder pattern
1. [ISSUE #77](https://github.com/sharding-sphere/sharding-sphere/issues/77) ShardingRule and TableRule change to Builder pattern

### Enhancements

1. [ISSUE #61](https://github.com/sharding-sphere/sharding-sphere/issues/61) Add the logical table name to the ShardingValue class
1. [ISSUE #66](https://github.com/sharding-sphere/sharding-sphere/issues/66) Statement on the JDBC tier supports get/set MaxFieldSize, MaxRows and QueryTimeout
1. [ISSUE #72](https://github.com/sharding-sphere/sharding-sphere/issues/72) Batch inserts supporting select union all
1. [ISSUE #78](https://github.com/sharding-sphere/sharding-sphere/issues/78) Simplifying sharding only configuration, without configuring logical table and real table correspondence
1. [ISSUE #80](https://github.com/sharding-sphere/sharding-sphere/issues/80) Simplifying the configuration that does not sharding, specifying the default data source, do not need configure TableRule

### Bug Fixes

1. [ISSUE #63](https://github.com/sharding-sphere/sharding-sphere/issues/63) No table name or table alias is added to the ORDER BY and GROUP BY derivation columns
1. [ISSUE #65](https://github.com/sharding-sphere/sharding-sphere/issues/65) Performance enhancement for parsing condition context
1. [ISSUE #67](https://github.com/sharding-sphere/sharding-sphere/issues/67) The soft transaction log cannot be deleted when routed to multiple tables
1. [ISSUE #71](https://github.com/sharding-sphere/sharding-sphere/issues/71) Routing single sharding key by OFFSET of LIMIT error
1. [ISSUE #75](https://github.com/sharding-sphere/sharding-sphere/issues/75) MemoryTransactionLogStorage retry times update concurrency problem

## 1.2.0

### New Features

1. [ISSUE #53](https://github.com/sharding-sphere/sharding-sphere/issues/53) The relationship between the real table and the logical table is not configured, and the real table is dynamically calculated by the sharding algorithm
1. [ISSUE #58](https://github.com/sharding-sphere/sharding-sphere/issues/58) Soft transaction: the initial version of the best effort type

### Refactor

1. [ISSUE #49](https://github.com/sharding-sphere/sharding-sphere/issues/49) Adjust the property configuration
1. [ISSUE #51](https://github.com/sharding-sphere/sharding-sphere/issues/51) Refactor of Hint interface

### Bug Fixes

1. [ISSUE #43](https://github.com/sharding-sphere/sharding-sphere/issues/43) The yaml file contains Chinese, and the operating system mode is not UTF-8 encoding, resulting in yaml can not be parsed
1. [ISSUE #48](https://github.com/sharding-sphere/sharding-sphere/issues/48) Yaml file is not closed after reading
1. [ISSUE #57](https://github.com/sharding-sphere/sharding-sphere/issues/57) At the analytic level, we can identify sub queries to ensure that the behavior of supplementary columns can be accurately positioned

## 1.1.0

### New Features

1. [ISSUE #40](https://github.com/sharding-sphere/sharding-sphere/issues/40) Support YAML configuration
1. [ISSUE #41](https://github.com/sharding-sphere/sharding-sphere/issues/41) Support Spring namespace configuration
1. [ISSUE #42](https://github.com/sharding-sphere/sharding-sphere/issues/42) Support inline expression configuration

### Bug Fixes

1. [ISSUE #25](https://github.com/sharding-sphere/sharding-sphere/issues/25) The problem of repeated results will appear under the OR expression

## 1.0.1

### Enhancements

1. [ISSUE #39](https://github.com/sharding-sphere/sharding-sphere/issues/39) Support the use of Hint method to register the key value to SQL routing

### Bug Fixes

1. [ISSUE #11](https://github.com/sharding-sphere/sharding-sphere/issues/11) The count function returns incorrectly without aliases
1. [ISSUE #13](https://github.com/sharding-sphere/sharding-sphere/issues/13) The Insert statement does not write column names or write column names but column names do not contain sharding fields, occur broadcast route
1. [ISSUE #16](https://github.com/sharding-sphere/sharding-sphere/issues/16) For now a new connection pool is executed every time SQL is executed. Instead, each ShardingDataSource object should be changed to share a pool
1. [ISSUE #18](https://github.com/sharding-sphere/sharding-sphere/issues/18) When query Count, getObject() throws Exception: Unsupported data type: Object
1. [ISSUE #19](https://github.com/sharding-sphere/sharding-sphere/issues/19) In SUM and AVG functions, merger is not executed if aliases are not added, and null pointer exception fired if aliases are added
1. [ISSUE #38](https://github.com/sharding-sphere/sharding-sphere/issues/38) The compatibility between JPA and Sharding-JDBC. JPA automatically add the column aliases of SELECT, resulting in ORDER BY obtaining ResultSet data only by aliases rather than column names

## 1.0.0

1. Initial version
