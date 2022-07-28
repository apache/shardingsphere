+++
title = "单表"
weight = 2
+++

## 语法说明

```sql
SHOW SINGLE TABLE (table | RULES) [FROM databaseName]

SHOW SINGLE TABLES

COUNT SINGLE_TABLE RULE [FROM databaseName]

table:
    TABLE tableName
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

### Single Table Rule Count

| 列        | 说明                 |
|-----------|---------------------|
| rule_name | 规则名称              |
| database  | 单表所在的数据库名称    |
| count     | 规则个数              |

## 示例

*SHOW SINGLE TABLES RULES*

```sql
sql> SHOW SINGLE TABLES RULES;
+---------+---------------+
| name    | resource_name |
+---------+---------------+
| default | ds_1          |
+---------+---------------+
1 row in set (0.01 sec)
```

*SHOW SINGLE TABLE tableName*

```sql
sql> SHOW SINGLE TABLE t_single_0;
+----------------+---------------+
| table_name     | resource_name |
+----------------+---------------+
| t_single_0     | ds_0          |
+----------------+---------------+
1 row in set (0.01 sec)
```

*SHOW SINGLE TABLES*

```sql
mysql> SHOW SINGLE TABLES;
+--------------+---------------+
| table_name   | resource_name |
+--------------+---------------+
| t_single_0   | ds_0          |
| t_single_1   | ds_1          |
+--------------+---------------+
2 rows in set (0.02 sec)
```

*COUNT SINGLE_TABLE RULE*

```sql
mysql> COUNT SINGLE_TABLE RULE;
+--------------+----------+-------+
| rule_name    | database | count |
+--------------+----------+-------+
| t_single_0   | ds       | 2     |
+--------------+----------+-------+
1 row in set (0.02 sec)
```
