+++
title = "数据分片"
weight = 1
+++

## 配置入口

类名称：org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

可配置属性：

| *名称*                                     | *数据类型*                            | *说明*            | *默认值* |
| ----------------------------------------- | ------------------------------------ | ----------------- | ------- |
| tableRuleConfigs (+)                      | Collection\<TableRuleConfiguration\> | 分片规则列表        | -       |
| bindingTableGroups (*)                    | Collection\<String\>                 | 绑定表规则列表      | 无      |
| broadcastTables (*)                       | Collection\<String\>                 | 广播表规则列表      | 无      |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration        | 默认分库策略        | 不分片   |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration        | 默认分表策略        | 不分片   |
| defaultKeyGeneratorConfig (?)             | KeyGeneratorConfiguration            | 默认自增列生成器配置 | 雪花算法 |

## 逻辑表配置

类名称：org.apache.shardingsphere.sharding.api.config.TableRuleConfiguration

可配置属性：

| *名称*                              | *数据类型*                     | *说明*                                                      | *默认值*                                                                            |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| logicTable                         | String                        | 逻辑表名称                                                   | -                                                                                  |
| actualDataNodes (?)                | String                        | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持行表达式 | 使用已知数据源与逻辑表名称生成数据节点，用于广播表或只分库不分表且所有库的表结构完全一致的情况 |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | 分库策略                                                     | 使用默认分库策略                                                                     |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | 分表策略                                                     | 使用默认分表策略                                                                     |
| keyGeneratorConfig (?)             | KeyGeneratorConfiguration     | 自增列生成器                                                 | 使用默认自增主键生成器                                                                |

## 分片策略配置

### 标准分片策略配置

类名称：StandardShardingStrategyConfiguration

可配置属性：

| *名称*                      | *数据类型*                 | *说明*                  |
| -------------------------- | ------------------------- | ----------------------- |
| shardingColumn             | String                    | 分片列名称               |
| shardingAlgorithm          | StandardShardingAlgorithm | 标准分片算法实现类        |

Apache ShardingSphere 内置的标准分片算法 StandardShardingAlgorithm 的实现类。

包名称：`org.apache.shardingsphere.sharding.strategy.algorithm.sharding`

| *类名称*                              | *说明*                      |
| ------------------------------------ | --------------------------- |
| inline.InlineShardingAlgorithm       | 基于行表达式的分片。算法详情请参考[行表达式](/cn/features/sharding/other-features/inline-expression) |
| ModuloShardingAlgorithm              | 基于取模的分片算法             |
| HashShardingAlgorithm                | 基于哈希取模的分片算法         |
| range.StandardRangeShardingAlgorithm | 基于时间的分片算法             |
| range.CustomRangeShardingAlgorithm   | 基于用户自定义时间格式的分片算法 |
| DatetimeShardingAlgorithm            | 基于范围的分片算法             |
| CustomDateTimeShardingAlgorithm      | 基于用户自定义范围的分片算法    |

### 复合分片策略配置

类名称：ComplexShardingStrategyConfiguration

可配置属性：

| *名称*             | *数据类型*                    | *说明*                   |
| ----------------- | ---------------------------- | ------------------------ |
| shardingColumns   | String                       | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | 复合分片算法实现类          |

### Hint 分片策略配置

类名称：HintShardingStrategyConfiguration

可配置属性：

| *名称*             | *数据类型*             | *说明*      |
| ----------------- | --------------------- | ----------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint分片算法 |

### 不分片策略配置

类名称：NoneShardingStrategyConfiguration

## 自增主键策略配置

类名称：KeyGeneratorConfiguration

可配置属性：

| *名称*               | *数据类型*            | *说明*            |
| -------------------- | -------------------- | ---------------- |
| column               | String               | 自增列名称        |
| keyGenerateAlgorithm | KeyGenerateAlgorithm | 自增主键算法实现类 |

Apache ShardingSphere 内置的自增主键算法的实现类包括：

### 雪花算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.keygen.SnowflakeKeyGenerateAlgorithm

可配置属性：

| *属性名称*                                     | *数据类型* | *说明*                                      |
| --------------------------------------------- | --------- | ------------------------------------------- |
| worker.id (?)                                 | long      | 工作机器唯一标识，默认为 0                     |
| max.vibration.offset (?)                      | int       | 最大抖动上限值，范围[0, 4096)，默认为1。注：若使用此算法生成值作分片值，建议配置此属性。此算法在不同毫秒内所生成的 key 取模 2^n (2^n一般为分库或分表数) 之后结果总为 0 或 1。为防止上述分片问题，建议将此属性值配置为 (2^n)-1 |
| max.tolerate.time.difference.milliseconds (?) | long      | 最大容忍时钟回退时间，单位：毫秒。默认为 10 毫秒 |

### UUID

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.keygen.UUIDKeyGenerateAlgorithm

可配置属性：无
