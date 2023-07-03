+++
title = "CREATE READWRITE_SPLITTING RULE"
weight = 1
+++

## Description

The `CREATE READWRITE_SPLITTING RULE` syntax is used to create a read/write splitting rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateReadwriteSplittingRule ::=
  'CREATE' 'READWRITE_SPLITTING' 'RULE' ifNotExists? readwriteSplittingDefinition (',' readwriteSplittingDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

readwriteSplittingDefinition ::=
  ruleName '(' dataSourceDefinition (',' transactionalReadQueryStrategyDefinition)? (',' loadBalancerDefinition)? ')'

dataSourceDefinition ::=
    'WRITE_STORAGE_UNIT' '=' writeStorageUnitName ',' 'READ_STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')' 

transactionalReadQueryStrategyDefinition ::=
    'TRANSACTIONAL_READ_QUERY_STRATEGY' '=' transactionalReadQueryStrategyType

loadBalancerDefinition ::=
    'TYPE' '(' 'NAME' '=' loadBalancerType (',' propertiesDefinition)? ')'

ruleName ::=
  identifier

writeStorageUnitName ::=
  identifier

storageUnitName ::=
  identifier

transactionalReadQueryStrategyType ::=
  string

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

### Note

- `transactionalReadQueryStrategyType` specifies the routing strategy for read query within a transaction, please refer to [YAML configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/);
- `loadBalancerType` specifies the load balancing algorithm type, please refer to [Load Balance Algorithm](/en/user-manual/common-config/builtin-algorithm/load-balance/);
- Duplicate `ruleName` will not be created;
- `ifNotExists` clause used to avoid the `Duplicate readwrite_splitting rule` error.

### Example

#### Create a read/write splitting rule

```sql
CREATE READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

#### Create read/write splitting rule with the `ifNotExists` clause

- read/write splitting rule

```sql
CREATE READWRITE_SPLITTING RULE IF NOT EXISTS ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

### Reserved words

`CREATE`, `READWRITE_SPLITTING`, `RULE`, `WRITE_STORAGE_UNIT`, `READ_STORAGE_UNITS`
, `TYPE`, `NAME`, `PROPERTIES`, `TRUE`, `FALSE`

### Related links

- [Reserved words](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [Load Balance Algorithm](/en/user-manual/common-config/builtin-algorithm/load-balance/)
