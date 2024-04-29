+++
title = "ALTER READWRITE_SPLITTING RULE"
weight = 2
+++

## Description

The `ALTER READWRITE_SPLITTING RULE` syntax is used to alter a readwrite-splitting rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterReadwriteSplittingRule ::=
  'ALTER' 'READWRITE_SPLITTING' 'RULE' readwriteSplittingDefinition (',' readwriteSplittingDefinition)*

readwriteSplittingDefinition ::=
  ruleName '(' dataSourceDefinition (',' transactionalReadQueryStrategyDefinition)? (',' loadBalancerDefinition)? ')'

dataSourceDefinition ::=
    'WRITE_STORAGE_UNIT' '=' writeStorageUnitName ',' 'READ_STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')' 

transactionalReadQueryStrategyDefinition ::=
    'TRANSACTIONAL_READ_QUERY_STRATEGY' '=' transactionalReadQueryStrategyType

loadBalancerDefinition ::=
    'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

ruleName ::=
  identifier

writeStorageUnitName ::=
  identifier

storageUnitName ::=
  identifier

transactionalReadQueryStrategyType ::=
  string

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

- `transactionalReadQueryStrategyType` specifies the routing strategy for read query within a transaction, please refer to [YAML configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/);
- `algorithmType` specifies the load balancing algorithm type, please refer to [Load Balance Algorithm](/en/user-manual/common-config/builtin-algorithm/load-balance/).

### Example

#### Alter a readwrite-splitting rule

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

### Reserved word

`ALTER`, `READWRITE_SPLITTING`, `RULE`, `WRITE_STORAGE_UNIT`, `READ_STORAGE_UNITS`
, `TYPE`, `NAME`, `PROPERTIES`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [Load Balance Algorithm](/en/user-manual/common-config/builtin-algorithm/load-balance/)
