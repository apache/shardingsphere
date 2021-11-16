+++
title = "Shadow"
weight = 6 
+++

## Definition

```sql
SHOW SHADOW shadowRule | RULES [FROM schemaName]

SHOW SHADOW TABLE RULES [FROM schemaName]

SHOW SHADOW ALGORITHMS [FROM schemaName]

shadowRule: 
    RULE ruleName
```
- Support querying all shadow rules and specified table query
- Support querying all shadow table rules
- Support querying all shadow algorithms

## Description

### Shadow Rule

| Column      | Description      |
| -----------  | -----------     |
| rule_name    | Rule name       |
| source_name  | Source database |
| shadow_name  | Shadow database |
| shadow_table | Shadow table    |

### Shadow Table Rule

| Column                 | Description          |
| ----------------------| --------------------- |
| shadow_table          | Shadow table          |
| shadow_algorithm_name | Shadow algorithm name |

### Shadow Algorithms

| Column                | Description                  |
| -------------------   | ---------------------------- |
| shadow_algorithm_name | Shadow algorithm name        |
| type                  | Shadow algorithm type        |
| properties            | Shadow algorithm parameters  |

## Example

*SHOW SHADOW RULES*

```sql
mysql> show shadow rules;
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
mysql> show shadow rule shadow_rule_1;
+------------------+-------------+-------------+--------------+
| rule_name        | source_name | shadow_name | shadow_table |
+------------------+-------------+-------------+--------------+
| shadow_rule_1    | ds_1        | ds_shadow_1 | t_order      |
+------------------+-------------+-------------+--------------+
1 rows in set (0.01 sec)
```

*SHOW SHADOW TABLE RULES*

```sql
mysql> show shadow table rules;
+--------------+--------------------------------------------------------------------------------+
| shadow_table | shadow_algorithm_name                                                          |
+--------------+--------------------------------------------------------------------------------+
| t_order_1    | user_id_match_algorithm,simple_note_algorithm_1                                |  
+--------------+--------------------------------------------------------------------------------+
1 rows in set (0.01 sec)
```

*SHOW SHADOW ALGORITHMS*

```sql
mysql> show shadow algorithms;
+-------------------------+--------------------+-------------------------------------------+
| shadow_algorithm_name   | type               | properties                                |
+-------------------------+--------------------+-------------------------------------------+
| user_id_match_algorithm | COLUMN_REGEX_MATCH | operation=insert,column=user_id,regex=[1] |
| simple_note_algorithm_1 | SIMPLE_NOTE        | shadow=true,foo=bar                       |
+-------------------------+--------------------+-------------------------------------------+
2 rows in set (0.01 sec)
```
