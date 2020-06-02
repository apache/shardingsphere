+++
title = "Java API Configuration"
weight = 1
+++

## User API

DataSource factory of Apache ShardingSphere. 

| *Name*             | *DataType*                | *Description*                    |
| ------------------ | ------------------------- | -------------------------------- |
| dataSourceMap      | Map\<String, DataSource\> | Data sources configuration       |
| shardingRuleConfig | ShardingRuleConfiguration | Data sharding configuration rule |
| props (?)          | Properties                | Property configuration           |

## Configuration Item Explanation

### Data Sharding

#### Root Configuration

Class name: ShardingRuleConfiguration

Attributes:

| *Name*                                    | *DataType*                                 | *Description*                                                |
| ----------------------------------------- | ------------------------------------------ | ------------------------------------------------------------ |
| tableRuleConfigs                          | Collection\<TableRuleConfiguration\>       | Sharding rule list                                           |
| bindingTableGroups (?)                    | Collection\<String\>                       | Binding table rule list                                      |
| broadcastTables (?)                       | Collection\<String\>                       | Broadcast table rule list                                    |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration              | Default database sharding strategy                           |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration              | Default table sharding strategy                              |
| defaultKeyGeneratorConfig (?)             | KeyGeneratorConfiguration                  | Default key generator configuration, use user-defined ones or built-in ones, e.g. SNOWFLAKE/UUID. Default key generator is `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator` |

#### Logic Table Configuration

Class name: TableRuleConfiguration

Attributes:

| *Name*                             | *DataType*                    | *Description*                                                |
| ---------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| logicTable                         | String                        | Name of logic table                                          |
| actualDataNodes (?)                | String                        | Describe data source names and actual tables, delimiter as point, multiple data nodes split by comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | Databases sharding strategy, use default databases sharding strategy if absent |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | Tables sharding strategy, use default databases sharding strategy if absent |
| keyGeneratorConfig (?)             | KeyGeneratorConfiguration     | Key generator configuration, use default key generator if absent |

#### Sharding Strategy Configuration

##### Standard Sharding Strategy Configuration

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

##### Complex Sharding Strategy Configuration

Class name: ComplexShardingStrategyConfiguration

Attributes:

| *Name*            | *DataType*                   | *Description*                             |
| ----------------- | ---------------------------- | ----------------------------------------- |
| shardingColumns   | String                       | Sharding column name, separated by commas |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | Complex sharding algorithm                |

##### Hint Sharding Strategy Configuration

Class name: HintShardingStrategyConfiguration

Attributes:

The implementation class of `ShardingStrategyConfiguration`,  used to configure hint sharding strategies.

| *Name*            | *DataType*            | *Description*           |
| ----------------- | --------------------- | ----------------------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint sharding algorithm |

##### None Sharding Strategy Configuration

Class name: NoneShardingStrategyConfiguration

#### KeyGeneratorConfiguration

| *Name* | *DataType* | *Description*                                                |
| ------ | ---------- | ------------------------------------------------------------ |
| column | String     | Column name of key generator                                 |
| type   | String     | Type of key generator, use user-defined ones or built-in ones, e.g. SNOWFLAKE, UUID |
| props  | Properties | The Property configuration of key generators                 |

#### Properties

Property configuration that can include these properties of these key generators.

##### SNOWFLAKE

