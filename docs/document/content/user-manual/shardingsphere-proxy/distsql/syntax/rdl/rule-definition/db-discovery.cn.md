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
    ruleName (resources, typeDefinition, heartbeatDefinition)

databaseDiscoveryRuleConstruction
    ruleName (resources, TYPE = discoveryTypeName, HEARTBEAT = discoveryHeartbeatName)

databaseDiscoveryTypeDefinition
    discoveryTypeName (typeDefinition)

databaseDiscoveryHeartbaetDefinition
    discoveryHeartbeatName (PROPERTIES (properties)) 

resources:
    RESOURCES(resourceName [, resourceName] ...)

typeDefinition:
    TYPE(NAME=typeName [, PROPERTIES([properties] )] )

heartbeatDefinition
    HEARTBEAT (PROPERTIES (properties)) 

properties:
    property [, property] ...

property:
    key=value                          
```

- `discoveryType` 指定数据库发现服务类型，`ShardingSphere` 内置支持 `MGR`
- 重复的 `ruleName` 将无法被创建
- 正在被使用的 `discoveryType` 和 `discoveryHeartbeat` 无法被删除
- 带有 `-` 的命名在改动时需要使用 `" "`
- 移除 `discoveryRule` 时不会移除被该 `discoveryRule` 使用的 `discoveryType` 和 `discoveryHeartbeat`

## 示例

### 创建 `discoveryRule` 时同时创建 `discoveryType` 和 `discoveryHeartbeat`

```sql
CREATE DB_DISCOVERY RULE ha_group_0 (
RESOURCES(ds_0, ds_1, ds_2),
TYPE(NAME=mgr,PROPERTIES('groupName'='92504d5b-6dec')),
HEARTBEAT(PROPERTIES('keepAliveCron'='0/5 * * * * ?'))
);

ALTER DB_DISCOVERY RULE ha_group_0 (
RESOURCES(ds_0, ds_1, ds_2),
TYPE(NAME=mgr,PROPERTIES('groupName'='246e9612-aaf1')),
HEARTBEAT(PROPERTIES('keepAliveCron'='0/5 * * * * ?'))
);

DROP DB_DISCOVERY RULE ha_group_0;

DROP DB_DISCOVERY TYPE ha_group_0_mgr;

DROP DB_DISCOVERY HEARTBEAT ha_group_0_heartbeat;

```

### 使用已有的 `discoveryType` 和 `discoveryHeartbeat` 创建 `discoveryRule`

```sql
CREATE DB_DISCOVERY TYPE ha_group_1_mgr(
  TYPE(NAME=mgr,PROPERTIES('groupName'='92504d5b-6dec'))
);

CREATE DB_DISCOVERY HEARTBEAT ha_group_1_heartbeat(
  PROPERTIES('keepAliveCron'='0/5 * * * * ?')
);

CREATE DB_DISCOVERY RULE ha_group_1 (
RESOURCES(ds_0, ds_1, ds_2),
TYPE=ha_group_1_mgr,
HEARTBEAT=ha_group_1_heartbeat
);

ALTER DB_DISCOVERY TYPE ha_group_1_mgr(
  TYPE(NAME=mgr,PROPERTIES('groupName'='246e9612-aaf1'))
);

ALTER DB_DISCOVERY HEARTBEAT ha_group_1_heartbeat(
  PROPERTIES('keepAliveCron'='0/10 * * * * ?')
);

ALTER DB_DISCOVERY RULE ha_group_1 (
RESOURCES(ds_0, ds_1),
TYPE=ha_group_1_mgr,
HEARTBEAT=ha_group_1_heartbeat
);

DROP DB_DISCOVERY RULE ha_group_1;

DROP DB_DISCOVERY TYPE ha_group_1_mgr;

DROP DB_DISCOVERY HEARTBEAT ha_group_1_heartbeat;
```