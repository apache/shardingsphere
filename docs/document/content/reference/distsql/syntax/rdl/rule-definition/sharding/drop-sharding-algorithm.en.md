+++
title = "DROP SHARDING ALGORITHM"
weight = 12
+++

## Description

The `DROP SHARDING ALGORITHM` syntax is used to drop sharding algorithm for specified database.

### Syntax

```sql
DropShardingAlgorithm ::=
  'DROP' 'SHARDING' 'ALGORITHM' shardingAlgorithmName ('FROM' databaseName)?

shardingAlgorithmName ::=
  identifier

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Drop sharding algorithm for specified database.

```sql
DROP SHARDING ALGORITHM t_order_hash_mod FROM test1;
```

- Drop sharding algorithm for current database.

```sql
DROP SHARDING ALGORITHM t_order_hash_mod;
```

### Reserved word

`DROP`, `SHARDING`, `ALGORITHM`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
