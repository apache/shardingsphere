+++
title = "数据分片"
weight = 2
+++

## 定义

### Sharding Table Rule

```sql
CREATE SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

ALTER SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

DROP SHARDING TABLE RULE tableName [, tableName] ...

shardingTableRuleDefinition:
    tableName(resources [, shardingColumn] [, shardingAlgorithm] [, keyGenerateStrategy])

resources:
    RESOURCES(resourceName [, resourceName] ...))

shardingColumn:
    SHARDING_COLUMN=columnName

shardingAlgorithm:
    TYPE(NAME=shardingAlgorithmType [, PROPERTIES([algorithmProperties] )] )

keyGenerateStrategy:
    GENERATED_KEY(COLUMN=columnName,strategyDefinition)

strategyDefinition:
    TYPE(NAME=keyGenerateStrategyType [, PROPERTIES([algorithmProperties] )] )

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value                          
```
- `RESOURCES` 需使用 RDL 管理的数据源资源
- `shardingAlgorithmType` 指定自动分片算法类型，请参考  [自动分片算法](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding/)
- `keyGenerateStrategyType` 指定分布式主键生成策，请参考 [分布式主键](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen/)
- 重复的 `tableName` 将无法被创建

### Sharding Binding Table Rule

```sql
CREATE SHARDING BINDING TABLE RULES(bindTableRulesDefinition [, bindTableRulesDefinition] ...)

ALTER SHARDING BINDING TABLE RULES(bindTableRulesDefinition [, bindTableRulesDefinition] ...)

DROP SHARDING BINDING TABLE RULES

bindTableRulesDefinition:
    (tableName [, tableName] ... )
```
- `ALTER` 会使用新的配置直接覆盖数据库内的绑定表配置

### Sharding Broadcast Table Rule

```sql
CREATE SHARDING BROADCAST TABLE RULES (tableName [, tableName] ... )

ALTER SHARDING BROADCAST TABLE RULES (tableName [, tableName] ... )

DROP SHARDING BROADCAST TABLE RULES
```
- `ALTER` 会使用新的配置直接覆盖数据库内的广播表配置

## 示例

### Sharding Table Rule

```sql
CREATE SHARDING TABLE RULE t_order (
RESOURCES(resource_0,resource_1),
SHARDING_COLUMN=order_id,
TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=4)),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);

ALTER SHARDING TABLE RULE t_order (
RESOURCES(resource_0,resource_1),
SHARDING_COLUMN=order_id,
TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=10)),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);

DROP SHARDING TABLE RULE t_order, t_order_item;
```

### Sharding Binding Table Rule

```sql
CREATE SHARDING BINDING TABLE RULES (
(t_order,t_order_item),
(t_1,t_2)
);

ALTER SHARDING BINDING TABLE RULES (
(t_order,t_order_item)
);

DROP SHARDING BINDING TABLE RULES;
```

### Sharding Broadcast Table Rule

```sql
CREATE SHARDING BROADCAST TABLE RULES (t_b,t_a);

ALTER SHARDING BROADCAST TABLE RULES (t_b,t_a,t_3);

DROP SHARDING BROADCAST TABLE RULES;
```
