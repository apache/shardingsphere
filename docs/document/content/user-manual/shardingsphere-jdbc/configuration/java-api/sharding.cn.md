+++
title = "数据分片"
weight = 1
+++

## 配置入口

类名称：org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

可配置属性：

| *名称*                               | *数据类型*                                    | *说明*            | *默认值* |
| ----------------------------------- | -------------------------------------------- | ----------------- | ------- |
| tables (+)                          | Collection\<ShardingTableRuleConfiguration\> | 分片表规则列表      | -       |
| bindingTableGroups (*)              | Collection\<String\>                         | 绑定表规则列表      | 无      |
| broadcastTables (*)                 | Collection\<String\>                         | 广播表规则列表      | 无      |
| defaultDatabaseShardingStrategy (?) | ShardingStrategyConfiguration                | 默认分库策略        | 不分片   |
| defaultTableShardingStrategy (?)    | ShardingStrategyConfiguration                | 默认分表策略        | 不分片   |
| defaultKeyGenerateStrategy (?)       | KeyGeneratorConfiguration                    | 默认自增列生成器配置 | 雪花算法 |

## 逻辑表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

可配置属性：

| *名称*                        | *数据类型*                     | *说明*                                                            | *默认值*                                                                            |
| ---------------------------- | ----------------------------- | ----------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| logicTable                   | String                        | 逻辑表名称                                                         | -                                                                                  |
| actualDataNodes (?)          | String                        | 由数据源名 + 表名组成，以小数点分隔。<br />多个表以逗号分隔，支持行表达式 | 使用已知数据源与逻辑表名称生成数据节点，用于广播表或只分库不分表且所有库的表结构完全一致的情况 |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | 分库策略                                                           | 使用默认分库策略                                                                     |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | 分表策略                                                           | 使用默认分表策略                                                                     |
| keyGenerateStrategy (?)             | KeyGeneratorConfiguration     | 自增列生成器                                                        | 使用默认自增主键生成器                                                               |

## 分片策略配置

### 标准分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.StandardShardingStrategyConfiguration

可配置属性：

| *名称*             | *数据类型*                 | *说明*           |
| ----------------- | ------------------------- | ---------------- |
| shardingColumn    | String                    | 分片列名称        |
| shardingAlgorithm | StandardShardingAlgorithm | 标准分片算法实现类 |

Apache ShardingSphere 内置的标准分片算法实现类包括：

#### 行表达式分片算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.sharding.inline.InlineShardingAlgorithm

可配置属性：

| *属性名称*                                 | *数据类型* | *说明*                                              | *默认值* |
| ----------------------------------------- | --------- | --------------------------------------------------- | ------- |
| algorithm.expression                      | String    | 分片算法的行表达式                                    | -       |
| allow.range.query.with.inline.sharding (?)| boolean   | 是否允许范围查询。注意：范围查询会无视分片策略，进行全路由 | false   |

#### 取模分片算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.sharding.ModuloShardingAlgorithm

可配置属性：

| *属性名称* | *数据类型* | *说明*  |
| --------- | --------- | ------- |
| mod.value | int       | 分片数量 |

#### 哈希取模分片算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.sharding.HashShardingAlgorithm

可配置属性：

| *属性名称* | *数据类型* | *说明*  |
| --------- | --------- | ------- |
| mod.value | int       | 分片数量 |

#### 固定容量范围分片算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.sharding.range.StandardRangeShardingAlgorithm

可配置属性：

| *属性名称*        | *数据类型* | *说明*                      |
| ---------------- | --------- | -------------------------- |
| partition.lower  | long      | 范围下界，超过边界的数据会报错 |
| partition.upper  | long      | 范围上界，超过边界的数据会报错 |
| partition.volume | long      | 分片容量                    |

#### 自定义边界范围分片算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.sharding.range.CustomRangeShardingAlgorithm

可配置属性：

| *属性名称*        | *数据类型* | *说明*                            |
| ---------------- | --------- | --------------------------------- |
| partition.ranges | String    | 分片的范围边界，多个范围边界以逗号分隔 |

#### 定长时间段分片算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.sharding.DatetimeShardingAlgorithm

可配置属性：

| *属性名称*         | *数据类型* | *说明*                                          |
| ----------------- | --------- | ----------------------------------------------- |
| epoch             | String    | 分片时间的起始纪元，时间戳格式：yyyy-MM-dd HH:mm:ss |
| partition.seconds | long      | 单一分片所能承载的最大时间，单位：秒                |

#### 自定义时间边界分片算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.sharding.CustomDateTimeShardingAlgorithm

可配置属性：

| *属性名称*            | *数据类型* | *说明*                              |
| -------------------- | --------- | ----------------------------------- |
| datetime.format      | String    | 时间戳格式，例如：yyyy-MM-dd HH:mm:ss |
| table.suffix.format  | String    | TODO                                |
| datetime.lower       | String    | TODO                                |
| datetime.upper       | String    | TODO                                |
| datetime.step.unit   | String    | TODO                                |
| datetime.step.amount | String    | TODO                                |

### 复合分片策略配置

类名称：ComplexShardingStrategyConfiguration

可配置属性：

| *名称*             | *数据类型*                    | *说明*                   |
| ----------------- | ---------------------------- | ------------------------ |
| shardingColumns   | String                       | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | 复合分片算法实现类          |

Apache ShardingSphere 暂无内置复合分片算法实现类。

### Hint 分片策略配置

类名称：HintShardingStrategyConfiguration

可配置属性：

| *名称*             | *数据类型*             | *说明*      |
| ----------------- | --------------------- | ----------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint分片算法 |

Apache ShardingSphere 暂无内置复合分片算法实现类。

### 不分片策略配置

类名称：NoneShardingStrategyConfiguration

可配置属性：无

## 自增主键策略配置

类名称：KeyGeneratorConfiguration

可配置属性：

| *名称*    | *数据类型*            | *说明*            |
| --------- | -------------------- | ---------------- |
| column    | String               | 自增列名称        |
| algorithm | KeyGenerateAlgorithm | 自增主键算法实现类 |

Apache ShardingSphere 内置的自增主键算法实现类包括：

### 雪花算法

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.keygen.SnowflakeKeyGenerateAlgorithm

可配置属性：

| *属性名称*                                     | *数据类型* | *说明*                                                                                                                                                                                         | *默认值* |
| --------------------------------------------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ |
| worker.id (?)                                 | long      | 工作机器唯一标识                                                                                                                                                                                 | 0      |
| max.vibration.offset (?)                      | int       | 最大抖动上限值，范围[0, 4096)。注：若使用此算法生成值作分片值，建议配置此属性。此算法在不同毫秒内所生成的 key 取模 2^n (2^n一般为分库或分表数) 之后结果总为 0 或 1。为防止上述分片问题，建议将此属性值配置为 (2^n)-1 | 1      |
| max.tolerate.time.difference.milliseconds (?) | long      | 最大容忍时钟回退时间，单位：毫秒                                                                                                                                                                   | 10 毫秒 |

### UUID

类名称：org.apache.shardingsphere.sharding.strategy.algorithm.keygen.UUIDKeyGenerateAlgorithm

可配置属性：无
