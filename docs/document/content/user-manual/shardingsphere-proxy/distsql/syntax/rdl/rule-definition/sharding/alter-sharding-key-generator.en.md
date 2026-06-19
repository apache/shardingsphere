+++
title = "ALTER SHARDING KEY GENERATOR"
weight = 9
+++

## Description

The `ALTER SHARDING KEY GENERATOR` syntax is used to alter an independent sharding key generator in the currently selected database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterShardingKeyGenerator ::=
  'ALTER' 'SHARDING' 'KEY' 'GENERATOR' keyGeneratorName '(' algorithmDefinition ')'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal

keyGeneratorName ::=
  identifier

algorithmType ::=
  string
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `keyGeneratorName` is the name of the sharding key generator to be altered;
- `algorithmDefinition` defines the new key generator algorithm and its properties;
- If the specified sharding key generator does not exist, a missing rule error will be reported;
- `algorithmType` is the key generation algorithm type. For details, refer to [Distributed Primary Key](/en/user-manual/common-config/builtin-algorithm/keygen/).

### Example

- Alter a sharding key generator

```sql
ALTER SHARDING KEY GENERATOR snowflake_generator (
TYPE(NAME="UUID")
);
```

- Alter a sharding key generator with properties

```sql
ALTER SHARDING KEY GENERATOR snowflake_generator (
TYPE(NAME="SNOWFLAKE",PROPERTIES("worker-id"=2))
);
```

### Reserved word

`ALTER`, `SHARDING`, `KEY`, `GENERATOR`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING KEY GENERATOR](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-key-generator/)
- [SHOW SHARDING KEY GENERATOR](/en/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-key-generator/)
