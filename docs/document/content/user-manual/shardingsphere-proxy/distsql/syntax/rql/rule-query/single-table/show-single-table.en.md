+++
title = "SHOW SINGLE TABLE"
weight = 1
+++

### Description

The `SHOW SINGLE TABLE` syntax is used to query single tables for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowSingleTable::=
  'SHOW' 'SINGLE' ('TABLES' ('LIKES' likeLiteral)?|'TABLE' tableName) ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column            | Description                                             |
|-------------------|---------------------------------------------------------|
| table_name        | Single table name                                       |
| storage_unit_name | The storage unit name where the single table is located |


### Example

- Query specified single table for specified database.

```sql
SHOW SINGLE TABLE t_user FROM sharding_db;
```

```sql
mysql> SHOW SINGLE TABLE t_user FROM sharding_db;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_user     | ds_0              |
+------------+-------------------+
1 row in set (0.00 sec)
```

- Query specified single table for current database.

```sql
SHOW SINGLE TABLE t_user;
```

```sql
mysql> SHOW SINGLE TABLE t_user;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_user     | ds_0              |
+------------+-------------------+
1 row in set (0.00 sec)
```

- Query single tables for specified database.

```sql
SHOW SINGLE TABLES FROM sharding_db;
```

```sql
mysql> SHOW SINGLE TABLES FROM sharding_db;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_user     | ds_0              |
+------------+-------------------+
1 row in set (0.00 sec)
```

- Query single tables for current database.

```sql
SHOW SINGLE TABLES;
```

```sql
mysql> SHOW SINGLE TABLES;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_user     | ds_0              |
+------------+-------------------+
1 row in set (0.00 sec)
```

- Query the single tables whose table name end with `order_5` for the specified logic database.

```sql
SHOW SINGLE TABLES LIKE '%order_5' FROM sharding_db;
```

```sql
mysql> SHOW SINGLE TABLES LIKE '%order_5' FROM sharding_db;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_order_5  | ds_1              |
+------------+-------------------+
1 row in set (0.11 sec)
```

- Query the single tables whose table name end with `order_5` for the current logic database

```sql
SHOW SINGLE TABLES LIKE '%order_5';
```

```sql
mysql> SHOW SINGLE TABLES LIKE '%order_5';
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_order_5  | ds_1              |
+------------+-------------------+
1 row in set (0.11 sec)
```

### Reserved word

`SHOW`, `SINGLE`, `TABLE`, `TABLES`, `LIKE`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
