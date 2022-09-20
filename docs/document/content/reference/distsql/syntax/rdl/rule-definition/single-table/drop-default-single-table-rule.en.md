+++
title = "DROP DEFAULT SINGLE TABLE RULE"
weight = 2
+++

## Description

The `DROP DEFAULT SINGLE TABLE RULE` syntax is used to drop a default single table rule.

### Syntax

```sql
DropDefaultSingleTableRule ::=
  'DROP' 'DEFAULT' 'SINGLE' 'TABLE' 'RULE' ifExists?

ifExists ::=
  'IF' 'EXISTS'
```


### Example

#### drop a default single table rule

```sql
DROP DEFAULT SINGLE TABLE RULE;
```

### Reserved word

`DROP`, `SHARDING`, `SINGLE`, `TABLE`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
