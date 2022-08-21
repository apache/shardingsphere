+++
title = "CREATE SHARDING ALGORITHM"
weight = 4
+++

## Description

The `CREATE SHARDING ALGORITHM` syntax is used to create a sharding algorithm for the currently selected database.

### Syntax

```sql
CreateShardingAlgorithm ::=
  'CREATE' 'SHARDING' 'ALGORITHM' shardingAlgorithmName '(' algorithmDefinition ')'

algorithmDefinition ::=
  'TYPE' '(' 'NAME' '=' algorithmType ( ',' 'PROPERTIES'  '(' propertyDefinition  ')' )?')'  

propertyDefinition ::=
  ( key  '=' value ) ( ',' key  '=' value )*

shardingAlgorithmName ::=
  identifier
  
algorithmType ::=
  identifier
```

### Supplement

- `algorithmType` is the sharding algorithm type. For detailed sharding algorithm type information, please refer to [Sharding Algorithm](/en/user-manual/common-config/builtin-algorithm/sharding/).

### Example

#### 1.Create sharding algorithms

```SQL
-- create a sharding algorithm of type INLINE
CREATE SHARDING ALGORITHM inline_algorithm (
    TYPE(NAME="inline", PROPERTIES("algorithm-expression"="t_order_${user_id % 2}"))
);

-- create a sharding algorithm of type AUTO_INTERVAL
CREATE SHARDING ALGORITHM interval_algorithm (
    TYPE(NAME="auto_interval", PROPERTIES("datetime-lower"="2022-01-01 00:00:00", "datetime-upper"="2022-01-03 00:00:00", "sharding-seconds"="86400"))
);
```

### Reserved word

`CREATE`, `SHARDING`, `ALGORITHM`, `TYPE`, `NAME`, `PROPERTIES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
