+++
title = "Sharding"
weight = 2
+++

## Definition

### Sharding Table Rule

```sql
SHOW SHARDING TABLE tableRule | RULES [FROM schemaName]

SHOW SHARDING ALGORITHMS [FROM schemaName]

tableRule:
    RULE tableName
```
-  Support query all data fragmentation rules and specified table query
-  Support query all sharding algorithms

### Sharding Binding Table Rule

```sql
SHOW SHARDING BINDING TABLE RULES [FROM schemaName]
```

### Sharding Broadcast Table Rule

```sql
SHOW SHARDING BROADCAST TABLE RULES [FROM schemaName]
```

## Description

### Sharding Table Rule

| Column                         | Description                                              |
| ------------------------------ | -------------------------------------------------------- |
| table                          | Logical table name                                       |
| actualDataNodes                | Actual data node                                         |
| actualDataSources              | Actual data source（Displayed when creating rules by RDL）|
| databaseStrategyType           | Database sharding strategy type                          |
| databaseShardingColumn         | Database sharding column                                 |
| databaseShardingAlgorithmType  | Database sharding algorithm type                         |
| databaseShardingAlgorithmProps | Database sharding algorithm parameter                    |
| tableStrategyType              | Table sharding strategy type                             |
| tableShardingColumn            | Table sharding column                                    |
| tableShardingAlgorithmType     | Database sharding algorithm type                         |
| tableShardingAlgorithmProps    | Database sharding algorithm parameter                    |
| keyGenerateColumn              | Distributed primary key generation column                |
| keyGeneratorType               | Distributed primary key generation type                  |
| keyGeneratorProps              | Distributed primary key generation parameter             |

### Sharding Algorithms

| Column | Description                  |
| -------| -----------------------------|
| name   | Sharding algorithm name      |
| type   | Sharding algorithm type      |
| props  | Sharding algorithm parameters|


### Sharding Binding Table Rule

| Column                | Description                 | 
| --------------------- | --------------------------  |
| shardingBindingTables | sharding Binding Table list |

### Sharding Broadcast Table Rule

| Column                  | Description                   |
| ----------------------- | ----------------------------- |
| shardingBroadcastTables | sharding Broadcast Table list |

## Example

### Sharding Table Rule

*SHOW SHARDING TABLE RULES*
```sql
mysql> show sharding table rules;
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
| table        | actualDataNodes                 | actualDataSources | databaseStrategyType | databaseShardingColumn | databaseShardingAlgorithmType | databaseShardingAlgorithmProps         | tableStrategyType | tableShardingColumn | tableShardingAlgorithmType | tableShardingAlgorithmProps                       | keyGenerateColumn | keyGeneratorType | keyGeneratorProps |
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
| t_order      | ds_${0..1}.t_order_${0..1}      |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_${order_id % 2}      | order_id          | SNOWFLAKE        | worker-id:123     |
| t_order_item | ds_${0..1}.t_order_item_${0..1} |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_item_${order_id % 2} | order_item_id     | SNOWFLAKE        | worker-id:123     |
| t2           |                                 | ds_0,ds_1         |                      |                        |                               |                                        | mod               | id                  | mod                        | sharding-count:10                                 |                   |                  |                   |
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
3 rows in set (0.02 sec)
```

*SHOW SHARDING TABLE RULE tableName*
```sql
mysql> show sharding table rule t_order;
+---------+----------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+----------------------------------------------+-------------------+------------------+-------------------+
| table   | actualDataNodes            | actualDataSources | databaseStrategyType | databaseShardingColumn | databaseShardingAlgorithmType | databaseShardingAlgorithmProps         | tableStrategyType | tableShardingColumn | tableShardingAlgorithmType | tableShardingAlgorithmProps                  | keyGenerateColumn | keyGeneratorType | keyGeneratorProps |
+---------+----------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+----------------------------------------------+-------------------+------------------+-------------------+
| t_order | ds_${0..1}.t_order_${0..1} |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_${order_id % 2} | order_id          | SNOWFLAKE        | worker-id:123     |
+---------+----------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+----------------------------------------------+-------------------+------------------+-------------------+
1 row in set (0.01 sec)
```

*SHOW SHARDING ALGORITHMS*
```sql
mysql> show sharding algorithms;
+-------------------------+--------+-----------------------------------------------------+
| name                    | type   | props                                               |
+-------------------------+--------------------------------------------------------------+
| t_order_inline          | INLINE | algorithm-expression=t_order_${order_id % 2}        |
| t_order_item_inline     | INLINE | algorithm-expression=t_order_item_${order_id % 2}   |
+-------------------------+--------+-----------------------------------------------------+
2 row in set (0.01 sec)
```

### Sharding Binding Table Rule

```sql
mysql> show sharding binding table rules from sharding_db;
+----------------------+
| shardingBindingTables |
+----------------------+
| t_order,t_order_item |
| t1,t2                |
+----------------------+
2 rows in set (0.00 sec)
```

### Sharding Broadcast Table Rule

```sql
mysql> show sharding broadcast table rules;
+------------------------+
| shardingBroadcastTables |
+------------------------+
| t_1                    |
| t_2                    |
+------------------------+
2 rows in set (0.00 sec)
```
