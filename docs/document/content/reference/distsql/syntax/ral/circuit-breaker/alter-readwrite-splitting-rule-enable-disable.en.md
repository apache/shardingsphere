+++
title = "ALTER READWRITE_SPLITTING RULE ENABLE/DISABLE"
weight = 2
+++

## Description

The `ALTER READWRITE_SPLITTING RULE ENABLE/DISABLE` syntax is used enable/disable a specified read source for specified readwrite splitting rule.

### Syntax

```sql
AlterReadwriteSplittingRule ::=
  'ALTER' 'READWRITE_SPLITTING' 'RULE' groupName ( 'ENABLE' | 'DISABLE' ) storageUnitName 'FROM' databaseName

groupName ::=
  identifier

storageUnitName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Disable a specified read source for specified readwrite splitting rule in specified database

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 DISABLE read_su_0 FROM test1;
```

- Enable a specified read source for specified readwrite splitting rule in specified database

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 ENABLE read_su_0 FROM test1;
```

- Disable a specified read source for specified readwrite splitting rule in current database

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 DISABLE read_su_0;
```

- Enable a specified read source for specified readwrite splitting rule in current database

```sql
ALTER READWRITE_SPLITTING RULE ms_group_1 ENABLE read_su_0;
```

### Reserved word

`ALTER`, `READWRITE_SPLITTING`, `RULE`, `ENABLE`, `DISABLE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
