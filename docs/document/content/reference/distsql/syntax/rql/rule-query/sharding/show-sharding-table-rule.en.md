+++
title = "SHOW SHARDING TABLE RULE"
weight = 3
+++


### Description

The `SHOW SHARDING TABLE RULE` syntax is used to query the sharding table rule in the specified database.

### Syntax
```
ShowShardingTableRule ::=
  'SHOW' 'SHARDING' 'TABLE' ('RULE' tableName | 'RULES') ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement
- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

 ### Return value description

| Column                            | Description                                               |
| --------------------------------- | --------------------------------------------------------- |
| table                             | Logical table name                                        |
| actual_data_nodes                 | Actual data node                                          |
| actual_data_sources               | Actual data source (Displayed when creating rules by RDL) |
| database_strategy_type            | Database sharding strategy type                           |
| database_sharding_column          | Database sharding column                                  |
| database_sharding_algorithm_type  | Database sharding algorithm type                          |
| database_sharding_algorithm_props | Database sharding algorithm properties                    |
| table_strategy_type               | Table sharding strategy type                              |
| table_sharding_column             | Table sharding column                                     |
| table_sharding_algorithm_type     | Table sharding algorithm type                             |
| table_sharding_algorithm_props    | Table sharding algorithm properties                       |
| key_generate_column               | Sharding key generator column                             |
| key_generator_type                | Sharding key generator type                               |
| key_generator_props               | Sharding key generator properties                         |

 ### Example
- Query the sharding table rules of the specified logical database
```sql
SHOW SHARDING TABLE RULES FROM sharding_db;
```
```sql
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
| table        | actual_data_nodes | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props | key_generate_column | key_generator_type | key_generator_props |
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
| t_order      |                   | ds_0,ds_1           |                        |                          |                                  |                                   | mod                 | order_id              | mod                           | sharding-count=4               |                     |                    |                     |
| t_order_item |                   | ds_0,ds_1           |                        |                          |                                  |                                   | mod                 | order_id              | mod                           | sharding-count=4               |                     |                    |                     |
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
2 rows in set (0.12 sec)
```

- Query the sharding table rules of the current logic database
```sql
SHOW SHARDING TABLE RULES;
```
```sql
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
| table        | actual_data_nodes | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props | key_generate_column | key_generator_type | key_generator_props |
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
| t_order      |                   | ds_0,ds_1           |                        |                          |                                  |                                   | mod                 | order_id              | mod                           | sharding-count=4               |                     |                    |                     |
| t_order_item |                   | ds_0,ds_1           |                        |                          |                                  |                                   | mod                 | order_id              | mod                           | sharding-count=4               |                     |                    |                     |
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
2 rows in set (0.12 sec)
```
- Query the specified sharding table rule
```sql
SHOW SHARDING TABLE RULE t_order;
```
```sql
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
| table        | actual_data_nodes | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props | key_generate_column | key_generator_type | key_generator_props |
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
| t_order      |                   | ds_0,ds_1           |                        |                          |                                  |                                   | mod                 | order_id              | mod                           | sharding-count=4               |                     |                    |                     |
+--------------+-------------------+---------------------+------------------------+--------------------------+----------------------------------+-----------------------------------+---------------------+-----------------------+-------------------------------+--------------------------------+---------------------+--------------------+---------------------+
1 rows in set (0.12 sec)
```
