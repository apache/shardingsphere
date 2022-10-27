+++
title = "DROP READWRITE_SPLITTING RULE"
weight = 4
+++

## Description

The `DROP READWRITE_SPLITTING RULE` syntax is used to drop readwrite splitting rule for specified database

### Syntax

```sql
DropReadwriteSplittingRule ::=
  'DROP' 'READWRITE_SPLITTING' 'RULE' ('FROM' databaseName)

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Drop readwrite splitting rule for specified database

```sql
DROP READWRITE_SPLITTING RULE ms_group_1 FROM test1;
```

- Drop readwrite splitting rule for current database

```sql
DROP READWRITE_SPLITTING RULE ms_group_1;
```

### Reserved word

`DROP`, `READWRITE_SPLITTING`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
