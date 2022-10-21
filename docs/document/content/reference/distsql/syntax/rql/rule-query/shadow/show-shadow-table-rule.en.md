+++
title = "SHOW SHADOW TABLE RULE"
weight = 3
+++

### Description

The `SHOW SHADOW TABLE RULE` syntax is used to query shadow table rules for specified database.

### Syntax

```
ShowEncryptRule::=
  'SHOW' 'SHADOW' 'TABLE' 'RULES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column                | Description           |
| --------------------- | --------------------- |
| shadow_table          | Shadow table          |
| shadow_algorithm_name | Shadow algorithm name |

### Example

- Query shadow table rules for specified database.

```sql
SHOW SHADOW TABLE RULES FROM test1;
```

```sql
mysql> SHOW SHADOW TABLE RULES FROM test1;
+--------------+-------------------------------------------------------+
| shadow_table | shadow_algorithm_name                                 |
+--------------+-------------------------------------------------------+
| t_order_item | shadow_rule_t_order_item_value_match                  |
| t_order      | simple_hint_algorithm,shadow_rule_t_order_regex_match |
+--------------+-------------------------------------------------------+
2 rows in set (0.00 sec)
```

- Query shadow table rules for current database.

```sql
SHOW SHADOW TABLE RULES;
```

```sql
mysql> SHOW SHADOW TABLE RULES;
+--------------+-------------------------------------------------------+
| shadow_table | shadow_algorithm_name                                 |
+--------------+-------------------------------------------------------+
| t_order_item | shadow_rule_t_order_item_value_match                  |
| t_order      | simple_hint_algorithm,shadow_rule_t_order_regex_match |
+--------------+-------------------------------------------------------+
2 rows in set (0.01 sec)
```
### Reserved word

`SHOW`, `SHADOW`, `TABLE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
