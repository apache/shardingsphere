+++
pre = "<b>3.3. </b>"
title = "Readwrite-splitting"
weight = 3
chapter = true
+++

## Background

Database throughput has faced the bottleneck with increasing TPS. For the application with massive concurrence read but less write in the same time, we can divide the database into a primary database and a replica database. The primary database is responsible for the insert, delete and update of transactions, while the replica database is responsible for queries. It can significantly improve the query performance of the whole system by effectively avoiding row locks.

One primary database with multiple replica databases can further enhance processing capacity by distributing queries evenly into multiple data replicas. Multiple primary databases with multiple replica databases can enhance not only throughput but also availability. Therefore, the system can still run normally, even though any database is down or physical disk destroyed.

Different from the sharding that separates data to all nodes according to sharding keys, readwrite-splitting routes read and write separately to primary database and replica databases according SQL analysis.

![background](https://shardingsphere.apache.org/document/current/img/readwrite-splitting/background.png)

Data in readwrite-splitting nodes are consistent, whereas that in shards is not. The combined use of sharding and readwrite-splitting will effectively enhance the system performance.

## Challenges

Though readwrite-splitting can enhance system throughput and availability, it also brings inconsistent data, including that among multiple primary databases and among primary databases and replica databases. What’s more, it also brings the same problem as data sharding, complicating developer and operator’s maintenance and operation. The following diagram has shown the complex topological relations between applications and database groups when sharding used together with readwrite-splitting.

![challenges](https://shardingsphere.apache.org/document/current/img/readwrite-splitting/challenges.png)

## Goal

The main design goal of readwrite-splitting of Apache ShardingSphere is to try to reduce the influence of readwrite-splitting, in order to let users use primary-replica database group like one database.

## Application Scenarios

### Complex primary-secondary database architecture

Many systems rely on the configuration of primary-secondary database architecture to improve the throughput of the whole system. Nevertheless, this configuration can make it more complex to use services. 

After accessing ShardingSphere, the read/write splitting feature can be used to manage primary-secondary databases and achieve transparent read/write splitting, enabling users to use databases with primary/secondary architecture just like using one single database.

## Related References

[Java API](/en/user-manual/shardingsphere-jdbc/java-api/rules/readwrite-splitting)\
[YAML Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting)\
[Spring Boot Starter](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/readwrite-splitting)\
[Spring Namespace](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/readwrite-splitting)
