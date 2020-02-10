+++
pre = "<b>3.4.3.3 </b>"
toc = true
title = "Seata BASE transaction"
weight = 4
+++

## Principle

When integrating `Seata AT` transaction, we need to integrate TM, RM and TC component into ShardingTransactionManager. Seata have proxied DataSource in order to communication with TC throng RPC 
protocal.  Similarly, we can wrap user configured datasource into seata DataSource proxy to make sure distribute transaction after sharding.

![Seata BASE transaction](https://shardingsphere.apache.org/document/current/img/transaction/sharding-transaciton-base-seata-at-design.png)

### 1.Init(Init Seata component)

When an application containing `ShardingTransactionBaseSeataAT` startup, the user-configured DataSource will be wrapped into seata `DataSourceProxy` through `seata.conf`, then registered into RM.

### 2.Begin(begin global transaction)

TM controls the boundaries of global transactions. TM obtains the global transaction ID by sending Begin instructions to TC. All branch transactions participate in the global transaction through 
this global transaction ID. The context of the global transaction ID will be stored in the thread local variable.

### 3.Execute sharding physical SQLs

Physical SQL in Seata global transaction will be intercepted to generate undo snapshots by RM and sends participate instructions to TC to join global transaction. 
since sharding physical SQLs executed in multi-threads, global transaction context should transfer from main thread to child thread, which is exactly the same as context transfer between services.

### 4.Commit/rollback(submit seata transaction）

When submitting a seata transaction, TM sends TC the commit and rollback instructions of the global transaction. TC coordinates all branch transactions for commit and rollback according to the global transaction ID.