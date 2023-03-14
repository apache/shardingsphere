+++
title = "CREATE READWRITE_SPLITTING RULE"
weight = 2
+++

## Description

The `CREATE READWRITE_SPLITTING RULE` syntax is used to create a readwrite splitting rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateReadwriteSplittingRule ::=
  'CREATE' 'READWRITE_SPLITTING' 'RULE' ifNotExists? readwriteSplittingDefinition (',' readwriteSplittingDefinition)*

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

readwriteSplittingDefinition ::=
  ruleName '(' (staticReadwriteSplittingDefinition | dynamicReadwriteSplittingDefinition) (',' loadBalancerDefinition)? ')'

staticReadwriteSplittingDefinition ::=
    'WRITE_STORAGE_UNIT' '=' writeStorageUnitName ',' 'READ_STORAGE_UNITS' '(' storageUnitName (',' storageUnitName)* ')'

dynamicReadwriteSplittingDefinition ::=
    'AUTO_AWARE_RESOURCE' '=' resourceName

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

- Support the creation of static readwrite-splitting rules and dynamic readwrite-splitting rules;
- Dynamic readwrite-splitting rules rely on database discovery rules;
- `loadBalancerType` specifies the load balancing algorithm type, please refer to [Load Balance Algorithm](/en/user-manual/common-config/builtin-algorithm/load-balance/);
- Duplicate `ruleName` will not be created;
- `ifNotExists` clause used for avoid `Duplicate readwrite_splitting rule` error.

### Example

#### Create a statics readwrite splitting rule

```sql
CREATE READWRITE_SPLITTING RULE ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

#### Create a dynamic readwrite splitting rule

```sql
CREATE READWRITE_SPLITTING RULE ms_group_1 (
    AUTO_AWARE_RESOURCE=group_0
    TYPE(NAME="random")
);
```

#### Create readwrite splitting rule with `ifNotExists` clause

- Statics readwrite splitting rule

```sql
CREATE READWRITE_SPLITTING RULE IF NOT EXISTS ms_group_0 (
    WRITE_STORAGE_UNIT=write_ds,
    READ_STORAGE_UNITS(read_ds_0,read_ds_1),
    TYPE(NAME="random")
);
```

- Dynamic readwrite splitting rule

```sql
CREATE READWRITE_SPLITTING RULE IF NOT EXISTS ms_group_1 (
    AUTO_AWARE_RESOURCE=group_0
    TYPE(NAME="random")
);
```

### Reserved word

`CREATE`, `READWRITE_SPLITTING`, `RULE`, `WRITE_STORAGE_UNIT`, `READ_STORAGE_UNITS`, `AUTO_AWARE_RESOURCE`
, `TYPE`, `NAME`, `PROPERTIES`, `TRUE`, `FALSE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [Load Balance Algorithm](/en/user-manual/common-config/builtin-algorithm/load-balance/)
