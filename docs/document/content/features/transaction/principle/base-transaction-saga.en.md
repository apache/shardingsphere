+++
pre = "<b>3.4.3.2 </b>"
toc = true
title = "BASE Transaction Saga"
weight = 3
+++

## Principle

The implementation class of Saga is `SagaShardingTransactionManager`. ShardingSphere intercepts the parsing and routing results of logical SQL by Hook. In this way, reverse SQL can be generated 
before physical SQL is executed, and then the SQL call chain is handed over to Saga engine at the transaction submission stage.

![Saga BASE transaction](https://shardingsphere.apache.org/document/current/img/transaction/sharding-transaction-base-saga-design.png)

### 1.Init(Init Saga component)

When an application containing SagaShardingTransactionManager startup, saga-actuator engined will be initialized through `saga.properties` configuration.

### 2.Begin(Begin Saga global transaction)

Every time a saga global transaction is created, the context of this global transaction (`SagaTransactionContext`) is generated. The transaction context records the forward and reverse SQL of all 
sub-transactions, which is used as metadata to generate SQL call graph processed by saga engine.

### 3.Execute sharding physical SQLs

Before physical SQL is executed, ShardingSphere generates reverse SQL according to the type of SQL, which is implemented by intercepting parsing results by Hook.

### 4.Commit/rollback(submitting saga transaction)

The commit phase generates the SQL call graph required by the saga engine, the commit operation generates the Forward Recovery (forward SQL compensation) task, and the rollback operation generates the Backward Recovery task (reverse SQL compensation).
