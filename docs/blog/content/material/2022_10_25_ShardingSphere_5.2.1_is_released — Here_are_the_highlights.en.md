+++
title = "ShardingSphere 5.2.1 is released — Here are the highlights"
weight = 77
chapter = true 
+++

Our new 5.2.1 release brings new features and enhancements such as the ShardingSphere system database, SQL HINT mandatory sharding routing, asynchronous data consistency check, and support for Consul and Nacos governance center.

# Introduction

Following 1.5 months in development, [Apache ShardingSphere](https://shardingsphere.apache.org/) 5.2.1 is released. Our community merged 614 PRs from teams and individuals around the world. The [resulting 5.2.1 release](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md) has been optimized in terms of features, performance, testing, documentation, examples, etc.

The new release brings the following highlights:

- ShardingSphere system database
- SQL HINT mandatory sharding routing
- Asynchronous data consistency check

Version 5.2.1 introduces a new ShardingSphere system database to provide statistical information for distributed databases. **The statistical information can help the SQL Federation execution engine evaluate the execution costs to select an execution plan with the lowest costs.** Moreover, ShardingSphere can collect the distribution information after data sharding, providing references for automatic sharding scaling management.

**SQL HINT is an important feature provided by ShardingSphere 5. x, with which users can flexibly control routes through SQL annotations.** The new release enhances the SQL HINT capability and supports data-sharding mandatory routing.

Users only need to add annotations in the format: `/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=1, t_order.SHARDING_TABLE_VALUE=1 */` before transferring sharding values to the ShardingSphere routing engine. Compared with the former HintManager method, SQL HINT is more flexible without requiring users to write code.

**The new version also enhances the data consistency check capability during data migration, enabling asynchronous execution of the check. Users can view the data migration progress through** [**DistSQL**](https://medium.com/nerd-for-tech/intro-to-distsql-an-open-source-more-powerful-sql-bada4099211)**, which improves the ease of data migration.**

Moreover, the **governance center now supports Consul and Nacos**, providing more choices for users. SQL compatibility is greatly improved. Read/write splitting has also been optimized, as Proxy can be started forcibly by using the `-f` parameter after the secondary database is disabled.

Next, this post will introduce the updates of ShardingSphere 5.2.1 in detail.

# Notable Highlights

## ShardingSphere System Database

Like [MySQL](https://www.mysql.com/) and [PostgreSQL](https://www.postgresql.org/) system databases, ShardingSphere 5.2.1 introduces a new system database to manage the metadata of databases.

ShardingSphere system database mainly stores dynamic and static metadata. Dynamic metadata refers to frequently updated data, such as statistical information from distributed databases, which needs to be collected and maintained regularly through built-in scheduling tasks.

In comparison, static metadata refers to data that will remain the same without user modification. An example is the status parameter of the ShardingSphere distributed database set by users, which only needs to be stored in the metadata database for querying.

As this function is currently in the experimental phase, you need to start collecting metadata through the following configuration.

```java
proxy-metadata-collector-enabled: true
```

Connect Proxy through the command line and then execute the `SHOW DATABASES` statement. We can see the new `shardingsphere` system database which stores the metadata of distributed databases.

![img](https://shardingsphere.apache.org/blog/img/2022_10_25_ShardingSphere_5.2.1_is_released — Here_are_the_highlights1.png)

Currently, the `sharding_table_statistics` table is added to the `shardingsphere` system database, used for collecting the distribution information of sharding tables, including `row_count` and `size`.

![img](https://shardingsphere.apache.org/blog/img/2022_10_25_ShardingSphere_5.2.1_is_released — Here_are_the_highlights2.png)

Statistical information provided by the ShardingSphere system database can help the SQL Federation execution engine evaluate the execution cost. This allows for the selection of a suitable association order and method and achieves efficient execution.

Moreover, by collecting the data distribution information and loading information of storage nodes, ShardingSphere can carry out automatic sharding scaling, reducing the operation & maintenance costs for users.

## SQL HINT Mandatory Sharding Routing

In some special business scenarios, the fields used for sharding exist in external business logic rather than SQL, database, and table structures. Currently, `Hint` is needed to introduce sharding key value to complete sharding routing.

Before version 5.2.1, there were two ways to use `Hint`. One way is to use it through HintManager in the JDBC access port, while another way is to start the `proxy-hint-enabled` parameter in the Proxy access port.

In the first case, users need to write codes to call the `addDatabaseShardingValue` and `addTableShardingValue` methods to set the values for the database and table sharding. However, if the HintManager method is used, users have to modify the original logic at some cost.

```java
// Sharding database and table with using HintManagerString sql = "SELECT * FROM t_order";
try (HintManager hintManager = HintManager.getInstance();
     Connection conn = dataSource.getConnection();
     PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.addDatabaseShardingValue("t_order", 1);
    hintManager.addTableShardingValue("t_order", 2);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            // ...        
        }
    }
}
```

If it’s the second case, start the `proxy-hint-enabled` parameter first, and then clear the sharding values through the following [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/) configuration.

Nevertheless, the Hint-based method will transform Proxy's thread processing model from IO multiplexing to one independent thread for each request, which will reduce Proxy's throughput. This means that users have to make tradeoffs.

```java
-- For the current connection, add sharding values yy to table xx. xx: logical table name; yy: database sharding value
ADD SHARDING HINT DATABASE_VALUE t_order= 100;
-- For the current connection, add sharding values yy to table xx. xx: logical table name; yy: table sharding value
ADD SHARDING HINT TABLE_VALUE t_order = 100;
-- For the current connection, clear sharding hint settings
CLEAR SHARDING HINT;
```

As there are problems with both methods, version 5.2.1 adds the SQL Hint mandatory sharding routing feature. This allows users to control routes flexibly through SQL annotations. There’s no need to modify the logic of the original code and the thread processing model in the Proxy access port is not affected.

The annotation format only supports `/* */` and content has to start with `SHARDINGSPHERE_HINT:`.

Optional properties include:

- `{table}.SHARDING_DATABASE_VALUE`: Used to add data source sharding key value corresponding to `{table}`. Multiple properties are separated by a comma.
- `{table}.SHARDING_TABLE_VALUE`: Used to add table sharding key value corresponding to `{table}`. Multiple properties are separated by a comma.

The following is an example of using the `HINT_INLINE` algorithm.

```sql
rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      databaseStrategy:
        hint:
          shardingAlgorithmName: database_hint_inline
      tableStrategy:
        hint:
          shardingAlgorithmName: t_order_hint_inline
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    none:  shardingAlgorithms:
    database_hint_inline:
      type: HINT_INLINE
      props:
        algorithm-expression: ds_${value % 2}
    t_order_hint_inline:
      type: HINT_INLINE
      props:
        algorithm-expression: t_order_${value % 2}
```

We can transfer the sharding value of the `t_order` table through SQL Hint. We can see from the `PREVIEW` statement that although SQL statements have no specified sharding key, the sharding key-value pair introduced by SQL Hint can also achieve sharding routing.

```mysql
/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=1, t_order.SHARDING_TABLE_VALUE=1 */
SELECT * FROM t_order;PREVIEW /* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=1, t_order.SHARDING_TABLE_VALUE=1 */ SELECT * FROM t_order;
+------------------+----------------------------------------------------------------------------------------------------------------------+
| data_source_name | actual_sql                                                                                                           |
+------------------+----------------------------------------------------------------------------------------------------------------------+
| ds_1             | /* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=1, t_order.SHARDING_TABLE_VALUE=1 */ SELECT * FROM t_order_1 |
+------------------+----------------------------------------------------------------------------------------------------------------------+
```

## Asynchronous data consistency check

Before version 5.2.1, users had to wait for the server to return check results synchronously. Sometimes the check was time-consuming, so timeout could occur in the `session` of database servers.

In such cases, users couldn’t observe the result and could only check logs, which were not user-friendly. In response to the problem, version 5.2.1 supports asynchronous data consistency check capability as well as a set of DistSQL as follows:

- CHECK MIGRATION jobId — Asynchronous data consistency check
- SHOW MIGRATION CHECK STATUS jobId — Query the check progress
- START MIGRATION CHECK jobId — Start the check job
- STOP MIGRATION CHECK jobId — Stop the check job

Through the above DistSQL, users can manage the data migration more easily. Examples are as follows:

```mysql
-- Execute consistency check
mysql> CHECK MIGRATION 'j0101270cbb6714cff0d7d13db9fa23c7c0a1' BY TYPE (NAME='DATA_MATCH');
Query OK, 0 rows affected (0.24 sec)-- Query the check progress
mysql> SHOW MIGRATION CHECK STATUS 'j0101270cbb6714cff0d7d13db9fa23c7c0a1';
+---------+--------+---------------------+-------------------+-------------------------+----------------+------------------+---------------+
| tables  | result | finished_percentage | remaining_seconds | check_begin_time        | check_end_time | duration_seconds | error_message |
+---------+--------+---------------------+-------------------+-------------------------+----------------+------------------+---------------+
| t_order | false  | 0                   | 2450              | 2022-10-12 16:07:17.082 |                | 14               |               |
+---------+--------+---------------------+-------------------+-------------------------+----------------+------------------+---------------+
1 row in set (0.02 sec)-- Stop the check job
mysql> STOP MIGRATION CHECK 'j0101270cbb6714cff0d7d13db9fa23c7c0a1';
Query OK, 0 rows affected (0.06 sec)-- Start the check job
mysql> START MIGRATION CHECK 'j0101270cbb6714cff0d7d13db9fa23c7c0a1';
Query OK, 0 rows affected (5.13 sec)
```

Please refer to the [official documentation](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/migration/usage/) and stay tuned for future posts with more detailed information.

# Enhancements

## Kernel

This update optimized the SQL parsing of [Oracle](https://www.oracle.com/index.html) and [PostgreSQL](https://www.postgresql.org/), greatly improving the SQL compatibility of ShardingSphere. Detailed SQL parsing optimization can be found in the release notes section below.

It’s a long-term mission for the ShardingSphere community to improve SQL parsing compatibility, and anyone interested is welcome to join us.

Version 5.2.1 also supports read/write splitting. After the secondary database is disabled, [ShardingSphere-Proxy](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/) can be started forcibly by using the `-f` parameter.

In this case, the Proxy can still be started for ops & maintenance work even if part of the storage nodes are unavailable. Moreover, the kernel function has been optimized in terms of the display results of the `SHOW PROCESSLIST` statement and added thread display of the `Sleep` status.

By optimizing the logic of unicast routing and reusing cached database connections as much as possible, ShardingSphere's execution performance is improved.

## Elastic scaling

The elastic scaling module only supports data migration for tables with a unique key, and tables are allowed to migrate to new table names. Moreover, it optimized the use of thread pools in data migration. The data consistency check can be interrupted, which makes it easier for users to manage data migration.

## Distributed Governance

In terms of distributed governance, the governance center is now able to support Consul and Nacos, providing you with more choices. At the same time, the built-in H2 database is used to support persisting metadata information in a standalone pattern.

In the running mode, the `overwrite` configuration item is removed. Metadata information is based on the data in the governance center, and users can dynamically change the rules and configurations through DistSQL.

# Release Notes

In the following sections, you will find the release notes of ShardingSphere 5.2.1. To improve user experience, the API of some functions is modified in this update. Please refer to the API changes section below for details.

## New Features

1. Add ShardingSphere default system database to support global metadata management
2. Support asynchronous data consistency check
3. Added support for Consul governance center
4. Added support for Nacos governance center
5. Added support for the view function in the governance center

## Enhancements

1. SQL Federation engine adds ADVANCED executor and adapts to openGauss database
2. Support ShardingSphere Proxy startup after read-write splitting read database is disabled
3. SQL HINT supports force sharding route
4. Show processlist supports showing Proxy connections (sleep, active)
5. Optimized ShardingSphere-JDBC data source configuration error message
6. ShardingSphere-JDBC supports SpringBoot 3. x version
7. Support load MySQL, PostgreSQL, openGauss, and SQLServer view metadata
8. Update Snakeyaml to 1.33 and open YAML 3MB limit
9. Reuse cached connections as possible when unicast sharding
10. Support Parsing ALTER ROLE in Oracle
11. Add support of ALTER RESOURCE COST for Oracle
12. Support parsing Drop Materialized View in Oracle
13. Support parsing DROP LIBRARY in Oracle
14. Support parsing DROP JAVA in Oracle
15. Support parsing DROP PLUGGABLE DATABASE in Oracle
16. Support parsing DROP INDEX TYPE in Oracle
17. Support Parsing ALTER PACKAGE in openGauss
18. Support openGauss select offset, count statement parse, and remove useless syntax in PostgreSQL grammar
19. Add max_size to openGauss syntax
20. Optimize alter view/drop view parse logic and fix alter view refresher bug
21. Add SQL parser error detail to ParseCancellationException
22. Add support for parse OptOnConflict for Postgres
23. Enhance support for ALTER TABLE and ALTER VIEW in PostgreSQL
24. Add a missing keyword in the PostgreSQL Declare Statement
25. Add JSON function support to MySQL parser
26. ShardingSphere-Proxy automatically adapts to cgroup memory limits in the Docker environment
27. Show migration status DistSQL respond new added error_message column
28. Show migration status respond new added processed_records_count column
29. Support MySQL 8 caching_sha2_password authentication in incremental dump
30. Improve drop pipeline process configuration
31. Support unique key table migration
32. Support migrating table to new name table
33. Improve thread pool usage in pipeline task and runner
34. Support cancelable data consistency check
35. DistSQL: When creating or altering the readwrite-splitting rule, check duplicate write or read resources
36. DistSQL: Add validity check for `ALTER SHARDING BINDING TABLE RULES`
37. Standalone mode H2 supports persistent metadata
38. Fix openGauss cursor execution in xa transaction
39. Added transaction-related exceptionsBug Fix

## Bug Fixes

1. Generate proper placeholder for PostgreSQL when rewriting
2. Fix opengauss update set parse exception
3. Fix parse exception when executing insert statement with a negative value
4. Fix wrong connectDescriptorUrlPattern in OracleDataSourceMetaData
5. Fix insert SQL garbled after sharding rewrote in special rules
6. Fix exception when execute select * from information_schema.tables
7. Fix exception when executing alter view rename
8. Fix PostgreSQL check data source permission when using rolsuper
9. DistSQL: fix NPE for `REFRESH TABLE METADATA` when there is no resource
10. Fix Unmodified table metadata when modifying rules
11. Fix database discovery
12. The MySQL.NORMAL_REPLICATION algorithm cannot find the primary node”
13. Fixed using etcd to build a cluster event not aware
14. Fix NPE occurred when transaction management is not created
15. Fix routing exception that occurs when the sharding value of the InlineShardingAlgorithm algorithm exceeds Integer

## API Changes

1. SQL HINT syntax format adjust to SQL-style format
2. DistSQL: Remove syntax `COUNT DATABASE RULES`
3. ShardingSphere mode removes overwrite configuration
4. Agent: Optimize configuration of agent.yaml

# Relevant Links

**🔗** [**Download Link**](https://shardingsphere.apache.org/document/current/en/downloads/)

**🔗** [**Release Notes**](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md)

**🔗** [**Project Address**](https://shardingsphere.apache.org/)

**🔗**[ **Cloud Sub-project Address**](https://github.com/apache/shardingsphere-on-cloud)

# Community Contribution

This Apache ShardingSphere 5.2.1 release is the result of 614 merged PRs, committed by 38 contributors. Thank you for your efforts!

![img](https://shardingsphere.apache.org/blog/img/2022_10_25_ShardingSphere_5.2.1_is_released — Here_are_the_highlights3.png)