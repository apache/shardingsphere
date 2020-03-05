+++
pre = "<b>3.4.1.1 </b>"
toc = true
title = "两阶段事务-XA"
weight = 1
+++

两阶段事务提交采用的是X/OPEN组织所定义的[DTP模型](http://pubs.opengroup.org/onlinepubs/009680699/toc.pdf)，通过抽象出来的`AP`, `TM`, `RM`的概念可以保证事务的强一致性。
其中`TM`和`RM`间采用`XA`的协议进行双向通信。
与传统的本地事务相比，XA事务增加了prepare阶段，数据库除了被动接受提交指令外，还可以反向通知调用方事务是否可以被提交。
因此`TM`可以收集所有分支事务的prepare结果，最后进行原子的提交，保证事务的强一致性。

![两阶段提交模型](https://shardingsphere.apache.org/document/current/img/transaction/2pc-tansaction-modle_cn.png)

Java通过定义JTA接口实现了XA的模型，JTA接口里的`ResourceManager`需要数据库厂商提供XA的驱动实现，而`TransactionManager`则需要事务管理器的厂商实现，传统的事务管理器需要同应用服务器绑定，因此使用的成本很高。
而嵌入式的事务管器可以以jar包的形式提供服务，同ShardingSphere集成后，可保证分片后跨库事务强一致性。

通常，只有使用了事务管理器厂商所提供的XA事务连接池，才能支持XA的事务。ShardingSphere整合XA事务时，分离了XA事务管理和连接池管理，这样接入XA时，可以做到对业务的零侵入。
