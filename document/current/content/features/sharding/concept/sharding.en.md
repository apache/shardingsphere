+++
toc = true
title = "Sharding"
weight = 2
+++

## Sharding Key

The database field used in sharding refers to the key field in horizontal sharding of the database (table). 
For example, in last number modulo of order ID sharding, order ID is taken as the sharding key. 
The full route executed when there is no sharding field in SQL has a poor performance but supports multiple sharding fields.

## Sharding Algorithm

The sharding of data can be achieved by sharding algorithms through `=`, `BETWEEN` and `IN`. 
Highly flexible, the sharding algorithm needs to be implemented by developers themselves.

Currently, 4 kinds of sharding algorithms are available. 
Since the sharding algorithm and the achievement of business are highly correlated, instead of providing built-in sharding algorithms, 
it extracts all kinds of situations by sharding strategies to provide higher abstraction and the interface for developers to implement sharding algorithm by themselves.

- Precise Sharding Algorithm

`PreciseShardingAlgorithm` is to process the sharding case in which single sharding key `=` and `IN` are used, together with `StandardShardingStrategy`.

- Range Sharding Algorithm

`RangeShardingAlgorithm` is to process the sharding case in which single sharding key `BETWEEN AND` is used, together with `StandardShardingStrategy`.

- Complex Keys Sharding Algorithm

`ComplexKeysShardingAlgorithm` is to process the sharding case in which multiple sharding keys are used, together with `ComplexShardingStrategy`. 
It has a relatively complex logic that requires developers to deal by themselves.
 
- Hint Sharding Algorithm

`HintShardingAlgorithm` is to process the sharding case in which Hint is used, together with `HintShardingStrategy`.

## Sharding Strategy

It includes the sharding key and the sharding algorithm, and the latter one is extracted out for its independence. 
Only sharding key + sharding algorithm, i.e., the sharding strategy, can be used in sharding operation. For now, 5 kinds of sharding strategies are available.

- Standard Sharding Strategy

`StandardShardingStrategy` provides support for the sharding operation of `=`, `IN` and `BETWEEN AND` in SQL. 
`StandardShardingStrategy` only supports single sharding keys and provides two sharding algorithms of `PreciseShardingAlgorithm` and `RangeShardingAlgorithm`. 
`PreciseShardingAlgorithm` is compulsory and used to operate the sharding of `=` and `IN`. 
`RangeShardingAlgorithm` is optional and used to operate the sharding of `BETWEEN AND`. 
`BETWEEN AND` in SQL will operate by way of all data node route without the configuration of `RangeShardingAlgorithm`.

- Complex Sharding Strategy

`ComplexShardingStrategy` provides support for the sharding operation of `=`, `IN` and `BETWEEN AND` in SQL. 
`ComplexShardingStrategy` supports multiple sharding keys, but since their relationships are so complex that there is not too much encapsulation, 
the combination of sharding keys and sharding operators are in the algorithm interface and achieved by developers with the most flexibility.

- Inline Sharding Strategy

Using Groovy expressions, `InlineShardingStrategy` provides single-key support for the sharding operation of `=` and `IN` in SQL. 
Simple sharding algorithms can be used through a simple configuration to avoid laborious Java code developments. 
For example, `t_user${u_id % 8}` means table t_user is divided into 8 tables according to u_id, with table names from t_user0 to t_user7.

- Hint Sharding Strategy

`HintShardingStrategy` refers to the sharding strategy by Hint rather than SQL parsing.

- None sharding strategy

`NoneShardingStrategy` refers to the strategy with no sharding.

## SQL Hint

In the case that the `ShardingColumn` is not decided by SQL but other outer conditions, SQL Hint can be used flexibly to inject `ShardingColumn`. 
For example, in the internal system, databases are divided according to the staff’s ID, but this column does not exist in the database. 
SQL Hint can be used by two ways, Java API and SQL comment (to be finished).
