+++
title = "Following 6 months of development Apache ShardingSphere 5.0.0-beta has been officially released!"
weight = 14
chapter = true
+++

As an Apache top-level project, ShardingSphere goes through community verification, voting and many other steps before it can be released. Such steps ensure the release is compliant with the Apache Release License specifications, and meeting the users’ expectations set for the 5.0.0-beta milestone. The current version’s architecture has been completed and the version is officially available.

## Release Features:

### 1. Highlight Features

#### DistSQL – A New SQL Type for a Distributed Database Ecosystem

SQL is a database query programming language for accessing, querying, updating, and managing relational database systems. Most of the existing general database systems tend to rewrite and extend SQL to better fit their own database system with higher flexibility and functionality.

DistSQL (Distributed SQL) is a special built-in language proposed by Apache ShardingSphere providing additional functional operation capability in comparison to the standard SQL. Users can use ShardingSphere just like other database systems with DistSQL, therefore no longer positioning ShardingSphere as a middleware architecture for programmers, but also making it transferable to an infrastructure product for operation and maintenance.

ShardingSphere currently includes three types of DistSQL including RDL, RQL and RAL:

* RDL (Resource & Rule Definition Language): create, modify and delete resources and rules.
* RQL (Resource & Rule Query Language): query and show resources and rules.
* RAL (Resource & Rule Administration Language): hint, distributed transaction switching, distributed query execution plan and other incremental functions.

ShardingSphere proposes the concept of Database Plus, empowering traditional databases to build a highly secure & enhanced distributed database ecosystem, by leveraging open-source database system software such as MySQL and PostgreSQL, while at the same time meeting practical business needs. The distributed SQL used with this distributed database system converts ShardingSphere-Proxy from a YAML configuration driven middleware to a SQL driven distributed database system.

In the 5.0.0-beta, users can easily initiate ShardingSphere-Proxy and use DistSQL to dynamically create, modify, delete sharding tables, encrypt tables, and dynamically inject instances of database resources, create read-write-splitting strategies, show all the configurations and distributed transaction types, engage in dynamic migration of distributed database tables etc.
DistSQL allows users to query and manage all database resources and ShardingSphere’s distributed metadata in a standardized and familiar way. 

In the future DistSQL will further redefine the boundary between Middleware and database, allowing users to leverage ShardingSphere as if they were using a database natively.

#### Full Join PostgreSQL Open-Source Ecosystem

PostgreSQL is widely considered to be the most powerful enterprise-level open-source database.
ShardingSphere clients include ShardingSphere-JDBC and ShardingSphere-Proxy, with ShardingSphere-Proxy containing both MySQL and PostgreSQL. As PostgreSQL has greatly matured and become increasingly adopted, the ShardingSphere team has directed its attention to the PostgreSQL proxy. 

This release has greatly improved PostgreSQL in its SQL parsing and compatibility layers, protocol access and permission control layers. 
The improved version of ShardingSphere-Proxy PostgreSQL is this release’s main feature and will be continuously improved in the future as it marks the beginning of compatibility with the open-source PostgreSQL.

In the future, ShardingSphere PostgreSQL-Proxy will not only provide sharding and security distributed solutions to users, but also fine-grained authentication, data access control etc. 

#### ShardingSphere Pluggable Architecture

Pluggable architecture pursues the independence and non-awareness of each module through a highly flexible, pluggable and extensible kernel. These functions are combined in a super positional manner.

In ShardingSphere, many functional implementation classes are loaded by inserting SPIs (Service Provider Interface). SPI APIs for third party implementation or extension, can be used for architecture extension or component substitution.

Currently data sharding, Readwrite-splitting, data encryption, Shadow databases, instance exploration and other functions including the protocol implementations of MySQL, PostgreSQL, SQLServer, Oracle and other SQL and protocol support can be installed into ShardingSphere as plug-ins. ShardingSphere now offers dozens of SPIs as extension points for the system, and the number is still growing. 

Pluggable architecture’s improvement effectively evolves ShardingSphere into a distributed database ecosystem. The pluggable and extensible concepts provide a customized combinational database solution that can be built upon with Lego-like blocks. For example, traditional relational databases can be scaled out and encrypted at the same time, while distributed database solutions can be built independently.

### 2. New Features

#### New Open Observability

ShardingSphere provides automated probes to effectively separate observability from the main functionality. This brings significant convenience for user-customized tracing, metrics, and logging. 
OpenTracing, Jaeger, Zipkin based tracing probes and Prometheus Metrics probes, as well as a default logging implementation also have built-in implementations.

