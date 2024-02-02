+++
title = "Apache ShardingSphere 5.2.0 is Released!"
weight = 74
chapter = true 
+++

Our new 5.2.0 release enhances features such as SQL audit, elastic migration, SQL execution process management, and data governance on the cloud.

### Introduction

Since [Apache ShardingSphere](https://shardingsphere.apache.org/) released version 5.1.2 at the end of June, our community has continued to optimize and enhance its product features. The community merged 1,728 PRs from teams and individuals around the world. The resulting 5.2.0 release has been optimized in terms of its features, performance, testing, documentation, examples, etc.

The establishment of the [shardingsphere-on-cloud](https://github.com/apache/shardingsphere-on-cloud) sub-project shows ShardingSphere’s commitment to being cloud native. We welcome anyone interested in Go, database, and cloud to join the shardingsphere-on-cloud community.

**The 5.2.0 release brings the following highlights:**

* SQL audit for data sharding.

* Elastic data migration.

* SQL execution process management.

* Shardingsphere-on-cloud sub-project goes live.

Newly added features, including SQL audit for data sharding and MySQL SHOW PROCESSLIST & KILL, can enhance users’ capability to manage ShardingSphere.

The SQL audit feature allows users to manage SQL audit to prevent the business system from being disrupted by inefficient SQL. The MySQL SHOW PROCESSLIST & KILL feature allows users to quickly view the SQL in execution through the SHOW PROCESSLIST statement, and forcibly cancel slow SQL.

**The new version also supports elastic data migration. It supports the migration of data from [Oracle](https://www.oracle.com/index.html), [MySQL](https://www.mysql.com/), and [PostgreSQL](https://www.postgresql.org/) to the distributed database ecosystem composed of ShardingSphere + MySQL or PostgreSQL, completing the transformation from a single database to a distributed one.** The ShardingSphere community will support more features for heterogeneous database migration in future releases. Stay tuned for more updates.

The new version also transferred Helm Charts from the ShardingSphere repository to the **shardingsphere-on-cloud sub-project. It is designed to provide distributed database solutions of ShardingSphere + MySQL or PostgreSQL on the cloud.** This version significantly improves SQL parsing support for different databases and upgrades [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/)’s parameter usage specifications. It removes the Memory operating mode from ShardingSphere mode and supports distributed transactions across multiple logical databases. This post will introduce the updates of ShardingSphere 5.2.0.

### Highlights

### SQL audit for data sharding

In large-scale data sharding scenarios, if a user executes an SQL query without the sharding feature, the SQL query will be routed to the underlying database for execution.

As a result, a large number of database connections will be occupied and businesses will be severely affected by timeout or other issues. If the user performs UPDATE/DELETE operations, a large amount of data may be incorrectly updated or deleted.

In response to the above problems, ShardingSphere 5.2.0 provides the SQL audit for data sharding feature and allows users to configure audit strategies. The strategy specifies multiple audit algorithms, and users can decide whether audit rules should be disabled. If any audit algorithm fails to pass, SQL execution will be prohibited. The configuration of SQL audit for data sharding is as follows.

```sql
rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: t_order_inline
      auditStrategy:
        auditorNames:
          - sharding_key_required_auditor
        allowHintDisable: true
  defaultAuditStrategy:
    auditorNames:
      - sharding_key_required_auditor
    allowHintDisable: true

  auditors:
    sharding_key_required_auditor:
      type: DML_SHARDING_CONDITIONS
```

In view of complex business scenarios, the new feature allows users to dynamically disable the audit algorithm by using SQL hints so that the business SQL that is allowable in partial scenarios can be executed. Currently, ShardingSphere 5.2.0 has a built-in DML disables full-route audit algorithm. Users can also implement ShardingAuditAlgorithm interface by themselves to realize more advanced SQL audit functions.

```sql
/* ShardingSphere hint: disableAuditNames=sharding_key_required_auditor */ SELECT * FROM t_order;
```

### Elastic data migration

Data migration has always been a focus of the ShardingSphere community. Before 5.2.0, users needed to add an external table as a single sharding table, and then modify the sharding rules to trigger the migration, which was too complex and difficult for ordinary users.

To improve the ease of data migration, **ShardingSphere 5.2.0 provides a new data migration feature, coupled with DistSQL for elastic migration. Users can migrate data from the existing single database to the distributed database system composed of ShardingSphere + MySQL or PostgreSQL in an SQL-like manner, achieving the transformation from a single database to a distributed one.**

![img](https://shardingsphere.apache.org/blog/img/2022_09_08_Apache_ShardingSphere_5.2.0_is_Released,_Bringing_New_Cloud_Native_Possibilities1.png)

The new feature is capable of migrating Oracle data to PostgreSQL. Users can create sharding rules and sharding tables through DistSQL first, that is to create new distributed databases and tables, and then run MIGRATE TABLE ds.schema.table INTO table to trigger data migration.

During the migration process, users can also use the dedicated DistSQL for data migration in the table to manage the migration job status and data consistency. For more information about the new feature, please refer to the official document [[Data Migration](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/migration/)].

### SQL Execution Process Management

The native MySQL database provides the SHOW PROCESSLIST statement, allowing the user to view the currently running thread. Users can kill the thread with the KILL statement for SQL that takes too long to be temporarily terminated.

![](https://shardingsphere.apache.org/blog/img/2022_09_08_Apache_ShardingSphere_5.2.0_is_Released,_Bringing_New_Cloud_Native_Possibilities2.png)

The SHOW PROCESSLIST and KILL statements are widely used in daily operation and maintenance management. To enhance users’ ability to manage ShardingSphere, version 5.2.0 supports the MySQL SHOW PROCESSLIST and KILL statements. When a user executes a DDL/DML statement through ShardingSphere, ShardingSphere automatically generates a unique UUID identifier as an ID and stores the SQL execution information in each instance.

The following figure shows the results while executing the SHOW PROCESSLIST and KILL statements in ShardingSphere. When the user executes the SHOW PROCESSLIST statement, ShardingSphere processes the SQL execution information based on the current operating mode.

If the current mode is cluster mode, ShardingSphere collects and synchronizes the SQL execution information of each compute node through the governance center, and then returns the summary to the user. If the current mode is the standalone mode, ShardingSphere only returns SQL execution information in the current compute node.

**The user determines whether to execute the KILL statement based on the result returned by the SHOW PROCESSLIST, and ShardingSphere cancels the SQL in execution based on the ID in the KILL statement.**

![](https://shardingsphere.apache.org/blog/img/2022_09_08_Apache_ShardingSphere_5.2.0_is_Released,_Bringing_New_Cloud_Native_Possibilities3.png)

### [Shardingsphere-on-cloud](https://github.com/apache/shardingsphere-on-cloud) sub-project goes live

Shardingsphere-on-cloud is a project of Apache ShardingSphere providing cloud-oriented solutions. Version 0.1.0 has been released and it has been officially voted as a sub-project of Apache ShardingSphere.

Shardinsphere-on-cloud will continue to release various configuration templates, deployment scripts, and other automation tools for ShardingSphere on the cloud.

It will also polish the engineering practices in terms of high availability, data migration, observability, shadow DB, security, and audit, optimize the delivery mode of Helm Charts, and continue to enhance its cloud native management capabilities through Kubernetes Operator. Currently, there are already introductory issues in the project repository to help those who are interested in Go, Database, and Cloud to quickly get up and running.

## Enhancement

### Kernel

The ShardingSphere community optimizes the SQL parsing capability of different databases in this release, greatly improving ShardingSphere’s SQL compatibility.

Detailed SQL parsing optimization can be seen in the update log section below. It’s a long-term mission for the ShardingSphere community to improve SQL parsing support. Anyone who is interested is welcome to work with us.

Version 5.2.0 also supports the column-visible feature for MySQL, Oracle, [SQLServer](https://www.microsoft.com/en-us/sql-server/sql-server-downloads), and H2 databases, in a bid to meet the requirements of business SQL compatibility during a system upgrade. The read/write splitting feature supports the Cartesian product configuration, which greatly simplifies user configurations.

### Access Port

In version 5.2.0, [ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/) is capable of monitoring specified IP addresses and integrates openGauss database drivers by default. [ShardingSphere-JDBC ](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc)supports c3p0 data sources, and Connection.prepareStatement can specify the columns.

### Distributed Transaction

In terms of distributed transactions, the original logical database-level transaction manager is adjusted to a global manager, supporting distributed transactions across multiple logical databases.

At the same time, it removed the XA statement’s ability to control distributed transactions as XA transactions are now automatically managed by ShardingSphere, which simplifies the operation for users.

## Update logs

Below are all the update logs of ShardingSphere 5.2.0. To deliver a better user experience, this release adjusted the API of part of the functions, which can be seen from the API changes part below.

### New Feature

- Kernel: Support SQL audit for sharding feature

- Kernel: Support MySQL show processlist and kill process id feature

- Scaling: Add dedicated DistSQL for data migration

- Scaling: Basic support for migration of data to heterogeneous database

- DistSQL: New syntax CREATE/ALTER/SHOW MIGRATION PROCESS CONFIGURATION

- DistSQL: New syntax ALTER MIGRATION PROCESS CONFIGURATION

- DistSQL: New syntax SHOW MIGRATION PROCESS CONFIGURATION

- DistSQL: New syntax ADD MIGRATION SOURCE RESOURCE

- DistSQL: New syntax SHOW SQL_TRANSLATOR RULE

- DistSQL: New syntax CREATE SHARDING AUDITOR

- DistSQL: New syntax ALTER SHARDING AUDITOR

- DistSQL: New syntax SHOW SHARDING AUDIT ALGORITHMS

### Enhancement

- Kernel: Support column visible feature for MySQL, Oracle, SQLServer and H2

- Kernel: Support cartesian product configuration for read/write splitting

- Kernel: Support spring namespace and spring boot usage for sql translator

- Kernel: Support JSR-310 Year and Month in IntervalShardingAlgorithm

- Kernel: Support broadcast table update/delete limit statement

- Kernel: Support create index on table(column) statement rewrite when config encrypts

- Kernel: Support openGauss cursor, fetch, move, close statement for sharding and read/write splitting

- Kernel: Support encrypt column rewrite when execute column is null in predicate

- Kernel: Support encrypt show create table return logic columns

- Kernel: Support create table with index statement rewrite when config encrypt

- Kernel: Support PostgreSQL create operator statement parse

- Kernel: Support PostgreSQL create materialized view statement parse

- Kernel: Support PostgreSQL nested comments parse

- Kernel: Support PostgreSQL alter subscription statement parse

- Kernel: Support PostgreSQL create group statement parse

- Kernel: Support PostgreSQL alter statictics statement parse

- Kernel: Support PostgreSQL create foreign table statement parse

- Kernel: Support PostgreSQL alter server statement parse

- Kernel: Support PostgreSQL create foreign data wrapper statement parse

- Kernel: Support PostgreSQL create event trigger statement parse

- Kernel: Support PostgreSQL security label statement parse

- Kernel: Support PostgreSQL reindex statement parse

- Kernel: Support PostgreSQL reassign owned statement and refresh materialized view statement parse

- Kernel: Support PostgreSQL prepare transaction statement parse

- Kernel: Support PostgreSQL create collation statement parse

- Kernel: Support PostgreSQL lock statement parse

- Kernel: Support PostgreSQL alter rule statement parse

- Kernel: Support PostgreSQL notify statement parse

- Kernel: Support PostgreSQL unlisten statement parse

- Kernel: Support Oracle alter function and alter hierarchy statement parse

- Kernel: Support Oracle alter pluggable database statement parse

- Kernel: Support Oracle alter materialized view log statement parse

- Kernel: Support Oracle alter diskgroup statement parse

- Kernel: Support Oracle alter operator statement parse

- Kernel: Support oracle alter cluster statement parse

- Kernel: Support oracle alter audit policy statement parse

- Kernel: Support Oracle alter index type statement parse

- Kernel: Support Oracle lock table statement parse

- Kernel: Support Oracle alter java statement parse

- Kernel: Support Oracle inline constraint statement parse

- Kernel: Support openGauss geometric operator statement parse

- Kernel: Optimize MySQL visible/invisible parse of create/alter table statements

- Kernel: Support scope of variable prefixed with @@ in MySQL SET statement parse

- Kernel: Support MySQL create procedure with create view parse

- Kernel: Support column segments parse in create index on table statement

- Kernel: Support openGauss cursor, fetch, move, close statement for sharding, readwrite-splitting

- Kernel: Support encrypt column rewrite when execute column is null in predicate

- Kernel: Support encrypt show create table return logic columns

- Kernel: Support create table with index statement rewrite when config encrypt

- Kernel: Support parsing ALTER LOCKDOWN PROFILE in Oracle

- Kernel: Support parsing ALTER MATERIALIZED VIEW in Oracle

- Kernel: Support parsing ALTER MATERIALIZED ZONEMAP in Oracle

- Kernel: Support parsing ALTER LIBRARY in Oracle

- Kernel: Support parsing ALTER INMEMORY JOIN GROUP in Oracle

- Kernel: Support parsing DROP OPERATOR in Oracle

- Kernel: Support parsing DROP RESTORE POINT in Oracle

- Kernel: Support parsing CREATE RESTORE POINT in Oracle

- Kernel: Support parsing DROP INMEMORY JOIN GROUP in Oracle

- Kernel: Support parsing create_bit_xor_table in MySQL

- Kernel: Support parsing MySQL DO statement

- Kernel: Support parsing DropServer in openGauss

- Kernel: Support parsing CREATE AGGREGATE In openGauss

- Kernel: Support parsing ALTER ROUTINE in PostgreSQL

- Kernel: Add PostgreSQL Create Cast Statement

- Kernel: Add PostgreSQL Create Aggregate Statement

- Kernel: Support fetch/move/close cursor statement in PostgreSQL

- Kernel: Support Parsing ALTER PUBLICATION in PostgreSQL

- Kernel: Add PostgreSQL Create Access Method Statement

- Kernel: Support Parsing ALTER POLICY in PostgreSQL

- Kernel: Support parsing ALTER OPERATOR in PostgreSQL

- Kernel: Add PostgreSQL Copy Statement

- Kernel: Add PostgreSQL Comment Statement

- Kernel: Support listen statement in PostgreSQL

- Kernel: Support DECLARE cursor statement

- Access port: Add default serverConfig in helm charts

- Access port: Assemble openGauss JDBC Driver into Proxy distribution

- Access port: ShardingSphere-Proxy listen on specified IP addresses

- Access port: Support COM_STMT_SEND_LONG_DATA in MySQL Proxy

- Access port: SELECT VERSION() support alias in MySQL Proxy

- Access port: Fix openGauss Proxy could not be connected if no resource defined

- Access port: Support using JRE defined in JAVA_HOME in ShardingSphere-Proxy’s startup script

- Access port: Avoid client blocked when OOM occurred in ShardingSphere-Proxy

- Access port: Support using c3p0 in ShardingSphere-JDBC

- Access port: Support SET NAMES with value quoted by double-quote

- Access port: Connection.prepareStatement with columns arguments is available in ShardingSphere-JDBC

- Scaling: Improve MySQL connect and reconnect

- Scaling: Fix MySQL json column may cause leak at incremental task

- Scaling: Add permission check for PostgreSQL data sources

- Scaling: Incremental migration support for MySQL MGR mode

- Scaling: Improve job progress persistence

- Scaling: Start job DistSQL execute and return synchronously

- Scaling: Inventory migration support table has primary key and unique key

- Scaling: Close unerlying ElasticJob when stopping job

- Scaling: Improve logical replication slot name generation for PostgreSQL and openGauss

- Scaling: Make query DistSQL could be executed when no database selected

- DistSQL: Add worker_id to the result set of SHOW INSTANCE LIST & SHOW INSTANCE INFO

- DistSQL: Improve the result of EXPORT DATABASE CONFIG

- DistSQL: Support more databases for FORMAT SQL

- DistSQL: Optimize the execution logic of CREATE TRAFFIC RULE

- DistSQL: Support assistEncryptor for Encrypt RDL

- DistSQL: Add sharding algorithm type check when CREATE SHARDING TABLE RULE

- Distributed governance: Support database discovery to configure multiple groups of high availability under the same logic database

- Distributed governance: Support ShardingSphere-Proxy to start up under empty logic library

- Distributed governance: Support for isolating EventBus events by instance

- Distributed governance: Support the database to detect changes in the master node and restart the detection heartbeat task

- Distributed governance: Support ShardingSphere-Proxy to generate new worker-id when re-registering in cluster mode

- Distributed governance: Thrown exception when inserting expression value in shadow column on executing insert

- Distributed governance: Support distributed transactions across multiple logical databases

- Distributed governance: Support executing truncate in XA & PostgreSQL

- Distributed governance: Support alter local transaction rule with DistSQL

- Distributed governance: Support global transaction manager

- Distributed governance: Delete support for branch transaction on proxy

### Bug Fix

- Kernel: Fix single table metadata refresh error caused by filtering DataSourceContainedRule

- Kernel: Fix parsing exception caused by the null value of MySQL blob type

- Kernel: Fix PostgreSQL/openGauss reset statement parse error

- Kernel: Fix wrong parameter rewrite when use sharding and encrypt

- Kernel: Fix the failed conversion of Month related classes on IntervalShardingAlgorithm

- Kernel: Fix NullPointerException when execute select union statement contains subquery

- Kernel: Fix wrong encrypt rewrite result due to incorrect order of metadata

- Kernel: Fix MySQL trim function parse error

- Kernel: Fix MySQL insert values with _binary parse error

- Access port: Fix MySQL syntax error cannot be thrown to client

- Access port: Avoid EventLoop blocked because of closing JDBC resources

- Access port: Correct server status flags returned by MySQL Proxy

- Access port: Fix a possible connection leak issue if Proxy client disconnected in transaction

- Access port: Fixed a possible consistency issue with the statement being executed when the Proxy client is disconnected

- Access port: Avoid pooled connection polluted by executing SET statements

- Access port: Make SHOW TABLES FROM work in ShardingSphere-Proxy

- Access port: Fix PostgreSQL DDL could not be executed by Extended Query

- Access port: Fix SHOW VARIABLES could not be executed in PostgreSQL Proxy without resource

- Access port: Fix FileNotFoundException when use ShardingSphere Driver with SpringBoot fatjar

- Scaling: Fix the problem that the table contains both primary key and unique index at inventory migration

- Scaling: Improve incremental migration, support the latest position in the middle of batch insert event

- Scaling: Fix the error caused by null field value in openGauss incremental migration

- DistSQL: Fix incorrect strategy name in result of SHOW SHARDING TABLE RULES

- DistSQL: Fix current rule config is modified in advance when ALTER SHARDING TABLE RULE

- DistSQL: Fix connection leak when ALTER RESOURCE

- DistSQL: Fix CREATE TRAFFIC RULE failed when load balance algorithm is null

- Distributed governance: Fix that the monitoring heartbeat task was not stopped when the database was discovered and the logical library was deleted

- Distributed governance: Fix cluster mode ShardingSphere-JDBC load all logic database

- Distributed governance: Fix worker-id generated by SnowflakeKeyGenerateAlgorithm in cluster mode may exceed the maximum value

- Shadow DB: Fix DistSQL adding shadow algorithm exception without shadow data source

- Distributed transaction: Fix cross-database data source confusion caused by same data source name in multiple logical databases

- Distributed transaction: Fix RUL DistSQL execution failure in transaction

- Distributed transaction: Fix begin for PostgreSQL & openGauss

- Agent: Fixed the error of null value in contextManager when collecting metric data

### Refactor

- Kernel: ShardingSphere metadata refactoring for splitting actual metadata and logical metadata

- Kernel: Use ConnectionContext, QueryContext to remove ThreadLocal in FetchOrderByValueQueuesHolder, TrafficContextHolder, SQLStatementDatabaseHolder and TransactionHolder

- Access port: Modify the default value of the ShardingSphere-Proxy version in the helm chart

- Access port: Docker container will exit if ShardingSphere-Proxy failed to startup

- Access port: Helm Charts in ShardingSphere repository are transferred to sub-project shardingsphere-on-cloud

- Scaling: Plenty of refactor for better code reuse

- DistSQL: Add a new category named RUL

- Distributed governance: Refactor the schedule module and split it into cluster schedule and standalone schedule

- Distributed governance: Remove memory mode, keep standalone mode and cluster mode

- Distributed governance: Refactoring metadata table loading logic and persistence logic

- Distributed governance: Refactoring distributed locks to retain the most concise interface design

- Testing: Refactor: Unify The Creation for Proxy Containers in IT from ENV Modules

- Testing: Refactor: Unify The Configuration for container created by testcontainer

### API Changes

- Kernel: Remove SQL passthrough to data source feature

- Kernel: Add new assistedQueryEncryptorName and remove QueryAssistedEncryptAlgorithm interface

- Kernel: Refactor readwrite-splitting api to improve user experience

- Kernel: Remove check-duplicate-table-enabled configuration

- Kernel: Remove useless config item show-process-list-enabled configuration

- Scaling: Change keyword for part of data migration DistSQL

- Scaling: Redesign part of data migration DistSQL

- DistSQL: Unify parameter type specification

- DistSQL: Split SHOW INSTANCE MODE to SHOW MODE INFO and SHOW INSTANCE INFO

- DistSQL: Change DROP SCALING jobId to CLEAN MIGRATION jobId

- DistSQL: Remove COUNT INSTANCE RULES

- Distributed governance: Add database found that high availability supports all the slave libraries to go offline, and the main library undertakes the read traffic configuration

- Distributed governance: SnowflakeKeyGenerateAlgorithm supports configuring worker-id in standalone mode

- Shadow DB: Replace sourceDataSourceName with productionDataSourceName in Shadow API Configuration

- Authority: Remove deprecated native authority provider

## Relevant Links

🔗 [Download Link](https://shardingsphere.apache.org/document/current/en/downloads/)

🔗 [Update Logs](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md)

🔗 [Project Address](https://shardingsphere.apache.org/)

🔗 [Cloud Sub-project Address](https://github.com/apache/shardingsphere-on-cloud)

## Community Contribution

The Apache ShardingSphere 5.2.0 release is the result of 1,728 merged PRs, committed by 64 Contributors. Thank you for your efforts!

![](https://shardingsphere.apache.org/blog/img/2022_09_08_Apache_ShardingSphere_5.2.0_is_Released,_Bringing_New_Cloud_Native_Possibilities4.png)

## Author

Duan Zhengqiang, a senior middleware development engineer at [SphereEx](https://www.sphere-ex.com/en/) & Apache ShardingSphere PMC.

He started to contribute to Apache ShardingSphere middleware in 2018 and used to play a leading role in sharding practices dealing with massive data. With rich practical experience, he loves open-source and is willing to contribute. Now he focuses on the development of [Apache ShardingSphere](https://shardingsphere.apache.org/) kernel module.
