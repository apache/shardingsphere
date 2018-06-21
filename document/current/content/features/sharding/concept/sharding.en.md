+++
toc = true
title = "Sharding"
weight = 2
+++

## Sharding Column

The sharding field. e.g. To split tables by using modulo operation for the mantissa of the order ID, the sharding column is order ID. It also supports multiple sharding columns. Notice: If there is no sharding column in query SQL, all tables will be route to all data nodes which is in poor performance.

## Sharding Algorithm

Supporting =, BETWEEN and IN algorithms. Sharding algorithm currently need to be implemented by users. There are 4 types of sharding algorithms.

- Precise sharding algorithm

PreciseShardingAlgorithm, used to single sharding column deal with `=` and `IN` in SQL, use with StandardShardingStrategy together.

- Range sharding algorithm

RangeShardingAlgorithm, used to single sharding column deal with `BETWEEN AND` in SQL, use with StandardShardingStrategy together.

- Complex keys sharding algorithm

ComplexKeysShardingAlgorithm, used to multiple sharding columns deal with `=`, `IN` and `BETWEEN AND`, use with ComplexShardingStrategy together.

- Hint sharding algorithm

HintShardingAlgorithm, used to hint sharding, use with HintShardingStrategy together.

## Sharding Strategy

Include sharding columns and sharding algorithms. There are 5 types of sharding strategies.

- Standard sharding strategy

Support `=`, `IN` and `BETWEEN AND` in SQL with single sharding column. It provide PreciseShardingAlgorithm and RangeShardingAlgorithm, PreciseShardingAlgorithm is required, used to deal with `=` and `IN`. RangeShardingAlgorithm is optional, used to deal with `BETWEEN AND`, if absent, `BETWEEN AND` will not trigger sharding, just route to all data nodes.

- Complex sharding strategy

Support `=`, `IN` and `BETWEEN AND` in SQL with multiple sharding columns. Because of complicated with multiple sharding columns, end users need to process by themselves.

- Inline sharding strategy

Use groovy expression, supporting  `=` and `IN` sharding in SQL, for single sharding column only. For simple sharding algorithm, use inline expression it best practice to avoid java codes. For example: `t_user${u_id % 8}` means table t_user sharding by u_id, the result of table appendix is u_id mod 8, name of actual tables are from `t_user0` to `t_user7`.

- Hint sharding strategy

Use hint but not SQL to indicate sharding result.

- None sharding strategy

Do not sharding.

## SQL Hint

In some cases that ShardingColumn is decided by business conditions, not by certain SQL, then you can use SQL Hint to flexibly achieve injection of ShardingColumn. e.g. If you want to split database according to the employees' ID, but ID column not exists in tables, then you can use SQL Hint to do data sharding. ThreadLocal or SQL annotations(TO DO) method can be used to make SQL Hint.
