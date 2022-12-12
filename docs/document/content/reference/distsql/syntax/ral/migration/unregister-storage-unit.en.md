+++
title = "UNREGISTER MIGRATION SOURCE STORAGE UNIT"
weight = 5
+++

### Description

The `UNREGISTER MIGRATION SOURCE STORAGE UNIT` syntax is used to unregister migration source storage unit from the current connection

### Syntax

```sql
UnregisterStorageUnit ::=
  'DROP' 'STORAGE' 'UNIT' ( 'IF' 'EXISTS' )? storageUnitName  ( ',' storageUnitName )* 

storageUnitName ::=
  identifier
```

### Supplement

- `UNREGISTER MIGRATION SOURCE STORAGE UNIT` will only unregister storage unit in Proxy, the real data source corresponding to the storage unit will not be
  dropped;

### Example

- Drop a migration source storage unit

```sql
UNREGISTER MIGRATION SOURCE STORAGE UNIT su_0;
```

- Drop multiple migration source storage units

```sql
UNREGISTER MIGRATION SOURCE STORAGE UNIT su_1, su_2;
```

### Reserved word

`UNREGISTER`、`MIGRATION`、`SOURCE`、`STORAGE`、`UNIT`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
