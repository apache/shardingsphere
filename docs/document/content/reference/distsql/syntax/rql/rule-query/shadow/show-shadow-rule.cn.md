+++
title = "SHOW SHADOW RULE"
weight = 2
+++

### 描述

`SHOW SHADOW RULE` 语法用于查询指定逻辑库中的影子规则。

### 语法

```
ShowEncryptRule::=
  'SHOW' 'SHADOW' ('RULES'|'RULE' shadowRuleName) ('FROM' databaseName)?

shadowRuleName ::=
  identifier
  
databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列           | 说明           |
| ------------ | ------------- |
| rule_name    | 规则名称       |
| source_name  | 数据源名称     | 
| shadow_name  | 影子数据源名称  |
| shadow_table | 影子表         |

### 示例

- 查询指定逻辑库中的指定影子规则

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

- 查询当前逻辑库中的指定影子规则

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

- 查询指定逻辑库中的影子规则

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

- 查询当前逻辑库中的影子规则

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

### 保留字

`SHOW`、`SHADOW`、`RULE`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

