+++
title = "数据库发现"
weight = 5
+++

## 定义

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
- `discoveryType` 指定数据库发现服务类型，`ShardingSphere` 内置支持 `MGR`
- 重复的 `ruleName` 将无法被创建

## 示例

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
