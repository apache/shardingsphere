+++
title = "XA两阶段事务"
weight = 2
+++

`XAShardingTransactionManager` 为Apache ShardingSphere 的分布式事务的XA实现类。
它主要负责对多数据源进行管理和适配，并且将相应事务的开启、提交和回滚操作委托给具体的 XA 事务管理器。

![XA事务实现原理](https://shardingsphere.apache.org/document/current/img/transaction/2pc-xa-transaction-design.png)

## 开启全局事务

收到接入端的 `set autoCommit=0` 时，`XAShardingTransactionManager` 将调用具体的 XA 事务管理器开启 XA 全局事务，以 XID 的形式进行标记。

## 执行真实分片SQL

`XAShardingTransactionManager`将数据库连接所对应的 XAResource 注册到当前 XA 事务中之后，事务管理器会在此阶段发送 `XAResource.start` 命令至数据库。
数据库在收到 `XAResource.end` 命令之前的所有 SQL 操作，会被标记为 XA 事务。

例如:

```
XAResource1.start             ## Enlist阶段执行
statement.execute("sql1");    ## 模拟执行一个分片SQL1
statement.execute("sql2");    ## 模拟执行一个分片SQL2
XAResource1.end               ## 提交阶段执行
```

示例中的 `sql1` 和 `sql2` 将会被标记为 XA 事务。

## 提交或回滚事务

`XAShardingTransactionManager` 在接收到接入端的提交命令后，会委托实际的 XA 事务管理进行提交动作，
事务管理器将收集到的当前线程中所有注册的 XAResource，并发送 `XAResource.end` 指令，用以标记此 XA 事务边界。
接着会依次发送 `prepare` 指令，收集所有参与 XAResource 投票。
若所有 XAResource 的反馈结果均为正确，则调用 `commit` 指令进行最终提交；
若有任意 XAResource 的反馈结果不正确，则调用 `rollback` 指令进行回滚。
在事务管理器发出提交指令后，任何 XAResource 产生的异常都会通过恢复日志进行重试，以保证提交阶段的操作原子性，和数据强一致性。

例如:

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
