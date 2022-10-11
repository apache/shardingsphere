+++
title = "CREATE SHARDING AUDITOR"
weight = 8
+++

## Description

The `CREATE SHARDING AUDITOR` syntax is used to add a sharding key auditor for the currently selected
logic database

### Syntax

```sql
CreateShardingAlgorithm ::=
  'CREATE' 'SHARDING' 'AUDITOR' auditorName '(' algorithmDefinition ')'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )*

auditorName ::=
  identifier
  
algorithmType ::=
  string
```

### Supplement

- `algorithmType` is the sharding audit algorithm type. For detailed sharding audit algorithm type information, please refer
  to [SHARDING AUDIT ALGORITHM](/en/user-manual/common-config/builtin-algorithm/audit/).

### Example

#### Create a sharding auditor

```sql
CREATE SHARDING AUDITOR sharding_key_required_auditor (
    TYPE(NAME="DML_SHARDING_CONDITIONS", PROPERTIES("a"="b"))
);
```

### Reserved word

`CREATE`, `SHARDING`, `AUDITOR`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
