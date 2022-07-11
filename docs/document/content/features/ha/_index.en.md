+++
pre = "<b>4.7. </b>"
title = "HA"
weight = 7
chapter = true
+++

## Definition

High availability is the most basic requirement for modern systems. It is also an essential element of the database, which in turn is the cornerstone of any system.
In a distributed database system, storage nodes and compute nodes are different in terms of their high availability schemes.
Stateful storage nodes are required to have capabilities such as data consistency and synchronization, liveness probe, and primary-node election.
Stateless compute nodes need to sense storage nodes' changes, setup load balancers independently, and enable service discovery and request distribution.
Apache ShardingSphere' high availability module (HA) is mainly designed to ensure a 24/7 database service as much as possible.

## Related Concepts

### High Availability Type

Apache ShardingSphere does not provide database high availability capability. It senses the change of databases' primary-secondary relationship through a third-party provided high availability solution.
Specifically, ShardingSphere is capable of finding databases, automatically sensing the primary/secondary database relationship, and correcting compute nodes' connections to databases.

### Dynamic Read/Write Splitting

When high availability and read/write splitting are adopted together, it is not necessary to configure specific primary and secondary databases for read/write splitting. 
Highly available data sources dynamically correct the primary/secondary relationship of read/write splitting and properly channel read/write traffic.

## Limitations

### Supported
* MySQL MGR single-primary mode
* MySQL Primary/secondary replication mode
* openGauss Primary/secondary replication mode

### Not supported
* MySQL MGR Multi-primary mode

## How it works

The high availability solution provided by Apache ShardingSphere allows you to carry out secondary custom development and achieve expansion, 
which is mainly divided into four steps: pre-check, primary database dynamic discovery, secondary database dynamic discovery and configuration synchronization.

![Overview](https://shardingsphere.apache.org/document/current/img/discovery/overview.en.png)

## Related References
[Java API](/en/user-manual/shardingsphere-jdbc/java-api/rules/ha)\
[YAML Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/ha)\
[Spring Boot Starter](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha)\
[Spring Namespace](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha)

[Source Code](https://github.com/apache/shardingsphere/tree/master/shardingsphere-features/shardingsphere-db-discovery)



