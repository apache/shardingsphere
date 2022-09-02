## 5.2.0

### New Feature

1. Support SQL audit for sharding feature
1. Support MySQL show processlist and kill process list id feature
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
1. broadcast table support update/delete limit statement
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
1. Support parsing MySQL DO statement.
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
1. Support listen statement in postgreSQL
1. Support DECLARE cursor statement
1. add default serverConfig in helm charts
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
1. DistSQL: Add paramter `writeDataSourceQueryEnabled` for RDL READWRITE_SPLITTING RULE.
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

### Bug Fix

1. Fix single table metadata refresh error caused by filtering DataSourceContainedRule
1. Fix parsing exception caused by the null value of MySQL blob type
1. Fix PostgreSQL/openGauss reset statement parse error
1. Fix wrong parameter rewrite when use sharding and encrypt
1. Fix the failed conversion of Month related classes on IntervalShardingAlgorithm
1. Fix NullPointerException when execute select union statement contains subquery
1. Fix wrong encrypt rewrite result due to incorrect order of metadata
1. fix MySQL trim function parse error
1. fix MySQL insert values with _binary parse error
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

### Bug Fix

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
1. Scaling: Fix data source property url and jdbcUrl compatibility
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

### Bug Fix
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

### Bug Fix

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
1. Fix encrypt rewrite wrong result with table level queryWithCipherColumn
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
1. Scaling: Fix DB connection leak on XA initialization which triggered by data consistency check
1. Scaling: Fix PostgreSQL replication stream exception on multiple data sources
1. Scaling: Fix migrating updated record exception on PostgreSQL incremental phase
1. Scaling: Fix MySQL 5.5 check BINLOG_ROW_IMAGE option failure
1. Scaling: Fix PostgreSQL xml data type consistency check
1. Fix database discovery failed to modify cron configuration
1. Fix single read data source use weight loadbalance algorithm error
1. Fix create redundant data souce without memory mode
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
1. Scaling: Add basic support for openGauss
1. Scaling: Add incremental task completion detect algorithm SPI interface
1. Scaling: Add data consistency check algorithm SPI interface
1. Scaling: Basic support of create table on target automatically for MySQL and openGauss
1. Scaling: Support auto switch cluster configuration when job finished
1. Scaling: Add more DistSQL support such as data consistency check, etc

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
1. Scaling: Improve support for PostgreSQL
1. Scaling: Support concurrent data consistency check of source and target side

### Refactor

1. Refactor the SingleTable feature to support Encrypt multiple data sources
1. Adjust the persistent data structure of the registry center state node
1. Remove the SQL rewrite of DML for Shadow
1. Support the SQL routing of DDL for Shadow
1. Scaling: Refactor default implementation of incremental task completion detect algorithm
1. Scaling: Refactor default implementation of data consistency check algorithm
1. Scaling: Remove HTTP API and binary distribution

### Bug Fix

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
1. Refactor the metadata content and its update process

### Bug Fix

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
1. Refactor GovernmentShardingSphereDataSource
1. ShardingSphere UI merges configuration center and registration center display layout

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

1. Optimization for Sharing Parser with ANTLR Visitor improving the parsing performance of long SQL by 100%~1000%
1. Use multiple threads to load metadata for different data sources
1. Support `allow.range.query.with.inline.sharding` option
1. The docker of ShardingSphere-Proxy supports loading external lib packages
1. Support integration with Spring using @ShardingSphereTransactionType
1. Enhance ShardingDataSource to compatible with Seata in micro-service distribution transaction

### Refactor

1. Remove leaf key generator

### Bug Fixes

1. Fix an exception caused by using a CHAR/VARCHAR type column as an order by item
1. Refine `DataTypeName` syntax rules of all database dialects
1. Fix an exception caused by executing `BEGIN` using prepared statement of MySQL C API
1. Fix the problem that `ALTER TABLE` fails to execute when the field type of the table contains Integer/Double/BigDecimal
1. Fix the problem of the stop index dislocation of segment with alias
1. Fix the problem that overwriting SQL `SELECT * FROM tbl WHERE encrypt_col = ? AND (sharding_key = ? OR sharding_key = ?)` when using sharding + encrypt would throw StringIndexOutOfBoundsException
1. Fix the problem of incorrect decoding after AES encoding when using ShardingSphere-Proxy in Spring Boot
1. Fix a long-time problem of adding schema dynamically in ShardingSphere-UI

