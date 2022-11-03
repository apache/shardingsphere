+++
title = "CREATE DB_DISCOVERY RULE"
weight = 2
+++

## 描述

`CREATE DB_DISCOVERY RULE` 语法用于创建数据库发现规则

### 语法定义

```sql
CreateDatabaseDiscoveryRule ::=
  'CREATE' 'DB_DISCOVERY' 'RULE' databaseDiscoveryDefinition ( ',' databaseDiscoveryDefinition)*

databaseDiscoveryDefinition ::=
    ruleName '(' 'STORAGE_UNITS' '(' storageUnitName ( ',' storageUnitName )* ')' ',' 'TYPE' '(' 'NAME' '=' typeName ( ',' 'PROPERTIES' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* )? ',' 'HEARTBEAT' '(' 'key' '=' 'value' ( ',' 'key' '=' 'value' )* ')' ')' 
        
ruleName ::=
  identifier

storageUnitName ::=
  identifier

typeName ::=
  identifier

discoveryHeartbeatName ::=
  identifier
```

### 补充说明

- `discoveryType` 指定数据库发现服务类型，`ShardingSphere` 内置支持 `MySQL.MGR`；
- 重复的 `ruleName` 将无法被创建。

### 示例

- 创建数据库发现规则

```sql
CREATE DB_DISCOVERY RULE db_discovery_group_0 (
    STORAGE_UNITS(su_0, su_1, su_2),
    TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
    HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);
```

### 保留字

`CREATE`、`DB_DISCOVERY`、`RULE`、`STORAGE_UNITS`、`TYPE`、`NAME`、`PROPERTIES`、`HEARTBEAT`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)