### 3. Enhancements

#### Enhanced Distributed Query Capability

Join and sub queries for cross-database instances are some of the most cumbersome issues. Using traditional database middleware could limit business level functionality - therefore SQL’s application scope needs to be considered by R&D personnel.

This release enhances distributed query functionality which supports join queries and sub-queries across different database instances. At the same time it greatly improves compatibility between supported SQL cases for MySQL/PostgreSQL/Oracle/SQLServer in distributed scenarios through SQL parsing, routing, and execution level enhancements and bug fixing. 

The improvements in this release allow users to achieve a smooth transition from a traditional database cluster to a distributed database cluster with low risk, high efficiency and zero transformation by introducing ShardingSphere. 

Currently, distributed query capability enhancement is still in the POC stage, with room for improvement in terms of performance. The community is warmly encouraged to participate in the co-development.

#### Enhanced Distributed User and Privilege Control

User security and permission control some of the most important functions in a database field. Although simple password setting and database-level access control at the library level were already provided in the 5.0.0-alpha, these features have now been further upgraded. This update changes the password setting from using configuration file to using standard SQL online to create and update distributed users and their access permissions. 

Whether you were using MySQL, PostgreSQL or OpenGauss in your business scenario, you can continue to use your native database SQL dialect. Username, hostname, password, library, table and other free combination of authority control management can be used in ShardingSphere’s distributed system. ShardingSphere-Proxy's Proxy access mode enables users to migrate their original database permissions and user systems seamlessly.

In future releases, access control at the column level, view level, and even the possibility for functions to limit access for each row of data will be provided. ShardingSphere also provides access to third-party business systems or user-specific security systems, allowing ShardingSphere-Proxy to connect with third-party security control systems and provide the most standard database access management mode.

The permission module is currently in the development stage, and the finalized functions will be presented in the next version.

#### Simplified API Changes Capabilities

ShardingSphere's pluggable architecture provides users with rich scalable capabilities with common functions already built-in, to increase ease of use. 
For example, database and table sharding strategy is preset with hash sharding, time range sharding, module sharding and other strategies. 
Data encryption is preset with AES, RC4, MD5 encryption. To further simplify operation, powerful new distSQL capability allows users to dynamically create a sharded or encrypted table online with a single SQL.

To satisfy more complex scenarios, ShardingSphere also provides the strategy interfaces for related algorithms allowing users to implement more complicated functionalities for their own business scenarios. The coexistence philosophy of built-in strategies for users’ general needs, and specific scenarios’ corresponding interfaces has always been the architectural design philosophy of ShardingSphere.

### 4. Other Features

#### Performance Improvements: Optimized Metadata Loading Performance

Startup time issues could be encountered when launching ShardingSphere’s previous versions, especially in the occurrence involving thousands of servers - since ShardingSphere helps users manage all database instances and metadata information. 

With this release, significant performance tuning and several architectural tweaks are introduced to specifically address the community's metadata loading issues. Differently from the original JDBC driver loading mode, the parallel SQL query mode for different database dialects takes out all metadata information at a single time, thus greatly improving startup performance.

#### Usability: New Performance Testing System

In the process of constantly improving and developing new functions, ShardingSphere had previously lacked a complete and comprehensive integration & performance testing system, which can ensure that every commit be compiled normally without affecting other modules and can observe upward and downward performance trends. 

With this release, integration tests have also been included to ensure data sharding, data encryption, Readwrite-splitting, distributed management and control, access control, SQL support and other functions. The system provides basic guarantees for monitoring and tuning performance across databases, different sharding or encryption strategies, and different versions.
With this release, relevant performance test reports and dashboard will also be developed for the community, allowing users to observe ShardingSphere’s performance changes. The entire test system source code will be made available to the community to facilitate user test deployment.

In addition to above mentioned features, for a comprehensive list of enhancements, performance optimizations, bug fixes etc. please refer to the list below: 

### Other New Features:

1. New DistSQL for loading and presenting ShardingSphere’s configuration.
2. Support for join queries and sub-queries across different databased instances.
3. Data gateway is added to support heterogeneous databases.
4. Support create and modify user permission online or dynamically.
5. New automated probes module.

### API Changes:

