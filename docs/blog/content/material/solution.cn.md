+++
title = "刚柔并济的开源分布式事务解决方案"
weight = 7
chapter = true
+++

## 刚柔并济的开源分布式事务解决方案

### 作者

张亮，京东数科数据研发负责人，Apache ShardingSphere发起人 & PPMC

热爱开源，目前主导开源项目ShardingSphere(原名Sharding-JDBC)和Elastic-Job。擅长以java为主分布式架构以及以Kubernetes和Mesos为主的云平台方向，推崇优雅代码，对如何写出具有展现力的代码有较多研究。

目前主要精力投入在将ShardingSphere打造为业界一流的金融级数据解决方案之上。ShardingSphere已经进入Apache孵化器，是京东集团首个进入Apache基金会的开源项目，也是Apache基金会首个分布式数据库中间件。

---
姜宁，华为开源能力中心技术专家，Apache ServiceComb项目负责人。前红帽软件首席软件工程师，在企业级开源中间件开发方面有十余年经验，有丰富的Java开发和使用经验，函数式编程爱好者。从2006年开始一直从事Apache开源中间件项目的开发工作，先后参与Apache CXF，Apache Camel，以及Apache ServiceMix的开发。对微服务架构，WebServices，Enterprise Integration Pattern，SOA， OSGi 均有比较深入的研究。

博客地址：https://willemjiang.github.io/

---

冯征，红帽软件工程师。2009年加入红帽软件公司，主要从事事务管理器方面的工作，做为核心开发人员参与了 Narayana 和 BlackTie 项目，在与多个应用服务器（Wildfly, Karaf, Tomcat）和框架（Common DBCP, Spring Boot）的事务处理集成方面有过贡献。从2017年开始参与了Apache ServiceComb项目，目前是PMC成员之一。对于分布式事务处理以及微服务环境中的事务处理，有过深入的研究。

### 导读

相比于数据分片方案的逐渐成熟，集性能、透明化、自动化、强一致、并能适用于各种应用场景于一体的分布式事务解决方案则显得凤毛麟角。基于两（三）阶段提交的分布式事务的性能瓶颈以及柔性事务的业务改造问题，使得分布式事务至今依然是令架构师们头疼的问题。

Apache ShardingSphere（Incubating）不失时机的在2019年初，提供了一个刚柔并济的一体化分布式事务解决方案。如果您的应用系统正在受到这方面的困扰，不妨倒上一杯咖啡，花十分钟阅读此文，说不定会有些收获呢？

### 背景

数据库事务需要满足ACID（原子性、一致性、隔离性、持久性）4个特性。

- 原子性（Atomicity）指事务作为整体来执行，要么全部执行，要么全不执行。

- 一致性（Consistency）指事务应确保数据从一个一致的状态转变为另一个一致的状态。

- 隔离性（Isolation）指多个事务并发执行时，一个事务的执行不应影响其他事务的执行。

- 持久性（Durability）指已提交的事务修改数据会被持久保存。

在单一数据节点中，事务仅限于对单一数据库资源的访问控制，称之为本地事务。几乎所有的成熟的关系型数据库都提供了对本地事务的原生支持。 但是在基于微服务的分布式应用环境下，越来越多的应用场景要求对多个服务的访问及其相对应的多个数据库资源能纳入到同一个事务当中，分布式事务应运而生。

关系型数据库虽然对本地事务提供了完美的ACID原生支持。 但在分布式的场景下，它却成为系统性能的桎梏。如何让数据库在分布式场景下满足ACID的特性或找寻相应的替代方案，是分布式事务的重点工作。

#### 本地事务

在不开启任何分布式事务管理器的前提下，让每个数据节点各自管理自己的事务。 它们之间没有协调以及通信的能力，也并不互相知晓其他数据节点事务的成功与否。 本地事务在性能方面无任何损耗，但在强一致性以及最终一致性方面则力不从心。

#### 两阶段提交

XA协议最早的分布式事务模型是由X/Open国际联盟提出的X/Open Distributed Transaction Processing（DTP）模型，简称XA协议。

基于XA协议实现的分布式事务对业务侵入很小。 它最大的优势就是对使用方透明，用户可以像使用本地事务一样使用基于XA协议的分布式事务。 XA协议能够严格保障事务ACID特性。

严格保障事务ACID特性是一把双刃剑。 事务执行在过程中需要将所需资源全部锁定，它更加适用于执行时间确定的短事务。 对于长事务来说，整个事务进行期间对数据的独占，将导致对热点数据依赖的业务系统并发性能衰退明显。 因此，在高并发的性能至上场景中，基于XA协议两阶段提交类型的分布式事务并不是最佳选择。

