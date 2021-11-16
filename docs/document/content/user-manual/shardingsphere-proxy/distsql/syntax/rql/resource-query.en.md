+++
title = "Resource Query"
weight = 1
+++

## Syntax

```sql
SHOW SCHEMA RESOURCES [FROM schemaName]
```

## Return Value Description

| Column        | Description             |
| ------------- | ----------------------- |
| name          | Data source name        |
| type          | Data source type        |
| host          | Data source host        |
| port          | Data source port        |
| db            | Database name           |
| attribute     | Data source parameter   |

## Example

```sql
mysql> show schema resources;
+------+-------+-----------+------+------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | attribute                                                                                                                                                                                           |
+------+-------+-----------+------+------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_0 | MySQL | 127.0.0.1 | 3306 | ds_0 | {"minPoolSize":1,"connectionTimeoutMilliseconds":30000,"maxLifetimeMilliseconds":1800000,"readOnly":false,"idleTimeoutMilliseconds":60000,"maxPoolSize":50} |
| ds_1 | MySQL | 127.0.0.1 | 3306 | ds_1 | {"minPoolSize":1,"connectionTimeoutMilliseconds":30000,"maxLifetimeMilliseconds":1800000,"readOnly":false,"idleTimeoutMilliseconds":60000,"maxPoolSize":50} |
+------+-------+-----------+------+------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.84 sec)
```
