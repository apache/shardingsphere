+++
title = "The quick explanation of ShardingSphere transaction module"
weight = 12
chapter = true
+++

## Distributed Transaction Implementation on Sharding-Sphere

<!-- ## 分布式事务在Sharding-Sphere中的实现 -->
### Lecturer
<!-- ### 讲师简介 -->

<!-- **赵俊** -->
**Jun Zhao**

Work at JD Finance
<!-- 京东金融 -->
Senior Java Development Engineer
<!-- 高级Java开发工程师 -->

<!-- - 多年互联网开发经验，热爱开源技术，对分布式存储有浓厚的兴趣。熟悉ElasticSearch、HBase、Presto、Storm等离线和实时数据处理 -->
- Years of internet development experience, love open source technology, full of interested in distributed storage. And familiar with online and realtime data processing with ElasticSearch、HBase、Presto、Storm and et.

<!-- - 目前主要在Sharding-Sphere团队负责分布式事务的开发 -->
- Be responsible for distributed transaction implementation in Sharding-Sphere team.

### Using Scenarios of Distributed Transaction
<!-- ### 分布式事务的使用场景 -->

#### ACID

<!-- 一切从ACID开始说起。ACID是本地事务所具有的四大特征： -->
 Everything starts with ACID(atomicity, consistency, isolation, durability). ACID are features of database local transaction as following,

<!-- *   **Atomicity：原子性** -->
*   **Atomicity**

Atomicity guarantees that each transaction is treated as a single "unit", which either succeeds completely, or fails completely.
    <!-- 事务作为整体来执行，要么全部执行，要么全不执行。 -->

<!-- *   **Consistency：一致性** -->
*   **Consistency**

    <!-- 事务应确保数据从一个一致的状态转变为另一个一致的状态。 -->

Consistency ensures that a transaction can only bring the database from one valid state to another.

<!-- *   **Isolation：隔离性** -->
*   **Isolation**

Transactions are often executed concurrently, and the effects of an incomplete transaction might not even be visible to other transactions.
    <!-- 多个事务并发执行时，一个事务的执行不应影响其他事务的执行。 -->

<!-- *   **Durability：持久性** -->
*   **Durability**

    <!-- 已提交的事务修改数据会被持久保持。 -->

Durability guarantees that once a transaction has been committed, it will remain committed even in the case of a system failure (e.g., power outage or crash).

Local transaction of relational database guarantees ACID perfectly. But in distributed scenarios, its performance will be a bottleneck. This post will explain How to make ACID features available or the alternative for distributed database.

<!-- 关系型数据库的本地事务完美的提供了对ACID的原生支持。但在分布式的场景下，它却成为系统性能的桎梏。如何让数据库在分布式场景下满足ACID的特性或找寻相应的替代方案，是本文将要阐述的话题。 -->

<!-- #### CAP和Base理论 -->
#### CAP and BASE Theorem
<!-- 对于互联网应用而言，随着访问量和数据量的激增，传统的单体架构模式将无法满足业务的高速发展。这时，开发者需要把单体应用拆分为多个独立的小应用，把单个数据库按照分片规则拆分为多个库和多个表。 -->

For the internet application, traditional all-in-one architecture can't satisfy the business requirement with increasing of visit and data gradually. the developer of the application needs to split the big application into several independent small applications, And all data in one database will be reorganized into multiple databases and tables.

But how to keep ACID among multi databases will be a hard tech problem, the CAP and BASE are basic theorem when to discuss the problem in distributed system.

<!-- 数据拆分后，如何在多个数据库节点间保证本地事务的ACID特性则成为一个技术难题，并且由此而衍生出了CAP和BASE经典理论。 -->

<!-- CAP理论指出，对于分布式的应用而言，不可能同时满足C（一致性），A（可用性），P（分区容错性），由于网络分区是分布式应用的基本要素，因此开发者需要在C和A上做出平衡。   -->

The CAP theorem states that it is impossible for a distributed data system to simultaneously provide more than two out of the following three guarantees.

  * Consistency
  * Availability
  * Partition tolerance

No distributed system is safe from network failures, thus network partitioning generally has to be tolerated. In the presence of a network partition, one has to choose between consistency and availability.

BASE (basically available, soft state, eventually consistent) theorem is the result of trade off Consistency against availability.

<!-- 由于C和A互斥性，其权衡的结果就是BASE理论。   -->

