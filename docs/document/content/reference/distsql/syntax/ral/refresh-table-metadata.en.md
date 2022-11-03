+++
title = "REFRESH TABLE METADATA"
weight = 6
+++

### Description

The `REFRESH TABLE METADATA` syntax is used to refresh table metadata.

### Syntax

```sql
RefreshTableMetadata ::=
  'REFRESH' 'TABLE' 'METADATA' ( (tableName)? | tableName 'FROM' 'STORAGE' 'UNIT' storageUnitName)?

tableName ::=
  identifier

storageUnitName ::=
  identifier
```

### Supplement

- When `tableName` and `storageUnitName` is not specified, the default is to refresh all table metadata.

- refresh table metadata need to use `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Refresh metadata for specified table in specified storage unit

```sql
REFRESH TABLE METADATA t_order FROM STORAGE UNIT su_1;
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

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
