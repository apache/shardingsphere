+++
title = "Single Table"
weight = 7
+++

## Definition

```sql
SHOW SINGLE TABLE RULES [FROM schemaName]

SHOW SINGLE tableDefinition | TABLES [FROM schemaName]

tableDefinition:
    TABLE tableName
```

## Description

### Single Table Rule

| Column        | Description       |
| ------------- | ------------------|
| name          | Rule name         |
| resource_name | Data source name  |

### Single Table

| Column        | Description        |
| ------------- | ------------------ |
| table_name    | Single table name  |
| resource_name | Data source name   |

## Example

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
