+++
title = "SET SHARDING HINT DATABASE_VALUE"
weight = 3
+++

### 描述

`SET SHARDING HINT DATABASE_VALUE` 语法用于针对当前连接，设置 hint 仅对数据库分片有效，并添加分片值
### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
SetShardingHintDatabaseValue ::=
  'SET' 'SHARDING' 'HINT' 'DATABASE_VALUE' '=' databaseShardingValue

  databaseShardingValue ::=
    int
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 示例

- 设置数据库分片值

```sql
SET SHARDING HINT DATABASE_VALUE = 100;
```

### 保留字

`SET`、`SHARDING`、`HINT`、`DATABASE_VALUE`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)