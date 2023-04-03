+++
title = "SHOW DEFAULT SHARDING STRATEGY"
weight = 4
+++

### 描述

`SHOW DEFAULT SHARDING STRATEGY` 语法用于查询指定逻辑库的默认分片策略。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowDefaultShardingStrategy::=
  'SHOW' 'DEFAULT' 'SHARDING' 'STRATEGY' ('FROM' databaseName)?

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                        | 说明     |
|--------------------------|--------|
| name                     | 分片策略范围 |
| type                     | 分片策略类型 |
| sharding_column          | 分片键    |
| sharding_algorithm_name  | 分片算法名称 |
| sharding_algorithm_type  | 分片算法类型 |
| sharding_algorithm_props | 分片算法参数 |

### 示例

- 查询指定逻辑库的默认分片策略

```sql
SHOW DEFAULT SHARDING STRATEGY FROM sharding_db;
```

```sql
mysql> SHOW DEFAULT SHARDING STRATEGY FROM sharding_db;
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
| name     | type     | sharding_column | sharding_algorithm_name | sharding_algorithm_type | sharding_algorithm_props                            |
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
| TABLE    | STANDARD | order_id        | table_inline            | inline                  | {algorithm-expression=t_order_item_${order_id % 2}} |
| DATABASE | STANDARD | order_id        | table_inline            | inline                  | {algorithm-expression=t_order_item_${order_id % 2}} |
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
2 rows in set (0.00 sec)
```

- 查询当前逻辑库的默认分片策略

```sql
SHOW DEFAULT SHARDING STRATEGY;
```

```sql
mysql> SHOW DEFAULT SHARDING STRATEGY;
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
| name     | type     | sharding_column | sharding_algorithm_name | sharding_algorithm_type | sharding_algorithm_props                            |
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
| TABLE    | STANDARD | order_id        | table_inline            | inline                  | {algorithm-expression=t_order_item_${order_id % 2}} |
| DATABASE | STANDARD | order_id        | table_inline            | inline                  | {algorithm-expression=t_order_item_${order_id % 2}} |
+----------+----------+-----------------+-------------------------+-------------------------+-----------------------------------------------------+
2 rows in set (0.00 sec)
```

### 保留字

`SHOW`、`DEFAULT`、`SHARDING`、`STRATEGY`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

