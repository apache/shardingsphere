+++
title = "SHOW SHARDING BROADCAST TABLE RULES"
weight = 15
+++

### 描述

`SHOW SHARDING BROADCAST TABLE RULES` 语法用于查询指定逻辑库中的广播表

### 语法

```
ShowShardingBroadcastTableRules::=
  'SHOW' 'SHARDING' 'BROADCAST' 'TABLE' 'RULES'('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                         | 说明           |
| ---------------------------| --------------|
| sharding_broadcast_tables  | 广播表名称      |

### 示例

- 查询指定逻辑库中的广播表

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

- 查询当前逻辑库中的广播表

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

### 保留字

`SHOW`、`SHARDING`、`BROADCAST`、`TABLE`、`RULES`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

