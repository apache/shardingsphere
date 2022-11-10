+++
title = "DROP BROADCAST TABLE RULE"
weight = 17
+++

## Description

The `DROP BROADCAST TABLE RULE` syntax is used to drop broadcast table rule for specified broadcast tables

### Syntax

```sql
DropBroadcastTableRule ::=
  'DROP' 'BROADCAST' 'TABLE' 'RULE'  tableName (',' tableName)* 

tableName ::=
  identifier
```

### Supplement

- `tableName` can use the table of existing broadcast rules

### Example

- Drop broadcast table rule for specified broadcast table

```sql
DROP BROADCAST TABLE RULE t_province, t_city;
```

### Reserved word

`DROP`, `BROADCAST`, `TABLE`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
