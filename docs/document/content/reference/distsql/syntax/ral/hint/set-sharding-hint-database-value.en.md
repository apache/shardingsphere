+++
title = "SET SHARDING HINT DATABASE_VALUE"
weight = 3
+++

### Description

The `SET SHARDING HINT DATABASE_VALUE` syntax is used to set sharding value for database sharding only for current connection.

### Syntax

```sql
SetShardingHintDatabaseValue ::=
  'SET' 'SHARDING' 'HINT' 'DATABASE_VALUE' '=' databaseShardingValue

  databaseShardingValue ::=
    int
```

### Example

- Set the sharding database value

```sql
SET SHARDING HINT DATABASE_VALUE = 100;
```

### Reserved word

`SET`, `SHARDING`, `HINT`, `DATABASE_VALUE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
