+++
title = "DROP SHARDING TABLE REFERENCE RULE"
weight = 15
+++

## Description

The `DROP SHARDING TABLE REFERENCE RULE` syntax is used to drop specified sharding table reference rule.

### Syntax

```sql
DropShardingTableReferenceRule ::=
  'DROP' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  ruleName (',' ruleName)*

ruleName ::=
  identifier
```

### Example

- Drop a specified sharding table reference rule

```sql
DROP SHARDING TABLE REFERENCE RULE ref_0;
```

- Drop multiple sharding table reference rules

```sql
DROP SHARDING TABLE REFERENCE RULE ref_0, ref_1;
```

### Reserved word

`DROP`, `SHARDING`, `TABLE`, `REFERENCE`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
