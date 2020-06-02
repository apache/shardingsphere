+++
title = "Java API 配置项"
weight = 1
+++

## 用户 API

Apache ShardingSphere 数据源创建工厂。

| *名称*             | *数据类型*                 | *说明*          |
| ------------------ |  ------------------------ | -------------- |
| dataSourceMap      | Map\<String, DataSource\> | 数据源配置      |
| ruleConfigurations | ShardingRuleConfiguration | 配置规则集合    |
| props (?)          | Properties                | 属性配置        |

## 配置项说明

### 数据分片

#### 配置入口

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

#### 逻辑表配置

类名称：TableRuleConfiguration

可配置属性：

| *名称*                              | *数据类型*                     | *说明*                                                                                                                                                                                                      |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logicTable                         | String                        | 逻辑表名称                                                                                                                                                                                                   |
| actualDataNodes (?)                | String                        | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况    |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | 分库策略，缺省表示使用默认分库策略                                                                                                                                                                              |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | 分表策略，缺省表示使用默认分表策略                                                                                                                                                                              |
| keyGeneratorConfig (?)             | KeyGeneratorConfiguration     | 自增列值生成器配置，缺省表示使用默认自增主键生成器                                                                                                                                                                |

#### 分片策略配置

##### 标准分片策略配置

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

##### 复合分片策略配置

类名称：ComplexShardingStrategyConfiguration

可配置属性：

| *名称*             | *数据类型*                    | *说明*                   |
| ----------------- | ---------------------------- | ------------------------ |
| shardingColumns   | String                       | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | 复合分片算法实现类          |

##### Hint 分片策略配置

类名称：HintShardingStrategyConfiguration

可配置属性：

| *名称*             | *数据类型*             | *说明*      |
| ----------------- | --------------------- | ----------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint分片算法 |

##### 不分片策略配置

类名称：NoneShardingStrategyConfiguration

#### KeyGeneratorConfiguration

| *名称*             | *数据类型*                    | *说明*                                                                         |
| ----------------- | ---------------------------- | ------------------------------------------------------------------------------ |
| column            | String                       | 自增列名称                                                                      |
| type              | String                       | 自增列值生成器类型，可自定义或选择内置类型：SNOWFLAKE/UUID |
| props             | Properties                   | 自增列值生成器的相关属性配置                                                      |

#### Properties

属性配置项，可以为以下自增列值生成器的属性。

##### SNOWFLAKE

| *名称*                                              | *数据类型*  | *说明*                                                                                                   |
| --------------------------------------------------- | ---------- | ------------------------------------------------------------------------------------------------------- |
| worker.id (?)                                        | long       | 工作机器唯一id，默认为0                                                                                  |
| max.tolerate.time.difference.milliseconds (?)        | long       | 最大容忍时钟回退时间，单位：毫秒。默认为10毫秒                                                               |
| max.vibration.offset (?)                             | int        | 最大抖动上限值，范围[0, 4096)，默认为1。注：若使用此算法生成值作分片值，建议配置此属性。此算法在不同毫秒内所生成的key取模2^n (2^n一般为分库或分表数) 之后结果总为0或1。为防止上述分片问题，建议将此属性值配置为(2^n)-1 |

#### EncryptRuleConfiguration

| *名称*               |*数据类型*                                    | *说明*                                                                          |
| ------------------- | ------------------------------------------- | ------------------------------------------------------------------------------ |
| encryptors          | Map<String, EncryptorRuleConfiguration>     | 加解密器配置列表，可自定义或选择内置类型：MD5/AES                                    |
| tables              | Map<String, EncryptTableRuleConfiguration>  | 加密表配置列表                                                                   |

#### EncryptorRuleConfiguration

| *名称*               |*数据类型*                    | *说明*                                                                          |
| ------------------- | ---------------------------- | ------------------------------------------------------------------------------ |
| type                | String                       | 加解密器类型，可自定义或选择内置类型：MD5/AES                                       |
| properties          | Properties                   | 属性配置, 注意：使用AES加密器，需要配置AES加密器的KEY属性：aes.key.value              |

#### EncryptTableRuleConfiguration

| *名称*               |*数据类型*                                     | *说明*                            |
| ------------------- | -------------------------------------------- | --------------------------------- |
| tables              | Map<String, EncryptColumnRuleConfiguration>  | 加密列配置列表                      |

#### EncryptColumnRuleConfiguration

