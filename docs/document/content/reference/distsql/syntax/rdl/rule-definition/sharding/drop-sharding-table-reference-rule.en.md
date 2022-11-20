+++
title = "DROP SHARDING TABLE REFERENCE RULE"
weight = 15
+++

## Description

The `DROP SHARDING TABLE REFERENCE RULE` syntax is used to drop specified sharding table reference rule.

### Syntax

```sql
DropShardingTableReferenceRule ::=
  'DROP' 'SHARDING' 'TABLE' 'REFERENCE' 'RULE'  (tableName (',' tableName)* )?

tableName ::=
  identifier
```

### Supplement

- When `SHARDING TABLE REFERENCE RULE` is not specified, default is drop all sharding table reference rules.

### Example

- Drop specified sharding table reference rule

```sql
DROP SHARDING TABLE REFERENCE RULE (t_order, t_order_item);
```

- Drop all sharding table reference rule

```sql
DROP SHARDING TABLE REFERENCE RULE;
```

### Reserved word

`DROP`, `SHARDING`, `TABLE`, `REFERENCE`, `RULE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
