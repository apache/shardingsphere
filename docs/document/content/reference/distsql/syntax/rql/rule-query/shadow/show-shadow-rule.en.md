+++
title = "SHOW SHADOW RULE"
weight = 2
+++

### Description

The `SHOW SHADOW RULE` syntax is used to query shadow rules for specified database.

### Syntax

```
ShowEncryptRule::=
  'SHOW' 'SHADOW' ('RULES'|'RULE' shadowRuleName) ('FROM' databaseName)?

shadowRuleName ::=
  identifier
  
databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column       | Description            |
| ------------ | ---------------------- |
| rule_name    | Shadow rule name       |
| source_name  | Data source name       |
| shadow_name  | Shadow data source name|
| shadow_table | Shadow table           |




### Example

- Query specified shadow rule in specified database.

```sql
SHOW SHADOW RULE shadow_rule FROM test1;
```

```sql
mysql> SHOW SHADOW RULE shadow_rule FROM test1;
+-------------+-------------+-------------+----------------------+
| rule_name   | source_name | shadow_name | shadow_table         |
+-------------+-------------+-------------+----------------------+
| shadow_rule | ds_0        | ds_1        | t_order_item,t_order |
+-------------+-------------+-------------+----------------------+
1 row in set (0.00 sec)
```

- Query specified shadow rule in current database.

```sql
SHOW SHADOW RULE shadow_rule；
```

```sql
mysql> SHOW SHADOW RULE shadow_rule;
+-------------+-------------+-------------+----------------------+
| rule_name   | source_name | shadow_name | shadow_table         |
+-------------+-------------+-------------+----------------------+
| shadow_rule | ds_0        | ds_1        | t_order_item,t_order |
+-------------+-------------+-------------+----------------------+
1 row in set (0.01 sec)
```

- Query shadow rules for specified database.

```sql
SHOW SHADOW RULES FROM test1;
```

```sql
mysql> SHOW SHADOW RULES FROM test1;
+-------------+-------------+-------------+----------------------+
| rule_name   | source_name | shadow_name | shadow_table         |
+-------------+-------------+-------------+----------------------+
| shadow_rule | ds_0        | ds_1        | t_order_item,t_order |
+-------------+-------------+-------------+----------------------+
1 row in set (0.00 sec)
```

- Query shadow rules for current database.

```sql
SHOW SHADOW RULES;
```

```sql
mysql> SHOW SHADOW RULES;
+-------------+-------------+-------------+----------------------+
| rule_name   | source_name | shadow_name | shadow_table         |
+-------------+-------------+-------------+----------------------+
| shadow_rule | ds_0        | ds_1        | t_order_item,t_order |
+-------------+-------------+-------------+----------------------+
1 row in set (0.00 sec)
```
### Reserved word

`SHOW`、`SHADOW`、`RULE`、`RULES`、`FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
