+++
title = "SHOW UNLOADED SINGLE TABLES"
weight = 4
+++

### Description

The `SHOW UNLOADED SINGLE TABLES` syntax is used to query unloaded single tables.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showUnloadedSingleTables::=
  'SHOW' 'UNLOADED' 'SINGLE' 'TABLES' ('FROM' fromClause)?

fromClause ::=
  databaseName ('STORAGE' 'UNIT' storageUnitName ('SCHEMA' schemaName)?)?
  | 'STORAGE' 'UNIT' storageUnitName ('SCHEMA' schemaName)?
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return value description

| Column            | Description                                             |
|-------------------|---------------------------------------------------------|
| table_name        | Single table name                                       |
| storage_unit_name | The storage unit name where the single table is located |
| schema_name       | Schema name, returned only when schema metadata exists  |


### Example

- Query unloaded single tables.

```sql
SHOW UNLOADED SINGLE TABLES;
```

```sql
mysql> SHOW UNLOADED SINGLE TABLES;
+------------+-------------------+
| table_name | storage_unit_name |
+------------+-------------------+
| t_single   | ds_1              |
+------------+-------------------+
1 row in set (0.01 sec)
```

- Query unloaded single tables from specified storage unit.

```sql
SHOW UNLOADED SINGLE TABLES FROM STORAGE UNIT ds_0;
```

### Reserved word

`SHOW`, `UNLOADED`, `SINGLE`, `TABLES`, `FROM`, `STORAGE`, `UNIT`, `SCHEMA`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