1. API in read and write splitting module configuration changed to read-write-splitting.
2. API for ShardingProxy user permission configuration changed to Authority.
3. Using dataSourceClassName to optimize the dataSource configuration of ShardingJDBC.
4. Automated ShardingTable configuration strategy, provide standard built-in shard table.
5. Removed ShardingProxy acceptor-size configuration option.
6. Added built-in shard algorithm SPI so users can set up the shard algorithm through class name like in the version 4.x.

### Enhancements:

1. Startup metadata loading performance has been significantly improved.
2. Greatly enhanced the parsing abilities for Oracle/SQLServer/PostgreSQL database.
3. Supporting initialization of the user permission MySQL/PostgreSQL/SQLServer/Oracle.
4. Supporting DDL language for data encryption.
5. When sharding and encryption are used together, SQL is supported for modifying the table named owner.
6. Using SELECT* to rewrite SQL, overwrite columns to add escape characters to avoid column conflicts with keywords.
7. Supporting PostgreSQL JSON/JSONB/ for pattern matching operator parsing.
8. Supporting MySQL/PostgreSQL CREATE/ALTER/DROP TABLESPACE.
9. Supporting PostgreSQL PREPARE, EXECUTE, DEALLOCATE.
10. Supporting PostgreSQL EXPLAIN.
11. Supporting PostgreSQL START/END TRANSACTION.
12. Supporting PostgreSQL ALTER/DROP INDEX.
13. Supporting PostgreSQL dialect CREATE TABLESPACE.
14. Supporting MySQL CREATE LOADABLE FUNCTION.
15. Supporting MySQL/PostgreSQL ALTER TABLE RENAME.
16. Supporting PostgreSQL protocol Close command.

### Changes:

1. New registry storage structure.
2. Removed support for NACOS and Apollo's Configuration Centre.
3. ShardingScaling introduces ElasticJob to handle migration tasks.
4. Refactoring the storage and online update of the kernel metadata information.

### Bug fixes:

1. Fixed issue where SELECT * wildcard SQL could not be used for read/write separation.
2. The custom sharding algorithm did not match the configuration type and the class instance did not meet expectations issue is fixed.
3. Fixed the NoSuchTableException when executing DROP TABLE IF EXISTS.
4. Fixed UPDATE ... SET ... rewrite error.
5. Fixed CREATE/ALTER TABLE statement using foreign key to reference TABLE overwrite error.
6. Fixed the issue when querying subqueries in the temporal table field verification exception.
7. Fixed Oracle/SQL92 SELECT ... WHERE ... LIKE class cast exception.
8. Fixed MySQL SELECT EXISTS ... FROM ... parsing exception.
9. Fixed SHOW INDEX result exception.
10. Fixed the rewrite and merge result exception for SELECT... GROUP BY ...
11. Fixed the encryption and decryption error for CREATE TABLE rewrite.
12. Fixed issue with PostgreSQL Proxy reading text parameter values incorrectly.
13. Fixed PostgreSQL Proxy support for array objects.
14. Fixed ShardingProxy Datatype conversion issues.
15. PostgreSQL Proxy supports the use of the Numeric type.
16. Fixed the issue with incorrect Tag for PostgreSQL Proxy transactions related to Command Complete.
17. Fixed the issue that might return packets that were not expected by the client.

**Download Link:** <https://shardingsphere.apache.org/document/current/en/downloads/>

**Update Logs:** <https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md>

**Project Address:** <https://shardingsphere.apache.org/>

**Mailing List:** <https://shardingsphere.apache.org/community/en/contribute/subscribe/>

#### Community Contribution:

The release of Apache ShardingSphere 5.0.0-beta could not have happened without the outstanding support and contribution of the community. 

Since the 5.0.0-alpha release until now, 41 contributors have contributed 1574 PR, enhanced the optimization, iteration, and the release of ShardingSphere 5.0.0. 

#### About the author:

![](https://shardingsphere.apache.org/blog/img/ss_5.0.0beta1.jpg)

> **Pan Juan | Trista**
>
>SphereEx co-founder, Apache member, Apache ShardingSphere PMC, Apache brpc (Incubating) mentor, Release manager. 
>
>Senior DBA at JD Technology, she was responsible for the design and development of JD Digital Science and Technology's intelligent database platform. She now focuses on distributed database & middleware ecosystem, and the open-source community. 
Recipient of the "2020 China Open-Source Pioneer" award, she is frequently invited to speak and share her insights at relevant conferences in the fields of database & database architecture.
>
>**GitHub:** <https://tristazero.github.io>


