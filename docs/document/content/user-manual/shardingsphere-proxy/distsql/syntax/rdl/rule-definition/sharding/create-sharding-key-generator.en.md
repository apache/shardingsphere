+++
title = "CREATE SHARDING KEY GENERATOR"
weight = 8
+++

## Description

The `CREATE SHARDING KEY GENERATOR` syntax is used to create an independent sharding key generator for the currently selected database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateShardingKeyGenerator ::=
  'CREATE' 'SHARDING' 'KEY' 'GENERATOR' ifNotExists? keyGeneratorName '(' algorithmDefinition ')'

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

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

- `keyGeneratorName` is the name of the independent sharding key generator;
- `algorithmDefinition` defines the key generator algorithm and its properties;
- `algorithmType` is the key generation algorithm type. For details, refer to [Distributed Primary Key](/en/user-manual/common-config/builtin-algorithm/keygen/);
- `ifNotExists` clause is used to avoid `Duplicate sharding key generator` error.

### Example

- Create a sharding key generator

```sql
CREATE SHARDING KEY GENERATOR snowflake_generator (
TYPE(NAME="SNOWFLAKE",PROPERTIES("worker-id"=1))
);
```

- Create a sharding key generator with `ifNotExists` clause

```sql
CREATE SHARDING KEY GENERATOR IF NOT EXISTS snowflake_generator (
TYPE(NAME="SNOWFLAKE")
);
```

### Reserved word

`CREATE`, `SHARDING`, `KEY`, `GENERATOR`, `IF`, `NOT`, `EXISTS`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [ALTER SHARDING KEY GENERATOR](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/alter-sharding-key-generator/)
- [SHOW SHARDING KEY GENERATOR](/en/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-key-generator/)
