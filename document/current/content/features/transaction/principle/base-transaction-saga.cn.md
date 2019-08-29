+++
pre = "<b>3.4.3.2 </b>"
toc = true
title = "Saga柔性事务"
weight = 3
+++

## 实现原理

Saga柔性事务的实现类为`SagaShardingTransactionMananger`, ShardingSphere通过Hook的方式拦截逻辑SQL的解析和路由结果，这样，在分片物理SQL执行前，可以生成逆向SQL，在事务提交阶段再把SQL调用链交给Saga引擎处理。

![柔性事务Saga](https://shardingsphere.apache.org/document/current/img/transaction/sharding-transaction-base-saga-design.png)

### 1.Init（Saga引擎初始化）

包含Saga柔性事务的应用启动时，saga-actuator引擎会根据`saga.properties`的配置进行初始化的流程。

### 2.Begin（开启Saga全局事务）

每次开启Saga全局事务时，将会生成本次全局事务的上下文（`SagaTransactionContext`），事务上下文记录了所有子事务的正向SQL和逆向SQL，作为生成事务调用链的元数据使用。

### 3.执行物理SQL

在物理SQL执行前，ShardingSphere根据SQL的类型生成逆向SQL，这里是通过Hook的方式拦截Parser的解析结果进行实现。

### 4.Commit/rollback（提交Saga事务）

提交阶段会生成Saga执行引擎所需的调用链路图，commit操作产生ForwardRecovery（正向SQL补偿）任务，rollback操作产生BackwardRecovery任务（逆向SQL补偿）。
