+++
title = "Seata 柔性事务"
weight = 3
+++

[Seata](https://github.com/seata/seata)是阿里集团和蚂蚁金服联合打造的分布式事务框架。
其 AT 事务的目标是在微服务架构下，提供增量的事务 ACID 语意，让开发者像使用本地事务一样，使用分布式事务，核心理念同 Apache ShardingSphere 一脉相承。

Seata AT 事务模型包含TM (事务管理器)，RM (资源管理器) 和 TC (事务协调器)。
TC 是一个独立部署的服务，TM 和 RM 以 jar 包的方式同业务应用一同部署，它们同 TC 建立长连接，在整个事务生命周期内，保持远程通信。
TM 是全局事务的发起方，负责全局事务的开启，提交和回滚。
RM 是全局事务的参与者，负责分支事务的执行结果上报，并且通过 TC 的协调进行分支事务的提交和回滚。

Seata 管理的分布式事务的典型生命周期：

1. TM 要求 TC 开始一个全新的全局事务。TC 生成一个代表该全局事务的 XID。
2. XID 贯穿于微服务的整个调用链。
3. 作为该 XID 对应到的 TC 下的全局事务的一部分，RM 注册本地事务。
4. TM 要求 TC 提交或回滚 XID 对应的全局事务。
5. TC 驱动 XID 对应的全局事务下的所有分支事务完成提交或回滚。

![Seata AT事务模型](https://shardingsphere.apache.org/document/current/img/transaction/seata-at-transaction.png)
