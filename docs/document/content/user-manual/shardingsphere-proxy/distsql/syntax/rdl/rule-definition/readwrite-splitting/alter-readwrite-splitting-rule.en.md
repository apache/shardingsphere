+++
title = "ALTER READWRITE_SPLITTING RULE"
weight = 3
+++

## Description

The `ALTER READWRITE_SPLITTING RULE` syntax is used to alter a readwrite splitting rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterReadwriteSplittingRule ::=
  'ALTER' 'READWRITE_SPLITTING' 'RULE' readwriteSplittingDefinition (',' readwriteSplittingDefinition)*

readwriteSplittingDefinition ::=
  ruleName '(' (staticReadwriteSplittingDefinition | dynamicReadwriteSplittingDefinition) (',' loadBalancerDefinition)? ')'

staticReadwriteSplittingDefinition ::=
    'WRITE_STORAGE_UNIT' '=' writeStorageUnitName ',' 'READ_STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')'

dynamicReadwriteSplittingDefinition ::=
    'AUTO_AWARE_RESOURCE' '=' resourceName (',' 'WRITE_DATA_SOURCE_QUERY_ENABLED' '=' ('TRUE' | 'FALSE'))?

loadBalancerDefinition ::=
    'TYPE' '(' 'NAME' '=' loadBalancerType (',' propertiesDefinition)? ')'

ruleName ::=
  identifier

writeStorageUnitName ::=
  identifier

storageUnitName ::=
  identifier

resourceName ::=
  identifier
    
loadBalancerType ::=
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

- Dynamic readwrite-splitting rules rely on database discovery rules;
- `loadBalancerType` specifies the load balancing algorithm type, please refer to Load Balance Algorithm;

### Example

#### Alter a statics readwrite splitting rule

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

#### Alter a dynamic readwrite splitting rule

```sql
ALTER READWRITE_SPLITTING RULE ms_group_1 (
    AUTO_AWARE_RESOURCE=group_0,
    WRITE_DATA_SOURCE_QUERY_ENABLED=false,
    TYPE(NAME="random")
);
```

### Reserved word

`ALTER`, `READWRITE_SPLITTING`, `RULE`, `WRITE_RESOURCE`, `READ_RESOURCES`, `AUTO_AWARE_RESOURCE`
, `WRITE_DATA_SOURCE_QUERY_ENABLED`, `TYPE`, `NAME`, `PROPERTIES`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
