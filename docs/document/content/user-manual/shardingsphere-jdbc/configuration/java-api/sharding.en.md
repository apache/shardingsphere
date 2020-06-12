+++
title = "Sharding"
weight = 1
+++

## Root Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

Attributes:

| *Name*                              | *DataType*                                   | *Description*                      | *Default Value* |
| ----------------------------------- | -------------------------------------------- | ---------------------------------- | --------------- |
| tables (+)                          | Collection\<ShardingTableRuleConfiguration\> | Sharding table rules               | -               |
| bindingTableGroups (*)              | Collection\<String\>                         | Binding table rules                | Empty           |
| broadcastTables (*)                 | Collection\<String\>                         | Broadcast table rules              | Empty           |
| defaultDatabaseShardingStrategy (?) | ShardingStrategyConfiguration                | Default database sharding strategy | Not sharding    |
| defaultTableShardingStrategy (?)    | ShardingStrategyConfiguration                | Default table sharding strategy    | Not sharding    |
| defaultKeyGenerateStrategy (?)       | KeyGeneratorConfiguration                    | Default key generator              | Snowflake       |

## Sharding Table Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

Attributes:

| *Name*                       | *DataType*                    | *Description*                                                                                                                         | *Default Value*                             |
| ---------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------- |
| logicTable                   | String                        | Name of logic table                                                                                                                   | -                                           |
| actualDataNodes (?)          | String                        | Describe data source names and actual tables, delimiter as point.<br /> Multiple data nodes split by comma, support inline expression | Broadcast table or databases sharding only. |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | Databases sharding strategy                                                                                                           | Use default databases sharding strategy     |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | Tables sharding strategy                                                                                                              | Use default tables sharding strategy        |
| keyGenerateStrategy (?)             | KeyGeneratorConfiguration     | Key generator configuration                                                                                                           | Use default key generator                   |

## Sharding Strategy Configuration

### Standard Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration

Attributes:

| *Name*                     | *DataType*                | *Description*                                   |
| -------------------------- | ------------------------- | ----------------------------------------------- |
| shardingColumn             | String                    | Sharding column name                            |
| shardingAlgorithm          | StandardShardingAlgorithm | Standard sharding algorithm class               |

Apache ShardingSphere built-in implemented classes of StandardShardingAlgorithm are:

#### Inline Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.inline.InlineShardingAlgorithm

Attributes:

| *Name*                                    | *DataType* | *Description*                                                                                            | *Default Value* |
| ----------------------------------------- | ---------- | -------------------------------------------------------------------------------------------------------- | --------------- |
| algorithm.expression                      | String     | Inline expression sharding algorithm                                                                     | -               |
| allow.range.query.with.inline.sharding (?)| boolean    | Whether range query is allowed. Note: range query will ignore sharding strategy and conduct full routing | false           |

#### Modulo Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.ModuloShardingAlgorithm

Attributes:

| *Name*    | *DataType* | *Description*  |
| --------- | ---------- | -------------- |
| mod.value | int        | Sharding count |

#### Hash Modulo Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.HashShardingAlgorithm

Attributes:

| *Name*    | *DataType* | *Description*  |
| --------- | ---------- | -------------- |
| mod.value | int        | Sharding count |

#### Volume Range Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.range.StandardRangeShardingAlgorithm

Attributes:

| *Name*           | *DataType* | *Description*                                            |
| ---------------- | ---------- | -------------------------------------------------------- |
| partition.lower  | long       | Range lower bound, throw exception if lower than bound   |
| partition.upper  | long       | Range upper bound, throw exception if upper than bound   |
| partition.volume | long       | Sharding volume                                          |

#### Customized Range Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.range.CustomRangeShardingAlgorithm

Attributes:

| *Name*           | *DataType* | *Description*                                                     |
| ---------------- | ---------- | ----------------------------------------------------------------- |
| partition.ranges | String     | Range of sharding border, multiple boundaries separated by commas |

#### Fixed Range Volume Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.range.StandardRangeShardingAlgorithm

Attributes:

| *Name*           | *DataType* | *Description*                                                       |
| ---------------- | ---------- | ------------------------------------------------------------------- |
| partition.lower  | long       | Lower bound of range, data beyond the boundary will report an error |
| partition.upper  | long       | Upper bound of range, data beyond the boundary will report an error |
| partition.volume | long       | Range volume                                                        |

#### Custom Range Bound Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.range.CustomRangeShardingAlgorithm

Attributes:

| *Name*           | *DataType* | *Description*                                                       |
| ---------------- | ---------- | --------------------------------- |
| partition.ranges | String     | 分片的范围边界，多个范围边界以逗号分隔 |

#### Fixed Time Range Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.DatetimeShardingAlgorithm

Attributes:

| *Name*            | *DataType* | *Description*                                      |
| ----------------- | ---------- | -------------------------------------------------- |
| epoch             | String     | Shard datetime epoch, pattern: yyyy-MM-dd HH:mm:ss |
| partition.seconds | long       | Max seconds for the data in one shard              |

#### Custom Datetime Bound Sharding Algorithm

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.sharding.CustomDateTimeShardingAlgorithm

Attributes:

| *Name*               | *DataType* | *Description*                                  |
| -------------------- | ---------- | ---------------------------------------------- |
| datetime.format      | String     | Datetime pattern, example: yyyy-MM-dd HH:mm:ss |
| table.suffix.format  | String     | TODO                                           |
| datetime.lower       | String     | TODO                                           |
| datetime.upper       | String     | TODO                                           |
| datetime.step.unit   | String     | TODO                                           |
| datetime.step.amount | String     | TODO                                           |

### Complex Sharding Strategy Configuration

Class name: ComplexShardingStrategyConfiguration

Attributes:

| *Name*            | *DataType*                   | *Description*                             |
| ----------------- | ---------------------------- | ----------------------------------------- |
| shardingColumns   | String                       | Sharding column name, separated by commas |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | Complex sharding algorithm                |

There is no built-in complex keys sharding algorithm implementation class in Apache ShardingSphere.

### Hint Sharding Strategy Configuration

Class name: HintShardingStrategyConfiguration

Attributes:

| *Name*            | *DataType*            | *Description*           |
| ----------------- | --------------------- | ----------------------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint sharding algorithm |

There is no built-in hint sharding algorithm implementation class in Apache ShardingSphere.

### None Sharding Strategy Configuration

Class name: NoneShardingStrategyConfiguration

Attributes: None

## Key Generator Configuration

Class name: KeyGeneratorConfiguration

Attributes:

| *Name*    | *DataType*           | *Description*                |
| --------- | -------------------- | ---------------------------- |
| column    | String               | Column name of key generate  |
| algorithm | KeyGenerateAlgorithm | Key generate algorithm class |

Apache ShardingSphere built-in implemented classes of KeyGenerateAlgorithm are:

### Snowflake

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.keygen.SnowflakeKeyGenerateAlgorithm

Attributes:

| *Name*                                        | *DataType* | *Description*                                                                | *Default Value* |
| --------------------------------------------- | ---------- | ---------------------------------------------------------------------------- | --------------- |
| worker.id (?)                                 | long       | The unique ID for working machine                                            | 0               |
| max.tolerate.time.difference.milliseconds (?) | long       | The max tolerate time for different server's time difference in milliseconds | 10 milliseconds |
| max.vibration.offset (?)                      | int        | The max upper limit value of vibrate number, range `[0, 4096)`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1`| 1 |

### UUID

Class name: org.apache.shardingsphere.sharding.strategy.algorithm.keygen.UUIDKeyGenerateAlgorithm

Attributes: None