#### 柔性事务

如果将实现了ACID的事务要素的事务称为刚性事务的话，那么基于BASE事务要素的事务则称为柔性事务。 BASE是基本可用、柔性状态和最终一致性这3个要素的缩写。

- 基本可用（Basically Available）保证分布式事务参与方不一定同时在线。

- 柔性状态（Soft state）则允许系统状态更新有一定的延时，这个延时对客户来说不一定能够察觉。

- 最终一致性（Eventually consistent）通常是通过消息传递的方式保证系统的最终一致性。

在ACID事务中对一致性和隔离性的要求很高，在事务执行过程中，必须将所有的资源占用。 柔性事务的理念则是通过业务逻辑将互斥锁操作从资源层面上移至业务层面。通过放宽对强一致性和隔离性的要求，只要求当整个事务最终结束的时候，数据是一致的。而在事务执行期间，任何读取操作得到的数据都有可能被改变。这种弱一致性的设计可以用来换取系统吞吐量的提升。

Saga是典型的柔性事务管理器。Sagas这个概念来源于三十多年前的一篇数据库论文[http://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf] ，一个Saga事务是一个由多个短时事务组成的长时的事务。 在分布式事务场景下，我们把一个Saga分布式事务看做是一个由多个本地事务组成的事务，每个本地事务都有一个与之对应的补偿事务。在Saga事务的执行过程中，如果某一步执行出现异常，Saga事务会被终止，同时会调用对应的补偿事务完成相关的恢复操作，这样保证Saga相关的本地事务要么都是执行成功，要么通过补偿恢复成为事务执行之前的状态。

TCC（Try-Cancel/Confirm实现)是另一种柔性事务的协调实现。TCC借助两阶段提交协议提供了一种比较完美的恢复方式。在TCC方式下，cancel补偿显然是在第二阶段需要执行业务逻辑来取消第一阶段产生的后果。Try是在第一阶段执行相关的业务操作，完成相关业务资源的占用，例如预先分配票务资源，或者检查并刷新用户账户信用额度。 在取消阶段释放相关的业务资源，例如释放预先分配的票务资源或者恢复之前占用的用户信用额度。 那我们为什么还要加入确认操作呢？这需要从业务资源的使用生命周期来入手。在try过程中，我们只是占用的业务资源，相关的执行操作只是出于待定状态，只有在确认操作执行完毕之后，业务资源才能真正被确认。

基于ACID的强一致性事务和基于BASE的最终一致性事务都不是银弹，只有在最适合的场景中才能发挥它们的最大长处。可通过下表详细对比它们之间的区别，以帮助开发者进行技术选型。

<center>

|  对比 |本地事务|两阶段提交|柔性事务|
| :--------:   | :-----:  | :----:  | :----:  |
| 业务改造  |无|无|实现相关接口|
|  一致性 |不支持|支持|最终一致|
| 隔离性  |不支持|支持|业务方保证|
| 并发性能  |无影响|严重衰退|略微衰退|
| 适合场景  |业务方处理不一致|短事务 & 低并发|长事务 & 高并发|

</center>

#### 挑战

由于应用的场景不同，需要开发者能够合理的在性能与功能之间权衡各种分布式事务。

两阶段提交与柔性事务的API和功能并不完全相同，在它们之间并不能做到自由的透明切换。在开发决策阶段，就不得不在两阶段提交的事务和柔性事务之间抉择，使得设计和开发成本被大幅增加。

基于XA的两阶段提交事务使用相对简单，但是无法很好的应对互联网的高并发或复杂系统的长事务场景；柔性事务则需要开发者对应用进行改造，接入成本非常高，并且需要开发者自行实现资源占用和反向补偿。

### ShardingSphere的分布式事务

整合现有的成熟事务方案，为本地事务、两阶段提交和柔性事务提供统一的分布式事务接口，并弥补当前方案的不足，提供一站式的分布式事务解决方案是Apache ShardingSphere（Incubating）分布式事务模块的主要设计目标。该模块的名称是sharding-transaction。可以用刚柔并济、自动化和透明化这3个关键词来概括sharding-transaction模块的设计理念和功能呈现。

1.刚柔并济

同时提供基于XA的两阶段提交事务与基于Saga的柔性事务解决方案，并且能够一起配合使用。

2.自动化

XA事务和Saga事务都通过自动化的方式完成，使用方无感知。XA事务无需使用XADataSource接口以及JTA事务管理器；Saga事务也无需用户自行实现补偿接口。

3.透明化

