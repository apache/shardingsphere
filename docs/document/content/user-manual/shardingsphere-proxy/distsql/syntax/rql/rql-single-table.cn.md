+++
title = "单表"
weight = 7
+++

## 定义

```sql
SHOW SINGLE TABLE RULES [FROM schemaName]

SHOW SINGLE tableDefinition | TABLES [FROM schemaName]

tableDefinition:
    TABLE tableName
```

## 说明

### Single Table Rule

| 列            | 说明          |
| ------------- | ------------ |
| name          | 规则名称      |
| resource_name | 数据源名称    |

### Single Table

| 列            | 说明          |
| ------------- | ------------ |
| table_name    | 单表名称      |
| resource_name | 数据源名称     |

## 示例

### Single Table Rule

```sql
mysql> show single table rules;
+---------+---------------+
| name    | resource_name |
+---------+---------------+
| default | ds_0          |
+---------+---------------+
1 row in set (3.59 sec)
```

### Single Table

```sql
mysql> show single tables;
+--------------+---------------+
| table_name   | resource_name |
+--------------+---------------+
| t_single_0   | ds_0          |
| t_single_1   | ds_1          |
+--------------+---------------+
2 rows in set (0.02 sec)
```
