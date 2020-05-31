+++
pre = "<b>3.9.2. </b>"
title = "SPI 列表"
weight = 2
chapter = true
+++

## SQL 解析

### SQLParserConfiguration

| *SPI 名称*                    | *详细说明*                                        |
| ----------------------------- | ------------------------------------------------ |
| SQLParserConfiguration        | 用于规定用于解析 SQL 的 ANTLR 语法文件及其语法树访问器 |

| *已知实现类*                   | *详细说明*                                        |
| ----------------------------- | ------------------------------------------------ |
| MySQLParserConfiguration      | 基于 MySQL 的SQL 解析器实现                        |
| PostgreSQLParserConfiguration | 基于 PostgreSQL 的SQL 解析器实现                   |
| SQLServerParserConfiguration  | 基于 SQLServer 的SQL 解析器实现                    |
| OracleParserConfiguration     | 基于 Oracle 的SQL 解析器实现                       |
| SQL92ParserConfiguration      | 基于 SQL92 的SQL 解析器实现                        |

### ParsingHook

| *SPI 名称*             | *详细说明*                            |
| ---------------------- | ------------------------------------ |
| ParsingHook            | 用于SQL 解析过程追踪                   |

| *已知实现类*            | *详细说明*                            |
| ---------------------- | ------------------------------------ |
| OpenTracingParsingHook | 使用 OpenTracing 协议追踪 SQL 解析过程 |

## 数据库协议

### DatabaseProtocolFrontendEngine

| *SPI 名称*                       | *详细说明*                                      |
| ------------------------------- | ---------------------------------------------- |
| DatabaseProtocolFrontendEngine  | 用于ShardingSphere-Proxy解析与适配访问数据库的协议 |

| *已知实现类*                      | *详细说明*                                      |
| -------------------------------- | ---------------------------------------------- |
| MySQLProtocolFrontendEngine      | 基于 MySQL 的数据库协议实现                      |
| PostgreSQLProtocolFrontendEngine | 基于 PostgreSQL 的SQL 解析器实现                 |

## 配置

### ShardingSphereRuleBuilder

| *SPI 名称*                | *详细说明*                               |
| ------------------------- | -------------------------------------- |
| ShardingSphereRuleBuilder | 用于将用户配置转化为规则对象               |

| *已知实现类*               | *详细说明*                               |
| ------------------------- | --------------------------------------- |
| ShardingRuleBuilder       | 用于将分片用户配置转化为分片规则对象        |
| MasterSlaveRuleBuilder    | 用于将读写分离用户配置转化为读写分离规则对象 |
| ReplicaRuleBuilder        | 用于将多副本用户配置转化为多副本规则对象     |
| EncryptRuleBuilder        | 用于将加密用户配置转化为加密规则对象        |
| ShadowRuleBuilder         | 用于将影子库用户配置转化为影子库规则对象     |

### YamlRuleConfigurationSwapper

| *SPI 名称*                              | *详细说明*                                   |
| --------------------------------------- | ------------------------------------------ |
| YamlRuleConfigurationSwapper            | 用于将 YAML 配置转化为标准用户配置             |

| *已知实现类*                             | *详细说明*                                   |
| --------------------------------------- | ------------------------------------------- |
| ShardingRuleConfigurationYamlSwapper    | 用于将分片的 YAML 配置转化为分片标准配置        |
| MasterSlaveRuleConfigurationYamlSwapper | 用于将读写分离的 YAML 配置转化为读写分离标准配置 |
| ReplicaRuleConfigurationYamlSwapper     | 用于将多副本的 YAML 分片配置转化为多副本标准配置 |
| EncryptRuleConfigurationYamlSwapper     | 用于将加密的 YAML 分片配置转化为加密标准配置     |
| ShadowRuleConfigurationYamlSwapper      | 用于将影子库的 YAML 分片配置转化为影子库标准配置 |

### ShardingSphereYamlConstruct

| *SPI 名称*                                     | *详细说明*                        |
| ---------------------------------------------- | ------------------------------- |
| ShardingSphereYamlConstruct                    | 用于将定制化对象和 YAML 相互转化    |

| *已知实现类*                                    | *详细说明*                        |
| ---------------------------------------------- | -------------------------------- |
| NoneShardingStrategyConfigurationYamlConstruct | 用于将不分片策略对象和 YAML 相互转化 |

