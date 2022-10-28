+++
title = "ADD SHARDING HINT DATABASE_VALUE"
weight = 3
+++

### Description

The `ADD SHARDING HINT DATABASE_VALUE` syntax is used to add sharding database value to specified table for current connection.

### Syntax

```sql
AddShardingHintDatabaseValue ::=
  'ADD' 'SHARDING' 'HINT' 'DATABASE_VALUE' shardingHintDatabaseValueDefination
  
  shardingHintDatabaseValueDefination ::=
    tableName '=' databaseShardingValue

  tableName ::=
    identifier

  databaseShardingValue ::=
    int
```

### Example

- Add the database sharding value for specified table

```sql
ADD SHARDING HINT DATABASE_VALUE t_order = 100;
```

### Reserved word

`ADD`, `SHARDING`, `HINT`, `DATABASE_VALUE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
