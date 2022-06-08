+++
title = "柔性事务"
weight = 3
+++

## 支持项

* 支持数据分片后的跨库事务；
* 支持 RC 隔离级别；
* 通过 undo 快照进行事务回滚；
* 支持服务宕机后的，自动恢复提交中的事务。

## 不支持项

* 不支持除 RC 之外的隔离级别。

## 待优化项

* Apache ShardingSphere 和 SEATA 重复 SQL 解析。
