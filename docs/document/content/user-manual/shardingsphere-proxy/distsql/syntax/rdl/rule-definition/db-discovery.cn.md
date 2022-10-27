+++
title = "数据库发现"
weight = 4
+++

## 语法说明

```sql
CREATE DB_DISCOVERY RULE ruleDefinition [, ruleDefinition] ...

ALTER DB_DISCOVERY RULE ruleDefinition [, ruleDefinition] ...

DROP DB_DISCOVERY RULE ruleName [, ruleName] ...

CREATE DB_DISCOVERY TYPE databaseDiscoveryTypeDefinition [, databaseDiscoveryTypeDefinition] ...

ALTER DB_DISCOVERY TYPE databaseDiscoveryTypeDefinition [, databaseDiscoveryTypeDefinition] ...

DROP DB_DISCOVERY TYPE discoveryTypeName [, discoveryTypeName] ...

CREATE DB_DISCOVERY HEARTBEAT databaseDiscoveryHeartbaetDefinition [, databaseDiscoveryHeartbaetDefinition] ...

ALTER DB_DISCOVERY HEARTBEAT databaseDiscoveryHeartbaetDefinition [, databaseDiscoveryHeartbaetDefinition] ...

DROP DB_DISCOVERY HEARTBEAT discoveryHeartbeatName [, discoveryHeartbeatName] ...

ruleDefinition:
    (databaseDiscoveryRuleDefinition | databaseDiscoveryRuleConstruction)

databaseDiscoveryRuleDefinition
    ruleName (storageUnits, typeDefinition, heartbeatDefinition)

databaseDiscoveryRuleConstruction
    ruleName (storageUnits, TYPE = discoveryTypeName, HEARTBEAT = discoveryHeartbeatName)

databaseDiscoveryTypeDefinition
    discoveryTypeName (typeDefinition)

databaseDiscoveryHeartbaetDefinition
    discoveryHeartbeatName (PROPERTIES (properties)) 

storageUnits:
    STORAGE_UNITS(storageUnitName [, storageUnitName] ...)

typeDefinition:
    TYPE(NAME=typeName [, PROPERTIES([properties] )] )

heartbeatDefinition
    HEARTBEAT (PROPERTIES (properties)) 

properties:
    property [, property] ...

property:
    key=value                          
```

### 参数解释
| 名称                    | 数据类型    | 说明                          |
|:-----------------------|:-----------|:-----------------------------|
| discoveryTypeName      | IDENTIFIER | 数据库发现类型名                |
| ruleName               | IDENTIFIER | 规则名称                       |
| discoveryHeartbeatName | IDENTIFIER | 监听心跳名称                    |
| typeName               | STRING     | 数据库发现类型，如：MySQL.MGR    |
| storageUnitName        | IDENTIFIER | 资源名称                       |

### 注意事项

- `discoveryType` 指定数据库发现服务类型，`ShardingSphere` 内置支持 `MySQL.MGR`；
- 重复的 `ruleName` 将无法被创建；
- 正在被使用的 `discoveryType` 和 `discoveryHeartbeat` 无法被删除；
- 带有 `-` 的命名在改动时需要使用 `" "`；
- 移除 `discoveryRule` 时不会移除被该 `discoveryRule` 使用的 `discoveryType` 和 `discoveryHeartbeat`。

## 示例

### 创建 `discoveryRule` 时同时创建 `discoveryType` 和 `discoveryHeartbeat`

```sql
CREATE DB_DISCOVERY RULE db_discovery_group_0 (
STORAGE_UNITS(ds_0, ds_1, ds_2),
TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec')),
HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);

ALTER DB_DISCOVERY RULE db_discovery_group_0 (
STORAGE_UNITS(ds_0, ds_1, ds_2),
TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='246e9612-aaf1')),
HEARTBEAT(PROPERTIES('keep-alive-cron'='0/5 * * * * ?'))
);

DROP DB_DISCOVERY RULE db_discovery_group_0;

DROP DB_DISCOVERY TYPE db_discovery_group_0_mgr;

DROP DB_DISCOVERY HEARTBEAT db_discovery_group_0_heartbeat;

```

### 使用已有的 `discoveryType` 和 `discoveryHeartbeat` 创建 `discoveryRule`

```sql
CREATE DB_DISCOVERY TYPE db_discovery_group_1_mgr(
  TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='92504d5b-6dec'))
);

CREATE DB_DISCOVERY HEARTBEAT db_discovery_group_1_heartbeat(
  PROPERTIES('keep-alive-cron'='0/5 * * * * ?')
);

CREATE DB_DISCOVERY RULE db_discovery_group_1 (
STORAGE_UNITS(ds_0, ds_1, ds_2),
TYPE=db_discovery_group_1_mgr,
HEARTBEAT=db_discovery_group_1_heartbeat
);

ALTER DB_DISCOVERY TYPE db_discovery_group_1_mgr(
  TYPE(NAME='MySQL.MGR',PROPERTIES('group-name'='246e9612-aaf1'))
);

ALTER DB_DISCOVERY HEARTBEAT db_discovery_group_1_heartbeat(
  PROPERTIES('keep-alive-cron'='0/10 * * * * ?')
);

ALTER DB_DISCOVERY RULE db_discovery_group_1 (
STORAGE_UNITS(ds_0, ds_1),
TYPE=db_discovery_group_1_mgr,
HEARTBEAT=db_discovery_group_1_heartbeat
);

DROP DB_DISCOVERY RULE db_discovery_group_1;

DROP DB_DISCOVERY TYPE db_discovery_group_1_mgr;

DROP DB_DISCOVERY HEARTBEAT db_discovery_group_1_heartbeat;
```
