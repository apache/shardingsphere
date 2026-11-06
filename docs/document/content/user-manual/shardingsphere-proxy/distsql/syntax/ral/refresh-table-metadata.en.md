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
  distSQLIdentifier

storageUnitName ::=
  distSQLIdentifier

schemaName ::=
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

### Supplement

- When `tableName` and `storageUnitName` is not specified, the default is to refresh all table metadata.

- refresh table metadata need to use `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

- If there are no tables or views in the schema, the schema will be deleted.

- `tableName`, `storageUnitName`, and `schemaName` can be unquoted identifiers, backtick-quoted identifiers, or double-quoted identifiers.


- Explicit refresh validates all physical tables associated with each selected logical table, including tables in other storage units or schemas, regardless of `check-table-metadata-enabled`.
  Inconsistent structures or partially missing physical tables cause the refresh to fail before new metadata is published.
  `FROM STORAGE UNIT` selects the refresh targets; it does not limit consistency validation to that storage unit.

- After a node applies a table structure change or removal, statements prepared against the previous structure fail with `Table metadata changed; prepare the statement again.` and must be prepared again.
  ShardingSphere-JDBC and the MySQL protocol expose error `10025` (`HY000`); PostgreSQL and openGauss use SQLState `58000` through the existing protocol mapping.
  Unchanged or unrelated tables keep their existing bindings, and result sets already created remain readable.
  In cluster mode, this takes effect on each node when it applies the metadata change.

- After changing tables directly in the backend, make all related physical tables consistent, run the explicit refresh, and prepare affected statements again.
  Preparing a statement does not automatically refresh the shared metadata.

### Example

- Refresh specified table's metadata in specified schema of a specified storage unit

```sql
REFRESH TABLE METADATA `t_order` FROM STORAGE UNIT "ds_1" SCHEMA `db_schema`;
```

- Refresh all tables' metadata in specified schema of a specified storage unit

```sql
REFRESH TABLE METADATA FROM STORAGE UNIT `ds_1` SCHEMA "db_schema";
```

- Refresh metadata for specified table in specified storage unit

```sql
REFRESH TABLE METADATA "t_order" FROM STORAGE UNIT ds_1;
```

- Refresh metadata for specified table

```sql
REFRESH TABLE METADATA `t_order`;
```
- Refresh all table metadata

```sql
REFRESH TABLE METADATA;
```

### Reserved word

`REFRESH`, `TABLE`, `METADATA`, `FROM`, `STORAGE`, `UNIT`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