| *Name*                                              | *DataType* | *Description*                                                                                                                                                                                                                   |
| --------------------------------------------------- | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| worker.id (?)                                        |   long     | The unique id for working machine, the default value is `0`                                                                                                                                                                    |
| max.tolerate.time.difference.milliseconds (?)        |   long     | The max tolerate time for different server's time difference in milliseconds, the default value is `10`                                                                                                                         |
| max.vibration.offset (?)                             |    int     | The max upper limit value of vibrate number, range `[0, 4096)`, the default value is `1`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1` |

#### EncryptRuleConfiguration

| *Name*              | *DataType*                                  | *Description*                                                                  |
| ------------------- | ------------------------------------------- | ------------------------------------------------------------------------------ |
| encryptors          | Map<String, EncryptorRuleConfiguration>     | Encryptor names and encryptors                                                 |
| tables              | Map<String, EncryptTableRuleConfiguration>  | Encrypt table names and encrypt tables                                         |

#### EncryptorRuleConfiguration

| *Name*              | *DataType*                   | *Description*                                                                               |
| ------------------- | ---------------------------- | ------------------------------------------------------------------------------------------- |
| type                | String                       | Type of encryptor，use user-defined ones or built-in ones, e.g. MD5/AES                      |
| properties          | Properties                   | Properties, Notice: when use AES encryptor, `aes.key.value` for AES encryptor need to be set |

#### EncryptTableRuleConfiguration

| *Name*              | *DataType*                                   | *Description*                              |
| ------------------- | -------------------------------------------- | ------------------------------------------ |
| tables              | Map<String, EncryptColumnRuleConfiguration>  | Encrypt column names and encrypt column    |

#### EncryptColumnRuleConfiguration

| *Name*              | *DataType*                   | *Description*                                                                                         |
| ------------------- | ---------------------------- |  ---------------------------------------------------------------------------------------------------- |
| plainColumn         | String                       | Plain column name                                                                                     |
| cipherColumn        | String                       | Cipher column name                                                                                    |
| assistedQueryColumn | String                       | AssistedColumns for query，when use ShardingQueryAssistedEncryptor, it can help query encrypted data  |
| encryptor           | String                       | Encryptor name                                                                                        |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                             | *DataType* | *Description*                                                |
| ---------------------------------- | ---------- | ------------------------------------------------------------ |
| sql.show (?)                       | boolean    | Show SQL or not, default value: false                        |
| executor.size (?)                  | int        | Work thread number, default value: CPU core number           |
| max.connections.size.per.query (?) | int        | The maximum connection number allocated by each query of each physical database. default value: 1 |
| check.table.metadata.enabled (?)   | boolean    | Check meta-data consistency or not in initialization, default value: false                        |
| query.with.cipher.column (?)       | boolean    | When there is a plainColumn, use cipherColumn or not to query, default value: true                |
| allow.range.query.with.inline.sharding (?)    | boolean   | Allow or not execute range query with inline sharding strategy, default value: false        |

### Read-Write Split

#### MasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Description*                       |
| --------------------- | ---------------------------- | ----------------------------------- |
| dataSourceMap         | Map\<String, DataSource\>    | Mapping of data source and its name |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Master slave rule configuration     |
| props (?)             | Properties                   | Property configurations             |

#### MasterSlaveRuleConfiguration

| *Name*                   | *DataType*                      | *Description*                     |
| ------------------------ | ------------------------------- | --------------------------------- |
| name                     | String                          | Read-write split data source name |
| masterDataSourceName     | String                          | Master database source name       |
| slaveDataSourceNames     | Collection\<String\>            | Slave database source name list   |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | Slave database load balance       |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                             | *Data Type* | *Description*                                                |
| ---------------------------------- | ----------- | ------------------------------------------------------------ |
| sql.show (?)                       | boolean     | Print SQL parse and rewrite log or not, default value: false |
| executor.size (?)                  | int         | Be used in work thread number implemented by SQL; no limits if it is 0. default value: 0 |
| max.connections.size.per.query (?) | int         | The maximum connection number allocated by each query of each physical database, default value: 1 |
| check.table.metadata.enabled (?)   | boolean     | Check meta-data consistency or not in initialization, default value: false |

### data encryption

#### EncryptDataSourceFactory

| *Name*                | *DataType*                   | *Description*      |
| --------------------- | ---------------------------- | ------------------ |
| dataSource            | DataSource                   | Data source        |
| encryptRuleConfig     | EncryptRuleConfiguration     | encrypt rule configuration |
| props (?)             | Properties                   | Property configurations |

#### EncryptRuleConfiguration

| *Name*              | *DataType*                                  | *Description*                                               |
| ------------------- | ------------------------------------------- | ----------------------------------------------------------- |
| encryptors          | Map<String, EncryptorRuleConfiguration>     | Encryptor names and encryptors                              |
| tables              | Map<String, EncryptTableRuleConfiguration>  | Encrypt table names and encrypt tables                      |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                            | *DataType*| *Description*                                                                        |
| ----------------------------------| --------- | ------------------------------------------------------------------------------------ |
| sql.show (?)                      | boolean   | Print SQL parse and rewrite log or not, default value: false                         |
| query.with.cipher.column (?)      | boolean   | When there is a plainColumn, use cipherColumn or not to query, default value: true   |

### Orchestration

#### OrchestrationShardingDataSourceFactory

| *Name*              | *DataType*                 | *Description*                          |
| ------------------- | -------------------------- | -------------------------------------- |
| dataSourceMap       | Map\<String, DataSource\>  | Same as `ShardingDataSourceFactory`    |
| shardingRuleConfig  | ShardingRuleConfiguration  | Same as `ShardingDataSourceFactory`    |
| props (?)           | Properties                 | Same as `ShardingDataSourceFactory`    |
| orchestrationConfig | OrchestrationConfiguration | Orchestration rule configurations |

#### OrchestrationMasterSlaveDataSourceFactory

| *Name*                | *Data Type*                  | *Description*                          |
| --------------------- | ---------------------------- | -------------------------------------- |
| dataSourceMap         | Map<String, DataSource>      | Same as `MasterSlaveDataSourceFactory` |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Same as `MasterSlaveDataSourceFactory` |
| configMap (?)         | Map<String, Object>          | Same as `MasterSlaveDataSourceFactory` |
| props (?)             | Properties                   | Same as `ShardingDataSourceFactory`    |
| orchestrationConfig   | OrchestrationConfiguration   | Orchestration rule configurations |

#### OrchestrationEncryptDataSourceFactory

| *Name*                | *DataType*                   | *Description*                      |
| --------------------- | ---------------------------- | ---------------------------------- |
| dataSource            | DataSource                   | Same as `EncryptDataSourceFactory` |
| encryptRuleConfig     | EncryptRuleConfiguration     | Same as `EncryptDataSourceFactory` |
| props (?)             | Properties                   | Same as `EncryptDataSourceFactory` |
| orchestrationConfig   | OrchestrationConfiguration   | Orchestration rule configurations  |


#### OrchestrationConfiguration

| *Name*          | *Data Type*                 | *Description*                                                |
| --------------- | --------------------------- | ------------------------------------------------------------ |
| instanceConfigurationMap | Map\<String, CenterConfiguration\>  | config map of config-center&registry-center，the key is center's name，the value is the config-center/registry-center   |


#### CenterConfiguration

| *Name*                           | *Data Type* | *Description*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| instanceType                      | String     | The type of center instance(zookeeper/etcd/apollo/nacos)                                       |
| properties                        | String     | Properties for center instance config, such as options of zookeeper                        |
| orchestrationType                 | String     | The type of orchestration center: config_center or registry_center or metadata_center, if both, use "setOrchestrationType("registry_center,config_center,metadata_center");"                  |
| serverLists                       | String     | Connect to server lists in center, including IP address and port number; addresses are separated by commas, such as `host1:2181,host2:2181` |
| namespace (?)                     | String     | Namespace of center instance                                                                    |

Common configuration in properties as follow:

| *Name*                           | *Data Type* | *Description*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| overwrite                         | boolean    | Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations                          |

If type of center is `zookeeper` with config-center&registry-center&metadata-center, properties could be set with the follow options:

| *Name*                           | *Data Type* | *Description*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| digest (?)                       | String      | Connect to authority tokens in registry center; default indicates no need for authority |
| operationTimeoutMilliseconds (?) | int         | The operation timeout millisecond number, default to be 500 milliseconds |
| maxRetries (?)                   | int         | The maximum retry count, default to be 3 times               |
| retryIntervalMilliseconds (?)    | int         | The retry interval millisecond number, default to be 500 milliseconds |
| timeToLiveSeconds (?)            | int         | The living time for temporary nodes, default to be 60 seconds |

If type of center is `etcd` with config-center&registry-center&metadata-center, properties could be set with the follow options:

| *Name*                           | *Data Type* | *Description*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| timeToLiveSeconds (?)            | long        | The etcd TTL in seconds, default to be 30 seconds            |

If type of center is `apollo` with config-center, properties could be set with the follow options:

| *Name*                           | *Data Type* | *Description*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| appId (?)          | String        | Apollo appId, default to be "APOLLO_SHARDINGSPHERE"                                |
| env (?)            | String        | Apollo env, default to be "DEV"                                                    |
| clusterName (?)    | String        | Apollo clusterName, default to be "default"                                        |
| administrator (?)  | String        | Apollo administrator, default to be ""                                             |
| token (?)          | String        | Apollo token, default to be ""                                                     |
| portalUrl (?)      | String        | Apollo portalUrl, default to be ""                                                 |
| connectTimeout (?) | int           | Apollo connectTimeout, default to be 1000 milliseconds                             |
| readTimeout (?)    | int           | Apollo readTimeout, default to be 5000 milliseconds                                |

If type of center is `nacos` with config-center&registry-center, properties could be set with the follow options:

| *Name*                           | *Data Type* | *Description*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| group (?)          | String        | Nacos group, "SHARDING_SPHERE_DEFAULT_GROUP" in default                  |
| timeout (?)        | long          | Nacos timeout, default to be 3000 milliseconds                           |
