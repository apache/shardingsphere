+++
title = "SHOW SHARDING KEY GENERATE STRATEGY"
weight = 14
+++

### 描述

`SHOW SHARDING KEY GENERATE STRATEGY` 语法用于查询指定逻辑库中的分片主键生成策略。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowShardingKeyGenerateStrategies ::=
  'SHOW' 'SHARDING' 'KEY' 'GENERATE' ('STRATEGY' keyGenerateStrategyName | 'STRATEGIES') ('FROM' databaseName)?

keyGenerateStrategyName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。如果也未使用 `DATABASE` 则会提示 `No database selected`；
- 使用 `SHOW SHARDING KEY GENERATE STRATEGIES` 查询全部分片主键生成策略；
- 使用 `SHOW SHARDING KEY GENERATE STRATEGY <name>` 查询指定分片主键生成策略。

### 返回值说明

| 列             | 说明              |
|----------------|-------------------|
| name           | 分片主键生成策略名称 |
| type           | 分片主键生成策略类型 |
| table          | 逻辑表名称         |
| column         | 主键列名称         |
| sequence       | 序列名称           |
| generator_name | 关联主键生成器名称   |
| generator_type | 关联主键生成器类型   |
| generator_props| 关联主键生成器参数   |

### 示例

- 查询指定逻辑库中的全部分片主键生成策略

```sql
SHOW SHARDING KEY GENERATE STRATEGIES FROM sharding_db;
```

```sql
mysql> SHOW SHARDING KEY GENERATE STRATEGIES FROM sharding_db;
+-------------------------+----------+---------+----------+-----------+---------------------+----------------+------------------+
| name                    | type     | table   | column   | sequence  | generator_name      | generator_type | generator_props  |
+-------------------------+----------+---------+----------+-----------+---------------------+----------------+------------------+
| order_id_strategy       | column   | t_order | order_id |           | snowflake_generator | snowflake      | {"worker-id":1}  |
| order_sequence_strategy | sequence |         |          | order_seq | uuid_generator      | uuid           |                  |
+-------------------------+----------+---------+----------+-----------+---------------------+----------------+------------------+
2 rows in set (0.00 sec)
```

- 查询当前逻辑库中的指定分片主键生成策略

```sql
SHOW SHARDING KEY GENERATE STRATEGY order_id_strategy;
```

```sql
mysql> SHOW SHARDING KEY GENERATE STRATEGY order_id_strategy;
+-------------------+--------+---------+----------+----------+---------------------+----------------+-----------------+
| name              | type   | table   | column   | sequence | generator_name      | generator_type | generator_props |
+-------------------+--------+---------+----------+----------+---------------------+----------------+-----------------+
| order_id_strategy | column | t_order | order_id |          | snowflake_generator | snowflake      | {"worker-id":1} |
+-------------------+--------+---------+----------+----------+---------------------+----------------+-----------------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`SHARDING`、`KEY`、`GENERATE`、`STRATEGY`、`STRATEGIES`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING KEY GENERATE STRATEGY](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-key-generate-strategy/)
