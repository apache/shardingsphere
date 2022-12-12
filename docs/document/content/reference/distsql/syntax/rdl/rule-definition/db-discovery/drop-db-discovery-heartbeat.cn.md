+++
title = "DROP DB_DISCOVERY HEARTBEAT"
weight = 5
+++

## 描述

`DROP DB_DISCOVERY HEARTBEAT` 语法用于为指定逻辑库删除数据库发现心跳

### 语法定义

```sql
DropDatabaseDiscoveryHeartbeat ::=
  'DROP' 'DB_DISCOVERY' 'HEARTBEAT'  dbDiscoveryHeartbeatName (',' dbDiscoveryHeartbeatName)*  ('FROM' databaseName)?

dbDiscoveryHeartbeatName ::=
  identifier

databaseName ::=
  identifier
```

### 补充说明

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`。

### 示例

- 为指定数据库删除多个数据库发现心跳
 
```sql
DROP DB_DISCOVERY HEARTBEAT group_0_heartbeat, group_1_heartbeat FROM test1;
```

- 为当前数据库删除单个数据库发现心跳

```sql
DROP DB_DISCOVERY HEARTBEAT group_0_heartbeat;
```

### 保留字

`DROP`、`DB_DISCOVERY`、`HEARTBEAT`、`FROM`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)