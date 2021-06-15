+++
title = "Seata BASE transaction"
weight = 3
+++

[Seata](https://github.com/seata/seata) is a distributed transaction framework developed by Alibaba Group and Ant Finance. 
The goal of AT transaction is to provide incremental transaction ACID semantics under the micro-service architecture, 
so that developers can use distributed transactions as they use local transactions. 
The core idea of AT transaction is the same as Apache ShardingSphere.

Seata AT transaction model includes TM (Transaction Manager), RM (Resource Manager) and TC (Transaction Coordinator). 
TC is an independent service that needs to be deployed separately. 
TM and RM are deployed together with user applications in the form of jar packages. 
They establish long connections with TC and keep RPC throughout the transaction life cycle.
The initiator of global transaction is TM, which is in charge of begin and commit/rollback of global transaction.
The participant of global transaction is RM, which is in charge of reporting the execution results of branch transaction, and commit/rollback is executed through TC coordination.

A typical lifecycle of Seata managed distributed transaction:

1. TM asks TC to begin a new global transaction. TC generates a XID representing the global transaction.
2. XID is propagated through micro-services' invoke chain.
3. RM register local transaction as a branch of the corresponding global transaction of XID to TC.
4. TM asks TC for commit or rollback the corresponding global transaction of XID.
5. TC drives all branch transactions under the corresponding global transaction of XID to finish branch commit or rollback.

![Seata AT transaction model](https://shardingsphere.apache.org/document/current/img/transaction/seata-at-transaction.png)
