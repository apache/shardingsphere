+++
title = "数据分片"
weight = 1
+++

## 语法说明

### Sharding Table Rule

```sql
CREATE SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

CREATE DEFAULT SHARDING shardingScope STRATEGY (shardingStrategy)

ALTER SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

DROP SHARDING TABLE RULE tableName [, tableName] ...

CREATE SHARDING ALGORITHM shardingAlgorithmDefinition [,  shardingAlgorithmDefinition] ...

ALTER SHARDING ALGORITHM shardingAlgorithmDefinition [,  shardingAlgorithmDefinition] ...

DROP SHARDING ALGORITHM algorithmName [, algorithmName] ...

shardingTableRuleDefinition:
    shardingAutoTableRule | shardingTableRule
   
shardingAutoTableRule:
    tableName(resources COMMA shardingColumn COMMA algorithmDefinition (COMMA keyGenerateStrategy)?)
    
shardingTableRule:
    tableName(dataNodes (COMMA  databaseStrategy)? (COMMA tableStrategy)? (COMMA keyGenerateStrategy)?)

resources:
    RESOURCES(resource [, resource] ...)

dataNodes:
    DATANODES(dataNode [, dataNode] ...)

resource:
    resourceName | inlineExpression

dataNode:
    resourceName | inlineExpression

shardingColumn:
    SHARDING_COLUMN=columnName

algorithmDefinition:
    TYPE(NAME=shardingAlgorithmType [, PROPERTIES([algorithmProperties])])

keyGenerateStrategy:
    GENERATED_KEY(COLUMN=columnName, strategyDefinition)

shardingScope:
    DATABASE | TABLE

databaseStrategy:
    DATABASE_STRATEGY(shardingStrategy)

tableStrategy:
    TABLE_STRATEGY(shardingStrategy)

shardingStrategy:
    TYPE=strategyType, shardingColumn, shardingAlgorithm

shardingColumn:
    SHARDING_COLUMN=columnName
    
shardingAlgorithm:
    SHARDING_ALGORITHM=shardingAlgorithmName

strategyDefinition:
    TYPE(NAME=keyGenerateStrategyType [, PROPERTIES([algorithmProperties])])

shardingAlgorithmDefinition:
    shardingAlgorithmName(algorithmDefinition)

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value                          
```
- `RESOURCES` 需使用 RDL 管理的数据源资源
- `shardingAlgorithmType` 指定自动分片算法类型，请参考  [自动分片算法](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/sharding/)
- `keyGenerateStrategyType` 指定分布式主键生成策略，请参考 [分布式主键](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/keygen/)
- 重复的 `tableName` 将无法被创建
- `shardingAlgorithm` 能够被不同的 `Sharding Table Rule` 复用，因此在执行 `DROP SHARDING TABLE RULE` 时，对应的 `shardingAlgorithm` 不会被移除
- 如需移除 `shardingAlgorithm`，请执行 `DROP SHARDING ALGORITHM`
- `strategyType` 指定分片策略，请参考[分片策略](/cn/features/sharding/concept/sharding/#%E5%88%86%E7%89%87%E7%AD%96%E7%95%A5)
- `Sharding Table Rule` 同时支持 `Auto Table` 和 `Table` 两种类型，两者在语法上有所差异，对应配置文件请参考 [数据分片](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/) 

### Sharding Binding Table Rule

```sql
CREATE SHARDING BINDING TABLE RULES bindTableRulesDefinition [, bindTableRulesDefinition] ...

ALTER SHARDING BINDING TABLE RULES bindTableRulesDefinition [, bindTableRulesDefinition] ...

DROP SHARDING BINDING TABLE RULES bindTableRulesDefinition [, bindTableRulesDefinition] ...

bindTableRulesDefinition:
    (tableName [, tableName] ... )
```
- `ALTER` 会使用新的配置直接覆盖数据库内的绑定表配置

### Sharding Broadcast Table Rule

```sql
CREATE SHARDING BROADCAST TABLE RULES (tableName [, tableName] ...)

ALTER SHARDING BROADCAST TABLE RULES (tableName [, tableName] ...)

DROP SHARDING BROADCAST TABLE RULES (tableName [, tableName] ...)
```
- `ALTER` 会使用新的配置直接覆盖数据库内的广播表配置

## 示例

### Sharding Table Rule

*Auto Table*
```sql
CREATE SHARDING TABLE RULE t_order (
RESOURCES(resource_0,resource_1),
SHARDING_COLUMN=order_id,TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=4)),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);

ALTER SHARDING TABLE RULE t_order (
RESOURCES(resource_0,resource_1,resource_2,resource_3),
SHARDING_COLUMN=order_id,TYPE(NAME=hash_mod,PROPERTIES("sharding-count"=16)),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);

DROP SHARDING TABLE RULE t_order;

DROP SHARDING ALGORITHM t_order_hash_mod;
```

*Table*

```sql
CREATE SHARDING TABLE RULE t_order_item (
DATANODES("resource_${0..1}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE=standard,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE=standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=table_inline),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES("worker-id"=123)))
);

ALTER SHARDING TABLE RULE t_order_item (
DATANODES("resource_${0..3}.t_order_item${0..3}"),
DATABASE_STRATEGY(TYPE=standard,SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE=standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=table_inline),
GENERATED_KEY(COLUMN=another_id,TYPE(NAME=uuid,PROPERTIES("worker-id"=123)))
);

DROP SHARDING TABLE RULE t_order_item;

CREATE DEFAULT SHARDING DATABASE STRATEGY (
TYPE = standard,SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=algorithmsName
);

CREATE SHARDING ALGORITHM database_inline (
TYPE(NAME=inline,PROPERTIES("algorithm-expression"="resource_${user_id % 2}"))
),table_inline (
TYPE(NAME=inline,PROPERTIES("algorithm-expression"="t_order_item_${order_id % 2}"))
);

ALTER SHARDING ALGORITHM database_inline (
TYPE(NAME=inline,PROPERTIES("algorithm-expression"="resource_${user_id % 4}"))
),table_inline (
TYPE(NAME=inline,PROPERTIES("algorithm-expression"="t_order_item_${order_id % 4}"))
);

DROP SHARDING ALGORITHM database_inline;
```

### Sharding Binding Table Rule

```sql
CREATE SHARDING BINDING TABLE RULES (t_order,t_order_item),(t_1,t_2);

ALTER SHARDING BINDING TABLE RULES (t_order,t_order_item);

DROP SHARDING BINDING TABLE RULES;

DROP SHARDING BINDING TABLE RULES (t_order,t_order_item);
```

### Sharding Broadcast Table Rule

```sql
CREATE SHARDING BROADCAST TABLE RULES (t_b,t_a);

ALTER SHARDING BROADCAST TABLE RULES (t_b,t_a,t_3);

DROP SHARDING BROADCAST TABLE RULES;

DROP SHARDING BROADCAST TABLE RULES t_b;
```
