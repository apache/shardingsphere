+++
title = "SHOW SHARDING TABLE REGERENCE RULES"
weight = 14

+++

### 描述

`SHOW SHARDING BINDING TABLE RULES` 语法用于查询指定逻辑库中具有关联关系的分片表

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowShardingBindingTableRules::=
  'SHOW' 'SHARDING' 'TABLE' 'REFERENCE' 'RULES' ('FROM' databaseName)?

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

| 列                       | 说明             |
| -------------------------| ----------------|
| name                     | 分片表关联规则名称 |
| sharding_table_reference | 分片表关联关系    |

### 示例

- 查询指定逻辑库中的分片表关联规则

```sql
SHOW SHARDING TABLE REFERENCE RULES FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES FROM sharding_db;
+-------+--------------------------+
| name  | sharding_table_reference |
+-------+--------------------------+
| ref_0 | t_a,t_b                  |
| ref_1 | t_c,t_d                  |
+-------+--------------------------+
2 rows in set (0.00 sec)
```

- 查询当前逻辑库中的分片表关联规则

```sql
SHOW SHARDING TABLE REFERENCE RULES;
```

```sql
mysql> SHOW SHARDING TABLE REFERENCE RULES;
+-------+--------------------------+
| name  | sharding_table_reference |
+-------+--------------------------+
| ref_0 | t_a,t_b                  |
| ref_1 | t_c,t_d                  |
+-------+--------------------------+
2 rows in set (0.00 sec)
```

### 保留字

`SHOW`、`SHARDING`、`BINDING`、`TABLE`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

