+++
title = "Java API"
weight = 2
+++

## 5.0.0-beta

### 数据分片

#### 配置入口

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

#### 分片表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

可配置属性：

| *名称*                        | *数据类型*                     | *说明*                                                            | *默认值*                                                                            |
| ---------------------------- | ----------------------------- | ----------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| logicTable                   | String                        | 分片逻辑表名称                                                      | -                                                                                  |
| actualDataNodes (?)          | String                        | 由数据源名 + 表名组成，以小数点分隔。<br /> 多个表以逗号分隔，支持行表达式    | 使用已知数据源与逻辑表名称生成数据节点，用于广播表或只分库不分表且所有库的表结构完全一致的情况        |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | 分库策略                                                           | 使用默认分库策略                                                                     |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | 分表策略                                                           | 使用默认分表策略                                                                     |
| keyGenerateStrategy (?)      | KeyGeneratorConfiguration     | 自增列生成器                                                        | 使用默认自增主键生成器                                                               |

#### 自动分片表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingAutoTableRuleConfiguration

可配置属性：

| *名称*                   | *数据类型*                     | *说明*                       | *默认值*            |
| ----------------------- | ----------------------------- | ---------------------------- | ------------------ |
| logicTable              | String                        | 分片逻辑表名称                 | -                  |
| actualDataSources (?)   | String                        | 数据源名称，多个数据源以逗号分隔   | 使用全部配置的数据源  |
| shardingStrategy (?)    | ShardingStrategyConfiguration | 分片策略                      | 使用默认分片策略      |
| keyGenerateStrategy (?) | KeyGeneratorConfiguration     | 自增列生成器                   | 使用默认自增主键生成器 |

#### 分片策略配置

##### 标准分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型*  | *说明*      |
| --------------------- | ---------- | ---------- |
| shardingColumn        | String     | 分片列名称   |
| shardingAlgorithmName | String     | 分片算法名称 |

##### 复合分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型*  | *说明*                    |
| --------------------- | ---------- | ------------------------ |
| shardingColumns       | String     | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithmName | String     | 分片算法名称               |

##### Hint 分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型*  | *说明*      |
| --------------------- | ---------- | ----------- |
| shardingAlgorithmName | String     | 分片算法名称  |

##### 不分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration

可配置属性：无

算法类型的详情，请参见 [内置分片算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/sharding)。

##### 分布式序列策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration

可配置属性：

| *名称*           | *数据类型* | *说明*           |
| ---------------- | -------- | --------------- |
| column           | String   | 分布式序列列名称   |
| keyGeneratorName | String   | 分布式序列算法名称 |

算法类型的详情，请参见 [内置分布式序列算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/keygen)。

### 读写分离

#### 配置入口

类名称：ReadwriteSplittingRuleConfiguration

可配置属性：

| *名称*             | *数据类型*                                                   | *说明*            |
| ----------------- | ----------------------------------------------------------- | ----------------- |
| dataSources (+)   | Collection\<ReadwriteSplittingDataSourceRuleConfiguration\> | 读写数据源配置      |
| loadBalancers (*) | Map\<String, ShardingSphereAlgorithmConfiguration\>         | 从库负载均衡算法配置 |

#### 读写分离数据源配置

类名称：ReadwriteSplittingDataSourceRuleConfiguration

可配置属性：

| *名称*                     | *数据类型*             | *说明*             | *默认值*       |
| -------------------------- | -------------------- | ----------------- | ------------ |
| name                       | String               | 读写分离数据源名称   | -             |
| writeDataSourceName        | String               | 写库数据源名称      | -             |
| readDataSourceNames (+)    | Collection\<String\> | 读库数据源名称列表   | -             |
| loadBalancerName (?)       | String               | 读库负载均衡算法名称 | 轮询负载均衡算法 |

算法类型的详情，请参见 [内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance)。

### 数据加密

#### 配置入口

类名称：org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

可配置属性：

| *名称*                     | *数据类型*                                           | *说明*                                                 | *默认值* |
| ------------------------- | --------------------------------------------------- | ----------------------------------------------------- | ------- |
| tables (+)                | Collection\<EncryptTableRuleConfiguration\>         | 加密表规则配置                                           |        |
| encryptors (+)            | Map\<String, ShardingSphereAlgorithmConfiguration\> | 加解密算法名称和配置                                      |        |
| queryWithCipherColumn (?) | boolean                                             | 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询 | true   |

#### 加密表规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

可配置属性：

| *名称*      | *数据类型*                                     | *说明*           |
| ----------- | -------------------------------------------- | --------------- |
| name        | String                                       | 表名称           |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | 加密列规则配置列表 |

