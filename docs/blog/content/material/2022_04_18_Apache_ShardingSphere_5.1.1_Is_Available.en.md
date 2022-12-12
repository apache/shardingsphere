+++ 
title = "Apache ShardingSphere 5.1.1 Is Available"
weight = 50
chapter = true 
+++

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/kabom19xrf3ch18gfrk4.png)
> In less than two months since the release of Apache ShardingSphere 5.1.0, our community has merged 698 PRs from teams or individuals around the world to bring you the new 5.1.1 release. This release has a number of optimizations in terms of features, performance, testing, documentation, examples and more.

> The performance aspect deserves special mention, as we adopt the industry-standard TPC-C test model to run Apache ShardingSphere 5.1.1 and the openGauss 3.0 database on 16 physical servers, achieving over 10 million tpmC, the best performance in the industry at the same scale!

> This article is an overview of the ShardingSphere 5.1.1 update.

Apache ShardingSphere 5.1.1 update optimizes the functionality and performance of 5.1.0, and fixes some issues. The following will give you a general overview of some of the updates.

## Kernel
The kernel is ShardingSphere’s foundation, and it remains our goal to build a fully functional and stable kernel with high performance. In 5.1.1, ShardingSphere optimizes a lot of logic at the kernel level, fixing the problems found in different scenarios in the previous version, including data sharding, read/write splitting, data encryption, and improved the performance of each scenario to varying degrees.