## 内核

### DatabaseType

| *SPI 名称*             | *详细说明*                |
| ---------------------- | ------------------------ |
| DatabaseType           | 支持的数据库类型           |

| *已知实现类*            | *详细说明*                |
| ---------------------- | ------------------------ |
| SQL92DatabaseType      | 遵循 SQL92 标准的数据库类型 |
| MySQLDatabaseType      | MySQL 数据库              |
| MariaDBDatabaseType    | MariaDB 数据库            |
| PostgreSQLDatabaseType | PostgreSQL 数据库         |
| OracleDatabaseType     | Oracle 数据库             |
| SQLServerDatabaseType  | SQLServer 数据库          |
| H2DatabaseType         | H2 数据库                 |

### RuleMetaDataLoader

| *SPI 名称*             | *详细说明*         |
| ---------------------- | ----------------- |
| RuleMetaDataLoader     | 用于元数据初始化    |

| *已知实现类*            | *详细说明*         |
| ---------------------- | ----------------- |
| ShardingMetaDataLoader | 用于分片元数据初始化 |
| EncryptMetaDataLoader  | 用于加密元数据初始化 |

### RuleMetaDataDecorator

| *SPI 名称*                | *详细说明*        |
| ------------------------ | ---------------- |
| RuleMetaDataDecorator    | 用于元数据更新     |

| *已知实现类*               | *详细说明*        |
| ------------------------- | ---------------- |
| ShardingMetaDataDecorator | 用于分片元数据更新 |
| EncryptMetaDataDecorator  | 用于加密元数据更新 |

### RouteDecorator

| *SPI 名称*                | *详细说明*              |
| ------------------------- | --------------------- |
| RouteDecorator            | 用于处理路由结果        |

| *已知实现类*               | *详细说明*             |
| ------------------------- | --------------------- |
| ShardingRouteDecorator    | 用于处理分片路由结果     |
| MasterSlaveRouteDecorator | 用于处理读写分离路由结果 |
| ReplicaRouteDecorator     | 用于处理多副本路由结果   |
| ShadowRouteDecorator      | 用于处理影子库路由结果   |

### SQLRewriteContextDecorator

| *SPI 名称*                         | *详细说明*                 |
| ---------------------------------- | ------------------------- |
| SQLRewriteContextDecorator         | 用于处理 SQL 改写结果       |

| *已知实现类*                        | *详细说明*                 |
| ---------------------------------- | ------------------------- |
| ShardingSQLRewriteContextDecorator | 用于处理分片 SQL 改写结果   |
| EncryptSQLRewriteContextDecorator  | 用于处理加密 SQL 改写结果   |
| ShadowSQLRewriteContextDecorator   | 用于处理影子库 SQL 改写结果 |

### ExecuteGroupDecorator

| *SPI 名称*                   | *详细说明*             |
| ---------------------------- | --------------------- |
| ExecuteGroupDecorator        | 用于修改数据节点分组结果 |

| *已知实现类*                  | *详细说明*             |
| ---------------------------- | --------------------- |
| ReplicaExecuteGroupDecorator | 用于多副本数据节点分组   |

### SQLExecutionHook

| *SPI 名称*                     | *详细说明*                        |
| ----------------------------- | --------------------------------- |
| SQLExecutionHook              | SQL执行过程监听器 |

| *已知实现类*                   | *详细说明*                         |
| ----------------------------- | --------------------------------- |
| TransactionalSQLExecutionHook | 基于事务的SQL执行过程监听器          |
| OpenTracingSQLExecutionHook   | 基于 OpenTracing 的SQL执行过程监听器 |

### ResultProcessEngine

| *SPI 名称*                   | *详细说明*           |
| ---------------------------- | ------------------- |
| ResultProcessEngine          | 用于处理结果集        |

| *已知实现类*                  | *详细说明*           |
| ---------------------------- | ------------------- |
| ShardingResultMergerEngine   | 用于处理分片结果集归并 |
| EncryptResultDecoratorEngine | 用于处理加密结果集改写 |

## 数据分片

### ShardingAlgorithm

| *SPI 名称*                       | *详细说明*                   |
| ------------------------------- | ---------------------------- |
| ShardingAlgorithm               | 分片算法                      |

