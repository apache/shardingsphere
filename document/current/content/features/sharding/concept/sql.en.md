+++
toc = true
title = "SQL"
weight = 1
+++

## Logic Table

It refers collectively to the same kind of databases (tables) of horizontal sharding. 
For instance, the order data is divided into 10 tables according to the last number of the primary key, and they are from `t_order_0` to `t_order_9`, whose logic name is `t_order`.

## Actual Table

The physical table that really exists in the sharding database, e.g. `t_order_0` to `t_order_9` in the instance above.

## Data Node

The smallest unit of data sharding, consist of source data name and table name, e.g. `ds_0.t_order_0`.

## Binding Table

It refers to the primary table and the copy table with the same sharding rules, 
such as, `t_order` table and `t_order_item` table are both of order ID sharding, so they are binding tables of each other. 
Cartesian product correlation will not appear in the multi-table correlating query, so the query efficiency will increases to a large extend. Take this one for example, if SQL is:

```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

Without binding table configuration, suppose the sharding key `order_id` routes value 10 to sharding 0, and value 11 to sharding 1, there will be 4 SQL after routing in Cartesian product:

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
SELECT i.* FROM t_order_0 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
SELECT i.* FROM t_order_1 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

With binding table configuration, there should be 2 SQL after routing:

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

In them, table `t_order` in the left end of FROM will be taken by Sharding-Sphere as the primary table of query. 
In a similar way, Sharding-Sphere will also take table `t_order` in the left end of FROM as the primary table of the whole binding table. 
All the route computations will only use the sharding strategy of the primary table, so sharding computation of `t_order_item` table will use the conditions of `t_order`. 
Due to this, sharding keys between binding tables should be totally identical.

## Logic Index

Some databases (such as PostgreSQL) do not allow indexes with an identical name existing in the same database; 
other databases (such as MySQL), however, only forbid indexes with an identical name existing in the same table. 
Logic index is used in the former situation where it requires to rewrite index names in the same database but not in the same table as index name + table name, 
and the previous index name becomes logic index. 
