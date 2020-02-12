+++
pre = "<b>3.4.3.3 </b>"
toc = true
title = "Seata柔性事务"
weight = 4
+++

## 实现原理

整合`Seata AT`事务时，需要把TM，RM，TC的模型融入到ShardingSphere 分布式事务的SPI的生态中。在数据库资源上，Seata通过对接DataSource接口，让JDBC操作可以同TC进行RPC通信。同样，ShardingSphere也是面向DataSource接口对用户配置的物理DataSource进行了聚合，因此把物理DataSource二次包装为Seata
的DataSource后，就可以把Seata AT事务融入到ShardingSphere的分片中。

![柔性事务Seata](https://shardingsphere.apache.org/document/current/img/transaction/sharding-transaciton-base-seata-at-design.png)

### 1.Init（Seata引擎初始化）

包含Seata柔性事务的应用启动时，用户配置的数据源会按`seata.conf`的配置，适配为Seata事务所需的`DataSourceProxy`，并且注册到RM中。

### 2.Begin（开启Seata全局事务）

TM控制全局事务的边界，TM通过向TC发送Begin指令，获取全局事务ID，所有分支事务通过此全局事务ID，参与到全局事务中；全局事务ID的上下文存放在当前线程变量中。

### 3.执行分片物理SQL

处于Seata全局事务中的分片SQL通过RM生成undo快照，并且发送participate指令到TC，加入到全局事务中。ShardingSphere的分片物理SQL是按多线程方式执行，因此整合Seata AT事务时，需要在主线程和子线程间进行全局事务ID的上下文传递，这同服务间的上下文传递思路完全相同。

### 4.Commit/rollback（提交Seata事务）

提交Seata事务时，TM会向TC发送全局事务的commit和rollback指令，TC根据全局事务ID协调所有分支事务进行commit和rollback。