+++
title = "数据分片"
weight = 2
+++

## 定义

### Sharding Table Rule

```sql
SHOW SHARDING TABLE tableRule | RULES [FROM schemaName]

tableRule:
    RULE tableName
```
-  支持查询所有数据分片规则和指定表查询

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

| 列                             | 说明                                 |
| ------------------------------ | ----------------------------------- |
| table                          | 逻辑表名                             |
| actualDataNodes                | 实际的数据节点                        |
| actualDataSources              | 实际的数据源（通过 RDL 创建的规则时显示） |
| databaseStrategyType           | 数据库分片策略类型                     |
| databaseShardingColumn         | 数据库分片键                          |
| databaseShardingAlgorithmType  | 数据库分片算法类型                     |
| databaseShardingAlgorithmProps | 数据库分片算法参数                     |
| tableStrategyType              | 表分片策略类型                        |
| tableShardingColumn            | 表分片键                             |
| tableShardingAlgorithmType     | 表分片算法类型                        |
| tableShardingAlgorithmProps    | 表分片算法参数                        |
| keyGenerateColumn              | 分布式主键生成列                      |
| keyGeneratorType               | 分布式主键生成器类型                   |
| keyGeneratorProps              | 分布式主键生成器参数                   |

### Sharding Binding Table Rule

| 列                    | 说明      |
| --------------------- | -------- |
| shardingBindingTables | 绑定表名称 |

### Sharding Broadcast Table Rule

| 列                      | 说明      |
| ----------------------- | -------- |
| shardingBroadcastTables | 广播表名称 |

## 示例

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
