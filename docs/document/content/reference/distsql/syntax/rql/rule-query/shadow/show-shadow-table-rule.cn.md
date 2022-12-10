+++
title = "SHOW SHADOW TABLE RULE"
weight = 3
+++

### 描述

`SHOW SHADOW TABLE RULE` 语法用于查询指定逻辑库中的影子表规则。

### 语法

```
ShowEncryptRule::=
  'SHOW' 'SHADOW' 'TABLE' 'RULES' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                     | 说明       |
| --------------------- | ---------- |
| shadow_table          | 影子表      |
| shadow_algorithm_name | 影子算法名称 |

### 示例

- 查询指定逻辑库中的影子表规则

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

- 查询当前逻辑库中的影子表规则

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

### 保留字

`SHOW`、`SHADOW`、`TABLE`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

