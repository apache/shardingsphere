+++
title = "Change History"
weight = 2
+++

## 5.0.0-beta

### Sharding

#### Root Configuration

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

#### Sharding Table Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

Attributes:

| *Name*                       | *DataType*                    | *Description*                                                                                                                         | *Default Value*                            |
| ---------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| logicTable                   | String                        | Name of sharding logic table                                                                                                          | -                                          |
| actualDataNodes (?)          | String                        | Describe data source names and actual tables, delimiter as point.<br /> Multiple data nodes split by comma, support inline expression | Broadcast table or databases sharding only |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | Databases sharding strategy                                                                                                           | Use default databases sharding strategy    |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | Tables sharding strategy                                                                                                              | Use default tables sharding strategy       |
| keyGenerateStrategy (?)      | KeyGeneratorConfiguration     | Key generator configuration                                                                                                           | Use default key generator                  |

#### Sharding Automatic Table Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingAutoTableRuleConfiguration

Attributes:

| *Name*                  | *DataType*                    | *Description*                                               | *Default Value*                 |
| ----------------------- | ----------------------------- | ----------------------------------------------------------- | ------------------------------- |
| logicTable              | String                        | Name of sharding logic table                                | -                               |
| actualDataSources (?)   | String                        | Data source names.<br /> Multiple data nodes split by comma | Use all configured data sources |
| shardingStrategy (?)    | ShardingStrategyConfiguration | Sharding strategy                                           | Use default sharding strategy   |
| keyGenerateStrategy (?) | KeyGeneratorConfiguration     | Key generator configuration                                 | Use default key generator       |

#### Sharding Strategy Configuration

##### Standard Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*           |
| --------------------- | ---------- | ----------------------- |
| shardingColumn        | String     | Sharding column name    |
| shardingAlgorithmName | String     | Sharding algorithm name |

##### Complex Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*                             |
| --------------------- | ---------- | ----------------------------------------- |
| shardingColumns       | String     | Sharding column name, separated by commas |
| shardingAlgorithmName | String     | Sharding algorithm name                   |

##### Hint Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*           |
| --------------------- | ---------- | ----------------------- |
| shardingAlgorithmName | String     | Sharding algorithm name |

##### None Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration

Attributes: None

Please refer to [Built-in Sharding Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding) for more details about type of algorithm.

##### Key Generate Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration

Attributes:

| *Name*           | *DataType* | *Description*               |
| ---------------- | ---------- | --------------------------- |
| column           | String     | Column name of key generate |
| keyGeneratorName | String     | key generate algorithm name |

Please refer to [Built-in Key Generate Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen) for more details about type of algorithm.

### Readwrite-splitting

#### Root Configuration

Class name: ReadwriteSplittingRuleConfiguration

Attributes:

| *Name*            | *DataType*                                            | *Description*                                                          |
| ----------------- | ----------------------------------------------------- | ---------------------------------------------------------------------- |
| dataSources (+)   | Collection\<ReadwriteSplittingDataSourceRuleConfiguration\> | Data sources of write and reads                                  |
| loadBalancers (*) | Map\<String, ShardingSphereAlgorithmConfiguration\>   | Load balance algorithm name and configurations of replica data sources |

#### Readwrite-splitting Data Source Configuration

Class name: ReadwriteSplittingDataSourceRuleConfiguration

Attributes:

| *Name*                     | *DataType*           | *Description*                                  | *Default Value*                    |
| -------------------------- | -------------------- | ---------------------------------------------- | ---------------------------------- |
| name                       | String               | Readwrite-splitting data source name           | -                                  |
| writeDataSourceName        | String               | Write sources source name                      | -                                  |
| readDataSourceNames (+)    | Collection\<String\> | Read sources source name list                  | -                                  |
| loadBalancerName (?)       | String               | Load balance algorithm name of replica sources | Round robin load balance algorithm |

### Encryption

#### Root Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration

Attributes:

| *Name*                    | *DataType*                                          | *Description*                                                                                  | *Default Value* |
| ------------------------- | --------------------------------------------------- | ---------------------------------------------------------------------------------------------- | --------------- |
| tables (+)                | Collection\<EncryptTableRuleConfiguration\>         | Encrypt table rule configurations                                                              |                 |
| encryptors (+)            | Map\<String, ShardingSphereAlgorithmConfiguration\> | Encrypt algorithm name and configurations                                                      |                 |
| queryWithCipherColumn (?) | boolean                                             | Whether query with cipher column for data encrypt. User you can use plaintext to query if have | true            |

