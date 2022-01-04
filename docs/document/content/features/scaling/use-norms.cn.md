+++
title = "使用规范"
weight = 2
+++

## 支持项

* 将外围数据迁移至 Apache ShardingSphere 所管理的数据库；
* 将 Apache ShardingSphere 的数据节点进行扩容或缩容。

## 不支持项

* 无主键表扩缩容；
* 复合主键表扩缩容；
* 不支持在当前存储节点之上做迁移，需要准备一个全新的数据库集群作为迁移目标库。
