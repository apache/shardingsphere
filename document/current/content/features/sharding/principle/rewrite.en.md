+++
toc = true
title = "Rewrite Engine"
weight = 3
+++

The SQL written by developers facing logic databases and logic tables cannot be executed directly in actual databases. 
The SQL rewrite is used to rewrite logic SQL into the rightly executable one in actual databases, including two parts, correctness rewrite and optimization rewrite.

## Correctness Rewrite

In the situation that contains sharding tables, it requires to rewrite logic table names in sharding settings into actual table names acquired after routing. 
Database sharding does not require the rewrite of table names. In addition to that, there are also column addition, pagination information revision and other content.

## Identifier Rewrite

Identifiers that need to be rewritten include table name, index name and schema name.
Table name rewrite refers to the process to locate the position of logic tables in the original SQL and rewrite it as the physical table. 
Table name rewrite is one typical situation that requires to parse SQL. From a most plain case, if the logic SQL is as follow:

```
SELECT order_id FROM t_order WHERE order_id=1;
```

Suppose the SQL is set with sharding key order_id, and its conditions will be routed to Sharding Table 1. Then, the SQL after rewriting should be:

```
SELECT order_id FROM t_order_1 WHERE order_id=1;
```

In this most simple kind of SQL situation, whether parsing SQL to abstract syntax tree seems unimportant, SQL can be rewritten only by searching for and substituting characters. 
But in the following situation, it is unable to rewrite SQL rightly merely by searching for and substituting characters:

In this most simple kind of SQL situation, whether parsing SQL to abstract syntax tree seems unimportant, SQL can be rewritten only by searching for and substituting characters. 
But in the following situation, it is unable to rewrite SQL rightly merely by searching for and substituting characters:

```
SELECT order_id FROM t_order WHERE order_id=1 AND remarks=` t_order xxx`;
```

The SQL rightly rewritten is supposed to be:

```
SELECT order_id FROM t_order_1 WHERE order_id=1 AND remarks=` t_order xxx`;
```

Rather than:

```
SELECT order_id FROM t_order_1 WHERE order_id=1 AND remarks=` t_order_1 xxx`;
```

Because there may be similar characters besides the table name, the simple character substitute method cannot be used to rewrite SQL.
Here is another more complex SQL rewrite situation:

```
SELECT t_order.order_id FROM t_order WHERE t_order.order_id=1 AND remarks=` t_order xxx`;
```

The SQL above takes table name as the identifier of the field, so it should also be revised with SQL rewrite:

```
SELECT t_order_1.order_id FROM t_order_1 WHERE t_order_1.order_id=1 AND remarks=` t_order xxx`;
```

But if there is another name defined in SQL, it is not necessary to revise that, even though that name is the same as the table name. For example:

```
SELECT t_order.order_id FROM t_order AS t_order WHERE t_order.order_id=1 AND remarks=` t_order xxx`;
```

SQL rewrite only requires to revise its table name:

```
SELECT t_order.order_id FROM t_order_1 AS t_order WHERE t_order.order_id=1 AND remarks=` t_order xxx`;
```

Index name is another identifier that can be rewritten. 
In some databases (such as MySQL), the index is created with the dimension of tables, and its names in different tables can repeat. 
In other databases (such as PostgreSQL), however, the index is created with the dimension of databases, index names in different tables are required to be one and the only.

In the table sharding situation, the same logic table will be separated into many physical tables in one database. 
Therefore, index names created for those physical tables are not allowed to repeat. 
As a result, ShardingSphere will revise the index name as the suffix of logic index names plus its physical table name.

In ShardingSphere, schema management method is similar as that of the table. 
It uses logic schema to manage a set of data, so it requires to replace the logic schema written by users in SQL with physical database schema.

ShardingSphere still does not support the use of schema in DQL and DML statement, for example:

```
SHOW COLUMNS FROM t_order FROM order_ds;
```

Schema rewrite refers to rewriting logic schema to a right and real schema found arbitrarily with unicast route.

## Derived Column

The derived column in query statements usually results from two situations. 
First, in result merging, ShardingSphere needs to acquire the corresponding data, but it is not returned through the query SQL. 
This kind of situation aims mainly at GROUP BY and ORDER BY. 
Result merging requires the sorting and ranking according to items of `GROUP BY` and `ORDER BY` field. 
But if the sorting and ranking items are not included in the original SQL, it should be rewritten. 
Look at the situation where the original SQL has the information required by result merging:

