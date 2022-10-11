+++
title = "ALTER DEFAULT SINGLE TABLE RULE"
weight = 2
+++

## Description

The `ALTER DEFAULT SINGLE TABLE RULE` syntax is used to alter a default single table rule.

### Syntax

```sql
AlterDefaultSingleTableRule ::=
  'ALTER' 'DEFAULT' 'SINGLE' 'TABLE' 'RULE' singleTableDefinition

singleTableDefinition ::=
  'RESOURCE' '=' resourceName

resourceName ::=
  identifier
```

### Supplement

- `RESOURCE` needs to use data source resource managed by RDL.

### Example

#### Alter a default single table rule

```sql
ALTER DEFAULT SINGLE TABLE RULE RESOURCE = ds_0;
```

### Reserved word

`ALTER`, `SHARDING`, `SINGLE`, `TABLE`, `RULE`, `RESOURCE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
