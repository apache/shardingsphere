+++
title = "Apache ShardingSphere整合Seata AT分布式事务"
weight = 7
chapter = true
+++

### 背景知识

Seata 是阿里集团和蚂蚁金服联合打造的分布式事务框架，目前版本包含了 AT 事务和 TCC 事务。其中AT事务的目标是在微服务架构下，提供增量的事务 ACID 语意，让用户像使用本地事务一样，使用分布式事务，核心理念同 ShardingSphere 一脉相承。

Github: https://github.com/seata/seata

### Seata AT模型

Seata AT 事务模型包含 TM (事务管理器)，RM (资源管理器)，TC (事务协调器)。

其中TC是一个独立的服务需要单独部署，TM 和 RM 以 jar 包的方式同业务应用部署在一起，它们同 TC 建立长连接，在整个事务生命周期内，保持 RPC 通信。 

其中全局事务的发起方作为 TM，全局事务的参与者作为 RM ; TM 负责全局事务的 begin 和 commit/rollback，RM 负责分支事务的执行和 commit/rollback。

![](https://shardingsphere.apache.org/blog/img/seata1.jpg)

### ShardingSphere 分布式事务 SPI

ShardingSphere 提供了一套接入分布式事务的 SPI ，设计的目标是保证数据分片后，事务的 ACID 语意。分布式事务的实现目前主要包含两阶段的 XA 和 BASE 柔性事务。Seata AT 事务作为 BASE 柔性事务的一种实现，可以无缝接入到 ShardingSphere 生态中。

![](https://shardingsphere.apache.org/blog/img/seata2.jpg)

两阶段XA事务方面，我们已经整合了 Atomikos，Narayana，Bitronix 事务管理器，XA 事务底层依赖具体的数据库厂商对 XA 两阶段提交协议的支持，通常 XA 协议通过在 Prepare 和 Commit 阶段进行 2PL(2阶段锁)，保证了分布式事务的 ACID，通常适用于短事务及非云化环境（云化环境下一次 IO 操作大概需要 20 ms，两阶段锁会锁住资源长达 40 ms，因此事务的 TPS 会降到 25/s 左右，非云化环境通常一次 IO 只需几毫秒，因此锁热点数据的时间相对较低）\[1\]。

BASE 柔性事务方面，目前我们已经完成了对 ServiceComb Saga 的整合，Saga 通过一阶段提交+补偿的方式提高了整体事务的性能，其中补偿的方式同 Seata 大致相同，即对分片后的物理 SQL 进行 revert 来生成补偿的 SQL，但 Saga 模型在理论上不支持隔离级别，适用于对性能要求较高，对一致性要求比较低的业务。Seata AT 事务在一阶段提交+补偿的基础上，通过 TC 的全局锁实现了RC隔离级别的支持，是介于 XA 和 Saga 之间的另一种实现。消息柔性事务方面，也欢迎大家参考我们的 SPI 提供整合的方案。

### 整合方案

整合 Seata AT 事务时，需要把 TM，RM，TC 的模型融入到 ShardingSphere 分布式事务的 SPI 的生态中。在数据库资源上，Seata 通过对接 DataSource 接口，让 JDBC 操作可以同 TC 进行 RPC 通信。同样，ShardingSphere 也是面向 DataSource 接口对用户配置的物理 DataSource 进行了聚合，因此把物理 DataSource 二次包装为 Seata 的 DataSource 后，就可以把 Seata AT 事务融入到 ShardingSphere 的分片中。

在 Seata 模型中，全局事务的上下文存放在线程变量中，通过扩展服务间的 transport，可以完成线程变量的传递，分支事务通过线程变量判断是否加入到整个 Seata 全局事务中。而 ShardingSphere 的分片执行引擎通常是按多线程执行，因此整合 Seata AT 事务时，需要扩展主线程和子线程的事务上下文传递，这同服务间的上下文传递思路完全相同。

![](https://shardingsphere.apache.org/blog/img/seata3.jpg)

### Quick Start

我们已经实现了 base-seata-raw-jdbc-example，大家可以自行进行尝试。

https://github.com/apache/incubator-shardingsphere-example/tree/dev/sharding-jdbc-example/transaction-example/transaction-base-seata-example/transaction-base-seata-raw-jdbc-example

操作手册：

1.按照 seata-work-shop 中的步骤，下载并启动 seata server。

https://github.com/seata/seata-workshop

参考 Step6 和 Step7 即可

2.在每一个分片数据库实例中执行 resources/sql/undo_log.sql 脚本，创建 undo_log 表

3.Run YamlConfigurationTransactionExample.java

### 待优化项

Seata AT 事务在 Revert SQL 时，需要对 ShardingSphere 分片后的物理 SQL 进行二次的解析，这里我们需要设计一个 SPI ，避免 SQL 二次解析的性能损耗。

参考论文

[1]: Transactions for Distributed Actors in the Cloud

https://www.microsoft.com/en-us/research/wp-content/uploads/2016/10/EldeebBernstein-TransactionalActors-MSR-TR-1.pdf


