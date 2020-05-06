+++
pre = "<b>3.4.1.2 </b>"
title = "Seata柔性事务"
weight = 3
+++

## Seata柔性事务

[Seata](https://github.com/seata/seata)是阿里集团和蚂蚁金服联合打造的分布式事务框架，截止到0.5.x版本包含了AT事务和TCC事务。其中AT事务的目标是在微服务架构下，提供增量的事务ACID语意，让用户像使用本地事务一样，使用分布式事务，核心理念同ShardingSphere一脉相承。

## Seata AT事务模型

`Seata AT`事务模型包含TM(事务管理器)，RM(资源管理器)，TC(事务协调器)。其中TC是一个独立的服务需要单独部署，TM和RM以jar包的方式同业务应用部署在一起，它们同TC建立长连接，在整个事务生命周期内，保持RPC通信。
其中全局事务的发起方作为TM，全局事务的参与者作为RM ; TM负责全局事务的begin和commit/rollback，RM负责分支事务的执行结果上报，并且通过TC的协调进行commit/rollback。

![Seata AT事务模型](https://shardingsphere.apache.org/document/current/img/transaction/seata-at-transaction.png)