| *已知实现类*                     | *详细说明*                    |
| ------------------------------- | ---------------------------- |
| InlineShardingAlgorithm         | 基于行表达式的分片算法          |
| ModuloShardingAlgorithm         | 基于取模的分片算法             |
| HashShardingAlgorithm           | 基于哈希取模的分片算法          |
| DatetimeShardingAlgorithm       | 基于时间的分片算法             |
| CustomDateTimeShardingAlgorithm | 基于用户自定义时间格式的分片算法 |
| StandardRangeShardingAlgorithm  | 基于范围的分片算法             |
| CustomRangeShardingAlgorithm    | 基于用户自定义范围的分片算法     |

### KeyGenerateAlgorithm

| *SPI 名称*                    | *详细说明*                    |
| ----------------------------- | ---------------------------- |
| KeyGenerateAlgorithm          | 分布式主键生成算法             |

| *已知实现类*                   | *详细说明*                    |
| ----------------------------- | ---------------------------- |
| SnowflakeKeyGenerateAlgorithm | 基于雪花算法的分布式主键生成算法 |
| UUIDKeyGenerateAlgorithm      | 基于UUID的分布式主键生成算法    |

### TimeService

| *SPI 名称*                  | *详细说明*                   |
| --------------------------- | --------------------------- |
| TimeService                 | 获取当前时间进行路由           |

| *已知实现类*                 | *详细说明*                       |
| --------------------------- | ------------------------------- |
| DefaultTimeService          | 从应用系统时间中获取当前时间进行路由 |
| DatabaseTimeServiceDelegate | 从数据库中获取当前时间进行路由      |

### DatabaseSQLEntry

| *SPI 名称*                 | *详细说明*                          |
| -------------------------- | ---------------------------------- |
| DatabaseSQLEntry           | 获取当前时间的数据库方言              |

| *已知实现类*                | *详细说明*                          |
| -------------------------- | ---------------------------------- |
| MySQLDatabaseSQLEntry      | 从 MySQL 获取当前时间的数据库方言     |
| PostgreSQLDatabaseSQLEntry | 从 PostgreSQL 获取当前时间的数据库方言|
| OracleDatabaseSQLEntry     | 从 Oracle 获取当前时间的数据库方言    |
| SQLServerDatabaseSQLEntry  | 从 SQLServer 获取当前时间的数据库方言 |

## 读写分离

### MasterSlaveLoadBalanceAlgorithm

| *SPI 名称*                                 | *详细说明*              |
| ----------------------------------------- | ----------------------- |
| MasterSlaveLoadBalanceAlgorithm           | 读库负载均衡算法          |

| *已知实现类*                               | *详细说明*               |
| ----------------------------------------- | ----------------------- |
| RoundRobinMasterSlaveLoadBalanceAlgorithm | 基于轮询的读库负载均衡算法 |
| RandomMasterSlaveLoadBalanceAlgorithm     | 基于随机的读库负载均衡算法 |

## 数据加密

### Encryptor

| *SPI 名称*    | *详细说明*            |
| ------------ | --------------------- |
| Encryptor    | 数据加密算法           |

| *已知实现类*  | *详细说明*             |
| ------------ | --------------------- |
| MD5Encryptor | 基于 MD5 的数据加密算法 |
| AESEncryptor | 基于 AES 的数据加密算法 |
| RC4Encryptor | 基于 RC4 的数据加密算法 |

### QueryAssistedEncryptor

| *SPI 名称*             | *详细说明*                 |
| ---------------------- | ------------------------ |
| QueryAssistedEncryptor | 包含查询辅助列的数据加密算法 |

| *已知实现类*            | *详细说明*                 |
| ---------------------- | ------------------------- |
| 无                     |                           |

## 分布式事务

### ShardingTransactionManager

| *SPI 名称*                         | *详细说明*                 |
| --------------------------------- | -------------------------- |
| ShardingTransactionManager        | 分布式事务管理器             |

| *已知实现类*                       | *详细说明*                  |
| --------------------------------- | -------------------------- |
| XAShardingTransactionManager      | 基于 XA 的分布式事务管理器    |
| SeataATShardingTransactionManager | 基于 Seata 的分布式事务管理器 |

### XATransactionManager

