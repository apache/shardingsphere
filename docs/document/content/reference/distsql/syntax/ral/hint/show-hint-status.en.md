+++
title = "SHOW HINT STATUS"
weight = 7
+++

### Description

The `SHOW HINT STATUS` syntax is used to query hint settings for current connection.

### Syntax

```sql
ShowHintStatus ::=
  'SHOW' ('SHARDING'|'READWRITE_SPLITTING') 'HINT' 'STATUS'
```

### Supplement

- When `SHARDING`/`READWRITE_SPLITTING` is not specified, the default is clear all hint settings.

### Example

- Query hint settings of sharding

```sql
SHOW SHARDING HINT STATUS;
```

- Query hint settings of readwrite splitting

```sql
SHOW READWRITE_SPLITTING HINT STATUS;
```

### Reserved word

`SHOW`, `SHARDING`, `READWRITE_SPLITTING`, `HINT`, `STATUS`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
