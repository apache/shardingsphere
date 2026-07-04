+++
title = "DROP SHARDING KEY GENERATOR"
weight = 10
+++

## 描述

`DROP SHARDING KEY GENERATOR` 语法用于从当前逻辑库中删除分片主键生成器。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropShardingKeyGenerator ::=
  'DROP' 'SHARDING' 'KEY' 'GENERATOR' ifExists? keyGeneratorName (',' keyGeneratorName)*

ifExists ::=
  'IF' 'EXISTS'

keyGeneratorName ::=
  identifier

```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- `ifExists` 子句用于避免 `Sharding key generator not exists` 错误。

### 示例

- 删除分片主键生成器

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake;
```

- 删除多个分片主键生成器

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake, t_item_snowflake;
```

- 使用 `ifExists` 子句删除分片主键生成器

```sql
DROP SHARDING KEY GENERATOR IF EXISTS t_order_snowflake;
```

### 保留字

`DROP`、`SHARDING`、`KEY`、`GENERATOR`、`IF`、`EXISTS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
