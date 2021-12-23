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

| Column        | Description                                           |
| ------------- | ------------------------------------------------------|
| table_name    | Single table name                                     |
| resource_name | The resource name where the single table is located   |

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
+--------------+---------------+
| table_name   | resource_name |
+--------------+---------------+
| t_single_0   | ds_0          |
| t_single_1   | ds_1          |
+--------------+---------------+
2 rows in set (0.02 sec)
```
