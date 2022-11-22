+++
title = "Shadow"
weight = 6 
+++

## Syntax

```sql
SHOW SHADOW shadowRule | RULES [FROM databaseName]

SHOW SHADOW TABLE RULES [FROM databaseName]

SHOW SHADOW ALGORITHMS [FROM databaseName]

shadowRule: 
    RULE ruleName
```
- Support querying all shadow rules and specified table query
- Support querying all shadow table rules
- Support querying all shadow algorithms

## Return Value Description

### Shadow Rule

| Column       | Description     |
| ------------ | --------------- |
| rule_name    | Rule name       |
| source_name  | Source database |
| shadow_name  | Shadow database |
| shadow_table | Shadow table    |

### Shadow Table Rule

| Column                | Description           |
| --------------------- | --------------------- |
| shadow_table          | Shadow table          |
| shadow_algorithm_name | Shadow algorithm name |

### Shadow Algorithms

| Column                | Description                 |
| --------------------- | --------------------------- |
| shadow_algorithm_name | Shadow algorithm name       |
| type                  | Shadow algorithm type       |
| props                 | Shadow algorithm properties |
| is_default            | Default                     |

## Example

*SHOW SHADOW RULES*

```sql
mysql> SHOW SHADOW RULES;
+--------------------+-------------+-------------+--------------+
| rule_name          | source_name | shadow_name | shadow_table |
+--------------------+-------------+-------------+--------------+
| shadow_rule_1      | ds_1        | ds_shadow_1 | t_order      |
| shadow_rule_2      | ds_2        | ds_shadow_2 | t_order_item |
+--------------------+-------------+-------------+--------------+
2 rows in set (0.02 sec)
```
*SHOW SHADOW RULE ruleName*

```sql
mysql> SHOW SHADOW RULE shadow_rule_1;
+------------------+-------------+-------------+--------------+
| rule_name        | source_name | shadow_name | shadow_table |
+------------------+-------------+-------------+--------------+
| shadow_rule_1    | ds_1        | ds_shadow_1 | t_order      |
+------------------+-------------+-------------+--------------+
1 rows in set (0.01 sec)
```

*SHOW SHADOW TABLE RULES*

```sql
mysql> SHOW SHADOW TABLE RULES;
+--------------+--------------------------------------------------------------------------------+
| shadow_table | shadow_algorithm_name                                                          |
+--------------+--------------------------------------------------------------------------------+
| t_order_1    | user_id_match_algorithm,simple_hint_algorithm_1                                |  
+--------------+--------------------------------------------------------------------------------+
1 rows in set (0.01 sec)
```

*SHOW SHADOW ALGORITHMS*

```sql
mysql> SHOW SHADOW ALGORITHMS;
+-------------------------+--------------------+-------------------------------------------+----------------+
| shadow_algorithm_name   | type               | props                                     | is_default     |
+-------------------------+--------------------+-------------------------------------------+----------------+
| user_id_match_algorithm | REGEX_MATCH        | operation=insert,column=user_id,regex=[1] | false          |
| simple_hint_algorithm_1 | SIMPLE_HINT        | shadow=true,foo=bar                       | false          |
+-------------------------+--------------------+-------------------------------------------+----------------+
2 rows in set (0.01 sec)
```
