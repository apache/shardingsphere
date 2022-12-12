+++
title = "DROP SHARDING KEY GENERATOR"
weight = 11
+++

## Description

The `DROP SHARDING KEY GENERATOR` syntax is used to drop sharding key generator for specified database.

### Syntax

```sql
DropShardingKeyGenerator ::=
  'DROP' 'SHARDING' 'KEY' 'GENERATOR' keyGeneratorName ('FROM' databaseName)?

keyGeneratorName ::=
  identifier
  
databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Drop sharding key generator for specified database.

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake FROM test1;
```

- Drop sharding key generator for current database.

```sql
DROP SHARDING KEY GENERATOR t_order_snowflake;
```

### Reserved word

`DROP`, `SHARDING`, `KEY`, `GENERATOR`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
