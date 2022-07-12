+++
pre = "<b>4.6. </b>"
title = "Readwrite-splitting"
weight = 6
chapter = true
+++

## Definition

Read/write splitting is to split the database into primary and secondary databases. The primary database is responsible for handling transactional operations including additions, deletions and changes.
And the secondary database is responsible for the query operation of database architecture.

## Related Concepts

### Primary database
The primary database is used to add, update, and delete data operations. Currently, only single primary database is supported.

### Secondary database
The secondary database is used to query data operations and multi-secondary databases are supported.

### Primary-Secondary synchronization
It refers to the operation of asynchronously synchronizing data from a primary database to a secondary database. Due to the asynchronism of primary-secondary synchronization,
data from the primary and secondary databases may be inconsistent for a short time.

### Load balancer policy
Channel query requests to different secondary databases through load balancer policy.

## Impact on the System
There may be complex primary-secondary relational database clusters in users' systems, so applications need to access multiple data sources, which increases the cost of system maintenance and the
difficulty of business development. ShardingSphere enables users to use database clusters like a database through read/write splitting function, and the impact of read/write splitting will be transparent to users.

## Limitations
* Data synchronization of primary and secondary databases is not supported.
* Data inconsistency resulting from data synchronization delays between primary and secondary databases is not supported.
* Multi-write of primary database is not supported.
* Transactional consistency between primary and secondary databases is not supported. In the primary-secondary model, both data reads and writes in transactions use the primary database.

## How it works
ShardingSphere's read/write splitting mainly relies on the related functions of its kernel, including a parsing engine and a routing engine.
The parsing engine converts the user's SQL into Statement information that can be identified by ShardingSphere, and the routing engine performs SQL routing according to the read/write type of SQL and transactional status.
The routing from the secondary database supports a variety of load balancing algorithms, including polling algorithm, random access algorithm, weight access algorithm, etc.
Users can also expand the required algorithm according to the SPI mechanism. As shown in the figure below, ShardingSphere identifies read and write operations and routes them to different database instances respectively.

![原理介绍](https://shardingsphere.apache.org/document/current/img/readwrite-splitting/background.png)

## 相关参考
[Java API](/en/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting)\
[YAML Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting)\
[Spring Boot Starter](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/readwrite-splitting)\
[Spring Namespace](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/readwrite-splitting)
