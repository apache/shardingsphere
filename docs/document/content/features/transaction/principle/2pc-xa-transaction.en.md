+++
title = "XA Transaction"
weight = 2
+++

`XAShardingTransactionManager` is XA transaction manager of Apache ShardingSphere.
Its main responsibly is manage and adapt multiple data sources, and sent corresponding transactions to concrete XA transaction manager.

![Principle of sharding transaction XA](https://shardingsphere.apache.org/document/current/img/transaction/2pc-xa-transaction-design.png)

## Transaction Begin

When receiving `set autoCommit=0` from client, `XAShardingTransactionManager` will use XA transaction managers to start overall XA transactions, which is marked by XID.

## Execute actual sharding SQL


After `XAShardingTransactionManager` register the corresponding XAResource to the current XA transaction, transaction manager will send `XAResource.start` command to databases.
After databases received `XAResource.end` command, all SQL operator will mark as XA transaction. 

For example:

```
XAResource1.start             ## execute in the enlist phase
statement.execute("sql1");
statement.execute("sql2");
XAResource1.end               ## execute in the commit phase
```

`sql1` and `sql2` in example will be marked as XA transaction.

## Commit or Rollback

After `XAShardingTransactionManager` receives the commit command in the access, it will delegate it to the actual XA manager. 
It will collect all the registered XAResource in the thread, before sending `XAResource.end` to mark the boundary for the XA transaction. 
Then it will send prepare command one by one to collect votes from XAResource. 
If all the XAResource feedback is OK, it will send commit command to finally finish it; 
If there is any No XAResource feedback, it will send rollback command to roll back. 
After sending the commit command, all XAResource exceptions will be submitted again according to the recovery log to ensure the atomicity and high consistency.

For example:

```
XAResource1.prepare           ## ack: yes
XAResource2.prepare           ## ack: yes
XAResource1.commit
XAResource2.commit

XAResource1.prepare           ## ack: yes
XAResource2.prepare           ## ack: no
XAResource1.rollback
XAResource2.rollback
```
