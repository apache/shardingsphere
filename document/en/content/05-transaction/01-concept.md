+++
toc = true
title = "Core concept"
weight = 1
+++

Considering the performance, sharding-jdbc decides not to support strong consistency distributed transactions. In the future, it will support the B.A.S.E transaction which makes the final result of all the distributed databases consistent. Currently, in addition to supporting weak XA transactions, we have been able to provide the Best-Effort-Delivery transaction, one of the B.A.S.E transaction.

Notices:

* Support the none-cross-database transactions, e.g. table sharding without database sharding, or database sharding with the queries routed in the same database.

* Support the exception handling for the cross-database transactions due to logical exceptions. For example, in the same transaction, you want to update two databases, Sharding-JDBC will rollback all transactions for all two database when a null pointer is thrown after updating.

* Do not support the exception handling for the cross-database transactions due to network or hardware exceptions. For example, in the same transaction, you want to update two databases, Sharding-JDBC will only commit the transaction for second database when the first database is dead after updating.

# The B.A.S.E transaction

## The Best-Effort-Delivery transaction

### The concept

For the distributed databases, we believe the operations for all the databases will succeed eventually, so the system will keep on trying to send the operations to its corresponding database.

### The structure diagram 
![The Best-Effort-Delivery transaction](http://ovfotjrsi.bkt.clouddn.com/docs/img/architecture-soft-transaction-bed.png)

### The usage scenario

* To delete records by the primary key.
* Permanently update the record's status, e.g. to update notification service status.

### The usage limit

It is necessary to satisfy the idempotent requirement when the B.A.S.E transaction is used.

* The INSERT statement must contain the column of primary key which can not be auto_increment.
* The UPDATE statement must satisfy the idempotent requirement, and UPDATE xxx SET x=x+1 is not supported.
* All the DELETE statements are supported。

### The develop guide

* For the sharding-jdbc-transaction is developed by using JAVA and can be directly used in the form of jar package, so that you can use the Maven to import coordinates to use it.
* In order to ensure that the transactions are not lost, sharding-jdbc-transaction needs to store the log of all the transactions, which can be configured in the transaction manager configurations.
* You need to deploy discrete jobs and Zookeeper because of the asynchronous way of the B.A.S.E transactions. To simply those operations, we develop sharding-jdbc-transaction-async-job for sharding-jdbc-transaction, so you can create the high-available jobs to asynchronously deliver the B.A.S.E transactions. The startup script is start.sh.
* To help users develop easily, sharding-jdb-transaction provides memory-based transaction log storage and embedded asynchronous jobs.

### The operations to deploy the discrete jobs

* Create database to store transactions logs.
* Deploy Zookeeper for asynchronous jobs.
* Configure YAML.
* Download and extract sharding-jdbc-transaction-async-job-$VERSION.tar, and start asynchronous jobs by running start.sh.
 
## The TCC transaction

TODO
