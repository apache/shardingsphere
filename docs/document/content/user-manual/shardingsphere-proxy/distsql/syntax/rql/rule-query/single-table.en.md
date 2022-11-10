+++
title = "Single Table"
weight = 2
+++

## Syntax

```sql
SHOW DEFAULT SINGLE TABLE STORAGE UNIT [FROM databaseName]
    
SHOW SINGLE (TABLES | table) [FROM databaseName]

COUNT SINGLE_TABLE RULE [FROM databaseName]

table:
    TABLE tableName
```

## Return Value Description

### Single Table Storage Unit

| Column             | Description       |
|--------------------|-------------------|
| storage_unit_name  | Storage unit name |


### Single Table

| Column        | Description                                           |
| ------------- | ------------------------------------------------------|
| table_name    | Single table name                                     |
| resource_name | The resource name where the single table is located   |

### Single Table Rule Count

| Column    | Description                                         |
|-----------|-----------------------------------------------------|
| rule_name | Single table rule name                              |
| database  | The database name where the single table is located |
| count     | The count of single table rules                     |

## Example

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
