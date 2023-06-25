+++
title = "SHOW BROADCAST TABLE RULE"
weight = 1
+++

## 描述

`SHOW BROADCAST TABLE RULES` 语法用于查询指定数据库中具有广播规则的表。

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowBroadcastTableRule ::=
  'SHOW' 'BROADCAST' 'TABLE' 'RULES' ('FROM' databaseName)? 

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`； 如未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列               | 说明    |
|-----------------|-------|
| broadcast_table | 广播表名称 |

### 示例

- 查询指定数据库中具有广播规则的表

```sql
SHOW BROADCAST TABLE RULES FROM sharding_db;
```

```sql
mysql> SHOW BROADCAST TABLE RULES FROM sharding_db;
+-----------------+
| broadcast_table |
+-----------------+
| t_a             |
| t_b             |
| t_c             |
+-----------------+
3 rows in set (0.00 sec)
```

- 查询当前逻辑库中具有广播规则的表

```sql
SHOW BROADCAST TABLE RULES;
```

```sql
mysql> SHOW BROADCAST TABLE RULES;
+-----------------+
| broadcast_table |
+-----------------+
| t_a             |
| t_b             |
| t_c             |
+-----------------+
3 rows in set (0.00 sec)
```

### 保留字

`SHOW`、`BROADCAST`、`TABLE`、`RULES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)