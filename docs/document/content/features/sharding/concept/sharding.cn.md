+++
title = "分片"
weight = 2
+++

## 分片键

用于分片的数据库字段，是将数据库（表）水平拆分的关键字段。例：将订单表中的订单主键的尾数取模分片，则订单主键为分片字段。
SQL 中如果无分片字段，将执行全路由，性能较差。
除了对单分片字段的支持，Apache ShardingSphere 也支持根据多个字段进行分片。

## 分片算法

通过分片算法将数据分片，支持通过 `=`、`>=`、`<=`、`>`、`<`、`BETWEEN` 和 `IN` 分片。
分片算法需要应用方开发者自行实现，可实现的灵活度非常高。

目前提供4种分片算法。
由于分片算法和业务实现紧密相关，因此并未提供内置分片算法，而是通过分片策略将各种场景提炼出来，提供更高层级的抽象，并提供接口让应用开发者自行实现分片算法。

- 精确分片算法

对应 PreciseShardingAlgorithm，用于处理使用单一键作为分片键的 `=` 与 `IN` 进行分片的场景。需要配合 StandardShardingStrategy 使用。

- 范围分片算法

对应 RangeShardingAlgorithm，用于处理使用单一键作为分片键的 `BETWEEN AND`、`>`、`<`、`>=`、`<=`进行分片的场景。需要配合 StandardShardingStrategy 使用。

- 复合分片算法

对应 ComplexKeysShardingAlgorithm，用于处理使用多键作为分片键进行分片的场景，包含多个分片键的逻辑较复杂，需要应用开发者自行处理其中的复杂度。需要配合 ComplexShardingStrategy 使用。

- Hint分片算法

对应 HintShardingAlgorithm，用于处理使用 `Hint` 行分片的场景。需要配合 HintShardingStrategy 使用。

## 分片策略

包含分片键和分片算法，由于分片算法的独立性，将其独立抽离。真正可用于分片操作的是分片键 + 分片算法，也就是分片策略。目前提供 5 种分片策略。

- 标准分片策略

对应 StandardShardingStrategy。提供对 SQ L语句中的 `=`, `>`, `<`, `>=`, `<=`, `IN` 和 `BETWEEN AND` 的分片操作支持。
StandardShardingStrategy 只支持单分片键，提供 PreciseShardingAlgorithm 和 RangeShardingAlgorithm 两个分片算法。
PreciseShardingAlgorithm 是必选的，用于处理 `=` 和 `IN` 的分片。
RangeShardingAlgorithm 是可选的，用于处理 `BETWEEN AND`, `>`, `<`, `>=`, `<=`分片，如果不配置 RangeShardingAlgorithm，SQL 中的 `BETWEEN AND` 将按照全库路由处理。

- 复合分片策略

对应 ComplexShardingStrategy。复合分片策略。提供对 SQL 语句中的 `=`, `>`, `<`, `>=`, `<=`, `IN` 和 `BETWEEN AND` 的分片操作支持。
ComplexShardingStrategy 支持多分片键，由于多分片键之间的关系复杂，因此并未进行过多的封装，而是直接将分片键值组合以及分片操作符透传至分片算法，完全由应用开发者实现，提供最大的灵活度。

- 行表达式分片策略

对应 InlineShardingStrategy。使用 Groovy 的表达式，提供对 SQL 语句中的 `=` 和 `IN` 的分片操作支持，只支持单分片键。
对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的 Java 代码开发，如: `t_user_$->{u_id % 8}` 表示 `t_user` 表根据 `u_id` 模 8，而分成 8 张表，表名称为 `t_user_0` 到 `t_user_7`。
详情请参见[行表达式](/cn/features/sharding/concept/inline-expression/)。

- Hint分片策略

对应 HintShardingStrategy。通过 Hint 指定分片值而非从 SQL 中提取分片值的方式进行分片的策略。

- 不分片策略

对应 NoneShardingStrategy。不分片的策略。

## SQL Hint

对于分片字段非 SQL 决定，而由其他外置条件决定的场景，可使用 SQL Hint 灵活的注入分片字段。
例：内部系统，按照员工登录主键分库，而数据库中并无此字段。SQL Hint 支持通过 Java API 和 SQL 注释（待实现）两种方式使用。
详情请参见[强制分片路由](/cn/features/sharding/concept/hint/)。
