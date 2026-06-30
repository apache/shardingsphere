+++
title = "SHOW TABLE METADATA"
weight = 11
+++

### Description

The `SHOW TABLE METADATA` syntax is used to query table metadata.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowTableMetadata ::=
  'SHOW' 'TABLE' 'METADATA' tableName (',' tableName)* ('FROM' databaseName)?

tableName ::=
  distSQLIdentifier

databaseName ::=
  distSQLIdentifier

distSQLIdentifier ::=
  identifier | quotedIdentifier

quotedIdentifier ::=
  '`' identifier '`' | '"' identifier '"'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns       | Description    |
|---------------|----------------|
| database_name | database name  |
| table_name    | table name     |
| type          | metadata type  |
| name          | metadata name  |
| value         | metadata value |

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

- `tableName` and `databaseName` can be unquoted identifiers, backtick-quoted identifiers, or double-quoted identifiers.

### Example

- Query metadata of multiple tables from specified database

```sql
SHOW TABLE METADATA t_order, `t_order_1` FROM "sharding_db";
```

```sql
mysql> SHOW TABLE METADATA t_order, `t_order_1` FROM "sharding_db";
+---------------+------------+--------+----------+---------------------------+
| database_name | table_name | type   | name     | value                     |
+---------------+------------+--------+----------+---------------------------+
| sharding_db   | t_order_1  | COLUMN | order_id | {"name":"order_id",...}    |
| sharding_db   | t_order_1  | COLUMN | user_id  | {"name":"user_id",...}     |
| sharding_db   | t_order_1  | COLUMN | status   | {"name":"status",...}      |
| sharding_db   | t_order_1  | INDEX  | PRIMARY  | {"name":"PRIMARY",...}     |
| sharding_db   | t_order    | COLUMN | order_id | {"name":"order_id",...}    |
| sharding_db   | t_order    | COLUMN | user_id  | {"name":"user_id",...}     |
| sharding_db   | t_order    | COLUMN | status   | {"name":"status",...}      |
| sharding_db   | t_order    | INDEX  | PRIMARY  | {"name":"PRIMARY",...}     |
+---------------+------------+--------+----------+---------------------------+
8 rows in set (0.01 sec)
```

- Query metadata of one table from specified database

```sql
SHOW TABLE METADATA "t_order" FROM `sharding_db`;
```

```sql
mysql> SHOW TABLE METADATA "t_order" FROM `sharding_db`;
+---------------+------------+--------+----------+---------------------------+
| database_name | table_name | type   | name     | value                     |
+---------------+------------+--------+----------+---------------------------+
| sharding_db   | t_order    | COLUMN | order_id | {"name":"order_id",...}    |
| sharding_db   | t_order    | COLUMN | user_id  | {"name":"user_id",...}     |
| sharding_db   | t_order    | COLUMN | status   | {"name":"status",...}      |
| sharding_db   | t_order    | INDEX  | PRIMARY  | {"name":"PRIMARY",...}     |
+---------------+------------+--------+----------+---------------------------+
4 rows in set (0.00 sec)
```

- Query metadata of multiple tables from current database

```sql
SHOW TABLE METADATA `t_order`, "t_order_1";
```

```sql
mysql> SHOW TABLE METADATA `t_order`, "t_order_1";
+---------------+------------+--------+----------+---------------------------+
| database_name | table_name | type   | name     | value                     |
+---------------+------------+--------+----------+---------------------------+
| sharding_db   | t_order_1  | COLUMN | order_id | {"name":"order_id",...}    |
| sharding_db   | t_order_1  | COLUMN | user_id  | {"name":"user_id",...}     |
| sharding_db   | t_order_1  | COLUMN | status   | {"name":"status",...}      |
| sharding_db   | t_order_1  | INDEX  | PRIMARY  | {"name":"PRIMARY",...}     |
| sharding_db   | t_order    | COLUMN | order_id | {"name":"order_id",...}    |
| sharding_db   | t_order    | COLUMN | user_id  | {"name":"user_id",...}     |
| sharding_db   | t_order    | COLUMN | status   | {"name":"status",...}      |
| sharding_db   | t_order    | INDEX  | PRIMARY  | {"name":"PRIMARY",...}     |
+---------------+------------+--------+----------+---------------------------+
8 rows in set (0.00 sec)
```

- Query metadata of one table from current database

```sql
SHOW TABLE METADATA "t_order";
```

```sql
mysql> SHOW TABLE METADATA "t_order";
+---------------+------------+--------+----------+---------------------------+
| database_name | table_name | type   | name     | value                     |
+---------------+------------+--------+----------+---------------------------+
| sharding_db   | t_order    | COLUMN | order_id | {"name":"order_id",...}    |
| sharding_db   | t_order    | COLUMN | user_id  | {"name":"user_id",...}     |
| sharding_db   | t_order    | COLUMN | status   | {"name":"status",...}      |
| sharding_db   | t_order    | INDEX  | PRIMARY  | {"name":"PRIMARY",...}     |
+---------------+------------+--------+----------+---------------------------+
4 rows in set (0.01 sec)
```

### Reserved word

`SHOW`, `TABLE`, `METADATA`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
