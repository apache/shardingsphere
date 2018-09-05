+++
toc = true
title = "SQL"
weight = 1
+++

## 逻辑表

对于水平拆分的数据库(表)的同一类表的总称。例：订单数据根据主键尾数拆分为10张表,分别是t_order_0到t_order_9，他们的逻辑表名为t_order。

## 真实表

在分片的数据库中真实存在的物理表。即上个示例中的t_order_0到t_order_9。

## 数据节点

数据分片的最小单元。由数据源名称和数据表组成，例：ds_0.t_order_0。

## 绑定表

指分片规则一致的主表和子表。例如：t_order表和t_order_item表，均按照订单ID分片，则此两张表互为绑定表关系。绑定表之间的多表关联查询不会出现笛卡尔积关联，关联查询效率将大大提升。举例说明，如果SQL为：

```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

在不配置绑定表关系时，假设分片键order_id将数值10路由至第0片，将数值11路由至第1片，那么路由后的SQL应该为4条，它们呈现为笛卡尔积：

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_0 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

在配置绑定表关系后，路由的SQL应该为2条：

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

其中表t_order在FROM的最左侧，Sharding-Sphere将会以它作为本次查询的主表。所有路由计算将会只使用主表的分片策略，那么t_order_item表的分片计算将会使用t_order的条件。


其中t_order在FROM的最左侧，Sharding-Sphere将会以它作为整个绑定表的主表。所有路由计算将会只使用主表的策略，那么t_order_item表的分片计算将会使用t_order的条件。故绑定表之间的分区键要完全相同。

## 逻辑索引

某些数据库（如：PostgreSQL）不允许同一个库存在名称相同索引，某些数据库（如：MySQL）则允许只要同一个表中不存在名称相同的索引即可。逻辑索引用于同一个库不允许出现相同索引名称的分表场景，需要将同库不同表的索引名称改写为索引名 + 表名，改写之前的索引名称成为逻辑索引。
