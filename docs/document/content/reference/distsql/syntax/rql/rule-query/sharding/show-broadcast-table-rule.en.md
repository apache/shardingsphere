+++
title = "SHOW BROADCAST TABLE RULE"
weight = 15
+++

## Description

The `SHOW BROADCAST TABLE RULE` syntax is used to broadcast tables for specified database.

### Syntax

```sql
ShowBroadcastTableRule ::=
  'SHOW' 'BROADCAST' 'TABLE' 'RULES' ('FROM' databaseName)? 

databaseName ::=
  identifier
```

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Return value description

| Column                | Description           |
| --------------------- | --------------------- |
| broadcast_table       | Broadcast table name  |

### Example

- Query broadcast tables for specified database.

```sql
SHOW BROADCAST TABLE RULES FROM test1;
```

```sql
mysql> SHOW BROADCAST TABLE RULES FROM test1;
+-----------------+
| broadcast_table |
+-----------------+
| t_a             |
| t_b             |
| t_c             |
+-----------------+
3 rows in set (0.00 sec)
```

- Query broadcast table for current database.

```sql
SHOW BROADCAST TABLE RULES;
```

```sql
mysql> SHOW BROADCAST TABLE RULES;
+-----------------+
| broadcast_table |
+-----------------+
| t_a             |
| t_b             |
| t_c             |
+-----------------+
3 rows in set (0.00 sec)
```

### Reserved word

`SHOW`, `BROADCAST`, `TABLE`, `RULES`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
