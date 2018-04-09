+++
toc = true
title = "核心概念"
weight = 1
+++


## LogicTable
逻辑表，对于水平拆分的数据库(表)的同一类表的总称。例：订单数据根据主键尾数拆分为10张表,分别是t_order_0到t_order_9，他们的逻辑表名为t_order。

## ActualTable
在分片的数据库中真实存在的物理表。即上个示例中的t_order_0到t_order_9。

## DataNode
数据分片的最小单元。由数据源名称和数据表组成，例：ds_1.t_order_0。配置时默认各个分片数据库的表结构均相同，直接配置逻辑表和真实表对应关系即可。如果各数据库的表结果不同，可使用ds.actual_table配置。

## BindingTable
指在任何场景下分片规则均一致的主表和子表。例：订单表和订单项表，均按照订单ID分片，则此两张表互为BindingTable关系。BindingTable关系的多表关联查询不会出现笛卡尔积关联，关联查询效率将大大提升。
那么在进行SQL路由时，如果SQL为：
```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?
```
其中t_order在FROM的最左侧，Sharding-JDBC将会以它作为整个绑定表的主表。所有路由计算将会只使用主表的策略，那么t_order_item表的分片计算将会使用t_order的条件。故绑定表之间的分区键要完全相同。

## ShardingColumn
分片字段。用于将数据库(表)水平拆分的关键字段。例：订单表订单ID分片尾数取模分片，则订单ID为分片字段。SQL中如果无分片字段，将执行全路由，性能较差。Sharding-JDBC支持多分片字段。

## ShardingAlgorithm
分片算法。Sharding-JDBC通过分片算法将数据分片，支持通过等号、BETWEEN和IN分片。分片算法目前需要业务方开发者自行实现，可实现的灵活度非常高。未来Sharding-JDBC也将会实现常用分片算法，如range，hash和tag等。

## LogicIndex
数据分片的逻辑索引名称，DDL语句中水平拆分的表，同一类表的总称。例：订单数据根据主键尾数拆分为10张表,分别是t_order_0到t_order_9，他们的逻辑表名为t_order，对于DROP INDEX t_order_index语句，
需在TableRule中配置逻辑索引t_order_index。

## 数据源策略与表策略

Sharding-JDBC认为对于分片策略存有两种维度。
- 数据源分片策略DatabaseShardingStrategy：数据被分配的目标数据源。
- 表分片策略TableShardingStrategy：数据被分配的目标表，该目标表存在与该数据的目标数据源内。故表分片策略是依赖与数据源分片策略的结果的。
这里注意的是两种策略的API完全相同，以下针对策略API的讲解将适用于这两种策略。

## SQL Hint
对于分片字段非SQL决定，而由其他外置条件决定的场景，可使用SQL Hint灵活的注入分片字段。例：内部系统，按照员工登录ID分库，而数据库中并无此字段。SQL Hint支持通过ThreadLocal和SQL注释(待实现)两种方式使用。

## Config Map
通过ConfigMap可以配置分库分表或读写分离数据源的元数据，可通过调用ConfigMapContext.getInstance()获取ConfigMap中的shardingConfig和masterSlaveConfig数据。例：如果机器权重不同则流量可能不同，可通过ConfigMap配置机器权重元数据。

## 分片键

分片键是分片策略的第一个参数。分片键表示的是SQL语句中WHERE中的条件列。分片键可以配置多个。

## 分片算法

Sharding-JDBC提供了5种分片策略。由于分片算法和业务实现紧密相关，因此Sharding-JDBC并未提供内置分片算法，而是通过分片策略将各种场景提炼出来，提供更高层级的抽象，并提供接口让应用开发者自行实现分片算法。

- StandardShardingStrategy

标准分片策略。提供对SQL语句中的=, IN和BETWEEN AND的分片操作支持。StandardShardingStrategy只支持单分片键，提供PreciseShardingAlgorithm和RangeShardingAlgorithm两个分片算法。PreciseShardingAlgorithm是必选的，用于处理=和IN的分片。RangeShardingAlgorithm是可选的，用于处理BETWEEN AND分片，如果不配置RangeShardingAlgorithm，SQL中的BETWEEN AND将按照全库路由处理。

- ComplexShardingStrategy

复合分片策略。提供对SQL语句中的=, IN和BETWEEN AND的分片操作支持。ComplexShardingStrategy支持多分片键，由于多分片键之间的关系复杂，因此Sharding-JDBC并未做过多的封装，而是直接将分片键值组合以及分片操作符交于算法接口，完全由应用开发者实现，提供最大的灵活度。

- InlineShardingStrategy

Inline表达式分片策略。使用Groovy的Inline表达式，提供对SQL语句中的=和IN的分片操作支持。InlineShardingStrategy只支持单分片键，对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的Java代码开发，如: t_user_${user_id % 8} 表示t_user表按照user_id按8取模分成8个表，表名称为t_user_0到t_user_7。

- HintShardingStrategy

通过Hint而非SQL解析的方式分片的策略。

- NoneShardingStrategy

不分片的策略。