<!-- 对于大部分的分布式应用而言，只要数据在规定的时间内达到最终一致性即可。我们可以把符合传统的ACID叫做刚性事务，把满足BASE理论的最终一致性事务叫做柔性事务。  -->

For the most of distributed application, it's enough that data to be eventually consistent state in a reasonable time. If we call transactions that satisfy ACID as hard transactions, then transactions based on BASE are called BASE transactions.

Without regarding system requirements and performance, pursuing strong consistency is not the best solution. The distributed application can benefit from both hard and BASE transaction at the same time. The local service adopt strong consistent transaction, and the service across multi systems adopt eventually consistent transaction. How to trade off performance against consistency of a system is a key challenge for the architect and developer.

<!-- 一味的追求强一致性，并非最佳方案。对于分布式应用来说，刚柔并济是更加合理的设计方案，即在本地服务中采用强一致事务，在跨系统调用中采用最终一致性。如何权衡系统的性能与一致性，是十分考验架构师与开发者的设计功力的。 -->

![](https://shardingsphere.apache.org/blog/img/realization1.jpg)

<!-- ### 业界方法 -->
### Distributed Transaction Method

<!-- 具体到分布式事务的实现上，业界主要采用了XA协议的强一致规范以及柔性事务的最终一致规范。 -->
For implementation of distributed transaction, There are two main available specifications, XA(using 2PC) and BASE transaction.

#### XA

XA standard describes the interface between transaction manager(TM) and resource manager(RM) by the X/Open CAE Specification for Distributed Transaction Processing。

<!-- XA是X/Open CAE Specification (Distributed Transaction Processing)模型中定义的TM（Transaction Manager）与RM（Resource Manager）之间进行通信的接口。 -->

The XAResource interface is a Java mapping of the industry standard XA interface, And its implementation varies among database vendors。

<!-- Java中的javax.transaction.xa.XAResource定义了XA接口，它依赖数据库厂商对jdbc-driver的具体实现。 -->

<!-- mysql-connector-java-5.1.30的实现可参考： -->
The implementation details by MySQL can be found in com.mysql.jdbc.jdbc2.optional.MysqlXAConnection class of mysql-connector-java v5.1.30 package.


<!-- 在XA规范中，数据库充当RM角色，应用需要充当TM的角色，即生成全局的txId，调用XAResource接口，把多个本地事务协调为全局统一的分布式事务。   -->
In XA specification, TM(the application) controls a transaction which involves multiple RM(the database).
TM needs a global transaction id(xid) to call XAResource interfaces and coordinate multi local transactions into a global unique distributed transaction.

<!-- **一阶段提交：弱XA** -->

**One Phase Commit：weak XA**

![](https://shardingsphere.apache.org/blog/img/realization2.jpg)

<!-- 弱XA通过去掉XA的Prepare阶段，以达到减少资源锁定范围而提升并发性能的效果。典型的实现为在一个业务线程中，遍历所有的数据库连接，依次做commit或者rollback。弱XA同本地事务相比，性能损耗低，但在事务提交的执行过程中，若出现网络故障、数据库宕机等预期之外的异常，将会造成数据不一致，且无法进行回滚。基于弱XA的事务无需额外的实现成本，因此Sharding-Sphere默认支持。 -->

Comparing to 2PC, weak XA without prepare phase can benefit for reducing resource blocking and improving concurrency. The implementation only needs to iterate and call commit/callback on each datasource connection in a business thread. Performance of weak XA almost without reducing compares to local transaction. When has network failures、database crash and et., weak XA will lead inconsistent data and can't be rollback. Weak XA transaction implementation doesn't need extra efforts, so Sharding-Sphere supports it by default.

<!-- **二阶段提交：2PC** -->

**Two Phase Commit：2PC**

![](https://shardingsphere.apache.org/blog/img/realization3.jpg)

<!-- 二阶段提交是XA的标准实现。它将分布式事务的提交拆分为2个阶段：prepare和commit/rollback。 -->
Two phase commit is a standard implementation of XA. The distributed transaction commit consists of two phases: prepare and commit/rollback.

All local transactions will use default isolation level to lock resources, record redo and undo logs when XA global transaction enabled. In the prepare stage, TM initiates a prepare request to each RM and waits for response. and then to execute a commit operation if all RMs send yes or decides to execute a rollback if an TM sends no. if all TM said yes, but some of nodes crash on commit stage, The XA recover will be used to commit compensation when the node recovery, and make sure that data is consistent.

<!-- 开启XA全局事务后，所有子事务会按照本地默认的隔离级别锁定资源，并记录undo和redo日志，然后由TM发起prepare投票，询问所有的子事务是否可以进行提交：当所有子事务反馈的结果为“yes”时，TM再发起commit；若其中任何一个子事务反馈的结果为“no”，TM则发起rollback；如果在prepare阶段的反馈结果为yes，而commit的过程中出现宕机等异常时，则在节点服务重启后，可根据XA recover再次进行commit补偿，以保证数据的一致性。 -->

On 2PC model, the prepare stage needs to wait all involved RMs response and that may lead resource locked for a long time，so it's not suitable for high concurrency and time-consumed sub transaction scenario.

<!-- 2PC模型中，在prepare阶段需要等待所有参与子事务的反馈，因此可能造成数据库资源锁定时间过长，不适合并发高以及子事务生命周长较长的业务场景。 -->

Sharding-Sphere supports XA-based strong consistent transaction solution, third-party XA implementations, such as Atomikos and Narayana, can be inject into Sharding-Sphere by SPI.
<!-- Sharding-Sphere支持基于XA的强一致性事务解决方案，可以通过SPI注入不同的第三方组件作为事务管理器实现XA协议，如Atomikos和Narayana。 -->


<!-- #### 柔性事务 -->

### BASE Transaction

<!-- 柔性事务是对XA协议的妥协和补偿，它通过对强一致性要求的降低，以达到降低数据库资源锁定时间的效果。柔性事务的种类很多，可以通过各种不同的策略来权衡使用。 -->

BASE transaction is a compromise to XA, don't need data to be strong consistent any more which benefits for reducing lock time of resource. There are a variety of BASE transaction implementations, various strategies can be used to trade off.


<!-- **一阶段提交 + 补偿 ：最大努力送达（BED）** -->
**One Phase Commit with Compensation ：Best Effort Delivery(BED)**

BED is a compensate strategy for weak XA. Using transaction table records all transactional operation SQL, the record will be deleted if sub transaction success to commit or retry until max configured retries. Trying it best to make data consistent that's why we call it BED. In different business scenes, in order to balance consistency and availability, using synchronous or asynchronous retry.

<!-- 最大努力送达，是针对于弱XA的一种补偿策略。它采用事务表记录所有的事务操作SQL，如果子事务提交成功，将会删除事务日志；如果执行失败，则会按照配置的重试次数，尝试再次提交，即最大努力的进行提交，尽量保证数据的一致性，这里可以根据不同的业务场景，平衡C和A，采用同步重试或异步重试。

这种策略的优点是无锁定资源时间，性能损耗小。缺点是尝试多次提交失败后，无法回滚，它仅适用于事务最终一定能够成功的业务场景。因此BED是通过事务回滚功能上的妥协，来换取性能的提升。 -->

The advantage of the strategy is free of resource lock, low performance loss. But the transaction can't be rollback if still failed until reaches max reties, so it's only useful for the transaction that will be successful one hundred percentage. For the sake of improving performance, BED sacrifices rollback function of transaction.

![](https://shardingsphere.apache.org/blog/img/realization4.jpg)

<!-- **TCC： Try-Confirm-Cancel**   -->

**TCC： Try-Confirm-Cancel**  

<!-- TCC模型是把锁的粒度完全交给业务处理，它需要每个子事务业务都实现Try-Confirm/Cancel接口。 -->
TCC model gives the control of lock to business level, and all sub transactions business must implement Try-Confirm/Cancel interface.

<!-- *   **Try:**

    尝试执行业务；

    完成所有业务检查（一致性）；

    预留必须业务资源（准隔离性）； -->

*   **Try:**

    Try to execute business;

    Finish all business check(Consistency);

    Reserve required resource for business(Isolation);

<!-- *   **Confirm:**

    确认执行业务；

    真正执行业务，不作任何业务检查；

    只使用Try阶段预留的业务资源；

    Confirm操作满足幂等性； -->

*   **Confirm:**

    Confirm execute business;

    Execute the business logic without business check;

    Only use reserved resource in the Try phase;

    Confirm operation is idempotent;


<!-- *   **Cancel:**

    取消执行业务；

    释放Try阶段预留的业务资源；

    Cancel操作满足幂等性。 -->

*   **Cancel:**

    Cancel execute business;

    Release reserved resource in the Try phase;

    Cancel operation is idempotent;

<!-- 这三个阶段都会按本地事务的方式执行，不同于XA的prepare，TCC无需将XA的投票期间的所有资源挂起，因此极大的提高了吞吐量。   -->

Different from XA prepare stage, the three stages will execute as the local transaction way. TCC don't need to lock all the resource during voting, and improves system concurrency.


<!-- 下面对TCC模式下，A账户往B账户汇款100元为例子，对业务的改造进行详细的分析： -->
Using TCC model, in case of account A transfers 100 dollars to account B, The following figure shows the transformation of the business to support TCC, and the detail analysis of business changes as following:

![](https://shardingsphere.apache.org/blog/img/realization5.jpg)

<!-- 汇款服务和收款服务分别需要实现，Try-Confirm-Cancel接口，并在业务初始化阶段将其注入到TCC事务管理器中。 -->

Remittance and collection service need to implement Try-Confirm-Cancel interface, and injects it
into TCC transaction manager in the business initiation stage.

<!-- 汇款服务 -->

Remittance service

<!-- *   **Try：**

    检查A账户有效性，即查看A账户的状态是否为“转帐中”或者“冻结”；

    检查A账户余额是否充足；

    从A账户中扣减100元，并将状态置为“转账中”；

    预留扣减资源，将从A往B账户转账100元这个事件存入消息或者日志中； -->
*   **Try：**

    Check effectiveness of account A, namely, check status of account A whether in "transferring" or "Frozen";

    Check whether account A has enough money;

    Deduct 100 dollars from account A and update the status to "Transferring"

    Reserve the deduction resource, store the event that transfer 100 dollars from account A to B into the message or log.

<!-- *   **Confirm：**

    不做任何操作；-->

*   **Confirm：**    

    no operation;

<!-- *   **Cancel：**

    A账户增加100元；

    从日志或者消息中，释放扣减资源。 -->

*   **Cancel：**

   Add 100 dollars into account A;

   Release the deduction resource from messages or logs;

<!-- 收款服务 -->

Collection service

<!-- *   **Try：**

    检查B账户账户是否有效； -->

*   **Try**

    Check effectiveness of account B;


<!-- *   **Confirm**：

    读取日志或者消息，B账户增加100元；

    从日志或者消息中，释放扣减资源； -->

 *   **Confirm**

    Read log or message, add 100 dollars into account B;

    Release the deduction resource from messages or logs;

<!-- *   **Cancel：**

    不做任何操作。 -->

*   **Cancel**

    No operation;

<!-- 由此可以看出，TCC模型对业务的侵入强，改造的难度大。   -->

It can be seen that TCC model is intrusive for the business, and hard to business transformation.

<!-- **消息驱动** -->

**Message-Driven Transaction Model**

![](https://shardingsphere.apache.org/blog/img/realization6.jpg)

<!-- 消息一致性方案是通过消息中间件保证上下游应用数据操作的一致性。基本思路是将本地操作和发送消息放在一个事务中，下游应用向消息系统订阅该消息，收到消息后执行相应操作。本质上是依靠消息的重试机制，达到最终一致性。消息驱动的缺点是：耦合度高，需要在业务系统中引入MQ，导致系统复杂度增加。 -->

The message-based consistent solution depends on message middleware to make sure upstream and downstream applications keep the data consistent. The basic idea is to put the local operation and sending message into a local transaction, the downstream application consumes the message and executes the corresponding operation. It essentially relies on retry mechanism of message middleware to achieve eventually consistent. The disadvantage of message-driven is highly couple with message queue, which may increase the complexity of business system.

<!-- **SAGA** -->

**SAGA**

<!-- Saga起源于1987年Hector & Kenneth发表的论文Sagas。 -->

Saga derived from a [paper](https://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf) published in 1987 by Hector & Kenneth.

<!-- Saga工作原理 -->

 Saga work principle

<!-- Saga模型把一个分布式事务拆分为多个本地事务，每个本地事务都有相应的执行模块和补偿模块（ TCC中的Confirm和Cancel）。当Saga事务中任意一个本地事务出错时，可以通过调用相关的补偿方法恢复之前的事务，达到事务最终的一致性。 -->

The saga model splits a distributed transaction into multiple local transactions(like Confirm and Cancel on TCC). Each local transactions has its own execution module and compensation module. When any of local transaction fails, the corresponding compensation method is called to restore the executed transaction, achieving the eventual consistency of the transactions.

Once compensating transactions (C1, C2, ..., Cn-1) are defined for Saga (T1, T2, ..., Tn). Then, the saga system can make the following guarantee:
Either the sequence T1, T2, ..., Tn(which is the preferable one) or T1, T2, ...,Tx, Cx, ..., C2, C1 will be executed.

<!-- 当每个Saga子事务 T1, T2, …, Tn 都有对应的补偿定义 C1, C2, …, Cn-1,那么Saga系统可以保证：

*   子事务序列 T1, T2, …, Tn得以完成 (最佳情况)；

*   或者序列 T1, T2, …, Tj, Cj, …, C2, C1, 0 < j < n, 得以完成。 -->


 For the lack of prepare stage on Saga model, transactions can't keep isolation to each other. So loss of update, dirty read and et problem will happen when a resource is operated concurrently by multi transactions. The problem can be settled by concurrency control in business level, such as lock or pre-allocation resource.


<!-- 由于Saga模型中没有Prepare阶段，因此事务间不能保证隔离性，当多个Saga事务操作同一资源时，就会产生更新丢失、脏数据读取等问题，这时需要在业务层控制并发，例如：

*   在应用层面加锁；

*   应用层面预先冻结资源。 -->




<!-- Saga恢复方式 -->

Saga recovery

<!-- Saga支持向前和向后恢复： -->

<!-- *   向后恢复：补偿所有已完成的事务，如果任一子事务失败；

*   向前恢复：重试失败的事务，假设每个子事务最终都会成功。 -->

The saga model supports both forward recovery and reverse recovery. Backward recovery compensates all the completed transaction when any transaction fails. In contrast, Forward recovery attempts to retry the transaction that currently fails. It's based on the hypothesis that all transactions will be  successful finally.

<!-- 显然，向前恢复没有必要提供补偿事务，如果你的业务中，子事务（最终）总会成功，或补偿事务难以定义或不可能，向前恢复更符合你的需求。理论上补偿事务永不失败，然而，在分布式世界中，服务器可能会宕机、网络可能会失败，甚至数据中心也可能会停电，这时需要提供故障恢复后回退的机制，比如人工干预。 -->

Obviously, Forward recovery don't need compensation. If the transaction in the business will eventually succeed or backward compensating transaction is hard to define or impossible, forward recovery is more suitable for you. However, server crash, network and even datacenter power failure can happen in the real world. Recovery mechanism for failure are also needed, such as interference by people.

<!-- 总的来说，TCC和MQ都是以服务为范围进行分布式事务的处理，而XA、BED、SAGA则是以数据库为范围进行分布式处理，我们更趋向于选择后者，对于业务而言侵入小，改造的成本低。 -->

All in word, TCC and MQ are both depend on business transformation, but XA, BED and SAGA only involve database. So latter is more preferable solution, which is less invasive for business, and low transformation cost.


<!-- ### Sharding-Sphere对分布式事务的支持 -->
### Distributed Transaction of Sharding-Sphere

<!-- Sharding-Sphere是一套开源的分布式数据库中间件解决方案组成的生态圈，它由Sharding-JDBC、Sharding-Proxy和Sharding-Sidecar这3款相互独立的产品组成。它们均提供标准化的数据水平扩展、分布式事务和分布式治理等功能，可适用于如Java同构、异构语言、容器、云原生等各种多样化的应用场景。 -->

[ShardingSphere](https://github.com/sharding-sphere/sharding-sphere/) is an open-source ecosystem consisted of a set of distributed database middleware solutions, including 3 independent products, JDBC, Proxy & Sidecar. They all provide functions of data scale out, distributed transaction and distributed governance, applicable in a variety of situations such as Java isomorphism, heterogeneous language container and cloud native.

<!-- 项目地址： -->

<!-- Sharding-Sphere同时支持XA和柔性事务，它允许每次对数据库的访问，可以自由选择事务类型。分布式事务对业务操作完全透明，极大地降低了引入分布式事务的成本。 -->

Both XA and BASE transaction are supported by Sharding-Sphere, and allows to select different transaction type for per request. Distributed transactions are completely transparent to business operations, which greatly reduces the cost of introducing distributed transactions.

<!-- #### 事务模型 -->

### Transaction Model

![](https://shardingsphere.apache.org/blog/img/realization7.jpg)

<!-- Sharding-Sphere事务管理器集成了XA和柔性事务模型：

- 对于XA事务而言，采用SPI的方式让弱XA、Atomikos、Narayana间保持互斥；

- 对于柔性事务而言，根据每次连接中事务的类型，可以选择独立的事务管理器进行处理，每个事务管理器都会实现标准的ShardingTransaction接口，在TransactionEvent到来时，执行对应的begin、commit、rollback操作。 -->

Sharding-Sphere TM integrated XA and BASE transaction model:

- For XA transaction, using SPI makes weak XA、Atomikos、Narayana are mutually exclusive.

<!-- 下面将Sharding-Sphere内部如何用事件驱动方式，将事务从分片主流程中解耦进行详细说明： -->
The following section will explain how to decouple transaction from main process by event-driven:

![](https://shardingsphere.apache.org/blog/img/realization8.jpg)

<!-- 从图可以看出在Sharding-core在调用执行引擎时，会根据SQL的种类产生事件进行分发。事务监听线程在收到符合要求的事件后，再调用对应的事务处理器进行处理。 -->

As can be seen from above figure, Sharding-core will produce and dispatch various events according SQL, and the thread of transaction listener will invoke corresponding transaction processor to handle when event arrives.

<!-- #### Sharding-Proxy事务实现 -->

### Transaction Implementation of Sharding-Sphere


<!-- Sharding-Proxy是基于netty开发的数据库中间代理层，实现了标准的MySQL协议，可以看做是一个实现了数据分片的数据库。Sharding-Proxy已经实现了基于Atomikos的XA事务，为了保证所有的子事务都处于同一个线程之中，整个Proxy的线程模型进行了如下的调整： -->

Sharding-Proxy is a netty based database middle layer proxy, it implements MySQL protocol, and could be regarded as a database with built-in data sharding ability. Sharding-Proxy has implemented XA transaction based on Atomikos. For ensuring all sub transaction in the same thread, the changes of proxy thread model can be seen from the following figure.

![](https://shardingsphere.apache.org/blog/img/realization9.jpg)

<!-- 当开启事务后，Proxy后端的SQL命令执行引擎将采用一通道一线程的模式，此事务线程的生命周期同通道保持一致。事务处理的具体过程与Proxy彻底解耦，即Proxy将发布事务类型的事件，然后Sharding-Sphere-TM根据传入的事务消息，选择具体的TM进行处理。 -->

When transaction enabled, SQL engine will use Channel-Thread pattern on Proxy backend, and lifecycle of channel and transaction thread keeps same. The process procedure of transaction are totally decoupled with Proxy, namely, the transaction event produced by Proxy will consumed by Sharding-Sphere TM.

<!-- 压测结果表明：XA事务的插入和更新的性能，基本上同跨库的个数呈线性关系，查询的性能基本不受影响，建议在并发量不大，每次事务涉及的库在10个以内时，可以使用XA。 -->
Pressure test results show that insert and update performance of XA transaction is linear with amount of databases, and performance of query don't have obvious change.
so XA could be used in scenario with low concurrency and databases are not more than 10 in a transaction.

![](https://shardingsphere.apache.org/blog/img/realization10.jpg)

<!-- Atomikos事务管理器原理分析 -->

Principle Analysis of Atomikos Transaction Manager

![](https://shardingsphere.apache.org/blog/img/realization11.jpg)

<!-- Atomikos的事务管理器可以内嵌到业务进程中，当应用调用TransactionManager.begin时，将会创建本次XA事务，并且与当前线程关联。同时Atomikos也对DataSource中的connection做了二次封装，代理connection中含有本次事务相关信息的状态，并且拦截了connection的JDBC操作。 -->

Transaction manager of Atomikos can imbeded into business process, a XA transaction will be created and binds with current thread when application calls TransactionManager.begin method. Connection of dataSource was proxied by Atomikos, so JDBC manipulation and transaction info of the connection will be intercepted by Atomikos.

<!-- 在createStatement时，调用XAResource.start进行资源注册；在close时，调用XAResource.end让XA事务处于idle可提交状态；在commit或rollback时，依次调用prepare和commit进行二阶段提交。 -->

XAResource.start method will be executed when createStatement; the close method will call XAResource.end that makes XA transaction to be idle state; Calling prepare and commit in turn for two-phase commit when commit or rollback.

<!-- **Sharding-Sphere的Saga事务实现** -->

**Saga Transaction Implementation of Sharding-Sphere**

<!-- Sharding-Sphere通过与Apache Service Comb的合作，将采用Service Comb的Saga事务引擎作为的分布式事务实现。 -->

Cooperate with Apache Service Comb, Sharding-Sphere will use Saga engine of Service Comb to implement the distributed transaction.

<!-- Apache Service Comb是华为开源的微服务框架，其中微服务事务处理框架分为集中式和分布式协调器。未来会在Sharding-Sphere内部集成Saga集中式协调器，支持同一线程内不同服务（本地）间的分布式事务。 -->

[Apache Service Comb](https://github.com/apache/incubator-servicecomb-saga) is an open source micro-service framework initiated by HuaWei, the distributed transaction framework of micro-service can be divided into centralized and distributed coordinators.
Sharding-Sphere will integrate centralized coordinator of Saga, and support distributed transaction for  services in same thread in the future.

<!-- Service Comb 集中式事务协调器 -->

Centralized Transaction Coordinator of Service Comb


![](https://shardingsphere.apache.org/blog/img/realization12.jpg)

<!-- 集中式的协调器，包含了Saga调用请求接收、分析、执行以及结果查询的内容。任务代理模块需要预先知道Saga事务调用关系图，执行模块根据生成的调用图产生调用任务，调用相关微服务服务接口。如果服务调用执行出错，会调用服务的相关的补偿方法回滚。 -->

Centralized transaction coordinator including receiving, analysis, execution and result query for Saga request. Task agent module need to know predefined invoke relation graph of Sega, and execution module will generate invoke task according to invocation graph, and invoke micro-service interface. the compensation method will be invoke when service happens failure.

<!-- Saga执行模块通过分析请求的JSON数据，来构建一个调用关系图。Sharding-Sphere是通过JSON描述Saga事务串行调用子事务或者并行调用子事务。关系调用图被Saga实现中的任务运行模块分解成为一个一个执行任务，执行任务由任务消费者获取并生成相关的调用 （同时支持串行和并行调用）。Saga任务会根据执行的情况向Saga Log中记录对应的Saga事务的关键事件，并可以通过事件查看器查查询执行情况。 -->

Saga execution module builds an invocation graph by analyzing request JSON data. Sequentially or concurrently invocation of sub transaction of Saga was described by JSON in Sharding-Sphere.  Invocation relation graph was transferred into multiple execution tasks by execution module of Saga, the execution task consumer will generate corresponding invocation, both sequential and concurrent invocation are supported. Saga task will record key transaction event into logs according execution state, which can be viewed by event query engine.

<!-- Sharding-Sphere内嵌Saga事务管理器 -->

Imbeded Saga Transaction Manager of Sharding-Sphere

![](https://shardingsphere.apache.org/blog/img/realization13.jpg)

<!-- Saga以jar包的形式提供分布式事务治理能力。 -->
Saga provides distributed transaction service governance in form of jar.

<!-- 对Sharding-Sphere而言，confirm和cancel过程代表了子事务中的正常执行SQL和逆向执行SQL，（未来Sharding-Sphere将提供自动生成逆向SQL的能力）。当启用Saga柔性事务后，路由完成之后的物理数据源将开启本地自动提交事务，每次confirm和cancel都会直接提交。 -->

For Sharding-Sphere, the procedure of confirm and cancel represent normal and backward execution SQL of sub-transaction. Automation of reverse SQL generation is in the plan. After enabling Saga BASE transaction, routed physical datasource will enable transaction auto commit, every confirm and cancel will be submitted directly.

<!-- 在Sharding-Sphere内部，触发SQL执行引擎后，将会产生Saga事务事件，这时Sharding-Sphere事务监听器会注册本次子事务的confirm和cancel至Saga事务管理器的队列中；在业务线程触发commit和rollback后，Saga事务管理器再根据子事务执行的结果，判断进行confirm重试或者cancel流程。 -->

Inside of Sharding-Sphere, multi Saga transaction events will be generate when SQL engine was triggered, and confirm and cancel of current sub transaction will be registered into queue of Saga transaction manager; Saga transaction manager will execute confirm or cancel procedure according sub transaction execution result when commit or rollback was triggered.

<!-- ### 未来计划 -->
### Plans in the future

<!-- 未来Sharding-Sphere将按照文中介绍的Sharding-Sphere-TM逐步完善整个事务框架： -->
Evolution of Sharding-Sphere-TM framework will following the introduction on above, and main parts are as follow:

<!-- *   弱XA事务 （已发布）

*   基于Atomikos的XA事务（近期发布）

*   基于Narayana的XA事务（规划中）

*   BED柔性事务（已发布）

*   SAGA（开发中）

*   TCC（规划中） -->

* Weak XA transaction (released)
* Atomikos based XA transaction(to be released)
* BED BASE transaction(released)
* SAGA(working on)
* TCC(in planing)


<!-- 如果前面的分享太过冗长，那么千言万语汇聚成一张表格，欢迎阅读。 -->

If the previous sharing is too lengthy, then a thousand words are gathered into a table, welcome to read.

![](https://shardingsphere.apache.org/blog/img/realization14.jpg)

<!-- 未来，我们将不断优化当前的特性，陆续推出大家关注的柔性事务、数据治理等更多新特性。如果有什么想法、意见和建议，也欢迎留言交流，更欢迎加入到Sharding-Sphere的开源项目中： -->

In the future, feature will be optimized continuously, more new features such as BASE transactions and data governance that everyone concerns about will be launched one after another. Any thoughts, advices, suggestions, comments are appreciated, and welcome to join the open source community of Sharding-Sphere.

*   https://github.com/sharding-sphere/sharding-sphere/

*   https://gitee.com/sharding-sphere/sharding-sphere/

### Q&A

<!-- **Q1**：基于XA的事物，可以应用到微服务架构中吗？ -->
**Q1**: Does XA based transaction can be used int micro-service architecture ?

<!-- **A1**：目前我们是把事务管理器内嵌到JVM进程中，对于并发量小，短事务的业务，可以用XA。 -->

**A1**: Currently, transaction manager has been imbedded into JVM process, it's ok to use for low concurrency, shot transaction.

<!-- **Q2**：对于各个事务框架开发计划的先后顺序是基本什么来确定的呢？ -->
 **Q2**: For the order of transaction frameworks development, why ?
<!-- **A2**：基于难易程度，所以我们把TCC放到了最后。 -->
**A2**:  Based on the degree of difficulty, so we put TCC at the end.


<!-- **Q3**：支持多语言吗？比如golang？ -->
**Q3**: Does Sharding-Sphere support multi-language ? such as Golang?
<!-- **A3**：多语言可以用Sharding-Proxy。 -->
**A3**: For non java application, Sharding-Proxy are available.

<!--
**Q4**：这次是Proxy实现分布式事务吧？我记得之前Sharding-JDBC有实现。 -->
**Q4**: In this time, is Proxy implements distributed transaction ? But I remember Sharding-JDBC has implemented it ago.

<!-- **A4**：这次是整个SS的事务实现，包含Sharding-JDBC和Proxy，目前SJ的实现是弱XA和BED（最大努力送达），以后会增加SAGA和TCC。 -->
**A4**: Transaction implementation of SS including Sharding-JDBC and Proxy, SJ has implemented weak XA and BED, SAGA and TCC will be introduced in the future.


<!-- **Q5**：如果我只想用SS里的事务模块，可以吗？ -->
**Q5**: If I only want to use transaction module of SS, is it possible?

<!-- **A5**：SS是以事件驱动的方式进行的架构，未来事务模块只负责事务相关的处理。 -->
**A5**: Architecture of SS is event-driven, transaction module will only responsible for transaction in the future.


<!-- **Q6**：SAGA不支持ACID中的I，咱们这边怎么考虑的呢？ -->
**Q6**: SAGA doesn't support I of ACID, how do you think of this?
<!-- **A6**：目前暂不支持隔离性，今后我们有增加I的规划，其实所有的柔性事务都不支持I，TCC增加了Try阶段，可以理解是准隔离性，使用SAGA时，可以在业务层面控制并发，防止脏读等产生。 -->
**A6**: Now, Isolation is unsupported, and we have plan to support it. Actually all of BASE transaction don't support I, Try stage of TCC can be regarded as isolation. Control of concurrency on business level can avoid dirty read.


<!-- **Q7**：那意思，现在3的版本还不能单独用事务的模块？ -->
**Q7**: In version 3.0, does module of transaction works independently ?

<!-- **A7**：现在3.0版本，事务模块依赖了Sharding-JDBC模块，事务模块需要监听Sharding-JDBC和Proxy中的事件，然后进行事务操作。如果你想单独用事务模块，需要按Core中定义的事件，在你的业务里进行发布。 -->

 **A7**: In version 3.0, transaction module depends on Sharding-JDBC module, and executes transaction when received the event of Sharding-JDBC and Proxy. If you want to use the transaction module independently, you need to handle the event according to the definition of core module.

<!-- ### 直播回放 -->
### Live playbacks
https://m.qlchat.com/topic/details?topicId=2000001669563722&tracePage=liveCenter
