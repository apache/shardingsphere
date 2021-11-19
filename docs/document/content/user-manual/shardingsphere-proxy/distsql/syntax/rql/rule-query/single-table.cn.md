+++
title = "单表"
weight = 2
+++

## 语法说明

```sql
SHOW SINGLE TABLE (tableRule | RULES) [FROM schemaName]

tableRule:
    RULE tableName
```

## 返回值说明

| 列            | 说明          |
| ------------- | ------------ |
| table_name    | 单表名称      |
| resource_name | 数据源名称    |

## 示例

```sql
mysql> show single table rules;
+--------------+---------------+
| table_name   | resource_name |
+--------------+---------------+
| t_single_0   | ds_0          |
| t_single_1   | ds_1          |
+--------------+---------------+
2 rows in set (0.02 sec)
```