#### 加密列规则配置

类名称：org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

可配置属性：

| *名称*                  | *数据类型* | *说明*        |
| ----------------------- | -------- | ------------- |
| logicColumn             | String   | 逻辑列名称     |
| cipherColumn            | String   | 密文列名称     |
| assistedQueryColumn (?) | String   | 查询辅助列名称 |
| plainColumn (?)         | String   | 原文列名称     |
| encryptorName           | String   | 加密算法名称   |

#### 加解密算法配置

类名称：org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

可配置属性：

| *名称*      |*数据类型*   | *说明*           |
| ---------- | ---------- | ---------------- |
| name       | String     | 加解密算法名称     |
| type       | String     | 加解密算法类型     |
| properties | Properties | 加解密算法属性配置 |

算法类型的详情，请参见 [内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/encrypt)。

### 影子库

#### 配置入口

类名称：org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

可配置属性：

| *名称*                 | *数据类型*             | *说明*                                             |
| --------------------- | --------------------- | ------------------------------------------------- |
| column                | String                | SQL 中的影子字段名，该值为 true 的 SQL 会路由到影子库执行 |
| sourceDataSourceNames | List\<String\>        | 生产数据库名称                                       |
| shadowDataSourceNames | List\<String\>        | 影子数据库名称，与上面一一对应                          |

### 分布式治理

#### 配置项说明

##### 治理

*配置入口*

类名称：org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration

可配置属性：

| *名称*                       | *数据类型*                    | *说明*                                                 |
| --------------------------- | --------------------------- | ------------------------------------------------------ |
| name                        | String                      | 注册中心实例名称                                          |
| registryCenterConfiguration | RegistryCenterConfiguration | 注册中心实例的配置                                         |
| overwrite                   | boolean                     | 本地配置是否覆盖配置中心配置，如果可覆盖，每次启动都以本地配置为准  |

注册中心的类型可以为 Zookeeper 或 etcd。

*治理实例配置*

类名称：org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration

可配置属性：

| *名称*         | *数据类型* | *说明*                                                                    |
| ------------- | ---------- | ----------------------------------------------------------------------- |
| type          | String     | 治理实例类型，如：Zookeeper, etcd                                          |
| serverLists   | String     | 治理服务列表，包括 IP 地址和端口号，多个地址用逗号分隔，如: host1:2181,host2:2181 |
| props         | Properties | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                           |
| overwrite     | boolean    | 本地配置是否覆盖配置中心配置；如果覆盖，则每次启动都参考本地配置                     | 

ZooKeeper 属性配置

| *名称*                            | *数据类型* | *说明*                | *默认值* |
| -------------------------------- | --------- | -------------------- | ------- |
| digest (?)                       | String    | 连接注册中心的权限令牌   | 无需验证  |
| operationTimeoutMilliseconds (?) | int       | 操作超时的毫秒数        | 500 毫秒 |
| maxRetries (?)                   | int       | 连接失败后的最大重试次数  | 3 次    |
| retryIntervalMilliseconds (?)    | int       | 重试间隔毫秒数          | 500 毫秒 |
| timeToLiveSeconds (?)            | int       | 临时节点存活秒数        | 60 秒    |

Etcd 属性配置

| *名称*                 | *数据类型* | *说明*     | *默认值* |
| --------------------- | --------- | ---------- | ------ |
| timeToLiveSeconds (?) | long      | 数据存活秒数 | 30 秒    |

## ShardingSphere-4.x

### 数据分片

#### ShardingDataSourceFactory

| *名称*                 | *数据类型*                    | *说明*              |
| :-------------------- | :--------------------------- | :----------------- |
| dataSourceMap         | Map<String, DataSource>      | 数据源配置           |
| shardingRuleConfig    | ShardingRuleConfiguration    | 数据分片规则配置      |
| props (?)             | Properties                   | 属性配置             |

#### ShardingRuleConfiguration

| *名称*                                     | *数据类型*                                | *说明*                               |
| :---------------------------------------- | :--------------------------------------- | :--------------------------------- |
| tableRuleConfigs                          | Collection<TableRuleConfiguration>       | 分片规则列表                         |
| bindingTableGroups (?)                    | Collection<String>                       | 绑定表规则列表                        |
| broadcastTables (?)	                    | Collection<String>                       | 广播表规则列表                        |
| defaultDataSourceName (?)                 | String	                               | 未配置分片规则的表将根据默认数据源定位    |
| defaultDatabaseShardingStrategyConfig (?)	| ShardingStrategyConfiguration	           | 默认数据库分片策略                     |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration            | 默认分表策略                          |
| defaultKeyGeneratorConfig (?)             | KeyGeneratorConfiguration	               | 默认密钥生成器配置，使用用户定义的或内置的，例如 雪花/UUID。默认密钥生成器是 `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator`|
| masterSlaveRuleConfigs (?)                | Collection<MasterSlaveRuleConfiguration> | 读写分离规则，默认值表示不使用读写分离     |

