+++
title = "CREATE SHADOW RULE"
weight = 1
+++

## Description

The `CREATE SHADOW RULE` syntax is used to create a shadow rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateShadowRule ::=
  'CREATE' 'SHADOW' 'RULE' ifNotExists? shadowRuleDefinition (',' shadowRuleDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

shadowRuleDefinition ::=
  ruleName '(' storageUnitMapping shadowTableRule (',' shadowTableRule)* ')'
    
storageUnitMapping ::=
  'SOURCE' '=' storageUnitName ',' 'SHADOW' '=' storageUnitName

shadowTableRule ::=
  tableName '(' shadowAlgorithm ')'
    
shadowAlgorithm ::=
  'TYPE' '(' 'NAME' '=' algorithmType ',' propertiesDefinition ')'

ruleName ::=
  identifier

storageUnitName ::=
  identifier

tableName ::=
  identifier

algorithmName ::=
  identifier

algorithmType ::=
  string

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- Duplicate `ruleName` cannot be created;
- `storageUnitMapping` specifies the mapping relationship between the `source` database and the shadow library. You need to
  use the storage unit managed by RDL, please refer
  to [STORAGE UNIT](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/);
- `shadowAlgorithm` can act on multiple `shadowTableRule` at the same time;
- If `algorithmName` is not specified, it will be automatically generated according to `ruleName`, `tableName`
  and `algorithmType`;
- `algorithmType` currently supports `VALUE_MATCH`, `REGEX_MATCH` and `SQL_HINT`;
- `ifNotExists` caluse is used for avoid `Duplicate shadow rule` error.

### Example

- Create a shadow rule

```sql
CREATE SHADOW RULE shadow_rule(
  SOURCE=demo_ds,
  SHADOW=demo_ds_shadow,
  t_order(TYPE(NAME="SQL_HINT")), 
  t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))
);
```

- Create a shadow rule with `ifNotExists` clause

```sql
CREATE SHADOW RULE IF NOT EXISTS shadow_rule(
  SOURCE=demo_ds,
  SHADOW=demo_ds_shadow,
  t_order(TYPE(NAME="SQL_HINT")), 
  t_order_item(TYPE(NAME="VALUE_MATCH", PROPERTIES("operation"="insert","column"="user_id", "value"='1')))
);
```

### Reserved word

`CREATE`, `SHADOW`, `RULE`, `SOURCE`, `SHADOW`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [STORAGE UNIT](https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/storage-unit-definition/)