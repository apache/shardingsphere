+++
title = "CREATE DB_DISCOVERY HEARTBEAT"
weight = 4
+++

## 描述

`CREATE DB_DISCOVERY HEARTBEAT` 语法用于创建数据库发现心跳包规则

### 语法定义

```sql
CreateDatabaseDiscoveryHeartbeat ::=
  'CREATE' 'DB_DISCOVERY' 'HEARTBEAT' databaseDiscoveryHeartbaetDefinition ( ',' databaseDiscoveryHeartbaetDefinition )*

databaseDiscoveryHeartbaetDefinition ::=
    discoveryHeartbeatName '(' 'PROPERTIES' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' ')'
    
discoveryHeartbeatName ::=
  identifier
```

### 补充说明

- 带有 `-` 的命名在改动时需要使用 `" "`。

### 示例

#### 创建 `HEARTBEAT`

```sql
CREATE DB_DISCOVERY HEARTBEAT db_discovery_group_1_heartbeat(
  PROPERTIES('keep-alive-cron'='0/5 * * * * ?')
);
```

### 保留字

`CREATE`、`DB_DISCOVERY`、`HEARTBEAT`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)