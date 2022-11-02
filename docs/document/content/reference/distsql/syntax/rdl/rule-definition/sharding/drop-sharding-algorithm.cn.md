+++
title = "DROP SHARDING ALGORITHM"
weight = 12
+++

## 描述

`DROP SHARDING ALGORITHM` 语法用于删除指定逻辑库的指定分片算法

### 语法定义

```sql
DropShardingAlgorithm ::=
  'DROP' 'SHARDING' 'ALGORITHM' shardingAlgorithmName ('FROM' databaseName)?

shardingAlgorithmName ::=
  identifier

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 删除指定逻辑库的指定分片算法

```sql
DROP SHARDING ALGORITHM t_order_hash_mod FROM test1;
```

- 删除当前逻辑库的指定分片算法

```sql
DROP SHARDING ALGORITHM t_order_hash_mod;
```

### 保留字

`DROP`、`SHARDING`、`ALGORITHM`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)