In terms of database dialects, the kernel improves the support for [MySQL](https://www.mysql.com/) tablespaces and improves SQL support for [Oracle](https://www.oracle.com/database/), [SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads), [PostgreSQL](https://www.postgresql.org/), [openGauss](https://opengauss.org/en/), and other databases. Additionally, the initial support for PostgreSQL / openGauss schema, a three-tier structure, has been implemented in the ShardingSphere kernel and will be improved with the future releases.

## Interface
This update fixes a potential performance risk for ShardingSphere-JDBC, optimizes the non-essential interaction logic between internal ShardingSphere-JDBC and the database connection pool, and reduces the performance overhead of ShardingSphere-JDBC.

In terms of ShardingSphere-Proxy, support for MySQL / PostgreSQL data types has been improved. The occasional `ResultSet` closed error for ShardingSphere-Proxy MySQL has been fixed, and ShardingSphere-Proxy MySQL also has initial support for executing multiple statements at the protocol level to improve the performance of batch operations.

In terms of client support, ShardingSphere-Proxy PostgreSQL improves the support for PostgreSQL JDBC Driver 42.3.x, and ShardingSphere-Proxy openGauss improves the support for openGauss JDBC Driver 3.0.

## Elastic Scale-out
In this update, in addition to fixing the problem that migration tasks cannot be restarted by DistSQL after reporting errors, the elastic scaling has been improved and new features of stop source writing, resume source writing, and partial tables scaling have been implemented. For cases that do not meet the migration conditions, elastic scale-out can fail quickly and find out the problem in time to avoid extra time costs for users.
 

## DistSQL
DistSQL has been optimized in terms of user experience, adding more calibration and reducing the possibility of user configuration errors in using DistSQL. We also fixed an issue where some user input conflicted with DistSQL keywords.

## Distributed Governance
In terms of read/write splitting and database discovery, in addition to the original MGR, MySQL adds a new database discovery method by querying primary-secondary delay, which can automatically switch read/write splitting data sources by getting the secondary delay, reducing the threshold for users to use dynamic read/write splitting.

In cluster mode, the metadata storage structure has been optimized and reconstructed, and the problems caused by ZooKeeper session timeout and case mismatch on table names have been fixed.

## Distributed Transactions
In terms of transactions, ShardingSphere-JDBC adds support for Savepoint, and ShardingSphere-Proxy adds support for Savepoint in XA scenarios, in addition to the original support for `Savepoint` for `LOCAL` transactions.

When Narayana is used as an XA implementation, ShardingSphere supports the configuration of Narayana to make the use of XA more conveniently.

With PostgreSQL / openGauss, when an exception occurs in a transaction, ShardingSphere can properly abort the transaction and automatically roll back.

The above is an introduction to some of the updates to Apache ShardingSphere 5.1.1, please refer to the update log for details. The ShardingSphere community will be releasing detailed explanations of some of these features, so stay tuned.

ShardingSphere 5.1.1 has no changes at the API level but has made many improvements in functionality and performance — you’re welcome to try it out.

## Update Log

### New Features

- Kernel: PostgreSQL supports `alter materialized` view
- Kernel: PostgreSQL supports `declare `syntax
- Kernel: PostgreSQL supports `discard` syntax
- Kernel: PostgreSQL supports `$$` mark
- Kernel: Support MySQL to create Tablespace Statements
- Elastic Scale-out: Implement stop source writing and resume source writing
- Elastic Scale-out: Support partial tables scaling
- DistSQL: New syntax `SHOW UNUSED RESOURCES`
- Distributed Governance: New persistent XA recovery Id in governance center
- Distributed Governance: Database discovery adds a new feature of delaying master-slave delay
- Distributed Transaction: ShardingSphere-Proxy supports savepoint
- Distributed Transaction: PostgreSQL & openGauss transaction exceptions, automatic rollback
- Distributed Transaction: Narayana XA transaction ease
- Distributed Transaction: ShardingSphere-JDBC supports savepoint

### Optimizations

- Kernel: Refactor kernel functional code to improve performance
- Interface: Reduce ShardingSphere-Proxy Docker image size
- Interface: ShardingSphere-Proxy supports setting character encoding with syntaxes such as `set names`
- Interface: ShardingSphere-Proxy MySQL Supports Bulk Statements
- Interface: ShardingSphere-Proxy supports the openGauss JDBC Driver 3.0 client
- Elastic Scale-out: Only one Proxy node in a ShardingSphere-Proxy cluster needs to do completion detection
- Elastic Scale-out: Optimize the field types in the input and output configurations, changing them from int to Integer to make it easier to configure as null via DistSQL
- Elastic Scale-out: Optimize MySQL calibration and SQL
- Elastic Scale-out: Optimize progress deletion and progress checking
- Elastic Scale-out: Optimize the problem that FinishedCheckJob cannot do a completion check under the error state
- Elastic Scale-out: If there are tables that are not eligible for migration, report an error as soon as possible
- Elastic Scale-out: Reuse ClusterPersistRepository when PipelineAPIFactory creates GovernanceRepositoryAPI
- Elastic Scale-out: Upgrade jobId generation algorithm; jobId supports idempotency
- DistSQL: `CREATE/ALTER ENCRYPT RULE` syntax supports configuring data types and lengths
- DistSQL; Unify the display of `SHOW ALL VARIABLES` and `SHOW VARIABLE `syntax
- DistSQL: `DROP BINDING TABLE RULES `syntax eliminates the effect of binding order on deletion results
- DistSQL: `SHOW INSTANCE LIST` syntax adds the display of `mode_type` fields
- DistSQL: `ENABLE/DISABLE INSTANCE `syntax adds calibration of patterns
- DistSQL: Add calibration to see if a rule is in use when deleting a read/write splitting rule
- DistSQL: Add calibration for resource renaming when creating read/write splitting rules
- DistSQL: `SHOW READWRITE_SPLITTING READ RESOURCES` adds display of the delay time
- DistSQL: `DROP RULE` syntax supports pre-judgement of `IF EXISTS`
- DistSQL: Optimize `ADD/ALTER RESOURCE `connection failure messages
- Distributed Governance: DistSQL Add schema version number to support bulk execution of DistSQL
- Distributed Governance: Optimize persistent metadata in clustered mode
- Distributed Governance: Database discovery creates `JOB` to add schemaName marker

## Refactoring

- Kernel: Refactor encryption and decryption test cases
- Kernel: Refactor the metadata model to fit the PostgreSQL database and schema model
- Elastic Scale-out: Pipeline module removes HikariCP dependencies
- Distributed Governance: Reconfigure the storage node structure of the governance centre
- Distributed Governance: Refactor the metadata structure of the governance centre
- Distributed Governance: Adjust database discovery MGR module to MySQL module

## Problem Fixes

- Kernel: Fix the exception where a constant could not get a variable
- Kernel: Fix `InsertValueContext.getValue` conversion exceptions
- Kernel: Fix distinct aggregate function column exception

![Image description](https://dev-to-uploads.s3.amazonaws.com/uploads/articles/97yc8p3q36qmko4hr25d.png)
 

## Apache ShardingSphere Open Source Project Links:

[ShardingSphere Github](https://github.com/apache/shardingsphere/issues?page=1&q=is%3Aopen+is%3Aissue+label%3A%22project%3A+OpenForce+2022%22)

[ShardingSphere Twitter](https://twitter.com/ShardingSphere)
[ShardingSphere Slack](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md)
[ShardingSphere Medium ](https://medium.com/@shardingsphere)
[ShardingSphere Release Note](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md)

## Author
**Weijie WU**

> SphereEx Infrastructure R&D Engineer, Apache ShardingSphere PMC
> He focuses on the R&D of Apache ShardingSphere interface and ShardingSphere subproject ElasticJob.

