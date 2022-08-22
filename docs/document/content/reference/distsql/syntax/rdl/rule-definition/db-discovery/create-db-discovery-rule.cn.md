+++
title = "CREATE DB_DISCOVERY RULE"
weight = 2
+++

## 描述

`CREATE DB_DISCOVERY RULE` 语法用于创建数据库发现规则

### 语法定义

```sql
CreateDatabaseDiscoveryRule ::=
  'CREATE' 'DB_DISCOVERY' 'RULE' ( databaseDiscoveryDefinition | databaseDiscoveryConstruction ) ( ',' ( databaseDiscoveryDefinition | databaseDiscoveryConstruction ) )*

databaseDiscoveryDefinition ::=
    ruleName '(' 'RESOURCES' '(' resourceName ( ',' resourceName )* ')' ',' 'TYPE' '(' 'NAME' '=' typeName ( ',' 'PROPERTIES' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* )? ',' 'HEARTBEAT' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' ')' 
    
databaseDiscoveryConstruction ::=
    ruleName '(' 'RESOURCES' '(' resourceName ( ',' resourceName )* ')' ',' 'TYPE' '=' discoveryTypeName ',' 'HEARTBEAT' '=' discoveryHeartbeatName ')'
    
ruleName ::=
  identifier

resourceName ::=
  identifier

typeName ::=
  identifier

discoveryHeartbeatName ::=
  identifier
```

### 补充说明

- `discoveryType` 指定数据库发现服务类型，`ShardingSphere` 内置支持 `MySQL.MGR`；
- 重复的 `ruleName` 将无法被创建；
- 正在被使用的 `discoveryType` 和 `discoveryHeartbeat` 无法被删除；
- 带有 `-` 的命名在改动时需要使用 `" "`；
- 移除 `discoveryRule` 时不会移除被该 `discoveryRule` 使用的 `discoveryType` 和 `discoveryHeartbeat`。

### 示例

#### 创建 `discoveryRule` 时同时创建 `discoveryType` 和 `discoveryHeartbeat`

```sql
CREATE DB_DISCOVERY RULE db_discovery_group_0 (
    RESOURCES(ds_0, ds_1, ds_2),
    TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
    HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

#### 使用已有的 `discoveryType` 和 `discoveryHeartbeat` 创建 `discoveryRule`

```sql
CREATE DB_DISCOVERY RULE db_discovery_group_1 (
    RESOURCES(ds_0, ds_1, ds_2),
    TYPE=db_discovery_group_1_mgr,
    HEARTBEAT=db_discovery_group_1_heartbeat
);
```

### 保留字

`CREATE`、`DB_DISCOVERY`、`RULE`、`RESOURCES`、`TYPE`、`NAME`、`PROPERTIES`、`HEARTBEAT`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)