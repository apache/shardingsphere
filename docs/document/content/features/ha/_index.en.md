+++
pre = "<b>3.4. </b>"
title = "HA"
weight = 4
chapter = true
+++

## Definition

High availability is the most basic requirement for modern systems. It is also an essential element of the database, which in turn is the cornerstone of any system.
In a distributed database system, storage nodes and compute nodes are different in terms of their high availability schemes.
Stateful storage nodes are required to have capabilities such as data consistency and synchronization, liveness probe, and primary-node election.
Stateless compute nodes need to sense storage nodes' changes, setup load balancers independently, and enable service discovery and request distribution.
Apache ShardingSphere' high availability module (HA) is mainly designed to ensure a 24/7 database service as much as possible.

## How it works

The high availability solution provided by Apache ShardingSphere allows you to carry out secondary custom development and achieve expansion, 
which is mainly divided into four steps: pre-check, primary database dynamic discovery, secondary database dynamic discovery and configuration synchronization.

![Overview](https://shardingsphere.apache.org/document/current/img/discovery/overview.en.png)

## Related References

[Java API](/en/user-manual/shardingsphere-jdbc/java-api/rules/ha)\
[YAML Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/ha)\
[Spring Boot Starter](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha)\
[Spring Namespace](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha)
