+++
title = "Sharding"
weight = 1
+++

## Syntax

### Sharding Table Rule

```sql
SHOW SHARDING TABLE tableRule | RULES [FROM databaseName]

SHOW SHARDING ALGORITHMS [FROM databaseName]

SHOW UNUSED SHARDING ALGORITHMS [FROM databaseName]
    
SHOW SHARDING AUDITORS [FROM databaseName]

SHOW SHARDING TABLE RULES USED ALGORITHM shardingAlgorithmName [FROM databaseName]

SHOW SHARDING KEY GENERATORS [FROM databaseName]

SHOW UNUSED SHARDING KEY GENERATORS [FROM databaseName]

SHOW SHARDING TABLE RULES USED KEY GENERATOR keyGeneratorName [FROM databaseName]

SHOW DEFAULT SHARDING STRATEGY 

SHOW SHARDING TABLE NODES

tableRule:
    RULE tableName
```
-  Support query all data fragmentation rules and specified table query
-  Support query all sharding algorithms
-  Support query all sharding audit algorithms

### Sharding Binding Table Rule

```sql
SHOW SHARDING BINDING TABLE RULES [FROM databaseName]
```

### Sharding Broadcast Table Rule

```sql
SHOW SHARDING BROADCAST TABLE RULES [FROM databaseName]
```

### Sharding Table Rule

| Column                            | Description                                               |
| --------------------------------- | --------------------------------------------------------- |
| table                             | Logical table name                                        |
| actual_data_nodes                 | Actual data node                                          |
| actual_data_sources               | Actual data source (Displayed when creating rules by RDL) |
| database_strategy_type            | Database sharding strategy type                           |
| database_sharding_column          | Database sharding column                                  |
| database_sharding_algorithm_type  | Database sharding algorithm type                          |
| database_sharding_algorithm_props | Database sharding algorithm properties                     |
| table_strategy_type               | Table sharding strategy type                              |
| table_sharding_column             | Table sharding column                                     |
| table_sharding_algorithm_type     | Table sharding algorithm type                             |
| table_sharding_algorithm_props    | Table sharding algorithm properties                         |
| key_generate_column               | Sharding key generator column                             |
| key_generator_type                | Sharding key generator type                               |
| key_generator_props               | Sharding key generator properties                         |

### Sharding Algorithms

| Column | Description                   |
| ------ | ----------------------------- |
| name   | Sharding algorithm name       |
| type   | Sharding algorithm type       |
| props  | Sharding algorithm properties |

### Unused Sharding Algorithms

| Column | Description                   |
| ------ | ----------------------------- |
| name   | Sharding algorithm name       |
| type   | Sharding algorithm type       |
| props  | Sharding algorithm properties |

### Sharding auditors

| Column | Description                         |
| ------ |-------------------------------------|
| name   | Sharding audit algorithm name       |
| type   | Sharding audit algorithm type       |
| props  | Sharding audit algorithm properties |

### Sharding key generators

| Column | Description                       |
| ------ | --------------------------------- |
| name   | Sharding key generator name       |
| type   | Sharding key generator type       |
| props  | Sharding key generator properties |

### Unused Sharding Key Generators

| Column | Description                       |
| ------ | --------------------------------- |
| name   | Sharding key generator name       |
| type   | Sharding key generator type       |
| props  | Sharding key generator properties |

### Default Sharding Strategy

| Column                    | Description                    |
| --------------------------| -------------------------------|
| name                      | Strategy name                  |
| type                      | Sharding strategy type         |
| sharding_column           | Sharding column                |
| sharding_algorithm_name   | Sharding algorithm name        |
| sharding_algorithm_type   | Sharding algorithm type        |
| sharding_algorithm_props  | Sharding algorithm properties  |

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

### Sharding Scaling Rule

| Column                   | Description                            |
|--------------------------|----------------------------------------|
| name                     | name of sharding scaling rule          |
| input                    | data read configuration                |
| output                   | data write configuration               |
| stream_channel           | algorithm of stream channel            |
| completion_detector      | algorithm of completion detecting      |
| data_consistency_checker | algorithm of data consistency checking |

## Example

### Sharding Table Rule

*SHOW SHARDING TABLE RULES*
```sql
mysql> SHOW SHARDING TABLE RULES;
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
| table        | actual_data_nodes               | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props         | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props                       | key_generate_column | key_generator_type | key_generator_props |
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
| t_order      | ds_${0..1}.t_order_${0..1}      |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_${order_id % 2}      | order_id          | SNOWFLAKE        |                   |
| t_order_item | ds_${0..1}.t_order_item_${0..1} |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_item_${order_id % 2} | order_item_id     | SNOWFLAKE        |                   |
| t2           |                                 | ds_0,ds_1         |                      |                        |                               |                                        | mod               | id                  | mod                        | sharding-count:10                                 |                   |                  |                   |
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
3 rows in set (0.02 sec)
```

