+++
title = "Single Table"
weight = 2
+++

## Syntax

```sql
SHOW SINGLE TABLE (tableRule | RULES) [FROM schemaName]

SHOW SINGLE TABLES 

tableRule:
    RULE tableName
```

## Return Value Description

### Single Table Rule

| Column        | Description      |
| ------------- | ---------------- |
| name          | Rule name        |
| resource_name | Data source name |

### Single Table

| Column        | Description                                                     |
| ------------- | ----------------------------------------------------------------|
| table_name    | Single table name                                               |
| resource_name | The name of the data source where the single table is located   |

## Example

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
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_single   | ds_0          |
+------------+---------------+
1 row in set (0.05 sec)
```
