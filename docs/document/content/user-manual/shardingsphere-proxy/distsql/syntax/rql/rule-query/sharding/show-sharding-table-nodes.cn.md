+++
title = "SHOW SHARDING TABLE NODES"
weight = 9
+++

### 描述

`SHOW SHARDING TABLE NODES` 语法用于查询指定逻辑库中的指定分片表的节点分布。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowShardingTableNode::=
  'SHOW' 'SHARDING' 'TABLE' 'NODES' tableName? ('FROM' databaseName)?

tableName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列     | 说明     |
|-------|--------|
| name  | 分片规则名称 |
| nodes | 分片节点   |

### 示例

- 查询指定逻辑库中指定分片表的节点分布

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

- 查询当前逻辑库中指定分片表的节点分布

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
1 row in set (0.00 sec)
```

- 查询指定逻辑库中所有分片表的节点分布

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

- 查询当前逻辑库中所有分片表的节点分布

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
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`SHARDING`、`TABLE`、`NODES`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)

