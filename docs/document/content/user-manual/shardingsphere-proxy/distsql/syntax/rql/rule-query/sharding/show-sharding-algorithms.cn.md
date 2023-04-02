+++
title = "SHOW SHARDING ALGORITHMS"
weight = 2
+++

### 描述

`SHOW SHARDING ALGORITHMS` 语法用于查询指定逻辑库的分片算法。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowShardingAlgorithms::=
  'SHOW' 'SHARDING' 'ALGORITHMS' ('FROM' databaseName)?

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

| 列     | 说明     |
|-------|--------|
| name  | 分片算法名称 |
| type  | 分片算法类型 |
| props | 分片算法参数 |

### 示例

- 查询指定逻辑库的分片算法

```sql
SHOW SHARDING ALGORITHMS FROM sharding_db;
```

```sql
mysql> SHOW SHARDING ALGORITHMS FROM sharding_db;
+-------------------------+--------+-----------------------------------------------------+
| name                    | type   | props                                               |
+-------------------------+--------+-----------------------------------------------------+
| t_order_inline          | INLINE | algorithm-expression=t_order_${order_id % 2}        |
| t_order_item_inline     | INLINE | algorithm-expression=t_order_item_${order_id % 2}   |
+-------------------------+--------+-----------------------------------------------------+
2 rows in set (0.01 sec)
```

- 查询当前逻辑库的分片算法

```sql
SHOW SHARDING ALGORITHMS;
```

```sql
mysql> SHOW SHARDING ALGORITHMS;
+-------------------------+--------+-----------------------------------------------------+
| name                    | type   | props                                               |
+-------------------------+--------+-----------------------------------------------------+
| t_order_inline          | INLINE | algorithm-expression=t_order_${order_id % 2}        |
| t_order_item_inline     | INLINE | algorithm-expression=t_order_item_${order_id % 2}   |
+-------------------------+--------+-----------------------------------------------------+
2 rows in set (0.01 sec)
```

### 保留字

`SHOW`、`SHARDING`、`ALGORITHMS`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
