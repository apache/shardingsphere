+++
title = "REFRESH DATABASE METADATA"
weight = 10
aliases = ["/user-manual/shardingsphere-proxy/distsql/syntax/ral/refresh-database-metadata/"]
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

- Refresh loads the latest physical table metadata and validates all related tables before publishing it, regardless of `check-table-metadata-enabled`.
  Inconsistent structures or partially missing physical tables cause the refresh to fail before publication.
  The optional `FORCE` keyword does not change this behavior.

- After each node applies a table structure change or removal, statements prepared against that table must be prepared again; otherwise new executions fail with `Table metadata changed; prepare the statement again.`.
  ShardingSphere-JDBC and the MySQL protocol expose error `10025` (`HY000`); PostgreSQL and openGauss use SQLState `58000` through the existing protocol mapping.
  Unchanged or unrelated tables keep their existing bindings, and result sets already created remain readable.

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

- [Reserved word](/en/user-manual/distsql/syntax/reserved-word/)
