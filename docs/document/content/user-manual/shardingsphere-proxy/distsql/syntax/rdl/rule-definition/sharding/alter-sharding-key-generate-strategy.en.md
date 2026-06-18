+++
title = "ALTER SHARDING KEY GENERATE STRATEGY"
weight = 8
+++

## Description

The `ALTER SHARDING KEY GENERATE STRATEGY` syntax is used to alter sharding key generate strategy for the currently selected database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterShardingKeyGenerateStrategy ::=
  'ALTER' 'SHARDING' 'KEY' 'GENERATE' 'STRATEGY' keyGenerateStrategyName '(' keyGenerateStrategyDefinition ')'

keyGenerateStrategyDefinition ::=
  columnKeyGenerateStrategyDefinition
  | sequenceKeyGenerateStrategyDefinition

columnKeyGenerateStrategyDefinition ::=
  'TABLE' '=' tableName ',' 'COLUMN' '=' columnName ',' keyGenerateAlgorithmDefinition

sequenceKeyGenerateStrategyDefinition ::=
  'SEQUENCE' '=' sequenceName ',' keyGenerateAlgorithmDefinition

keyGenerateAlgorithmDefinition ::=
  algorithmDefinition
  | 'GENERATOR' '=' keyGeneratorName

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType (',' propertiesDefinition)? ')'

propertiesDefinition ::=
  'PROPERTIES' '(' key '=' value (',' key '=' value)* ')'

key ::=
  string

value ::=
  literal

keyGenerateStrategyName ::=
  identifier

tableName ::=
  identifier

columnName ::=
  identifier

sequenceName ::=
  identifier | string

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

- The syntax structure is consistent with `CREATE SHARDING KEY GENERATE STRATEGY`;
- `ALTER` replaces the existing sharding key generate strategy with the new definition;
- `GENERATOR=...` is used to switch to an existing sharding key generator;
- `TYPE(NAME=..., PROPERTIES(...))` is used to switch to a new inline algorithm definition;
- `algorithmType` is the key generate algorithm type. For details, refer to [Distributed Primary Key](/en/user-manual/common-config/builtin-algorithm/keygen/).

### Example

- Alter a column-based sharding key generate strategy by referencing an existing key generator

```sql
ALTER SHARDING KEY GENERATE STRATEGY order_id_strategy (
TABLE=t_order,
COLUMN=order_id,
GENERATOR=snowflake_generator
);
```

- Alter to a sequence-based sharding key generate strategy with inline algorithm

```sql
ALTER SHARDING KEY GENERATE STRATEGY order_sequence_strategy (
SEQUENCE="order_seq",
TYPE(NAME="uuid")
);
```

### Reserved word

`ALTER`, `SHARDING`, `KEY`, `GENERATE`, `STRATEGY`, `TABLE`, `COLUMN`, `SEQUENCE`, `TYPE`, `NAME`, `PROPERTIES`, `GENERATOR`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING KEY GENERATE STRATEGY](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-key-generate-strategy/)
