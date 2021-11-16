+++
title = "数据分片"
weight = 1
+++

## 定义

### Sharding Table Rule

```sql
SHOW SHARDING TABLE tableRule | RULES [FROM schemaName]

SHOW SHARDING ALGORITHMS [FROM schemaName]

tableRule:
    RULE tableName
```
-  支持查询所有数据分片规则和指定表查询
-  支持查询所有分片算法

### Sharding Binding Table Rule

```sql
SHOW SHARDING BINDING TABLE RULES [FROM schemaName]
```

### Sharding Broadcast Table Rule

```sql
SHOW SHARDING BROADCAST TABLE RULES [FROM schemaName]
```

## 说明

### Sharding Table Rule

| 列                                | 说明                                 |
| --------------------------------- | ----------------------------------- |
| table                             | 逻辑表名                             |
| actual_data_nodes                 | 实际的数据节点                        |
| actual_data_sources               | 实际的数据源（通过 RDL 创建的规则时显示） |
| database_strategy_type            | 数据库分片策略类型                     |
| database_sharding_column          | 数据库分片键                          |
| database_sharding_algorithm_type  | 数据库分片算法类型                     |
| database_sharding_algorithm_props | 数据库分片算法参数                     |
| table_strategy_type               | 表分片策略类型                        |
| table_sharding_column             | 表分片键                             |
| table_sharding_algorithm_type     | 表分片算法类型                        |
| table_sharding_algorithm_props    | 表分片算法参数                        |
| key_generate_column               | 分布式主键生成列                      |
| key_generator_type                | 分布式主键生成器类型                   |
| key_generator_props               | 分布式主键生成器参数                   |

### Sharding Algorithms

| 列     | 说明          |
| ------| --------------|
| name  | 分片算法名称    |
| type  | 分片算法类型    |
| props | 分片算法参数    |

### Sharding Binding Table Rule

| 列                      | 说明      |
| ----------------------- | -------- |
| sharding_binding_tables | 绑定表名称 |

### Sharding Broadcast Table Rule

| 列                        | 说明      |
| ------------------------- | -------- |
| sharding_broadcast_tables | 广播表名称 |

## 示例

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
