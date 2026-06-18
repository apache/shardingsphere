+++
title = "CREATE DEFAULT SHARDING STRATEGY"
weight = 4
+++

## Description

The `CREATE DEFAULT SHARDING STRATEGY` syntax is used to create a default sharding strategy

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
CreateDefaultShardingStrategy ::=
  'CREATE' 'DEFAULT' 'SHARDING' ('DATABASE' | 'TABLE') 'STRATEGY' ifNotExists? '(' shardingStrategy ')'

ifNotExists ::=
  'IF' 'NOT' 'EXISTS'

shardingStrategy ::=
  'TYPE' '=' strategyType ',' ('SHARDING_COLUMN' '=' columnName | 'SHARDING_COLUMNS' '=' columnNames) ',' 'SHARDING_ALGORITHM' '=' algorithmDefinition

strategyType ::=
  string

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ',' propertiesDefinition ')'  

columnNames ::=
  columnName (',' columnName)+

columnName ::=
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

- When using the complex sharding algorithm, multiple sharding columns need to be specified using `SHARDING_COLUMNS`;
- `algorithmType` is the sharding algorithm type. For detailed sharding algorithm type information, please refer
  to [Sharding Algorithm](/en/user-manual/common-config/builtin-algorithm/sharding/);
- `ifNotExists` clause is used for avoid `Duplicate default sharding strategy` error.

### Example

- create a default sharding table strategy

```sql
-- create a default sharding table strategy
CREATE DEFAULT SHARDING TABLE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}")))
);
```

- create a default sharding table strategy with `ifNotExists` clause

```sql
CREATE DEFAULT SHARDING TABLE STRATEGY IF NOT EXISTS (
    TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}")))
);
```

### Reserved word

`CREATE`, `DEFAULT`, `SHARDING`, `DATABASE`, `TABLE`, `STRATEGY`, `TYPE`, `SHARDING_COLUMN`, `SHARDING_COLUMNS`, `SHARDING_ALGORITHM`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
