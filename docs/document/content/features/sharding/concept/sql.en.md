+++
title = "SQL"
weight = 1
+++

## Logic Table

It refers collectively to horizontal sharding databases (tables) with the same logic and data structure. For instance, the order data is divided into 10 tables according to the last number of the primary key, and they are from `t_order_0` to `t_order_9`, whose logic name is `t_order`.

## Actual Table

The physical table that really exists in the sharding database, i.e., `t_order_0` to `t_order_9` in the instance above.

## Data Node

As the smallest unit of sharding, it consists of data source name and table name, e.g. `ds_0.t_order_0`.

## Binding Table

It refers to the primary table and the joiner table with the same sharding rules, for example, `t_order` and `t_order_item` are both sharded by `order_id`,  so they are binding tables with each other. Cartesian product correlation will not appear in the multi-table correlating query, so the query efficiency will increase greatly. Take this one for example, if SQL is:

```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

When binding table relations are not configured, suppose the sharding key `order_id` routes value 10 to sharding 0 and value 11 to sharding 1, there will be 4 SQLs in Cartesian product after routing:

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
SELECT i.* FROM t_order_0 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
SELECT i.* FROM t_order_1 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

With binding table configuration, there should be 2 SQLs after routing:

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

In them, table `t_order` in the left end of FROM will be taken by ShardingSphere as the primary table of query. In a similar way, ShardingSphere will also take table `t_order` in the left end of FROM as the primary table of the whole binding table. All the route computations will only use the sharding strategy of the primary table, so sharding computation of `t_order_item` table will use the conditions of `t_order`. Due to this, sharding keys in binding tables should be totally identical.

## Broadcast Table

It refers to tables that exist in all sharding database sources. Their structures and data are the same in each database. It can be applied to the small data volume scenario that needs to correlate with big data volume tables to query, dictionary table for example.
