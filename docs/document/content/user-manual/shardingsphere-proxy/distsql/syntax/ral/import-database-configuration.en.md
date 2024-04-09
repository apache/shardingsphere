+++
title = "IMPORT DATABASE CONFIGURATION"
weight = 13
+++

### Description

The `IMPORT DATABASE CONFIGURATION` syntax is used to import `YAML` configuration to specified database.

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

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

- The `IMPORT DATABASE CONFIGURATION` syntax only supports import operations on empty database.

### Example

- Import the configuration in `YAML` into the specified database

```sql
IMPORT DATABASE CONFIGURATION FROM FILE "/xxx/config_sharding_db.yaml";
```

- Import the configuration in `YAML` into the current database

```sql
IMPORT DATABASE CONFIGURATION FROM FILE "/xxx/config_sharding_db.yaml";
```

### Reserved word

`IMPORT`, `DATABASE`, `CONFIGURATION`, `FROM`, `FILE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
