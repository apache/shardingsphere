+++
title = "Sharding"
weight = 1
+++

## Root Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

Attributes:

| *Name*                              | *DataType*                                          | *Description*                                  | *Default Value* |
| ----------------------------------- | --------------------------------------------------- | ---------------------------------------------- | --------------- |
| tables (+)                          | Collection\<ShardingTableRuleConfiguration\>        | Sharding table rules                           | -               |
| autoTables (+)                      | Collection\<ShardingAutoTableRuleConfiguration\>    | Sharding automatic table rules                 | -               |
| bindingTableGroups (*)              | Collection\<String\>                                | Binding table rules                            | Empty           |
| broadcastTables (*)                 | Collection\<String\>                                | Broadcast table rules                          | Empty           |
| defaultDatabaseShardingStrategy (?) | ShardingStrategyConfiguration                       | Default database sharding strategy             | Not sharding    |
| defaultTableShardingStrategy (?)    | ShardingStrategyConfiguration                       | Default table sharding strategy                | Not sharding    |
| defaultKeyGenerateStrategy (?)      | KeyGeneratorConfiguration                           | Default key generator                          | Snowflake       |
| shardingAlgorithms (+)              | Map\<String, ShardingSphereAlgorithmConfiguration\> | Sharding algorithm name and configurations     | None            |
| keyGenerators (?)                   | Map\<String, ShardingSphereAlgorithmConfiguration\> | Key generate algorithm name and configurations | None            |

## Sharding Table Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

Attributes:

| *Name*                       | *DataType*                    | *Description*                                                                                                                         | *Default Value*                            |
| ---------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| logicTable                   | String                        | Name of sharding logic table                                                                                                          | -                                          |
| actualDataNodes (?)          | String                        | Describe data source names and actual tables, delimiter as point.<br /> Multiple data nodes split by comma, support inline expression | Broadcast table or databases sharding only |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | Databases sharding strategy                                                                                                           | Use default databases sharding strategy    |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | Tables sharding strategy                                                                                                              | Use default tables sharding strategy       |
| keyGenerateStrategy (?)      | KeyGeneratorConfiguration     | Key generator configuration                                                                                                           | Use default key generator                  |

## Sharding Automatic Table Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingAutoTableRuleConfiguration

Attributes:

| *Name*                  | *DataType*                    | *Description*                                               | *Default Value*                 |
| ----------------------- | ----------------------------- | ----------------------------------------------------------- | ------------------------------- |
| logicTable              | String                        | Name of sharding logic table                                | -                               |
| actualDataSources (?)   | String                        | Data source names.<br /> Multiple data nodes split by comma | Use all configured data sources |
| shardingStrategy (?)    | ShardingStrategyConfiguration | Sharding strategy                                           | Use default sharding strategy   |
| keyGenerateStrategy (?) | KeyGeneratorConfiguration     | Key generator configuration                                 | Use default key generator       |

## Sharding Strategy Configuration

### Standard Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*           |
| --------------------- | ---------- | ----------------------- |
| shardingColumn        | String     | Sharding column name    |
| shardingAlgorithmName | String     | Sharding algorithm name |

### Complex Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*                             |
| --------------------- | ---------- | ----------------------------------------- |
| shardingColumns       | String     | Sharding column name, separated by commas |
| shardingAlgorithmName | String     | Sharding algorithm name                   |

### Hint Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*           |
| --------------------- | ---------- | ----------------------- |
| shardingAlgorithmName | String     | Sharding algorithm name |

### None Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration

Attributes: None

## Key Generate Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration

Attributes:

| *Name*           | *DataType* | *Description*               |
| ---------------- | ---------- | --------------------------- |
| column           | String     | Column name of key generate |
| keyGeneratorName | String     | key generate algorithm name |

## Sharding Algorithm Configuration

Class name: org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

### Standard Sharding Algorithm Configuration

Apache ShardingSphere built-in standard sharding algorithm are:

#### Inline Sharding Algorithm

Type: INLINE

Attributes:

