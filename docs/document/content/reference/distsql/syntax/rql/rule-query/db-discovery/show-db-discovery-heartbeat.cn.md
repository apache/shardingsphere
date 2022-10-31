+++
title = "SHOW DB_DISCOVERY HEARTBEATS"
weight = 4
+++

### 描述

`SHOW DB_DISCOVERY HEARTBEATS` 语法用于查询指定逻辑库中的数据库发现心跳。

### 语法

```
ShowDatabaseDiscoveryHeartbeats::=
  'SHOW' 'DB_DISCOVERY' 'HEARTBEATS' ('FROM' databaseName)?

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 返回值说明

| 列                       | 说明              |
| ------------------------ | ---------------- |
| name                     | 数据库发现心跳名称  |
| props                    | 数据库发现心跳参数  |


### 示例

- 查询指定逻辑库中的数据库发现心跳

```sql
SHOW DB_DISCOVERY HEARTBEATS FROM test1;
```

```sql
mysql> SHOW DB_DISCOVERY HEARTBEATS FROM test1;
+-------------------+---------------------------------+
| name              | props                           |
+-------------------+---------------------------------+
| group_0_heartbeat | {keep-alive-cron=0/5 * * * * ?} |
+-------------------+---------------------------------+
1 row in set (0.00 sec)
```

- 查询当前逻辑库中的数据库发现心跳

```sql
SHOW DB_DISCOVERY HEARTBEATS;
```

```sql
mysql> SHOW DB_DISCOVERY HEARTBEATS;
+-------------------+---------------------------------+
| name              | props                           |
+-------------------+---------------------------------+
| group_0_heartbeat | {keep-alive-cron=0/5 * * * * ?} |
+-------------------+---------------------------------+
1 row in set (0.00 sec)
```

### 保留字

`SHOW`、`DB_DISCOVERY`、`HEARTBEATS`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)

