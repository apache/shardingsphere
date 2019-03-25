+++
pre = "<b>3.4.3.2 </b>"
toc = true
title = "柔性事务"
weight = 3
+++

## 设计

![柔性事务设计](https://shardingsphere.apache.org/document/current/img/transaction/transaction-base-design_cn.png)

ShardingSphere的柔性事务需要实现Sharding事务管理器的SPI接口，用于管理事务的生命周期。
同时柔性事务还需要通过ShardingSphere的内部SQL Hook，获取与SQL相关的必要信息，帮助事务管理器控制分布式事务。
事务隔离引擎还处于计划阶段，因此柔性事务暂不支持资源隔离功能。