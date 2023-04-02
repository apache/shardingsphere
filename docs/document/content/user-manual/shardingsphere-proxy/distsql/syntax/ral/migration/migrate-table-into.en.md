+++
title = "MIGRATE TABLE INTO"
weight = 6
+++

### Description

`MIGRATE TABLE INTO` syntax is used to migration table from source to target

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
MigrateTableInto ::=
  'MIGRATE' 'TABLE' migrationSource '.' tableName 'INTO' (databaseName '.')? tableName

migrationSource ::=
  identifier

databaseName ::=
  identifier

tableName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Migrate table from source to current database

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

- Migrate table from source to specified database

```sql
UNREGISTER MIGRATION SOURCE STORAGE UNIT ds_1, ds_2;
```

### Reserved word

`MIGRATE`, `TABLE`, `INTO`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
