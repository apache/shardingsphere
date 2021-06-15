+++
title = "XA两阶段事务"
weight = 1
+++

两阶段事务提交采用的是 X/OPEN 组织所定义的[DTP模型](http://pubs.opengroup.org/onlinepubs/009680699/toc.pdf)所抽象的 AP（应用程序）, TM（事务管理器）和 RM（资源管理器） 概念来保证分布式事务的强一致性。
其中 TM 与 RM 间采用 XA 的协议进行双向通信。
与传统的本地事务相比，XA 事务增加了准备阶段，数据库除了被动接受提交指令外，还可以反向通知调用方事务是否可以被提交。
`TM` 可以收集所有分支事务的准备结果，并于最后进行原子提交，以保证事务的强一致性。

![两阶段提交模型](https://shardingsphere.apache.org/document/current/img/transaction/2pc-tansaction-modle.png)

Java 通过定义 JTA 接口实现了 XA 模型，JTA 接口中的 `ResourceManager` 需要数据库厂商提供 XA 驱动实现，
`TransactionManager` 则需要事务管理器的厂商实现，传统的事务管理器需要同应用服务器绑定，因此使用的成本很高。
而嵌入式的事务管器可以以 jar 包的形式提供服务，同 Apache ShardingSphere 集成后，可保证分片后跨库事务强一致性。

通常，只有使用了事务管理器厂商所提供的 XA 事务连接池，才能支持 XA 的事务。Apache ShardingSphere 在整合 XA 事务时，采用分离 XA 事务管理和连接池管理的方式，做到对应用程序的零侵入。
