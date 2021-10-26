+++
title = "Sharding"
weight = 2
+++

## Definition

### Sharding Table Rule

```sql
CREATE SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

CREATE DEFAULT SHARDING (DATABASE | TABLE) STRATEGY shardingStrategy

ALTER SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

DROP SHARDING TABLE RULE tableName [, tableName] ...

DROP SHARDING ALGORITHM algorithmName [, algorithmName] ...

shardingTableRuleDefinition:
    tableName(resources [, shardingColumn] [, algorithmDefinition] [, keyGenerateStrategy])

resources:
    RESOURCES(resourceName [, resourceName] ...))

shardingColumn:
    SHARDING_COLUMN=columnName

algorithmDefinition:
    TYPE(NAME=shardingAlgorithmType [, PROPERTIES([algorithmProperties] )] )

keyGenerateStrategy:
    GENERATED_KEY(COLUMN=columnName, strategyDefinition)

shardingStrategy:
    (TYPE=strategyType, shardingColumn, shardingAlgorithm )

shardingAlgorithm
    : SHARDING_ALGORITHM=shardingAlgorithmName

strategyDefinition:
    TYPE(NAME=keyGenerateStrategyType [, PROPERTIES([algorithmProperties] )] )

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value                          
```
- `RESOURCES` needs to use data source resources managed by RDL
- `shardingAlgorithmType` specifies the type of automatic sharding algorithm, please refer to [Auto Sharding Algorithm](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding/)
- `keyGenerateStrategyType` specifies the distributed primary key generation strategy, please refer to [Key Generate Algorithm](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen/)
- Duplicate `tableName` will not be created
- `shardingAlgorithm` can be reused by different `Sharding Table Rule`, so when executing `DROP SHARDING TABLE RULE`, the corresponding `shardingAlgorithm` will not be removed
- To remove `shardingAlgorithm`, please execute `DROP SHARDING ALGORITHM`
- `strategyType` specifies the sharding strategy，please refer to[Sharding Strategy](https://shardingsphere.apache.org/document/current/en/features/sharding/concept/sharding/#sharding-strategy)

### Sharding Binding Table Rule

```sql
CREATE SHARDING BINDING TABLE RULES(bindTableRulesDefinition [, bindTableRulesDefinition] ...)

ALTER SHARDING BINDING TABLE RULES(bindTableRulesDefinition [, bindTableRulesDefinition] ...)

DROP SHARDING BINDING TABLE RULES

bindTableRulesDefinition:
    (tableName [, tableName] ... )
```
- `ALTER` will overwrite the binding table configuration in the database with the new configuration

### Sharding Broadcast Table Rule

```sql
CREATE SHARDING BROADCAST TABLE RULES (tableName [, tableName] ... )

ALTER SHARDING BROADCAST TABLE RULES (tableName [, tableName] ... )

DROP SHARDING BROADCAST TABLE RULES
```
- `ALTER` will overwrite the broadcast table configuration in the database with the new configuration

## Example

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

DROP SHARDING ALGORITHM t_order_hash_mod;
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
