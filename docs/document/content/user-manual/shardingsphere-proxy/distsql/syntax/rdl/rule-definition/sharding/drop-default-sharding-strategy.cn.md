+++
title = "DROP DEFAULT SHARDING STRATEGY"
weight = 6
+++

## 描述

`DROP DEFAULT SHARDING STRATEGY` 语法用于从当前逻辑库中删除默认分片策略。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropDefaultShardingStrategy ::=
  'DROP' 'DEFAULT' 'SHARDING' ('TABLE' | 'DATABASE') 'STRATEGY' ifExists?

ifExists ::=
  'IF' 'EXISTS'

```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `ifExists` 子句用于避免 `Default sharding strategy not exists` 错误。

### 示例

- 删除默认表分片策略
 
```sql
DROP DEFAULT SHARDING TABLE STRATEGY;
```

- 删除默认库分片策略

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

`DROP`、`DEFAULT`、`SHARDING`、`TABLE`、`DATABASE`、`STRATEGY`、`IF`、`EXISTS`
### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