| *名称*               |*数据类型*                    | *说明*                                                                          |
| ------------------- | ---------------------------- | ------------------------------------------------------------------------------ |
| plainColumn        | String                       | 存储明文的字段                                                                   |
| cipherColumn       | String                       | 存储密文的字段                                                                   |
| assistedQueryColumn| String                       | 辅助查询字段，针对ShardingQueryAssistedEncryptor类型的加解密器进行辅助查询            |
| encryptor          | String                       | 加解密器名字                                                                      |

#### Properties

属性配置项，可以为以下属性。

| *名称*                             | *数据类型*  | *说明*                                          |
| ----------------------------------| --------- | -------------------------------------------------|
| sql.show (?)                      | boolean   | 是否开启SQL显示，默认值: false                      |
| executor.size (?)                 | int       | 工作线程数量，默认值: CPU核数                       |
| max.connections.size.per.query (?)| int       | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1   |
| check.table.metadata.enabled (?)  | boolean   | 是否在启动时检查分表元数据一致性，默认值: false        |
| query.with.cipher.column (?)      | boolean   | 当存在明文列时，是否使用密文列查询，默认值: true        |
| allow.range.query.with.inline.sharding (?)    | boolean   | 当使用inline分表策略时，是否允许范围查询，默认值: false        |

### 读写分离

#### MasterSlaveDataSourceFactory

读写分离的数据源创建工厂。

| *名称*                 | *数据类型*                    | *说明*             |
| --------------------- | ---------------------------- | ------------------ |
| dataSourceMap         | Map\<String, DataSource\>    | 数据源与其名称的映射  |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 读写分离规则         |
| props (?)             | Properties                   | 属性配置            |

#### MasterSlaveRuleConfiguration

读写分离规则配置对象。

| *名称*                    | *数据类型*                       | *说明*           |
| ------------------------ | ------------------------------- | ---------------- |
| name                     | String                          | 读写分离数据源名称 |
| masterDataSourceName     | String                          | 主库数据源名称    |
| slaveDataSourceNames     | Collection\<String\>            | 从库数据源名称列表 |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | 从库负载均衡算法   |

#### Properties

属性配置项，可以为以下属性。

| *名称*                              | *数据类型* | *说明*                                            |
| ---------------------------------- | --------- | ------------------------------------------------- |
| sql.show (?)                       | boolean   | 是否打印SQL解析和改写日志，默认值: false              |
| executor.size (?)                  | int       | 用于SQL执行的工作线程数量，为零则表示无限制。默认值: 0   |
| max.connections.size.per.query (?) | int       | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1    |
| check.table.metadata.enabled (?)   | boolean   | 是否在启动时检查分表元数据一致性，默认值: false         |

### 数据加密

#### EncryptDataSourceFactory

| *名称*                 | *数据类型*                    | *说明*             |
| --------------------- | ---------------------------- | ------------------ |
| dataSource            | DataSource                   | 数据源，任意连接池    |
| encryptRuleConfig     | EncryptRuleConfiguration     | 数据加密规则         |
| props (?)             | Properties                   | 属性配置            |

#### EncryptRuleConfiguration

| *名称*               |*数据类型*                                    | *说明*                                                                          |
| ------------------- | ------------------------------------------- | ------------------------------------------------------------------------------ |
| encryptors          | Map<String, EncryptorRuleConfiguration>     | 加解密器配置列表，可自定义或选择内置类型：MD5/AES                                    |
| tables              | Map<String, EncryptTableRuleConfiguration>  | 加密表配置列表                      |

#### Properties

属性配置项，可以为以下属性。

| *名称*                             | *数据类型*  | *说明*                                          |
| ----------------------------------| --------- | -------------------------------------------------|
| sql.show (?)                      | boolean   | 是否开启SQL显示，默认值: false                      |
| query.with.cipher.column (?)      | boolean   | 当存在明文列时，是否使用密文列查询，默认值: true       |

### 治理

#### OrchestrationShardingDataSourceFactory

数据分片 + 治理的数据源工厂。

| *名称*               | *数据类型*                  | *说明*                      |
| ------------------- |  ------------------------- | --------------------------- |
| dataSourceMap       | Map\<String, DataSource\>  | 同ShardingDataSourceFactory |
| shardingRuleConfig  | ShardingRuleConfiguration  | 同ShardingDataSourceFactory |
| props (?)           | Properties                 | 同ShardingDataSourceFactory |
| orchestrationConfig | OrchestrationConfiguration | 治理规则配置              |

