+++
title = "SHOW SHADOW TABLE RULES"
weight = 2
+++

### Description

The `SHOW SHADOW TABLE RULES` syntax is used to query shadow table rules for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowEncryptRule::=
  'SHOW' 'SHADOW' 'TABLE' 'RULES' ('FROM' databaseName)?

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

| Column                | Description           |
|-----------------------|-----------------------|
| shadow_table          | Shadow table          |
| shadow_algorithm_name | Shadow algorithm name |

### Example

- Query shadow table rules for specified database.

```sql
SHOW SHADOW TABLE RULES FROM shadow_db;
```

```sql
mysql> SHOW SHADOW TABLE RULES FROM shadow_db;
+--------------+-------------------------------------------------------+
| shadow_table | shadow_algorithm_name                                 |
+--------------+-------------------------------------------------------+
| t_order_item | shadow_rule_t_order_item_value_match                  |
| t_order      | sql_hint_algorithm,shadow_rule_t_order_regex_match |
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
| t_order      | sql_hint_algorithm,shadow_rule_t_order_regex_match |
+--------------+-------------------------------------------------------+
2 rows in set (0.01 sec)
```
### Reserved word

`SHOW`, `SHADOW`, `TABLE`, `RULES`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
