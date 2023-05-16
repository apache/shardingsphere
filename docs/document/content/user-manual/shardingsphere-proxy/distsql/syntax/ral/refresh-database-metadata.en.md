+++
title = "REFRESH DATABASE METADATA FROM GOVERNANCE CENTER"
weight = 9
+++

### Description

The `REFRESH DATABASE METADATA FROM GOVERNANCE CENTER` syntax is used to pull the latest configuration from the governance center and refresh the metadata of the local logic database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
RefreshDatabaseMetadataFromGovernanceCenter ::=
  'REFRESH' 'DATABASE' 'METADATA' databaseName? 'FROM' 'GOVERNANCE' 'CENTER'

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

- refresh table metadata need to use `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Refresh metadata for specified database

```sql
REFRESH DATABASE METADATA sharding_db FROM GOVERNANCE CENTER;
```

- Refresh all database metadata

```sql
REFRESH DATABASE METADATA FROM GOVERNANCE CENTER;
```

### Reserved word

`REFRESH`, `DATABASE`, `METADATA`, `FROM`, `GOVERNANCE`, `CENTER`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
