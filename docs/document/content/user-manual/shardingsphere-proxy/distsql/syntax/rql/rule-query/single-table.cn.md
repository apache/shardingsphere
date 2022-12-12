+++
title = "单表"
weight = 2
+++

## 语法说明

```sql
SHOW DEFAULT SINGLE TABLE STORAGE UNIT [FROM databaseName]
    
SHOW SINGLE (TABLES [LIKES likesLiteral] | table) [FROM databaseName]

COUNT SINGLE_TABLE RULE [FROM databaseName]

table:
    TABLE tableName
```

## 返回值说明

### Single Table Storage Unit

| 列                  | 说明           |
|--------------------|----------------|
| storage_unit_name  | 存储单元名称     |

### Single Table

| 列                | 说明                |
| ----------------- |---------------------|
| table_name        | 单表名称             |
| storage_unit_name | 单表所在的存储节点名称 |

### Single Table Rule Count

| 列          | 说明                 |
|------------|---------------------|
| rule_name  | 规则名称              |
| database   | 单表所在的数据库名称    |
| count      | 规则个数              |

## 示例

*SHOW DEFAULT SINGLE TABLE STORAGE UNIT*

```sql
sql> SHOW DEFAULT SINGLE TABLE STORAGE UNIT;
+-------------------+
| storage_unit_name |
+-------------------+
|  ds_0             |
+-------------------+
1 row in set (0.01 sec)
```

*SHOW SINGLE TABLE tableName*

```sql
sql> SHOW SINGLE TABLE t_single_0;
+----------------+-------------------+
| table_name     | storage_unit_name |
+----------------+-------------------+
| t_single_0     | ds_0              |
+----------------+-------------------+
1 row in set (0.01 sec)
```

*SHOW SINGLE TABLES LIKE*

```sql
mysql> SHOW SINGLE TABLES LIKE '%order_5';
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_order_5  | ds_1              |
+------------+-------------------+
1 row in set (0.11 sec)
```

*SHOW SINGLE TABLES*

```sql
mysql> SHOW SINGLE TABLES;
+--------------+-------------------+
| table_name   | storage_unit_name |
+--------------+-------------------+
| t_single_0   | ds_0              |
| t_single_1   | ds_1              |
+--------------+-------------------+
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
