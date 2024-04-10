+++
title = "IMPORT DATABASE CONFIGURATION"
weight = 14
+++

### Description

The `IMPORT DATABASE CONFIGURATION` syntax is used to import a database from the configuration in `YAML`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ExportDatabaseConfiguration ::=
  'IMPORT' 'DATABASE' 'CONFIGURATION' 'FROM' 'FILE' filePath

filePath ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When a database with the same name already exists in the metadata, it cannot be imported;
- When `databaseName` in YAML is empty, it cannot be imported;
- When `dataSources` in YAML is empty, only empty database will be imported.

### Example

```sql
IMPORT DATABASE CONFIGURATION FROM FILE "/xxx/config_sharding_db.yaml";
```

### Reserved word

`IMPORT`, `DATABASE`, `CONFIGURATION`, `FROM`, `FILE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
