+++
title = "SHOW UNUSED SHARDING ALGORITHMS"
weight = 4
+++

### 描述

`SHOW UNUSED SHARDING ALGORITHMS` 语法用于查询指定逻辑库未使用的分片算法。

### 语法

```
ShowShardingAlgorithms::=
  'SHOW' 'UNUSED' 'SHARDING' 'ALGORITHMS' ('FROM' databaseName)?

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

- 查询指定逻辑库未使用的分片算法

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

### 保留字

`SHOW`、`UNUSED`、`SHARDING`、`ALGORITHMS`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)
