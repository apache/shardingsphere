+++
title = "Apache ShardingSphere整合Seata AT分布式事务"
weight = 7
chapter = true
+++

## Apache ShardingSphere整合Seata AT分布式事务

### 背景知识

Seata是阿里集团和蚂蚁金服联合打造的分布式事务框架，目前版本包含了AT事务和TCC事务。其中AT事务的目标是在微服务架构下，提供增量的事务ACID语意，让用户像使用本地事务一样，使用分布式事务，核心理念同ShardingSphere一脉相承。

Github: https://github.com/seata/seata

### Seata AT模型

Seata AT事务模型包含TM(事务管理器)，RM(资源管理器)，TC(事务协调器)。

其中TC是一个独立的服务需要单独部署，TM和RM以jar包的方式同业务应用部署在一起，它们同TC建立长连接，在整个事务生命周期内，保持RPC通信。 

其中全局事务的发起方作为TM，全局事务的参与者作为RM ; TM负责全局事务的begin和commit/rollback，RM负责分支事务的执行和commit/rollback。

![](https://shardingsphere.apache.org/blog/img/seata1.jpg)

### ShardingSphere分布式事务SPI

ShardingSphere提供了一套接入分布式事务的SPI，设计的目标是保证数据分片后，事务的ACID语意。分布式事务的实现目前主要包含两阶段的XA和BASE柔性事务。Seata AT事务作为BASE柔性事务的一种实现，可以无缝接入到ShardingSphere生态中。

![](https://shardingsphere.apache.org/blog/img/seata2.jpg)

两阶段XA事务方面，我们已经整合了Atomikos，Narayana，Bitronix事务管理器，XA事务底层依赖具体的数据库厂商对XA两阶段提交协议的支持，通常XA协议通过在Prepare和Commit阶段进行2PL(2阶段锁)，保证了分布式事务的ACID，通常适用于短事务及非云化环境（云化环境下一次IO操作大概需要20ms，两阶段锁会锁住资源长达40ms，因此事务的TPS会降到25/s左右，非云化环境通常一次IO只需几毫秒，因此锁热点数据的时间相对较低）\[1\]。

BASE柔性事务方面，目前我们已经完成了对ServiceComb Saga的整合，Saga通过一阶段提交+补偿的方式提高了整体事务的性能，其中补偿的方式同Seata大致相同，即对分片后的物理SQL进行revert来生成补偿的SQL，但Saga模型在理论上不支持隔离级别，适用于对性能要求较高，对一致性要求比较低的业务。Seata AT事务在一阶段提交+补偿的基础上，通过TC的全局锁实现了RC隔离级别的支持，是介于XA和Saga之间的另一种实现。消息柔性事务方面，也欢迎大家参考我们的SPI提供整合的方案。

### 整合方案

整合Seata AT事务时，需要把TM，RM，TC的模型融入到ShardingSphere 分布式事务的SPI的生态中。在数据库资源上，Seata通过对接DataSource接口，让JDBC操作可以同TC进行RPC通信。同样，ShardingSphere也是面向DataSource接口对用户配置的物理DataSource进行了聚合，因此把物理DataSource二次包装为Seata的DataSource后，就可以把Seata AT事务融入到ShardingSphere的分片中。

在Seata模型中，全局事务的上下文存放在线程变量中，通过扩展服务间的transport，可以完成线程变量的传递，分支事务通过线程变量判断是否加入到整个Seata全局事务中。而ShardingSphere的分片执行引擎通常是按多线程执行，因此整合Seata AT事务时，需要扩展主线程和子线程的事务上下文传递，这同服务间的上下文传递思路完全相同。

![](https://shardingsphere.apache.org/blog/img/seata3.jpg)

### Quick Start

我们已经实现了base-seata-raw-jdbc-example，大家可以自行进行尝试。

https://github.com/apache/incubator-shardingsphere-example/tree/dev/sharding-jdbc-example/transaction-example/transaction-base-seata-example/transaction-base-seata-raw-jdbc-example

操作手册：

1.按照seata-work-shop中的步骤，下载并启动seata server。

https://github.com/seata/seata-workshop

参考 Step6 和 Step7即可

2.在每一个分片数据库实例中执行resources/sql/undo_log.sql脚本，创建undo_log表

3.Run YamlConfigurationTransactionExample.java

### 待优化项

Seata AT事务在Revert SQL时，需要对ShardingSphere分片后的物理SQL进行二次的解析，这里我们需要设计一个SPI，避免SQL二次解析的性能损耗。

参考论文

[1]: Transactions for Distributed Actors in the Cloud

https://www.microsoft.com/en-us/research/wp-content/uploads/2016/10/EldeebBernstein-TransactionalActors-MSR-TR-1.pdf


