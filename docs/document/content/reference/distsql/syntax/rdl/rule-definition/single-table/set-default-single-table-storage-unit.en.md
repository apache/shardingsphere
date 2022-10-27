+++
title = "SET DEFAULT SINGLE TABLE STORAGE UNIT"
weight = 2
+++

## Description

The `SET DEFAULT SINGLE TABLE STORAGE UNIT` syntax is used to set default single table storage unit.

### Syntax

```sql
SetDefaultSingleTableStorageUnit ::=
  'SET' 'DEFAULT' 'SINGLE' 'TABLE' 'STORAGE' 'UNIT' singleTableDefinition

singleTableDefinition ::=
  '=' (storageUnitName ｜ 'RANDOM')

storageUnitName ::=
  identifier
```

### Supplement

- `STORAGE UNIT` needs to use storage unit managed by RDL. The `RANDOM` keyword stands for random storage.


### Example

- Set a default single table storage unit

```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = su_0;
```

- Set the default single table storage unit to random storage

```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = RANDOM;
```

### Reserved word

`SET`, `DEFAULT`, `SINGLE`, `TABLE`, `STORAGE`, `UNIT`, `RANDOM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
