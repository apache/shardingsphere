+++
title = "数据分片"
weight = 1
+++

## 语法说明

### Sharding Table Rule

```sql
CREATE SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

ALTER SHARDING TABLE RULE shardingTableRuleDefinition [, shardingTableRuleDefinition] ...

DROP SHARDING TABLE RULE tableName [, tableName] ...

CREATE DEFAULT SHARDING shardingScope STRATEGY (shardingStrategy)

ALTER DEFAULT SHARDING shardingScope STRATEGY (shardingStrategy)

DROP DEFAULT SHARDING shardingScope STRATEGY;

CREATE SHARDING ALGORITHM shardingAlgorithmDefinition [, shardingAlgorithmDefinition] ...

ALTER SHARDING ALGORITHM shardingAlgorithmDefinition [, shardingAlgorithmDefinition] ...

DROP SHARDING ALGORITHM algorithmName [, algorithmName] ...

CREATE SHARDING KEY GENERATOR keyGeneratorDefinition [, keyGeneratorDefinition] ...

ALTER SHARDING KEY GENERATOR keyGeneratorDefinition [, keyGeneratorDefinition] ...

DROP SHARDING KEY GENERATOR keyGeneratorName [, keyGeneratorName] ...

shardingTableRuleDefinition:
    shardingAutoTableRule | shardingTableRule

shardingAutoTableRule:
    tableName(resources, shardingColumn, algorithmDefinition [, keyGenerateDeclaration])

shardingTableRule:
    tableName(dataNodes [, databaseStrategy] [, tableStrategy] [, keyGenerateDeclaration])

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

keyGenerateDeclaration:
    keyGenerateDefinition | keyGenerateConstruction

keyGenerateDefinition:
    KEY_GENERATE_STRATEGY(COLUMN=columnName, strategyDefinition)

shardingScope:
    DATABASE | TABLE

databaseStrategy:
    DATABASE_STRATEGY(shardingStrategy)

tableStrategy:
    TABLE_STRATEGY(shardingStrategy)

keyGenerateConstruction
    KEY_GENERATE_STRATEGY(COLUMN=columnName, KEY_GENERATOR=keyGenerateAlgorithmName)

shardingStrategy:
    TYPE=strategyType, shardingColumn, shardingAlgorithm

shardingAlgorithm:
    existingAlgorithm | autoCreativeAlgorithm

existingAlgorithm:
    SHARDING_ALGORITHM=shardingAlgorithmName

autoCreativeAlgorithm:
    SHARDING_ALGORITHM(algorithmDefinition)

strategyDefinition:
    TYPE(NAME=keyGenerateStrategyType [, PROPERTIES([algorithmProperties])])

shardingAlgorithmDefinition:
    shardingAlgorithmName(algorithmDefinition)

algorithmProperties:
    algorithmProperty [, algorithmProperty] ...

algorithmProperty:
    key=value   

keyGeneratorDefinition: 
    keyGeneratorName (algorithmDefinition)
```
- `RESOURCES` 需使用 RDL 管理的数据源资源；
- `shardingAlgorithmType` 指定自动分片算法类型，请参考  [自动分片算法](/cn/user-manual/common-config/builtin-algorithm/sharding/)；
- `keyGenerateStrategyType` 指定分布式主键生成策略，请参考 [分布式主键](/cn/user-manual/common-config/builtin-algorithm/keygen/)；
- 重复的 `tableName` 将无法被创建；
- `shardingAlgorithm` 能够被不同的 `Sharding Table Rule` 复用，因此在执行 `DROP SHARDING TABLE RULE` 时，对应的 `shardingAlgorithm` 不会被移除；
- 如需移除 `shardingAlgorithm`，请执行 `DROP SHARDING ALGORITHM`；
- `strategyType` 指定分片策略，请参考[分片策略](/cn/features/sharding/concept/sharding/#%E5%88%86%E7%89%87%E7%AD%96%E7%95%A5)；
- `Sharding Table Rule` 同时支持 `Auto Table` 和 `Table` 两种类型，两者在语法上有所差异，对应配置文件请参考 [数据分片](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/) ；
- 使用 `autoCreativeAlgorithm` 方式指定 `shardingStrategy` 时，将会自动创建新的分片算法，算法命名规则为 `tableName_strategyType_shardingAlgorithmType`，如 `t_order_database_inline`。

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

### Sharding Scaling Rule

```sql
CREATE SHARDING SCALING RULE scalingName [scalingRuleDefinition]

DROP SHARDING SCALING RULE scalingName

ENABLE SHARDING SCALING RULE scalingName

DISABLE SHARDING SCALING RULE scalingName

scalingRuleDefinition:
    [inputDefinition] [, outputDefinition] [, streamChannel] [, completionDetector] [, dataConsistencyChecker]

inputDefinition:
    INPUT ([workerThread] [, batchSize] [, rateLimiter])

outputDefinition:
    OUTPUT ([workerThread] [, batchSize] [, rateLimiter])

completionDetector:
    COMPLETION_DETECTOR (algorithmDefinition)

dataConsistencyChecker:
    DATA_CONSISTENCY_CHECKER (algorithmDefinition)

rateLimiter:
    RATE_LIMITER (algorithmDefinition)

streamChannel:
    STREAM_CHANNEL (algorithmDefinition)

workerThread:
    WORKER_THREAD=intValue

batchSize:
    BATCH_SIZE=intValue

intValue:
    INT
```
- `ENABLE` 用于设置启用哪个弹性伸缩配置；
- `DISABLE` 将禁用当前正在使用的配置；
- 创建逻辑库中第一个弹性伸缩配置时，默认启用。

## 示例

### Sharding Table Rule

*Key Generator*

```sql
CREATE SHARDING KEY GENERATOR snowflake_key_generator (
TYPE(NAME=SNOWFLAKE)
);

ALTER SHARDING KEY GENERATOR snowflake_key_generator (
TYPE(NAME=SNOWFLAKE)
);

DROP SHARDING KEY GENERATOR snowflake_key_generator;
```

*Auto Table*
```sql
CREATE SHARDING TABLE RULE t_order (
RESOURCES(resource_0,resource_1),
SHARDING_COLUMN=order_id,TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="4")),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake"))
);

ALTER SHARDING TABLE RULE t_order (
RESOURCES(resource_0,resource_1,resource_2,resource_3),
SHARDING_COLUMN=order_id,TYPE(NAME="hash_mod",PROPERTIES("sharding-count"="16")),
KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME="snowflake"))
);

DROP SHARDING TABLE RULE t_order;

DROP SHARDING ALGORITHM t_order_hash_mod;
```

*Table*

```sql
CREATE SHARDING ALGORITHM table_inline (
TYPE(NAME="inline",PROPERTIES("algorithm-expression"="t_order_item_${order_id % 2}"))
);

CREATE SHARDING TABLE RULE t_order_item (
DATANODES("resource_${0..1}.t_order_item_${0..1}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME="inline",PROPERTIES("algorithm-expression"="resource_${user_id % 2}")))),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=table_inline),
KEY_GENERATE_STRATEGY(COLUMN=another_id,KEY_GENERATOR=snowflake_key_generator)
);

ALTER SHARDING ALGORITHM database_inline (
TYPE(NAME="inline",PROPERTIES("algorithm-expression"="resource_${user_id % 4}"))
),table_inline (
TYPE(NAME="inline",PROPERTIES("algorithm-expression"="t_order_item_${order_id % 4}"))
);

ALTER SHARDING TABLE RULE t_order_item (
DATANODES("resource_${0..3}.t_order_item${0..3}"),
DATABASE_STRATEGY(TYPE="standard",SHARDING_COLUMN=user_id,SHARDING_ALGORITHM=database_inline),
TABLE_STRATEGY(TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=table_inline),
KEY_GENERATE_STRATEGY(COLUMN=another_id,KEY_GENERATOR=snowflake_key_generator)
);

DROP SHARDING TABLE RULE t_order_item;

DROP SHARDING ALGORITHM database_inline;

CREATE DEFAULT SHARDING DATABASE STRATEGY (
TYPE="standard",SHARDING_COLUMN=order_id,SHARDING_ALGORITHM=database_inline
);

ALTER DEFAULT SHARDING DATABASE STRATEGY (
TYPE="standard",SHARDING_COLUMN=another_id,SHARDING_ALGORITHM=database_inline
);

DROP DEFAULT SHARDING DATABASE STRATEGY;
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

### Sharding Scaling Rule

```sql
CREATE SHARDING SCALING RULE sharding_scaling(
INPUT(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  RATE_LIMITER(TYPE(NAME=QPS, PROPERTIES("qps"=50)))
),
OUTPUT(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  RATE_LIMITER(TYPE(NAME=TPS, PROPERTIES("tps"=2000)))
),
STREAM_CHANNEL(TYPE(NAME="MEMORY", PROPERTIES("block-queue-size"="10000"))),
COMPLETION_DETECTOR(TYPE(NAME="IDLE", PROPERTIES("incremental-task-idle-seconds-threshold"="1800"))),
DATA_CONSISTENCY_CHECKER(TYPE(NAME="DATA_MATCH", PROPERTIES("chunk-size"="1000")))
);

ENABLE SHARDING SCALING RULE sharding_scaling;

DISABLE SHARDING SCALING RULE sharding_scaling;

DROP SHARDING SCALING RULE sharding_scaling;
```
