+++
title = "Seata 柔性事务"
weight = 3
+++

整合 Seata AT 事务时，需要将 TM，RM 和 TC 的模型融入 Apache ShardingSphere 的分布式事务生态中。
在数据库资源上，Seata 通过对接 `DataSource` 接口，让 JDBC 操作可以同 TC 进行远程通信。
同样，Apache ShardingSphere 也是面向 `DataSource` 接口，对用户配置的数据源进行聚合。
因此，将 `DataSource` 封装为 基于Seata 的 `DataSource` 后，就可以将 Seata AT 事务融入到 Apache ShardingSphere的分片生态中。

![柔性事务Seata](https://shardingsphere.apache.org/document/current/img/transaction/sharding-transaciton-base-seata-at-design.png)

## 引擎初始化

包含 Seata 柔性事务的应用启动时，用户配置的数据源会根据 `seata.conf` 的配置，适配为 Seata 事务所需的 `DataSourceProxy`，并且注册至 RM 中。

## 开启全局事务

TM 控制全局事务的边界，TM 通过向 TC 发送 Begin 指令，获取全局事务 ID，所有分支事务通过此全局事务 ID，参与到全局事务中；全局事务 ID 的上下文存放在当前线程变量中。

## 执行真实分片SQL

处于 Seata 全局事务中的分片 SQL 通过 RM 生成 undo 快照，并且发送 `participate` 指令至 TC，加入到全局事务中。
由于 Apache ShardingSphere 的分片物理 SQL 采取多线程方式执行，因此整合 Seata AT 事务时，需要在主线程和子线程间进行全局事务 ID 的上下文传递。

## 提交或回滚事务

提交 Seata 事务时，TM 会向 TC 发送全局事务的提交或回滚指令，TC 根据全局事务 ID 协调所有分支事务进行提交或回滚。
