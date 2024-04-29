+++
title = "DROP SHARDING ALGORITHM"
weight = 11
+++

## 描述

`DROP SHARDING ALGORITHM` 语法用于删除指定逻辑库的指定分片算法。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShardingAlgorithm ::=
  'DROP' 'SHARDING' 'ALGORITHM' algorithmName ifExists? ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

algorithmName ::=
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

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`；
- `ifExists` 子句用于避免 `Sharding algorithm not exists` 错误。

### 示例

- 删除指定逻辑库的指定分片算法

```sql
DROP SHARDING ALGORITHM t_order_hash_mod FROM sharding_db;
```

- 删除当前逻辑库的指定分片算法

```sql
DROP SHARDING ALGORITHM t_order_hash_mod;
```

- 使用 `ifExists` 子句删除分片算法

```sql
DROP SHARDING ALGORITHM IF EXISTS t_order_hash_mod;
```

### 保留字

`DROP`、`SHARDING`、`ALGORITHM`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)