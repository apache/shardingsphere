+++
title = "数据分片"
weight = 1
+++

## 语法说明

### Sharding Table Rule

```sql
SHOW SHARDING TABLE tableRule | RULES [FROM databaseName]

SHOW SHARDING ALGORITHMS [FROM databaseName]

SHOW UNUSED SHARDING ALGORITHMS [FROM databaseName]
    
SHOW SHARDING AUDITORS [FROM databaseName]

SHOW SHARDING TABLE RULES USED ALGORITHM shardingAlgorithmName [FROM databaseName]

SHOW SHARDING KEY GENERATORS [FROM databaseName]

SHOW UNUSED SHARDING KEY GENERATORS [FROM databaseName]
    
SHOW UNUSED SHARDING AUDITORS [FROM databaseName]

SHOW SHARDING TABLE RULES USED KEY GENERATOR keyGeneratorName [FROM databaseName]
    
SHOW SHARDING TABLE RULES USED AUDITOR auditorName [FROM databaseName]

SHOW DEFAULT SHARDING STRATEGY 

SHOW SHARDING TABLE NODES

tableRule:
    RULE tableName
```
-  支持查询所有数据分片规则和指定表查询；
-  支持查询所有分片算法；
-  支持查询所有分片审计算法。

### Sharding Table Reference Rule

```sql
SHOW SHARDING TABLE REFERENCE RULES [FROM databaseName]
```

### Broadcast Table Rule

```sql
SHOW BROADCAST TABLE RULES [FROM databaseName]
```

### Sharding Table Rule

| 列                                  | 说明                                  |
|------------------------------------|---------------------------------------|
| table                              | 逻辑表名                               |
| actual_data_nodes                  | 实际的数据节点                          |
| actual_data_sources                | 实际的数据源（通过 RDL 创建的规则时显示）   |
| database_strategy_type             | 数据库分片策略类型                       |
| database_sharding_column           | 数据库分片键                            |
| database_sharding_algorithm_type   | 数据库分片算法类型                       |
| database_sharding_algorithm_props  | 数据库分片算法参数                       |
| table_strategy_type                | 表分片策略类型                          |
| table_sharding_column              | 表分片键                               |
| table_sharding_algorithm_type      | 表分片算法类型                          |
| table_sharding_algorithm_props     | 表分片算法参数                          |
| key_generate_column                | 分布式主键生成列                        |
| key_generator_type                 | 分布式主键生成器类型                     |
| key_generator_props                | 分布式主键生成器参数                     |
| auditor_types                      | 分片审计生成器参数                       |
| allow_hint_disable                 | 是否禁用分片审计hint                     |

### Sharding Algorithms

| 列     | 说明          |
| ------| --------------|
| name  | 分片算法名称    |
| type  | 分片算法类型    |
| props | 分片算法参数    |

### Unused Sharding Algorithms

| 列     | 说明          |
| ------| --------------|
| name  | 分片算法名称    |
| type  | 分片算法类型    |
| props | 分片算法参数    |

### Sharding Auditors

| 列     | 说明              |
| ------|-------------------|
| name  | 分片审计算法名称     |
| type  | 分片审计算法类型     |
| props | 分片审计算法参数     |

### Unused Sharding Auditors

| 列     | 说明              |
| ------|-------------------|
| name  | 分片审计算法名称     |
| type  | 分片审计算法类型     |
| props | 分片审计算法参数     |

### Sharding Key Generators

| 列     | 说明        |
| ------|-------------|
| name  | 主键生成器名称 |
| type  | 主键生成器类型 |
| props | 主键生成器参数 |

### Unused Sharding Key Generators

| 列     | 说明        |
| ------|-------------|
| name  | 主键生成器名称 |
| type  | 主键生成器类型 |
| props | 主键生成器参数 |

### Default Sharding Strategy

| 列                        | 说明          |
| --------------------------| -------------|
| name                      | 策略名称      |
| type                      | 分片策略类型   |
| sharding_column           | 分片键        |
| sharding_algorithm_name   | 分片算法名称   |
| sharding_algorithm_type   | 分片算法类型   |
| sharding_algorithm_props  | 分片算法参数   |

