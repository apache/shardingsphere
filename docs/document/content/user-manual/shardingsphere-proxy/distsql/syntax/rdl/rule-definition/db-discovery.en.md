+++
title = "DB Discovery"
weight = 4
+++

## Syntax

```sql
CREATE DB_DISCOVERY RULE ruleDefinition [, ruleDefinition] ...

ALTER DB_DISCOVERY RULE ruleDefinition [, ruleDefinition] ...

DROP DB_DISCOVERY RULE ruleName [, ruleName] ...

DROP DB_DISCOVERY TYPE discoveryTypeName [, discoveryTypeName] ...

DROP DB_DISCOVERY HEARTBEAT discoveryHeartbeatName [, discoveryHeartbeatName] ...

ruleDefinition:
    ruleName (storageUnits, typeDefinition, heartbeatDefinition)

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

### Parameters Explained

| name                   | DateType   | Description                                 |
|:-----------------------|:-----------|:--------------------------------------------|
| discoveryTypeName      | IDENTIFIER | Database discovery type name                |
| ruleName               | IDENTIFIER | Rule name                                   |
| discoveryHeartbeatName | IDENTIFIER | Detect heartbeat name                       |
| typeName               | STRING     | Database discovery type, such as: MySQL.MGR |
| storageUnitName        | IDENTIFIER | Storage unit name                           |

### Notes

- `discoveryType` specifies the database discovery service type, `ShardingSphere` has built-in support for `MySQL.MGR`
- Duplicate `ruleName` will not be created
- The `discoveryType` and `discoveryHeartbeat` being used cannot be deleted
- Names with `-` need to use `" "` when changing
- When removing the `discoveryRule`, the `discoveryType` and `discoveryHeartbeat` used by the `discoveryRule` will not be removed

## Example

### When creating a `discoveryRule`, create both `discoveryType` and `discoveryHeartbeat`

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