| *SPI 名称*                   | *详细说明*                         |
| ---------------------------- | --------------------------------- |
| XATransactionManager         | XA分布式事务管理器                  |

| *已知实现类*                  | *详细说明*                         |
| ---------------------------- | --------------------------------- |
| AtomikosTransactionManager   | 基于 Atomikos 的 XA 分布式事务管理器 |
| NarayanaXATransactionManager | 基于 Narayana 的 XA 分布式事务管理器 |
| BitronixXATransactionManager | 基于 Bitronix 的 XA 分布式事务管理器 |

### XADataSourceDefinition

| *SPI 名称*                        | *详细说明*                                                |
| -------------------------------- | --------------------------------------------------------- |
| XADataSourceDefinition           | 非 XA 数据源自动转化为 XA 数据源                             |

| *已知实现类*                      | *详细说明*                                                 |
| -------------------------------- | --------------------------------------------------------- |
| MySQLXADataSourceDefinition      | 非 XA 的 MySQL 数据源自动转化为 XA 的 MySQL 数据源           |
| MariaDBXADataSourceDefinition    | 非 XA 的 MariaDB 数据源自动转化为 XA 的 MariaDB 数据源       |
| PostgreSQLXADataSourceDefinition | 非 XA 的 PostgreSQL 数据源自动转化为 XA 的 PostgreSQL 数据源 |
| OracleXADataSourceDefinition     | 非 XA 的 Oracle 数据源自动转化为 XA 的 Oracle 数据源         |
| SQLServerXADataSourceDefinition  | 非 XA 的 SQLServer 数据源自动转化为 XA 的 SQLServer 数据源   |
| H2XADataSourceDefinition         | 非 XA 的 H2 数据源自动转化为 XA 的 H2 数据源                 |

### DataSourcePropertyProvider

| *SPI 名称*                  | *详细说明*                       |
| -------------------------- | ------------------------------- |
| DataSourcePropertyProvider | 用于获取数据源连接池的标准属性      |

| *已知实现类*                | *详细说明*                       |
| -------------------------- 
| ------------------------------- |
| HikariCPPropertyProvider   | 用于获取 HikariCP 连接池的标准属性 |

## 分布式治理

### ConfigCenterRepository

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| ConfigCenterRepository           | 配置中心                 |

| *已知实现类*                      | *详细说明*               |
| -------------------------------- | ----------------------- |
| CuratorZookeeperCenterRepository | 基于 ZooKeeper 的配置中心 |
| EtcdCenterRepository             | 基于 Etcd 的配置中心      |
| NacosCenterRepository            | 基于 Nacos 的配置中心     |
| ApolloCenterRepository           | 基于 Apollo 的配置中心    |

### RegistryCenterRepository

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| RegistryCenterRepository         | 注册中心                 |

| *已知实现类*                      | *详细说明*               |
| -------------------------------- | ----------------------- |
| CuratorZookeeperCenterRepository | 基于 ZooKeeper 的注册中心 |
| EtcdCenterRepository             | 基于 Etcd 的注册中心      |

### RootInvokeHook

| *SPI 名称*                 | *详细说明*                           |
| ------------------------- | ------------------------------------ |
| RootInvokeHook            | 请求调用入口追踪                       |

| *已知实现类*               | *详细说明*                            |
| ------------------------- | ------------------------------------ |
| OpenTracingRootInvokeHook | 基于 OpenTracing 协议的请求调用入口追踪 |

### MetricsTrackerManager

| *SPI 名称*                      | *详细说明*                    |
| ------------------------------- | --------------------------- |
| MetricsTrackerManager           | 度量指标追踪                  |

| *已知实现类*                     | *详细说明*                    |
| ------------------------------- | ---------------------------- |
| PrometheusMetricsTrackerManager | 基于 Prometheus 的度量指标追踪 |

## 弹性伸缩

| *SPI 名称*             | *详细说明*                    |
| ---------------------- | ---------------------------- |
| ScalingEntry           | 弹性伸缩入口                  |

| *已知实现类*            | *详细说明*                    |
| ---------------------- | ---------------------------- |
| MySQLScalingEntry      | 基于 MySQL 的弹性伸缩入口      |
| PostgreSQLScalingEntry | 基于 PostgreSQL 的弹性伸缩入口 |