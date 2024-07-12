+++
title = "分布式事务在Sharding-Sphere中的实现"
weight = 2
chapter = true
+++

### 讲师简介

**赵俊**

京东金融

高级Java开发工程师

- 多年互联网开发经验，热爱开源技术，对分布式存储有浓厚的兴趣。熟悉 ElasticSearch、HBase、Presto、Storm 等离线和实时数据处理

- 目前主要在 Sharding-Sphere 团队负责分布式事务的开发

### 分布式事务的使用场景

#### ACID

一切从 ACID 开始说起。ACID 是本地事务所具有的四大特征：

*   **Atomicity：原子性**
    
    事务作为整体来执行，要么全部执行，要么全不执行。
    
*   **Consistency：一致性**
    
    事务应确保数据从一个一致的状态转变为另一个一致的状态。
    
*   **Isolation：隔离性**
    
    多个事务并发执行时，一个事务的执行不应影响其他事务的执行。
    
*   **Durability：持久性**
    
    已提交的事务修改数据会被持久保持。
    

关系型数据库的本地事务完美的提供了对ACID的原生支持。但在分布式的场景下，它却成为系统性能的桎梏。如何让数据库在分布式场景下满足ACID的特性或找寻相应的替代方案，是本文将要阐述的话题。

#### CAP 和 Base 理论

对于互联网应用而言，随着访问量和数据量的激增，传统的单体架构模式将无法满足业务的高速发展。这时，开发者需要把单体应用拆分为多个独立的小应用，把单个数据库按照分片规则拆分为多个库和多个表。

数据拆分后，如何在多个数据库节点间保证本地事务的ACID特性则成为一个技术难题，并且由此而衍生出了 CAP 和 BASE 经典理论。

CAP 理论指出，对于分布式的应用而言，不可能同时满足C（一致性），A（可用性），P（分区容错性），由于网络分区是分布式应用的基本要素，因此开发者需要在C和A上做出平衡。  

由于 C 和 A 互斥性，其权衡的结果就是 BASE 理论。  

对于大部分的分布式应用而言，只要数据在规定的时间内达到最终一致性即可。我们可以把符合传统的 ACID 叫做刚性事务，把满足 BASE 理论的最终一致性事务叫做柔性事务。  

一味的追求强一致性，并非最佳方案。对于分布式应用来说，刚柔并济是更加合理的设计方案，即在本地服务中采用强一致事务，在跨系统调用中采用最终一致性。如何权衡系统的性能与一致性，是十分考验架构师与开发者的设计功力的。

