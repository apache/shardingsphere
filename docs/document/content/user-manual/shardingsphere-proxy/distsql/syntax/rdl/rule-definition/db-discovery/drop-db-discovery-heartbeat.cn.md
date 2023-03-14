+++
title = "DROP DB_DISCOVERY HEARTBEAT"
weight = 5
+++

## 描述

`DROP DB_DISCOVERY HEARTBEAT` 语法用于为指定逻辑库删除数据库发现心跳

### 语法定义

{{< tabs >}}
{{% tab name="语法" %}}
```sql
DropDatabaseDiscoveryHeartbeat ::=
  'DROP' 'DB_DISCOVERY' 'HEARTBEAT' ifExists? dbDiscoveryHeartbeatName (',' dbDiscoveryHeartbeatName)*  ('FROM' databaseName)?

ifExists ::=
  'IF' 'EXISTS'

dbDiscoveryHeartbeatName ::=
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

- 未指定 `databaseName` 时，默认是当前使用的 `DATABASE`。 如果也未使用 `DATABASE` 则会提示 `No database selected`；
- `ifExists` 子句用于避免 `Database discovery heartbeat not exists` 错误。

### 示例

- 为指定数据库删除多个数据库发现心跳
 
```sql
DROP DB_DISCOVERY HEARTBEAT group_0_heartbeat, group_1_heartbeat FROM discovery_db;
```

- 为当前数据库删除单个数据库发现心跳

```sql
DROP DB_DISCOVERY HEARTBEAT group_0_heartbeat;
```

- 使用 `ifExists` 子句删除数据库发现心跳

```sql
DROP DB_DISCOVERY HEARTBEAT IF EXISTS group_0_heartbeat;
```

### 保留字

`DROP`、`DB_DISCOVERY`、`HEARTBEAT`、`FROM`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)