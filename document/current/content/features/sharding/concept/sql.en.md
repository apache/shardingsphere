+++
toc = true
title = "SQL"
weight = 1
+++

## Logic Table

The integrated table name for horizontal-sharding tables. e.g. There are ten sharding order tables from t_order_0 to t_order_9, and their logic table name is t_order.

## Actual Table

The physical table really existed in the sharding database. e.g. t_order_0 ~ t_order_9 in the previous example.

## Data Node

The smallest unit of data sharding. It consists of data source name and table name, e.g. ds_0.t_order_0.

## Binding Table

The relational tables with the same sharding rules. 
For example: The t_order table and t_order_item table are all  sharding with order_id, then they are binding table each other. 
Query between binding tables do not use cartesian product join, efficiency of join query will be greatly improved.

If SQL is:

```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

If absent binding tables configuration, we assume sharding key is order_id, value `10` should route to sharding `0`, and value `11` should route to sharding `1`, the SQL after route are:

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_0 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

If binding tables configuration are present, the SQL after route are:

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

Table t_order is the first table at the left of `FROM`, Sharding-Sphere make this table as main table in this query. 
Route engine should only use main table's strategy, t_order_item just use t_order's sharding condition.

## Logic Index

Some databases(such as PostgreSQL) do not permit exist same index name in one database, Some databases(such as MySQL) permit unique indexes name based on tables. Logic index used for first scenario, it need to rewrite index name to index name + table name, the index name before rewrite is called logic index. 
