+++
title = "ADD SHARDING HINT TABLE_VALUE"
weight = 5
+++

### Description

The `ADD SHARDING HINT TABLE_VALUE` syntax is used to add table sharding value to specified table for current connection.

### Syntax

```sql
AddShardingHintDatabaseValue ::=
  'ADD' 'SHARDING' 'HINT' 'TABLE_VALUE' shardingHintTableValueDefination
  
  shardingHintTableValueDefination ::=
    tableName '=' tableShardingValue

  tableName ::=
    identifier

  tableShardingValue ::=
    int
```

### Example

- Add the table sharding value for specified table

```sql
ADD SHARDING HINT TABLE_VALUE t_order = 100;
```

### Reserved word

`ADD`, `SHARDING`, `HINT`, `TABLE_VALUE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
