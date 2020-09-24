+++
pre = "<b>3.3. </b>"
title = "Read-write splitting"
weight = 3
chapter = true
+++

## Background

With increasing system TPS, database capacity has faced great bottleneck effect. For the application system with massive concurrence read operations but less write operations in the same time, we can divide the database into a master database and a slave database. The master database is responsible for the addition, deletion and modification of transactions, while the slave database is responsible for queries. It can significantly improve the query performance of the whole system by effectively avoiding line locks caused by data renewal.

One master database with multiple slave databases can further enhance system processing capacity by distributing queries evenly into multiple data replicas. Multiple master databases with multiple slave databases can enhance not only system throughput but also system availability. Therefore, the system can still function normally, even though any database is down or physical disk is destroyed.

Different from the horizontal sharding that separates data to all nodes according to sharding keys, read-write split routes read operations and write operations separately to the master database and the slave database according to SQL meaning analysis.

![Read-write split](https://shardingsphere.apache.org/document/current/img/read-write-split/read-write-split.png)

Data in read-write split nodes is consistent, whereas that in horizontal shards is not. The combined use of horizontal sharding and read-write split will effectively enhance the system performance.

## Challenges

Though read-write split can enhance system throughput and availability, it also brings inconsistent data, including that between multiple master databases and between master databases and slave databases. What's more, it also brings the same problem as data sharding, complicating app developer and operator's maintenance and operation. The following picture has shown the complex topological relations between applications and database groups when sharding table and database are used together with read-write split.

![Sharding + Read-write split](https://shardingsphere.apache.org/document/current/img/read-write-split/sharding-read-write-split.png)

## Goal

**The main design goal of the read-write split modular of ShardingSphere is to try to reduce the influence of read-write split, in order to let users use primary-replica replication database group like one database.**