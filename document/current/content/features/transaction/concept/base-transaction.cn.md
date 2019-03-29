+++
pre = "<b>3.4.1.2 </b>"
toc = true
title = "Saga柔性事务"
weight = 3
+++

### Saga事务

为了更好地理解柔性事务的设计思路，需要先解释数个概念：

* 分支事务(BranchTransaction)： 分布式事务中，被路由到不同节点的实际SQL。
* 分支事务组(BranchTransactionGroup)： 分布式事务中，由同一个逻辑SQL生成的分支事务的组合。

![柔性事务Saga](https://shardingsphere.apache.org/document/current/img/transaction/saga-transaction-saga-design_cn.png)

当ShardingSphere对SQL进行解析时，事务引擎会将当前事务切换到新的分支事务组。
在开始并行执行路由后的实际SQL前，事务引擎会对这些SQL进行快照并注册对应分支事务到当前的分支事务组中。
当事务中所有的SQL均被解析并执行后，事务中可能存在数个分支事务组，每个分支事务组中也可能存在数个分支事务，如下图：

![Saga事务内容](https://shardingsphere.apache.org/document/current/img/transaction/saga-transaction-context_cn.png)

最后当用户提交事务时，Saga引擎按照分支事务组的顺序，使用其中分支事务的路由SQL进行重试或反向SQL进行回滚。
