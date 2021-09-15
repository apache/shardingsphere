+++
    title = "Shadow" 
weight = 6 
+++

## 定义

```sql
SHOW SHADOW shadowRule | RULES [FROM schemaName]
    
SHOW SHADOW ALGORITHMS [FROM schemaName]

shadowRule: 
    RULE ruleName
```
-  Support querying all shadow rules and specified table query
-  Support querying all shadow algorithms

## Description

### Shadow Rule

| Column      | Description           |
| ----------- | --------------------- |
| rule_name   | Rule name             |
| source_name | Source database       |
| shadow_name | Shadow database       |

### Shadow Algorithm

| Column                | Description                  |
| -------------------   | ---------------------------- |
| shadow_algorithm_name | Shadow algorithm name        |
| type                  | Shadow algorithm type        |
| props                 | Shadow algorithm parameters  |
| shadow_tables         | Shadow table                 |

## Example

*SHOW SHADOW RULES*

```sql
mysql> show shadow rules;
+--------------------+-------------+-------------+
| rule_name          | source_name | shadow_name |
+--------------------+-------------+-------------+
| shadow_rule_1      | ds_1        | ds_shadow_1 |
| shadow_rule_2      | ds_2        | ds_shadow_2 |
+--------------------+-------------+-------------+
2 rows in set (0.02 sec)
```
*SHOW SHADOW RULE ruleName*

```sql
mysql> show shadow rule shadow_rule_1;
+------------------+-------------+-------------+
| rule_name        | source_name | shadow_name |
+------------------+-------------+-------------+
| shadow_rule_1    | ds_1        | ds_shadow_1 |
+------------------+-------------+-------------+
1 rows in set (0.01 sec)
```

*SHOW SHADOW ALGORITHMS*

```sql
mysql> show shadow algorithms;
+-------------------------+--------------------+-------------------------------------------+----------------+
| shadow_algorithm_name   | type               | props                                     | shadow_tables  |
+-------------------------+--------------------+-------------------------------------------+----------------+
| user_id_match_algorithm | COLUMN_REGEX_MATCH | operation=insert,column=user_id,regex=[1] | t_order        |
| simple_note_algorithm_1 | SIMPLE_NOTE        | shadow=true,foo=bar                       | t_order,t_user |
| simple_note_algorithm_2 | SIMPLE_NOTE        | shadow=true                               |                |
+-------------------------+--------------------+-------------------------------------------+----------------+
3 rows in set (0.01 sec)
```

### 