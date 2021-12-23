+++
title = "Sharding"
weight = 1
+++

## Syntax

### Sharding Table Rule

```sql
SHOW SHARDING TABLE tableRule | RULES [FROM schemaName]

SHOW SHARDING ALGORITHMS [FROM schemaName]

SHOW SHARDING TABLE NODES;

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

## Return Value Description

### Sharding Table Rule

| Column                            | Description                                               |
| --------------------------------- | --------------------------------------------------------- |
| table                             | Logical table name                                        |
| actual_data_nodes                 | Actual data node                                          |
| actual_data_sources               | Actual data source (Displayed when creating rules by RDL) |
| database_strategy_type            | Database sharding strategy type                           |
| database_sharding_column          | Database sharding column                                  |
| database_sharding_algorithm_type  | Database sharding algorithm type                          |
| database_sharding_algorithm_props | Database sharding algorithm parameter                     |
| table_strategy_type               | Table sharding strategy type                              |
| table_sharding_column             | Table sharding column                                     |
| table_sharding_algorithm_type     | Database sharding algorithm type                          |
| table_sharding_algorithm_props    | Database sharding algorithm parameter                     |
| key_generate_column               | Distributed primary key generation column                 |
| key_generator_type                | Distributed primary key generation type                   |
| key_generator_props               | Distributed primary key generation parameter              |

### Sharding Algorithms

| Column | Description                   |
| ------ | ----------------------------- |
| name   | Sharding algorithm name       |
| type   | Sharding algorithm type       |
| props  | Sharding algorithm parameters |

### Sharding Table Nodes

| Column | Description          |
| -------| ---------------------|
| name   | Sharding rule name   |
| nodes  | Sharding nodes        |

### Sharding Binding Table Rule

| Column                  | Description                 | 
| ----------------------- | --------------------------- |
| sharding_binding_tables | sharding Binding Table list |

### Sharding Broadcast Table Rule

| Column                    | Description                   |
| ------------------------- | ----------------------------- |
| sharding_broadcast_tables | sharding Broadcast Table list |

## Example

### Sharding Table Rule

*SHOW SHARDING TABLE RULES*
```sql
mysql> show sharding table rules;
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
| table        | actual_data_nodes               | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props         | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props                       | key_generate_column | key_generator_type | key_generator_props |
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
| table   | actual_data_nodes          | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props         | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props                  | key_generate_column | key_generator_type | key_generator_props |
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

*SHOW SHARDING TABLE NODES*

```sql
mysql> show sharding table nodes;
+---------+----------------------------------------------------------------+
| name    | nodes                                                          |
+---------+----------------------------------------------------------------+
| t_order | ds_0.t_order_0, ds_1.t_order_1, ds_0.t_order_2, ds_1.t_order_3 |
+---------+----------------------------------------------------------------+
1 row in set (0.02 sec)
```

### Sharding Binding Table Rule

```sql
mysql> show sharding binding table rules from sharding_db;
+----------------------+
| sharding_binding_tables |
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
| sharding_broadcast_tables |
+------------------------+
| t_1                    |
| t_2                    |
+------------------------+
2 rows in set (0.00 sec)
```
