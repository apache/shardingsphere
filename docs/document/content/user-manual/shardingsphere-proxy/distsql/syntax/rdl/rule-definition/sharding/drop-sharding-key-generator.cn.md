+++
title = "DROP SHARDING KEY GENERATOR"
weight = 11
+++

## 描述

`DROP SHARDING KEY GENERATOR` 语法用于删除指定逻辑库的指定分片主键生成器

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShardingKeyGenerator ::=
  'DROP' 'SHARDING' 'KEY' 'GENERATOR' keyGeneratorName ('FROM' databaseName)?

keyGeneratorName ::=
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

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 删除指定逻辑库的指定分片主键生成器

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake FROM sharding_db;
```

- 删除当前逻辑库的指定分片主键生成器

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake;
```

### 保留字

`DROP`、`SHARDING`、`KEY`、`GENERATOR`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)