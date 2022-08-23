+++
title = "CREATE DEFAULT SINGLE TABLE RULE"
weight = 2
+++

## Description

The `CREATE DEFAULT SINGLE TABLE RULE` syntax is used to create a default single table rule.

### Syntax

```sql
CreateDefaultSingleTableRule ::=
  'CREATE' 'DEFAULT' 'SINGLE' 'TABLE' 'RULE' singleTableDefinition

singleTableDefinition ::=
  'RESOURCE' '=' resourceName

resourceName ::=
  identifier
```

### Supplement

- `RESOURCE` needs to use data source resource managed by RDL.

### Example

#### Create a default single table rule

```sql
CREATE DEFAULT SINGLE TABLE RULE RESOURCE = ds_0;
```

### Reserved word

`CREATE`, `SHARDING`, `SINGLE`, `TABLE`, `RULE`, `RESOURCE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
