+++
title = "MIGRATE TABLE INTO"
weight = 7
+++

### Description

`MIGRATE TABLE INTO` syntax is used to migration table from source to target

### Syntax

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

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Migrate table from source to current database

```sql
MIGRATE TABLE ds_0.t_order INTO t_order;
```

- Migrate table from source to specified database

```sql
UNREGISTER MIGRATION SOURCE STORAGE UNIT su_1, su_2;
```

### Reserved word

`MIGRATE`, `TABLE`, `INTO`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
