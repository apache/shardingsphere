+++
title = "CREATE SHARDING KEY GENERATE STRATEGY"
weight = 7
+++

## Description

The `CREATE SHARDING KEY GENERATE STRATEGY` syntax is used to create sharding key generate strategy for the currently selected database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateShardingKeyGenerateStrategy ::=
  'CREATE' 'SHARDING' 'KEY' 'GENERATE' 'STRATEGY' ifNotExists? keyGenerateStrategyName '(' keyGenerateStrategyDefinition ')'

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

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

- `columnKeyGenerateStrategyDefinition` is used to define a key generate strategy based on logical table and column;
- `sequenceKeyGenerateStrategyDefinition` is used to define a key generate strategy based on sequence name;
- `keyGenerateAlgorithmDefinition` supports two styles:
  - Define key generate algorithm inline by `TYPE(NAME=..., PROPERTIES(...))`;
  - Reference an existing sharding key generator by `GENERATOR=...`;
- When inline algorithm definition is used, the system will generate and bind the corresponding sharding key generator automatically;
- `algorithmType` is the key generate algorithm type. For details, refer to [Distributed Primary Key](/en/user-manual/common-config/builtin-algorithm/keygen/);
- `ifNotExists` clause is used to avoid `Duplicate sharding key generate strategy` error.

### Example

- Create a column-based sharding key generate strategy with inline algorithm definition

```sql
CREATE SHARDING KEY GENERATE STRATEGY order_id_strategy (
TABLE=t_order,
COLUMN=order_id,
TYPE(NAME="snowflake",PROPERTIES("worker-id"=1))
);
```

- Create a column-based sharding key generate strategy by referencing an existing key generator

```sql
CREATE SHARDING KEY GENERATE STRATEGY order_id_strategy (
TABLE=t_order,
COLUMN=order_id,
GENERATOR=snowflake_generator
);
```

- Create a sequence-based sharding key generate strategy

```sql
CREATE SHARDING KEY GENERATE STRATEGY order_sequence_strategy (
SEQUENCE="order_seq",
TYPE(NAME="uuid")
);
```

- Create sharding key generate strategy with `ifNotExists` clause

```sql
CREATE SHARDING KEY GENERATE STRATEGY IF NOT EXISTS order_id_strategy (
TABLE=t_order,
COLUMN=order_id,
GENERATOR=snowflake_generator
);
```

### Reserved word

`CREATE`, `SHARDING`, `KEY`, `GENERATE`, `STRATEGY`, `TABLE`, `COLUMN`, `SEQUENCE`, `TYPE`, `NAME`, `PROPERTIES`, `GENERATOR`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [SHOW SHARDING KEY GENERATE STRATEGY](/en/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/sharding/show-sharding-key-generate-strategy/)
