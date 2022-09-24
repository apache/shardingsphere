+++
title = "SHOW SHARDING ALGORITHMS"
weight = 3
+++

### 描述

`SHOW SHARDING ALGORITHMS` 语法用于查询指定逻辑库的分片算法。

### 语法

```
ShowShardingAlgorithms::=
  'SHOW' 'SHARDING' 'ALGORITHMS' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列     | 说明          |
| ------| --------------|
| name  | 分片算法名称    |
| type  | 分片算法类型    |
| props | 分片算法参数    |

### 示例

- 查询指定逻辑库的分片算法

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

### 保留字

`CREATE`、`SHARDING`、`ALGORITHMS`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
