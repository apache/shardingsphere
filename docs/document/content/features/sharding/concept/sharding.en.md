+++
title = "Sharding"
weight = 3
+++

## Sharding Key

Column used to determine database (table) sharding.
For example, in last number modulo of order ID sharding, order ID is taken as the sharding key. 
The full route executed when there is no sharding column in SQL has a poor performance. 
Besides single sharding column, Apache ShardingSphere also supports multiple sharding columns.

## Sharding Algorithm

Data sharding can be achieved by sharding algorithms through `=`, `>=`, `<=`, `>`, `<`, `BETWEEN` and `IN`.
It can be implemented by developers themselves, or using built-in syntactic sugar of Apache ShardingSphere, with high flexibility.

### Auto Sharding Algorithm

It provides syntactic sugar for sharding algorithm.
It used to manage all data nodes automatically, user do not care about the topology of physical data nodes.
It includes lots of implementation for Mod, Hash, Range and Time Interval etc.

### User-Defined Sharding Algorithm

It provides interfaces for developers to implement the sharding algorithm related to business implementation, and allows users to manage the physical topology physical data nodes by themselves.
It includes:

- Standard Sharding Algorithm

It is to process the sharding case in which single sharding keys `=`, `IN`, `BETWEEN AND`, `>`, `<`, `>=`, `<=` are used.

- Complex Keys Sharding Algorithm

It is to process the sharding case in which multiple sharding keys are used.
It has a relatively complex logic that requires developers to deal by themselves.

- Hint Sharding Algorithm

It is to process the sharding case in which Hint is used.

## Sharding Strategy

It includes the sharding key and the sharding algorithm, and the latter one is extracted out for its independence. 
Only sharding key + sharding algorithm can be used in sharding operation.

## SQL Hint

In the case that the sharding column is not decide by SQL but other external conditions, SQL hint can be used to inject sharding value. 
For example, databases are shard according to the staff’s ID, but column does not exist in the database. 
SQL Hint can be used by two ways, Java API and SQL comment (TODO).
Please refer to [Hint](/en/features/sharding/concept/hint/) for more details.
