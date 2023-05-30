+++
title = "SHOW MASK RULES"
weight = 1
+++

### 描述

`SHOW MASK RULES` 语法用于查询指定逻辑库中的数据脱敏规则。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowMaskRule::=
  'SHOW' 'MASK' ('RULES' | 'RULE' ruleName) ('FROM' databaseName)?

ruleName ::=
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

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`， 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列               | 说明       |
|-----------------|----------|
| table           | 表名       |
| column          | 列名       |
| algorithm_type  | 数据脱敏算法类型 |
| algorithm_props | 数据脱敏算法参数 |

### 示例

- 查询指定逻辑库中的所有数据脱敏规则

```sql
SHOW MASK RULES FROM mask_db;
```

```sql
mysql> SHOW MASK RULES FROM mask_db;
+---------+----------+------------------+--------------------------------+
| table   | column   | algorithm_type   | algorithm_props                |
+---------+----------+------------------+--------------------------------+
| t_mask  | phoneNum | MASK_FROM_X_TO_Y | to-y=2,replace-char=*,from-x=1 |
| t_mask  | address  | MD5              |                                |
| t_order | order_id | MD5              |                                |
| t_user  | user_id  | MASK_FROM_X_TO_Y | to-y=2,replace-char=*,from-x=1 |
+---------+----------+------------------+--------------------------------+
4 rows in set (0.01 sec)
```

- 查询当前逻辑库中的所有数据脱敏规则

```sql
SHOW MASK RULES;
```

```sql
mysql> SHOW MASK RULES;
+---------+----------+------------------+--------------------------------+
| table   | column   | algorithm_type   | algorithm_props                |
+---------+----------+------------------+--------------------------------+
| t_mask  | phoneNum | MASK_FROM_X_TO_Y | to-y=2,replace-char=*,from-x=1 |
| t_mask  | address  | MD5              |                                |
| t_order | order_id | MD5              |                                |
| t_user  | user_id  | MASK_FROM_X_TO_Y | to-y=2,replace-char=*,from-x=1 |
+---------+----------+------------------+--------------------------------+
4 rows in set (0.01 sec)
```

- 查询指定逻辑库中的指定数据脱敏算法

```sql
SHOW MASK RULE t_mask FROM mask_db;
```

```sql
mysql> SHOW MASK RULE t_mask FROM mask_db;
+--------+--------------+------------------+--------------------------------+
| table  | logic_column | mask_algorithm   | props                          |
+--------+--------------+------------------+--------------------------------+
| t_mask | phoneNum     | MASK_FROM_X_TO_Y | to-y=2,replace-char=*,from-x=1 |
| t_mask | address      | MD5              |                                |
+--------+--------------+------------------+--------------------------------+
2 rows in set (0.00 sec)
```

- 查询当前逻辑库中的指定数据脱敏算法

```sql
SHOW MASK RULE t_mask;
```

```sql
mysql> SHOW MASK RULE t_mask;
+--------+--------------+------------------+--------------------------------+
| table  | logic_column | mask_algorithm   | props                          |
+--------+--------------+------------------+--------------------------------+
| t_mask | phoneNum     | MASK_FROM_X_TO_Y | to-y=2,replace-char=*,from-x=1 |
| t_mask | address      | MD5              |                                |
+--------+--------------+------------------+--------------------------------+
2 rows in set (0.00 sec)
```

### 保留字

`SHOW`、`MASK`、`RULE`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

