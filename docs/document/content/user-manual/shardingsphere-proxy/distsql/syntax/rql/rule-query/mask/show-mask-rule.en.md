+++
title = "SHOW MASK RULES"
weight = 1
+++

### Description

The `SHOW MASK RULES` syntax is used to query mask rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowMaskRule::=
  'SHOW' 'MASK' ('RULES' | 'RULE' ruleName) ('FROM' databaseName)?

ruleName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column          | Description               |
|-----------------|---------------------------|
| table           | Table name                |
| column          | Column name               |
| algorithm_type  | Mask algorithm type       |
| algorithm_props | Mask algorithm properties |




### Example

- Query mask rules for specified database

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

- Query mask rules for current database

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

- Query specified mask rule for specified database

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

- Query specified mask rule for current database

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

### Reserved word

`SHOW`, `MASK`, `RULE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