在Apache ShardingSphere（Incubating）的两个接入端——Sharding-JDBC和Sharding-Proxy中，分别提供了面向本地事务接口的封装。使用方完全可以将被ShardingSphere管理的水平分片的多个数据源当成一个数据库使用，通过本地事务API即可实现完全的分布式事务的能力。用户可以透明地在应用中任意切换事务类型。

sharding-transaction模块由sharding-transaction-core，sharding-transaction-2pc和sharding-transaction-base这3个子模块组成。

- sharding-transaction-core:

提供了面向使用者的API与面向开发者的SPI。

- sharding-transaction-2pc:

两阶段提交事务父模块。目前只有sharding-transaction-xa模块，提供了XA协议的支持。未来会引入更多的基于两阶段提交的事务类型，如：percolator，参见：

[https://storage.googleapis.com/pub-tools-public-publication-data/pdf/36726.pdf]。

- sharding-transaction-base:

柔性事务父模块。目前只有sharding-transaction-saga模块，采用Apache ServiceComb Saga Actuator提供的Saga执行器提供柔性事务支持，并在其基础之上提供了反向SQL和快照的能力，并由此实现自动逆向补偿功能。

下面将对ShardingSphere的XA和Saga事务模块的功能亮点进行说明。

#### XA事务——三大XA事务管理器共护航

成熟的XA事务管理器非常多，Apache ShardingSphere（Incubating）并未选择重新造轮子，而是寄望于打造一个生态，将合适的轮子有机地整合在一起，提供成熟稳定的分布式事务处理能力。它的主要功能如下：

**1.复用成熟引擎，自动切换底层实现**

Sharding-transaction-xa模块进一步定义了面向XA事务管理器开发者的SPI，开发者仅需实现SPI定义的接口，即可自动加入至Apache ShardingSphere（Incubating）生态，作为其XA事务管理器。

Apache ShardingSphere（Incubating）官方目前实现了基于Atomikos和Bitronix的SPI，并且邀请了 Redhat JBoss 的XA事务引擎Narayana [https://github.com/jbosstm/narayana] 开发团队实现了JBoss的SPI。用户可以自行的在Atomikos，Bitronix和Narayana间选择自己喜欢的XA事务管理器。



受限于Apache基金会项目License的原因，Apache ShardingSphere（Incubating）将采用Apache协议的Atomikos作为其默认实现，关于基于LGPL协议的Bitronix和基于LGPL协议的Narayana，用户可以自行引用相应jar包至项目的classpath即可。



如果这3个XA事务管理器仍未满足用户需求，开发者则可通过扩展SPI来实现定制化的XA事务管理器。



**2.数据源透明化自动接入**



Apache ShardingSphere（Incubating）能够自动将XADataSource作为数据库驱动的数据源接入XA事务管理器。而针对于使用DataSource作为数据库驱动的应用，用户也无需改变其编码以及配置，Apache ShardingSphere（Incubating）通过自动适配的方式，在中间件内部将其转化为支持XA协议的XADataSource和XAConnection，并将其作为XA资源注册到底层的XA事务管理器中。



XA模块的架构图如下：


![](https://shardingsphere.apache.org/blog/img/solution1.jpg)

#### Saga事务—跨越柔性事务限制，实现自动补偿

在柔性事务中，每一次对数据库的更新操作都将数据真正的提交至数据库，以达到高并发系统中最佳资源释放的效果。当数据出现问题需要回滚时，通过柔性事务管理器保持数据的最终一致性以及隔离行为。Apache ShardingSphere（Incubating）采用Apache ServiceComb Saga Actuator [https://github.com/apache/servicecomb-saga-actuator] 作为Saga事务管理器，它的主要功能如下：



**1. 自动反向补偿**



Saga定义了一个事务中的每个子事务都有一个与之对应的反向补偿操作。由Saga事务管理器根据程序执行结果生成一张有向无环图，并在需要执行回滚操作时，根据该图依次按照相反的顺序调用反向补偿操作。Saga事务管理器只用于控制何时重试，合适补偿，并不负责补偿的内容，补偿的具体操作需要由开发者自行提供。



另一个柔性事务管理器TCC与Saga理念相似，均需要由使用方开发者提供补偿操作。除了补偿，TCC还提供了资源占用的能力，但也需要由使用方开发者提供资源占用操作。虽然功能上强于Saga，但TCC的使用成本较之Saga也更高。



由使用方开发者提供资源占用和补偿操作，这就使得柔性事务的解决方案始终难于大规模的在业务系统中落地。并且由于业务系统的介入，使得柔性事务框架的使用范畴始终定位于服务而非数据库，数据库能够直接使用的成熟的柔性事务管理器目前还不多见。



Apache ShardingSphere（Incubating）采用反向SQL技术，将对数据库进行更新操作的SQL自动生成数据快照以及反向SQL，并交由Apache ServiceComb Saga Actuator执行，使用方则无需再关注如何实现补偿方法，将柔性事务管理器的应用范畴成功的定位回了事务的本源——数据库层面。



对于能够处理复杂查询语句的Apache ShardingSphere（Incubating）SQL解析引擎来说，插入/更新/删除等语句解析难度则要小很多；ShardingSphere是通过拦截用户执行的SQL进行数据分片的，所有的SQL都能够被其直接管控。因此将反向SQL和补偿能力与Apache ServiceComb Saga Actuator相结合，达到了自动化柔性事务的能力，是数据分片和柔性事务结合的典范。



Saga模块的架构图如下：

![](https://shardingsphere.apache.org/blog/img/solution2.jpg)

#### 接入端—面向原生事务接口的分布式事务

Apache ShardingSphere（Incubating）的目标是像使用一个数据库一样使用分片后的多数据库，在事务模块，这个目标依然适用。无论被ShardingSphere所管理的数据库如何分片，面向开发者的逻辑数据库始终只有一个。因此，ShardingSphere的事务接口依然是原生的本地事务接口，即JDBC的java.sql.Connection的setAutoCommit, commit和rollback方法；以及面向数据库事务管理器的begin, commit和rollback语句。在用户调用原生本地事务接口的同时，ShardingSphere则通过sharding-transaction模块保证后端分片数据库的分布式事务。



由于原生的事务接口并不支持事务类型，因此ShardingSphere提供了3种方式供使用者切换事务类型。



1.通过SCTL（sharding-ctl，即ShardingSphere提供的数据库管理命令）切换当前事务类型。以SQL执行的方式输入即可，适用于Sharding-JDBC和Sharding-Proxy。例如：SCTL:SET TRANSACTION_TYPE=BASE

2.通过Threadlocal切换当前事务类型，适用于Sharding-JDBC。例如：TransactionTypeHolder.set (TransactionType.XA)

3.通过元注解，并与Spring配合使用切换当前事务类型，适用于Sharding-JDBC和Sharding-Proxy。例如：@ShardingTransactionType (TransactionType.BASE)

### 线路规划

分布式事务模块在github的开发分支 [https://github.com/apache/incubator-shardingsphere] 已经基本可用，将随着4.0.0.M1的版本发布，这也将是ShardingSphere进入Apache基金会孵化器之后的第一个发布版本。分布式事务是数据分片以及微服务架构的重要组成部分，也是Apache ShardingSphere（Incubating）的关注重心，发布之后仍将继续完善，线路规划如下。

#### 事务隔离引擎

在SQL反向引擎稳定之后，柔性事务的重点将放在打造事务隔离之上。由于事务的隔离性并非Saga所规划的范畴，因此Apache ShardingSphere（Incubating）会在Saga之外将其完善，与SQL反向引擎一起作为整个柔性事务的组成部分。



Apache ShardingSphere（Incubating）将通过乐观锁、悲观锁、无隔离等几种策略，做到读已提交、读未提交、可重复读以及序列化等隔离级别的一一支持。并通过多版本快照进一步提升系统的并发度。

#### 对外XA事务接口

Apache ShardingSphere（Incubating）的两个接入端Sharding-JDBC和Sharding-Proxy在支持自身的内部事务问题之后，将提供融入与其他数据源一起作为被JTA等分布式事务管理器管理的能力。



实现对外XA事务接口之后，Sharding-JDBC的DataSource将实现XADataSource接口，提供与其他数据源共同加入到一个XA事务的可能；Sharding-Proxy的数据库协议也将实现基于XA的两阶段提交协议；使其可以成为被XA所加载的资源管理器。



除此之外，ShardingSphere还会实现XA协议的recovery部分，即在事务处理器出现崩溃的情况时，可以有能力提供in-doubt transactions来实现事务恢复。


### 总结

Apache ShardingSphere（Incubating）提供的分布式事务能力可以通过下表总结一下，读者不妨与文章开始时的表格对比一下，看看ShardingSphere的分布式事务模块所带来的变化。

 
![](https://shardingsphere.apache.org/blog/img/solution3.jpg)

在高速发展的Apache ShardingSphere（Incubating）中，分布式事务的雏形已成，我们会尽快将其打造为可用的产品，并持续为社区提供优质解决方案。对于一篇不算短的文章，阅读完此文的您，相信一定对这个领域有一定兴趣。不妨先尝试一下，是否满足您的预期？或者干脆加入我们的社区，一起打造更完善的分布式事务方案。 
