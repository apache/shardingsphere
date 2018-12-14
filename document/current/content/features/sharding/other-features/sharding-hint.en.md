+++
toc = true
title = "Sharding Hint"
weight = 3
+++

## Motivation

Sharding by sharding-column and sharding-value which parsed from SQL is a good way to compatible with original database access layer. 
But if the sharding condition is not exist in SQL, then will route to all data nodes.

In some special scenario, sharding condition exist at outside logic, not in SQL. 
So there need a new route method for this, in ShardingSphere, we call it as `Hint`.

## Mechanism

ShardingSphere use `ThreadLocal` to manage sharding-columns and sharding-values. 
Developers can use `HintManager` to add sharding conditions by coding, sharding conditions effective only on current thread.

Expect coding, ShardingSphere plan to add hint into SQL comments, make developers use it easier.

When using hint, SQL will ignore original sharding strategy, route to indicated data nodes directly. 
