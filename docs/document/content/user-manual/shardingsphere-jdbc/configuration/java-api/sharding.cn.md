+++
title = "数据分片"
weight = 1
+++

## 配置入口

类名称：org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

可配置属性：

| *名称*                               | *数据类型*                                          | *说明*                | *默认值* |
| ----------------------------------- | --------------------------------------------------- | -------------------- | ------- |
| tables (+)                          | Collection\<ShardingTableRuleConfiguration\>        | 分片表规则列表         | -       |
| autoTables (+)                      | Collection\<ShardingAutoTableRuleConfiguration\>    | 自动化分片表规则列表    | -       |
| bindingTableGroups (*)              | Collection\<String\>                                | 绑定表规则列表         | 无       |
| broadcastTables (*)                 | Collection\<String\>                                | 广播表规则列表         | 无       |
| defaultDatabaseShardingStrategy (?) | ShardingStrategyConfiguration                       | 默认分库策略           | 不分片   |
| defaultTableShardingStrategy (?)    | ShardingStrategyConfiguration                       | 默认分表策略           | 不分片   |
| defaultKeyGenerateStrategy (?)      | KeyGeneratorConfiguration                           | 默认自增列生成器配置    | 雪花算法 |
| shardingAlgorithms (+)              | Map\<String, ShardingSphereAlgorithmConfiguration\> | 分片算法名称和配置      | 无      |
| keyGenerators (?)                   | Map\<String, ShardingSphereAlgorithmConfiguration\> | 自增列生成算法名称和配置 | 无      |

## 分片表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

可配置属性：

| *名称*                        | *数据类型*                     | *说明*                                                            | *默认值*                                                                            |
| ---------------------------- | ----------------------------- | ----------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| logicTable                   | String                        | 分片逻辑表名称                                                      | -                                                                                  |
| actualDataNodes (?)          | String                        | 由数据源名 + 表名组成，以小数点分隔。<br />多个表以逗号分隔，支持行表达式 | 使用已知数据源与逻辑表名称生成数据节点，用于广播表或只分库不分表且所有库的表结构完全一致的情况 |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | 分库策略                                                           | 使用默认分库策略                                                                     |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | 分表策略                                                           | 使用默认分表策略                                                                     |
| keyGenerateStrategy (?)      | KeyGeneratorConfiguration     | 自增列生成器                                                        | 使用默认自增主键生成器                                                               |

## 自动分片表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingAutoTableRuleConfiguration

可配置属性：

| *名称*                   | *数据类型*                     | *说明*                       | *默认值*            |
| ----------------------- | ----------------------------- | ---------------------------- | ------------------ |
| logicTable              | String                        | 分片逻辑表名称                 | -                  |
| actualDataSources (?)   | String                        | 数据源名称，多个数据源以逗号分隔 | 使用全部配置的数据源  |
| shardingStrategy (?)    | ShardingStrategyConfiguration | 分片策略                      | 使用默认分片策略      |
| keyGenerateStrategy (?) | KeyGeneratorConfiguration     | 自增列生成器                   | 使用默认自增主键生成器 |

## 分片策略配置

### 标准分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型* | *说明*      |
| --------------------- | ---------- | ---------- |
| shardingColumn        | String     | 分片列名称   |
| shardingAlgorithmName | String     | 分片算法名称 |

### 复合分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型* | *说明*                    |
| --------------------- | ---------- | ------------------------ |
| shardingColumns       | String     | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithmName | String     | 分片算法名称               |

### Hint 分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型*  | *说明*      |
| --------------------- | ---------- | ----------- |
| shardingAlgorithmName | String     | 分片算法名称  |

### 不分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration

可配置属性：无

算法类型的详情，请参见[内置分片算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding)。

## 分布式序列策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration

可配置属性：

| *名称*           | *数据类型* | *说明*           |
| ---------------- | -------- | ---------------- |
| column           | String   | 分布式序列列名称   |
| keyGeneratorName | String   | 分布式序列算法名称 |

算法类型的详情，请参见[内置分布式序列算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen)。
