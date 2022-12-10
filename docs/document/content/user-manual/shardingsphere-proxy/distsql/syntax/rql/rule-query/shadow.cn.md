+++
title = "影子库压测"
weight = 6 
+++

## 语法说明

```sql
SHOW SHADOW shadowRule | RULES [FROM databaseName]

SHOW SHADOW TABLE RULES [FROM databaseName]

SHOW SHADOW ALGORITHMS [FROM databaseName]

shadowRule: 
    RULE ruleName
```
- 支持查询所有影子规则和指定表查询；
- 支持查询所有表规则；
- 支持查询所有影子算法。

## 返回值说明

### Shadow Rule

| 列           | 说明         |
| ------------ | ----------- |
| rule_name    | 规则名称     |
| source_name  | 源数据库     |
| shadow_name  | 影子数据库   |
| shadow_table | 影子表       |

### Shadow Table Rule

| 列                     | 说明       |
| --------------------- | ---------- |
| shadow_table          | 影子表      |
| shadow_algorithm_name | 影子算法名称 |

### Shadow Algorithms

| 列                    | 说明        |
| -------------------   | ---------- |
| shadow_algorithm_name | 影子算法名称 |
| type                  | 算法类型    |
| props                 | 算法参数    |
| is_default            | 是否默认    |

## 示例

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
