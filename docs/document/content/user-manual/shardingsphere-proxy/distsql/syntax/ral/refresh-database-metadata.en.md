+++
title = "REFRESH DATABASE METADATA FROM GOVERNANCE CENTER"
weight = 10
+++

### Description

The `REFRESH DATABASE METADATA` syntax is used to refresh the metadata of the local logic database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
RefreshDatabaseMetadata ::=
  'FORCE'? 'REFRESH' 'DATABASE' 'METADATA' databaseName?

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is to refresh all database metadata.

- When using `FORCE` to refresh metadata, the latest metadata will be obtained locally and written to the governance center. If without `FORCE`, it will be pulled from the governance center.

### Example

- Refresh metadata for specified database

```sql
REFRESH DATABASE METADATA sharding_db;
```

- Refresh all database metadata

```sql
REFRESH DATABASE METADATA;
```

- Force refresh all database metadata

```sql
FORCE REFRESH DATABASE METADATA;
```

### Reserved word

`FORCE`, `REFRESH`, `DATABASE`, `METADATA`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
