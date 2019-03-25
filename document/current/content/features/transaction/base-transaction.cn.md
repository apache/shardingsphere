+++
pre = "<b>3.4.3. </b>"
toc = true
title = "柔性事务"
weight = 3
+++

## 功能

* 完全支持跨库事务。
* 支持失败SQL重试及最大努力送达。
* 支持反向SQL、自动生成更新快照以及自动补偿。
* 默认使用关系型数据库进行快照及事务日志的持久化，支持使用SPI的方式加载其他类型的持久化。
* 暂不支持资源隔离。

## 设计

![柔性事务设计](https://shardingsphere.apache.org/document/current/img/transaction/transaction-base-design_cn.png)

ShardingSphere的柔性事务需要实现Sharding事务管理器的SPI接口，用于管理事务的生命周期。
同时柔性事务还需要通过ShardingSphere的内部SQL Hook，获取与SQL相关的必要信息，帮助事务管理器控制分布式事务。
事务隔离引擎还处于计划阶段，因此柔性事务暂不支持资源隔离功能。

## 支持情况

ShardingSphere的柔性事务已通过第三方SPI实现[Saga](https://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf)事务，Saga引擎使用[Servicecomb-Saga](https://github.com/apache/servicecomb-saga-actuator)。

### Saga事务

为了更好地理解柔性事务的设计思路，需要先解释数个概念

* 分支事务(BranchTransaction)： 分布式事务中，被路由到不同节点的实际SQL。
* 分支事务组(BranchTransactionGroup)： 分布式事务中，由同一个逻辑SQL生成的分支事务的组合。

![柔性事务Saga](https://shardingsphere.apache.org/document/current/img/transaction/saga-transaction-saga-design_cn.jpg)

当ShardingSphere对SQL进行解析时，事务引擎会将当前事务切换到新的分支事务组。
在开始并行执行路由后的实际SQL前，事务引擎会对这些SQL进行快照并注册对应分支事务到当前的分支事务组中。
当事务中所有的SQL均被解析并执行后，事务中可能存在数个分支事务组，每个分支事务组中也可能存在数个分支事务，如下图：

![Saga事务内容](https://shardingsphere.apache.org/document/current/img/transaction/saga-transaction-context_cn.jpg)

最后当用户提交事务时，Saga引擎按照分支事务组的顺序，使用其中分支事务的路由SQL进行重试或反向SQL进行回滚。

#### 注意
* 反向SQL需要**主键**，请确保在表结构中定义**主键**。
* 对于`INSERT`语句， 需要在SQL中显示插入**主键值**，如`INSERT INTO ${table_name} (id, value, ...) VALUES (11111, '', ....) (其中id为表主键)`。
* 若需要自动生成主键，可使用ShardingSphere的分布式主键。