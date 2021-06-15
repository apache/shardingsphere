+++
title = "Seata BASE transaction"
weight = 3
+++

When integrating Seata AT transaction, we need to integrate TM, RM and TC component into Apache Sharding transaction manager. 
Seata have proxied `DataSource` interface in order to RPC with TC. 
Similarly, Apache ShardingSphere faced to `DataSource` interface to aggregate data sources too. 
After Seata `DataSource` encapsulation, it is easy to put Seata AT transaction in to Apache ShardingSphere sharding ecosystem.


![Seata BASE transaction](https://shardingsphere.apache.org/document/current/img/transaction/sharding-transaciton-base-seata-at-design.png)

## Init Seata Engine

When an application containing `ShardingTransactionBaseSeataAT` startup, the user-configured DataSource will be wrapped into seata `DataSourceProxy` through `seata.conf`, then registered into RM.

## Transaction Begin

TM controls the boundaries of global transactions. TM obtains the global transaction ID by sending Begin instructions to TC. 
All branch transactions participate in the global transaction through this global transaction ID. 
The context of the global transaction ID will be stored in the thread local variable.

## Execute actual sharding SQL

Actual SQL in Seata global transaction will be intercepted to generate undo snapshots by RM and sends participate instructions to TC to join global transaction. 
Since actual sharding SQLs executed in multi-threads, global transaction context should transfer from main thread to child thread, which is exactly the same as context transfer between services.

## Commit or Rollback

When submitting a seata transaction, TM sends TC the commit and rollback instructions of the global transaction. TC coordinates all branch transactions for commit and rollback according to the global transaction ID.
