+++
title = "Hint"
weight = 6
+++

## Motivation

Apache ShardingSphere can be compatible with SQL in way of parsing SQL statements and extracting columns and values to shard. 
If SQL does not have sharding conditions, it is impossible to shard without full route.

In some applications, sharding conditions are not in SQL but in external business logic. 
So it requires to  designate sharding result externally, which is referred to as `Hint` in ShardingSphere.

## Mechanism

Apache ShardingSphere uses `ThreadLocal` to manage sharding key values. Users can program to add sharding conditions to `HintManager`, but the condition is only effective within the current thread.

In addition to the programming method, Apache ShardingSphere also plans to cite Hint through special notation in SQL, so that users can use that function in a more transparent way.

The SQL designated with sharding hint will ignore the former sharding logic but directly route to the designated node.
