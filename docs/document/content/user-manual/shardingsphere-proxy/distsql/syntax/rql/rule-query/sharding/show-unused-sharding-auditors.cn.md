+++
title = "SHOW UNUSED SHARDING AUDITORS"
weight = 8
+++

### 描述

`SHOW UNUSED SHARDING AUDITORS` 语法用于查询指定逻辑库中未被使用的分片审计器。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowUnusedShardingAuditors::=
  'SHOW' 'SHARDING' 'AUDITOR' ('FROM' databaseName)?

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

| 列     | 说明       |
|-------|----------|
| name  | 分片审计器名称  |
| type  | 分片审计算法类型 |
| props | 分片审计算法参数 |

### 示例

- 查询指定逻辑库中未被使用的分片审计器

```sql
SHOW UNUSED SHARDING AUDITORS FROM sharding_db;
```

```sql
mysql> SHOW UNUSED SHARDING AUDITORS FROM sharding_db;
+-------------------------------+-------------------------+-------+
| name                          | type                    | props |
+-------------------------------+-------------------------+-------+
| sharding_key_required_auditor | dml_sharding_conditions |       |
+-------------------------------+-------------------------+-------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中未被使用的的分片审计器

```sql
SHOW UNUSED SHARDING AUDITORS;
```

```sql
mysql> SHOW UNUSED SHARDING AUDITORS;
+-------------------------------+-------------------------+-------+
| name                          | type                    | props |
+-------------------------------+-------------------------+-------+
| sharding_key_required_auditor | dml_sharding_conditions |       |
+-------------------------------+-------------------------+-------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`UNUSED`、`SHARDING`、`AUDITORS`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

