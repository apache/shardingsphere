+++
title = "Single Table"
weight = 2
+++

## Syntax

```sql
SHOW SINGLE TABLE (tableRule | RULES) [FROM schemaName]

tableRule:
    RULE tableName
```

## Return Value Description

| Column        | Description        |
| ------------- | -------------------|
| table_name    | Single table name  |
| resource_name | Data source name   |

## Example

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
