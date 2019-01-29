+++
pre = "<b>3.2. </b>"
title = "Read-write splitting"
weight = 2
chapter = true
+++

## Background

With increasing system TPS, database capacity has faced great bottleneck effect. 
For the application system with massive concurrence read operations but less write operations in the same time, we can divide the database into a master database and a slave database. 
The master database is responsible for the addition, deletion and modification operations of transactions, while the slave database is responsible for query operations. 
It can significantly improve the query performance of the whole system by effectively avoiding line locks caused by data renewal.

The configuration of one master database with multiple slave databases can further enhance system processing capacity by distributing queries evenly into multiple data replicas. 
The configuration of multiple master databases with multiple slave databases can not only enhance system throughput but also the system availability. 
As a result, under the circumstance that any database goes down, or even the disk is physically destroyed, the normal function of the system will still not be affected.

Different from the horizontal sharding that separates the data to all data nodes according to sharding keys, read-write split routes read operations and write operations separately to the master database and slave database according to SQL meaning analysis.

[![Read-write split](https://shardingsphere.apache.org/document/current/img/read-write-split/read-write-split.png)

Data in read-write split nodes is consistent, whereas that in horizontal shards is not. 
The combined use of horizontal sharding and read-write split will effectively enhance the system performance.

## Challenges

Though enhancing system throughput and availability, read-write split also brings the problem of inconsistent data, including that between multiple master databases and between master databases and slave databases. 
What's more, it also brings the problem similar as data sharding, making application development and operation staff's maintenance work more complex. 
The following picture has shown the complex topological relations between application and database group when sharding table and database are used together with read-write split.

[![Sharding + Read-write split](https://shardingsphere.apache.org/document/current/img/read-write-split/sharding-read-write-split.png)

## Goal

**The main design goal of the read-write split modular of ShardingSphere is to try to reduce the influence of read-write split, in order to let users use master-slave database group like one database.**
