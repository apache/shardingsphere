+++
title = "SHOW RULES USED RESOURCE"
weight = 4
+++

### Description

The `SHOW RULES USED RESOURCE` syntax is used to query the rules that use the specified resource in the specified database.

### Syntax

```SQL
showRulesUsedResource ::=
  'SHOW' 'RULES' 'USED' 'RESOURCES' resourceName ('FROM' databaseName)?

resourceName ::=
  IDENTIFIER | STRING

databaseName ::=
  IDENTIFIER
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`; if `DATABASE` is not used, it will prompt `No database selected`.

### Return Value Description

| Column    | Description           |
| --------- | --------------------- |
| type      | features              |
| name      | Data source name      |

### Example

- Query the rules that use the specified resource in the specified database
```sql
SHOW RULES USED RESOURCE ds_0 FROM sharding_db;
```
```sql
+----------+--------------+
| type     | name         |
+----------+--------------+
| sharding | t_order      |
| sharding | t_order_item |
+----------+--------------+
2 rows in set (0.00 sec)
```

- Query the rules that use the specified resource in the current database
```sql
SHOW RULES USED RESOURCE ds_0;
```
```sql
+----------+--------------+
| type     | name         |
+----------+--------------+
| sharding | t_order      |
| sharding | t_order_item |
+----------+--------------+
2 rows in set (0.00 sec)
```
