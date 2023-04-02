+++
title = "SHOW SHARDING TABLE RULES USED AUDITOR"
weight = 12
+++

### 描述

`SHOW SHARDING TABLE RULES USED AUDITOR` 语法用于查询指定逻辑库中使用指定分片审计器的分片规则。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowShardingTableRulesUsedAuditor::=
  'SHOW' 'SHARDING' 'TABLE' 'RULES' 'USED' 'AUDITOR' AuditortorName ('FROM' databaseName)?

AuditortorName ::=
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

### 返回值说明

| 列    | 说明     |
|------|--------|
| type | 分片规则类型 |
| name | 分片规则名称 |

### 示例

- 查询指定逻辑库中使用指定分片审计器的分片规则

```sql
SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required_auditor FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required_auditor FROM sharding_db;
+-------+---------+
| type  | name    |
+-------+---------+
| table | t_order |
+-------+---------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中使用指定分片审计器的分片规则

```sql
SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required_auditor;
```

```sql
mysql> SHOW SHARDING TABLE RULES USED AUDITOR sharding_key_required_auditor;
+-------+---------+
| type  | name    |
+-------+---------+
| table | t_order |
+-------+---------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`SHARDING`、`TABLE`、`RULES`、`USED`、`AUDITOR`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

