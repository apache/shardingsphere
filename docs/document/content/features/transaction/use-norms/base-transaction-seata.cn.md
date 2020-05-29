+++
title = "Seata 柔性事务"
weight = 3
+++

## 支持项

* 支持数据分片后的跨库事务；
* 支持RC隔离级别；
* 通过undo快照进行事务回滚；
* 支持服务宕机后的，自动恢复提交中的事务。

## 不支持项

* 不支持除RC之外的隔离级别。

## 待优化项

* Apache ShardingSphere 和 Seata 重复 SQL 解析。
