+++
title = "影子库压测" 
weight = 6 
+++

## 定义

```sql
SHOW SHADOW shadowRule | RULES [FROM schemaName]
    
SHOW SHADOW ALGORITHMS [FROM schemaName]

shadowRule: 
    RULE ruleName
```
-  支持查询所有影子规则和指定表查询
-  支持查询所有影子算法

## 说明

### Shadow Rule

| 列          | 说明         |
| ----------- | ----------- |
| rule_name   | 规则名称     |
| source_name | 源数据库   |
| shadow_name | 影子数据库   |

### Shadow Algorithms

| 列                    | 说明          |
| -------------------   | ------------ |
| shadow_algorithm_name | 影子算法名称   |
| type                  | 算法类型      |
| props                 | 算法参数      |
| shadow_tables         | 影子表        |

## 示例

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