+++
title = "SHOW LOGICAL TABLES"
weight = 3
+++

### Description

The `SHOW LOGICAL TABLES` syntax is used to query logical tables in the specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowLogicalTables ::=
  'SHOW' 'FULL'? 'LOGICAL' 'TABLES' ('FROM' databaseName)? showLike?

databaseName ::=
  identifier

showLike ::=
  'LIKE' likePattern

likePattern ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`; if `DATABASE` is not used, it will prompt `No database selected`.
- `FULL` returns the logical table type.
- When the database contains schema metadata, the result also contains `schema_name`.

### Return Value Description

| Column      | Description                       |
|-------------|-----------------------------------|
| table_name  | Logical table name                |
| table_type  | Logical table type, returned only when `FULL` is specified |
| schema_name | Schema name, returned only when schema metadata exists |

### Example

- Query logical tables from current database

```sql
SHOW LOGICAL TABLES;
```

- Query full logical tables from specified database

```sql
SHOW FULL LOGICAL TABLES FROM sharding_db;
```

- Query logical tables with like clause

```sql
SHOW LOGICAL TABLES LIKE 't_order%';
```

### Reserved word

`SHOW`, `FULL`, `LOGICAL`, `TABLES`, `FROM`, `LIKE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