#### Encrypt Table Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration

Attributes:

| *Name*      | *DataType*                                   | *Description*                      |
| ----------- | -------------------------------------------- | ---------------------------------- |
| name        | String                                       | Table name                         |
| columns (+) | Collection\<EncryptColumnRuleConfiguration\> | Encrypt column rule configurations |

#### Encrypt Column Rule Configuration

Class name: org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration

Attributes:

| *Name*                  | *DataType* | *Description*              |
| ----------------------- | ---------- | -------------------------- |
| logicColumn             | String     | Logic column name          |
| cipherColumn            | String     | Cipher column name         |
| assistedQueryColumn (?) | String     | Assisted query column name |
| plainColumn (?)         | String     | Plain column name          |
| encryptorName           | String     | Encrypt algorithm name     |

#### Encrypt Algorithm Configuration

Class name: org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

Attributes:

| *Name*     | *DataType* | *Description*                |
| ---------- | ---------- | ---------------------------- |
| name       | String     | Encrypt algorithm name       |
| type       | String     | Encrypt algorithm type       |
| properties | Properties | Encrypt algorithm properties |

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt) for more details about type of algorithm.

### Shadow DB

#### Root Configuration

Class name: org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

Attributes:

| *Name*          | *DataType*            | *Description*                                                                                                                                                                    |
| --------------- | --------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| column          | String                | Shadow field name in SQL, SQL with a value of true will be routed to the shadow database for execution                                                                           |
| sourceDataSourceNames | List\<String\> | Source data source names |
| shadowDataSourceNames | List\<String\> | Shadow data source names |

Please refer to [Built-in Load Balance Algorithm List](/docs/document/content/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance.en.md) for more details about type of algorithm.

### Governance

#### Configuration Item Explanation

##### Management

*Configuration Entrance*

Class name: org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration

Attributes:

| *Name*                      | *Data Type*                  | *Description*             |
| --------------------------- | ---------------------------- | ------------------------- |
| name                        | String                       | Governance instance name  |
| registryCenterConfiguration | RegistryCenterConfiguration  | Config of registry-center |

The type of registryCenter could be Zookeeper or Etcd.

*Governance Instance Configuration*

Class name: org.apache.shardingsphere.governance.repository.api.config.ClusterPersistRepositoryConfiguration

Attributes:

| *Name*      | *Data Type* | *Description*                                                                                                                                 |
| ----------- | ----------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| type        | String      | Governance instance type, such as: Zookeeper, etcd                                                                                            |
| serverLists | String      | The list of servers that connect to governance instance, including IP and port number, use commas to separate, such as: host1:2181,host2:2181 |
| props       | Properties  | Properties for center instance config, such as options of zookeeper                                                                           |
| overwrite   | boolean     | Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations     | 

ZooKeeper Properties Configuration

| *Name*                           | *Data Type* | *Description*                                  | *Default Value*       |
| -------------------------------- | ----------- | ---------------------------------------------- | --------------------- |
| digest (?)                       | String      | Connect to authority tokens in registry center | No need for authority |
| operationTimeoutMilliseconds (?) | int         | The operation timeout milliseconds             | 500 milliseconds      |
| maxRetries (?)                   | int         | The maximum retry count                        | 3                     |
| retryIntervalMilliseconds (?)    | int         | The retry interval milliseconds                | 500 milliseconds      |
| timeToLiveSeconds (?)            | int         | Time to live seconds for ephemeral nodes       | 60 seconds            |


Etcd Properties Configuration

| *Name*                | *Data Type* | *Description*                         | *Default Value* |
| --------------------- | ----------- | ------------------------------------- | --------------- |
| timeToLiveSeconds (?) | long        | Time to live seconds for data persist | 30 seconds      |

## ShardingSphere-4.x

### Sharding

#### ShardingDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap         | Map<String, DataSource>      | Data sources configuration |
| shardingRuleConfig    | ShardingRuleConfiguration    | Data sharding configuration rule    |
| props (?)             | Properties                   | Property configurations             |

