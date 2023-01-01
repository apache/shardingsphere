+++
title = "REFRESH TABLE METADATA FROM STORAGE UNIT"
weight = 7
+++

### Description

The `REFRESH TABLE METADATA FROM STORAGE UNIT` syntax is used to Refresh the tables’ metadata in a schema of a specified storage unit.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
RefreshTableMetadataFromStorageUnit ::=
  'REFRESH' 'TABLE' 'METADATA' 'FROM' 'STORAGE' 'UNIT' storageUnitName 'SCHEMA' schemaName

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

- If there are no tables in the schema, the schema will be deleted

### Example

- Refresh the tables’ metadata in a schema of a specified storage unit

```sql
REFRESH TABLE METADATA FROM STORAGE UNIT ds_1 SCHEMA db_schema;
```

### Reserved word

`REFRESH`, `TABLE`, `METADATA`, `FROM`, `STORAGE`, `UNIT`, `SCHEMA`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