###  Change Logs

1. [MILESTONE](https://github.com/apache/shardingsphere/milestone/12)

## 4.0.1

### Bug Fixes

1. Using guava cache to fix parsing deadlock.
1. Oracle insert SQL could not work in encrypt mode.
1. Proxy for PostgreSQL decode parameters error in all types except String.
1. COM_STM_EXECUTE of proxy for MySQL could not support sysbench.
1. None sharding strategy could not config in spring-boot.
1. Plain column could not get from resultSet in encrypt mode.
1. WasNull field was wrong in GroupByStreamMergeResult.
1. Metadata.getColumns could not work in JDBC.
1. IN operator contains space and `\n` `\t` `\r` could not supported by parser.

### Enhancement

1. Optimize antlr performance using two-stage parsing strategy.
1. Add class filter constructor to restrict the illegal class from YAML.

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
1. LEAF key generator available.
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
1. Delete statement with alias available.
1. Check and disable updating sharding column.
1. Fix wrong type of TINYINT and SMALLINT as INTEGER.

###  Change Logs

1. [MILESTONE #3](https://github.com/apache/shardingsphere/milestone/3)
1. [MILESTONE #4](https://github.com/apache/shardingsphere/milestone/4)
1. [MILESTONE #5](https://github.com/apache/shardingsphere/milestone/5)
1. [MILESTONE #6](https://github.com/apache/shardingsphere/milestone/6)
1. [MILESTONE #7](https://github.com/apache/incubator-shardingsphere/milestone/7)
1. [MILESTONE #8](https://github.com/apache/incubator-shardingsphere/milestone/8)
1. [MILESTONE #9](https://github.com/apache/incubator-shardingsphere/milestone/9)

## 4.0.0.RC3

### New Features

1. ShardingSphere-UI, an orchestration management platform for ShardingSphere comes online.
1. Not only SQLs from MySQL, PostgreSQL, SQLServer, Oracle, but any SQL92 Syntax can be parsed correctly and used in ShardingSphere.

### Enhancement

1. Support using less-than character(<) and greater-than character(>) for sharding data.
1. When primary and replica dataSources exist, support executing `SELECT FOR UPDATE` on primary data source.
1. Support hint in ShardingSphere-Proxy.
1. Finish parsing DAL syntax for MySQL.
1. Make configuration of orchestration compatible between ShardingSphere-JDBC and ShardingSphere-Proxy.

### Bug Fixes

1. Through Bug fix, the feature of encryption becomes much stable and applicable.
1. Support delete statement with alias.
1. Check and disable updating sharding column.
1. Fix wrong type of TINYINT and SMALLINT as INTEGER.

### Refactor

1. Rename optimized module to preprocessor module.
1. Decouple rewrite core module and sharding/encrypt features.

### Change Logs

1. [MILESTONE](https://github.com/apache/incubator-shardingsphere/milestone/8)

## 4.0.0.RC2

### API Changes

1. Optimize and delete API and configuration item of sharding logic index.
1. Update the API of encryption to support the encrypted and plain data coexistence.

### New Features

1. Integration of Seata for distributed transaction.
1. User can do data encryption by using ShardingProxy.
1. User can use Leaf-segment generator to get distributed ID.
1. Support Skywalking plugin for application performance management.

### Enhancement

1. Renew modified dataSources, not all the datasources to improve performance for configuration orchestration.
1. Improve the compatibility of SQL parsing.

### Refactor

1. Remove DatabaseType enum, use dynamic SPI to load data source type.
1. The parse engine upgrade from the 2nd generation to 3rd.
1. The Refactoring of SQL rewriting module.

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