#### ShardingRuleConfiguration

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| tableRuleConfigs         | Collection<TableRuleConfiguration> | Sharding rule list                |
| bindingTableGroups (?)   | Collection<String>              | Binding table rule list              |
| broadcastTables (?)	   | Collection<String>              | Broadcast table rule list            |
| defaultDataSourceName (?)| String	                         |Tables not configured with sharding rules will locate according to default data sources |
| defaultDatabaseShardingStrategyConfig (?)	| ShardingStrategyConfiguration	| Default database sharding strategy |
| defaultTableShardingStrategyConfig (?) | ShardingStrategyConfiguration | Default table sharding strategy |
| defaultKeyGeneratorConfig (?) | KeyGeneratorConfiguration	| Default key generator configuration, use user-defined ones or built-in ones, e.g. SNOWFLAKE/UUID. Default key generator is `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator`|
| masterSlaveRuleConfigs (?) | Collection<MasterSlaveRuleConfiguration> | Read-write split rules, default indicates not using read-write split |

#### TableRuleConfiguration

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| logicTable	           | String	                         | Name of logic table                  |
| actualDataNodes (?)	   | String	                         | Describe data source names and actual tables, delimiter as point, multiple data nodes split by comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | Databases sharding strategy, use default databases sharding strategy if absent |
| tableShardingStrategyConfig (?) | ShardingStrategyConfiguration | Tables sharding strategy, use default databases sharding strategy if absent |
| keyGeneratorConfig (?) | KeyGeneratorConfiguration | Key generator configuration, use default key generator if absent |
| encryptorConfiguration (?) | EncryptorConfiguration | Encrypt generator configuration |

#### StandardShardingStrategyConfiguration

##### Subclass of ShardingStrategyConfiguration.

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| shardingColumn           | String	                         | Sharding column name                  |
| preciseShardingAlgorithm | PreciseShardingAlgorithm        | Precise sharding algorithm used in `=` and `IN` |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm        | Range sharding algorithm used in `BETWEEN` |

##### ComplexShardingStrategyConfiguration
The implementation class of `ShardingStrategyConfiguration`, used in complex sharding situations with multiple sharding keys.

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| shardingColumns	        | String	                     | Sharding column name, separated by commas |
| shardingAlgorithm	        | ComplexKeysShardingAlgorithm	 | Complex sharding algorithm |

##### InlineShardingStrategyConfiguration
The implementation class of `ShardingStrategyConfiguration`, used in sharding strategy of inline expression.

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| shardingColumns	        | String	                     | Sharding column name, separated by commas |
| algorithmExpression	    | String                         | Inline expression of sharding strategies, should conform to groovy syntax; refer to Inline expression for more details |

##### HintShardingStrategyConfiguration
The implementation class of `ShardingStrategyConfiguration`, used to configure hint sharding strategies.

| *Name*                   | *DataType*                      | *Description*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| shardingAlgorithm	       | HintShardingAlgorithm	         | Hint sharding algorithm |

##### NoneShardingStrategyConfiguration
The implementation class of `ShardingStrategyConfiguration`, used to configure none-sharding strategies.
KeyGeneratorConfiguration

| *Name*                   | *DataType*                      | *Description*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| column                   | String                          | Column name of key generator |
| type	                   | String	                         | Type of key generator, use user-defined ones or built-in ones, e.g. SNOWFLAKE, UUID |
| props	                   | Properties	                     | The Property configuration of key generators |

#### Properties

Property configuration that can include these properties of these key generators.

