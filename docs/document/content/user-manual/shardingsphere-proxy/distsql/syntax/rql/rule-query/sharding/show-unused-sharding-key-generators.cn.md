+++
title = "SHOW UNUSED SHARDING KEY GENERATORS"
weight = 6
+++

### 描述

`SHOW UNUSED SHARDING KEY GENERATORS` 语法用于查询指定逻辑库中未被使用的分布式主键生成器。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowUnusedShardingKeyGenerators::=
  'SHOW' 'UNUSED' 'SHARDING' 'KEY' 'GENERATOR' ('FROM' databaseName)?

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

| 列     | 说明         |
|-------|------------|
| name  | 分布式主键生成器名称 |
| type  | 分布式主键生成器类型 |
| props | 分布式主键生成器参数 |

### 示例

- 查询指定逻辑库中未被使用的分布式主键生成器

```sql
SHOW UNUSED SHARDING KEY GENERATORS FROM sharding_db;
```

```sql
mysql> SHOW UNUSED SHARDING KEY GENERATORS FROM sharding_db;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake |       |
+-------------------------+-----------+-------+
1 row in set (0.01 sec)
```

- 查询当前逻辑库中未被使用的分布式主键生成器

```sql
SHOW UNUSED SHARDING KEY GENERATORS;
```

```sql
mysql> SHOW UNUSED SHARDING KEY GENERATORS;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake |       |
+-------------------------+-----------+-------+
1 row in set (0.02 sec)
```

### 保留字

`SHOW`、`UNUSED`、 `SHARDING`、`KEY`、`GENERATORS`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

