+++
title = "ShardingSphere 5.3.2 Release: Apollo Integration, Better Data Migration, and Stronger Encryption"
weight = 94
chapter = true 
+++

![img](https://shardingsphere.apache.org/blog/img/2023_04_03_ShardingSphere_5.3.2_Release_Apollo_Integration,_Better_Data_Migration,_and_Stronger_Encryption1.png)

Apache ShardingSphere has recently released version 5.3.2 after two months of development, which includes 797 PRs from teams and individuals worldwide. The new version includes significant improvements in functionality, performance, testing, documentation, and examples. With this update, users can experience an enhanced and problem-solving version. Let's take a quick preview of what's new:

- ShardingSphere Driver now supports Apollo, which is a great addition for users.
- Data migration now supports migrating tables with any index structure, including tables without an index, making it more convenient for users.
- The data encryption fuzzy query now supports the CONCAT function, improving the user's experience.

One of the highlights of this update is that the ShardingSphere Driver now supports Apollo. In version 5.3.0, ShardingSphere removed modules such as Spring Boot Starter and standardized the use of the standard JDBC Driver to specify configuration files. However, the ShardingSphere Driver could only read configuration from the file system, which was limited to scenarios where the configuration was managed uniformly in the configuration center. With the recent update, ShardingSphere 5.3.2 adjusted the configuration reading of the Driver to an SPI interface and achieved configuration reading from the Apollo configuration center.

![img](https://shardingsphere.apache.org/blog/img/2023_04_03_ShardingSphere_5.3.2_Release_Apollo_Integration,_Better_Data_Migration,_and_Stronger_Encryption2.png)

The JDBC URL is written as follows:

```bash
# Read the configuration file from the absolute path
jdbc:shardingsphere:absolutepath:/path/to/config.yml

# Read the configuration file from the classpath
jdbc:shardingsphere:classpath:config/shardingsphere/config.yml

# Read the configuration file from the Apollo configuration center
jdbc:shardingsphere:apollo:apollo.meta?appid=ss-test&namespace=config
```

Of course, providing an SPI interface means that developers can customize the configuration reading logic according to their own scenarios, such as implementing configuration reading from other registration centers. At the same time, the ShardingSphere community welcomes developers to contribute to the common configuration implementation, allowing more users to benefit from it.

## Migrate Tables with Any Index Structure, Including Tables Without an Index

ShardingSphere is now providing support for migrating tables with any index structure, including tables without an index. This is a significant improvement that will greatly benefit users with complex application scenarios.

In some cases, tables may have composite primary keys, while in others, they may not have primary keys but have composite unique indexes. In some cases, tables may not have any primary keys or indexes at all. ShardingSphere has taken these complex scenarios into account and has made certain improvements to support data migration for such cases.

This is a significant step forward in providing users with more flexibility in managing their data and ensuring that their applications are running smoothly. The improved data migration support will make it easier for users to manage their data, regardless of the complexity of their table structures.

![img](https://shardingsphere.apache.org/blog/img/2023_04_03_ShardingSphere_5.3.2_Release_Apollo_Integration,_Better_Data_Migration,_and_Stronger_Encryption3.png)

[1] Concurrency is supported between shards, but data partitioning within a table does not support concurrency.

[2] `DATA_MATCH` consistency check is not supported. `CRC32_MATCH` is only supported by MySQL.

[3] Breakpoint resume is supported only for the first field.

[4] Primary key tables of some field types can not be aggregated and sorted, so the `DATA_MATCH` consistency check is not supported, such as `VARBINARY` in MySQL.

## The Data Encryption Fuzzy Query Now Supports the CONCAT Function

Since ShardingSphere 5.3.0 supported encrypted column fuzzy queries, the community received feedback from users regarding this function. The fuzzy query `LIKE` is often used in conjunction with the wildcard `%` through the `CONCAT` function in SQL. Some users reported that ShardingSphere does not support queries with wildcards concatenated through the `CONCAT` function in SQL and can only complete wildcard concatenation on parameter values themselves.

With the release of version 5.3.2, ShardingSphere has made further improvements in supporting functions in encryption. Now, the wildcard for fuzzy queries can be used directly in SQL in the following format:

```
select * from t where user_name like CONCAT('%', ?, '%')
```

Regarding the support for other functions, if you are interested in Apache ShardingSphere, you are welcome to participate in the community to help us achieve further improvement in the support for other functions.

## Release Notes

**API Changes**

- Proxy: Added the property `system-log-level` to support dynamic changes to the log level.
- DistSQL: Removed the Hint-related DistSQL; users can now use SQL `Hint` instead.

**New Features**

- Scaling: Pipeline job now supports tables with any index structure.

**Enhancements**

- Scaling: Uses JDBC stream query for inventory dump and data consistency check.
- Scaling: Pipeline jobs are now compatible with tables that have MySQL binary-type fields as primary keys.
- Scaling: The migration and other jobs are now implemented in a blocking manner, making it easier for ejob to obtain the real lifecycle of the jobs and support failover.
- Scaling: Improved large table records count estimation.
- Scaling: Support proxy sharding rule absent for migration jobs, including migration to a single table or pure encryption.
- Scaling: Added `useServerPrepStmts=false` to avoid exceeding the limit of precompiled statements on the MySQL server.
- Scaling: MySQL binlog now returns accurate data types when parsing datetime/time instead of strings.
- Scaling: Added global status for the pipeline preparation stage.
- Scaling: Added `netTimeoutForStreamingResults` to prevent MySQL streaming data consumption from being too slow and causing the connection to be closed by the server.
- Authority: Supports specifying password authentication method.
- Authority: Added md5 authentication support for openGauss protocol.
- Agent: Added more monitoring metrics for JDBC.
- Kernel: ShardingSphere Driver configuration is now based on SPI and supports Apollo.
- Kernel: Adjusted `SKIP_ENCRYPT_REWRITE `SQL Hint to `SKIP_SQL_REWRITE`to support more scenarios.
- Kernel: Support openGauss `EXPLAIN PERFORMANCE` syntax.
- Encrypt: `LIKE` now supports `CONCAT` function.

**Bug Fixes**

- Scaling: Fixed the issue where `TestDecodingPlugin` parsed Unicode characters incorrectly.
- Scaling: PostgreSQL migration now supports fields related to JSON.
- DistSQL: `CREATE SHARDING TABLE RULE` now supports `NONE` strategy.
- Kernel: Fixed the issue of an error when using the system database with an empty data source.
- Kernel: Fixed the issue where setting worker-id does not take effect with Standalone mode.
- Kernel: Cleared storage node information when deleting read/write-splitting and database discovery rules.
- Kernel: Fixed the abnormal problem of Column index out of range in single table complex query.
- Kernel: Fixed the issue where PostgreSQL `LIKE` queries (using lowercase) failed.
- Kernel: Fixed the exception of built-in metabase data collection when the front and back database types were inconsistent.
- Kernel: Fixed the problem of routing error reporting under certain table names.
- Kernel: Fixed the issue where parsing errors occurred when creating MySQL stored procedures.
- Kernel: Fixed NPE caused by incorrect extraction of table names in union statements.
- Kernel: Fixed the issue where uppercase table name constraints were not rewritten.
- Kernel: Fixed the issue where PostgreSQL/OpenGauss SQL containing money type caused parsing errors.
- Kernel: Fixed the issue where using placeholders and type conversions simultaneously in PostgreSQL/OpenGauss caused SQL parsing errors.
- Kernel: Fixed the issue where syntax errors occurred when parsing constant type conversions in PostgreSQL/OpenGauss.
- Kernel: Fixed Chinese character encoding exception when executing select with SQL Federation engine.
- Kernel: Fixed IndexOutOfBoundsException when executing set variable statement in JDBC adapter.
- Kernel: Fixed index does not exist exception when executing drop index statement.
- Proxy: Properly handled number signs in MySQL Proxy binary protocol.
- Proxy: Fixed the issue where PostgreSQL/openGauss Proxy failed to handle bytea data type.
- Proxy: Fixed the issue where PostgreSQL Proxy failed to parse time value with microseconds.
- Proxy: Fixed the exception when the client passes quoted charset to PostgreSQL/openGauss Proxy.

Overall, this new version of Apache ShardingSphere brings significant improvements and features that can help users solve their problems more efficiently. The community has done an excellent job of addressing user feedback and improving the overall user experience.

This Apache ShardingSphere 5.3.2 release is the result of 797 merged PRs, made by 37 contributors. Thank you for your passion!

![img](https://shardingsphere.apache.org/blog/img/2023_04_03_ShardingSphere_5.3.2_Release_Apollo_Integration,_Better_Data_Migration,_and_Stronger_Encryption4.png)

# Relevant Links

🔗 [Download Link](https://shardingsphere.apache.org/document/current/en/downloads/)

🔗 [Release Notes](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md)

🔗 [Project Address](https://shardingsphere.apache.org/)

🔗 [ShardingSphere-on-Cloud](https://github.com/apache/shardingsphere-on-cloud)
