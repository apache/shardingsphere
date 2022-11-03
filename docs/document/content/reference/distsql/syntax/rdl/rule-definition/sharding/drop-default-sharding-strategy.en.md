+++
title = "DROP DEFAULT SHARDING STRATEGY"
weight = 7
+++

## Description

The `DROP DEFAULT SHARDING STRATEGY` syntax is used to drop default sharding strategy for specified database.

### Syntax

```sql
DropDefaultShardingStrategy ::=
  'DROP' 'DEFAULT' 'SHARDING' ('TABLE' | 'DATABASE') 'STRATEGY' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Drop default sharding table strategy for specified database.

```sql
DROP DEFAULT SHARDING TABLE STRATEGY FROM test1;
```

- Drop default sharding database strategy for current database.

```sql
DROP DEFAULT SHARDING DATABASE STRATEGY;
```

### Reserved word

`DROP`, `DEFAULT` , `SHARDING`, `TABLE`, `DATABASE` ,`STRATEGY`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
