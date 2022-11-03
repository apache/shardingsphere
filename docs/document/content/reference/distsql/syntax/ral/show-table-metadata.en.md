+++
title = "SHOW TABLE METADATA"
weight = 8
+++

### Description

The `SHOW TABLE METADATA` syntax is used to query tabe metadata.

### Syntax

```sql
ShowTableMetadata ::=
  'SHOW' 'TABLE' 'METADATA' tableName (',' tableName)* ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```

### Return Value Description

| Columns     | Description   |
|-------------|---------------|
| schema_name | database name |
| table_name  | table name    |
| type        | metadata type |
| name        | metadata name |

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Query matadata of multiple tables from specified database

```sql
SHOW TABLE METADATA t_order, t_order_1 FROM test1;
```

```sql
mysql> SHOW TABLE METADATA t_order, t_order_1 FROM test1;
+-------------+------------+--------+----------+
| schema_name | table_name | type   | name     |
+-------------+------------+--------+----------+
| test1       | t_order_1  | COLUMN | order_id |
| test1       | t_order_1  | COLUMN | user_id  |
| test1       | t_order_1  | COLUMN | status   |
| test1       | t_order_1  | INDEX  | PRIMARY  |
| test1       | t_order    | COLUMN | order_id |
| test1       | t_order    | COLUMN | user_id  |
| test1       | t_order    | COLUMN | status   |
| test1       | t_order    | INDEX  | PRIMARY  |
+-------------+------------+--------+----------+
8 rows in set (0.01 sec)
```

- Query metadata of one table from specified database

```sql
SHOW TABLE METADATA t_order FROM test1;
```

```sql
mysql> SHOW TABLE METADATA t_order FROM test1;
+-------------+------------+--------+----------+
| schema_name | table_name | type   | name     |
+-------------+------------+--------+----------+
| test1       | t_order    | COLUMN | order_id |
| test1       | t_order    | COLUMN | user_id  |
| test1       | t_order    | COLUMN | status   |
| test1       | t_order    | INDEX  | PRIMARY  |
+-------------+------------+--------+----------+
4 rows in set (0.00 sec)
```

- Query metadata of multiple tables from current database

```sql
SHOW TABLE METADATA t_order, t_order_1;
```

```sql
mysql> SHOW TABLE METADATA t_order, t_order_1;
+-------------+------------+--------+----------+
| schema_name | table_name | type   | name     |
+-------------+------------+--------+----------+
| test1       | t_order_1  | COLUMN | order_id |
| test1       | t_order_1  | COLUMN | user_id  |
| test1       | t_order_1  | COLUMN | status   |
| test1       | t_order_1  | INDEX  | PRIMARY  |
| test1       | t_order    | COLUMN | order_id |
| test1       | t_order    | COLUMN | user_id  |
| test1       | t_order    | COLUMN | status   |
| test1       | t_order    | INDEX  | PRIMARY  |
+-------------+------------+--------+----------+
8 rows in set (0.00 sec)
```

- Query metadata of one table from current database

```sql
SHOW TABLE METADATA t_order;
```

```sql
mysql> SHOW TABLE METADATA t_order;
+-------------+------------+--------+----------+
| schema_name | table_name | type   | name     |
+-------------+------------+--------+----------+
| test1       | t_order    | COLUMN | order_id |
| test1       | t_order    | COLUMN | user_id  |
| test1       | t_order    | COLUMN | status   |
| test1       | t_order    | INDEX  | PRIMARY  |
+-------------+------------+--------+----------+
4 rows in set (0.01 sec)
```

### Reserved word

`SHOW`, `TABLE`, `METADATA`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
