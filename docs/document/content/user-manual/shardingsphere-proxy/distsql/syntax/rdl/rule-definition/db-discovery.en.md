+++
title = "DB Discovery"
weight = 4
+++

## Syntax

```sql
CREATE DB_DISCOVERY RULE databaseDiscoveryRuleDefinition [, databaseDiscoveryRuleDefinition] ...

ALTER DB_DISCOVERY RULE databaseDiscoveryRuleDefinition [, databaseDiscoveryRuleDefinition] ...

DROP DB_DISCOVERY RULE ruleName [, ruleName] ...

databaseDiscoveryRuleDefinition:
    ruleName(resources, discoveryTypeDefinition)

resources:
    RESOURCES(resourceName [, resourceName] ...)

discoveryTypeDefinition:
    TYPE(NAME=discoveryType [, PROPERTIES([algorithmProperties] )] )

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value                          
```
- `discoveryType` specifies the database discovery service type, `ShardingSphere` has built-in support for `MGR`
- Duplicate `ruleName` will not be created

## Example

```sql
CREATE DB_DISCOVERY RULE ha_group_0 (
RESOURCES(resource_0,resource_1),
TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec',keepAliveCron=''))
);

ALTER DB_DISCOVERY RULE ha_group_0 (
RESOURCES(resource_0,resource_1,resource_2),
TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec' ,keepAliveCron=''))
);

DROP DB_DISCOVERY RULE ha_group_0;
```
