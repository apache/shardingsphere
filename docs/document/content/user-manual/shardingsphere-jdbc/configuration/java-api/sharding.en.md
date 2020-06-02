+++
title = "Sharding"
weight = 1
+++

## Root Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

Attributes:

| *Name*                                    | *DataType*                           | *Description*                      | *Default Value* |
| ----------------------------------------- | ------------------------------------ | ---------------------------------- | --------------- |
| tableRuleConfigs (+)                      | Collection\<TableRuleConfiguration\> | Sharding rules                     | -               |
| bindingTableGroups (*)                    | Collection\<String\>                 | Binding table rules                | Empty           |
| broadcastTables (*)                       | Collection\<String\>                 | Broadcast table rules              | Empty           |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration        | Default database sharding strategy | Not sharding    |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration        | Default table sharding strategy    | Not sharding    |
| defaultKeyGeneratorConfig (?)             | KeyGeneratorConfiguration            | Default key generator              | Snowflake       |

## Logic Table Configuration

Class name: org.apache.shardingsphere.sharding.api.config.TableRuleConfiguration

Attributes:

| *Name*                             | *DataType*                    | *Description*                                                                                                                   | *Default Value*                             |
| ---------------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------- |
| logicTable                         | String                        | Name of logic table                                                                                                             | -                                           |
| actualDataNodes (?)                | String                        | Describe data source names and actual tables, delimiter as point, multiple data nodes split by comma, support inline expression | Broadcast table or databases sharding only. |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | Databases sharding strategy                                                                                                     | Use default databases sharding strategy     |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | Tables sharding strategy                                                                                                        | Use default tables sharding strategy        |
| keyGeneratorConfig (?)             | KeyGeneratorConfiguration     | Key generator configuration                                                                                                     | Use default key generator                   |

## Sharding Strategy Configuration

### Standard Sharding Strategy Configuration

Class name: StandardShardingStrategyConfiguration

Attributes:

| *Name*                     | *DataType*                | *Description*                                   |
| -------------------------- | ------------------------- | ----------------------------------------------- |
| shardingColumn             | String                    | Sharding column name                            |
| shardingAlgorithm          | StandardShardingAlgorithm | Standard sharding algorithm class               |

Apache ShardingSphere built-in implemented classes of StandardShardingAlgorithm.

Package name: `org.apache.shardingsphere.sharding.strategy.algorithm.sharding`

| *Class name*                         | *Description*                          |
| ------------------------------------ | -------------------------------------- |
| inline.InlineShardingAlgorithm       | Inline sharding algorithm, refer to [Inline expression](/en/features/sharding/other-features/inline-expression) for more details. |
| ModuloShardingAlgorithm              | Modulo sharding algorithm              |
| HashShardingAlgorithm                | Hash sharding algorithm                |
| range.StandardRangeShardingAlgorithm | Datetime sharding algorithm            |
| range.CustomRangeShardingAlgorithm   | Customized datetime sharding algorithm |
| DatetimeShardingAlgorithm            | Range sharding algorithm               |
| CustomDateTimeShardingAlgorithm      | Customized range sharding algorithm    |

### Complex Sharding Strategy Configuration

Class name: ComplexShardingStrategyConfiguration

Attributes:

| *Name*            | *DataType*                   | *Description*                             |
| ----------------- | ---------------------------- | ----------------------------------------- |
| shardingColumns   | String                       | Sharding column name, separated by commas |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | Complex sharding algorithm                |

### Hint Sharding Strategy Configuration

Class name: HintShardingStrategyConfiguration

Attributes:

The implementation class of `ShardingStrategyConfiguration`,  used to configure hint sharding strategies.

| *Name*            | *DataType*            | *Description*           |
| ----------------- | --------------------- | ----------------------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint sharding algorithm |

### None Sharding Strategy Configuration

Class name: NoneShardingStrategyConfiguration

## KeyGeneratorConfiguration

| *Name* | *DataType* | *Description*                                                |
| ------ | ---------- | ------------------------------------------------------------ |
| column | String     | Column name of key generator                                 |
| type   | String     | Type of key generator, use user-defined ones or built-in ones, e.g. SNOWFLAKE, UUID |
| props  | Properties | The Property configuration of key generators                 |

### Properties

Property configuration that can include these properties of these key generators.

### SNOWFLAKE

| *Name*                                              | *DataType* | *Description*                                                                                                                                                                                                                   |
| --------------------------------------------------- | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| worker.id (?)                                        |   long     | The unique id for working machine, the default value is `0`                                                                                                                                                                    |
| max.tolerate.time.difference.milliseconds (?)        |   long     | The max tolerate time for different server's time difference in milliseconds, the default value is `10`                                                                                                                         |
| max.vibration.offset (?)                             |    int     | The max upper limit value of vibrate number, range `[0, 4096)`, the default value is `1`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1` |
