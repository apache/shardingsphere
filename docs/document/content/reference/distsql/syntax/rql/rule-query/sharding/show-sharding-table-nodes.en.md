+++
title = "SHOW SHARDING TABLE NODES"
weight = 10

+++

### Description

`SHOW SHARDING TABLE NODES` syntax is used to query sharding table nodes in specified database.

### Syntax

```
ShowShardingTableNode::=
  'SHOW' 'SHARDING' 'TABLE' 'NODE'('FROM' databaseName)?

databaseName ::=
  identifier
```

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| Columns     | Descriptions       |
| ------------| -------------------|
| name        | Sharding rule name |
| nodes       | Sharding nodes     |

### Example

- Query sharding table nodes for the specified logical database

```sql
SHOW SHARDING TABLE NODES FROM test1;
```

```sql
mysql> SHOW SHARDING TABLE NODES FROM test1;
+--------------+------------------------------------------------------------------------------------------------------------+
| name         | nodes                                                                                                      |
+--------------+------------------------------------------------------------------------------------------------------------+
| t_order_item | resource_0.t_order_item_0, resource_0.t_order_item_1, resource_1.t_order_item_0, resource_1.t_order_item_1 |
+--------------+------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec)
```

- Query sharding table nodes for the current logical database

```sql
SHOW SHARDING TABLE NODES;
```

```sql
mysql> SHOW SHARDING TABLE NODES;
+--------------+------------------------------------------------------------------------------------------------------------+
| name         | nodes                                                                                                      |
+--------------+------------------------------------------------------------------------------------------------------------+
| t_order_item | resource_0.t_order_item_0, resource_0.t_order_item_1, resource_1.t_order_item_0, resource_1.t_order_item_1 |
+--------------+------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec
```

### Reserved word

`SHOW`, `SHARDING`, `TABLE`, `NODE`, `FROM`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)

