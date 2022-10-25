+++
title = "SHOW SINGLE TABLE"
weight = 2
+++

### Description

The `SHOW SINGLE TABLE` syntax is used to query single tables for specified database.

### Syntax

```
ShowSingleTable::=
  'SHOW' 'SINGLE' ('TABLES'|'TABLE' tableName) ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column        | Description                                           |
| ------------- | ------------------------------------------------------|
| table_name    | Single table name                                     |
| resource_name | The resource name where the single table is located   |


### Example

- Query specified single table for specified database.

```sql
SHOW SINGLE TABLE t_user FROM test1;
```

```sql
mysql> SHOW SINGLE TABLE t_user FROM test1;
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_user     | ds_0          |
+------------+---------------+
1 row in set (0.00 sec)
```

- Query specified single table for current database.

```sql
SHOW SINGLE TABLE t_user;
```

```sql
mysql> SHOW SINGLE TABLE t_user;
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_user     | ds_0          |
+------------+---------------+
1 row in set (0.00 sec)
```

- Query single tables for specified database.

```sql
SHOW SINGLE TABLES FROM test1;
```

```sql
mysql> SHOW SINGLE TABLES FROM test1;
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_user     | ds_0          |
+------------+---------------+
1 row in set (0.00 sec)
```

- Query single tables for current database.

```sql
SHOW SINGLE TABLES;
```

```sql
mysql> SHOW SINGLE TABLES;
+------------+---------------+
| table_name | resource_name |
+------------+---------------+
| t_user     | ds_0          |
+------------+---------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `SINGLE`, `TABLE`, `TABLES`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
