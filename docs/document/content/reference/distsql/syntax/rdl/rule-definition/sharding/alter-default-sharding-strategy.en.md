+++
title = "ALTER DEFAULT SHARDING STRATEGY"
weight = 6
+++

## Description

The `ALTER DEFAULT SHARDING STRATEGY` syntax is used to alter a default sharding strategy

### Syntax

```sql
AlterDefaultShardingStrategy ::=
  'ALTER' 'DEFAULT' 'SHARDING' ('DATABASE' | 'TABLE') 'STRATEGY' '(' shardingStrategy ')'

shardingStrategy ::=
  'TYPE' '=' strategyType ',' ( 'SHARDING_COLUMN' '=' columnName  | 'SHARDING_COLUMNS' '=' columnNames ) ',' ( 'SHARDING_ALGORITHM' '=' algorithmName | algorithmDefinition )

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ',' 'PROPERTIES'  '(' propertyDefinition ')' ')'  

columnNames ::=
  columnName (',' columnName)+

columnName ::=
  identifier

algorithmName ::=
  identifier
  
algorithmType ::=
  string
```

### Supplement

- When using the complex sharding algorithm, multiple sharding columns need to be specified using `SHARDING_COLUMNS`;
- `algorithmType` is the sharding algorithm type. For detailed sharding algorithm type information, please refer
  to [Sharding Algorithm](/en/user-manual/common-config/builtin-algorithm/sharding/).

### Example

- Alter a default sharding table strategy

```sql
ALTER DEFAULT SHARDING TABLE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}")))
);
```

### Reserved word

`ALTER`, `DEFAULT`, `SHARDING`, `DATABASE`, `TABLE`, `STRATEGY`, `TYPE`, `SHARDING_COLUMN`, `SHARDING_COLUMNS`, `SHARDING_ALGORITHM`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
