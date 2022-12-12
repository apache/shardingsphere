+++
title = "DROP TRAFFIC RULE"
weight = 10
+++

### Description

The `DROP TRAFFIC RULE` syntax is used to drop specified dual routing rule.

### Syntax

```sql
DropTrafficRule ::=
  'DROP' 'TRAFFIC' 'RULE' ruleName (',' ruleName)?

ruleName ::=
  identifier
```

### Example

- Drop specified traffic rule

```sql
DROP TRAFFIC RULE sql_match_traffic;
```

- Drop mutiple traffic rules
```sql
DROP TRAFFIC RULE sql_match_traffic, sql_hint_traffic;
```

### Reserved word

`DROP`, `TRAFFIC`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