| *Name*                                    | *DataType* | *Description*                                                                                            | *Default Value* |
| ----------------------------------------- | ---------- | -------------------------------------------------------------------------------------------------------- | --------------- |
| algorithm.expression                      | String     | Inline expression sharding algorithm                                                                     | -               |
| allow.range.query.with.inline.sharding (?)| boolean    | Whether range query is allowed. Note: range query will ignore sharding strategy and conduct full routing | false           |

#### Modulo Sharding Algorithm

Type: MOD

Attributes:

| *Name*         | *DataType* | *Description*  |
| -------------- | ---------- | -------------- |
| sharding.count | int        | Sharding count |

#### Hash Modulo Sharding Algorithm

Type: HASH_MOD

Attributes:

| *Name*         | *DataType* | *Description*  |
| -------------- | ---------- | -------------- |
| sharding.count | int        | Sharding count |

#### Volume Based Range Sharding Algorithm

Type: VOLUME_RANGE

Attributes:

| *Name*          | *DataType* | *Description*                                          |
| --------------- | ---------- | ------------------------------------------------------ |
| range.lower     | long       | Range lower bound, throw exception if lower than bound |
| range.upper     | long       | Range upper bound, throw exception if upper than bound |
| sharding.volume | long       | Sharding volume                                        |

#### Boundary Based Range Sharding Algorithm

Type: BOUNDARY_RANGE

Attributes:

| *Name*          | *DataType* | *Description*                                                     |
| --------------- | ---------- | ----------------------------------------------------------------- |
| sharding.ranges | String     | Range of sharding border, multiple boundaries separated by commas |

#### Fixed Time Range Sharding Algorithm

Type: FIXED_INTERVAL

Attributes:

| *Name*           | *DataType* | *Description*                                               |
| ---------------- | ---------- | ----------------------------------------------------------- |
| datetime.lower   | String     | Shard datetime begin boundary, pattern: yyyy-MM-dd HH:mm:ss |
| datetime.upper   | String     | Shard datetime end boundary, pattern: yyyy-MM-dd HH:mm:ss   |
| sharding.seconds | long       | Max seconds for the data in one shard                       |

#### Mutable Interval Sharding Algorithm

Type: MUTABLE_INTERVAL

Attributes:

| *Name*                       | *DataType* | *Description*                                                                                                        | *Default Value* |
| ---------------------------- | ---------- | -------------------------------------------------------------------------------------------------------------------- | --------------- |
| datetime.pattern             | String     | Timestamp pattern of sharding value, must can be transformed to Java LocalDateTime. For example: yyyy-MM-dd HH:mm:ss | -               |
| datetime.lower               | String     | Datetime sharding lower boundary, pattern is defined `datetime.pattern`                                              | -               |
| datetime.upper (?)           | String     | Datetime sharding upper boundary, pattern is defined `datetime.pattern`                                              | Now             |
| table.suffix.pattern         | String     | Suffix pattern of actual tables, must can be transformed to Java LocalDateTime. For example: yyyyMM                  | -               |
| datetime.interval.amount (?) | int        | Interval of sharding value                                                                                       | 1               |
| datetime.interval.unit (?)   | String     | Unit of sharding value interval, must can be transformed to Java ChronoUnit's Enum value. For example: MONTHS    | DAYS            |

### Complex Sharding Algorithm Configuration

There is no built-in complex sharding algorithm in Apache ShardingSphere.

### Hint Sharding Algorithm Configuration

There is no built-in hint sharding algorithm in Apache ShardingSphere.

## Key Generate Algorithm Configuration

Apache ShardingSphere built-in key generate algorithm are:

### Snowflake

Type: SNOWFLAKE

Attributes:

| *Name*                                        | *DataType* | *Description*                                                                | *Default Value* |
| --------------------------------------------- | ---------- | ---------------------------------------------------------------------------- | --------------- |
| worker.id (?)                                 | long       | The unique ID for working machine                                            | 0               |
| max.tolerate.time.difference.milliseconds (?) | long       | The max tolerate time for different server's time difference in milliseconds | 10 milliseconds |
| max.vibration.offset (?)                      | int        | The max upper limit value of vibrate number, range `[0, 4096)`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1`| 1 |

### UUID

Type: UUID

Attributes: None
