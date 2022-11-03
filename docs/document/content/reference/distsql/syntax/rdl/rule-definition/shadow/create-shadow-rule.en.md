+++
title = "CREATE SHADOW RULE"
weight = 2
+++

## Description

The `CREATE SHADOW RULE` syntax is used to create a shadow rule.

### Syntax

```sql
CreateShadowRule ::=
  'CREATE' 'SHADOW' 'RULE' shadowDefinition ( ',' shadowDefinition )*

shadowDefinition ::=
  ruleName '(' storageUnitMapping shadowTableRule ( ',' shadowTableRule )* ')'
    
storageUnitMapping ::=
    'SOURCE' '=' storageUnitName ',' 'SHADOW' '=' storageUnitName

shadowTableRule ::=
    tableName '(' shadowAlgorithm ')'
    
shadowAlgorithm ::=
    'TYPE' '('  'NAME' '=' shadowAlgorithmType ',' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' ) ')'

ruleName ::=
  identifier

storageUnitName ::=
  identifier

tableName ::=
  identifier

algorithmName ::=
  identifier

shadowAlgorithmType ::=
  string
```

### Supplement

- Duplicate `ruleName` cannot be created;
- `storageUnitMapping` specifies the mapping relationship between the `source` database and the shadow library. You need to
  use the storage unit managed by RDL, please refer
  to [STORAGE UNIT](https://shardingsphere.apache.org/document/current/en/reference/distsql/syntax/rdl/storage-unit-definition/);
- `shadowAlgorithm` can act on multiple `shadowTableRule` at the same time;
- If `algorithmName` is not specified, it will be automatically generated according to `ruleName`, `tableName`
  and `shadowAlgorithmType`;
- `shadowAlgorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SIMPLE_HINT`.

### Example

- Create a shadow rule

```sql
CREATE SHADOW RULE shadow_rule(
  SOURCE=demo_su,
  SHADOW=demo_su_shadow,
  t_order(TYPE(NAME="SIMPLE_HINT", PROPERTIES("shadow"="true", "foo"="bar"))), 
  t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))
);
```

### Reserved word

`CREATE`, `SHADOW`, `RULE`, `SOURCE`, `SHADOW`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [STORAGE UNIT](https://shardingsphere.apache.org/document/current/en/reference/distsql/syntax/rdl/storage-unit-definition/)