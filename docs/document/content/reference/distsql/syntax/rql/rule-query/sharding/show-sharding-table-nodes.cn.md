+++
title = "SHOW SHARDING TABLE NODES"
weight = 10
+++

### 描述

`SHOW SHARDING TABLE NODES` 语法用于查询指定逻辑库中的分片节点。

### 语法

```
ShowShardingTableNode::=
  'SHOW' 'SHARDING' 'TABLE' 'NODE'('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列     | 说明          |
| ------| --------------|
| name  | 分片规则名称    |
| nodes | 分片节点       |

### 示例

- 查询指定逻辑库中的分片节点

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

- 查询当前逻辑库中的分片节点

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

`SHOW`、`SHARDING`、`TABLE`、`NODE`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

