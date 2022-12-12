+++
title = "CLEAR HINT"
weight = 6
+++

### Description

The `CLEAR HINT` syntax is used to clear hint settings for current connection.

### Syntax

```sql
ClearHint ::=
  'CLEAR' ('SHARDING'|'READWRITE_SPLITTING')? 'HINT' 
```

### Supplement

- When `SHARDING`/`READWRITE_SPLITTING` is not specified, the default is clear all hint settings.

### Example

- Clear hint settings of sharding

```sql
CLEAR SHARDING HINT;
```

- Clear hint settings of readwrite splitting

```sql
CLEAR READWRITE_SPLITTING HINT;
```

- Clear all hint settings

```sql
CLEAR HINT;
```

### Reserved word

`CLEAR`, `SHARDING`, `READWRITE_SPLITTING`, `HINT`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
