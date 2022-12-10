+++
title = "SHOW TRAFFIC RULE"
weight = 9
+++

### Description

The `SHOW TRAFFIC RULE` syntax is used to query specified dual routing rule.

### Syntax

```sql
ShowTrafficRule ::=
  'SHOW' 'TRAFFIC' ('RULES' | 'RULE' ruleName)?

ruleName ::=
  identifier
```

### Supplement

- When `ruleName` not specified, the default is show all traffic rules

### Return Value Description

| Colume              | Description                  |
|---------------------|------------------------------|
| name                | traffic rule name            |
| labels              | compute node labels          |
| algorithm_type      | traffic algorithm type       |
| algorithm_props     | traffic algorithn properties |
| load_balancer_type  | load balancer type           |
| load_balancer_props | load balancer properties     |

### Example

- Query specified traffic rule

```sql
SHOW TRAFFIC RULE sql_match_traffic;
```

```sql
mysql> SHOW TRAFFIC RULE sql_match_traffic;
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
| name              | labels | algorithm_type | algorithm_props                                                                | load_balancer_type | load_balancer_props |
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
| sql_match_traffic | OLTP   | SQL_MATCH      | sql=SELECT * FROM t_order WHERE order_id = 1; UPDATE t_order SET order_id = 5; | RANDOM             |                     |
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
1 row in set (0.00 sec)
```

- Query all traffic rules

```sql
SHOW TRAFFIC RULES;
```

```sql
mysql> SHOW TRAFFIC RULES;
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
| name              | labels | algorithm_type | algorithm_props                                                                | load_balancer_type | load_balancer_props |
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
| sql_match_traffic | OLTP   | SQL_MATCH      | sql=SELECT * FROM t_order WHERE order_id = 1; UPDATE t_order SET order_id = 5; | RANDOM             |                     |
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
1 row in set (0.04 sec)
```

### Reserved word

`SHOW`, `TRAFFIC`, `RULE`, `RULES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
