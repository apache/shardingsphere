+++
title = "ALTER READWRITE_SPLITTING RULE"
weight = 3
+++

## Description

The `ALTER READWRITE_SPLITTING RULE` syntax is used to alter a readwrite splitting rule.

### Syntax

```sql
AlterReadwriteSplittingRule ::=
  'ALTER' 'READWRITE_SPLITTING' 'RULE' readwriteSplittingDefinition ( ',' readwriteSplittingDefinition )*

readwriteSplittingDefinition ::=
  ruleName '(' ( staticReadwriteSplittingDefinition | dynamicReadwriteSplittingDefinition ) ( ',' loadBalancerDefinition )? ')'

staticReadwriteSplittingDefinition ::=
    'WRITE_RESOURCE' '=' writeResourceName ',' 'READ_RESOURCES' '(' ruleName (',' ruleName)* ')'

dynamicReadwriteSplittingDefinition ::=
    'AUTO_AWARE_RESOURCE' '=' resourceName ( ',' 'WRITE_DATA_SOURCE_QUERY_ENABLED' '=' ('TRUE' | 'FALSE') )?

loadBalancerDefinition ::=
    'TYPE' '(' 'NAME' '=' loadBalancerType ( ',' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' )? ')'

ruleName ::=
  identifier

writeResourceName ::=
  identifier

resourceName ::=
  identifier
    
loadBalancerType ::=
  string
```

### Supplement

- Dynamic readwrite-splitting rules rely on database discovery rules;
- `loadBalancerType` specifies the load balancing algorithm type, please refer to Load Balance Algorithm;

### Example

#### Alter a statics readwrite splitting rule

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_RESOURCE=write_ds,
    READ_RESOURCES(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

#### Alter a dynamic readwrite splitting rule

```sql
ALTER READWRITE_SPLITTING RULE ms_group_1 (
    AUTO_AWARE_RESOURCE=group_0,
    WRITE_DATA_SOURCE_QUERY_ENABLED=false,
    TYPE(NAME="random",PROPERTIES("read_weight"="2:1"))
);
```

### Reserved word

`ALTER`, `READWRITE_SPLITTING`, `RULE`, `WRITE_RESOURCE`, `READ_RESOURCES`, `AUTO_AWARE_RESOURCE`
, `WRITE_DATA_SOURCE_QUERY_ENABLED`, `TYPE`, `NAME`, `PROPERTIES`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
