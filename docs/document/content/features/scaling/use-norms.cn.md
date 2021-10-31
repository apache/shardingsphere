+++
title = "使用规范"
weight = 2
+++

## 支持项

* 将外围数据迁移至 Apache ShardingSphere 所管理的数据库；
* 将 Apache ShardingSphere 的数据节点进行扩容或缩容。

## 不支持项

* 不支持无主键表的扩容和缩容，主键必须是单字段。
* 不支持在当前proxy使用中的数据库集群做迁移，需要准备一个新的数据库集群作为迁移目标库。