#### OrchestrationMasterSlaveDataSourceFactory

读写分离 + 治理的数据源工厂。

| *名称*                 | *数据类型*                    | *说明*                         |
| --------------------- | ---------------------------- | ------------------------------ |
| dataSourceMap         | Map\<String, DataSource\>    | 同MasterSlaveDataSourceFactory |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 同MasterSlaveDataSourceFactory |
| props (?)             | Properties                   | 同ShardingDataSourceFactory    |
| orchestrationConfig   | OrchestrationConfiguration   | 治理规则配置                 |

#### OrchestrationEncryptDataSourceFactory

数据加密 + 治理的数据源工厂。

| *名称*                 | *数据类型*                    | *说明*                         |
| --------------------- | ---------------------------- | ------------------------------ |
| dataSource            | DataSource                   | 同EncryptDataSourceFactory     |
| encryptRuleConfig     | EncryptRuleConfiguration     | 同EncryptDataSourceFactory     |
| props (?)             | Properties                   | 同ShardingDataSourceFactory    |
| orchestrationConfig   | OrchestrationConfiguration   | 治理规则配置                 |

#### OrchestrationConfiguration

治理规则配置对象。

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| instanceConfigurationMap | Map\<String, CenterConfiguration\>  | 配置/注册/元数据中心的配置map，key为名称，value为配置/注册/元数据中心   |

#### CenterConfiguration

用于配置配置/注册/元数据中心。

| *名称*                             | *数据类型* | *说明*                                                                               |
| --------------------------------- | ---------- | ----------------------------------------------------------------------------------- |
| instanceType                      | String     | 配置/注册/元数据中心的实例类型，例如zookeeper或etcd、apollo、nacos                                       |
| properties                        | String     | 配置本实例需要的其他参数，例如zookeeper的连接参数等，具体参考properties配置                         |
| orchestrationType                 | String     | 治理类型，例如config_center/registry_center/metadata_center，如果都是，可以"setOrchestrationType("registry_center,config_center,metadata_center");"              |
| serverLists                       | String     | 连接配置/注册/元数据中心服务器的列表，包括IP地址和端口号，多个地址用逗号分隔。如: host1:2181,host2:2181 |
| namespace (?)                     | String     | 配置/注册/元数据中心的命名空间                                                                     |

其中properties的通用配置如下：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| overwrite                         | boolean    | 本地配置是否覆盖配置中心配置，如果可覆盖，每次启动都以本地配置为准                         |

如果采用了zookeeper作为配置中心或（和）注册中心或 (和) 元数据中心，那么properties还可以配置：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| digest (?)                        | String     | 连接注册中心的权限令牌。缺省为不需要权限验证                                             |
| operationTimeoutMilliseconds (?)  | int        | 操作超时的毫秒数，默认500毫秒                                                          |
| maxRetries (?)                    | int        | 连接失败后的最大重试次数，默认3次                                                       |
| retryIntervalMilliseconds (?)     | int        | 重试间隔毫秒数，默认500毫秒                                                            |
| timeToLiveSeconds (?)             | int        | 临时节点存活秒数，默认60秒                                                             |

如果采用了etcd作为配置中心或（和）注册中心或 (和) 元数据中心，那么properties还可以配置：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| timeToLiveSeconds (?)             | long        | TTL时间，单位为秒，默认30秒                                     |

如果采用了apollo作为配置中心，那么properties还可以配置：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| appId (?)          | String        | apollo appId，默认值为"APOLLO_SHARDINGSPHERE"                               |
| env (?)            | String        | apollo env，默认值为"DEV"                                                   |
| clusterName (?)    | String        | apollo clusterName，默认值为"default"                                       |
| administrator (?)  | String        | apollo administrator，默认值为""                                            |
| token (?)          | String        | apollo token，默认值为""                                                    |
| portalUrl (?)      | String        | apollo portalUrl，默认值为""                                                |
| connectTimeout (?) | int           | apollo connectTimeout，默认值为1000毫秒                                      |
| readTimeout (?)    | int           | apollo readTimeout，默认值为5000毫秒                                         |

如果采用了nacos作为配置中心或 (和) 注册中心，那么properties还可以配置：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| group (?)          | String        | nacos group配置，默认值为"SHARDING_SPHERE_DEFAULT_GROUP"                     |
| timeout (?)        | long          | nacos 获取数据超时时间，单位为毫秒，默认值为3000毫秒                            |
