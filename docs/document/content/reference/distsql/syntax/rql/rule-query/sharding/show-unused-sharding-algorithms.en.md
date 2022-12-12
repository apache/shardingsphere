+++
title = "SHOW UNUSED SHARDING ALGORITHMS"
weight = 4
+++

### Description

The `SHOW UNUSED SHARDING ALGORITHMS` syntax is used to query the unused sharding algorithms in the specified database.

### Syntax

```
ShowShardingAlgorithms::=
  'SHOW' 'UNUSED' 'SHARDING' 'ALGORITHMS' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column | Description                   |
| ------ | ----------------------------- |
| name   | Sharding algorithm name       |
| type   | Sharding algorithm type       |
| props  | Sharding algorithm properties |

### Example

- Query the unused sharding table algorithms of the specified logical database

```sql
SHOW UNUSED SHARDING ALGORITHMS;
```

```sql
mysql> SHOW UNUSED SHARDING ALGORITHMS;
+---------------+--------+-----------------------------------------------------+
| name          | type   | props                                               |
+---------------+--------+-----------------------------------------------------+
| t1_inline     | INLINE | algorithm-expression=t_order_${order_id % 2}        |
+---------------+--------+-----------------------------------------------------+
1 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `UNUSED`, `SHARDING`, `ALGORITHMS`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
