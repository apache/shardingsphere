+++
title = "SHOW LOGICAL TABLES"
weight = 3
+++

### 描述

`SHOW LOGICAL TABLES` 语法用于查询指定逻辑库中的逻辑表。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowLogicalTables ::=
  'SHOW' 'FULL'? 'LOGICAL' 'TABLES' ('FROM' databaseName)? showLike?

databaseName ::=
  identifier

showLike ::=
  'LIKE' likePattern

likePattern ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`；如果也未使用 `DATABASE` 则会提示 `No database selected`。
- `FULL` 会返回逻辑表类型。
- 当逻辑库存在 Schema 元数据时，返回结果还会包含 `schema_name`。

### 返回值说明

| 列           | 说明                         |
|-------------|----------------------------|
| table_name  | 逻辑表名称                      |
| table_type  | 逻辑表类型，仅在指定 `FULL` 时返回     |
| schema_name | Schema 名称，仅在存在 Schema 元数据时返回 |

### 示例

- 查询当前逻辑库中的逻辑表

```sql
SHOW LOGICAL TABLES;
```

- 查询指定逻辑库中的完整逻辑表信息

```sql
SHOW FULL LOGICAL TABLES FROM sharding_db;
```

- 使用 `LIKE` 查询逻辑表

```sql
SHOW LOGICAL TABLES LIKE 't_order%';
```

### 保留字

`SHOW`、`FULL`、`LOGICAL`、`TABLES`、`FROM`、`LIKE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