```
SELECT order_id, user_id FROM t_order ORDER BY user_id;
```

Since user_id is used in ranking, the result merging needs the data able to acquire user_id. 
The SQL above is able to acquire user_id data, so there is no need to add columns.

If the selected item does not contain the column required by result merging, it will need to add column, as the following SQL:

```
SELECT order_id FROM t_order ORDER BY user_id;
```

Since the original SQL does not contain user_id that result merging requires to acquire, the SQL needs to be rewritten by adding columns, and after that, it will be:

```
SELECT order_id, user_id AS ORDER_BY_DERIVED_0 FROM t_order ORDER BY user_id;
```

What’s to be mentioned, derived column will only add the missing column rather than all of them; 
the SQL that includes `*` in SELECT will also selectively add columns according to the meta-data information of tables. 
Here is a relatively complex SQL derived column case:

```
SELECT o.* FROM t_order o, t_order_item i WHERE o.order_id=i.order_id ORDER BY user_id, order_item_id;
```

Suppose only the t_order_item table contains order_item_id column, according to the meta-data information of tables, 
the user_id in sorting item exists in table t_order as merging result, but order_item_id does not exist in t_order, so it needs to add columns. The SQL after that will be:

```
SELECT o.*, order_item_id AS ORDER_BY_DERIVED_0 FROM t_order o, t_order_item i WHERE o.order_id=i.order_id ORDER BY user_id, order_item_id;
```

Another situation of derived column is using AVG aggregation function. 
In distributed situations, it is not right to calculate the average value with avg1 + avg2 + avg3 / 3, and it should be written as (sum1 + sum2 + sum3) / (count1 + count2 + count3). 
This requires to rewrite the SQL that contains AVG as SUM and COUNT and recalculate the average value in result merging. Such as the following SQL:

```
SELECT AVG(price) FROM t_order WHERE user_id=1;
```

Should be rewritten as:

```
SELECT COUNT(price) AS AVG_DERIVED_COUNT_0, SUM(price) AS AVG_DERIVED_ SUM _0 FROM t_order WHERE user_id=1;
```

Then it can calculate the right average value through result merging.

The last kind of derived column happens in SQL with INSERT. 
With database auto-increment key, there is no need to fill in primary key field. 
But database auto-increment key cannot satisfy the requirement of only one primary key being in the distributed situation. 
So ShardingSphere provides a generation strategy for distributed auto-increment key, through derived column, enabling users to replace the current auto-increment key with a distributed one without changing existing codes invisibly. 
Distributed auto-increment key generation strategy will be expounded in the following part, here we only explain the content related with SQL rewrite. 
For example, if the primary key of t_order is order_id, and the original SQL is:

```
INSERT INTO t_order (`field1`, `field2`) VALUES (10, 1);
```

It can be seen that the SQL above does not include an auto-increment key, which will be filled by the database itself. 
After ShardingSphere set an auto-increment key, the SQL will be rewritten as:

```
INSERT INTO t_order (`field1`, `field2`, order_id) VALUES (10, 1, xxxxx);
```

Rewritten SQL will add auto-increment key name and its value generated automatically in the last part of INSERT FIELD and INSERT VALUE. 
`xxxxx` in the SQL above stands for the latter one.
If INSERT SQL does not contain the column name of the table, ShardingSphere can also automatically generate auto-increment key by comparing the number of parameter and column in the table meta-information. 
For example, the original SQL is:

```
INSERT INTO t_order VALUES (10, 1);
```

The rewritten SQL only needs to add an auto-increment key in the column where the primary key lays:

```
INSERT INTO t_order VALUES (xxxxx, 10, 1);
```

In auto-increment key derived column, if the user writes SQL with the method of placeholder, he only needs to rewrite parameter list but not SQL itself.

## Pagination Revision

Acquiring pagination data from multiple databases is different from the situation of a single database. 
Suppose every 10 pieces of data are divided to be one page, it is not right to take the second page of data, acquire LIMIT 10, 10 under sharding situations, 
and take out the first 10 pieces of data according to sorting conditions after merging. For example, if the SQL is:
 
