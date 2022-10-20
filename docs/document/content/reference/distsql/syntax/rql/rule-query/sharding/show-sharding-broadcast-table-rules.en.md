+++
title = "SHOW SHARDING BROADCAST TABLE RULES"
weight = 15

+++

### Description

`SHOW SHARDING BROADCAST TABLE RULES` syntax is used to query sharding broadcast table rules for specified logical database

### Syntax

```
ShowShardingBroadcastTableRules::=
  'SHOW' 'SHARDING' 'BROADCAST' 'TABLE' 'RULES'('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| Columns                  | Descriptions                    |
| -------------------------| --------------------------------|
| sharding_broadcast_tables| sharding Broadcast Table list   |

### Example

- Query sharding broadcast table rules for the spicified logical database

```sql
SHOW SHARDING BROADCAST TABLE RULES FROM test1;
```

```sql
mysql> SHOW SHARDING BROADCAST TABLE RULES FROM test1;
+---------------------------+
| sharding_broadcast_tables |
+---------------------------+
| t_a                       |
| t_b                       |
+---------------------------+
2 rows in set (0.00 sec)
```

- Query sharding broadcast table rules for the current logical database

```sql
SHOW SHARDING BROADCAST TABLE RULES;
```

```sql
mysql> SHOW SHARDING BROADCAST TABLE RULES;
+---------------------------+
| sharding_broadcast_tables |
+---------------------------+
| t_a                       |
| t_b                       |
+---------------------------+
2 rows in set (0.01 sec)
```

### Reserved word

`SHOW`、`SHARDING`、`BROADCAST`、`TABLE`、`RULES`、`FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
