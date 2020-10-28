+++
pre = "<b>3.3. </b>"
title = "Primary-Replica Replication"
weight = 3
chapter = true
+++

## Background

With increasing system TPS, database capacity has faced great bottleneck effect. For the application system with massive concurrence read operations but less write operations in the same time, we can divide the database into a primary database and a replica database. The primary database is responsible for the addition, deletion and modification of transactions, while the replica database is responsible for queries. It can significantly improve the query performance of the whole system by effectively avoiding line locks caused by data renewal.

One primary database with multiple replica databases can further enhance system processing capacity by distributing queries evenly into multiple data replicas. Multiple primary databases with multiple replica databases can enhance not only system throughput but also system availability. Therefore, the system can still function normally, even though any database is down or physical disk is destroyed.

Different from the horizontal sharding that separates data to all nodes according to sharding keys, primary-replica replication routes read operations and write operations separately to the primary database and the replica database according to SQL meaning analysis.

![Primary-replica replication](https://shardingsphere.apache.org/document/current/img/read-write-split/read-write-split.png)

Data in primary-replica replication split nodes is consistent, whereas that in horizontal shards is not. The combined use of horizontal sharding and primary-replica replication will effectively enhance the system performance.

## Challenges

Though primary-replica replication can enhance system throughput and availability, it also brings inconsistent data, including that between multiple primary databases and between primary databases and replica databases. What's more, it also brings the same problem as data sharding, complicating app developer and operator's maintenance and operation. The following picture has shown the complex topological relations between applications and database groups when sharding table and database used together with primary-replica replication.

![Sharding + Primary-Replica Replication](https://shardingsphere.apache.org/document/current/img/read-write-split/sharding-read-write-split.png)

## Goal

**The main design goal of the primary-replica replication modular of ShardingSphere is to try to reduce the influence of primary-replica replication, in order to let users use primary-replica replication database group like one database.**