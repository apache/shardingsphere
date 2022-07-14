+++
title = "SHOW RULES USED RESOURCE"
weight = 4
+++

### 描述

`SHOW RULES USED RESOURCE` 语法用于查询指定逻辑库中使用指定资源的规则。

### 语法

```SQL
showRulesUsedResource ::=
  'SHOW' 'RULES' 'USED' 'RESOURCES' resourceName ('FROM' databaseName)?

resourceName ::=
  IDENTIFIER | STRING

databaseName ::=
  IDENTIFIER
```

### 特别说明

- 未指定 `databaseName` 时, 默认是当前使用的 `DATABASE`； 如未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列        | 说明       |
| --------- | --------- |
| type      | 特性       |
| name      | 数据源名称  |

### 示例

- 查询指定逻辑库中使用指定资源的规则
```sql
SHOW RULES USED RESOURCE ds_0 FROM sharding_db;
```
```sql
+----------+--------------+
| type     | name         |
+----------+--------------+
| sharding | t_order      |
| sharding | t_order_item |
+----------+--------------+
2 rows in set (0.00 sec)
```

- 查询当前逻辑库中使用指定资源的规则
```sql
SHOW RULES USED RESOURCE ds_0;
```
```sql
+----------+--------------+
| type     | name         |
+----------+--------------+
| sharding | t_order      |
| sharding | t_order_item |
+----------+--------------+
2 rows in set (0.00 sec)
```
