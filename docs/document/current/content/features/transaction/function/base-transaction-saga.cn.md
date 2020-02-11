+++
pre = "<b>3.4.2.3 </b>"
toc = true
title = "柔性事务-Saga"
weight = 3
+++

## 功能

* 完全支持跨库事务
* 支持失败SQL重试及最大努力送达
* 支持反向SQL、自动生成更新快照以及自动补偿
* 默认使用关系型数据库进行快照及事务日志的持久化，支持使用SPI的方式加载其他类型的持久化

## 不支持项

* 暂不支持资源隔离
* 暂不支持服务宕机后，自动恢复提交中的commit和rollback

## 支持情况

ShardingSphere的柔性事务已通过第三方SPI实现[Saga](https://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf)事务，Saga引擎使用[Servicecomb-Saga](https://github.com/apache/servicecomb-saga-actuator)。

#### 注意
* 反向SQL需要**主键**，请确保在表结构中定义**主键**。
* 对于`INSERT`语句， 需要在SQL中显示插入**主键值**，如`INSERT INTO ${table_name} (id, value, ...) VALUES (11111, '', ....) (其中id为表主键)`。
* 若需要自动生成主键，可使用ShardingSphere的分布式主键。



