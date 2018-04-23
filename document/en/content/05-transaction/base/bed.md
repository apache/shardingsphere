+++
toc = true
title = "Best-Effort-Delivery"
weight = 1
+++

## Concept

For the distributed databases, we believe the operations for all the databases will succeed eventually, so the system will keep on trying to send the operations to its corresponding database.

![The Best-Effort-Delivery transaction](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture-soft-transaction-bed.png)

## Usage scenario

* To delete records by the primary key.
* Permanently update the record's status, e.g. to update notification service status.

## Usage standard

It is necessary to satisfy the idempotent requirement when the B.A.S.E transaction is used.

* The INSERT statement must contain the column of primary key which can not be auto_increment.
* The UPDATE statement must satisfy the idempotent requirement, and UPDATE xxx SET x=x+1 is not supported.
* All the DELETE statements are supported。

## Develop guide

* For the sharding-jdbc-transaction is developed by using JAVA and can be directly used in the form of jar package, so that you can use the Maven to import coordinates to use it.
* In order to ensure that the transactions are not lost, sharding-jdbc-transaction needs to store the log of all the transactions, which can be configured in the transaction manager configurations.
* You need to deploy discrete jobs and Zookeeper because of the asynchronous way of the B.A.S.E transactions. To simply those operations, we develop sharding-jdbc-transaction-async-job for sharding-jdbc-transaction, so you can create the high-available jobs to asynchronously deliver the B.A.S.E transactions. The startup script is start.sh.
* To help users develop easily, sharding-jdb-transaction provides memory-based transaction log storage and embedded asynchronous jobs.

## Independent job deploy guide

* Create database to store transactions logs.
* Deploy Zookeeper for asynchronous jobs.
* Configure YAML.
* Download and extract sharding-jdbc-transaction-async-job-$VERSION.tar, and start asynchronous jobs by running start.sh.
 