SNOWFLAKE

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| worker.id (?)                      | long	       | The unique id for working machine, the default value is `0` |
| max.tolerate.time.difference.milliseconds (?) | long | The max tolerate time for different server’s time difference in milliseconds, the default value is `10` |
| max.vibration.offset (?) | int | The max upper limit value of vibrate number, range `[0, 4096)`, the default value is `1`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1` |

### Readwrite-splitting

#### MasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap         | Map<String, DataSource>      | Mapping of data source and its name |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Master slave rule configuration     |
| props (?)             | Properties                   | Property configurations             |

#### MasterSlaveRuleConfiguration

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| name                     | String                          | Readwrite-splitting data source name |
| masterDataSourceName     | String                          | Master database source name          |
| slaveDataSourceNames     | Collection<String>              | Slave database source name list      |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | Slave database load balance          |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| sql.show (?)                       | boolean     | Print SQL parse and rewrite log or not, default value: false |
| executor.size (?)                  | int         | Be used in work thread number implemented by SQL; no limits if it is 0. default value: 0 |
| max.connections.size.per.query (?) | int         | The maximum connection number allocated by each query of each physical database, default value: 1 |
| check.table.metadata.enabled (?)   | boolean     | Check meta-data consistency or not in initialization, default value: false |

### Data Masking

#### EncryptDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSource	        | DataSource                   | Data source                         |
| encryptRuleConfig     | EncryptRuleConfiguration     | encrypt rule configuration          |
| props (?)             | Properties                   | Property configurations             |

#### EncryptRuleConfiguration

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| encryptors	| Map<String, EncryptorRuleConfiguration>	| Encryptor names and encryptors        |
| tables	| Map<String, EncryptTableRuleConfiguration>	| Encrypt table names and encrypt tables |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| sql.show (?)                       | boolean     | Print SQL parse and rewrite log or not, default value: false |
| query.with.cipher.column (?)	     | boolean     | When there is a plainColumn, use cipherColumn or not to query, default value: true |

### Orchestration

#### OrchestrationShardingDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap	        | Map<String, DataSource>	   | Same as `ShardingDataSourceFactory` |
| shardingRuleConfig	| ShardingRuleConfiguration	   | Same as `ShardingDataSourceFactory` |
| props (?)	            | Properties	               | Same as `ShardingDataSourceFactory` |
| orchestrationConfig	| OrchestrationConfiguration   | Orchestration rule configurations   |

#### OrchestrationMasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap	        | Map<String, DataSource>	   | Same as `MasterSlaveDataSourceFactory` |
| masterSlaveRuleConfig	| MasterSlaveRuleConfiguration | Same as `MasterSlaveDataSourceFactory` |
| configMap (?)	        | Map<String, Object>	       | Same as `MasterSlaveDataSourceFactory` |
| props (?)	            | Properties	               | Same as `ShardingDataSourceFactory`    |
| orchestrationConfig	| OrchestrationConfiguration   | Orchestration rule configurations      |

#### OrchestrationEncryptDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSource	        | DataSource	               | Same as `EncryptDataSourceFactory`  |
| encryptRuleConfig	    | EncryptRuleConfiguration	   | Same as `EncryptDataSourceFactory`  |
| props (?)	            | Properties	               | Same as `EncryptDataSourceFactory`  |
| orchestrationConfig	| OrchestrationConfiguration   | Orchestration rule configurations   |

#### OrchestrationConfiguration

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| instanceConfigurationMap	| Map<String, CenterConfiguration>	| config map of config-center&registry-center，the key is center’s name，the value is the config-center/registry-center |

#### CenterConfiguration

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| type	| String	| The type of center instance(zookeeper/etcd/apollo/nacos)
| properties	| String	| Properties for center instance config, such as options of zookeeper |
| orchestrationType	| String	| The type of orchestration center: config-center or registry-center, if both, use “setOrchestrationType(“registry_center,config_center”);” |
| serverLists	| String	| Connect to server lists in center, including IP address and port number; addresses are separated by commas, such as `host1:2181,host2:2181` |
| namespace (?) |	String	| Namespace of center instance |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| overwrite | boolean |	Local configurations overwrite center configurations or not; if they overwrite, each start takes reference of local configurations |

If type of center is `zookeeper` with config-center&registry-center, properties could be set with the follow options:

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| digest (?)	| String |	Connect to authority tokens in registry center; default indicates no need for authority |
| operationTimeoutMilliseconds (?) |	int	| The operation timeout millisecond number, default to be 500 milliseconds |
| maxRetries (?)	| int	| The maximum retry count, default to be 3 times |
| retryIntervalMilliseconds (?) |	int	| The retry interval millisecond number, default to be 500 milliseconds |
| timeToLiveSeconds (?)	| int	| The living time for temporary nodes, default to be 60 seconds |

If type of center is `etcd` with config-center&registry-center, properties could be set with the follow options:

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| timeToLiveSeconds (?)	             | long        | The etcd TTL in seconds, default to be 30 seconds |

If type of center is `apollo` with config-center&registry-center, properties could be set with the follow options:

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| appId (?) |	String |	Apollo appId, default to be “APOLLO_SHARDINGSPHERE” |
| env (?)	| String | Apollo env, default to be “DEV” |
| clusterName (?) |	String	| Apollo clusterName, default to be “default” |
| administrator (?) |	String	| Apollo administrator, default to be "” |
| token (?) |	String	| Apollo token, default to be "” |
| portalUrl (?) |	String	| Apollo portalUrl, default to be "” |
| connectTimeout (?) |	int |	Apollo connectTimeout, default to be 1000 milliseconds |
| readTimeout (?) |	int |	Apollo readTimeout, default to be 5000 milliseconds |

If type of center is `nacos` with config-center&registry-center, properties could be set with the follow options:

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| group (?)                          | String      | Nacos group, “SHARDING_SPHERE_DEFAULT_GROUP” in default      |
| timeout (?)                        | long        | Nacos timeout, default to be 3000 milliseconds               |

## ShardingSphere-3.x

### Sharding

#### ShardingDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap         | Map<String, DataSource>      | Data sources configuration          |
| shardingRuleConfig    | ShardingRuleConfiguration    | Data sharding configuration rule    |
| configMap (?)	        | Map<String, Object>	       | Config map                          |
| props (?)             | Properties                   | Property configurations             |

#### ShardingRuleConfiguration

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| tableRuleConfigs         | Collection<TableRuleConfiguration> | Table rule configuration               |
| bindingTableGroups (?)   | Collection<String>              | Binding table groups              |
| broadcastTables (?)	   | Collection<String>              | Broadcast table groups            |
| defaultDataSourceName (?)| String	                         |Tables not configured with sharding rules will locate according to default data sources |
| defaultDatabaseShardingStrategyConfig (?)	| ShardingStrategyConfiguration	| Default database sharding strategy |
| defaultTableShardingStrategyConfig (?) | ShardingStrategyConfiguration | Default table sharding strategy |
| defaultKeyGeneratorConfig (?) | KeyGenerator	| Default key generator, default value is `io.shardingsphere.core.keygen.DefaultKeyGenerator`|
| masterSlaveRuleConfigs (?) | Collection<MasterSlaveRuleConfiguration> | Read-write splitting rule configuration |

#### TableRuleConfiguration

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| logicTable	           | String	                         | Name of logic table                  |
| actualDataNodes (?)	   | String	                         | Describe data source names and actual tables, delimiter as point, multiple data nodes split by comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | Databases sharding strategy, use default databases sharding strategy if absent |
| tableShardingStrategyConfig (?) | ShardingStrategyConfiguration | Tables sharding strategy, use default databases sharding strategy if absent |
| logicIndex (?)	| String	| Name if logic index. If use DROP INDEX XXX SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables |
| keyGeneratorConfig (?) | String | Key generator column name, do not use Key generator if absent |
| keyGenerator (?)	| KeyGenerator	| Key generator, use default key generator if absent |

#### StandardShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| shardingColumn           | String	                         | Sharding column name                  |
| preciseShardingAlgorithm | PreciseShardingAlgorithm        | Precise sharding algorithm used in `=` and `IN` |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm        | Range sharding algorithm used in `BETWEEN` |

##### ComplexShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| shardingColumns	        | String	                     | Sharding column name, separated by commas |
| shardingAlgorithm	        | ComplexKeysShardingAlgorithm	 | Complex sharding algorithm |

##### InlineShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*                   | *DataType*                      | *Explanation*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| shardingColumns	        | String	                     | Sharding column name, separated by commas |
| algorithmExpression	    | String                         | Inline expression of sharding strategies, should conform to groovy syntax; refer to Inline expression for more details |

##### HintShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*                   | *DataType*                      | *Description*                        |
| :----------------------- | :------------------------------ | :----------------------------------- |
| shardingAlgorithm	       | HintShardingAlgorithm	         | Hint sharding algorithm |

##### NoneShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

#### Properties

Enumeration of properties.

| *Name*                             | *Data Type* | *Explanation*                                                |
| :--------------------------------- | :---------- | :----------------------------------------------------------- |
| sql.show (?) |	boolean	| Print SQL parse and rewrite log, default value: false |
| executor.size (?) |	int	| The number of SQL execution threads, zero means no limit. default value: 0 |
| max.connections.size.per.query (?)	| int |	Max connection size for every query to every actual database. default value: 1 |
| check.table.metadata.enabled (?)	| boolean |	Check the metadata consistency of all the tables, default value : false |

#### configMap

User-defined arguments.

### Readwrite-splitting

#### MasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Description*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap         | Map<String, DataSource>      | Map of data sources and their names |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Master slave rule configuration     |
| configMap (?)         | Map<String, Object>          | Config map                          |
| props (?)             | Properties                   | Properties                          |

#### MasterSlaveRuleConfiguration

| *Name*                   | *DataType*                      | *Description*                    |
| :----------------------- | :------------------------------ | :------------------------------- |
| name                     | String                          | Name of master slave data source |
| masterDataSourceName     | String                          | Name of master data source       |
| slaveDataSourceNames     | Collection<String>              | Names of Slave data sources      |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | Load balance algorithm           |

#### configMap

User-defined arguments.

#### PropertiesConstant

Enumeration of properties.

| *Name*                             | *DataType* | *Description*                                                |
| :--------------------------------- | :--------- | :----------------------------------------------------------- |
| sql.show (?)                       | boolean    | To show SQLS or not, default value: false                    |
| executor.size (?)                  | int        | The number of working threads, default value: CPU count      |
| max.connections.size.per.query (?) | int        | Max connection size for every query to every actual database. default value: 1 |
| check.table.metadata.enabled (?)   | boolean    | Check the metadata consistency of all the tables, default value : false |

### Orchestration

#### OrchestrationShardingDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap	        | Map<String, DataSource>	   | Same as `ShardingDataSourceFactory` |
| shardingRuleConfig	| ShardingRuleConfiguration	   | Same as `ShardingDataSourceFactory` |
| configMap (?)	        | Map<String, Object>          | Same with `ShardingDataSourceFactory` |
| props (?)	            | Properties	               | Same as `ShardingDataSourceFactory` |
| orchestrationConfig	| OrchestrationConfiguration   | Orchestration rule configurations   |

#### OrchestrationMasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| dataSourceMap	        | Map<String, DataSource>	   | Same as `MasterSlaveDataSourceFactory` |
| masterSlaveRuleConfig	| MasterSlaveRuleConfiguration | Same as `MasterSlaveDataSourceFactory` |
| configMap (?)	        | Map<String, Object>	       | Same as `MasterSlaveDataSourceFactory` |
| props (?)	            | Properties	               | Same as `ShardingDataSourceFactory`    |
| orchestrationConfig	| OrchestrationConfiguration   | Orchestration configurations      |

#### OrchestrationConfiguration

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| name	                | String	                   | Name of orchestration instance      |
| overwrite	            | boolean	                   | Use local configuration to overwrite registry center or not |
| regCenterConfig	    | RegistryCenterConfiguration  | Registry center configuration |

#### RegistryCenterConfiguration

| *Name*                | *DataType*                   | *Explanation*                       |
| :-------------------- | :--------------------------- | :---------------------------------- |
| serverLists	| String	| Registry servers list, multiple split as comma. Example: host1:2181,host2:2181 |
| namespace (?)	| String	| Namespace of registry |
| digest (?)	| String	| Digest for registry. Default is not need digest. |
| operationTimeoutMilliseconds (?)	| int	| Operation timeout time in milliseconds. Default value is 500 milliseconds. |
| maxRetries (?)	| int	| Max number of times to retry. Default value is 3 |
| retryIntervalMilliseconds (?)	| int	| Time interval in milliseconds on each retry. Default value is 500 milliseconds. |
| timeToLiveSeconds (?)	| int	| Time to live in seconds of ephemeral keys. Default value is 60 seconds. |

## ShardingSphere-2.x

### Readwrite-splitting

#### concept

In order to relieve the pressure on the database, the write and read operations are separated into different data sources. The write library is called the master library, and the read library is called the slave library. One master library can be configured with multiple slave libraries.

#### Support item

1. Provides a readwrite-splitting configuration with one master and multiple slaves, which can be used independently or with sub-databases and sub-meters.
2. Independent use of readwrite-splitting to support SQL transparent transmission.
3. In the same thread and the same database connection, if there is a write operation, subsequent read operations will be read from the main library to ensure data consistency.
4. Spring namespace.
5. Hint-based mandatory main library routing.

#### Unsupported item

1. Data synchronization between the master library and the slave library.
2. Data inconsistency caused by the data synchronization delay of the master library and the slave library.
3. Double writing or multiple writing in the main library.

#### Code development example

##### only readwrite-splitting

```java
// Constructing a readwrite-splitting data source, the readwrite-splitting data source implements the DataSource interface, which can be directly processed as a data source. masterDataSource, slaveDataSource0, slaveDataSource1, etc. are real data sources configured using connection pools such as DBCP
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("masterDataSource", masterDataSource);
dataSourceMap.put("slaveDataSource0", slaveDataSource0);
dataSourceMap.put("slaveDataSource1", slaveDataSource1);

