+++
title = "数据分片"
weight = 1
+++

## 配置入口

类名称：ShardingRuleConfiguration

可配置属性：

| *名称*                                     | *数据类型*                                  | *说明*                                                                                         
| ----------------------------------------- | ------------------------------------------ | ----------------------------------------------------------------------------------------------- |
| tableRuleConfigs                          | Collection\<TableRuleConfiguration\>       | 分片规则列表                                                                                      |
| bindingTableGroups (?)                    | Collection\<String\>                       | 绑定表规则列表                                                                                    |
| broadcastTables (?)                       | Collection\<String\>                       | 广播表规则列表                                                                                    |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration              | 默认分库策略                                                                                      |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration              | 默认分表策略                                                                                      |
| defaultKeyGeneratorConfig (?)             | KeyGeneratorConfiguration                  | 默认自增列值生成器配置，缺省将使用org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator |

## 逻辑表配置

类名称：TableRuleConfiguration

可配置属性：

| *名称*                              | *数据类型*                     | *说明*                                                                                                                                                                                                      |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logicTable                         | String                        | 逻辑表名称                                                                                                                                                                                                   |
| actualDataNodes (?)                | String                        | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况    |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | 分库策略，缺省表示使用默认分库策略                                                                                                                                                                              |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | 分表策略，缺省表示使用默认分表策略                                                                                                                                                                              |
| keyGeneratorConfig (?)             | KeyGeneratorConfiguration     | 自增列值生成器配置，缺省表示使用默认自增主键生成器                                                                                                                                                                |

## 分片策略配置

### 标准分片策略配置

类名称：StandardShardingStrategyConfiguration

可配置属性：

| *名称*                      | *数据类型*                 | *说明*                  |
| -------------------------- | ------------------------- | ----------------------- |
| shardingColumn             | String                    | 分片列名称               |
| shardingAlgorithm          | StandardShardingAlgorithm | 标准分片算法实现类        |

Apache ShardingSphere内置的标准分片算法 StandardShardingAlgorithm 的实现类。

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

## KeyGeneratorConfiguration

| *名称*             | *数据类型*                    | *说明*                                                                         |
| ----------------- | ---------------------------- | ------------------------------------------------------------------------------ |
| column            | String                       | 自增列名称                                                                      |
| type              | String                       | 自增列值生成器类型，可自定义或选择内置类型：SNOWFLAKE/UUID |
| props             | Properties                   | 自增列值生成器的相关属性配置                                                      |

### Properties

属性配置项，可以为以下自增列值生成器的属性。

### SNOWFLAKE

| *名称*                                              | *数据类型*  | *说明*                                                                                                   |
| --------------------------------------------------- | ---------- | ------------------------------------------------------------------------------------------------------- |
| worker.id (?)                                        | long       | 工作机器唯一id，默认为0                                                                                  |
| max.tolerate.time.difference.milliseconds (?)        | long       | 最大容忍时钟回退时间，单位：毫秒。默认为10毫秒                                                               |
| max.vibration.offset (?)                             | int        | 最大抖动上限值，范围[0, 4096)，默认为1。注：若使用此算法生成值作分片值，建议配置此属性。此算法在不同毫秒内所生成的key取模2^n (2^n一般为分库或分表数) 之后结果总为0或1。为防止上述分片问题，建议将此属性值配置为(2^n)-1 |
