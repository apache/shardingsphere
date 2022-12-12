+++
title = "Apache ShardingSphere 5.1.0 Now Available"
weight = 35
chapter = true
+++

# Apache ShardingSphere 5.1.0 Now Available
![1](https://shardingsphere.apache.org/blog/img/Apache_ShardingSphere_releases_5.1.0.png)

[Apache ShardingSphere 5.1.0](https://github.com/apache/shardingsphere) is officially released and available. The previous 5.0.0 GA version was launched in November last year, and marked ShardingSphere’s evolution from middleware to an ecosystem.

This meant gaining the power to transform any database in a distributed database system, and enhance it with features such as data sharding, distributed transaction, data encryption, SQL audit, database gateway, and more.

For the past three months, the ShardingSphere community received a lot of feedback from developers, partners, and users across different industries. We’d like to extend our gratitude for the feedback they provided, because, without it, this update would not be possible.

Our community author and Apache ShardingSphere PMC, [Meng Haoran](https://twitter.com/HaoranMeng2), shares with you in detail what’s new in Apache ShardingSphere version 5.1.0.

Based on user feedback from the 5.0.0 GA version, we also decided to commit our efforts to improve ShardingSphere’s ecosystem, kernel and feature modules:

## Kernel
Building a powerful and stable kernel has always been the purpose of ShardingSphere.

In the new version we fix a large number of issues to better support parsing for [PostgreSQL](https://www.postgresql.org/) and [openGauss](https://opengauss.org/en/) SQL, and now support function parsing and `binlog` statement parsing.

W also optimized the rewriter engine and improved efficiency for loading massive single tables, to further improve overall kernel performance. Moreover, ShardingSphere now adds the SQL `hint` function that enables users to use the forced routing function more conveniently.

## Access Terminal
For ShardingSphere-Proxy, we fix the issue of parsing [MySQL](https://www.mysql.com/)/PostgreSQL protocol, while we also added `SCRAM SHA-256`authentication mode to support openGauss and optimize the openGauss batch inserts protocol to improve the data insert performance.

For ShardingSphere-JDBC, we removed check for `NULL` values in rules, so users can still use JDBC even if there is no value in rules. We also optimized the metadata of the logical database only loading the specified `schemaName`to accelerate boot-up.

## Elastic Scale-Out

We made many adjustments to elastic scale-out in this version.

First, the original scaling module is moved to the data-pipeline module under the kernel. In the future, this module will provide most data processing capabilities except for data migration.

Second, scaling configuration has been moved from `server.yaml` to the`config-sharding.yaml` configuration file. Together with data sharding, elastic scale-out will provide users with better data sharding services.

## DistSQL
Many practical languages can now be implemented. More tools are provided for users to manage the ShardingSphere distributed database ecosystem.

Some distributed cluster governance capabilities are optimized as well. For example, when users enable/stop instances through `instanceId` while there is only one secondary database, the users will be informed that they cannot stop the instances — significantly improving user experience.

## Read/Write Splitting and High Availability
The API of read/write splitting and high availability are both optimized. Read/write splitting now supports both static and dynamic configurations, while the static configuration needs to be used with high availability.

The high availability configuration and algorithm are isolated, making its configuration more unified and concise. Additionally, `SpringBoot` and `Spring Namespace`now support the configuration of high availability as well as the implementation of openGauss’ high availability feature.

## Shadow Database
The shadow database feature has been partly optimized. It now supports logic data source transmission, provides checking for data types that are not supported by column matching shadow algorithms, annotates that shadow algorithm is reconstructed as `HINT` shadow algorithm, removes enable attribute in configuration, and optimizes the determining logic of shadow algorithm, improving performance.

This post only covers a a part of the updates we made to some functions. While developing version 5.1.0, we merged 1000+ PRs from the community. Based on version 5.0.0 GA, version 5.1.0 has been significantly improved in terms of its kernel capabilities, core functions, and performance to deliver a better user experience.

Here are the details of the release of version 5.1.0:

## New Features
1. Support SQL `hint`

2. New DistSQL syntax: `SHOW AUTHORITY RULE`

3. New DistSQL syntax: `SHOW TRANSACTION RULE`

4. New DistSQL syntax: `ALTER TRANSACTION RULE`

5. New DistSQL syntax: `SHOW SQL_PARSER RULE`

6. New DistSQL syntax: `ALTER SQL_PARSER RULE`

7. New DistSQL syntax: `ALTER DEFAULT SHARDING STRATEGY`

8. New DistSQL syntax: `DROP DEFAULT SHARDING STRATEGY`

9. New DistSQL syntax: `CREATE DEFAULT SINGLE TABLE RULE`

10. New DistSQL syntax: `SHOW SINGLE TABLES`

11. New DistSQL syntax: `SHOW SINGLE TABLE RULES`

12. New DistSQL syntax: `SHOW SHARDING TABLE NODES`

13. New DistSQL syntax: `CREATE/ALTER/DROP SHARDING KEY GENERATOR`

14. New DistSQL syntax: `SHOW SHARDING KEY GENERATORS`

15. New DistSQL syntax: `REFRESH TABLE METADATA`

16. New DistSQL syntax: `PARSE SQL`, output the abstract syntax tree obtained by parsing SQL

17. New DistSQL syntax: `SHOW UNUSED SHARDING ALGORITHMS`

18. New DistSQL syntax: `SHOW UNUSED SHARDING KEY GENERATORS`

19. New DistSQL syntax: `CREATE/DROP SHARDING SCALING RULE`

20. New DistSQL syntax: `ENABLE/DISABLE SHARDING SCALING RULE`

21. New DistSQL syntax: `SHOW SHARDING SCALING RULES`

22. New DistSQL syntax: `SHOW INSTANCE MODE`

23. New DistSQL syntax: `COUNT SCHEMA RULES`

24. Scaling: Add `rateLimiter` configuration and QPS TPS implementation

25. Scaling: Add `DATA_MATCH` data consistency check

26. Scaling: Add `batchSize` configuration to avoid possible OOME

27. Scaling: Add `streamChannel`configuration and `MEMORY` implementation

28. Scaling: Support MySQL `BINARY` data type

29. Scaling: Support MySQL `YEAR` data type

30. Scaling: Support PostgreSQL `BIT` data type

31. Scaling: Support PostgreSQL `MONEY` data type

32. Database discovery adds support for JDBC `Spring Boot`

33. Database discovery adds support for JDBC `Spring Namespace`

34. Database discovery adds support for openGauss

35. Shadow DB adds support for logical data source transfer

36. Add data type validator for column matching shadow algorithm

37. Add support for `xa start/end/prepare/commit/recover` in encrypt case with only one data source

## API Changes
1. Redesign the database discovery related DistSQL syntax

2. In DistSQL, the keyword `GENERATED_KEY` is adjusted to `KEY_GENERATE_STRATEGY`

3. Native authority provider is marked as deprecated and will be removed in a future version

4. Scaling: Move scaling configuration from `server.yaml` to `config-sharding.yaml`

5. Scaling: Rename `clusterAutoSwitchAlgorithm` SPI to `completionDetector` and refactor method parameter

6. Scaling: Data consistency check API method rename and return type change


8. Database discovery module API refactoring
Read/write-splitting supports static and dynamic configuration

9. Shadow DB remove the enable configuration

10. Shadow algorithm type modified

## Enhancements
1. Improve load multi single table performance

2. Remove automatically added order by primary key clause

3. Optimize binding table route logic without sharding column in join condition

4. Support update sharding key when the sharding routing result keep the same

5. Optimize rewrite engine performance

6. Support `select union/union all` statements by federation engine

7. Support insert on duplicate key update sharding column when route context keep same

8. Use union all to merge sql route units for simple select to improve performance

9. Supports autocommit in `ShardingSphere-Proxy`

10. ShardingSphere openGauss Proxy supports `SHA-256` authentication method

11. Remove property `java.net.preferIPv4Stack=true` from Proxy startup script

12. Remove the verification of null rules for JDBC

13. Optimize performance of executing openGauss batch bind

14. Disable Netty resource leak detector by default

15. Supports describe prepared statement in PostgreSQL / openGauss Proxy

16. Optimize performance of executing PostgreSQL batched inserts

17. Add `instance_id` to the result of `SHOW INSTANCE LIST`

18. Support to use `instance_id` to perform operations when `enable/disable` a proxy instance

19. Support auto creative algorithm when `CREATE SHARING TABLE RULE`, reducing the steps of creating rule

20. Support specifying an existing KeyGenerator when `CREATE SHARDING TABLE RULE`

21. `DROP DATABASE` supports `IF EXISTS` option

22. `DATANODES` in `SHARDING TABLE RULE` supports enumerated inline expressions

23. `CREATE/ALTER SHARDING TABLE RULE` supports complex sharding algorithm

24. `SHOW SHARDING TABLE NODES` supports non-inline scenarios (range, time, etc.)

25. When there is only one read data source in the read/write-splitting rule, it is not allowed to be disabled

26. Scaling: Add basic support of chunked streaming data consistency check

27. Shadow algorithm decision logic optimization to improve performance

## Refactoring
1. Refactor federation engine scan table logic

2. Avoid duplicated TCL SQL parsing when executing prepared statement in Proxy

3. Scaling: Add pipeline modules to redesign scaling

4. Scaling: Refactor several job configuration structure

5. Scaling: Precalculate tasks splitting and persist in job configuration

6. Scaling: Add basic support of pipeline-core code reuse for encryption job

7. Scaling: Add basic support of scaling job and encryption job combined running

8. Scaling: Add `input` and `output` configuration, including `workerThread` and `rateLimiter`

9. Scaling: Move `blockQueueSize` into `streamChannel`

10. Scaling: Change `jobId` type from integer to text

11. Optimize JDBC to load only the specified schema

12. Optimize meta data structure of the registry center

13. Rename Note shadow algorithm to `HINT` shadow algorithm

## Bug Fixes
1. Support parsing function

2. Fix alter table drop constrain

3. Fix optimize table route

4. Support Route resource group

5. Support parsing binlog

6. Support PostgreSQL/openGauss `&` and `|` operator

7. Support parsing openGauss insert on duplicate key

8. Support parse postgreSql/openGauss union

9. Support query which table has column contains keyword

10. Fix missing parameter in function

11. Fix sub query table with no alias

12. Fix utc `timestamp` function

13. Fix `alter encrypt` column

14. Support alter column with position `encrypt` column

15. Fix delete with schema for postgresql

16. Fix wrong route result caused by Oracle parser ambiguity

17. Fix projection count error when use sharding and encrypt

18. Fix `npe` when using shadow and `readwrite_splitting`

19. Fix wrong metadata when actual table is case insensitive

20. Fix encrypt rewrite exception when execute multiple table join query

21. Fix encrypt rewrite wrong result with table level `queryWithCipherColumn`

22. Fix parsing chinese

23. Fix encrypt exists sub query

24. Fix full route caused by the MySQL `BINARY` keyword in the sharding condition

25. Fix `getResultSet` method empty result exception when using `JDBCMemoryQueryResult` processing statement

26. Fix incorrect shard table validation logic when creating store function/procedure

27. Fix null charset exception occurs when connecting Proxy with some PostgreSQL client

28. Fix executing commit in prepared statement cause transaction status incorrect in MySQL Proxy

29. Fix client connected to Proxy may stuck if error occurred in PostgreSQL with non English locale

30. Fix file not found when path of configurations contains blank character

31. Fix transaction status may be incorrect cause by early flush

32. Fix the unsigned datatype problem when query with `PrepareStatement`

33. Fix protocol violation in implementations of prepared statement in MySQL Proxy

34. Fix caching too many connections in openGauss batch bind

35. Fix the problem of missing data in `SHOW READWRITE_SPLITTING RULES` when `db-discovery` and `readwrite-splitting` are used together

36. Fix the problem of missing data in `SHOW READWRITE_SPLITTING READ RESOURCES` when `db-discovery` and `readwrite-splitting` are used together

37. Fix the NPE when the CREATE SHARDING TABLE RULE statement does not specify the sub-database and sub-table strategy

38. Fix `NPE` when `PREVIEW SQL` by `schema.table`

39. Fix `DISABLE` statement could disable readwrite-splitting write data source in some cases

40. Fix `DIABLE INSTANCE` could disable the current instance in some cases

41. Fix the issue that user may query the unauthorized logic schema when the provider is `SCHEMA_PRIVILEGES_PERMITTED`

42. Fix `NPE` when authority provider is not configured

43. Scaling: Fix DB connection leak on XA initialization which triggered by data consistency check

44. Scaling: Fix PostgreSQL replication stream exception on multiple data sources

45. Scaling: Fix migrating updated record exception on PostgreSQL incremental phase

46. Scaling: Fix MySQL 5.5 check `BINLOG_ROW_IMAGE` option failure

47. Scaling: Fix PostgreSQL xml data type consistency check

48. Fix database discovery failed to modify cron configuration

49. Fix single read data source use weight `loadbalance` algorithm error

50. Fix create redundant data souce without memory mode

51. Fix column value matching shadow algorithm data type conversion exception

![2](https://shardingsphere.apache.org/blog/img/List_of_contributors.png)

## Apache ShardingSphere Open Source Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)

[ShardingSphere Slack Channel](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9%7EI4rYcR18bq0SYTg)

[Contributor Guide](https://shardingsphere.apache.org/community/cn/involved/)

## Author

**Haoran Meng**

SphereEx Senior Development Engineer

Apache ShardingSphere PMC

Previously responsible for the database products R&D at JingDong Technology, he is passionate about Open-Source and database ecosystems. Currently, he focuses on the development of the ShardingSphere database ecosystem and open source community building.

![2](https://shardingsphere.apache.org/blog/img/Meng_Haoran_Photo.png)