![](https://shardingsphere.apache.org/blog/img/realization1.jpg)

### 业界方法

具体到分布式事务的实现上，业界主要采用了XA协议的强一致规范以及柔性事务的最终一致规范。

#### XA

XA 是 X/Open CAE Specification (Distributed Transaction Processing)模型中定义的 TM（Transaction Manager）与RM（Resource Manager）之间进行通信的接口。

Java 中的 javax.transaction.xa.XAResource 定义了 XA 接口，它依赖数据库厂商对 jdbc-driver 的具体实现。

mysql-connector-java-5.1.30的实现可参考：

com.mysql.jdbc.jdbc2.optional.MysqlXAConnection。  

在 XA 规范中，数据库充当 RM 角色，应用需要充当 TM 的角色，即生成全局的 txId，调用 XAResource 接口，把多个本地事务协调为全局统一的分布式事务。  

**一阶段提交：弱XA**

![](https://shardingsphere.apache.org/blog/img/realization2.jpg)
弱 XA 通过去掉 XA 的 Prepare 阶段，以达到减少资源锁定范围而提升并发性能的效果。典型的实现为在一个业务线程中，遍历所有的数据库连接，依次做 commit 或者 rollback。弱XA同本地事务相比，性能损耗低，但在事务提交的执行过程中，若出现网络故障、数据库宕机等预期之外的异常，将会造成数据不一致，且无法进行回滚。基于弱XA的事务无需额外的实现成本，因此 Sharding-Sphere 默认支持。

**二阶段提交：2PC**

![](https://shardingsphere.apache.org/blog/img/realization3.jpg)

二阶段提交是 XA 的标准实现。它将分布式事务的提交拆分为 2 个阶段：prepare 和 commit/rollback。

开启XA全局事务后，所有子事务会按照本地默认的隔离级别锁定资源，并记录 undo 和 redo 日志，然后由 TM 发起 prepare 投票，询问所有的子事务是否可以进行提交：当所有子事务反馈的结果为“yes”时，TM 再发起 commit；若其中任何一个子事务反馈的结果为“no”，TM 则发起 rollback；如果在 prepare 阶段的反馈结果为 yes，而 commit 的过程中出现宕机等异常时，则在节点服务重启后，可根据 XA recover 再次进行 commit 补偿，以保证数据的一致性。

2PC模型中，在 prepare 阶段需要等待所有参与子事务的反馈，因此可能造成数据库资源锁定时间过长，不适合并发高以及子事务生命周长较长的业务场景。

Sharding-Sphere 支持基于 XA 的强一致性事务解决方案，可以通过 SPI 注入不同的第三方组件作为事务管理器实现 XA 协议，如 Atomikos 和 Narayana。

#### 柔性事务

柔性事务是对 XA 协议的妥协和补偿，它通过对强一致性要求的降低，已达到降低数据库资源锁定时间的效果。柔性事务的种类很多，可以通过各种不同的策略来权衡使用。

**一阶段提交 + 补偿 ：最大努力送达（BED）**

最大努力送达，是针对于弱 XA 的一种补偿策略。它采用事务表记录所有的事务操作 SQL，如果子事务提交成功，将会删除事务日志；如果执行失败，则会按照配置的重试次数，尝试再次提交，即最大努力的进行提交，尽量保证数据的一致性，这里可以根据不同的业务场景，平衡 C 和 A ，采用同步重试或异步重试。

这种策略的优点是无锁定资源时间，性能损耗小。缺点是尝试多次提交失败后，无法回滚，它仅适用于事务最终一定能够成功的业务场景。因此 BED 是通过事务回滚功能上的妥协，来换取性能的提升。

![](https://shardingsphere.apache.org/blog/img/realization4.jpg)

**TCC： Try-Confirm-Cancel**  

TCC模型是把锁的粒度完全交给业务处理，它需要每个子事务业务都实现Try-Confirm/Cancel接口。

*   **Try:**
    
    尝试执行业务；
    
    完成所有业务检查（一致性）；
    
    预留必须业务资源（准隔离性）；
    
*   **Confirm:**
    
    确认执行业务；
    
    真正执行业务，不作任何业务检查；
    
    只使用 Try 阶段预留的业务资源；
    
    Confirm 操作满足幂等性；
    
*   **Cancel:**
    
    取消执行业务；
    
    释放 Try 阶段预留的业务资源；
    
    Cancel 操作满足幂等性。
    

这三个阶段都会按本地事务的方式执行，不同于 XA 的 prepare，TCC无需将XA的投票期间的所有资源挂起，因此极大的提高了吞吐量。  

下面对 TCC 模式下，A 账户往 B 账户汇款 100 元为例子，对业务的改造进行详细的分析：

![](https://shardingsphere.apache.org/blog/img/realization5.jpg)

汇款服务和收款服务分别需要实现，Try-Confirm-Cancel 接口，并在业务初始化阶段将其注入到 TCC 事务管理器中。

汇款服务

*   **Try：**
    
    检查A账户有效性，即查看A账户的状态是否为“转帐中”或者“冻结”；
    
    检查A账户余额是否充足；
    
    从A账户中扣减 100 元，并将状态置为“转账中”；
    
    预留扣减资源，将从A往B账户转账 100 元这个事件存入消息或者日志中；
    
*   **Confirm：**
    
    不做任何操作；
    
*   **Cancel：**
    
    A账户增加 100 元；
    
    从日志或者消息中，释放扣减资源。
    

收款服务

*   **Try：**
    
    检查B账户账户是否有效；
    
*   **Confirm**：
    
    读取日志或者消息，B 账户增加 100 元；
    
    从日志或者消息中，释放扣减资源；
    
*   **Cancel：**
    
    不做任何操作。
    

由此可以看出，TCC 模型对业务的侵入强，改造的难度大。  

**消息驱动**

![](https://shardingsphere.apache.org/blog/img/realization6.jpg)

消息一致性方案是通过消息中间件保证上下游应用数据操作的一致性。基本思路是将本地操作和发送消息放在一个事务中，下游应用向消息系统订阅该消息，收到消息后执行相应操作。本质上是依靠消息的重试机制，达到最终一致性。消息驱动的缺点是：耦合度高，需要在业务系统中引入 MQ，导致系统复杂度增加。

**SAGA**

Saga 起源于 1987 年 Hector & Kenneth 发表的论文 Sagas。

参考地址：

https://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf

Saga工作原理

Saga 模型把一个分布式事务拆分为多个本地事务，每个本地事务都有相应的执行模块和补偿模块（ TCC 中的 Confirm 和 Cancel）。当 Saga 事务中任意一个本地事务出错时，可以通过调用相关的补偿方法恢复之前的事务，达到事务最终的一致性。

当每个 Saga 子事务 T1, T2, …, Tn 都有对应的补偿定义 C1, C2, …, Cn-1,那么 Saga 系统可以保证：

*   子事务序列 T1, T2, …, Tn得以完成 (最佳情况)；
    
*   或者序列 T1, T2, …, Tj, Cj, …, C2, C1, 0 < j < n, 得以完成。
    

由于Saga模型中没有Prepare阶段，因此事务间不能保证隔离性，当多个Saga事务操作同一资源时，就会产生更新丢失、脏数据读取等问题，这时需要在业务层控制并发，例如：

*   在应用层面加锁；
    
*   应用层面预先冻结资源。
    

Saga 恢复方式

Saga 支持向前和向后恢复：

*   向后恢复：补偿所有已完成的事务，如果任一子事务失败；
    
*   向前恢复：重试失败的事务，假设每个子事务最终都会成功。
    

显然，向前恢复没有必要提供补偿事务，如果你的业务中，子事务（最终）总会成功，或补偿事务难以定义或不可能，向前恢复更符合你的需求。理论上补偿事务永不失败，然而，在分布式世界中，服务器可能会宕机、网络可能会失败，甚至数据中心也可能会停电，这时需要提供故障恢复后回退的机制，比如人工干预。

总的来说，TCC 和 MQ 都是以服务为范围进行分布式事务的处理，而XA、BED、SAGA则是以数据库为范围进行分布式处理，我们更趋向于选择后者，对于业务而言侵入小，改造的成本低。

### Sharding-Sphere 对分布式事务的支持

Sharding-Sphere 是一套开源的分布式数据库中间件解决方案组成的生态圈，它由 Sharding-JDBC、Sharding-Proxy 和 Sharding-Sidecar 这3款相互独立的产品组成。它们均提供标准化的数据水平扩展、分布式事务和分布式治理等功能，可适用于如 Java 同构、异构语言、容器、云原生等各种多样化的应用场景。

项目地址：

https://github.com/sharding-sphere/sharding-sphere/

Sharding-Sphere 同时支持 XA 和柔性事务，它允许每次对数据库的访问，可以自由选择事务类型。分布式事务对业务操作完全透明，极大地降低了引入分布式事务的成本。

#### 事务模型

![](https://shardingsphere.apache.org/blog/img/realization7.jpg)

Sharding-Sphere 事务管理器集成了XA和柔性事务模型：

- 对于 XA 事务而言，采用 SPI 的方式让弱 XA、Atomikos、Narayana 间保持互斥；
    
- 对于柔性事务而言，根据每次连接中事务的类型，可以选择独立的事务管理器进行处理，每个事务管理器都会实现标准的 ShardingTransaction 接口，在 TransactionEvent 到来时，执行对应的 begin、commit、rollback 操作。
    

下面将 Sharding-Sphere 内部如何用事件驱动方式，将事务从分片主流程中解耦进行详细说明：

![](https://shardingsphere.apache.org/blog/img/realization8.jpg)

从图可以看出在 Sharding-core 在调用执行引擎时，会根据SQL的种类产生事件进行分发。事务监听线程在收到符合要求的事件后，再调用对应的事务处理器进行处理。

#### Sharding-Proxy事务实现

Sharding-Proxy 是基于 netty 开发的数据库中间代理层，实现了标准的 MySQL 协议，可以看做是一个实现了数据分片的数据库。Sharding-Proxy 已经实现了基于 Atomikos 的 XA 事务，为了保证所有的子事务都处于同一个线程之中，整个 Proxy 的线程模型进行了如下的调整：

![](https://shardingsphere.apache.org/blog/img/realization9.jpg)

当开启事务后，Proxy 后端的 SQL 命令执行引擎将采用一通道一线程的模式，此事务线程的生命周期同通道保持一致。事务处理的具体过程与 Proxy 彻底解耦，即 Proxy 将发布事务类型的事件，然后 Sharding-Sphere-TM 根据传入的事务消息，选择具体的 TM 进行处理。

压测结果表明：XA 事务的插入和更新的性能，基本上同跨库的个数呈线性关系，查询的性能基本不受影响，建议在并发量不大，每次事务涉及的库在 10 个以内时，可以使用 XA。

![](https://shardingsphere.apache.org/blog/img/realization10.jpg)

Atomikos 事务管理器原理分析

![](https://shardingsphere.apache.org/blog/img/realization11.jpg)

Atomikos 的事务管理器可以内嵌到业务进程中，当应用调用 TransactionManager.begin 时，将会创建本次 XA 事务，并且与当前线程关联。同时 Atomikos 也对 DataSource 中的 connection 做了二次封装，代理 connection 中含有本次事务相关信息的状态，并且拦截了 connection 的 JDBC 操作。

在 createStatement 时，调用 XAResource.start 进行资源注册；在 close 时，调用 XAResource.end 让 XA 事务处于 idle 可提交状态；在 commit 或 rollback 时，依次调用 prepare 和 commit 进行二阶段提交。

**Sharding-Sphere 的 Saga 事务实现**

Sharding-Sphere 通过与 Apache Service Comb 的合作，将采用 Service Comb 的 Saga 事务引擎作为的分布式事务实现。

Apache Service Comb 是华为开源的微服务框架，其中微服务事务处理框架分为集中式和分布式协调器。未来会在 Sharding-Sphere 内部集成 Saga 集中式协调器，支持同一线程内不同服务（本地）间的分布式事务。

参考链接：

https://github.com/apache/incubator-servicecomb-saga

Service Comb 集中式事务协调器

![](https://shardingsphere.apache.org/blog/img/realization12.jpg)

集中式的协调器，包含了 Saga 调用请求接收、分析、执行以及结果查询的内容。任务代理模块需要预先知道 Saga 事务调用关系图，执行模块根据生成的调用图产生调用任务，调用相关微服务服务接口。如果服务调用执行出错，会调用服务的相关的补偿方法回滚。

Saga 执行模块通过分析请求的 JSON 数据，来构建一个调用关系图。Sharding-Sphere 是通过 JSON 描述 Saga 事务串行调用子事务或者并行调用子事务。关系调用图被 Saga 实现中的任务运行模块分解成为一个一个执行任务，执行任务由任务消费者获取并生成相关的调用 （同时支持串行和并行调用）。Saga 任务会根据执行的情况向 Saga Log 中记录对应的 Saga 事务的关键事件，并可以通过事件查看器查查询执行情况。

Sharding-Sphere 内嵌 Saga 事务管理器

![](https://shardingsphere.apache.org/blog/img/realization13.jpg)

Saga 以 jar 包的形式提供分布式事务治理能力。

对 Sharding-Sphere 而言，confirm 和 cancel 过程代表了子事务中的正常执行 SQL 和逆向执行 SQL，（未来 Sharding-Sphere 将提供自动生成逆向SQL的能力）。当启用 Saga 柔性事务后，路由完成之后的物理数据源将开启本地自动提交事务，每次 confirm 和cancel 都会直接提交。

在 Sharding-Sphere 内部，触发 SQL 执行引擎后，将会产生 Saga 事务事件，这时 Sharding-Sphere 事务监听器会注册本次子事务的 confirm 和 cancel 至 Saga 事务管理器的队列中；在业务线程触发 commit 和 rollback 后，Saga 事务管理器再根据子事务执行的结果，判断进行 confirm 重试或者 cancel 流程。

### 未来计划

未来 Sharding-Sphere 将按照文中介绍的 Sharding-Sphere-TM 逐步完善整个事务框架：

*   弱 XA 事务 （已发布）
    
*   基于 Atomikos 的XA事务（近期发布）
    
*   基于 Narayana 的 XA 事务（规划中）
    
*   BED 柔性事务（已发布）
    
*   SAGA（开发中）
    
*   TCC（规划中）
    

如果前面的分享太过冗长，那么千言万语汇聚成一张表格，欢迎阅读。

![](https://shardingsphere.apache.org/blog/img/realization14.jpg)

未来，我们将不断优化当前的特性，陆续推出大家关注的柔性事务、数据治理等更多新特性。如果有什么想法、意见和建议，也欢迎留言交流，更欢迎加入到 Sharding-Sphere 的开源项目中：

*   https://github.com/sharding-sphere/sharding-sphere/
    
*   https://gitee.com/sharding-sphere/sharding-sphere/

### Q&A

**Q1**：基于 XA 的事物，可以应用到微服务架构中吗？

**A1**：目前我们是把事务管理器内嵌到 JVM 进程中，对于并发量小，短事务的业务，可以用 XA。

  

**Q2**：对于各个事务框架开发计划的先后顺序是基本什么来确定的呢？

**A2**：基于难易程度，所以我们把 TCC 放到了最后。

  

**Q3**：支持多语言吗？比如 golang？

**A3**：多语言可以用 Sharding-Proxy。

  

**Q4**：这次是 Proxy 实现分布式事务吧？我记得之前 Sharding-JDBC 有实现。

**A4**：这次是整个 SS 的事务实现，包含 Sharding-JDBC 和 Proxy ，目前 SJ 的实现是弱 XA 和 BED（最大努力送达），以后会增加 SAGA 和 TCC。

  

**Q5**：如果我只想用 SS 里的事务模块，可以吗？

**A5**：SS 是以事件驱动的方式进行的架构，未来事务模块只负责事务相关的处理。

  

**Q6**：SAGA 不支持 ACID 中的 I，咱们这边怎么考虑的呢？

**A6**：目前暂不支持隔离性，今后我们有增加 I 的规划，其实所有的柔性事务都不支持 I，TCC 增加了 Try 阶段，可以理解是准隔离性，使用 SAGA 时，可以在业务层面控制并发，防止脏读等产生。

  

**Q7**：那意思，现在 3 的版本还不能单独用事务的模块？

**A7**：现在 3.0 版本，事务模块依赖了 Sharding-JDBC 模块，事务模块需要监听 Sharding-JDBC 和 Proxy 中的事件，然后进行事务操作。如果你想单独用事务模块，需要按 Core 中定义的事件，在你的业务里进行发布。

### 直播回放

https://m.qlchat.com/topic/details?topicId=2000001669563722&tracePage=liveCenter



