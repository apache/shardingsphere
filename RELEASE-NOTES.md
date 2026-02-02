## Release 5.5.4-SNAPSHOT

### Enhancements

1. Enhance MySQLTextResultSetRowPacket and MySQLDateBinaryProtocolValue to support LocalDateTime and LocalTime when value contains scale - [#37881](https://github.com/apache/shardingsphere/pull/37881)

### Bug Fixes

1. SQL Parser: Support Oracle SQL parser correctly extract REGEXP_SUBSTR parameters - [#37924](https://github.com/apache/shardingsphere/pull/37924)

## Release 5.5.3

### CVE

1. Fix CVE-2025-55163, CVE-2025-58056, CVE-2025-58057 [#36758](https://github.com/apache/shardingsphere/pull/36758)
1. Fix CVE-2025-48924 [#36085](https://github.com/apache/shardingsphere/pull/36085)
1. Fix CVE-2024-7254 [#36153](https://github.com/apache/shardingsphere/pull/36153)
1. Fix CVE-2015-5237, CVE-2024-7254, CVE-2022-3171, CVE-2021-22569, CVE-2021-22570 [#37888](https://github.com/apache/shardingsphere/pull/37888)
1. Fix CVE-2024-12798, CVE-2024-12801, CVE-2025-11226 [#37936](https://github.com/apache/shardingsphere/pull/37936)

### Metadata Storage Changes

1. Remove `default_strategies` prefix on sharding rule metadata persist - [#34664](https://github.com/apache/shardingsphere/pull/34664)

### API Changes

1. Remove SQL formatting feature - [#35390](https://github.com/apache/shardingsphere/pull/35390)
1. Remove logging rule feature - [#35458](https://github.com/apache/shardingsphere/pull/35458)
1. Remove configuration property key `system-log-level` - [#35493](https://github.com/apache/shardingsphere/pull/35493)
1. Change ShardingSphere SQL log topic from `ShardingSphere-SQL` to `org.apache.shardingsphere.sql` - [#37022](https://github.com/apache/shardingsphere/pull/37022)
1. Add temporary config key `instance-connection-enabled` - [#37694](https://github.com/apache/shardingsphere/pull/37694)

### New Features

1. Add ShardingSphere BOM - [#36866](https://github.com/apache/shardingsphere/issues/36866)
1. Decouple feature modules as pluggable - [#36086](https://github.com/apache/shardingsphere/pull/36086)
1. Decouple database types as pluggable - [#35346](https://github.com/apache/shardingsphere/pull/35346)
1. Decouple registry center types as pluggable - [#36087](https://github.com/apache/shardingsphere/pull/36087)
1. Proxy: Support Firebird Proxy - [#35937](https://github.com/apache/shardingsphere/pull/35937)
1. JDBC: Support ZooKeeper and ETCD URL format - [#37037](https://github.com/apache/shardingsphere/pull/37037)

### Enhancements

1. Build: Support compiling and using ShardingSphere under OpenJDK 24 and 25 - [#36688](https://github.com/apache/shardingsphere/issues/36688)
1. Metadata: Support IPv6 for JDBC connection URL - [#35289](https://github.com/apache/shardingsphere/issues/35289)
1. Metadata: Support to load partition tables for PostgreSQL - [#34346](https://github.com/apache/shardingsphere/pull/34346)
1. Metadata: Support for connecting to Presto's Memory Connector - [#34432](https://github.com/apache/shardingsphere/pull/34432)
1. Kernel: Add LocalDate data type support - [#37053](https://github.com/apache/shardingsphere/pull/37053)
1. SQL Parser: Support to parse stored procedure syntax for MySQL - [#36690](https://github.com/apache/shardingsphere/pull/36690)
1. SQL Parser: Support to parse more SQL statements for MySQL - [#36689](https://github.com/apache/shardingsphere/issues/36689)
1. SQL Parser: Support to parse more SQL statements for SQLServer - [#36695](https://github.com/apache/shardingsphere/issues/36695)
1. SQL Parser: Support to parse more SQL statements for Oracle - [#36696](https://github.com/apache/shardingsphere/issues/36696)
1. SQL Parser: Support to parse more SQL statements for Hive - [#36694](https://github.com/apache/shardingsphere/pull/36694) [#37074](https://github.com/apache/shardingsphere/pull/37074)
1. SQL Parser: Support to parse CREATE MATERIALIZED VIEW for Doris - [#31499](https://github.com/apache/shardingsphere/pull/31499)
1. SQL Parser: Support to parse CREATE FUNCTION with $$ symbol for PostgreSQL and openGauss - [#35947](https://github.com/apache/shardingsphere/pull/35947)
1. SQL Binder: Support to bind more SQL statements - [#36697](https://github.com/apache/shardingsphere/pull/36697)
1. SQL Binder: Add ALTER TABLE metadata check - [#35877](https://github.com/apache/shardingsphere/pull/35877)
1. SQL Router: Add SELECT with UNION ALL routing to multi data sources check - [#35037](https://github.com/apache/shardingsphere/pull/35037)
1. SQL Router: Improve support for executing tableless SQL with single data source - [#35659](https://github.com/apache/shardingsphere/pull/35659)
1. SQL Router: Add `max-union-size-per-datasource` property to batch UNION ALL rewrite per data source and keep parallel execution - [#37405](https://github.com/apache/shardingsphere/pull/37405)
1. DistSQL: Add job sharding nodes info to the query results of `SHOW MIGRATION LIST` - [#35053](https://github.com/apache/shardingsphere/pull/35053)
1. DistSQL: Add DistSQL for query storage units which used in single rule - [#35131](https://github.com/apache/shardingsphere/pull/35131)
1. Proxy: Implement write bool binary data type for PostgreSQL protocol - [#35831](https://github.com/apache/shardingsphere/pull/35831)
1. Proxy: Add authority check on SQL `SHOW CREATE DATABASE` for MySQL - [#36862](https://github.com/apache/shardingsphere/pull/36862)
1. Sharding: Using cache to avoid memory overflow from inline expression parsing - [#22196](https://github.com/apache/shardingsphere/issues/22196)
1. Sharding: Add digital suffix check with binding table names - [#35293](https://github.com/apache/shardingsphere/issues/35293)
1. Encrypt: Use EncryptDerivedColumnSuffix to enhance encrypt table subquery rewrite logic - [#34829](https://github.com/apache/shardingsphere/pull/34829)
1. Encrypt: Add quotes to encrypt rewrite derived columns - [#34950](https://github.com/apache/shardingsphere/pull/34950)
1. Encrypt: Support NOT LIKE operator in encryption feature - [#35984](https://github.com/apache/shardingsphere/pull/35984)
1. Readwrite-splitting: Update transactional read query strategy default type as PRIMARY - [#36477](https://github.com/apache/shardingsphere/pull/36477)
1. Pipeline: Improve pipeline job progress incremental latestActiveTimeMillis persistence - [#34655](https://github.com/apache/shardingsphere/pull/34655)
1. Pipeline: Improve heterogeneous databases migration - [#35424](https://github.com/apache/shardingsphere/pull/35424)
1. Pipeline: Improve DATA_MATCH data consistency check to use range streaming query - [#35740](https://github.com/apache/shardingsphere/pull/35740)
1. Pipeline: Add streaming-range-type property and LARGE impl in DATA_MATCH consistency check on MySQL sharding table for better performance - [#35786](https://github.com/apache/shardingsphere/pull/35786)
1. Pipeline: Improve migration consistency check to reflect storage unit maxPoolSize dynamically - [#36507](https://github.com/apache/shardingsphere/pull/36507)
1. Pipeline: Improve "show migration check status" inventory_finished_percentage column inaccurate result - [#36509](https://github.com/apache/shardingsphere/pull/36509)
1. Pipeline: Compatible with COMPLEX_INLINE allow-range-query-with-inline-sharding - [#36524](https://github.com/apache/shardingsphere/pull/36524)
1. Pipeline: Support pipeline job realtime reflection on proxy global properties after restarting - [#36749](https://github.com/apache/shardingsphere/pull/36749)
1. Pipeline: InventoryDumper reuse table inventory calculator for better function and performance - [#36830](https://github.com/apache/shardingsphere/pull/36830)
1. Pipeline: Improve "alter transmission rule": verify STREAM_CHANNEL TYPE NAME - [#36864](https://github.com/apache/shardingsphere/pull/36864)
1. Pipeline: Support multi-columns unique key first integer column splitting - [#36935](https://github.com/apache/shardingsphere/pull/36935)
1. Pipeline: Support unique key first integer column exact splitting - [#37517](https://github.com/apache/shardingsphere/pull/37517)
1. Pipeline: Support unique key first integer column possible null value - [#37522](https://github.com/apache/shardingsphere/pull/37522)
1. Pipeline: Support unique key first integer column exact or estimated splitting based on data sparseness - [#37542](https://github.com/apache/shardingsphere/pull/37542)
1. Pipeline: Support unique key first big integer column splitting - [#37574](https://github.com/apache/shardingsphere/pull/37574)
1. Pipeline: Support unique key first string column exact splitting - [#37543](https://github.com/apache/shardingsphere/pull/37543)
1. Pipeline: Support multi-columns unique key non-first column nullable - [#37647](https://github.com/apache/shardingsphere/pull/37647)
1. Encrypt: Support handling show create view result decoration in encrypt - [#37299](https://github.com/apache/shardingsphere/pull/37299)
1. JDBC: Enhance ResultSetUtils to support flexible string date/time conversions - [37424](https://github.com/apache/shardingsphere/pull/37424)
1. SQL Binder: Support Grant statement SQL bind - [#36207](https://github.com/apache/shardingsphere/pull/36207)

### Bug Fixes

1. Kernel: Fix not return generate key when set null in INSERT statement - [35783](https://github.com/apache/shardingsphere/pull/35783)
1. Kernel: Add AllowNotUseDatabaseSQLStatementAttribute to support some mysql dal statement execute without use database - [#37033](https://github.com/apache/shardingsphere/pull/37033)
1. Metadata: Fix the exception to rename schema name when executing ALTER SCHEMA - [#34465](https://github.com/apache/shardingsphere/pull/34465)
1. SQL Parser: Support multiple column names with pivot and unpivot clause - [35586](https://github.com/apache/shardingsphere/pull/35586)
1. SQL Parser: Fix set OnDuplicateKeyColumnsSegment on INSERT for PostgreSQL - [#34425](https://github.com/apache/shardingsphere/pull/34425)
1. SQL Parser: Fix SQL parser error when SQL contains implicit concat expression for MySQL - [#34660](https://github.com/apache/shardingsphere/pull/34660)
1. SQL Parser: Fix SQL parser error when SQL contains subquery with alias for Oracle - [#35239](https://github.com/apache/shardingsphere/pull/35239)
1. SQL Parser: Fix multiple SQLs split error when comma contained - [#31609](https://github.com/apache/shardingsphere/pull/31609)
1. SQL Binder: Fix unable to find the outer table in the NotExpressionBinder - [36135](https://github.com/apache/shardingsphere/pull/36135)
1. SQL Binder: Fix unable to find the outer table in the ExistsSubqueryExpressionBinder - [#36068](https://github.com/apache/shardingsphere/pull/36068)
1. SQL Binder: Fix column bind exception caused by oracle XMLELEMENT function first parameter without quote - [#36963](https://github.com/apache/shardingsphere/pull/36963)
1. SQL Binder: Fix correlated subquery in HAVING cannot reference outer query column - [#37872](https://github.com/apache/shardingsphere/pull/37872)
1. Transaction: Fix conflicting dependencies of BASE transaction integration module - [#35142](https://github.com/apache/shardingsphere/pull/35142)
1. Transaction: Alleviate connection leaks caused by SEATA client throwing exceptions - [#34463](https://github.com/apache/shardingsphere/pull/34463)
1. SQL Federation: Fix Operation not allowed after ResultSet closed exception when use SQL federation - [#35206](https://github.com/apache/shardingsphere/pull/35206)
1. DistSQL: Fix duplicate result when show rules used storage unit with readwrite-splitting rule - [#35129](https://github.com/apache/shardingsphere/pull/35129)
1. DistSQL: Fix missing comma in `ALTER SQL_FEDERATION RULE` - [#35523](https://github.com/apache/shardingsphere/pull/35523)
1. JDBC: Fix some wrong JDBC database metadata retrieve logics - [#34959](https://github.com/apache/shardingsphere/pull/34959)
1. JDBC: Fix getting database name from SQL statement context - [#34960](https://github.com/apache/shardingsphere/pull/34960)
1. JDBC: Support set data source properties type with java.time.Duration - [#35241](https://github.com/apache/shardingsphere/pull/35241)
1. JDBC: Resolve statement manager leaks when creating multiple statements - [#35665](https://github.com/apache/shardingsphere/pull/35665)
1. JDBC: Fix the issue where cached connections in DriverDatabaseConnectionManager were not released in time - [35834](https://github.com/apache/shardingsphere/pull/35834)
1. JDBC: Clear batch generated keys result set when call clearBatch method - [37204](https://github.com/apache/shardingsphere/pull/37204)
1. JDBC: Fix Oracle TIMESTAMP WITH TIME ZONE ORDER BY exception - [#37181](https://github.com/apache/shardingsphere/pull/37181)
1. Proxy: Fix `SHOW PROCESSLIST` not wait for all nodes - [#35348](https://github.com/apache/shardingsphere/pull/35348)
1. Proxy: Fix NoSuchElementException exception when execute MySQL SHOW VARIABLES without current database - [#35550](https://github.com/apache/shardingsphere/pull/35550)
1. Proxy: Fix column length for PostgreSQL string binary protocol value - [35840](https://github.com/apache/shardingsphere/pull/35840)
1. Proxy: Fix the connection leak caused by rollback failure in Proxy - [35867](https://github.com/apache/shardingsphere/pull/35867)
1. Proxy: Fix the behavior difference of select built-in function names with spaces -[#36537](https://github.com/apache/shardingsphere/pull/36537)
1. Proxy: Fix MySQL text protocol datetime fractional seconds output - [#37410](https://github.com/apache/shardingsphere/pull/37410)
1. Proxy: Fix IndexOutOfBoundsException for MySQL no-FROM multi-projection SELECT routed to admin path - [#37391](https://github.com/apache/shardingsphere/pull/37391)
1. Proxy: Fix MySQL binary protocol datetime/time fractional seconds precision - [#37294](https://github.com/apache/shardingsphere/pull/37294)
1. Proxy: Fix PostgreSQL boolean text output to return `t`/`f` as per protocol - [#37184](https://github.com/apache/shardingsphere/pull/37184)
1. Proxy: Fix PostgreSQL text protocol bytea output to use hex encoding and avoid truncation - [#37772](https://github.com/apache/shardingsphere/pull/37772)
1. Mode: Fix issue of drop schema can not work on standalone mode - [#34470](https://github.com/apache/shardingsphere/pull/34470)
1. Encrypt: Resolve rewrite issue in nested concat function - [#35815](https://github.com/apache/shardingsphere/pull/35815)
1. Sharding: Fix mod sharding algorithm judgement -[#36386](https://github.com/apache/shardingsphere/pull/36386)
1. Sharding: Fix check inline sharding algorithms in table rules - [#36999](https://github.com/apache/shardingsphere/pull/36999)
1. Sharding: Fix wrong sharding condition merge when sharding column in case-sensitive - [#37389](https://github.com/apache/shardingsphere/pull/37389)
1. Sharding: Fix wrong merge sharding condition logic caused by different type sharding value - [#37528](https://github.com/apache/shardingsphere/pull/37528)
1. Sharding: Prevent SELECT DISTINCT rewrite from losing global de-duplication across shards - [#37857](https://github.com/apache/shardingsphere/pull/37857)
1. Pipeline: Recover value of migration incremental importer batch size - [#34670](https://github.com/apache/shardingsphere/pull/34670)
1. Pipeline: Fix InventoryDumper first time dump SQL without ORDER BY on multiple columns unique key table - [#34736](https://github.com/apache/shardingsphere/pull/34736)
1. Pipeline: Fix MySQL JDBC query properties extension when SSL is required on server - [#36581](https://github.com/apache/shardingsphere/pull/36581)
1. Pipeline: Fix migration might skip some records on big table after job restarting - [#36878](https://github.com/apache/shardingsphere/pull/36878)
1. Pipeline: Fix unsigned number column value type inconsistent in inventory and incremental - [#37280](https://github.com/apache/shardingsphere/pull/37280)
1. Pipeline: Fix PostgreSQL migration create table SQL generation failure caused by locale-formatted sequence values - [#28360](https://github.com/apache/shardingsphere/issues/28360)
1. Pipeline: MySQLBinlogClient compatible with async exception - [#37631](https://github.com/apache/shardingsphere/issues/37631)
1. Pipeline: Fix SHOW MIGRATION RULE always display default values when ALTER MIGRATION RULE without STREAM_CHANNEL - [#37737](https://github.com/apache/shardingsphere/issues/37737)
1. DistSQL: Fix load single table with specific schema - [#37535](https://github.com/apache/shardingsphere/pull/37535)
1. Transaction: Fix XA data source enlist failure caused connection leaks - [37593](https://github.com/apache/shardingsphere/pull/37593)
1. Proxy: Fix command type error when use openGauss driver to execute statements in transaction - [37749](https://github.com/apache/shardingsphere/pull/37749)

### Change Logs

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/31)

## Release 5.5.2

### API Changes

### New Features

1. Firebird: Add Firebird SQL parser module and database type - [#33773](https://github.com/apache/shardingsphere/pull/33773)

### Enhancements

1. Metadata: Support table metadata loading concurrently - [#34009](https://github.com/apache/shardingsphere/pull/34009)
1. Metadata: Support setting `hive_conf_list`, `hive_var_list` and `sess_var_list` for jdbcURL when connecting to HiveServer2 - [#33749](https://github.com/apache/shardingsphere/pull/33749)
1. Metadata: Support connecting to HiveServer2 through database connection pools other than HikariCP - [#33762](https://github.com/apache/shardingsphere/pull/33762)
1. Metadata: Partial support for connecting to embedded ClickHouse `chDB` - [#33786](https://github.com/apache/shardingsphere/pull/33786)
1. SQL Parser: Support to parse more SQL statements for MySQL - [#36701](https://github.com/apache/shardingsphere/pull/36701)
1. SQL Parser: Support to parse more SQL statements for Doris - [#36700](https://github.com/apache/shardingsphere/pull/36700)
1. SQL Parser: Enhance CREATE VIEW, ALTER VIEW, DROP VIEW SQL parser - [#34283](https://github.com/apache/shardingsphere/pull/34283)
1. SQL Binder: Significantly enhance SQL binding capability - [#36702](https://github.com/apache/shardingsphere/issues/36702)
1. DistSQL: Check inline expression when create sharding table rule with inline sharding algorithm - [#33735](https://github.com/apache/shardingsphere/pull/33735)
1. Transaction: Support savepoint/release savepoint TCL statements in JDBC -[#34173](https://github.com/apache/shardingsphere/pull/34173)
1. JDBC: Show database name for JDBC when execute SHOW COMPUTE NODES - [#33437](https://github.com/apache/shardingsphere/pull/33437)
1. JDBC: Support ZonedDateTime on ResultSet - [#33660](https://github.com/apache/shardingsphere/issues/33660)
1. Proxy: Add query parameters and check for MySQL kill processId - [#33274](https://github.com/apache/shardingsphere/pull/33274)
1. Proxy: Support table not exist exception for PostgreSQL proxy - [#33885](https://github.com/apache/shardingsphere/pull/33274)
1. Proxy Native: Change the Base Docker Image of ShardingSphere Proxy Native - [#33263](https://github.com/apache/shardingsphere/issues/33263)
1. Proxy Native: Support connecting to HiveServer2 with ZooKeeper Service Discovery enabled in GraalVM Native Image - [#33768](https://github.com/apache/shardingsphere/pull/33768)
1. Proxy Native: Support local transactions of ClickHouse under GraalVM Native Image - [#33801](https://github.com/apache/shardingsphere/pull/33801)
1. Proxy Native: Support Seata AT integration under Proxy Native in GraalVM Native Image - [#33889](https://github.com/apache/shardingsphere/pull/33889)
1. Mode: Support modifying Hikari-CP configurations via props in standalone mode [#34185](https://github.com/apache/shardingsphere/pull/34185)
1. Agent: Simplify the usage of Agent's Docker Image - [#33356](https://github.com/apache/shardingsphere/pull/33356)
1. Sharding: Support GroupConcat function for aggregating multiple shards in MySQL, OpenGauss, Doris - [#33808](https://github.com/apache/shardingsphere/pull/33808)
1. Encrypt: Add non-support checker for WITH, COMBINE, INSERT SELECT on encrypt feature  - [#34175](https://github.com/apache/shardingsphere/pull/34175)
1. Encrypt: Support INSERT statement rewrite use quote [#34259](https://github.com/apache/shardingsphere/pull/34259)
1. SQL Binder: Support Flush statement SQL bind - [#36036](https://github.com/apache/shardingsphere/pull/36036)

### Bug Fixes

1. Metadata: Fix cannot connect to HiveServer2 using remote Hive Metastore Server - [#33837](https://github.com/apache/shardingsphere/pull/33837)
1. SQL Parser: Fix LiteralExpressionSegment cast exception in SQL parser - [#33332](https://github.com/apache/shardingsphere/pull/33332)
1. SQL Parser: Fix time extract function parse week and quarter error for PostgreSQL and openGauss - [#33564](https://github.com/apache/shardingsphere/pull/33564)
1. SQL Parser: Fix parse zone unreserved keyword error for MySQL - [#33720](https://github.com/apache/shardingsphere/pull/33720)
1. SQL Parser: Fix range parse error when use table owner for MySQL - [#33874](https://github.com/apache/shardingsphere/pull/33874)
1. SQL Parser: Fix the issue WHERE JOIN conditions cannot be extracted when more than two conditions are used - [#34707](https://github.com/apache/shardingsphere/pull/34707)
1. SQL Binder: Fix table does not exist exception when use HintManager#setDatabaseName to transparent - [#33370](https://github.com/apache/shardingsphere/pull/33370)
1. SQL Binder: Use Multimap and CaseInsensitiveString to replace CaseInsensitiveMap for supporting MySQL multi table join with same table alias - [#33303](https://github.com/apache/shardingsphere/pull/33303)
1. SQL Binder: Fix the combine statement cannot find the outer table when bind - [#33357](https://github.com/apache/shardingsphere/pull/33357)
1. SQL Binder: Fix SQL performance issues caused by repeated subquery fetches - [#33361](https://github.com/apache/shardingsphere/pull/33361)
1. SQL Binder: Fix the expression segment cannot find the outer table when binding - [#34015](https://github.com/apache/shardingsphere/pull/34015)
1. Proxy: Fix BatchUpdateException when execute INSERT INTO ON DUPLICATE KEY UPDATE in proxy adapter - [#33796](https://github.com/apache/shardingsphere/pull/33796)
1. Proxy: Fix "ALL PRIVILEGES ON `DB`.*" is not recognized during SELECT privilege verification for MySQL - [#34037](https://github.com/apache/shardingsphere/pull/34037)
1. Proxy: Fix MySQL longblob wrong column type returned by proxy protocol - [#34121](https://github.com/apache/shardingsphere/pull/34121)
1. Proxy: Fix MySQL proxy error if insert SQL contains more parameters not in insert values syntax - [#34287](https://github.com/apache/shardingsphere/pull/34287)
1. Mode: Fix `JDBCRepository` improper handling of H2-database in memory mode - [#33281](https://github.com/apache/shardingsphere/issues/33281)
1. Mode: Fix duplicate column names added when index changed in DDL - [#33982](https://github.com/apache/shardingsphere/issues/33281)
1. Sharding: Remove ShardingRouteAlgorithmException check logic temporarily to support different actual table name configuration - [#33367](https://github.com/apache/shardingsphere/pull/33367)
1. Sharding: Fix SQL COUNT with GROUP BY to prevent incorrect row returns - [#33380](https://github.com/apache/shardingsphere/pull/33380)
1. Sharding: Fix avg, sum, min, max function return empty data when no query result return - [#33449](https://github.com/apache/shardingsphere/pull/33449)
1. Encrypt: Fix merge exception without encrypt rule in database - [#33708](https://github.com/apache/shardingsphere/pull/33708)
1. Encrypt: Use SQL bind info in EncryptInsertPredicateColumnTokenGenerator to avoid wrong column table mapping - [#34110](https://github.com/apache/shardingsphere/pull/34110)

### Change Logs

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/30)

## Release 5.5.1

### API Changes

1. Authority: Mark privilege provider ALL_PERMITTED as deprecated and will be removed in future
1. DistSQL: Remove optional param usageCount from show storage units
1. Readwrite-splitting: Change dataSources to dataSourceGroups for YAML

### New Features

1. SQL Parser: Add Doris, Hive and Presto SQL parser module and database type

### Enhancements

1. Kernel: Support SQL hint extract when SQL contains dbeaver hint comment
1. Kernel: Add extract COMBINE LEFT SELECT in extractFromSelectStatementWithoutProjection method
1. Metadata: Collect table type for PostgreSQL
1. Metadata: Support CHARACTER VARYING type metadata load for PostgreSQL and openGauss - [#34221](https://github.com/apache/shardingsphere/pull/34221)
1. DistSQL: Check privilege when registering or altering storage unit
1. DistSQL: Check duplicate actual data nodes when creating or altering sharding table rule
1. DistSQL: Add like support for show storage units
1. DistSQL: Rollback if import database configuration failed
1. DistSQL: add table type to result set of show logical tables
1. Proxy: Make the results of show tables in order
1. Proxy: Trigger metadata collection when creating and deleting database and table
1. Proxy: Optimize Agent to support collecting metrics data from multiple data sources when used with Driver
1. SQL Parser: Support to parse more SQL statements for MySQL - [#36703](https://github.com/apache/shardingsphere/issues/36703)
1. SQL Parser: Support to parse more SQL statements for PostgreSQL - [#36704](https://github.com/apache/shardingsphere/issues/36704)
1. SQL Parser: Support to parse more SQL statements for Oracle - [#36705](https://github.com/apache/shardingsphere/issues/36705)
1. SQL Federation: Support SQL federation bit_count function for MySQL
1. SQL Federation: Improve atan and anan2 SQL function for MySQL
1. SQL Federation: Support federated query bin function for MySQL
1. SQL Federation: Support for federated query NOT operator
1. Transaction: Support for switching transaction types
1. Transaction: Use same transaction type in one transaction in JDBC
1. Proxy Native: Add more graalvm reachability metadata for caffeine cache
1. Mode: Change shadow、sharding's algorithms node path to shadow_algorithms and sharding_algorithms node path
1. Sharding: Revise all local index for sharding table and add object uniqueness level spi to control index token generator
1. Sharding: Add NullsOrderType.LOW and NullsOrderType.HIGH to handle NULL order by in sharding feature
1. Sharding: Add inline sharding algorithms match actual data nodes check
1. Sharding: Optimize sharding table index name rewriting rules and remove unnecessary suffix rewriting - [#31171](https://github.com/apache/shardingsphere/issues/31171)
1. Encrypt: Add unsupported check for combine statement with encrypt columns
1. Encrypt: Support select distinct(column) encrypt rewrite and refactor SubstitutableColumnNameToken build logic
1. Encrypt: Support like concat nested concat statement rewrite with encrypt feature
1. Pipeline: Add SHARDING_TOTAL_COUNT impl of JobExecutorServiceHandler to improve CPU core requirement
1. Pipeline: Support page query for inventory dumper and data consistency streaming query
1. Pipeline: Use case-insensitive identifiers to enhance the table metadata loader
1. Pipeline: Support primary key columns ordering for standard pipeline table metadata loader

### Bug Fixes

1. Kernel: Replace ThreadLocal with TransmittableThreadLocal in HintManager to support cross Thread usage
1. Kernel: Use ConnectionPropertiesParser spi to parse standardJdbcUrl to solve oracle url parse error
1. Kernel: Fix column count assert exception caused by postgresql system table judge
1. Metadata: Keep in-used storage node when unregister storage unit
1. Metadata: Fix the generated key column name case insensitivity error
1. SQL Parser: Support PostgreSQL do nothing conflict action
1. SQL Parser: Fix oracle nvl function and interval hour parse error
1. DistSQL: Fix load single table error after creating logical data source
1. DistSQL: Fix set default single table storage unit to random failed
1. DistSQL: Fix set default single table storage unit to logical data source failed
1. DistSQL: Fix NPE when import metadata
1. Fix the problem of missing storage unit when registering storage unit
1. SQL Binder: Add TableAvailable interface for CloseStatementContext, MoveStatementContext, FetchStatementContext and fix SQL rewrite test case
1. SQL Federation: Fix federated query LocalDateTime conversion
1. SQL Federation: Fix push down SQL execute error when sql contains chinese character with SQL federation
1. SQL Federation: Fix federation query binary type data query
1. SQL Federation: Fix null result in federated query for a single projected column
1. SQL Federation: Fix SQL federation unknown type exception caused by calcite wrong result type with bigint
1. Transaction: Fix savepoint manager not cleaned up in distributed transactions
1. Transaction: Fix PostgreSQL rollback only
1. Transaction: Fix transaction context not cleaned up when xa transaction is committed
1. Transaction: Fix setSavepoint method invocation not cleaning up
1. Transaction: Fix the issue that cursor is not rewritten
1. Proxy: Support binary type bool value in PostgreSQL
1. Proxy: Support array type prepared param in PostgreSQL
1. Proxy: Support binary protocol value for text array in PostgreSQL
1. Proxy: Fix duplicate results when querying information_schema.SCHEMATA
1. Proxy: Fix incorrect results for querying information_schema.SCHEMATA
1. Proxy: Fix NPE when execute show table status
1. Proxy: Fix no database selected exception occurs when show tables from database
1. Proxy: Fix the error that the process does not exit after proxy startup fail
1. Proxy: Fix the error that the persisted system metadata was not cleared after the database was deleted in the PostgreSQL scenario
1. Proxy: Fix no database selected exception in the query SQL
1. JDBC: Fix the NPE when it does not contain a specified logic database in Driver
1. Sharding: Fix Sharding column not tracked through aliases
1. Sharding: Fix alter view exception when config sharding rule and binding table rule
1. Sharding: Fix is need accumulate logic
1. Sharding: Fix NOT IN condition routing being treated as IN causing missing shards - [#32076](https://github.com/apache/shardingsphere/pull/32076)
1. Encrypt: Fix show create table wrong result with encrypt when data type contains float
1. Encrypt: Add insert select rewrite for encrypt
1. Encrypt: Fix the issue where updating a non-encrypted table and using a subquery on an encrypted table
1. Pipeline: Fix default data source lost when create migration job
1. Pipeline: InventoryTaskSplitter compatible with `BigInteger` primary key
1. Resolve the issue where "zip file closed" in versions prior to SpringBoot 2.3

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/29)

## Release 5.5.0

### API Changes

1. Kernel: Remove the sqlCommentParseEnabled configuration in SQL Parser Rule to reduce code complexity
1. SQL Parser: Consider removing sqlCommentParseEnabled config in SQLParser rule
1. Federation: Add allQueryUseSQLFederation config for sql federation
1. Proxy: Remove the outdated schemaName configuration of the Proxy

### New Features

1. DistSQL: New syntax for query plugin (SPI) implementation
1. DistSQL: New syntax for managing SQL_TRANSLATOR rule

### Enhancements

1. Authority: Add isSuper option for user
1. SQL Parser: Add EOF to throw exception when parse distsql rollback migration statement
1. SQL Parser: Support more plsql statement parse and add plsql parse assert logic
1. SQL Parser: Support parse index hint
1. SQL Parser: Support mysql intersect combine operation sql parse
1. SQL Parser: Support parse chinese white space for oracle
1. SQL Parser: Fix mysql TimeStampDiff function parse
1. SQL Parser: Fix sqlServer unqualified shorthand parsing
1. SQL Parser: Support sqlServer SEARCH unreserved word parsing
1. SQL Parser: Add SQL server MEMBER unreserved word
1. DistSQL: Use JSON format to output props in RQL
1. DistSQL: Optimize REFRESH DATABASE METADATA logic
1. DistSQL: SHOW COMPUTE NODES supports JDBC nodes
1. DistSQL: Optimize syntax of REFRESH DATABASE METADATA
1. DistSQL: Optimize the output of SHOW TABLE METADATA
1. SQL Binder: Add ParameterMarkerSegmentBinder logic for Oracle MergeStatementBinder
1. SQL Federation: Support mysql cross join statement for sql federation
1. Transaction: Add advice message in XATransactionCheckPrivilegeFailedException
1. Proxy: Add MySQL precompiled parameter verification to avoid turning on rewriteBatchedStatements=true when BenchmarkSQL connects to Proxy, causing an ArrayIndexOutOfBoundsException exception when the Proxy parameterCount exceeds 65535.
1. Proxy: Fix mysql client multi statements option in protocol
1. Sharding: Validate duplicate sharding actual data nodes
1. Sharding: Support null condition value routing
1. Pipeline: Improve CDC stability and performance
1. Pipeline: MemoryPipelineChannel supports configurable zero queue size for less memory consumption
1. Pipeline: Show dedicated error when mode type is not Cluster
1. Pipeline: Ignore error message in status DistSQL result on job cancelling

### Bug Fixes

1. MetaData: Fix database system schema is not built when create database
1. Metadata: Fix NPE of system schema builder rule
1. Governance: Fix thread blocking problem when create logic database for Etcd register center
1. Governance：Fix register storage units and create feature rules failure when use Standalone mode
1. SQL Parser: Fix PostgreSQL NPE when parse columnRef
1. SQL Parser: Fix npe cause by parse MySQL select window statement
1. SQL Federation: Fix Object 'DUAL' not found exception when execute select 1 from dual with sql federation
1. Transaction: Fix xa auto commit in executeQuery
1. Proxy: Restore original databaseName in connectionSession after unicast
1. Proxy: Fix show tables can be executed without use database
1. Proxy: Fix the incorrect current database after unicast routing
1. Proxy: Fix the problem that show tables can be executed before use database
1. Proxy: Fix multi statements with specified database name
1. Sharding: Fix routing error when joining tables in uppercase
1. Sharding: Fix drop sharding table exception when table name is uppercase
1. Sharding: Fix generated key with upper case column name
1. Readwrite-splitting: Fix check exception when using shadow data source
1. Pipeline: Fix commit/rollback migration job doesn't drop related consistency check job when check job is not completed
1. Pipeline: Fix show consistency check status stop_time display

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/28)


## Release 5.4.1

### New Features

1. Metadata: Standalone mode adapts to metadata new structure
1. Governance: Governance supports register instance level data source
1. Proxy: Supports dbcp and c3p0 connection pools

### Enhancements

1. Mode: Improve Standalone mode JDBC type impl reset data on initialization
1. JDBC: Move jdbc core META-INF/services/java.sql.Driver from test to main
1. Encrypt: Add duplicate name check for derived columns and logical columns
1. Encrypt: Deny DDL for cipher columns in Proxy
1. Encrypt: Add the default type for derived columns to varchar(4000)
1. Pipeline: Isolate ShardingSphereDataSource Standalone repository in pipeline
1. Pipeline: Disable system-schema-metadata-enabled in pipeline
1. Pipeline: Add algorithm columns in SHOW MIGRATION CHECK ALGORITHMS DistSQL result
1. Pipeline: Add type_alias column in SHOW MIGRATION CHECK ALGORITHMS DistSQL result

### Bug Fixes

1. Single Table: Fix not switched active version when CREATE/DROP table modifies the Single rule configuration
1. JDBC: Fix JDBC memory leak with MySQL in the 5.4.0 version
1. Pipeline: Fix get inventory position not correctly on breakpoint resuming when table names are similar
1. Pipeline: Fix CDC importer not start on breakpoint resuming when first inventory task is finished

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/27)


## Release 5.4.0

### API Changes

1. Metadata: Change sharding broadcast tables to global broadcast tables
1. JDBC: Remove exclamation mark (!) for global rules
1. DistSQL: Simplify keywords ASSISTED_QUERY, LIKE_QUERY in encrypt DistSQL
1. DistSQL: Optimize SQL_PARSER rule syntax
1. Encrypt: Adjust encryption yaml API to distinguish between encrypt, like, and assisted query configurations
1. Encrypt: Remove plain column and queryWithCipherColumn configuration in encrypt feature
1. Readwrite-splitting: Refactor read/write splitting api
1. Proxy: Remove property proxy-instance-type configuration
1. Proxy: Remove property proxy-backend-executor-suitable
1. Proxy: Remove property proxy-mysql-default-version
1. Scaling: Refactor commit rollback streaming to drop streaming
1. Sharding: Merge ShardingCacheRule into ShardingRule (Experimental)

### New Features

1. DistSQL: New syntax to manage SQL_FEDERATION rule
1. Proxy: Support Unix Domain Socket

### Enhancements

1. Scaling: CDC supports pure incremental mode
1. Scaling: CDC supports exporting data by transaction
1. Scaling: CDC supports MySQL and PostgreSQL
1. Scaling: CDC supports single table
1. Scaling: CDC supports all openGauss data types
1. Scaling: CDC supports replication reconnection
1. Scaling: Remove DataConsistencyCalculateAlgorithmChooser
1. Scaling: Improve performance of integer unique key table inventory data splitting
1. Scaling: Adjust process configuration default value to reduce resource consumption
1. Scaling: Auto refresh table metadata for migration
1. Scaling: Compatible with openGauss existing replication slot reuse when database not existing
1. Scaling: Show data consistency check status result should be empty when it's not completed
1. Scaling: Enable concurrent CRC32 match on source and target
1. Scaling: Pipeline job compatible with sharding rule audit strategy
1. Metadata: Refactor metadata persistence structure
1. Metadata: Optimize the process of loading single table metadata
1. Metadata: Support MySQL/PostgreSQL/openGauss system tables empty query
1. DistSQL: Add support for transactionalReadQueryStrategy for read/write splitting rule
1. DistSQL: Enhanced algorithm properties check
1. Transaction: Add privilege check
1. Transaction: Remove the TransactionTypeHolder and only create the current transaction manager
1. Parser: Support MySQL LOAD DATA and LOAD XML statement with single table or broadcast table
1. Parser: Improve the parsing support of high-priority SQL statements in the test results of the MySQL test program
1. Parser: Oracle dialect parser now supports Chinese comma
1. Encrypt: Support query of encrypt column in projection subquery when use encrypt feature
1. Kernel: Adds table existence metadata check for INSERT, DELETE, UPDATE and SELECT statements
1. JDBC: Implement batch execution for ShardingSphereStatement
1. Proxy: Frontend supports SSL/TLS
1. Proxy: Support Flush message for PostgreSQL/openGauss Proxy
1. Proxy: Support data type bit, bool for PostgreSQL Proxy

### Bug Fixes
1. Scaling: Fix pipeline job failure status persistence and usage
1. Scaling: Fix CDC DELETE event Record.beforeList is null
1. Scaling: Fix openGauss mppdb decoding plugin single quote issue
1. Scaling: Fix execute engine not closed after job stopping
1. Scaling: Fix stop job before task starting
1. Metadata: Fix case sensitive issue when loading schema meta data with H2 database
1. Metadata: Fix "object not found" exception when config PostgreSQL/openGauss schema name as database name
1. DistSQL: Fix wrong result of check_table_metadata_enabled when execute SHOW DIST VARIABLE
1. Encrypt: Fix SQL rewrite exception when use PostgreSQL/openGauss encrypt like feature
1. Sharding: Support null sharding condition pass to sharding algorithm to allow user control null value route
1. Parser: Support BETWEEN AND expression parsing in MySQL Projection
1. Mask: Fix wrong mask result when config same value of from-x and to-y with KEEP_FROM_X_TO_Y
1. Infra: Fix ClassNotFoundException may occur when missing pgjdbc
1. Proxy: Fix MySQL packet out of order when client sending pipelining requests

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/25)


## 5.3.2

### API Changes

1. Proxy: Add property `system-log-level`, support dynamic change of log level by DistSQL.
1. DistSQL: Remove Hint-related DistSQL, users can use `SQL Hint` instead

### New Features

1. Scaling: Support any type of column unique key table

### Enhancements

1. Scaling: Use stream query for inventory dump and data consistency check
1. Scaling: Compatible with `VARBINARY` column type in MySQL binlog parsing
1. Scaling: Refactor `AbstractSimplePipelineJob.execute` to blocking
1. Scaling: Improve table records count calculation
1. Scaling: Support proxy sharding rule absent for migration job
1. Scaling: Add `useServerPrepStmts=false` for pipeline job on MySQL
1. Scaling: Improve datetime/time parsing for MySQL binlog
1. Scaling: Add global status for prepare stage
1. Scaling: Add `netTimeoutForStreamingResults` for pipeline job on MySQL
1. Authority: Support specifying password authentication method
1. Authority: Add md5 authentication support for openGauss protocol
1. Agent: Add more metrics for JDBC
1. Kernel: ShardingSphere Driver configuration supports Apollo
1. Kernel: Adjust `SKIP_ENCRYPT_REWRITE SQL` Hint to `SKIP_SQL_REWRITE` to support more scenarios
1. Kernel: Support openGauss `EXPLAIN PERFORMANCE`
1. Encrypt: Like supports concat function

### Bug Fixes

1. Scaling: Fix unicode char and special char decoding for PostgreSQL incremental task
1. Scaling: Fix Migration not support PostgreSQL json type
1. DistSQL: `CREATE SHARDING TABLE RULE` supports `NONE` strategy
1. Kernel: Fix use system database error when data sources are empty
1. Kernel: Fix set worker-id does not take effect with Standalone mode
1. Kernel: Clear storage node information when delete readwrite-splitting and database discovery rules
1. Kernel: Fix the abnormal problem of Column index out of range in single table complex query
1. Kernel: Fix PostgreSQL like lower case failed.
1. Kernel: Fixed the exception of built-in metabase data collection when the front and back database types were inconsistent
1. Kernel: Fix the problem of routing error reporting under certain table names
1. Kernel: Fix MySQL create procedure parse error
1. Kernel: Fix union extract table name NPE
1. Kernel: Fix upper case table constraint not rewrite error
1. Kernel: Fix failed to parse PostgreSQL / openGauss SQL contains money type
1. Kernel: Fix PostgreSQL / openGauss positional parameter rule
1. Kernel: Fix PostgreSQL / openGauss failed to parse const with type cast
1. Kernel: Fix Chinese characters encode exception when execute select with sql federation engine
1. Kernel: Fix `IndexOutOfBoundsException` when execute set variable statement int jdbc adapter
1. Kernel: Fix index does not exist exception when execute drop index statement
1. Proxy: Properly handle number sign in MySQL Proxy binary protocol
1. Proxy: Fix PostgreSQL Proxy failed to handle bytea data type
1. Proxy: Fix PostgreSQL Proxy failed to parse time value with microseconds
1. Proxy: Fix PostgreSQL protocol codec for date type in binary format
1. Proxy: Fix possible CCE `PostgreSQLInt2BinaryProtocolValue`
1. Proxy: Fix possible error when client pass quoted charset to PostgreSQL/openGauss Proxy

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/24)


## 5.3.1

### New Features

1. Kernel: Add new data masking, dynamic data masking features, and built-in data masking algorithms
1. Scaling: Basic support of CDC feature
1. DistSQL: Add masking rule related DistSQL

### Enhancements

1. Kernel: Cluster mode avoids secondary refresh of metadata
1. Kernel: SHOW COMPUTE NODES supports displaying the version number of each instance
1. Kernel: System database add cluster information table
1. Kernel: Standalone mode persistent metadata supports MySQL
1. Kernel: SQL HINT performance improvement
1. Kernel: Restore routing to the specified database with Hint
1. Encrypt: Supports underscore wildcards for Encrypt CharDigestLikeEncryptAlgorithm
1. Kernel: Support SQL federation SELECT NULLS LAST/FIRST statement
1. Kernel: Refactor encrypt integration test logic and add more test cases
1. Kernel: Add salt props for MD5MaskAlgorithm, MD5EncryptAlgorithm
1. Kernel: Refactor ShardingConditionEngine to support SPI configuration
1. DistSQL: Add algorithm type check for `CREATE SHARDING TABLE RULE`

### Bug Fixes

1. Fix the problem of ZooKeeper cluster error reporting when ShardingSphere connects to Kubernetes
1. Kernel:  Fix use Consul in cluster mode start up failure
1. DB Discovery: Close heartbeat job when drop discovery rule
1. Kernel: Fix wrong decide result when execute same sharding condition subquery with SQL federation
1. Kernel:  Fix priority problem of UNION, INTERSECT, EXCEPT set operation in SQL Federation for PostgreSQL and openGauss dialect
1. Kernel:  Fix create view index out of range exception when view contains set operator
1. Kernel: Add XA resource exceeds length check
1. Kernel:  Fix transaction support for spring requires_new
1. Encrypt:  Fix AESEncryptAlgorithm decrypt exception when config char type with PostgreSQL and openGauss
1. Encrypt: Fix abnormal expansion result for shorthand when encrypt subquery contains an alias
1. Kernel:  Fix unsigned flag of column metadata was not serialized
1. Kernel: Fix PostgreSQL / openGauss select fetch parsing issue to support federation execution engine
1. Proxy: Fix packet sequence ID may be incorrect if error occurred in MySQL Proxy
1. Proxy: Fix error occur in Proxy when using PostgreSQL composite type
1. Proxy: Set proper column definition flag for MySQL COM_STMT_PREPARE
1. Proxy: When querying PG metadata through Proxy and the result set is empty, the labels are lost

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/23)


## 5.3.0

### API Changes

1. DistSQL: Refactor syntax API, please refer to the user manual
1. Proxy: Change the configuration style of global rule, remove the exclamation mark
1. Proxy: Allow zero-configuration startup, enable the default account root/root when there is no Authority configuration
1. Proxy: Remove the default logback.xml and use API initialization
1. JDBC: Remove the Spring configuration and use Driver + YAML configuration instead

### Enhancements

1. DistSQL: New syntax REFRESH DATABASE METADATA, refresh logic database metadata
1. Kernel: Support DistSQL REFRESH DATABASE METADATA to load configuration from the governance center and rebuild MetaDataContext
1. Support postgresql/openGauss setting transaction isolation level
1. Scaling: Increase inventory task progress update frequency
1. Scaling: DATA_MATCH consistency check support breakpoint resume
1. Scaling: Support drop consistency check job via DistSQL
1. Scaling: Rename column from sharding_total_count to job_item_count in job list DistSQL response
1. Scaling: Add sharding column in incremental task SQL to avoid broadcast routing
1. Scaling: Sharding column could be updated when generating SQL
1. Scaling: Improve column value reader for DATA_MATCH consistency check
1. DistSQL: Encrypt DistSQL syntax optimization, support like query algorithm
1. DistSQL: Add properties value check when REGISTER STORAGE UNIT
1. DistSQL: Remove useless algorithms at the same time when DROP RULE
1. DistSQL: EXPORT DATABASE CONFIGURATION supports broadcast tables
1. DistSQL: REGISTER STORAGE UNIT supports heterogeneous data sources
1. Encrypt: Support Encrypt LIKE feature
1. Automatically start distributed transactions when executing DML statements across multiple shards
1. Kernel: Support client \d for PostgreSQL and openGauss
1. Kernel: Support select group by, order by statement when column contains null values
1. Kernel: Support parse RETURNING clause of PostgreSQL/openGauss Insert
1. Kernel: SQL HINT performance improvement
1. Kernel: Support mysql case when then statement parse
1. Kernel: Supporting data source level heterogeneous database gateway
1. (Experimental) Sharding: Add sharding cache plugin
1. Proxy: Support more PostgreSQL datetime formats
1. Proxy: Support MySQL COM_RESET_CONNECTION
1. Scaling: Improve MySQLBinlogEventType.valueOf to support unknown event type
1. Kernel: Support case when for federation

### Bug Fixes

1. Scaling: Fix barrier node created at job deletion
1. Scaling: Fix part of columns value might be ignored in DATA_MATCH consistency check
1. Scaling: Fix jdbc url parameters are not updated in consistency check
1. Scaling: Fix tables sharding algorithm type INLINE is case-sensitive
1. Scaling: Fix incremental task on MySQL require mysql system database permission
1. Proxy: Fix the NPE when executing select SQL without storage node
1. Proxy: Support DATABASE_PERMITTED permission verification in unicast scenarios
1. Kernel: Fix the wrong value of worker-id in show compute nodes
1. Kernel: Fix route error when the number of readable data sources and weight configurations of the Weight algorithm are not equal
1. Kernel: Fix multiple groups of readwrite-splitting refer to the same load balancer name, and the load balancer fails problem
1. Kernel: Fix can not disable and enable compute node problem
1. JDBC: Fix data source is closed in ShardingSphereDriver cluster mode when startup problem
1. Kernel: Fix wrong rewrite result when part of logical table name of the binding table is consistent with the actual table name, and some are inconsistent
1. Kernel: Fix startup exception when use SpringBoot without configuring rules
1. Encrypt: Fix null pointer exception when Encrypt value is null
1. Kernel: Fix oracle parsing does not support varchar2 specified type
1. Kernel: Fix serial flag judgment error within the transaction
1. Kernel: Fix cursor fetch error caused by wasNull change
1. Kernel: Fix alter transaction rule error when refresh metadata
1. Encrypt: Fix EncryptRule cast to TransparentRule exception that occurs when the call procedure statement is executed in the Encrypt scenario
1. Encrypt: Fix exception which caused by ExpressionProjection in shorthand projection
1. Proxy: Fix PostgreSQL Proxy int2 negative value decoding incorrect
1. Proxy: PostgreSQL/openGauss support describe insert returning clause
1. Proxy: Fix gsql 3.0 may be stuck when connecting Proxy
1. Proxy: Fix parameters are missed when checking SQL in Proxy backend
1. Proxy: Enable MySQL Proxy to encode large packets
1. Kernel: Fix oracle parse comment without whitespace error
1. DistSQL: Fix show create table for encrypt table

### Refactor

1. Scaling: Reverse table name and column name when generating SQL if it's SQL keyword
1. Scaling: Improve increamental task failure handling
1. Kernel: Governance center node adjustment, unified hump to underscore

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/22)


## 5.2.1

### New Feature

1. Add ShardingSphere default system database to support global metadata management
1. Support asynchronous data consistency check
1. Added support for Consul governance center
1. Added support for Nacos governance center
1. Added support for the view function in the governance center

### Enhancement

1. SQL Federation engine adds ADVANCED executor and adapts to openGauss database
1. Support ShardingSphere Proxy startup after read-write splitting read database is disabled
1. SQL HINT supports force sharding route
1. Show processlist supports showing Proxy connections (sleep, active)
1. Optimized ShardingSphere-JDBC data source configuration error message
1. ShardingSphere-JDBC supports SpringBoot 3.x version
1. Support load MySQL, PostgreSQL, openGauss and SQLServer view metadata
1. Update Snakeyaml to 1.33 and open YAML 3MB limit
1. Reuse cached connections as possible when unicast sharding
1. Support Parsing ALTER ROLE in Oracle
1. Add support of ALTER RESOURCE COST for Oracle
1. Support parsing Drop Materialized View in Oracle
1. Support parsing DROP LIBRARY in Oracle
1. Support parsing DROP JAVA in Oracle
1. Support parsing DROP PLUGGABLE DATABASE in Oracle
1. Support parsing DROP INDEX TYPE in Oracle
1. Support Parsing ALTER PACKAGE in openGauss
1. Support openGauss select offset, count statement parse and remove useless syntax in PostgreSQL grammar
1. Add max_size to openGauss syntax
1. Optimize alter view/drop view parse logic and fix alter view refresher bug
1. Add sql parser error detail to ParseCancellationException
1. Add support for parse OptOnConflict for postgres
1. Enhance support for ALTER TABLE and ALTER VIEW in PostgreSQL
1. Add a missing keyword in PostgreSQL Declare Statement
1. Add json function support to mysql parser
1. ShardingSphere-Proxy automatically adapts to cgroup memory limits in Docker environment
1. Show migration status DistSQL respond new added error_message column
1. Show migration status respond new added processed_records_count column
1. Support MySQL 8 caching_sha2_password authentication in incremental dump
1. Improve drop pipeline process configuration
1. Support unique key table migration
1. Support migrate table to new name table
1. Improve thread pool usage in pipeline task and runner
1. Support cancelable data consistency check
1. DistSQL: When creating or altering readwrite-splitting rule, check duplicate write or read resources
1. DistSQL: Add validity check for `ALTER SHARDING BINDING TABLE RULES`
1. Standalone mode H2 support persistent metadata
1. Fix openGauss cursor execution in xa transaction
1. Added transaction related exceptions

### Bug Fixes

1. Generate proper placeholder for PostgreSQL when rewriting
1. Fix opengauss update set parse exception
1. Fix parse exception when execute insert statement with negative value
1. Fix wrong connectDescriptorUrlPattern in OracleDataSourceMetaData
1. Fix insert SQL garbled after sharding rewrote in special rules
1. Fix exception when execute select * from information_schema.tables
1. Fix exception when execute alter view rename
1. Fix PostgreSQL check data source permission when using rolsuper
1. DistSQL: fix NPE for `REFRESH TABLE METADATA` when there is no resource
1. Fix Unmodified table metadata when modify rules
1. Fix database discovery
1. The MySQL.NORMAL_REPLICATION algorithm cannot find primary node"
1. Fixed using etcd to build a cluster event not aware
1. Fix NPE occurred when transaction management is not created
1. Fix routing exception that occurs when the sharding value of the InlineShardingAlgorithm algorithm exceeds Integer

### API Changes

1. SQL HINT syntax format adjust to SQL-style format
1. DistSQL: Remove syntax `COUNT DATABASE RULES`
1. ShardingSphere mode remove overwrite configuration
1. Agent: Optimize configuration of agent.yaml

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/21)

## 5.2.0

### New Feature

1. Support SQL audit for sharding feature
1. Support MySQL show processlist and kill process id feature
1. Scaling: Add dedicated DistSQL for data migration
1. Scaling: Basic support migrate data to heterogeneous database
1. DistSQL: New syntax `CREATE MIGRATION PROCESS CONFIGURATION`
1. DistSQL: New syntax `ALTER MIGRATION PROCESS CONFIGURATION`
1. DistSQL: New syntax `SHOW MIGRATION PROCESS CONFIGURATION`
1. DistSQL: New syntax  `ADD MIGRATION SOURCE RESOURCE`
1. DistSQL: New syntax `SHOW SQL_TRANSLATOR RULE`
1. DistSQL: New syntax `CREATE SHARDING AUDITOR`
1. DistSQL: New syntax `ALTER SHARDING AUDITOR`
1. DistSQL: New syntax `SHOW SHARDING AUDIT ALGORITHMS`

### Enhancement

1. Support column visible feature for MySQL, Oracle, SQLServer and H2
1. Support cartesian product configuration for read write splitting
1. Support spring namespace and spring boot usage for sql translator
1. Support JSR-310 Year and Month in IntervalShardingAlgorithm
1. Support broadcast table update/delete limit statement
1. Support create index on table(column) statement rewrite when config encrypt
1. Support openGauss cursor, fetch, move, close statement for sharding, readwrite-splitting
1. Support encrypt column rewrite when execute column is null in predicate
1. Support encrypt show create table return logic columns
1. Support create table with index statement rewrite when config encrypt
1. Support PostgreSQL create operator statement parse
1. Support PostgreSQL create materialized view statement parse
1. Support PostgreSQL nested comments parse
1. Support PostgreSQL alter subscription statement parse
1. Support PostgreSQL create group statement parse
1. Support PostgreSQL alter statictics statement parse
1. Support PostgreSQL create foreign table statement parse
1. Support PostgreSQL alter server statement parse
1. Support PostgreSQL create foreign data wrapper statement parse
1. Support PostgreSQL create event trigger statement parse
1. Support PostgreSQL security label statement parse
1. Support PostgreSQL reindex statement parse
1. Support PostgreSQL reassign owned statement and refresh materialized view statement parse
1. Support PostgreSQL prepare transaction statement parse
1. Support PostgreSQL create collation statement parse
1. Support PostgreSQL lock statement parse
1. Support PostgreSQL alter rule statement parse
1. Support PostgreSQL notify statement parse
1. Support PostgreSQL unlisten statement parse
1. Support Oracle alter function and alter hierarchy statement parse
1. Support Oracle alter pluggable database statement parse
1. Support Oracle alter materialized view log statement parse
1. Support Oracle alter diskgroup statement parse
1. Support Oracle alter operator statement parse
1. Support oracle alter cluster statement parse
1. Support oracle alter audit policy statement parse
1. Support Oracle alter index type statement parse
1. Support Oracle lock table statement parse
1. Support Oracle alter java statement parse
1. Support Oracle inline constraint statement parse
1. Support openGauss geometric operator statement parse
1. Optimize MySQL visible/invisible parse of create/alter table statements
1. Support scope of variable prefixed with @@ in MySQL SET statement parse
1. Support MySQL create procedure with create view parse
1. Support column segments parse in create index on table statement
1. Support openGauss cursor, fetch, move, close statement for sharding, readwrite-splitting
1. Support encrypt column rewrite when execute column is null in predicate
1. Support encrypt show create table return logic columns
1. Support create table with index statement rewrite when config encrypt
1. Support parsing ALTER LOCKDOWN PROFILE in Oracle
1. Support parsing ALTER MATERIALIZED VIEW in Oracle
1. Support parsing ALTER MATERIALIZED ZONEMAP in Oracle
1. Support parsing ALTER LIBRARY in Oracle
1. Support parsing ALTER INMEMORY JOIN GROUP in Oracle
1. Support parsing DROP OPERATOR in Oracle
1. Support parsing DROP RESTORE POINT in Oracle
1. Support parsing CREATE RESTORE POINT in Oracle
1. Support parsing DROP INMEMORY JOIN GROUP in Oracle
1. Support parsing create_bit_xor_table in MySQL
1. Support parsing MySQL DO statement
1. Support parsing DropServer in openGauss
1. Support parsing CREATE AGGREGATE In openGauss
1. Support parsing ALTER ROUTINE in PostgreSQL
1. Add PostgreSQL Create Cast Statement
1. Add PostgreSQL Create Aggregate Statement
1. Support fetch/move/close cursor statement in PostgreSQL
1. Support Parsing ALTER PUBLICATION in PostgreSQL
1. Add PostgreSQL Create Access Method Statement
1. Support Parsing ALTER POLICY in PostgreSQL
1. Support parsing ALTER OPERATOR in PostgreSQL
1. Add PostgreSQL Copy Statement
1. Add PostgreSQL Comment Statement
1. Support listen statement in PostgreSQL
1. Support DECLARE cursor statement
1. Add default serverConfig in helm charts
1. Assemble openGauss JDBC Driver into Proxy distribution
1. ShardingSphere-Proxy listen on specified IP addresses
1. Support COM_STMT_SEND_LONG_DATA in MySQL Proxy
1. SELECT VERSION() support alias in MySQL Proxy
1. Fix openGauss Proxy could not be connected if no resource defined
1. Support using JRE defined in JAVA_HOME in ShardingSphere-Proxy's startup script
1. Avoid client blocked when OOM occurred in ShardingSphere-Proxy
1. Support using c3p0 in ShardingSphere-JDBC
1. Support SET NAMES with value quoted by double-quote
1. Connection.prepareStatement with columns arguments is available in ShardingSphere-JDBC
1. Scaling: Improve MySQL connect and reconnect
1. Scaling: Fix MySQL json column may cause leak at incremental task
1. Scaling: Add permission check for PostgreSQL data sources
1. Scaling: Incremental migration support for MySQL MGR mode
1. Scaling: Improve job progress persistence
1. Scaling: Start job DistSQL execute and return synchronously
1. Scaling: Inventory migration support table has primary key and unique key
1. Scaling: Close unerlying ElasticJob when stopping job
1. Scaling: Improve logical replication slot name generation for PostgreSQL and openGauss
1. Scaling: Make query DistSQL could be executed when no database selected
1. DistSQL: Add worker_id to the result set of `SHOW INSTANCE LIST` & `SHOW INSTANCE INFO`
1. DistSQL: Improve the result of `EXPORT DATABASE CONFIG`
1. DistSQL: Support more databases for `FORMAT SQL`
1. DistSQL: Optimize the execution logic of `CREATE TRAFFIC RULE`
1. DistSQL: Support `assistEncryptor` for Encrypt RDL
1. DistSQL: Add sharding algorithm type check when `CREATE SHARDING TABLE RULE`
1. Support database discovery to configure multiple groups of high availability under the same logic database
1. Support ShardingSphere-Proxy to start up under empty logic library
1. Support for isolating EventBus events by instance
1. Support the database to detect changes in the master node and restart the detection heartbeat task
1. Support ShardingSphere-Proxy to generate new worker-id when re-registering in cluster mode
1. Thrown exception when inserting expression value in shadow column on executing insert
1. Support distributed transactions across multiple logical databases
1. Support executing truncate in XA & PostgreSQL
1. Support alter local transaction rule with DistSQL
1. Support global transaction manager
1. Delete support for branch transaction on proxy

### Bug Fixes

1. Fix single table metadata refresh error caused by filtering DataSourceContainedRule
1. Fix parsing exception caused by the null value of MySQL blob type
1. Fix PostgreSQL/openGauss reset statement parse error
1. Fix wrong parameter rewrite when use sharding and encrypt
1. Fix the failed conversion of Month related classes on IntervalShardingAlgorithm
1. Fix NullPointerException when execute select union statement contains subquery
1. Fix wrong encrypt rewrite result due to incorrect order of metadata
1. Fix MySQL trim function parse error
1. Fix MySQL insert values with _binary parse error
1. Fix MySQL syntax error cannot be thrown to client
1. Avoid EventLoop blocked because of closing JDBC resources
1. Correct server status flags returned by MySQL Proxy
1. Fix a possible connection leak issue if Proxy client disconnected in transaction
1. Fixed a possible consistency issue with the statement being executed when the Proxy client is disconnected
1. Avoid pooled connection polluted by executing SET statements
1. Make SHOW TABLES FROM work in ShardingSphere-Proxy
1. Fix PostgreSQL DDL could not be executed by Extended Query
1. Fix SHOW VARIABLES could not be executed in PostgreSQL Proxy without resource
1. Fix FileNotFoundException when use ShardingSphere Driver with SpringBoot fatjar
1. Scaling: Fix the problem that the table contains both primary key and unique index at inventory migration
1. Scaling: Improve incremental migration, support the latest position in the middle of batch insert event
1. Scaling: Fix the error caused by null field value in openGauss incremental migration
1. DistSQL: Fix incorrect strategy name in result of `SHOW SHARDING TABLE RULES`
1. DistSQL: Fix current rule config is modified in advance when `ALTER SHARDING TABLE RULE`
1. DistSQL: Fix connection leak when `ALTER RESOURCE`
1. DistSQL: Fix `CREATE TRAFFIC RULE` failed when load balance algorithm is null
1. Fix that the monitoring heartbeat task was not stopped when the database was discovered and the logical library was deleted
1. Fix cluster mode ShardingSphere-JDBC load all logic database
1. Fix worker-id generated by SnowflakeKeyGenerateAlgorithm in cluster mode may exceed the maximum value
1. Fix `DistSQL` adding shadow algorithm exception without shadow data source
1. Fix cross-database data source confusion caused by same data source name in multiple logical databases
1. Fix RUL DistSQL execution failure in transaction
1. Fix begin for PostgreSQL & openGauss
1. Agent: Fixed the error of null value in contextManager when collecting metric data

### API Changes

1. Remove SQL passthrough to data source feature
1. Add new assistedQueryEncryptorName and remove QueryAssistedEncryptAlgorithm interface
1. Refactor readwrite-splitting api to improve user experience
1. Remove check-duplicate-table-enabled configuration
1. Remove useless config item show-process-list-enabled configuration
1. Scaling: Change keyword for part of data migration DistSQL
1. Scaling: Redesign part of data migration DistSQL
1. DistSQL: Unify parameter type specification
1. DistSQL: Split `SHOW INSTANCE MODE` to `SHOW MODE INFO` and `SHOW INSTANCE INFO`
1. DistSQL: Change `DROP SCALING jobId` to `CLEAN MIGRATION jobId`
1. DistSQL: Remove `COUNT INSTANCE RULES`
1. Add database found that high availability supports all the slave libraries to go offline, and the main library undertakes the read traffic configuration
1. SnowflakeKeyGenerateAlgorithm supports configuring worker-id in standalone mode
1. Replace `sourceDataSourceName` with `productionDataSourceName` in Shadow API Configuration
1. Authority: Remove deprecated native authority provider

### Refactor

1. ShardingSphere metadata refactoring for splitting actual metadata and logical metadata
1. Use ConnectionContext, QueryContext to remove ThreadLocal in FetchOrderByValueQueuesHolder, TrafficContextHolder, SQLStatementDatabaseHolder and TransactionHolder
1. Modify the default value of the ShardingSphere-Proxy version in the helm chart
1. Docker container will exit if ShardingSphere-Proxy failed to startup
1. Helm Charts in ShardingSphere repository are transferred to sub-project shardingsphere-on-cloud
1. Scaling: Plenty of refactor for better code reuse
1. DistSQL: Add a new category named RUL
1. Refactor the schedule module and split it into cluster schedule and standalone schedule
1. Remove memory mode, keep standalone mode and cluster mode
1. Refactoring metadata table loading logic and persistence logic
1. Refactoring distributed locks to retain the most concise interface design
1. Refactor : Unify The Creation for Proxy Containers in IT from ENV Modules
1. Refactor : Unify The Configuration for container created by testcontainer

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/20)

## 5.1.2

### New Feature

1. Kernel: Alpha version to support SQL dialect translate for MySQL and PostgreSQL
1. Kernel: Support custom schema for PostgreSQL and openGauss
1. Kernel: Support create/alter/drop view statement for PostgreSQL and openGauss
1. Kernel: Support openGauss cursor statement
1. Kernel: Support use customize system database
1. Kernel: Support get openGauss and MySQL create SQL
1. Kernel: Support get postgres create SQL
1. Proxy: Official support for quickly deploying a ShardingSphere-Proxy cluster with a ZooKeeper cluster in Kubernetes using Helm
1. JDBC: Support ShardingSphere JDBC Driver
1. Scaling: Support PostgreSQL auto create table
1. Scaling: Support scaling for table with customized schema in PostgreSQL and openGauss
1. Scaling: Support scaling for table with text primary key and without integer primary key
1. Mode: Registry center supports PG/openGauss three-level structure
1. Mode: Registry center supports database-level distributed lock

### Enhancement

1. Kernel: Support copy statement for PostgreSQL and openGauss
1. Kernel: Support alter/drop index statement for PostgreSQL
1. Kernel: Support update force index statement for MySQL
1. Kernel: Support create/alter/drop schema for openGauss
1. Kernel: Optimize RoundRobinReplicaLoadBalanceAlgorithm and RoundRobinTrafficLoadBalanceAlgorithm logic
1. Kernel: Optimize metadata loading logic when frontendDatabaseType and backendDatabaseType are different
1. Kernel: Refactor meta data load logic
1. Kernel: Optimize show processlist statement
1. Kernel: Improve performance about large tables loaded
1. Kernel: Support execute comment statement
1. Kernel: Support view in sharding rule
1. Kernel: Support parsing CREATE ROLLBACK SEGMENT in Oracle
1. Kernel: Support Parsing DROP TYPE in openGauss
1. Kernel: Support Parsing ALTER TYPE in openGauss
1. Kernel: Support parsing DROP DISKGROUP in Oracle
1. Kernel: Support parsing CREATE DISKGROUP in Oracle
1. Kernel: Support parsing DROP FLASHBACK ARCHIVE in Oracle
1. Kernel: Support Parsing CHECKPOINT in openGauss
1. Kernel: Support parsing CREATE FLASHBACK ARCHIVE in Oracle
1. Kernel: Add PostgreSQL Close Statement
1. Kernel: Support Parsing DROP CAST in openGauss
1. Kernel: Support parsing CREATE CAST in openGauss
1. Kernel: Support parsing CREATE CONTROL FILE in Oracle
1. Kernel: Support Parsing DROP DIRECTORY in openGauss
1. Kernel: Support parsing ALTER DIRECTORY in openGauss
1. Kernel: Support parsing CREATE DIRECTORY in openGauss
1. Kernel: Add PostgreSQL Checkpoint Statement
1. Kernel: Support parsing DROP SYNONYM in openGauss
1. Kernel: Support parsing CREATE SYNONYM in openGauss
1. Kernel: Support parsing ALTER SYNONYM in openGauss
1. Kernel: Add PostgreSQL CALL Statement
1. Kernel: Support parsing CREATE PFILE in Oracle
1. Kernel: Support parsing CREATE SPFILE in Oracle
1. Kernel: Support parsing ALTER SEQUENCE in Oracle
1. Kernel: Support parsing CREATE CONTEXT in Oracle
1. Kernel: Support Parsing ALTER PACKAGE in oracle
1. Kernel: Support parsing CREATE SEQUENCE in Oracle
1. Kernel: Support parsing ALTER ATTRIBUTE DIMENSION in Oracle
1. Kernel: Support parsing ALTER ANALYTIC VIEW in Oracle
1. Kernel: Use ShardingSphere SPI in SQLVisitorFacade
1. Kernel: Use ShardingSphere SPI in DatabaseTypedSQLParserFacade
1. Kernel: Support parsing ALTER OUTLINE in Oracle
1. Kernel: Support parsing DROP OUTLINE in Oracle
1. Kernel: Support parsing drop edition in oracle
1. Kernel: Support WITH Common Table Expression of SQLServer
1. Kernel: Exclude parenthesis from SubquerySegment's start and stop index in withClause
1. Kernel: Refactor JoinTableSegment
1. Kernel: Support parsing DROP SYNONYM in Oracle
1. Kernel: Support parsing CREATE DIRECTORY in Oracle
1. Kernel: Support parsing CREATE SYNONYM in Oracle
1. Kernel: Support for XmlNamespaces Clause of SQLServer SELECT Statement
1. Kernel: Support parsing Alter Database Dictionary in Oracle
1. Kernel: Support FOR Clause of SQLServer SELECT Statement
1. Kernel: Support Parsing ALTER DATABASE LINK in Oracle
1. Kernel: Support CREATE EDITION Parsing in Oracle
1. Kernel: Support parsing ALTER TRIGGER in Oracle
1. Kernel: Add SQLServer REVERT Statement
1. Kernel: Support Parsing DROP TEXT SEARCH in PostgreSQL
1. Kernel: Add drop server for PostgreSQL
1. Kernel: Support Parsing ALTER VIEW in Oracle
1. Kernel: Add drop access method for PostgreSQL
1. Kernel: Support Parsing DROP ROUTINE in PostgreSQL
1. Kernel: Proofread SQLServer DROP USER Statement
1. Kernel: Support parsing DROP TRIGGER in Oracle
1. Kernel: Support parsing Drop subscription in PostgreSQL
1. Kernel: Add drop operator class for PostgreSQL
1. Kernel: Support parsing DROP PUBLICATION in PostgreSQL
1. Kernel: Support Parsing DROP VIEW in Oracle
1. Kernel: Support Parsing DROP TRIGGER in PostgreSQL
1. Kernel: Support Parsing DROP DIRECTORY in Oracle
1. Kernel: Support Parsing DROP STATISTICS for PostgreSQL
1. Kernel: Add drop type SQL parser for PostgreSQL
1. Kernel: Support Parsing DROP RULE in PostgreSQL
1. Kernel: Proofread SQLServer ALTER LOGIN Statement
1. Kernel: Support parsing PostgreSQL DROP FOREIGN DATA WRAPPER
1. Kernel: Small changes to PostgreSQL DROP EVENT TRIGGER statement
1. Proxy: ShardingSphere-Proxy MySQL supports receiving MySQL packet more than 16 MB
1. Proxy: Supports netty parameter ChannelOption.SO_BACKLOG configurable in ShardingSphere-Proxy
1. Proxy: Optimize so-reuseaddr in netty to solve the problem of port occupied
1. Proxy: Docker image of ShardingSphere-Proxy supports aarch64 platform
1. Proxy: Make server version configurable in ShardingSphere-Proxy MySQL
1. Proxy: Supports more character sets in ShardingSphere-Proxy PostgreSQL/openGauss
1. Proxy: Make default port configurable in ShardingSphere-Proxy
1. Scaling: Compatible with HA ports for openGauss:3.0 when thread_pool enabled
1. Scaling: Optimize ZooKeeper event handling in PipelineJobExecutor to avoid blocking ZooKeeper events
1. Scaling: Make table name case-insensitive in whole process
1. Scaling: Improve replication slot cleanup for PostgreSQL and openGauss
1. Scaling: Improve lock protection for job preparation
1. Scaling: Support PostgreSQL insert on conflict do update
1. Scaling: Do not cache data source in GlobalDataSourceRegistry to avoid possible shared resource close issue
1. Scaling: Reuse data source pool as more as possible to reduce working database connections
1. DistSQL: `REFRESH TABLE METADATA` supports specifying PostgreSQL's schema
1. DistSQL: `ALTER SHARDING TABLE RULE` add validation of binding table
1. Mode: ShardingSphere-JDBC supports configuring database connection name
1. Distributed Transaction: DistSQL is prohibited from executing within a transaction
1. Distributed Transaction: autocommit = 0, DDL part of DML will automatically open the transaction

### Bug Fixes

1. Kernel: Fix parsing error about show statement for PostgreSQL and openGauss
1. Kernel: Fix parsing error about time extract function for for PostgreSQL and openGauss
1. Kernel: Fix parsing error about select mod function for for PostgreSQL and openGauss
1. Kernel: Fix PSQLException when execute join statement with multi schema in readwrite scenario
1. Kernel: Fix wrong route result when execute create schema statement in encrypt scenario
1. Kernel: Fix npe when execute drop schema if exist statement
1. Kernel: Fix wrong route result when execute SELECT LAST_INSERT_ID() AS id; statement
1. Kernel: Fix npe when execute use database when database doesn't contains datasource
1. Kernel: Fix create function with set var
1. Proxy: Fix NPE caused by column's case unmatched in PostgreSQLComDescribeExecutor
1. Proxy: Complete command tags for schema DDL in ShardingSphere-Proxy PostgreSQL / openGauss
1. Scaling: Fix MySQL unsigned type null value cause error during increment task
1. Scaling: Fix resource leak caused by error occurred when creating DataSource in ShardingSphere-Scaling
1. Scaling: Fix ShardingSphereDataSource creation ignoring other rules
1. Scaling: Fix on preparation job could not be stopped
1. Scaling: Fix data source property url and standardJdbcUrl compatibility
1. Scaling: Fix openGauss logical replication slot creation, avoid possible incremental data loss
1. Scaling: Update local job status before persisting job status to registry center, make sure it won't be overwritten later
1. Scaling: Handling null value in TestDecodingPlugin for PostgreSQL
1. DistSQL: Fix `SET VARIABLE` modification not taking effect in stand-alone and memory mode
1. DistSQL: Fix the inconsistency between `SHOW INSTANCE LIST` display data and actual data
1. DistSQL: Fix capitalization sensitivity in sharding DistSQL
1. Mode: Fix the new version metadata lost data after the Scaling changes the table sharding rules
1. Distributed Transaction: Fix getIndexInfo with catalog

### API Changes

1. DistSQL: Change `EXPORT SCHEMA CONFIG`  to `EXPORT DATABASE CONFIG`
1. DistSQL: Change `IMPORT SCHEMA CONFIG` to `IMPORT DATABASE CONFIG`
1. DistSQL: Change `SHOW SCHEMA  RESOURCES` to `SHOW DATABASE RESOURCES`
1. DistSQL: Change `COUNT SCHEMA RULES` to `COUNT DATABASE RULES`
1. Mode: Adjust db-discovery algorithm configuration
1. Authority: Authority provider `ALL_PRIVILEGES_PERMITTED` updated to `ALL_PERMITTED`
1. Authority: Authority provider `SCHEMA_PRIVILEGES_PERMITTED` updated to `DATABASE_PERMITTED`

### Refactor

1. Scaling: Refactor JobConfiguration, prepare for different types of jobs reuse and extension
1. Mode: Optimize compute node structure of the registry center
1. Mode: Use uuid instead of ip@port as instance id

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/19)

## 5.1.1

### New Feature
1. Kernel: support alter materialized view for PostgreSQL
1. Kernel: support declare for PostgreSQL
1. Kernel: support discard for PostgreSQL
1. Kernel: Add mode to parser to support $$ in PostgreSQL
1. Kernel: Support MySQL create tablespace statement parse
1. Scaling: Implement stop source writing and restore source writing
1. Scaling: Support partial tables scale-out
1. DistSQL: New DistSQL syntax: `SHOW UNUSED RESOURCES`
1. Mode: Added persistent `XA Recovery Id` to Governance Center
1. Mode: Database discovery adds delayed master-slave delay function
1. Distributed Transaction: Add savepoint support for ShardingSphere proxy
1. Distributed Transaction: Support auto rollback when report exception in transaction block for PostgreSQL and openGauss
1. Distributed Transaction: Make it is easy to use with Narayana
1. Distributed Transaction: Add savepoint support for ShardingSphere-JDBC

### Enhancement
1. Kernel: Refactor kernel to improve performance
1. Proxy: Reduce Docker image size of ShardingSphere-Proxy
1. Proxy: ShardingSphere-Proxy supports set names statements
1. Proxy: ShardingSphere-Proxy MySQL supports multi statements
1. Scaling: Only one proxy node could do data consistency check in proxy cluster
1. Scaling: Replace scaling input and output config fields type from int to Integer
1. Scaling: Update MySQL checksum SQL
1. Scaling: Improve scaling job progress deletion in reset and progress check before starting job
1. Scaling: Improve `FinishCheckJob` data consistency check when target tables already have the same data as source tables
1. Scaling: Break scaling job ASAP when there is unsupported table since primary key
1. Scaling: Reuse `ClusterPersistRepository` of proxy in `PipelineAPIFactory`
1. Scaling: Update jobId generation algorithm, and make it support idempotency
1. DistSQL: Support configuration data type and length when CREATE/ALTER ENCRYPT RULE
1. DistSQL: Unify the display results of `SHOW ALL VARIABLES` and `SHOW VARIABLE`
1. DistSQL: Remove the effect of binding order when `DROP BINDING TABLE RULES`
1. DistSQL: Add column `mode_type` in the result of `SHOW INSTANCE LIST`
1. DistSQL: Add validation to the mode when `ENABLE/DISABLE INSTANCE`
1. DistSQL: Check if the rule is in used when `DROP READWRITE_SPLITTING RULE`
1. DistSQL: Check duplicate resource names when `CREATE READWRITE_SPLITTING RULE`
1. DistSQL: Add column `delay_time` to the result of `SHOW READWRITE_SPLITTING READ RESOURCES`
1. DistSQL: Support `IF EXISTS` when `DROP RULE`
1. DistSQL: Optimize the prompt information of connection failure when `ADD/ALTER RESOURCE`
1. Mode: Add schema-level global distributed locks
1. Mode: Add schema version number to support batch execution of DistSQL
1. Mode: Persistent metadata optimization in cluster mode
1. Mode: The database discovery add the `schemaName` identifier when create a JOB

### Refactor

1. Kernel: Refactor test case for encrypt
1. Kernel: Refactor metadata to support PostgreSQL database and schema
1. Scaling: Remove HikariCP dependency in pipeline modules
1. Mode: Refactor governance center storage node structure
1. Mode: Refactor governance center meta data structure
1. Mode: Adjust the database discovery MGR module to MySQL module

### Bug Fixes
1. Kernel: Fix function with no parameter
1. Kernel: Fix `InsertValueContext.getValue` cast exception
1. Kernel: Fix aggregate distinct column error
1. Kernel: Fix NPE when rewrite parameter with schema
1. Kernel: Fix NPE caused by `GeneratedKeysResultSet` not return `columnName` in read-write splitting
1. Kernel: Fix show tables statement loses part of the single table
1. Kernel: Fix ModShardingAlgorithm wrong route result when exist same suffix table
1. Kernel: Fix sql parse error when contains key in assignment clause and optimize index parse
1. Kernel: Fix NumberFormatException when sharding algorithm config number props
1. Kernel: Fix wrong metadata when config single dataSource for read-write splitting
1. Kernel: Fix statement close exception when use `BatchPreparedStatementExecutor`
1. Kernel: Fix rewrite lowercase logic when sql contains shorthand projection
1. Kernel: Fix NullPointerException when start up proxy with memory mode
1. Proxy: Fix literals may be replaced by mistake in PostgreSQL/openGauss protocol
1. Proxy: Fix ShardingSphere-Proxy PostgreSQL with multi-schema cannot be connected by PostgreSQL JDBC Driver 42.3.x
1. Proxy: Fix timestamp nanos inaccurate in ShardingSphere-Proxy MySQL
1. Proxy: Complete ShardingSphere-Proxy PostgreSQL codec for numeric in binary format
1. Proxy: Potential performance issue and risk of OOM in ShardingSphere-JDBC
1. Proxy: Fix Operation not allowed after ResultSet closed occasionally happens in ShardingSphere-Proxy MySQL
1. Proxy: Fix NPE causes by ShardingSphere-JDBC executeBatch without addBatch
1. Scaling: Fix failed or stopped job could not be started by DistSQL except restarting proxy
1. DistSQL: Fix parsing exception for inline expression when `CREATE SHARDING TABLE RULE`
1. DistSQL: Fix parsing exception when password is keyword `password` in `ADD RESOURCE` statement
1. Mode: Fixed loss of compute nodes due to ZooKeeper session timeout
1. Mode: Fixed the case of the table name in the governance center
1. Mode: DistSQL enable disable instance refresh in-memory compute node status
1. Mode: Fixed database discovery unable to create Rule through DistSQL

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/18)

## 5.1.0

### New feature

1. Support SQL hint
1. New DistSQL syntax: SHOW AUTHORITY RULE
1. New DistSQL syntax: SHOW TRANSACTION RULE
1. New DistSQL syntax: ALTER TRANSACTION RULE
1. New DistSQL syntax: SHOW SQL_PARSER RULE
1. New DistSQL syntax: ALTER SQL_PARSER RULE
1. New DistSQL syntax: ALTER DEFAULT SHARDING STRATEGY
1. New DistSQL syntax: DROP DEFAULT SHARDING STRATEGY
1. New DistSQL syntax: CREATE DEFAULT SINGLE TABLE RULE
1. New DistSQL syntax: SHOW SINGLE TABLES
1. New DistSQL syntax: SHOW SINGLE TABLE RULES
1. New DistSQL syntax: SHOW SHARDING TABLE NODES
1. New DistSQL syntax: CREATE/ALTER/DROP SHARDING KEY GENERATOR
1. New DistSQL syntax: SHOW SHARDING KEY GENERATORS
1. New DistSQL syntax: REFRESH TABLE METADATA
1. New DistSQL syntax: PARSE SQL, Output the abstract syntax tree obtained by parsing SQL
1. New DistSQL syntax: SHOW UNUSED SHARDING ALGORITHMS
1. New DistSQL syntax: SHOW UNUSED SHARDING KEY GENERATORS
1. New DistSQL syntax: CREATE/DROP SHARDING SCALING RULE
1. New DistSQL syntax: ENABLE/DISABLE SHARDING SCALING RULE
1. New DistSQL syntax: SHOW SHARDING SCALING RULES
1. New DistSQL syntax: SHOW INSTANCE MODE
1. New DistSQL syntax: COUNT SCHEMA RULES
1. Scaling: Add `rateLimiter` configuration and `QPS` `TPS` implementation
1. Scaling: Add `DATA_MATCH` data consistency check
1. Scaling: Add `batchSize` configuration to avoid possible OOME
1. Scaling: Add `streamChannel` configuration and `MEMORY` implementation
1. Scaling: Support MySQL BINARY data type
1. Scaling: Support MySQL YEAR data type
1. Scaling: Support PostgreSQL BIT data type
1. Scaling: Support PostgreSQL MONEY data type
1. Database discovery adds support for JDBC Spring Boot
1. Database discovery adds support for JDBC Spring Namespace
1. Database discovery adds support for openGauss
1. Shadow DB adds support for logical data source transfer
1. Add data type validator for column matching shadow algorithm
1. Add support for xa start/end/prepare/commit/recover in encrypt case with only one data source

### API Change

1. Redesign the database discovery related DistSQL syntax
1. In DistSQL, the keyword GENERATED_KEY is adjusted to KEY_GENERATE_STRATEGY
1. Native authority provider is marked as deprecated and will be removed in a future version
1. Scaling: Move scaling configuration from server.yaml to config-sharding.yaml
1. Scaling: Rename clusterAutoSwitchAlgorithm SPI to completionDetector and refactor method parameter
1. Scaling: Data consistency check API method rename and return type change
1. Database discovery module API refactoring
1. Readwrite-splitting supports static and dynamic configuration
1. Shadow DB remove the enable configuration
1. Shadow algorithm type modified

### Enhancement

1. Improve load multi single table performance
1. Remove automatically added order by primary key clause
1. Optimize binding table route logic without sharding column in join condition
1. Support update sharding key when the sharding routing result keep the same
1. Optimize rewrite engine performance
1. Support select union/union all ... statements by federation engine
1. Support insert on duplicate key update sharding column when route context keep same
1. Use union all to merge sql route units for simple select to improve performance
1. Supports autocommit in ShardingSphere-Proxy
1. ShardingSphere openGauss Proxy supports sha256 authentication method
1. Remove property java.net.preferIPv4Stack=true from Proxy startup script
1. Remove the verification of null rules for JDBC
1. Optimize performance of executing openGauss batch bind
1. Disable Netty resource leak detector by default
1. Supports describe prepared statement in PostgreSQL / openGauss Proxy
1. Optimize performance of executing PostgreSQL batched inserts
1. Add instance_id to the result of SHOW INSTANCE LIST
1. Support to use instance_id to perform operations when enable/disable a proxy instance
1. Support auto creative algorithm when `CREATE SHARDING TABLE RULE`, reducing the steps of creating rule
1. Support specifying an existing KeyGenerator when CREATE SHARDING TABLE RULE
1. DROP DATABASE supports IF EXISTS option
1. DATANODES in SHARDING TABLE RULE supports enumerated inline expressions
1. CREATE/ALTER SHARDING TABLE RULE supports complex sharding algorithm
1. SHOW SHARDING TABLE NODES supports non-inline scenarios (range, time, etc.)
1. When there is only one read data source in the readwrite-splitting rule, it is not allowed to be disabled
1. Scaling: Add basic support of chunked streaming data consistency check
1. Shadow algorithm decision logic optimization to improve performance

### Refactor

1. Refactor federation engine scan table logic
1. Avoid duplicated TCL SQL parsing when executing prepared statement in Proxy
1. Scaling: Add pipeline modules to redesign scaling
1. Scaling: Refactor several job configuration structure
1. Scaling: Precalculate tasks splitting and persist in job configuration
1. Scaling: Add basic support of pipeline-core code reuse for encryption job
1. Scaling: Add basic support of scaling job and encryption job combined running
1. Scaling: Add `input` and `output` configuration, including `workerThread` and `rateLimiter`
1. Scaling: Move `blockQueueSize` into `streamChannel`
1. Scaling: Change jobId type from integer to text
1. Optimize JDBC to load only the specified schema
1. Optimize meta data structure of the registry center
1. Rename Note shadow algorithm to HINT shadow algorithm

### Bug Fixes

1. Support parsing function
1. Fix alter table drop constrain
1. Fix optimize table route
1. Support Route resource group
1. Support parsing binlog
1. Support postgreSql/openGauss '&' and '|' operator
1. Support parsing openGauss insert on duplicate key
1. Support parse postgreSql/openGauss union
1. Support query which table has column contains keyword
1. Fix missing parameter in function
1. Fix sub query table with no alias
1. Fix utc timestamp function
1. Fix alter encrypt column
1. Support alter column with position encrypt column
1. Fix delete with schema for postgresql
1. Fix wrong route result caused by oracle parser ambiguity
1. Fix projection count error when use sharding and encrypt
1. Fix npe when using shadow and readwrite_splitting
1. Fix wrong metadata when actual table is case insensitive
1. Fix encrypt rewrite exception when execute multiple table join query
1. Fix parsing chinese
1. Fix encrypt exists sub query
1. Fix full route caused by the MySQL BINARY keyword in the sharding condition
1. Fix getResultSet method empty result exception when using JDBCMemoryQueryResult processing statement
1. Fix incorrect shard table validation logic when creating store function/procedure
1. Fix null charset exception occurs when connecting Proxy with some PostgreSQL client
1. Fix executing commit in prepared statement cause transaction status incorrect in MySQL Proxy
1. Fix client connected to Proxy may stuck if error occurred in PostgreSQL with non English locale
1. Fix file not found when path of configurations contains blank character
1. Fix transaction status may be incorrect cause by early flush
1. Fix the unsigned datatype problem when query with PrepareStatement
1. Fix protocol violation in implementations of prepared statement in MySQL Proxy
1. Fix caching too many connections in openGauss batch bind
1. Fix the problem of missing data in SHOW READWRITE_SPLITTING RULES when db-discovery and readwrite-splitting are used together
1. Fix the problem of missing data in SHOW READWRITE_SPLITTING READ RESOURCES when db-discovery and readwrite-splitting are used together
1. Fix the NPE when the CREATE SHARDING TABLE RULE statement does not specify the sub-database and sub-table strategy
1. Fix NPE when PREVIEW SQL by schema.table
1. Fix DISABLE statement could disable readwrite-splitting write data source in some cases
1. Fix DIABLE INSTANCE could disable the current instance in some cases
1. Fix the issue that user may query the unauthorized logic schema when the provider is SCHEMA_PRIVILEGES_PERMITTED
1. Fix NPE when authority provider is not configured
1. Fix database discovery failed to modify cron configuration
1. Fix single read data source use weight loadbalance algorithm error
1. Fix create redundant data source without memory mode
1. Fix column value matching shadow algorithm data type conversion exception

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/17)

## 5.0.0

### New feature

1. Support parsing SQL comment
1. New DistSQL syntax: shadow rule management
1. New DistSQL syntax: scaling job management
1. New DistSQL syntax: disable proxy instance
1. New DistSQL syntax: disable readwrite-splitting read data source
1. New DistSQL syntax: `DROP SHARDING ALGORITHM`
1. New DistSQL syntax: `ALTER RESOURCE`
1. New DistSQL syntax: `CREATE SHARDING ALGORITHM`
1. New DistSQL syntax: `CREATE DEFAULT SHARDING [TABLE | DATABASE] STRATEGY`
1. New DistSQL syntax: `SHOW ALL VARIABLE`
1. New DistSQL syntax：`SHOW VARIABLE variableName;`
1. Support `3` modes, including Memory, Standalone and Cluster mode
1. Proxy supports for openGauss

### API Change

1. Add schema name configuration for ShardingSphere-JDBC
1. Add default sharding column configuration
1. Change the default authority provider from `NATIVE` to `ALL_PRIVILEGES_PERMITTED`
1. SCTL syntax adjustment, merged with DistSQL RAL syntax
1. `SHOW RESOURCES` DistSQL is adjusted to `SHOW SCHEMA RESOURCES`
1. Remove `shadow` logical field, support shadow algorithm

### Enhancement

1. Support parsing MySQL union/union all statement
1. Support PostgreSQL `ABORT` statement
1. Refactor and improve metadata loading process
1. Support PostgreSQL `CREATE INDEX` statement to generate index automatically when no index is specified
1. Support SQL statement execution with logical schema
1. Support binding tables configuration with different sharding columns
1. Optimize kernel performance
1. Proxy supports queries to part of information_schema tables to optimize client connection experience
1. DistSQL supports using quotation marks to use keywords as parameter names
1. The password in the `ADD RESOURCE` statement supports special characters
1. `ADD RESOURCE` supports custom JDBC parameters and connection pool properties
1. `DROP RESOURCE` supports optional parameter `ignore single tables`, used to ignore single table rule restrictions
1. Support the use of DistSQL to create sharding table rule based on readwrite-splitting rules
1. `SHOW DATABASES` statement supports like syntax
1. `CREATE SHARDING TABLE RULE` supports the use of inline expressions to specify resources
1. `CREATE SHARDING TABLE RULE` supports configuration using `dataNodes`
1. `CREATE SHARDING TABLE RULE` supports reuse of existing algorithms
1. `SET VARIABLE`, support to modify proxy configuration
1. PostgreSQL's protocol enhancements (Such as supports Portal, unspecified type)
1. Using Netty executor to optimize Proxy performance in specified scenarios
1. Make memory strictly fetch size configurable in Proxy

### Refactor

1. Refactor the SingleTable feature to support Encrypt multiple data sources
1. Adjust the persistent data structure of the registry center state node
1. Remove the SQL rewrite of DML for Shadow
1. Support the SQL routing of DDL for Shadow

### Bug Fixes

1. Fix sharding interval algorithm
1. Fix `SHOW INDEX FROM TABLE FROM TABLE` statement rewrite exception
1. Fix Encrypt multi tables join query rewrite exception
1. Fix subquery index out of range exception
1. Fix wrong result of Oracle paging query
1. Fix the rewrite exception when KeyGenerateStrategy is not configured in the Sharding feature
1. Fix federation executor engine exception caused by Oracle dialect case
1. Fix Sharding and Encrypt integration usage rewrite exception
1. Fix Oracle metadata loading exception
1. Fix the issue that `SHOW RESOURCES` statement cannot display custom attributes
1. Fix the issue that SQL execution exception is not thrown
1. Fix Etcd can not send node added event
1. Fix PostgreSQL rows contains null value may be missing in query result
1. Fix PostgreSQL metadata columns are out-of-order
1. Fix client character set may be incorrect in Proxy

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/16)

## 5.0.0-beta

### New feature

1. New DistSQL to load and show all the ShardingSphere configuration rules
1. Support join SQL from different database instances
1. Support multiple backend database types for new feature of database gateway
1. Support creating and updating the authentication online
1. Add new automated agent module

### API Change

1. `QueryReplica` configuration item is replaced by `read-write-splitting`
1. `Authentication` configuration item of ShardingProxy is replaced by `AUTHORITY`
1. Optimize the datasource configuration for ShardingJDBC with `dataSourceClassName`
1. New API for automated sharding table configuration to provide standard and automated sharding tables
1. Remove configuration item `acceptor-size` from ShardingProxy
1. Create a built-in sharding algorithm SPI which allows users to directly configure the class name as in 4.x

### Enhancement

1. Improve metadata loading process distinctly
1. Greatly enhance the SQL parsing for Oracle, SQLServer and PostgreSQL
1. Support loading privileges from MySQL/PostgreSQL/SQLServer/Oracle
1. Support DDL statement for encryption feature
1. Support rewrite owner table name of projection if using sharding and encrypt together
1. When using `SELECT *` from encrypt SQL, add quote char for rewritten column to avoid conflict with keyword
1. Support PostgreSQL JSON/JSONB/pattern matching operator parse
1. Support MySQL/PostgreSQL `CREATE/ALTER/DROP TABLESPACE` statement
1. Support PostgreSQL `PREPARE, EXECUTE, DEALLOCATE` statement
1. Support PostgreSQL `EXPLAIN` statement
1. Support PostgreSQL `START/END TRANSACTION` statement
1. Support PostgreSQL `ALTER/DROP INDEX` statement
1. Support CREATE `TABLESPACE` of PostgreSQL
1. Support MySQL `CREATE LOADABLE FUNCTION` statement
1. Support MySQL/PostgreSQL `ALTER TABLE RENAME` statement
1. Support PostgreSQL Close command

### Refactor

1. New schema structure in registry center
1. Remove Nacos and Apollo config center support
1. ShardingScaling leverages elasticJob as its job distribution

### Bug Fixes

1. Bug fix for cannot use `SELECT * wildcard` when readwrite-splitting only
1. Fix instance error when the custom sharding algorithm does not match the configuration type.
1. Fix NoSuchTableException when execute drop table if exists statement
1. Fix wrong table rewrite in `UPDATE ... SET ...` statement
1. Fix wrong table rewrite in CREATE/ALTER TABLE statement foreign key reference table
1. Fix projection owner check exception when exist subquery temporary table
1. Fix Oracle/SQL92 `SELECT ... WHERE ... LIKE` statement class cast exception
1. Fix MySQL `SELECT EXISTS ... FROM ...` statement parse error
1. Fix wrong result of SHOW INDEX statement
1. Fix SELECT `... GROUP BY ...` statement rewrite and merge result error
1. Fix CREATE TABLE statement rewrite error for encrypt
1. Fix exception occur in PostgreSQL Proxy when reading text format parameter values
1. Enhance the support of array object for PostgreSQL Proxy
1. Fix the bug of Datetype casting for ShardingProxy
1. PostgreSQL Proxy supports using numeric type
1. Fix PostgreSQL Proxy transaction command complete packet's tag incorrect
1. Fix PostgreSQL Proxy may return packet which is not expected by client

### Change Log

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/14)

## 5.0.0-alpha

### Build & Dependencies

1. Upgrade the minimum supported version of JDK to Java8
1. Update Zookeeper to version 3.6.x and curator to version 5.1.0
1. Update Google Guava to version 29.0-jre

### New Feature

1. The pluggable architecture is available and support function extension through the SPI
1. Independent SQL parsing engine is available to get SQL parsed AST for the different database dialects
1. New RDL(Rule Definition Language) feature for ShardingSphere Proxy supports creating sharding rules and sharding tables
1. ShardingSphere-Scaling supports resuming data migration from break-point
1. ShardingSphere-Scaling supports using ShardingSphere JDBC to migrate data to new cluster
1. ShardingSphere shadow database is available

### API Changes

1. New sharding/encryption/shadow/queryReplica API
1. New sharding algorithm and strategy API
1. New API for ShardingSphere Scaling to create task
1. Remove DefaultDataSourceName setting item
1. The separator of the parameter configuration item is changed from the dot ‘.’ to minus sign ‘-’
1. Change parameter allow.range.query.with.inline.sharding from global parameter to sharding algorithm parameter

### Refactor

1. Refactor the architecture of SQL parsing module base on the database dialects
1. Use SPI mechanism to reconstruct online metadata change processing
1. Rename Orchestration module to Governance module
1. Rename MasterSlave module to QueryReplica module
1. Refactor the metadata structure in the governance registration center

### Enhancement

1. The enhancement for MySQL and PostgreSQL SQL syntax definition and parsing process
1. The enhancement for sub-queries in different database dialects
1. Support MySQL view operations for non-sharding tables
1. Support MySQL stored function and procedure operations for non-sharding tables
1. Support SQLServer Top syntax
1. Optimize the metadata loading to improve the startup speed
1. Optimize batch insert performance
1. Supports the use of Oracle RAC JDBC URL
1. XA transaction manager adds support for Oracle database
1. ShardingSphere Proxy supports the use of p6sy driver
1. Add help information to the ShardingSphere Proxy startup script

### Bug Fixes

1. Fix alias rewriting error when processing OrderBy condition
1. Fix SQL rewriting error when MYSQL Insert statement contains expression
1. Fix parameter calculation error in update on duplicate SQL
1. Fix generatedKeys gets wrong when batch inserting
1. Fix the abnormal issue of multi-table verification in DML statement update operation
1. Fix the NPE problem caused by executing SQL when the table does not exist
1. Fix the exception when using the Show table command for an unconfigured table
1. Fix metadata loading error when Oracle database has multiple qualified users
1. Fix the issue that replica node cannot be enabled online
1. Fix the problem that ShardingSphere-JDBC does not support PostgreSQL array type
1. Fix the problem that ShardingSphere-Proxy does not respond when querying long blob data

###  Change Logs

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/10)

## 4.1.1

### Enhancement

1. Add Sharding-Scaling & Sharding-UI dockerfile
1. update MySQL & PostgreSQL version for proxy

### Bug Fixes

1. Fix parser syntax rule of SUBSTRING and CONVERT
1. Fix parser ColumnSegment ClassCastException
1. Fix TableMetaData load error when use Sharding-JDBC with oracle
1. Fix getSchema NPE when use Sharding-JDBC with oracle
1. Fix Sharding-JDBC parse sql NPE in PostgreSQL
1. Fix Sharding-Proxy receive error response for PostgreSQL JDBC client
1. Fix Sharding-Proxy response number of update is 0 for PostgreSQL JDBC client
1. Fix Sharding-Proxy receive null for PostgreSQL column meta data
1. Fix Sharding-Scaling NPE in MySQL incremental task

###  Change Logs

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/13)

## 4.1.0

### New Features

1. Support scaling for ShardingSphere (alpha version)
1. Move etcd registry center from experiment repo to apache repo
1. Upgrade to Java 8

### Enhancement

1. Optimization for Sharding Parser with ANTLR Visitor improving the parsing performance of long SQL by 100%~1000%
1. Use multiple threads to load metadata for different data sources
1. Support `allow.range.query.with.inline.sharding` option
1. The docker of ShardingSphere-Proxy supports loading external lib packages
1. Support integration with Spring using @ShardingSphereTransactionType
1. Enhance ShardingDataSource to compatible with Seata in micro-service distribution transaction

### Bug Fixes

1. Fix an exception caused by using a CHAR/VARCHAR type column as an order by item
1. Refine `DataTypeName` syntax rules of all database dialects
1. Fix an exception caused by executing `BEGIN` using prepared statement of MySQL C API
1. Fix the problem that `ALTER TABLE` fails to execute when the field type of the table contains Integer/Double/BigDecimal
1. Fix the problem of the stop index dislocation of segment with alias
1. Fix the problem that overwriting SQL `SELECT * FROM tbl WHERE encrypt_col = ? AND (sharding_key = ? OR sharding_key = ?)` when using sharding + encrypt would throw StringIndexOutOfBoundsException
1. Fix the problem of incorrect decoding after AES encoding when using ShardingSphere-Proxy in Spring Boot

###  Change Logs

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/12)

## 4.0.1

### Bug Fixes

1. Using guava cache to fix parsing deadlock.
1. Oracle insert SQL could not work in encrypt mode.
1. Proxy for PostgreSQL decode parameters error in all types except String.
1. COM_STM_EXECUTE of proxy for MySQL could not support sysbench.
1. None sharding strategy could not config in spring-boot.
1. WasNull field was wrong in GroupByStreamMergeResult.
1. Metadata.getColumns could not work in JDBC.
1. IN operator contains space and `\n` `\t` `\r` could not supported by parser.

### Enhancement

1. Optimize antlr performance using two-stage parsing strategy.

###  Change Logs

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/11)

## 4.0.0

### API Changes

1. Change package and maven groupId form `io.shardingsphere` to `org.apache.shardingsphere`.
1. Adjust ShardingSphere-JDBC configuration API.
1. Adjust persist structure for registry center.

### New Features

1. SQL92 Syntax available.
1. ShardingSphere-Proxy for PostgreSQL protocol available.
1. SQL 100% compatible if route to single data node.
1. Less-than(<), greater-than(>) and Less-than-equal(<=), greater-than-equal(>=) for sharding key operator available.
1. DISTINCT SQL syntax available.
1. Broadcast table available.
1. XA Transaction available, Atomikos, Narayana and Bitronix integrated.
1. BASE Transaction available, Seata integrated.
1. Data encrypt available.
1. Skywalking plugin available.
1. ShardingSphere-UI available, an orchestration management platform.

### Enhancement

1. MariaDB supported.
1. Improve the compatibility of SQL parsing.
1. `SELECT FOR UPDATE` route to primary data source only.
1. Hint in ShardingSphere-Proxy available.
1. Make configuration of orchestration consistent between ShardingSphere-JDBC and ShardingSphere-Proxy.
1. Renew modified data sources only, not renew all the data sources.
1. Vibrate configurable for Snowflake key generator.

### Bug Fixes

1. Improve the compatibility of JDBC Driver URL.

###  Change Logs

1. [MILESTONE #3](https://github.com/apache/shardingsphere/milestone/3)
1. [MILESTONE #4](https://github.com/apache/shardingsphere/milestone/4)
1. [MILESTONE #5](https://github.com/apache/shardingsphere/milestone/5)
1. [MILESTONE #6](https://github.com/apache/shardingsphere/milestone/6)
1. [MILESTONE #7](https://github.com/apache/incubator-shardingsphere/milestone/7)
1. [MILESTONE #8](https://github.com/apache/incubator-shardingsphere/milestone/8)
1. [MILESTONE #9](https://github.com/apache/incubator-shardingsphere/milestone/9)

## 4.0.0.RC3

### Enhancement

1. Support using less-than character(<) and greater-than character(>) for sharding data.
1. When primary and replica dataSources exist, support executing `SELECT FOR UPDATE` on primary data source.
1. Support hint in ShardingSphere-Proxy.
1. Finish parsing DAL syntax for MySQL.
1. Make configuration of orchestration compatible between ShardingSphere-JDBC and ShardingSphere-Proxy.

### Bug Fixes

1. Support delete statement with alias.
1. Check and disable updating sharding column.
1. Fix wrong type of TINYINT and SMALLINT as INTEGER.

### Change Logs

1. [MILESTONE](https://github.com/apache/incubator-shardingsphere/milestone/8)

## 4.0.0.RC2

### API Changes

1. Optimize and delete API and configuration item of sharding logic index.

### New Features

1. Integration of Seata for distributed transaction.
1. User can do data encryption by using ShardingProxy.
1. Support Skywalking plugin for application performance management.

### Enhancement

1. Renew modified dataSources, not all the datasources to improve performance for configuration orchestration.
1. Improve the compatibility of SQL parsing.
1. The parse engine upgrade from the 2nd generation to 3rd.

### Change Logs

1. [MILESTONE](https://github.com/apache/incubator-shardingsphere/milestone/7)


## 4.0.0.RC1

Merge all change logs of version 3.1.0.M1, 3.1.0, 3.1.0.1 and 4.0.0.M1. First apache release.

### API Changes

1. Adjust persist structure for orchestration's registry center.
1. Adjust ShardingSphere-JDBC configuration API.
1. Change package and maven groupId form `io.shardingsphere` to `org.apache.shardingsphere`.
1. Adjust spring-boot-starter.

### New Features

1. XA Transaction available.
1. Data encrypt available.
1. Use PostgreSQL protocol access ShardingSphere-Proxy available.
1. DISTINCT SQL syntax available.
1. Broadcast table.
1. All SQL 100% compatible if route to single data node (MySQL Only).

###  Change Logs

1. [MILESTONE #3](https://github.com/apache/shardingsphere/milestone/3)
1. [MILESTONE #4](https://github.com/apache/shardingsphere/milestone/4)
1. [MILESTONE #5](https://github.com/apache/shardingsphere/milestone/5)
1. [MILESTONE #6](https://github.com/apache/shardingsphere/milestone/6)


## 3.0.0

### Milestones

1. ShardingSphere-Proxy launch. Support the use of ShardingSphere in the form of database to support for MySQL CLI and GUI client

### New Features

#### Core

1. [ISSUE #290](https://github.com/apache/shardingsphere/issues/290) Support batch INSERT
1. [ISSUE #501](https://github.com/apache/shardingsphere/issues/501) Support OR
1. [ISSUE #980](https://github.com/apache/shardingsphere/issues/980) Support DCL
1. [ISSUE #1111](https://github.com/apache/shardingsphere/issues/1111) Support MySQL DAL

#### ShardingSphere-Proxy

1. [ISSUE #902](https://github.com/apache/shardingsphere/issues/902) Support XA transaction
1. [ISSUE #916](https://github.com/apache/shardingsphere/issues/916) Support authorization
1. [ISSUE #936](https://github.com/apache/shardingsphere/issues/936) Support registry center
1. [ISSUE #1046](https://github.com/apache/shardingsphere/issues/1046) Support multiple logic databases

### Enhancements

#### Core

1. [ISSUE #373](https://github.com/apache/shardingsphere/issues/373) Support `order by ?`
1. [ISSUE #610](https://github.com/apache/shardingsphere/issues/610) Route unicast for DQL without table
1. [ISSUE #701](https://github.com/apache/shardingsphere/issues/701) Caching parsed results to improve performance
1. [ISSUE #773](https://github.com/apache/shardingsphere/issues/773) Support sharding and autoincrement key of INSERT without column names
1. [ISSUE #935](https://github.com/apache/shardingsphere/issues/935) Use `YAML` instead of `JSON` to store configurations in registry center
1. [ISSUE #1004](https://github.com/apache/shardingsphere/issues/1004) Properties can configure for sharding and replica query independent
1. [ISSUE #1205](https://github.com/apache/shardingsphere/issues/1205) Execute engine enhancement

#### ShardingSphere-JDBC

1. [ISSUE #652](https://github.com/apache/shardingsphere/issues/652) Support `Spring Boot Starter` 2.X
1. [ISSUE #702](https://github.com/apache/shardingsphere/issues/702) Support `$->{..}` for inline expression
1. [ISSUE #719](https://github.com/apache/shardingsphere/issues/719) Support inject key generator objects by spring namespace
1. [ISSUE #720](https://github.com/apache/shardingsphere/issues/720) Support inject sharding algorithm objects by spring namespace

#### Sharding-Opentracing

1. [ISSUE #1172](https://github.com/apache/shardingsphere/issues/1172) Opentracing enhancement

### API changes

1. [ISSUE #1153](https://github.com/apache/shardingsphere/issues/1153) Adjust the maven artifactId for orchestration module
1. [ISSUE #1203](https://github.com/apache/shardingsphere/issues/1203) Adjust Spring namespace xsd for sharding and replica query
1. [ISSUE #1289](https://github.com/apache/shardingsphere/issues/1289) Adjust hint API
1. [ISSUE #1302](https://github.com/apache/shardingsphere/issues/1302) Refine package structure
1. [ISSUE #1305](https://github.com/apache/shardingsphere/issues/1305) Deprecated and remove sharding-jdbc-transaction-parent module
1. [ISSUE #1382](https://github.com/apache/shardingsphere/issues/1328) Remove type configuration in orchestration module

### Bug Fixes

#### Core

1. [ISSUE #569](https://github.com/apache/shardingsphere/issues/569) Failed to parse SQL for Oracle when ROWNUM is not at end
1. [ISSUE #628](https://github.com/apache/shardingsphere/issues/628) Support data type jsonb for PostgreSQL
1. [ISSUE #646](https://github.com/apache/shardingsphere/issues/646) When aliases in `SELECT ITEMS` correspond to the real column names of `GROUP BY` or `ORDER BY`, there is no need to generate derived columns
1. [ISSUE #806](https://github.com/apache/shardingsphere/issues/806) `NOT IN` parse exception
1. [ISSUE #827](https://github.com/apache/shardingsphere/issues/827) Endless loop for bad SQL like `SELECT * FROM table WHERE id IN ()`
1. [ISSUE #919](https://github.com/apache/shardingsphere/issues/919) Inline expression with groovy may cause memory leak
1. [ISSUE #993](https://github.com/apache/shardingsphere/issues/993) Fail to parsing PostgreSQL due to the quotation
1. [ISSUE #1015](https://github.com/apache/shardingsphere/issues/1015) Support SQL like `SELECT id, COUNT(*) FROM table GROUP BY 1,2`
1. [ISSUE #1120](https://github.com/apache/shardingsphere/issues/1120) Derived columns of `GROUP BY / ORDER BY` appear in query result
1. [ISSUE #1186](https://github.com/apache/shardingsphere/issues/1186) Dead lock may occur on MEMORY_STRICTLY mode when get connection on concurrency environment
1. [ISSUE #1265](https://github.com/apache/shardingsphere/issues/1265) RoundRobinReplicaLoadBalanceAlgorithm throw an ArrayIndexOutOfBoundsException when AtomicInteger overflow

#### ShardingSphere-JDBC

1. [ISSUE #372](https://github.com/apache/shardingsphere/issues/372) Reuse PreparedStatement cause cache of route result do not clean
1. [ISSUE #629](https://github.com/apache/shardingsphere/issues/629) Support transaction isolation on JDBC
1. [ISSUE #735](https://github.com/apache/shardingsphere/issues/735) Unexpected replica datasource routing result when using `Round-robin` load-balance algorithm in Mybatis
1. [ISSUE #1011](https://github.com/apache/shardingsphere/issues/1011) Can't resolve placeholder in `Spring Boot YAML` configuration

## 2.0.3

### New Features

#### Core

1. [ISSUE #600](https://github.com/apache/shardingsphere/issues/600) Support TCL

### Bug Fixes

#### Core

1. [ISSUE #540](https://github.com/apache/shardingsphere/issues/540) Support SQL that alias is the keyword
1. [ISSUE #577](https://github.com/apache/shardingsphere/issues/577) Support new line for `YAML` configuration

#### ShardingSphere-JDBC

1. [ISSUE #522](https://github.com/apache/shardingsphere/issues/522) Replica database does not need to execute the DDL for replica query


## 2.0.2

### Enhancements

#### Core

1. [ISSUE #475](https://github.com/apache/shardingsphere/issues/475) Support `CREATE INDEX`
1. [ISSUE #525](https://github.com/apache/shardingsphere/issues/525) Support `DROP INDEX`

### Bug Fixes

#### Core

1. [ISSUE #521](https://github.com/apache/shardingsphere/issues/521) `ShardingProperties` is invalid in `YAML` configuration
1. [ISSUE #529](https://github.com/apache/shardingsphere/issues/529) Table name capitalization cannot be queried
1. [ISSUE #541](https://github.com/apache/shardingsphere/issues/541) `IS NOT NULL` parse error
1. [ISSUE #557](https://github.com/apache/shardingsphere/issues/557) When `GROUP BY` and `ORDER BY` aliases are inconsistent, stream merging should be used
1. [ISSUE #559](https://github.com/apache/shardingsphere/issues/559) Support parsing numbers beginning with minus and decimal (e.g. `-.12`)
1. [ISSUE #567](https://github.com/apache/shardingsphere/issues/567) Add escape char for derived columns or alias when using MySQL

#### ShardingSphere-JDBC

1. [ISSUE #520](https://github.com/apache/shardingsphere/issues/520) Exception is no longer `DuplicateKeyException` when the unique key conflict


## 2.0.1

### Enhancements

#### Core

1. [ISSUE #490](https://github.com/apache/shardingsphere/issues/490) Using `rownum` greater than or equal to or less than or equal to the result of paging is incorrect in Oracle
1. [ISSUE #496](https://github.com/apache/shardingsphere/issues/496) Logical table names in sharding configuration can be case sensitive
1. [ISSUE #497](https://github.com/apache/shardingsphere/issues/497) Close registry center gracefully

### Bug Fixes

#### ShardingSphere-JDBC

1. [ISSUE #489](https://github.com/apache/shardingsphere/issues/489) Uses `RuntimeBeanReference` to prevent the creation of `InnerBean` on spring namespace
1. [ISSUE #491](https://github.com/apache/shardingsphere/issues/491) Can't close connection by `ResultSet.getStatement().getConnection().close()`


## 2.0.0

### Milestones

1. API adjust. Brand new groupId and artifactId for `Maven`, package name and spring namespace name. Simplify and enhance API configuration, inline expression fully configuration support
1. Support `spring-boot-starter` of `ShardingSphere-JDBC`
1. Dynamic configuration. `ZooKeeper` and `etcd` can be used as registry to dynamically modify data sources and sharding configurations
1. Database orchestration. Fusing database access procedures to access databases and disable access to replica databases
1. ConfigMap support. Predefined metadata can be obtained in the sharding and replica query strategy
1. Tracking system support. You can view the invocation chain of `ShardingSphere-JDBC` through `sky-walking` and other `Opentracing` based APM systems

### Enhancements

#### Core

1. [ISSUE #386](https://github.com/apache/shardingsphere/issues/386) Support SQL that does not contain table names, such as `SELECT 1`

#### ShardingSphere-JDBC

1. [ISSUE #407](https://github.com/apache/shardingsphere/issues/407) Support Hyphen properties for `sharding-jdbc-spring-boot-starter`
1. [ISSUE #424](https://github.com/apache/shardingsphere/issues/424) Providing SQL overall execution events

### Bug Fixes

#### Core

1. [ISSUE #387](https://github.com/apache/shardingsphere/issues/387) Prevent errors from keywords process when '`' exists in function + column name
1. [ISSUE #419](https://github.com/apache/shardingsphere/issues/419) When SQL is rewritten, it does not determine whether alias is a keyword without the escape character, which results in SQL exception
1. [ISSUE #464](https://github.com/apache/shardingsphere/issues/464) SQL if the varchar type is not closed due to the absence of matching single quotes, and the next varchar in SQL is the wrong SQL of Chinese characters, it will lead to higher use of CPU

#### ShardingSphere-JDBC

1. [ISSUE #394](https://github.com/apache/shardingsphere/issues/394) Can't only close statement
1. [ISSUE #398](https://github.com/apache/shardingsphere/issues/398) Use Hint routing to shield case sensitivity
1. [ISSUE #404](https://github.com/apache/shardingsphere/issues/404) Sharding-jdbc's spring-boot-starter does not support HikariDataSource
1. [ISSUE #436](https://github.com/apache/shardingsphere/issues/436) Replica query, when the RoundRobin algorithm is configured from the database and MyBatis is used, it can only be routed to the same replica database
1. [ISSUE #452](https://github.com/apache/shardingsphere/issues/452) Sharding of DDL statements to more than one table causes a connection leak
1. [ISSUE #472](https://github.com/apache/shardingsphere/issues/472) Before Connection executes createStatement, it calls getMetaData first and then setAutoCommit can not take effective connection to the database that was created later

## 1.5.4.1

### Bug Fixes

1. [ISSUE #382](https://github.com/apache/shardingsphere/issues/382) The query cannot be completed without sharding rule

## 1.5.4

### Bug Fixes

1. [ISSUE #356](https://github.com/apache/shardingsphere/issues/356) In the Where condition of SQL, the REGEXP operator is compatible with non sharding columns
1. [ISSUE #362](https://github.com/apache/shardingsphere/issues/362) Replica query using PreparedStatement does not invoke the setParameter method to cause errors
1. [ISSUE #370](https://github.com/apache/shardingsphere/issues/370) Error in calling getGeneratedKeys using native self increment primary key
1. [ISSUE #375](https://github.com/apache/shardingsphere/issues/375) Data can not be obtained after paging second pages route to a single node
1. [ISSUE #379](https://github.com/apache/shardingsphere/issues/379) When Mybatis is used to call Connection.getMetaData (), the connection is not close correct

## 1.5.3

### Enhancements

1. [ISSUE #98](https://github.com/apache/shardingsphere/issues/98) Replica query load balancing strategy support configuration
1. [ISSUE #196](https://github.com/apache/shardingsphere/issues/196) Replica query and sharding configuration independence

### Bug Fixes

1. [ISSUE #349](https://github.com/apache/shardingsphere/issues/349) Incorrect function of ResultSet.wasNull causes null numeric type in DB to zero
1. [ISSUE #351](https://github.com/apache/shardingsphere/issues/351) Tables that are included in the default data source but not in TableRule configuration are not properly executed
1. [ISSUE #353](https://github.com/apache/shardingsphere/issues/353) In the Where condition of SQL, it is compatible with non sharding columns !=, !> and !< operator
1. [ISSUE #354](https://github.com/apache/shardingsphere/issues/354) In the Where condition of SQL, NOT operators are compatible with non-sharding columns

## 1.5.2

### Milestones

1. The test engine of quality assurance, each SQL can run 60 test cases of different dimensions

### Enhancements

1. [ISSUE #335](https://github.com/apache/shardingsphere/issues/335) Support the GROUP BY + custom function SQL
1. [ISSUE #341](https://github.com/apache/shardingsphere/issues/341) Support ORDER BY xxx NULLS FIRST | LAST statement of Oracle

### Bug Fixes

1. [ISSUE #334](https://github.com/apache/shardingsphere/issues/334) Parsing ORDER BY with functions will resolve the following ASC and DESC to the name attribute of OrderItem
1. [ISSUE #335](https://github.com/apache/shardingsphere/issues/339) JOIN parsing is incorrect using the full name association of the table
1. [ISSUE #346](https://github.com/apache/shardingsphere/issues/346) Parsing table name error of DDL statement DROP TABLE IF EXISTS USER

## 1.5.1

### New Features

1. [ISSUE #314](https://github.com/apache/shardingsphere/issues/314) Support DDL type SQL

### Changes

1. [ISSUE #327](https://github.com/apache/shardingsphere/issues/327) Close sql.show configuration by default

### Bug Fixes

1. [ISSUE #308](https://github.com/apache/shardingsphere/issues/308) Invalid return of database native GeneratedKey
1. [ISSUE #309](https://github.com/apache/shardingsphere/issues/310) ORDER BY and GROUP BY in sub-queries are not included in the analytic context
1. [ISSUE #313](https://github.com/apache/shardingsphere/issues/313) Support <> operator
1. [ISSUE #317](https://github.com/apache/shardingsphere/issues/317) Parameter of LIMIT can not be type of Long
1. [ISSUE #320](https://github.com/apache/shardingsphere/issues/320) SQL rewriting error of GROUP BY + LIMIT
1. [ISSUE #323](https://github.com/apache/shardingsphere/issues/323) Parsing ORDER BY + Aggregation Expression error

## 1.5.0

### Milestones

1. The new SQL parsing module removes the dependence on Druid. We only need to parse the sharding context, and adopt a "semi understanding" concept for SQL to further improve performance and compatibility, and reduce code complexity
1. The new SQL rewrite module adds an optimized rewrite module
1. The new SQL merge module is rebuilt into 3 merging engines: streaming, memory and decorator

### New Features

1. Support Oracle, SQLServer and PostgreSQL
1. Non functional sub-query support

### Enhancements

1. [ISSUE #256](https://github.com/apache/shardingsphere/issues/256) Configurable display sharding execute to SQL log
1. [ISSUE #291](https://github.com/apache/shardingsphere/issues/291) Processing SQL use stream mode that contains only GroupBy

### Changes

1. Simplify the distributed self increasing sequence. Each table is supported by simplifying a multiple self increasing sequence to a single table supporting only a single distributed self increasing sequence, and no longer supporting workerID settings through environment variables
1. Remove support for OR

### Bug Fixes

1. [ISSUE #239](https://github.com/apache/shardingsphere/issues/239) LIMIT routes to multiple query result sets. If there is only one result set that is not empty, the result of paging is incorrect
1. [ISSUE #263](https://github.com/apache/shardingsphere/issues/263) Sharding and logical table configuration can be case insensitive
1. [ISSUE #292](https://github.com/apache/shardingsphere/issues/292) When the memory mode handles GROUP BY statement, if there is paging information, it needs to be rewritten
1. [ISSUE #295](https://github.com/apache/shardingsphere/issues/295) LIMIT 0 does not filter the result set according to paging restrictions

## 1.4.2

### Enhancements

1. [ISSUE #219](https://github.com/apache/shardingsphere/issues/219) Thread performance optimization
1. [ISSUE #215](https://github.com/apache/shardingsphere/issues/215) Aggregated result set of stream sort StreamingOrderByReducerResultSet performance optimization
1. [ISSUE #161](https://github.com/apache/shardingsphere/issues/161) When the result sets are merged, heap sort can be used to improve performance

### Bug Fixes

1. [ISSUE #212](https://github.com/apache/shardingsphere/issues/212) More meaningful hints for missing data source rules
1. [ISSUE #214](https://github.com/apache/shardingsphere/issues/214) table_name.column_name in (?,?) in WHERE can't parser expression
1. [ISSUE #180](https://github.com/apache/shardingsphere/issues/180) Batch execution of Update return inaccuracy
1. [ISSUE #225](https://github.com/apache/shardingsphere/issues/225) The last character of automatic generation of Id is not 0

## 1.4.1

### Enhancements

1. [ISSUE #191](https://github.com/apache/shardingsphere/issues/191) Generating KeyGenerator of workerId based on IP of host
1. [ISSUE #192](https://github.com/apache/shardingsphere/issues/192) Get workerId's KeyGenerator based on HOSTNAME's digital suffix
1. [ISSUE #210](https://github.com/apache/shardingsphere/issues/210) Routing to single database and single table to remove supplementary SQL statement fragments

### Bug Fixes

1. [ISSUE #194](https://github.com/apache/shardingsphere/issues/194) Some component exceptions in the close method of Connection, Statement, ResultSet and other interfaces cause the close method of another component to be not invoked
1. [ISSUE #199](https://github.com/apache/shardingsphere/issues/199) Sharding and reuse PreparedStatement object cause route error
1. [ISSUE #201](https://github.com/apache/shardingsphere/issues/201) Event transmission missing before batch operation execution
1. [ISSUE #203](https://github.com/apache/shardingsphere/issues/203) Merge events sent by the batch operation
1. [ISSUE #209](https://github.com/apache/shardingsphere/issues/209) Parallel execution of multiple limit queries leads to IndexOutOfBoundsException

## 1.4.0

### Enhancements

Automatic generation key implementation, including

1. [ISSUE #162](https://github.com/apache/shardingsphere/issues/162) Implementation of distributed primary key algorithm
1. [ISSUE #163](https://github.com/apache/shardingsphere/issues/163) Acquisition of a self increasing sequence of JDBC interfaces
1. [ISSUE #171](https://github.com/apache/shardingsphere/issues/171) Sharding-jdbc-core coordination automatic generation sequence transformation
1. [ISSUE #172](https://github.com/apache/shardingsphere/issues/172) The configuration of YAML and Spring supports the self increasing sequence

### Bug Fixes

1. [ISSUE #176](https://github.com/apache/shardingsphere/issues/176) The wasNull flag of AbstractMemoryResultSet is not reset in time

## 1.3.3

### Enhancements

1. [ISSUE #59](https://github.com/apache/shardingsphere/issues/59) PreparedStatement can call the correct underlying set method according to the parameter type when setting parameters

### Bug Fixes

1. [ISSUE #149](https://github.com/apache/shardingsphere/issues/149) When INSERT IGNORE INTO, if the data is duplicated, the value returned to -1 when ignored, and it should be returned to 0
1. [ISSUE #118](https://github.com/apache/shardingsphere/issues/118) In the same thread, DQL is executed first, then DML is executed, and DML operation is executed from the replica database
1. [ISSUE #122](https://github.com/apache/shardingsphere/issues/122) In cases where connections are not available (such as network interruption), transactions should be interrupted rather than retry
1. [ISSUE #152](https://github.com/apache/shardingsphere/issues/152) PreparedStatement's cache causes an array out of bound
1. [ISSUE #150](https://github.com/apache/shardingsphere/issues/150) With the latest SQLServer jdbc driver compatibility problem, Product Name should be changed from SQLServer to Microsoft SQL Server
1. [ISSUE #166](https://github.com/apache/shardingsphere/issues/166) Druid data source stat filter multi-thread error reporting should be added to database connection level synchronization

## 1.3.2

### Enhancements

1. [ISSUE #79](https://github.com/apache/shardingsphere/issues/79) Optimizes limit for only one target table, does not modify limit offset

### Bug Fixes

1. [ISSUE #36](https://github.com/apache/shardingsphere/issues/36) ShardingPreparedStatement cannot set parameters repeatedly
1. [ISSUE #114](https://github.com/apache/shardingsphere/issues/114) When ShardingPreparedStatement performs batch tasks, it repeatedly analyzes SQL and leads to OOM
1. [ISSUE #33](https://github.com/apache/shardingsphere/issues/33) According to the MySQL document, queries similar to limit 100 and -1 format are not supported
1. [ISSUE #124](https://github.com/apache/shardingsphere/issues/124) The return value of com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractStatementAdapter.getUpdateCount does not conform to the JDBC specification
1. [ISSUE #141](https://github.com/apache/shardingsphere/issues/141) Multi-thread executor parameter setting failure


## 1.3.1

### Enhancements

1. [ISSUE #91](https://github.com/apache/shardingsphere/issues/91) Open support for Statement.getGeneratedKeys can return the original database self increase primary key
1. [ISSUE #92](https://github.com/apache/shardingsphere/issues/92) Query DQL statement event sending

### Bug Fixes

1. [ISSUE #89](https://github.com/apache/shardingsphere/issues/89) Use replica query with sharding hint leads to conflict
1. [ISSUE #95](https://github.com/apache/shardingsphere/issues/95) Write operations in the same thread read from the primary database changed to the same thread and within the same connection

## 1.3.0

### New Features

1. [ISSUE #85](https://github.com/apache/shardingsphere/issues/85) New feature for replica query

### Enhancements

1. [ISSUE #82](https://github.com/apache/shardingsphere/issues/82) TableRule can import the dataSourceName attribute to specify the data source corresponding to the TableRule
1. [ISSUE #88](https://github.com/apache/shardingsphere/issues/88) Release restrictions on other databases, support standard SQL, do not support personalized paging statements

### Bug Fixes

1. [ISSUE #81](https://github.com/apache/shardingsphere/issues/81) Associative table query uses OR query condition to resolve the result exceptions

## 1.2.1

### Refactor

1. [ISSUE #60](https://github.com/apache/shardingsphere/issues/60) API adjust, remove ShardingDataSource, use factory instead
1. [ISSUE #76](https://github.com/apache/shardingsphere/issues/76) ShardingRule and TableRule change to Builder pattern
1. [ISSUE #77](https://github.com/apache/shardingsphere/issues/77) ShardingRule and TableRule change to Builder pattern

### Enhancements

1. [ISSUE #61](https://github.com/apache/shardingsphere/issues/61) Add the logical table name to the ShardingValue class
1. [ISSUE #66](https://github.com/apache/shardingsphere/issues/66) Statement on the JDBC tier supports get/set MaxFieldSize, MaxRows and QueryTimeout
1. [ISSUE #72](https://github.com/apache/shardingsphere/issues/72) Batch inserts supporting select union all
1. [ISSUE #78](https://github.com/apache/shardingsphere/issues/78) Simplifying sharding only configuration, without configuring logical table and real table correspondence
1. [ISSUE #80](https://github.com/apache/shardingsphere/issues/80) Simplifying the configuration that does not sharding, specifying the default data source, do not need configure TableRule

### Bug Fixes

1. [ISSUE #63](https://github.com/apache/shardingsphere/issues/63) No table name or table alias is added to the ORDER BY and GROUP BY derivation columns
1. [ISSUE #65](https://github.com/apache/shardingsphere/issues/65) Performance enhancement for parsing condition context
1. [ISSUE #67](https://github.com/apache/shardingsphere/issues/67) The soft transaction log cannot be deleted when routed to multiple tables
1. [ISSUE #71](https://github.com/apache/shardingsphere/issues/71) Routing single sharding key by OFFSET of LIMIT error
1. [ISSUE #75](https://github.com/apache/shardingsphere/issues/75) MemoryTransactionLogStorage retry times update concurrency problem

## 1.2.0

### New Features

1. [ISSUE #53](https://github.com/apache/shardingsphere/issues/53) The relationship between the real table and the logical table is not configured, and the real table is dynamically calculated by the sharding algorithm
1. [ISSUE #58](https://github.com/apache/shardingsphere/issues/58) Soft transaction: the initial version of the best effort type

### Refactor

1. [ISSUE #49](https://github.com/apache/shardingsphere/issues/49) Adjust the property configuration
1. [ISSUE #51](https://github.com/apache/shardingsphere/issues/51) Refactor of Hint interface

### Bug Fixes

1. [ISSUE #43](https://github.com/apache/shardingsphere/issues/43) The yaml file contains Chinese, and the operating system mode is not UTF-8 encoding, resulting in yaml can not be parsed
1. [ISSUE #48](https://github.com/apache/shardingsphere/issues/48) Yaml file is not closed after reading
1. [ISSUE #57](https://github.com/apache/shardingsphere/issues/57) At the analytic level, we can identify subqueries to ensure that the behavior of supplementary columns can be accurately positioned

## 1.1.0

### New Features

1. [ISSUE #40](https://github.com/apache/shardingsphere/issues/40) Support YAML configuration
1. [ISSUE #41](https://github.com/apache/shardingsphere/issues/41) Support Spring namespace configuration
1. [ISSUE #42](https://github.com/apache/shardingsphere/issues/42) Support inline expression configuration

### Bug Fixes

1. [ISSUE #25](https://github.com/apache/shardingsphere/issues/25) The problem of repeated results will appear under the OR expression

## 1.0.1

### Enhancements

1. [ISSUE #39](https://github.com/apache/shardingsphere/issues/39) Support the use of Hint method to register the key value to SQL routing

### Bug Fixes

1. [ISSUE #11](https://github.com/apache/shardingsphere/issues/11) The count function returns incorrectly without aliases
1. [ISSUE #13](https://github.com/apache/shardingsphere/issues/13) The Insert statement does not write column names or write column names but column names do not contain sharding fields, occur broadcast route
1. [ISSUE #16](https://github.com/apache/shardingsphere/issues/16) For now a new connection pool is executed every time SQL is executed. Instead, each ShardingDataSource object should be changed to share a pool
1. [ISSUE #18](https://github.com/apache/shardingsphere/issues/18) When query Count, getObject() throws Exception: Unsupported data type: Object
1. [ISSUE #19](https://github.com/apache/shardingsphere/issues/19) In SUM and AVG functions, merger is not executed if aliases are not added, and null pointer exception fired if aliases are added
1. [ISSUE #38](https://github.com/apache/shardingsphere/issues/38) The compatibility between JPA and ShardingSphere-JDBC. JPA automatically add the column aliases of SELECT, resulting in ORDER BY obtaining ResultSet data only by aliases rather than column names

## 1.0.0

1. Initial version
