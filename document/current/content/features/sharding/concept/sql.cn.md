+++
toc = true
title = "SQL"
weight = 1
+++

## 逻辑表

对于水平拆分的数据库(表)的同一类表的总称。例：订单数据根据主键尾数拆分为10张表,分别是t_order0到t_order9，他们的逻辑表名为t_order。

## 真实表

在分片的数据库中真实存在的物理表。即上个示例中的t_order0到t_order9。

## 数据节点

数据分片的最小单元。由数据源名称和数据表组成，例：ds1.t_order0。配置时默认各个分片数据库的表结构均相同，直接配置逻辑表和真实表对应关系即可。如果各数据库的表结果不同，可使用ds.actual_table配置。

## 绑定表

指在任何场景下分片规则均一致的主表和子表。例：订单表和订单项表，均按照订单ID分片，则此两张表互为BindingTable关系。BindingTable关系的多表关联查询不会出现笛卡尔积关联，关联查询效率将大大提升。
在进行SQL路由时，如果SQL为：

```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?
```

其中t_order在FROM的最左侧，Sharding-Sphere将会以它作为整个绑定表的主表。所有路由计算将会只使用主表的策略，那么t_order_item表的分片计算将会使用t_order的条件。故绑定表之间的分区键要完全相同。

## 逻辑索引

某些数据库（如：PostgreSQL）不允许同一个库存在名称相同索引，某些数据库（如：MySQL）则允许只要同一个表中不存在名称相同的索引即可。逻辑索引用于同一个库不允许出现相同索引名称的分表场景，需要将同库不同表的索引名称改写为索引名 + 表名，改写之前的索引名称成为逻辑索引。
