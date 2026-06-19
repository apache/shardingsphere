+++
title = "SHOW SHARDING KEY GENERATOR"
weight = 5
+++

### 描述

`SHOW SHARDING KEY GENERATOR` 语法用于查询指定逻辑库的分布式主键生成器。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowShardingKeyGenerators::=
  'SHOW' 'SHARDING' 'KEY' ('GENERATOR' keyGeneratorName | 'GENERATORS') ('FROM' databaseName)?

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
- 使用 `SHOW SHARDING KEY GENERATORS` 查询全部分布式主键生成器；
- 使用 `SHOW SHARDING KEY GENERATOR keyGeneratorName` 查询指定名称的分布式主键生成器。

### 返回值说明

| 列     | 说明         |
|-------|------------|
| name  | 分布式主键生成器名称 |
| type  | 分布式主键生成器类型 |
| props | 分布式主键生成器参数 |

### 示例

- 查询指定逻辑库的分布式主键生成器

```sql
SHOW SHARDING KEY GENERATORS FROM sharding_db;
```

```sql
mysql> SHOW SHARDING KEY GENERATORS FROM sharding_db;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake |       |
+-------------------------+-----------+-------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库的分布式主键生成器

```sql
SHOW SHARDING KEY GENERATORS;
```

```sql
mysql> SHOW SHARDING KEY GENERATORS;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake |       |
+-------------------------+-----------+-------+
1 row in set (0.00 sec)
```

- 查询指定名称的分布式主键生成器

```sql
SHOW SHARDING KEY GENERATOR snowflake_key_generator;
```

```sql
mysql> SHOW SHARDING KEY GENERATOR snowflake_key_generator;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake |       |
+-------------------------+-----------+-------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`SHARDING`、`KEY`、`GENERATOR`、`GENERATORS`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING KEY GENERATOR](/cn/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-key-generator/)