*SHOW SHARDING TABLE RULE tableName*
```sql
mysql> SHOW SHARDING TABLE RULE t_order;
+---------+----------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+----------------------------------------------+-------------------+------------------+-------------------+
| table   | actual_data_nodes          | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props         | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props                  | key_generate_column | key_generator_type | key_generator_props |
+---------+----------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+----------------------------------------------+-------------------+------------------+-------------------+
| t_order | ds_${0..1}.t_order_${0..1} |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_${order_id % 2} | order_id          | SNOWFLAKE        |                   |
+---------+----------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+----------------------------------------------+-------------------+------------------+-------------------+
1 row in set (0.01 sec)
```

*SHOW SHARDING ALGORITHMS*
```sql
mysql> SHOW SHARDING ALGORITHMS;
+-------------------------+--------+-----------------------------------------------------+
| name                    | type   | props                                               |
+-------------------------+--------+-----------------------------------------------------+
| t_order_inline          | INLINE | algorithm-expression=t_order_${order_id % 2}        |
| t_order_item_inline     | INLINE | algorithm-expression=t_order_item_${order_id % 2}   |
+-------------------------+--------+-----------------------------------------------------+
2 row in set (0.01 sec)
```

*SHOW UNUSED SHARDING ALGORITHMS*
```sql
mysql> SHOW UNUSED SHARDING ALGORITHMS;
+---------------+--------+-----------------------------------------------------+
| name          | type   | props                                               |
+---------------+--------+-----------------------------------------------------+
| t1_inline     | INLINE | algorithm-expression=t_order_${order_id % 2}        |
+---------------+--------+-----------------------------------------------------+
1 row in set (0.01 sec)
```

*SHOW SHARDING AUDITORS*
```sql
mysql> SHOW SHARDING AUDITORS;
+------------+-------------------------+-------+
| name       | type                    | props |
+------------+-------------------------+-------+
| dml_audit  | DML_SHARDING_CONDITIONS |       |
+------------+-------------------------+-------+
2 row in set (0.01 sec)
```

*SHOW SHARDING TABLE RULES USED ALGORITHM shardingAlgorithmName*
```sql
mysql> SHOW SHARDING TABLE RULES USED ALGORITHM t_order_inline;
+-------+---------+
| type  | name    |
+-------+---------+
| table | t_order |
+-------+---------+
1 row in set (0.01 sec)
```

*SHOW SHARDING KEY GENERATORS*
```sql
mysql> SHOW SHARDING KEY GENERATORS;
+------------------------+-----------+-----------------+
| name                   | type      | props           |
+------------------------+-----------+-----------------+
| t_order_snowflake      | snowflake |                 |
| t_order_item_snowflake | snowflake |                 |
| uuid_key_generator     | uuid      |                 |
+------------------------+-----------+-----------------+
3 row in set (0.01 sec)
```

*SHOW UNUSED SHARDING KEY GENERATORS*
```sql
mysql> SHOW UNUSED SHARDING KEY GENERATORS;
+------------------------+-----------+-----------------+
| name                   | type      | props           |
+------------------------+-----------+-----------------+
| uuid_key_generator     | uuid      |                 |
+------------------------+-----------+-----------------+
1 row in set (0.01 sec)
```

*SHOW SHARDING TABLE RULES USED KEY GENERATOR keyGeneratorName*
```sql
mysql> SHOW SHARDING TABLE RULES USED KEY GENERATOR keyGeneratorName;
+-------+---------+
| type  | name    |
+-------+---------+
| table | t_order |
+-------+---------+
1 row in set (0.01 sec)
```

*SHOW DEFAULT SHARDING STRATEGY*
```sql
mysql> SHOW DEFAULT SHARDING STRATEGY ;

+----------+---------+--------------------+-------------------------+-------------------------+------------------------------------------+
| name     | type    | sharding_column    | sharding_algorithm_name | sharding_algorithm_type | sharding_algorithm_props                 |
+----------+---------+--------------------+-------------------------+-------------------------+------------------------------------------+
| TABLE    | NONE    |                    |                         |                         |                                          |
| DATABASE | STANDARD| order_id           | database_inline         | INLINE                  | {algorithm-expression=ds_${user_id % 2}} |
+----------+---------+--------------------+-------------------------+-------------------------+------------------------------------------+
2 rows in set (0.07 sec)
```

*SHOW SHARDING TABLE NODES*

```sql
mysql> SHOW SHARDING TABLE NODES;
+---------+----------------------------------------------------------------+
| name    | nodes                                                          |
+---------+----------------------------------------------------------------+
| t_order | ds_0.t_order_0, ds_1.t_order_1, ds_0.t_order_2, ds_1.t_order_3 |
+---------+----------------------------------------------------------------+
1 row in set (0.02 sec)
```

### Sharding Binding Table Rule

```sql
mysql> SHOW SHARDING BINDING TABLE RULES;
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
mysql> SHOW SHARDING BROADCAST TABLE RULES;
+------------------------+
| sharding_broadcast_tables |
+------------------------+
| t_1                    |
| t_2                    |
+------------------------+
2 rows in set (0.00 sec)
```
