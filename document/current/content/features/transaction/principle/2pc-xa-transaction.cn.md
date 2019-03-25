+++
pre = "<b>3.4.3.1 </b>"
toc = true
title = "两阶段事务-XA"
weight = 2
+++

## 实现原理
![XA事务实现原理](https://shardingsphere.apache.org/document/current/img/transactoin/2pc-xa-transaction-design_cn.png)

ShardingSphere里定义了分布式事务的SPI接口`ShardingTransactionManager`，`Sharding-JDBC`和`Sharding-Proxy`为分布式事务的两个接入端。`XAShardingTransactionManager`为分布式事务的XA实现类，通过引入`sharding-transaction-xa-core`依赖，即可加入ShardingSphere的分布式事务生态中。`XAShardingTransactionManager`主要负责对`actual datasource`进行管理和适配，并且将接入端事务的`begin/commit/rollback`操作委托给具体的XA事务管理器。

### 处理流程

#### 1. begin

通常收到接入端的`set autoCommit=0`时，`XAShardingTransactionManager`会调用具体的XA事务管理器开启XA的全局事务，通常以XID的形式进行标记。

#### 2. SQL执行

ShardingSphere进行解析/优化/路由后，会生成逻辑SQL的分片SQLUnit，执行引擎为每个物理SQL创建连接的同时，物理连接所对应的XAResource也会被注册到当前XA事务中，事务管理器会在此阶段发送`XAResource.start`命令给数据库，数据库在收到`XAResource.end`命令之前的所有SQL操作，会被标记为XA事务。

例如:

```
XAResource1.start             -- enlist阶段执行
statement.execute("sql1");
statement.execute("sql2");
XAResource1.end               -- 提交阶段执行
```

这里sql1和sql2将会被标记为XA事务。

#### 3. commit/rollback

`XAShardingTransactionManager`收到接入端的提交命令后，会委托实际的XA事务管理进行提交动作，这时事务管理器会收集当前线程里所有注册的XAResource，首先发送`XAResource.end`指令，用以标记此XA事务的边界。
接着会依次发送prepare指令，收集所有参与XAResource投票，如果所有XAResource的反馈结果都是OK，则会再次调用commit指令进行最终提交，如果有一个XAResource的反馈结果为No，则会调用rollback指令进行回滚。
在事务管理器发出提交指令后，任何XAResource产生的异常都会通过recovery日志进行重试，来保证提交阶段的操作原子性，和数据强一致性。

例如:

```
XAResource1.prepare     -- ack: yes
XAResource2.prepare     -- ack: yes
XAResource1.commit
XAResource2.commit
     
XAResource1.prepare     -- ack: yes
XAResource2.prepare     -- ack: no
XAResource1.rollback
XAResource2.rollback
```