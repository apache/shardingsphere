+++
title = "SHOW SHARDING TABLE NODES"
weight = 9

+++

### Description

`SHOW SHARDING TABLE NODES` syntax is used to query sharding table nodes in specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowShardingTableNode::=
  'SHOW' 'SHARDING' 'TABLE' 'NODES' tableName? ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.

### Return value description

| Columns | Descriptions       |
|---------|--------------------|
| name    | Sharding rule name |
| nodes   | Sharding nodes     |

### Example

- Query sharding table nodes for specified table in the specified logical database

```sql
SHOW SHARDING TABLE NODES t_order_item FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE NODES t_order_item FROM sharding_db;
+--------------+------------------------------------------------------------------------------------------------------------+
| name         | nodes                                                                                                      |
+--------------+------------------------------------------------------------------------------------------------------------+
| t_order_item | resource_0.t_order_item_0, resource_0.t_order_item_1, resource_1.t_order_item_0, resource_1.t_order_item_1 |
+--------------+------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec)
```

- Query sharding table nodes for specified table in the current logical database

```sql
SHOW SHARDING TABLE NODES t_order_item;
```

```sql
mysql> SHOW SHARDING TABLE NODES t_order_item;
+--------------+------------------------------------------------------------------------------------------------------------+
| name         | nodes                                                                                                      |
+--------------+------------------------------------------------------------------------------------------------------------+
| t_order_item | resource_0.t_order_item_0, resource_0.t_order_item_1, resource_1.t_order_item_0, resource_1.t_order_item_1 |
+--------------+------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec
```

- Query sharding table nodes for all tables in the specified logical database

```sql
SHOW SHARDING TABLE NODES FROM sharding_db;
```

```sql
mysql> SHOW SHARDING TABLE NODES FROM sharding_db;
+--------------+------------------------------------------------------------------------------------------------------------+
| name         | nodes                                                                                                      |
+--------------+------------------------------------------------------------------------------------------------------------+
| t_order_item | resource_0.t_order_item_0, resource_0.t_order_item_1, resource_1.t_order_item_0, resource_1.t_order_item_1 |
+--------------+------------------------------------------------------------------------------------------------------------+
1 row in set (0.00 sec)
```

- Query sharding table nodes for all tables in the current logical database

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

`SHOW`, `SHARDING`, `TABLE`, `NODES`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