```
SELECT score FROM t_score ORDER BY score DESC LIMIT 1, 2;
```

The following picture shows the execution of pagination results without SQL rewrite.

![Pagination without rewrite](http://shardingsphere.apache.org/document/current/img/sharding/pagination_without_rewrite.png)

As shown in the picture, if you want to acquire the second and the third piece of data ordered by score common in both tables, and they are supposed to be `95` and `90`. 
Since the executed SQL can only acquire the second and the third piece of data from each table, i.e., `90` and `80` from t_score_0, `85` and `75` from t_score_1. 
When merging results, it can only merge from `90`, `80`, `85` and `75` already acquired, so the right result cannot be acquired anyway.

The right way is to rewrite pagination conditions as `LIMIT 0, 3`, take out all the data from the first two pages and combine sorting conditions to calculate the right data. 
The following picture shows the execution of pagination results after SQL rewrite.

![Pagination with rewrite](http://shardingsphere.apache.org/document/current/img/sharding/pagination_with_rewrite.png)

The latter the offset position is, the lower the efficiency of using LIMIT pagination will be. 
There are many ways to avoid using LIMIT as pagination method, such as constructing a secondary index to record line record number and line offset amount, 
or using the tail ID of last pagination data as the pagination method of conditions of the next query.

When revising pagination information, if the user uses placeholder method to write SQL, he only needs to rewrite parameter list rather than SQL itself.

## Batch Split

When using SQL inserted in batch, if the inserted data crosses sharding, the user needs to rewrite SQL to avoid writing excessive data into the database. 
The differences between insert operation and query operation are: 
though sharding keys that do not exist in current sharding are used in the query sentence, it will not have any influence on data; 
insert operation has to delete extra sharding keys. 
Take the following SQL for example:

```
INSERT INTO t_order (order_id, xxx) VALUES (1, 'xxx'), (2, 'xxx'), (3, 'xxx');
```

Suppose the database is divided into two parts according to odd or even number, execute this SQL after revising the table name only, and then both of the two shards will be written with the same record.
 
Though only the data that confirms to sharding conditions can be taken out from query statement, the realization schema with excessive data is not reasonable.
 
So SQL should be rewritten as:

```
INSERT INTO t_order_0 (order_id, xxx) VALUES (2, 'xxx');
INSERT INTO t_order_1 (order_id, xxx) VALUES (1, 'xxx'), (3, 'xxx');
```

IN query is similar as batch insertion, but IN operation will not lead to wrong data query result. 
Through rewriting IN query, the query performance can be further improved. Like the following SQL:

```
SELECT * FROM t_order WHERE order_id IN (1, 2, 3);
```

Is rewritten as:

```
SELECT * FROM t_order_0 WHERE order_id IN (2);
SELECT * FROM t_order_1 WHERE order_id IN (1, 3);
```

The query performance will be further improved. For now, ShardingSphere has not realized this rewrite strategy, so the current rewrite result is:

```
SELECT * FROM t_order_0 WHERE order_id IN (1, 2, 3);
SELECT * FROM t_order_1 WHERE order_id IN (1, 2, 3);
```

Though the execution result of SQL is right, but it has not achieved the most optimized query efficiency.

## Optimization Rewrite

Its purpose is an effective method to improve the performance without influencing the correctness of the query.

It can be divided into single node optimization and stream merging optimization.

### Single Node Optimization

It refers to the optimization that stops the SQL rewrite from the route to the single node. 
After acquiring one route result, if it is routed to a single data node, result merging is unnecessary to be involved, so there is no need for rewrites as derived column, pagination information and others. 
In particular, there is no need to read from the first piece of information, which reduces the pressure for the database to a large extent and saves meaningless consumption of the network bandwidth.

### Stream Merging Optimization

It only adds `ORDER BY` as well as sorting items and sorting order which are the same as grouping items, to the SQL that contains `GROUP BY`, and they are used to transfer memory merging to stream merging. 
In the result merging part, stream merging and memory merging will be explained in detail.

The overall structure division of rewrite engine is shown in the following picture.

![Rewrite Engine](http://shardingsphere.apache.org/document/current/img/sharding/rewrite_architecture_en.png)
