+++
pre = "<b>3.3. </b>"
title = "Readwrite-splitting"
weight = 3
chapter = true
+++

## Definition

Read/write splitting is to split the database into primary and secondary databases. The primary database is responsible for handling transactional operations including additions, deletions and changes.
And the secondary database is responsible for the query operation of database architecture.

## Impact on the System

There may be complex primary-secondary relational database clusters in users' systems, so applications need to access multiple data sources, which increases the cost of system maintenance and the
difficulty of business development. ShardingSphere enables users to use database clusters like a database through read/write splitting function, and the impact of read/write splitting will be transparent to users.

## How it works

ShardingSphere's read/write splitting mainly relies on the related functions of its kernel, including a parsing engine and a routing engine.
The parsing engine converts the user's SQL into Statement information that can be identified by ShardingSphere, and the routing engine performs SQL routing according to the read/write type of SQL and transactional status.
The routing from the secondary database supports a variety of load balancing algorithms, including polling algorithm, random access algorithm, weight access algorithm, etc.
Users can also expand the required algorithm according to the SPI mechanism. As shown in the figure below, ShardingSphere identifies read and write operations and routes them to different database instances respectively.

![implementation](https://shardingsphere.apache.org/document/current/img/readwrite-splitting/background.png)

## Related References

[Java API](/en/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting)\
[YAML Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting)\
[Spring Boot Starter](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/readwrite-splitting)\
[Spring Namespace](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/readwrite-splitting)