### Sharding Table Nodes

| 列     | 说明          |
| ------| --------------|
| name  | 分片规则名称    |
| nodes | 分片节点       |

### Sharding Table Reference Rule

| 列                       | 说明      |
|--------------------------|---------- |
| sharding_table_reference | 表关联关系 |

### Broadcast Table Rule

| 列              | 说明      |
| --------------- | -------- |
| broadcast_table | 广播表名称 |

### Sharding Table Rule

*SHOW SHARDING TABLE RULES*
```sql
mysql> SHOW SHARDING TABLE RULES;
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
| table        | actual_data_nodes               | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props         | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props                       | key_generate_column | key_generator_type | key_generator_props | auditor_types | allow_hint_disable |
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
| t_order      | ds_${0..1}.t_order_${0..1}      |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_${order_id % 2}      | order_id          | SNOWFLAKE        |                   | DML_SHARDING_CONDITIONS    |true      |
| t_order_item | ds_${0..1}.t_order_item_${0..1} |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_item_${order_id % 2} | order_item_id     | SNOWFLAKE        |                   |                            |          |
| t2           |                                 | ds_0,ds_1         |                      |                        |                               |                                        | mod               | id                  | mod                        | sharding-count:10                                 |                   |                  |                   |                            |          |
+--------------+---------------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+---------------------------------------------------+-------------------+------------------+-------------------+
3 rows in set (0.02 sec)
```

*SHOW SHARDING TABLE RULE tableName*
```sql
mysql> SHOW SHARDING TABLE RULE t_order;
+---------+----------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+----------------------------------------------+-------------------+------------------+-------------------+
| table   | actual_data_nodes          | actual_data_sources | database_strategy_type | database_sharding_column | database_sharding_algorithm_type | database_sharding_algorithm_props         | table_strategy_type | table_sharding_column | table_sharding_algorithm_type | table_sharding_algorithm_props                  | key_generate_column | key_generator_type | key_generator_props | auditor_types | allow_hint_disable |
+---------+----------------------------+-------------------+----------------------+------------------------+-------------------------------+----------------------------------------+-------------------+---------------------+----------------------------+----------------------------------------------+-------------------+------------------+-------------------+
| t_order | ds_${0..1}.t_order_${0..1} |                   | INLINE               | user_id                | INLINE                        | algorithm-expression:ds_${user_id % 2} | INLINE            | order_id            | INLINE                     | algorithm-expression:t_order_${order_id % 2} | order_id          | SNOWFLAKE        |                   | DML_SHARDING_CONDITIONS    |true      | 
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
| dml_audit              | uuid      |                 |
+------------------------+-----------+-----------------+
1 row in set (0.01 sec)
```

*SHOW UNUSED SHARDING KEY AUDITORS*
```sql
mysql> SHOW UNUSED SHARDING KEY AUDITORS;
+------------+-------------------------+-------+
| name       | type                    | props |
+------------+-------------------------+-------+
| dml_audit  | DML_SHARDING_CONDITIONS |       |
+------------+-------------------------+-------+
1 row in set (0.01 sec)
```

*SHOW SHARDING TABLE RULES USED KEY GENERATOR keyGeneratorName*
```sql
mysql> SHOW SHARDING TABLE RULES USED KEY GENERATOR t_order_snowflake;
+-------+---------+
| type  | name    |
+-------+---------+
| table | t_order |
+-------+---------+
1 row in set (0.01 sec)
```

*SHOW SHARDING TABLE RULES USED AUDITOR auditorName*
```sql
mysql> SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required;
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

### Sharding Table Reference Rule

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES;
+--------------------------+
| sharding_table_reference |
+--------------------------+
| t_order,t_order_item     |
| t1,t2                    |
+--------------------------+
2 rows in set (0.00 sec)
```

### Broadcast Table Rule

```sql
mysql> SHOW BROADCAST TABLE RULES;
+------------------------+
| broadcast_table        |
+------------------------+
| t_1                    |
| t_2                    |
+------------------------+
2 rows in set (0.00 sec)
```
