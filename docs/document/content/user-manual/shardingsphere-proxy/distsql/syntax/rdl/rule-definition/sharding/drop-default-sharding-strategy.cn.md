+++
title = "DROP DEFAULT SHARDING STRATEGY"
weight = 6
+++

## 描述

`DROP DEFAULT SHARDING STRATEGY` 语法用于删除指定逻辑库的默认分片策略。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropDefaultShardingStrategy ::=
  'DROP' 'DEFAULT' 'SHARDING' ('TABLE' | 'DATABASE') 'STRATEGY' ifExists? ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

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
- `ifExists` 子句用于避免 `Default sharding strategy not exists` 错误。

### 示例

- 为指定逻辑库删除默认表分片策略
 
```sql
DROP DEFAULT SHARDING TABLE STRATEGY FROM sharding_db;
```

- 为当前逻辑库删除默认库分片策略

```sql
DROP DEFAULT SHARDING DATABASE STRATEGY;
```

- 使用 `ifExists` 子句删除默认表分片策略

```sql
DROP DEFAULT SHARDING TABLE STRATEGY IF EXISTS;
```

- 使用 `ifExists` 子句删除默认库分片策略

```sql
DROP DEFAULT SHARDING DATABASE STRATEGY IF EXISTS;
```

### 保留字

`DROP`、`DEFAULT`、`SHARDING`、`TABLE`、`DATABASE`、`STRATEGY`、`FROM`
### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)