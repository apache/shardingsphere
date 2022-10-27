+++
title = "UNREGISTER STORAGE UNIT"
weight = 4
+++

### Description

The `UNREGISTER STORAGE UNIT` syntax is used to unregister storage unit from the current database

### Syntax

```sql
UnregisterStorageUnit ::=
  'DROP' 'STORAGE' 'UNIT' ( 'IF' 'EXISTS' )? storageUnitName  ( ',' storageUnitName )* ( 'IGNORE' 'SINGLE' 'TABLES' )?

storageUnitName ::=
  identifier
```

### Supplement

- `UNREGISTER STORAGE UNIT` will only unregister storage unit in Proxy, the real data source corresponding to the storage unit will not be
  dropped;
- Unable to unregister storage unit already used by rules. `Storage unit are still in used.` will be prompted when removing
  storage units used by rules;
- The storage unit need to be removed only contains `SINGLE TABLE RULE`, and when the user confirms that this restriction
  can be ignored, the `IGNORE SINGLE TABLES` keyword can be added to remove the storage unit.

### Example

- Drop a storage unit

```sql
UNREGISTER STORAGE UNIT su_0;
```

- Drop multiple storage units

```sql
UNREGISTER STORAGE UNIT su_1, su_2;
```

- Ignore single table rule remove storage unit

```sql
UNREGISTER STORAGE UNIT su_1 IGNORE SINGLE TABLES;
```

- Drop the storage unit if it exists

```sql
UNREGISTER STORAGE UNIT IF EXISTS su_2;
```

### Reserved word

`DROP`, `STORAGE`, `UNIT`, `IF`, `EXISTS`, `IGNORE`, `SINGLE`, `TABLES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
