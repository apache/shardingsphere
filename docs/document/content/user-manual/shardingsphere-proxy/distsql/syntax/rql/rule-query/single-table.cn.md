+++
title = "单表"
weight = 2
+++

## 语法说明

```sql
SHOW SINGLE TABLE (tableRule | RULES) [FROM schemaName]

SHOW SINGLE TABLES 

tableRule:
    RULE tableName
```

## 返回值说明

### Single Table Rule

| 列            | 说明          |
| ------------- | ------------ |
| name          | 规则名称      |
| resource_name | 数据源名称    |

### Single Table

| 列            | 说明                  |
| ------------- | -------------------- |
| table_name    | 单表名称              |
| resource_name | 单表所在的数据源名称    |

## 示例

*single table rules*

```sql
sql> show single table rules;
+---------+---------------+
| name    | resource_name |
+---------+---------------+
| default | ds_1          |
+---------+---------------+
1 row in set (0.01 sec)
```

*single tables*
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