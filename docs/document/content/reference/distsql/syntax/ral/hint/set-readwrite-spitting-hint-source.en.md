+++
title = "SET READWRITE_SPLITTING HINT SOURCE"
weight = 2
+++

### Description

The `SET READWRITE_SPLITTING HINT SOURCE` syntax is used to set readwrite splitting routing strategy for current connection.

### Syntax

```sql
SetReadwriteSplittingHintSource ::=
  'SET' 'READWRITE_SPLITTING' 'HINT' 'SOURCE' '='('auto'|'write')
```

### Example

- Set the read-write splitting routing strategy to auto

```sql
SET READWRITE_SPLITTING HINT SOURCE = auto;
```

- Set the read-write splitting routing strategy to write

```sql
SET READWRITE_SPLITTING HINT SOURCE = write;
```

### Reserved word

`SET`, `READWRITE_SPLITTING`, `HINT`, `SOURCE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
