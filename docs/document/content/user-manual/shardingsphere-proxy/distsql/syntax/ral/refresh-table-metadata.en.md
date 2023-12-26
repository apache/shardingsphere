+++
title = "REFRESH TABLE METADATA"
weight = 9
+++

### Description

The `REFRESH TABLE METADATA` syntax is used to refresh table metadata.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
RefreshTableMetadata ::=
  'REFRESH' 'TABLE' 'METADATA' (tableName | tableName 'FROM' 'STORAGE' 'UNIT' storageUnitName ('SCHEMA' schemaName)?)?

tableName ::=
  identifier

storageUnitName ::=
  identifier

schemaName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `tableName` and `storageUnitName` is not specified, the default is to refresh all table metadata.

- refresh table metadata need to use `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

- If there are no tables in the schema, the schema will be deleted.


### Example

- Refresh specified table's metadata in specified schema of a specified storage unit

```sql
REFRESH TABLE METADATA t_order FROM STORAGE UNIT ds_1 SCHEMA db_schema;
```

- Refresh all tables' metadata in specified schema of a specified storage unit

```sql
REFRESH TABLE METADATA FROM STORAGE UNIT ds_1 SCHEMA db_schema;
```

- Refresh metadata for specified table in specified storage unit

```sql
REFRESH TABLE METADATA t_order FROM STORAGE UNIT ds_1;
```

- Refresh metadata for specified table

```sql
REFRESH TABLE METADATA t_order;
```
- Refresh all table metadata

```sql
REFRESH TABLE METADATA;
```

### Reserved word

`REFRESH`, `TABLE`, `METADATA`, `FROM`, `STORAGE`, `UNIT`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
