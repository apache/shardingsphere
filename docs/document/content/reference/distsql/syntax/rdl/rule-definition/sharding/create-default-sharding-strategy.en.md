+++
title = "CREATE DEFAULT SHARDING STRATEGY"
weight = 5
+++

## Description

The `CREATE DEFAULT SHARDING STRATEGY` syntax is used to create a default sharding strategy

### Syntax

```sql
CreateDefaultShardingStrategy ::=
  'CREATE' 'DEFAULT' 'SHARDING' ('DATABASE' | 'TABLE') 'STRATEGY' '(' shardingStrategy ')'

shardingStrategy ::=
  'TYPE' '=' strategyType ',' ( 'SHARDING_COLUMN' '=' columnName  | 'SHARDING_COLUMNS' '=' columnNames ) ',' ( 'SHARDING_ALGORITHM' '=' algorithmName | algorithmDefinition )

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

columnNames ::=
  columnName (',' columnName)+

columnName ::=
  string

algorithmName ::=
  string
  
algorithmType ::=
  string
```

### Supplement

- When using the complex sharding algorithm, multiple sharding columns need to be specified using `SHARDING_COLUMNS`;
- `algorithmType` is the sharding algorithm type. For detailed sharding algorithm type information, please refer
  to [Sharding Algorithm](/en/user-manual/common-config/builtin-algorithm/sharding/).

### Example

#### 1.Create a default sharding strategy by using an existing sharding algorithm

```sql
-- create a sharding algorithm
CREATE SHARDING ALGORITHM database_inline (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${order_id % 2}"))
);

-- create a default sharding database strategy
CREATE DEFAULT SHARDING DATABASE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM=database_inline
);
```

#### 2.Create sharding algorithm and default sharding table strategy at the same time

```sql
-- create a default sharding table strategy
CREATE DEFAULT SHARDING TABLE STRATEGY (
    TYPE="standard", SHARDING_COLUMN=user_id, SHARDING_ALGORITHM(TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}")))
);
```

### Reserved word

`CREATE`, `DEFAULT`, `SHARDING`, `DATABASE`, `TABLE`, `STRATEGY`, `TYPE`, `SHARDING_COLUMN`, `SHARDING_COLUMNS`, `SHARDING_ALGORITHM`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
- [CREATE SHARDING ALGORITHM](/en/reference/distsql/syntax/rdl/rule-definition/create-sharding-algorithm/)
