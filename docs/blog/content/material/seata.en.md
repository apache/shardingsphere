+++
title = "Seata_AT"
weight = 7
chapter = true
+++

## Apache ShardingSphere merged Seata AT distributed transactions

### Background

Seata is a distributed transaction framework that jointly created by Ali group and Ant Financial services group. The current version of seata includes AT and TCC transaction. The goal of AT transaction is to provide incremental transaction ACID semantics under the micro service architecture, which let the users use distributed transactions just like local transactions. The core concept is the same as ShardingSphere. 

GitHub: https://github.com/seata/seata

### Seata AT model

The Seata AT transaction model includes TM (Transaction manager), RM (Resource manager) and TC (Transaction coordinator).

TC is an independent service and needs to be deployed separately, but TM and RM are deployed together with business applications in the form of a jar package, they establish a long-term connection with the TC and maintain RPC communication in the whole transaction life cycle.

Among this model, the initiator of the global transaction is TM and it is responsible for the “begin” and commit/rollback of global transactions, the participant of the global transaction is RM and it is responsible for the execution and commit/rollback of branch transactions. 

![](https://shardingsphere.apache.org/blog/img/seata1.jpg)

### SPI the distributed transaction of ShardingSphere 

ShardingSphere provides a set of SPIs for accessing distributed transactions. The goal of this design is to ensure the ACID semantics of transactions after data fragmentation. The realization of distributed transactions mainly includes 2PC XA and BASE transaction. As an implementation of BASE transaction, Seata AT transaction can be seamlessly connected to the ShardingSphere ecosystem.

![](https://shardingsphere.apache.org/blog/img/seata2.jpg)

For 2PC XA transaction, we have integrated Atomikos, Narayana, Bitronix transaction mangers. The bottom layer of XA transaction depends on the support of specific database vendors for the 2PC XA commit protocol, usually XA protocol doing 2PL (2 phase Lock) throughout Prepare and Commit which guarantees the ACID of distributed transactions. This usually suitable for short transaction and non-cloud environments (cloud environment takes around 20ms to do next IO, 2PL will lock resources up to 40ms, so the TPS of transaction will goes down to 25/s, non-cloud environment usually only takes a few milliseconds per IO, therefore the time to lock data is relatively low) \[1\].

In terms of BASE transactions, we have now completed the integration of ServiceComb Saga. Saga has improved the performance of the overall transaction through one-phase commit plus compensation. The compensation method is roughly the same as Seata, that is, the physical SQL after fragmentation Revert is used to generate compensated SQL, but the Saga model does not support isolation levels in theory, and is suitable for businesses with high performance requirements and low consistency requirements. On the basis of one-phase commit plus compensation, Seata AT transaction supports the RC isolation level through the global lock of TC, which is another implementation between XA and Saga. In terms of flexible news affairs, you are also welcome to refer to our SPI to provide integrated solutions.

### Integrated solution

When integrating Seata AT transactions, we need to integrate TM, RM and TC models into the ShardingSphere distributed transaction SPI ecosystem. For database resource, Seata allows JDBC operations to communicate with TC through RPC by docking with DataSource interface. Similarly, ShardingSphere also integrate physical DataSource that configured by the user into DataSource interface. Therefore, after the physical DataSource is repacked as Seata’s DataSource, Seata AT transactions can be integrated into ShardingSphere’s Shards. 

In the model of Seata, the context of global transaction is stored in thread variable, the transfer between thread variables can be completed by extending the transport between services, branch transaction determines whether or not join in entire Seata global transaction by thread variable. The sharding execution engine of ShardingSphere is usually executed in multiple threads. Therefore, when integrating Seata AT transactions, it is necessary to extend the transaction context transfer of the main thread and the sub-threads, which is exactly the same as the context transfer idea between services.

![](https://shardingsphere.apache.org/blog/img/seata3.jpg)

### Quick start

We have implemented base-seata-raw-jdbc-example, you can have a try on your own.
https://github.com/apache/incubator-shardingsphere-example/tree/dev/sharding-jdbc-example/transaction-example/transaction-base-seata-example/transaction-base-seata-raw-jdbc-example

Operation Manual

1.Follow the steps in seata-work-shop, download and start seata server.

https://github.com/seata/seata-workshop

This refers to step6 and step7.

2.Execute the script resources/sql/undo_log.sql in each database sharding, and create undo_log table.

3.Run YamlConfigurationTransactionExample.java

### Items to be optimized

When the Seata AT transaction is Revert SQL, we needs to parsed the physical SQL twice after the ShardingSphere fragmentation, so we needs to design an SPI to avoid the loss of the performance in the second SQL parsing.

Reference pape

[1]: Transactions for Distributed Actors in the Cloud

https://www.microsoft.com/en-us/research/wp-content/uploads/2016/10/EldeebBernstein-TransactionalActors-MSR-TR-1.pdf


