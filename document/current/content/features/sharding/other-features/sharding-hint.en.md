+++
toc = true
title = "Sharding Hint"
weight = 3
+++

## Motivation

ShardingSphere can be compatible with SQL in way of extracting sharding columns and values to shard after parsing SQL statements. 
Without sharding conditions in SQL statement, it is impossible to shard but requires full route.

In some application situations, sharding conditions do not exist in SQL but in external business logic. 
So it requires to provide an externally designated sharding result method, which is referred to as `Hint` in ShardingSphere.

## Mechanism

ShardingSphere uses `ThreadLocal` to manage sharding key values. 
Users can add sharding conditions to `HintManager` through programming, but the condition is only effective within the current thread.

In addition to using sharding hint through programming, ShardingSphere also plans to cite Hint through special notation in SQL, so that users can use that function in a more transparent way.

The SQL that is designated with sharding hint will ignore the former sharding logic but will directly route to the designated data node.