// Constructing readwrite-splitting configuration
MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
masterSlaveRuleConfig.setName("ms_ds");
masterSlaveRuleConfig.setMasterDataSourceName("masterDataSource");
masterSlaveRuleConfig.getSlaveDataSourceNames().add("slaveDataSource0");
masterSlaveRuleConfig.getSlaveDataSourceNames().add("slaveDataSource1");

DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(dataSourceMap, masterSlaveRuleConfig);
```

##### sharding table and database + readwrite-splitting

```java
// Constructing a readwrite-splitting data source, the readwrite-splitting data source implements the DataSource interface, which can be directly processed as a data source. masterDataSource, slaveDataSource0, slaveDataSource1, etc. are real data sources configured using connection pools such as DBCP
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("masterDataSource0", masterDataSource0);
dataSourceMap.put("slaveDataSource00", slaveDataSource00);
dataSourceMap.put("slaveDataSource01", slaveDataSource01);

dataSourceMap.put("masterDataSource1", masterDataSource1);
dataSourceMap.put("slaveDataSource10", slaveDataSource10);
dataSourceMap.put("slaveDataSource11", slaveDataSource11);

// Constructing readwrite-splitting configuration
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

// Continue to create ShardingDataSource through ShardingSlaveDataSourceFactory
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig0);
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig1);

DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

## ShardingSphere-1.x

### Readwrite-splitting

#### concept

In order to relieve the pressure on the database, the write and read operations are separated into different data sources. The write library is called the master library, and the read library is called the slave library. One master library can be configured with multiple slave libraries.

#### Support item

1. Provides a readwrite-splitting configuration with one master and multiple slaves, which can be used independently or with sub-databases and sub-meters.
2. In the same thread and the same database connection, if there is a write operation, subsequent read operations will be read from the main library to ensure data consistency.
3. Spring namespace.
4. Hint-based mandatory main library routing.

#### Unsupported item

1. Data synchronization between the master library and the slave library.
2. Data inconsistency caused by the data synchronization delay of the master library and the slave library.
3. Double writing or multiple writing in the main library.

#### Code development example

```java
// Constructing a readwrite-splitting data source, the readwrite-splitting data source implements the DataSource interface, which can be directly processed as a data source. masterDataSource, slaveDataSource0, slaveDataSource1, etc. are real data sources configured using connection pools such as DBCP
Map<String, DataSource> slaveDataSourceMap0 = new HashMap<>();
slaveDataSourceMap0.put("slaveDataSource00", slaveDataSource00);
slaveDataSourceMap0.put("slaveDataSource01", slaveDataSource01);
// You can choose the master-slave library load balancing strategy, the default is ROUND_ROBIN, and there is RANDOM to choose from, or customize the load strategy
DataSource masterSlaveDs0 = MasterSlaveDataSourceFactory.createDataSource("ms_0", "masterDataSource0", masterDataSource0, slaveDataSourceMap0, MasterSlaveLoadBalanceStrategyType.ROUND_ROBIN);

Map<String, DataSource> slaveDataSourceMap1 = new HashMap<>();
slaveDataSourceMap1.put("slaveDataSource10", slaveDataSource10);
slaveDataSourceMap1.put("slaveDataSource11", slaveDataSource11);
DataSource masterSlaveDs1 = MasterSlaveDataSourceFactory.createDataSource("ms_1", "masterDataSource1", masterDataSource1, slaveDataSourceMap1, MasterSlaveLoadBalanceStrategyType.ROUND_ROBIN);

// Constructing readwrite-splitting configuration
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("ms_0", masterSlaveDs0);
dataSourceMap.put("ms_1", masterSlaveDs1);

// Continue to create ShardingDataSource through ShardingSlaveDataSourceFactory
```