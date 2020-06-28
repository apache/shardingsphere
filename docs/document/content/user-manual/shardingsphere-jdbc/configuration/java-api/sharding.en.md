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

Please refer to [Built-in Sharding Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding) for more details about type of algorithm.

## Key Generate Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration

Attributes:

| *Name*           | *DataType* | *Description*               |
| ---------------- | ---------- | --------------------------- |
| column           | String     | Column name of key generate |
| keyGeneratorName | String     | key generate algorithm name |

Please refer to [Built-in Key Generate Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen) for more details about type of algorithm.
