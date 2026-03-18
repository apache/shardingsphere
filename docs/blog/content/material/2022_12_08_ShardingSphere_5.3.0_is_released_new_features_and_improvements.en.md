+++
title = "ShardingSphere 5.3.0 is released: new features and improvements"
weight = 82
chapter = true 
+++

![img](https://shardingsphere.apache.org/blog/img/2022_12_08_ShardingSphere_5.3.0_is_released_new_features_and_improvements1.png)

After 1.5 months in development, [Apache ShardingSphere 5.3.0](https://shardingsphere.apache.org/document/current/en/downloads/) is released. Our community merged [687 PRs](https://github.com/apache/shardingsphere/pulls?q=is%3Amerged+is%3Apr+milestone%3A5.3.0) from contributors around the world.

The new release has been improved in terms of features, performance, testing, documentation, examples, etc.

The 5.3.0 release brings the following highlights:

- Support fuzzy query for CipherColumn.
- Support Datasource-level heterogeneous database.
- Support checkpoint resume for data consistency check.
- Automatically start a distributed transaction, while executing DML statements across multiple shards.

Additionally, release 5.3.0 also brings the following adjustments:

- Remove the Spring configuration.
- Systematically refactor the DistSQL syntax.
- Refactor the configuration format of ShardingSphere-Proxy.

# Highlights

## **1. Support fuzzy query for CipherColumn**

In previous versions, ShardingSphere's Encrypt feature didn't support the use of the `LIKE` operator in SQL.

For a while users strongly requested adding the `LIKE` operator to the Encrypt feature. Usually, encrypted fields are mainly of the string type, and it is a common practice for the string to execute `LIKE`.

To minimize friction in accessing the Encrypt feature, our community has initiated a discussion about the implementation of encrypted `LIKE`.

Since then, we've received a lot of feedback.

Some community members even contributed their original encryption algorithm implementation supporting fuzzy queries after fully investigating conventional solutions.

🔗 **The relevant issue can be found** [**here**](https://github.com/apache/shardingsphere/issues/20435)**.**

🔗 **For the algorithm design, please refer to the** [**attachment**](https://github.com/apache/shardingsphere/files/9684570/default.pdf) **within the** [**issue**](https://github.com/apache/shardingsphere/files/9684570/default.pdf)**.**

The [single-character abstract algorithm] contributed by the community members is implemented as `CHAR_DIGEST_LIKE` in the ShardingSphere encryption algorithm SPI.

## 2. Support datasource-level heterogeneous database

[ShardingSphere](https://shardingsphere.apache.org/) supports a database gateway, but its heterogeneous capability is limited to the logical database in previous versions. This means that all the data sources under a logical database must be of the same database type.

This new release supports datasource-level heterogeneous databases at the kernel level. This means the datasources under a logical database can be different database types, allowing you to use different databases to store data.

Combined with ShardingSphere's SQL dialect conversion capability, this new feature significantly enhances ShardingSphere's heterogeneous data gateway capability.

## 3. Data migration: support checkpoint resume for data consistency check

Data consistency checks happen at the later stage of data migration.

Previously, the data consistency check was triggered and stopped by [DistSQL](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/). If a large amount of data was migrated and the data consistency check was stopped for any reason, the check would've had to be started again — which is sub-optimal and affects user experience.

ShardingSphere 5.3.0 now supports checkpoint storage, which means data consistency checks can be resumed from the checkpoint.

For example, if data is being verified during data migration and the user stops the verification for some reason, with the verification progress `(finished_percentage)` being 5%, then:

```mysql
mysql> STOP MIGRATION CHECK 'j0101395cd93b2cfc189f29958b8a0342e882';
Query OK, 0 rows affected (0.12 sec)
mysql> SHOW MIGRATION CHECK STATUS 'j0101395cd93b2cfc189f29958b8a0342e882';
+--------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| tables | result | finished_percentage | remaining_seconds | check_begin_time        | check_end_time          | duration_seconds | error_message |
+--------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
| sbtest | false  | 5                   | 324               | 2022-11-10 19:27:15.919 | 2022-11-10 19:27:35.358 | 19               |               |
+--------+--------+---------------------+-------------------+-------------------------+-------------------------+------------------+---------------+
1 row in set (0.02 sec)
```

In this case, the user restarts the data verification. But the work does not have to restart from the beginning. The verification progress `(finished_percentage)` remains at 5%.

```mysql
mysql> START MIGRATION CHECK 'j0101395cd93b2cfc189f29958b8a0342e882';
Query OK, 0 rows affected (0.35 sec)
mysql> SHOW MIGRATION CHECK STATUS 'j0101395cd93b2cfc189f29958b8a0342e882';
+--------+--------+---------------------+-------------------+-------------------------+----------------+------------------+---------------+
| tables | result | finished_percentage | remaining_seconds | check_begin_time        | check_end_time | duration_seconds | error_message |
+--------+--------+---------------------+-------------------+-------------------------+----------------+------------------+---------------+
| sbtest | false  | 5                   | 20                | 2022-11-10 19:28:49.422 |                | 1                |               |
+--------+--------+---------------------+-------------------+-------------------------+----------------+------------------+---------------+
1 row in set (0.02 sec)
```

**Limitation:** this new feature is unavailable with the `CRC32_MATCH` algorithm because the algorithm calculates all data at once.

## 4. Automatically start a distributed transaction while executing DML statements across multiple shards

Previously, even with XA and other distributed transactions configured, ShardingSphere could not guarantee the atomicity of DML statements that are routed to multiple shards — if users didn't manually enable the transaction.

Take the following SQL as an example:

```sql
insert into account(id, balance, transaction_id) values
(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4),
(5, 5, 5),(6, 6, 6),(7, 7, 7),(8, 8, 8);
```

When this SQL is sharded according to `id mod 2`, the ShardingSphere kernel layer will automatically split it into the following two SQLs and route them to different shards respectively for execution:

```sql
insert into account(id, balance, transaction_id) values
(1, 1, 1),(3, 3, 3),(5, 5, 5),(7, 7, 7);
insert into account(id, balance, transaction_id) values
(2, 2, 2),(4, 4, 4),(6, 6, 6),(8, 8, 8);
```

If the user does not manually start the transaction, and one of the sharded SQL fails to execute, the atomicity cannot be guaranteed because the successful operation cannot be rolled back.

ShardingSphere 5.3.0 is optimized in terms of distributed transactions. If distributed transactions are configured in ShardingSphere, they can be automatically started when DML statements are routed to multiple shards. This way, we can ensure atomicity when executing DML statements.

# Significant Adjustments

**1. Remove Spring configuration**

In earlier versions, [ShardingSphere-JDBC](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc) provided services in the format of DataSource. If you wanted to introduce ShardingSphere-JDBC without modifying the code in the [Spring](https://spring.io/)/[Spring Boot](https://spring.io/projects/spring-boot) project, you needed to use modules such as Spring/[Spring Boot Starter](https://www.javatpoint.com/spring-boot-starters) provided by ShardingSphere.

Although ShardingSphere supports multiple configuration formats, it also has the following problems:

1. When API changes, many config files need to be adjusted, which is a heavy workload.
2. The community has to maintain multiple config files.
3. The lifecycle management of [Spring bean](https://www.baeldung.com/spring-bean) is susceptible to other dependencies of the project such as PostProcessor failure.
4. Spring Boot Starter and Spring NameSpace are affected by Spring, and their configuration styles are different from YAML.
5. Spring Boot Starter and Spring NameSpace are affected by the version of Spring. When users access them, the configuration may not be identified and dependency conflicts may occur. For example, Spring Boot versions 1.x and 2.x have different configuration styles.

[ShardingSphere 5.1.2 first supported the introduction of ShardingSphere-JDBC in the form of JDBC Driver](https://medium.com/codex/shardingsphere-jdbc-driver-released-a-jdbc-driver-that-requires-no-code-modifications-5464c30bcd64?source=your_stories_page-------------------------------------). That means applications only need to configure the Driver provided by ShardingSphere at the JDBC URL before accessing to ShardingSphere-JDBC.

Removing the Spring configuration simplifies and unifies the configuration mode of ShardingSphere. This adjustment not only simplifies the configuraiton of ShardingSphere when using different configuration modes, but also reduces maintenance work for the ShardingSphere community.

**2. Systematically refactor the DistSQL syntax**

One of the characteristics of Apache ShardingSphere is its flexible rule configuration and resource control capability.

[DistSQL (Distributed SQL)](https://shardingsphere.apache.org/document/5.1.0/en/concepts/distsql/) is ShardingSphere's SQL-like operating language. It's used the same way as standard SQL, and is designed to provide incremental SQL operation capability.

ShardingSphere 5.3.0 systematically refactors DistSQL. The community redesigned the syntax, semantics and operating procedure of DistSQL. The new version is more consistent with ShardingSphere's design philosophy and focuses on a better user experience.

Please refer to the [latest ShardingSphere documentation](https://shardingsphere.apache.org/) for details. A DistSQL roadmap will be available soon, and you're welcome to leave your feedback.

**3. Refactor the configuration format of** [**ShardingSphere-Proxy**](https://shardingsphere.apache.org/document/current/en/quick-start/shardingsphere-proxy-quick-start/)

In this update, ShardingSphere-Proxy has adjusted the configuration format and reduced config files required for startup.

`server.yaml` before refactoring:

```yaml
rules:
  - !AUTHORITY
    users:
      - root@%:root
      - sharding@:sharding
    provider:
      type: ALL_PERMITTED
  - !TRANSACTION
    defaultType: XA
    providerType: Atomikos
  - !SQL_PARSER
    sqlStatementCache:
      initialCapacity: 2000
      maximumSize: 65535
    parseTreeCache:
      initialCapacity: 128
      maximumSize: 1024
```

`server.yaml` after refactoring:

```yaml
authority:
  users:
    - user: root@%
      password: root
    - user: sharding
      password: sharding
  privilege:
    type: ALL_PERMITTED
transaction:
  defaultType: XA
  providerType: Atomikos
sqlParser:
  sqlStatementCache:
    initialCapacity: 2000
    maximumSize: 65535
  parseTreeCache:
    initialCapacity: 128
    maximumSize: 1024
```

In ShardingSphere 5.3.0, `server.yaml` is no longer required to start Proxy. If no config file is provided by default, Proxy provides the default account root/root.

ShardingSphere is completely committed to becoming cloud native. Thanks to DistSQL, ShardingSphere-Proxy's config files can be further simplified, which is more friendly to container deployment.

# Release Notes

## API Changes

1. DistSQL: refactor syntax API, please refer to the user manual
2. Proxy: change the configuration style of global rule, remove the exclamation mark
3. Proxy: allow zero-configuration startup, enable the default account root/root when there is no Authority configuration
4. Proxy: remove the default `logback.xml `and use API initialization
5. JDBC: remove the Spring configuration and use Driver + YAML configuration instead

## Enhancements

1. DistSQL: new syntax `REFRESH DATABASE METADATA`, refresh logic database metadata
2. Kernel: support DistSQL `REFRESH DATABASE METADATA` to load configuration from the governance center and rebuild `MetaDataContext`
3. Support PostgreSQL/openGauss setting transaction isolation level
4. Scaling: increase inventory task progress update frequence
5. Scaling: `DATA_MATCH` consistency check support checkpoint resume
6. Scaling: support drop consistency check job via DistSQL
7. Scaling: rename column from `sharding_total_count` to `job_item_count` in job list DistSQL response
8. Scaling: add sharding column in incremental task SQL to avoid broadcast routing
9. Scaling: sharding column could be updated when generating SQL
10. Scaling: improve column value reader for `DATA_MATCH` consistency check
11. DistSQL: encrypt DistSQL syntax optimization, support like query algorithm
12. DistSQL: add properties value check when `REGISTER STORAGE UNIT`
13. DistSQL: remove useless algorithms at the same time when `DROP RULE`
14. DistSQL: `EXPORT DATABASE CONFIGURATION` supports broadcast tables
15. DistSQL: `REGISTER STORAGE UNIT` supports heterogeneous data sources
16. Encrypt: support `Encrypt` LIKE feature
17. Automatically start distributed transactions when executing DML statements across multiple shards
18. Kernel: support `client \d` for PostgreSQL and openGauss
19. Kernel: support select group by, order by statement when column contains null values
20. Kernel: support parse `RETURNING` clause of PostgreSQL/openGauss Insert
21. Kernel: SQL `HINT` performance improvement
22. Kernel: support mysql case when then statement parse
23. Kernel: support data source level heterogeneous database gateway
24. (Experimental) Sharding: add sharding cache plugin
25. Proxy: support more PostgreSQL datetime formats
26. Proxy: support MySQL `COM_RESET_CONNECTION`
27. Scaling: improve `MySQLBinlogEventType.valueOf` to support unknown event type
28. Kernel: support case when for federation

## Bug Fix

1. Scaling: fix barrier node created at job deletion
2. Scaling: fix part of columns value might be ignored in `DATA_MATCH` consistency check
3. Scaling: fix jdbc url parameters are not updated in consistency check
4. Scaling: fix tables sharding algorithm type `INLINE` is case-sensitive
5. Scaling: fix incremental task on MySQL require mysql system database permission
6. Proxy: fix the NPE when executing select SQL without storage node
7. Proxy: support `DATABASE_PERMITTED` permission verification in unicast scenarios
8. Kernel: fix the wrong value of `worker-id` in show compute nodes
9. Kernel: fix route error when the number of readable data sources and weight configurations of the Weight algorithm are not equal
10. Kernel: fix multiple groups of readwrite-splitting refer to the same load balancer name, and the load balancer fails problem
11. Kernel: fix can not disable and enable compute node problem
12. JDBC: fix data source is closed in ShardingSphereDriver cluster mode when startup problem
13. Kernel: fix wrong rewrite result when part of logical table name of the binding table is consistent with the actual table name, and some are inconsistent
14. Kernel: fix startup exception when use SpringBoot without configuring rules
15. Encrypt: fix null pointer exception when Encrypt value is null
16. Kernel: fix oracle parsing does not support varchar2 specified type
17. Kernel: fix serial flag judgment error within the transaction
18. Kernel: fix cursor fetch error caused by `wasNull` change
19. Kernel: fix alter transaction rule error when refresh metadata
20. Encrypt: fix `EncryptRule` cast to `TransparentRule` exception that occurs when the call procedure statement is executed in the `Encrypt` scenario
21. Encrypt: fix exception which caused by `ExpressionProjection` in shorthand projection
22. Proxy: fix PostgreSQL Proxy int2 negative value decoding incorrect
23. Proxy: PostgreSQL/openGauss support describe insert returning clause
24. Proxy: fix gsql 3.0 may be stuck when connecting Proxy
25. Proxy: fix parameters are missed when checking SQL in Proxy backend
26. Proxy: enable MySQL Proxy to encode large packets
27. Kernel: fix oracle parse comment without whitespace error
28. DistSQL: fix show create table for encrypt table

## Refactor

1. Scaling: reverse table name and column name when generating SQL if it's SQL keyword
2. Scaling: improve incremental task failure handling
3. Kernel: governance center node adjustment, unified hump to underscore

# Links

🔗 [Download Link](https://shardingsphere.apache.org/document/current/en/downloads/)

🔗 [Release Notes](https://github.com/apache/shardingsphere/discussions/22564)

🔗 [Project Address](https://shardingsphere.apache.org/)

🔗 [ShardingSphere-on-Cloud](https://github.com/apache/shardingsphere-on-cloud)

# Community Contribution

This Apache ShardingSphere 5.3.0 release is the result of 687 merged PRs, committed by 49 contributors. Thank you for your efforts.

![img](https://shardingsphere.apache.org/blog/img/2022_12_08_ShardingSphere_5.3.0_is_released_new_features_and_improvements2.png)
