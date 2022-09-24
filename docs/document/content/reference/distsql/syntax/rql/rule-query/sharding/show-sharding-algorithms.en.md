+++
title = "SHOW SHARDING ALGORITHMS"
weight = 3
+++

### Description

The `SHOW SHARDING ALGORITHMS` syntax is used to query the sharding algorithms in the specified database.

### Syntax

```
ShowShardingAlgorithms::=
  'SHOW' 'SHARDING' 'ALGORITHMS' ('FROM' databaseName)?

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

- Query the sharding table algorithms of the specified logical database

```sql
SHOW SHARDING ALGORITHMS;
```

```sql
mysql> SHOW SHARDING ALGORITHMS;
+-------------------------+--------+-----------------------------------------------------+
| name                    | type   | props                                               |
+-------------------------+--------+-----------------------------------------------------+
| t_order_inline          | INLINE | algorithm-expression=t_order_${order_id % 2}        |
| t_order_item_inline     | INLINE | algorithm-expression=t_order_item_${order_id % 2}   |
+-------------------------+--------+-----------------------------------------------------+
2 row in set (0.01 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `ALGORITHMS`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
