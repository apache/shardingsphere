+++
pre = "<b>3.4.1.2 </b>"
title = "Seata BASE transaction"
weight = 3
+++

## Seata BASE transaction

[Seata](https://github.com/seata/seata) is a distributed transaction framework developed by Alibaba Group and Ant Finance. As of 0.5.x, it includes AT and TCC transactions. The goal of AT transaction is to provide incremental transaction ACID semantics under the micro-service architecture, so that users can use distributed transactions as they use local transactions. The core idea of AT transaction is the same as ShardingSphere.

## Seata AT transaction model

`Seata AT` transaction model includes TM (transaction manager), RM (resource manager), TC (transaction coordinator). TC is an independent service that needs to be deployed separately. TM and RM are 
deployed together with business applications in the form of jar packages. They establish long connections with TC and keep RPC communication throughout the transaction life cycle.
The initiator of global transaction is TM, and the participant of global transaction is RM; TM is in charge of begin and commit/rollback of global transaction, RM is in charge of reporting the 
execution results of branch transaction, and commit/rollback is executed through TC coordination.

A typical lifecycle of Seata managed distributed transaction:

1. TM asks TC to begin a new global transaction. TC generates an XID representing the global transaction.
2. XID is propagated through microservices' invoke chain.
3. RM register local transaction as a branch of the corresponding global transaction of XID to TC.
4. TM asks TC for committing or rollbacking the corresponding global transaction of XID.
5. TC drives all branch transactions under the corresponding global transaction of XID to finish branch committing or rollbacking.

![Seata AT transaction model](https://shardingsphere.apache.org/document/current/img/transaction/seata-at-transaction.png)