#### TableRuleConfiguration

| *名称*                              | *数据类型*                         | *说明*                                       |
| :--------------------------------- | :-------------------------------- | :------------------------------------------ |
| logicTable	                     | String	                         | 逻辑表名称                                    |
| actualDataNodes (?)	             | String	                         | 描述数据源名称和实际表，分隔符为点，多个数据节点用逗号分割，支持内联表达式。不存在意味着仅分片数据库。示例：ds${0..7}.tbl${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration     | 数据库分片策略，如果不存在则使用默认的数据库分片策略  |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration     | 表分片策略，如果不存在则使用默认的数据库分片策略     |
| keyGeneratorConfig (?)             | KeyGeneratorConfiguration         | 主键生成器配置，如果不存在则使用默认主键生成器       |
| encryptorConfiguration (?)         | EncryptorConfiguration            | 加密生成器配置                                 |

#### StandardShardingStrategyConfiguration

ShardingStrategyConfiguration 的实现类

| *名称*                      | *数据类型*                   | *说明*                        |
| :------------------------- | :-------------------------- | :--------------------------- |
| shardingColumn             | String	                   | 分片键                        |
| preciseShardingAlgorithm   | PreciseShardingAlgorithm    | `=` 和 `IN` 中使用的精确分片算法 |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm      | `BETWEEN` 中使用的范围分片算法   |

#### ComplexShardingStrategyConfiguration

`ShardingStrategyConfiguration` 的实现类，用于具有多个分片键的复杂分片情况。

| *名称*                 | *数据类型*                      | *说明*             |
| :-------------------- | :----------------------------- | :---------------- |
| shardingColumns       | String	                     | 分片键，以逗号分隔    |
| shardingAlgorithm     | ComplexKeysShardingAlgorithm	 | 复杂分片算法         |

#### InlineShardingStrategyConfiguration

`ShardingStrategyConfiguration` 的实现类，用于行表达式的分片策略。

| *名称*                 | *数据类型*     | *说明*                                                        |
| :-------------------- | :------------ | :----------------------------------------------------------- |
| shardingColumns       | String        | 分片列名，以逗号分隔                                             |
| algorithmExpression   | String        | 行表达式的分片策略，应符合 groovy 语法；有关更多详细信息，请参阅行表达式 |

#### HintShardingStrategyConfiguration

`ShardingStrategyConfiguration` 的实现类，用于配置强制分片策略。

| *名称*                    | *数据类型*                       | *说明*                   |
| :----------------------- | :------------------------------ | :---------------------- |
| shardingAlgorithm	       | HintShardingAlgorithm	         | 强制分片算法              |

##### NoneShardingStrategyConfiguration

`ShardingStrategyConfiguration` 的实现类，用于配置非分片策略。

### 自增主键生成器

| *名称*                   | *数据类型*                      | *说明*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| column                   | String                          | 主键                      |
| type	                   | String	                         | 主键生成器的类型，使用用户定义的或内置的，例如 雪花，UUID |
| props	                   | Properties	                     | 主键生成器的属性配置 |

#### 属性配置

属性配置项，可以是以下属性。

SNOWFLAKE

| *名称*                                        | *数据类型* | *说明*                                                       |
| :--------------------------------------------- | :---------- | :------------------------------------------------------- |
| worker.id (?)                                 | long   | 工作机器唯一 id，默认为 0                                      |
| max.tolerate.time.difference.milliseconds (?) | long   | 最大容忍时钟回退时间，单位：毫秒。默认为 10 毫秒               |
| max.vibration.offset (?)                      | int    | 最大抖动上限值，范围 [0, 4096)，默认为 1。<br/> 注：若使用此算法生成值作分片值，建议配置此属性。<br/> 此算法在不同毫秒内所生成的 key 取模 2^n (2^n 一般为分库或分表数) 之后结果总为 0 或 1。<br/> 为防止上述分片问题，建议将此属性值配置为 (2^n)-1 |

### 读写分离

#### MasterSlaveDataSourceFactory

| *名称*                | *数据类型*                   | *说明*                     |
| :-------------------- | :--------------------------- | :--------------------- |
| dataSourceMap         | Map<String, DataSource>      | 数据源及其名称的映射       |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 读写分离规则配置          |
| props (?)             | Properties                   | 属性配置                 |

#### MasterSlaveRuleConfiguration

| *名称*                   | *数据类型*                      | *说明*                     |
| :----------------------- | :------------------------------ | :---------------------- |
| name                     | String                          | 读写分离数据源名称        |
| masterDataSourceName     | String                          | 主数据库源名称            |
| slaveDataSourceNames     | Collection<String>              | 从数据库源名称列表          |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | 从库负载均衡算法           |

#### 属性配置

属性配置项，可以是以下属性。

| *名称*                             | *数据类型*    | *说明*                                             |
| :--------------------------------- | :---------- | :------------------------------------------------ |
| sql.show (?)                       | boolean     | 是否打印 SQL 日志，默认值：false                      |
| executor.size (?)                  | int         | 用于 SQL 实现的工作线程号；如果为 0，则没有限制。默认值：0 |
| max.connections.size.per.query (?) | int         | 每个物理数据库每次查询分配的最大连接数，默认值：1          |
| check.table.metadata.enabled (?)   | boolean     | 初始化时是否检查元数据的一致性，默认值：false            |

### 数据脱敏

#### EncryptDataSourceFactory

| *名称*                | *数据类型*                   | *说明*                 |
| :-------------------- | :--------------------------- | :------------------ |
| dataSource	        | DataSource                   | 数据源               |
| encryptRuleConfig     | EncryptRuleConfiguration     | 加密规则配置          |
| props (?)             | Properties                   | 属性配置             |

#### EncryptRuleConfiguration

| *名称*                   | *数据类型*                           | *说明*                  |
| :---------------------- | :---------------------------------- | :--------------------- |
| encryptors	| Map<String, EncryptorRuleConfiguration>	    | 加密器名称和加密器        |
| tables	    | Map<String, EncryptTableRuleConfiguration>	| 加密表名和加密表          |

#### 属性配置

属性配置项，可以是以下属性。

| *名称*                             | *数据类型* | *说明*                                    |
| :--------------------------------- | :---------- | :------------------------------------ |
| sql.show (?)                       | boolean     | 是否打印 SQL 日志，默认值：false          |
| query.with.cipher.column (?)	     | boolean     | 有普通列时，是否使用加密列查询，默认值：true |

### 编排

#### OrchestrationShardingDataSourceFactory

| *名称*                 | *数据类型*                    | *说明*                              |
| :-------------------- | :--------------------------- | :--------------------------------- |
| dataSourceMap	        | Map<String, DataSource>	   | 与 `ShardingDataSourceFactory` 相同 |
| shardingRuleConfig	| ShardingRuleConfiguration	   | 与 `ShardingDataSourceFactory` 相同 |
| props (?)	            | Properties	               | 与 `ShardingDataSourceFactory` 相同 |
| orchestrationConfig	| OrchestrationConfiguration   | 编排规则配置                         |

#### OrchestrationMasterSlaveDataSourceFactory

| *名称*                 | *数据类型*                    | *说明*                               |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap	        | Map<String, DataSource>	   | 与 `MasterSlaveDataSourceFactory` 相同 |
| masterSlaveRuleConfig	| MasterSlaveRuleConfiguration | 与 `MasterSlaveDataSourceFactory` 相同 |
| configMap (?)	        | Map<String, Object>	       | 与 `MasterSlaveDataSourceFactory` 相同 |
| props (?)	            | Properties	               | 与 `MasterSlaveDataSourceFactory` 相同  |
| orchestrationConfig	| OrchestrationConfiguration   | 编排规则配置                             |

#### OrchestrationEncryptDataSourceFactory

| *名称*                | *数据类型*                   | *说明*                               |
| :-------------------- | :--------------------------- | :--------------------------------- |
| dataSource	        | DataSource	               | 与 `EncryptDataSourceFactory` 相同  |
| encryptRuleConfig	    | EncryptRuleConfiguration	   | 与 `EncryptDataSourceFactory` 相同  |
| props (?)	            | Properties	               | 与 `EncryptDataSourceFactory` 相同  |
| orchestrationConfig	| OrchestrationConfiguration   | 编排规则配置                         |

#### OrchestrationConfiguration

| *名称*                 | *数据类型*                    | *说明*                              |
| :-------------------- | :--------------------------- | :---------------------------------- |
| instanceConfigurationMap	| Map<String, CenterConfiguration>	| config-center&registry-center 的配置，key 是 center 的名称，value 是 config-center/registry-center |

#### CenterConfiguration

| *名称*                 | *数据类型*                    | *说明*                               |
| :-------------------- | :--------------------------- | :---------------------------------- |
| type              	| String	| 注册中心类型 (zookeeper/etcd/apollo/nacos)                                   |
| properties	        | String	| 注册中心的配置属性，例如zookeeper的配置属性                                             |
| orchestrationType 	| String	| 编排中心的类型：config-center 或 registry-center，如果两者都使用 `setOrchestrationType("registry_center,config_center");` |
| serverLists	        | String	| 注册中心服务列表，包括 IP 地址和端口号，多个地址用逗号分隔，如: host1:2181,host2:2181 |
| namespace (?)         | String	| 命名空间                                                                    |

#### 属性配置

属性配置项，可以是以下属性。

| *名称*         | *数据类型*   | *说明*                                                 |
| :------------ | :---------- | :---------------------------------------------------- |
| overwrite     | boolean     |	本地配置是否覆盖配置中心配置； 如果覆盖，则每次启动都参考本地配置 |

如果注册中心类型是 `zookeeper`，则可以使用以下选项设置属性：

| *名称*                             | *数据类型*   | *说明*                                       |
| :-------------------------------- | :---------- | :------------------------------------------ |
| digest (?)	                    | String | 连接注册中心的权限令牌；默认表示不需要权限               |
| operationTimeoutMilliseconds (?)  | int	 | 操作超时毫秒数，默认为 500 毫秒                       |
| maxRetries (?)                	| int	 | 最大重试次数，默认为 3 次                            |
| retryIntervalMilliseconds (?)     | int	 | 重试间隔毫秒数，默认为 500 毫秒                       |
| timeToLiveSeconds (?)	            | int    | 临时节点的存活时间，默认 60 秒                        |

如果注册中心类型是 `etcd`，则可以使用以下选项设置属性：

| *名称*                             | *数据类型* | *说明*                      |
| :--------------------------------- | :---------- | :---------------------- |
| timeToLiveSeconds (?)	             | long        | etcd TTL 秒，默认为 30 秒 |

如果注册中心类型是 `apollo`，则可以使用以下选项设置属性：

| *名称*                             | *数据类型* | *说明*                           |
| :--------------------------------- | :---------- | :--------------------------- |
| appId (?)              | String   | Apollo appId，默认为 `APOLLO_SHADINGSPHERE`   |
| env (?)                | String   | Apollo env，默认为 `DEV`                      |
| clusterName (?)        | String	| Apollo clusterName，默认为 `default`          |
| administrator (?)      | String	| Apollo administrator，默认为 ``               |
| token (?)              | String	| Apollo token，默认为 ``                       |
| portalUrl (?)          | String	| Apollo portalUrl，默认为 ``                   |
| connectTimeout (?)     | int      | Apollo connectTimeout，默认为 1000 毫秒        |
| readTimeout (?)        | int      | Apollo readTimeout，默认为 5000 毫秒           |

如果注册中心类型是 `nacos`，则可以使用以下选项设置属性：

| *名称*                             | *数据类型* | *说明*                                                |
| :--------------------------------- | :---------- | :------------------------------------------------ |
| group (?)                          | String      | Nacos 组，默认为 `SHADING_SPHERE_DEFAULT_GROUP`     |
| timeout (?)                        | long        | Nacos 超时时间，默认为 3000 毫秒                      |

## ShardingSphere-3.x

### 数据分片

#### ShardingDataSourceFactory

| *名称*                | *数据类型*                   | *说明*                 |
| :-------------------- | :--------------------------- | :------------------ |
| dataSourceMap         | Map<String, DataSource>      | 数据源配置            |
| shardingRuleConfig    | ShardingRuleConfiguration    | 数据分片规则配置       |
| configMap (?)	        | Map<String, Object>	       | 用户自定义的参数       |
| props (?)             | Properties                   | 属性配置             |

#### ShardingRuleConfiguration

| *名称*                                     | *数据类型*                                | *说明*                                |
| :---------------------------------------- | :--------------------------------------- | :----------------------------------- |
| tableRuleConfigs                          | Collection<TableRuleConfiguration>       | 分片规则列表                           |
| bindingTableGroups (?)                    | Collection<String>                       | 绑定表规则列表                          |
| broadcastTables (?)	                    | Collection<String>                       | 广播表规则列表                          |
| defaultDataSourceName (?)                 | String	                               | 未配置分片规则的表将根据默认数据源定位       |
| defaultDatabaseShardingStrategyConfig (?)	| ShardingStrategyConfiguration	           | 默认数据库分片策略                       |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration            | 默认分表策略                            |
| defaultKeyGeneratorConfig (?)             | KeyGeneratorConfiguration	               | 默认密钥生成器配置，使用用户定义的或内置的，例如 雪花/UUID。默认密钥生成器是 `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator`|
| masterSlaveRuleConfigs (?)                | Collection<MasterSlaveRuleConfiguration> | 读写分离规则，默认值表示不使用读写分离       |

#### TableRuleConfiguration

| *名称*                              | *数据类型*                     | *说明*                                       |
| :--------------------------------- | :---------------------------- | :------------------------------------------ |
| logicTable	                     | String	                     | 逻辑表名称                                    |
| actualDataNodes (?)	             | String	                     | 描述数据源名称和实际表，分隔符为点，多个数据节点用逗号分割，支持内联表达式。不存在意味着仅分片数据库。示例：ds${0..7}.tbl${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | 数据库分片策略，如果不存在则使用默认的数据库分片策略  |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | 表分片策略，如果不存在则使用默认的数据库分片策略     |
| logicIndex (?)	                 | String	                     | 逻辑索引，如果在 Oracle/PostgreSQL 中使用 DROP INDEX XXX SQL，则需要设置此属性以查找实际表 |
| keyGeneratorConfig (?)             | String                        | 主键列配置，如果不存在则使用默认主键列              |
| keyGenerator (?)	                 | KeyGenerator	                 | 主键生成器配置，如果不存在则使用默认主键生成器         |

#### StandardShardingStrategyConfiguration

ShardingStrategyConfiguration 的实现类

| *名称*                      | *数据类型*                     | *说明*                        |
| :------------------------- | :---------------------------- | :--------------------------- |
| shardingColumn             | String	                     | 分片键                        |
| preciseShardingAlgorithm   | PreciseShardingAlgorithm      | `=` 和 `IN` 中使用的精确分片算法 |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm        | `BETWEEN` 中使用的范围分片算法   |

##### ComplexShardingStrategyConfiguration

`ShardingStrategyConfiguration` 的实现类，用于具有多个分片键的复杂分片策略。

| *名称*                   | *数据类型*                    | *说明*              |
| :---------------------- | :--------------------------- | :----------------- |
| shardingColumns	      | String	                     | 分片键，以逗号分隔    |
| shardingAlgorithm	      | ComplexKeysShardingAlgorithm | 复杂分片算法         |

##### InlineShardingStrategyConfiguration

`ShardingStrategyConfiguration` 的实现类，用于行表达式的分片策略。

| *名称*                     | *数据类型*                      | *说明*                              |
| :------------------------ | :----------------------------- | :--------------------------------- |
| shardingColumns	        | String	                     | 分片列名，以逗号分隔                   |
| algorithmExpression	    | String                         | 行表达式的分片策略，应符合 groovy 语法；有关更多详细信息，请参阅行表达式 |

##### HintShardingStrategyConfiguration

`ShardingStrategyConfiguration` 的实现类，用于配置强制分片策略。

| *名称*                    | *数据类型*                       | *说明*                        |
| :----------------------- | :------------------------------ | :--------------------------- |
| shardingAlgorithm	       | HintShardingAlgorithm	         | 强制分片算法                   |

##### NoneShardingStrategyConfiguration

`ShardingStrategyConfiguration` 的实现类，用于配置非分片策略。

#### 属性配置

枚举属性

| *名称*                                 | *数据类型* | *说明*                                             |
| :------------------------------------ | :-------- | :------------------------------------------------ |
| sql.show (?)                          | boolean	| 是否打印 SQL 日志，默认值：false                      |
| executor.size (?)                     | int	    | 用于 SQL 实现的工作线程号；如果为 0，则没有限制。默认值：0 |
| max.connections.size.per.query (?)	| int       | 每个物理数据库每次查询分配的最大连接数，默认值：1          |
| check.table.metadata.enabled (?)	    | boolean   | 初始化时是否检查元数据的一致性，默认值：false             |

#### configMap

用户定义的参数。

### 读写分离

#### MasterSlaveDataSourceFactory

| *名称*                | *数据类型*                     | *说明*                  |
| :-------------------- | :--------------------------- | :--------------------- |
| dataSourceMap         | Map<String, DataSource>      | 数据源及其名称的映射       |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 读写分离规则配置          |
| configMap (?)         | Map<String, Object>          | 用户自定义的参数          |
| props (?)             | Properties                   | 属性配置                 |

#### MasterSlaveRuleConfiguration

| *名称*                   | *数据类型*                      | *说明*                        |
| :----------------------- | :------------------------------ | :---------------------- |
| name                     | String                          | 读写分离数据源名称        |
| masterDataSourceName     | String                          | 主数据库源名称            |
| slaveDataSourceNames     | Collection<String>              | 从数据库源名称列表          |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | 从库负载均衡算法           |

#### configMap

用户定义的参数。

#### PropertiesConstant

枚举属性。

| *名称*                                 | *数据类型*  | *说明*                                            |
| :------------------------------------ | :-------- | :------------------------------------------------ |
| sql.show (?)                          | boolean	| 是否打印 SQL 日志，默认值：false                      |
| executor.size (?)                     | int	    | 用于 SQL 实现的工作线程号；如果为 0，则没有限制。默认值：0 |
| max.connections.size.per.query (?)	| int       | 每个物理数据库每次查询分配的最大连接数，默认值：1          |
| check.table.metadata.enabled (?)	    | boolean   | 初始化时是否检查元数据的一致性，默认值：false             |

### 编排

#### OrchestrationShardingDataSourceFactory

| *名称*                | *数据类型*                   | *说明*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap	        | Map<String, DataSource>	   | 与 `ShardingDataSourceFactory` 相同 |
| shardingRuleConfig	| ShardingRuleConfiguration	   | 与 `ShardingDataSourceFactory` 相同 |
| configMap (?)	        | Map<String, Object>          | 与 `ShardingDataSourceFactory` 相同 |
| props (?)	            | Properties	               | 与 `ShardingDataSourceFactory` 相同 |
| orchestrationConfig	| OrchestrationConfiguration   | 编排规则配置                          |

#### OrchestrationMasterSlaveDataSourceFactory

| *名称*                | *数据类型*                   | *说明*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap	        | Map<String, DataSource>	   | 与 `MasterSlaveDataSourceFactory` 相同 |
| masterSlaveRuleConfig	| MasterSlaveRuleConfiguration | 与 `MasterSlaveDataSourceFactory` 相同 |
| configMap (?)	        | Map<String, Object>	       | 与 `MasterSlaveDataSourceFactory` 相同 |
| props (?)	            | Properties	               | 与 `MasterSlaveDataSourceFactory` 相同  |
| orchestrationConfig	| OrchestrationConfiguration   | 编排规则配置                             |

#### OrchestrationConfiguration

| *名称*                | *数据类型*                     | *说明*                                                 |
| :-------------------- | :--------------------------- | :----------------------------------------------------- |
| name	                | String	                   | 编排实例名称                                             |
| overwrite	            | boolean	                   | 本地配置是否覆盖配置中心配置； 如果覆盖，则每次启动都参考本地配置 |
| regCenterConfig	    | RegistryCenterConfiguration  | 注册中心配置                                             |

#### RegistryCenterConfiguration

| *名称*                             | *数据类型*                    | *说明*                               |
| :-------------------------------- | :--------------------------- | :---------------------------------- |
| serverLists	                    | String	| 注册中心服务列表，包括 IP 地址和端口号，多个地址用逗号分隔，如: host1:2181,host2:2181 |
| namespace (?)	                    | String	| 命名空间                                                                    |
| digest (?)	                    | String	| 连接注册中心的权限令牌；默认表示不需要权限                                       |
| operationTimeoutMilliseconds (?)	| int	    | 操作超时毫秒数，默认为 500 毫秒                                                 |
| maxRetries (?)	                | int	    | 最大重试次数，默认为 3 次                                                      |
| retryIntervalMilliseconds (?)	    | int	    | 重试间隔毫秒数，默认为 500 毫秒                                                |
| timeToLiveSeconds (?)	            | int	    | 临时节点的存活时间，默认 60 秒                                                 |

## ShardingSphere-2.x

### 读写分离

#### 概念

为了缓解数据库压力，将写入和读取操作分离为不同数据源，写库称为主库，读库称为从库，一主库可配置多从库。

#### 支持项

1. 提供了一主多从的读写分离配置，可独立使用，也可配合分库分表使用。
2. 独立使用读写分离支持 SQL 透传。
3. 同一线程且同一数据库连接内，如有写入操作，以后的读操作均从主库读取，用于保证数据一致性。
4. Spring 命名空间。
5. 基于 Hint 的强制主库路由。

#### 不支持项

1. 主库和从库的数据同步。
2. 主库和从库的数据同步延迟导致的数据不一致。
3. 主库双写或多写。

#### 代码开发示例

##### 读写分离

```java
// 构造一个读写分离数据源，读写分离数据源实现了 DataSource 接口，可以直接作为数据源进行处理。 masterDataSource、slaveDataSource0、slaveDataSource1 等都是使用 DBCP 等连接池配置的真实数据源。
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("masterDataSource", masterDataSource);
dataSourceMap.put("slaveDataSource0", slaveDataSource0);
dataSourceMap.put("slaveDataSource1", slaveDataSource1);

// 构建读写分离配置
MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
masterSlaveRuleConfig.setName("ms_ds");
masterSlaveRuleConfig.setMasterDataSourceName("masterDataSource");
masterSlaveRuleConfig.getSlaveDataSourceNames().add("slaveDataSource0");
masterSlaveRuleConfig.getSlaveDataSourceNames().add("slaveDataSource1");

DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig);
```

##### 分库分表 + 读写分离

```java
// 构造一个读写分离数据源，读写分离数据源实现了 DataSource 接口，可以直接作为数据源进行处理。 masterDataSource、slaveDataSource0、slaveDataSource1 等都是使用 DBCP 等连接池配置的真实数据源。
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("masterDataSource0", masterDataSource0);
dataSourceMap.put("slaveDataSource00", slaveDataSource00);
dataSourceMap.put("slaveDataSource01", slaveDataSource01);

dataSourceMap.put("masterDataSource1", masterDataSource1);
dataSourceMap.put("slaveDataSource10", slaveDataSource10);
dataSourceMap.put("slaveDataSource11", slaveDataSource11);

// 构建读写分离配置
MasterSlaveRuleConfiguration masterSlaveRuleConfig0 = new MasterSlaveRuleConfiguration();
masterSlaveRuleConfig0.setName("ds_0");
masterSlaveRuleConfig0.setMasterDataSourceName("masterDataSource0");
masterSlaveRuleConfig0.getSlaveDataSourceNames().add("slaveDataSource00");
masterSlaveRuleConfig0.getSlaveDataSourceNames().add("slaveDataSource01");

MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
masterSlaveRuleConfig1.setName("ds_1");
masterSlaveRuleConfig1.setMasterDataSourceName("masterDataSource1");
masterSlaveRuleConfig1.getSlaveDataSourceNames().add("slaveDataSource10");
masterSlaveRuleConfig1.getSlaveDataSourceNames().add("slaveDataSource11");

// 继续通过 ShardingSlaveDataSourceFactory 创建 ShardingDataSource
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig0);
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig1);

DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

## ShardingSphere-1.x

### 读写分离

#### 概念

为了缓解数据库压力，将写入和读取操作分离为不同数据源，写库称为主库，读库称为从库，一主库可配置多从库。

#### 支持项

1. 提供了一主多从的读写分离配置，可独立使用，也可配合分库分表使用。
2. 同一线程且同一数据库连接内，如有写入操作，以后的读操作均从主库读取，用于保证数据一致性。
3. Spring 命名空间。
4. 基于 Hint 的强制主库路由。

#### 不支持项

1. 主库和从库的数据同步。
2. 主库和从库的数据同步延迟导致的数据不一致。
3. 主库双写或多写。

#### 代码开发示例

```java
// 构造一个读写分离数据源，读写分离数据源实现了 DataSource 接口，可以直接作为数据源进行处理。 masterDataSource、slaveDataSource0、slaveDataSource1 等都是使用 DBCP 等连接池配置的真实数据源。
Map<String, DataSource> slaveDataSourceMap0 = new HashMap<>();
slaveDataSourceMap0.put("slaveDataSource00", slaveDataSource00);
slaveDataSourceMap0.put("slaveDataSource01", slaveDataSource01);
// You can choose the master-slave library load balancing strategy, the default is ROUND_ROBIN, and there is RANDOM to choose from, or customize the load strategy
DataSource masterSlaveDs0 = MasterSlaveDataSourceFactory.createDataSource("ms_0", "masterDataSource0", masterDataSource0, slaveDataSourceMap0, MasterSlaveLoadBalanceStrategyType.ROUND_ROBIN);

Map<String, DataSource> slaveDataSourceMap1 = new HashMap<>();
slaveDataSourceMap1.put("slaveDataSource10", slaveDataSource10);
slaveDataSourceMap1.put("slaveDataSource11", slaveDataSource11);
DataSource masterSlaveDs1 = MasterSlaveDataSourceFactory.createDataSource("ms_1", "masterDataSource1", masterDataSource1, slaveDataSourceMap1, MasterSlaveLoadBalanceStrategyType.ROUND_ROBIN);

// 构建读写分离配置
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("ms_0", masterSlaveDs0);
dataSourceMap.put("ms_1", masterSlaveDs1);
```
