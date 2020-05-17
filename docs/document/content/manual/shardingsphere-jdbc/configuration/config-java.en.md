+++
title = "Java Configuration"
weight = 1
+++

## Configuration Instance

### Data Sharding

```java
     DataSource getShardingDataSource() throws SQLException {
         ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
         shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
         shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
         shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
         shardingRuleConfig.getBroadcastTables().add("t_config");
         shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
         shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new ModuloShardingTableAlgorithm()));
         return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new Properties());
     }
     
     private static KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
         KeyGeneratorConfiguration result = new KeyGeneratorConfiguration("SNOWFLAKE", "order_id");
         return result;
     }
     
     TableRuleConfiguration getOrderTableRuleConfiguration() {
         TableRuleConfiguration result = new TableRuleConfiguration("t_order", "ds${0..1}.t_order${0..1}");
         result.setKeyGeneratorConfig(getKeyGeneratorConfiguration());
         return result;
     }
     
     TableRuleConfiguration getOrderItemTableRuleConfiguration() {
         TableRuleConfiguration result = new TableRuleConfiguration("t_order_item", "ds${0..1}.t_order_item${0..1}");
         return result;
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("ds0", DataSourceUtil.createDataSource("ds0"));
         result.put("ds1", DataSourceUtil.createDataSource("ds1"));
         return result;
     }
```

### Read-Write Split

```java
     DataSource getMasterSlaveDataSource() throws SQLException {
         MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration("ds_master_slave", "ds_master", Arrays.asList("ds_slave0", "ds_slave1"));
         return MasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig, new Properties());
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("ds_master", DataSourceUtil.createDataSource("ds_master"));
         result.put("ds_slave0", DataSourceUtil.createDataSource("ds_slave0"));
         result.put("ds_slave1", DataSourceUtil.createDataSource("ds_slave1"));
         return result;
     }
```

### Data Masking

```java
    DataSource getEncryptDataSource() throws SQLException {
        return EncryptDataSourceFactory.createDataSource(DataSourceUtil.createDataSource("demo_ds"), getEncryptRuleConfiguration(), new Properties());
    }

    private static EncryptRuleConfiguration getEncryptRuleConfiguration() {
        Properties props = new Properties();
        props.setProperty("aes.key.value", "123456");
        EncryptorRuleConfiguration encryptorConfig = new EncryptorRuleConfiguration("AES", props);
        EncryptColumnRuleConfiguration columnConfig = new EncryptColumnRuleConfiguration("plain_pwd", "cipher_pwd", "", "aes");
        EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration(Collections.singletonMap("pwd", columnConfig));
        EncryptRuleConfiguration encryptRuleConfig = new EncryptRuleConfiguration();
        encryptRuleConfig.getEncryptors().put("aes", encryptorConfig);
        encryptRuleConfig.getTables().put("t_encrypt", tableConfig);
		return encryptRuleConfig;
    }
```

### Data Sharding + Read-Write Split

```java
    DataSource getDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        shardingRuleConfig.getBroadcastTables().add("t_config");
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new PreciseModuloShardingDatabaseAlgorithm()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new PreciseModuloShardingTableAlgorithm()));
        shardingRuleConfig.setMasterSlaveRuleConfigs(getMasterSlaveRuleConfigurations());
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new Properties());
    }
    
    private static KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
        KeyGeneratorConfiguration result = new KeyGeneratorConfiguration("SNOWFLAKE", "order_id");
        return result;
    }
    
    TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("t_order", "ds_${0..1}.t_order_${[0, 1]}");
        result.setKeyGeneratorConfig(getKeyGeneratorConfiguration());
        return result;
    }
    
    TableRuleConfiguration getOrderItemTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item_${[0, 1]}");
        return result;
    }
    
    List<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration("ds_0", "demo_ds_master_0", Arrays.asList("demo_ds_master_0_slave_0", "demo_ds_master_0_slave_1"));
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration("ds_1", "demo_ds_master_1", Arrays.asList("demo_ds_master_1_slave_0", "demo_ds_master_1_slave_1"));
        return Lists.newArrayList(masterSlaveRuleConfig1, masterSlaveRuleConfig2);
    }
    
    Map<String, DataSource> createDataSourceMap() {
        final Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_master_0", DataSourceUtil.createDataSource("demo_ds_master_0"));
        result.put("demo_ds_master_0_slave_0", DataSourceUtil.createDataSource("demo_ds_master_0_slave_0"));
        result.put("demo_ds_master_0_slave_1", DataSourceUtil.createDataSource("demo_ds_master_0_slave_1"));
        result.put("demo_ds_master_1", DataSourceUtil.createDataSource("demo_ds_master_1"));
        result.put("demo_ds_master_1_slave_0", DataSourceUtil.createDataSource("demo_ds_master_1_slave_0"));
        result.put("demo_ds_master_1_slave_1", DataSourceUtil.createDataSource("demo_ds_master_1_slave_1"));
        return result;
    }
```
### Data Sharding + Data Masking

```java
    public DataSource getDataSource() throws SQLException {
            ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
            shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
            shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
            shardingRuleConfig.getTableRuleConfigs().add(getOrderEncryptTableRuleConfiguration());
            shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
            shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "demo_ds_${user_id % 2}"));
            shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new PreciseModuloShardingTableAlgorithm()));
            shardingRuleConfig.setEncryptRuleConfig(getEncryptRuleConfiguration());
            return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new Properties());
        }
        
        private static TableRuleConfiguration getOrderTableRuleConfiguration() {
            TableRuleConfiguration result = new TableRuleConfiguration("t_order", "demo_ds_${0..1}.t_order_${[0, 1]}");
            result.setKeyGeneratorConfig(getKeyGeneratorConfiguration());
            return result;
        }
        
        private static TableRuleConfiguration getOrderItemTableRuleConfiguration() {
            TableRuleConfiguration result = new TableRuleConfiguration("t_order_item", "demo_ds_${0..1}.t_order_item_${[0, 1]}");
            result.setEncryptorConfig(new EncryptorConfiguration("MD5", "status", new Properties()));
            return result;
        }
        
        private static EncryptRuleConfiguration getEncryptRuleConfiguration() {
            Properties props = new Properties();
            props.setProperty("aes.key.value", "123456");
            EncryptorRuleConfiguration encryptorConfig = new EncryptorRuleConfiguration("AES", props);
            EncryptColumnRuleConfiguration columnConfig = new EncryptColumnRuleConfiguration("plain_order", "cipher_order", "", "aes");
            EncryptTableRuleConfiguration tableConfig = new EncryptTableRuleConfiguration(Collections.singletonMap("order_id", columnConfig));
            EncryptRuleConfiguration encryptRuleConfig = new EncryptRuleConfiguration();
            encryptRuleConfig.getEncryptors().put("aes", encryptorConfig);
            encryptRuleConfig.getTables().put("t_order", tableConfig);
			return encryptRuleConfig;
        }
        
        private static Map<String, DataSource> createDataSourceMap() {
            Map<String, DataSource> result = new HashMap<>();
            result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
            result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
            return result;
        }
        
        private static KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
            return new KeyGeneratorConfiguration("SNOWFLAKE", "order_id", new Properties());
        }
```

### Orchestration

```java
    DataSource getDataSource() throws SQLException {
        // OrchestrationShardingDataSourceFactory 可替换成 OrchestrationMasterSlaveDataSourceFactory 或 OrchestrationEncryptDataSourceFactory
        return OrchestrationShardingDataSourceFactory.createDataSource(
                createDataSourceMap(), createShardingRuleConfig(), new HashMap<String, Object>(), new Properties(), 
                new OrchestrationConfiguration(createCenterConfigurationMap()));
    }
    private Map<String, CenterConfiguration> createCenterConfigurationMap() {
        Map<String, CenterConfiguration> instanceConfigurationMap = new HashMap<String, CenterConfiguration>();
        CenterConfiguration config = createCenterConfiguration();
        instanceConfigurationMap.put("orchestration-shardingsphere-data-source", config);
        return instanceConfigurationMap;
    }
    private CenterConfiguration createCenterConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("overwrite", overwrite);
        CenterConfiguration result = new CenterConfiguration("zookeeper", properties);
        result.setServerLists("localhost:2181");
        result.setNamespace("shardingsphere-orchestration");
        result.setOrchestrationType("registry_center,config_center,metadata_center");
        return result;
    }
```

## Configuration Item Explanation

### Data Sharding

#### ShardingDataSourceFactory

| *Name*             | *DataType*                | *Explanation*                    |
| ------------------ | ------------------------- | -------------------------------- |
| dataSourceMap      | Map\<String, DataSource\> | Data sources configuration       |
| shardingRuleConfig | ShardingRuleConfiguration | Data sharding configuration rule |
| props (?)          | Properties                | Property configuration           |

#### ShardingRuleConfiguration

| *Name*                                    | *DataType*                                 | *Explanation*                                                |
| ----------------------------------------- | ------------------------------------------ | ------------------------------------------------------------ |
| tableRuleConfigs                          | Collection\<TableRuleConfiguration\>       | Sharding rule list                                           |
| bindingTableGroups (?)                    | Collection\<String\>                       | Binding table rule list                                      |
| broadcastTables (?)                       | Collection\<String\>                       | Broadcast table rule list                                    |
| defaultDataSourceName (?)                 | String                                     | Tables not configured with sharding rules will locate according to default data sources |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration              | Default database sharding strategy                           |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration              | Default table sharding strategy                              |
| defaultKeyGeneratorConfig (?)             | KeyGeneratorConfiguration                  | Default key generator configuration, use user-defined ones or built-in ones, e.g. SNOWFLAKE/UUID. Default key generator is `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator` |
| masterSlaveRuleConfigs (?)                | Collection\<MasterSlaveRuleConfiguration\> | Read-write split rules, default indicates not using read-write split |

#### TableRuleConfiguration

| *Name*                             | *DataType*                    | *Description*                                                |
| ---------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| logicTable                         | String                        | Name of logic table                                          |
| actualDataNodes (?)                | String                        | Describe data source names and actual tables, delimiter as point, multiple data nodes split by comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | Databases sharding strategy, use default databases sharding strategy if absent |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | Tables sharding strategy, use default databases sharding strategy if absent |
| keyGeneratorConfig (?)             | KeyGeneratorConfiguration     | Key generator configuration, use default key generator if absent |
| encryptorConfiguration (?)         | EncryptorConfiguration        | Encrypt generator configuration                              |


#### StandardShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*                     | *DataType*               | *Explanation*                                   |
| -------------------------- | ------------------------ | ----------------------------------------------- |
| shardingColumn             | String                   | Sharding column name                            |
| preciseShardingAlgorithm   | PreciseShardingAlgorithm | Precise sharding algorithm used in `=` and `IN` |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm   | Range sharding algorithm used in `BETWEEN`      |

#### ComplexShardingStrategyConfiguration

The implementation class of `ShardingStrategyConfiguration`, used in complex sharding situations with  multiple sharding keys.

| *Name*            | *DataType*                   | *Explanation*                             |
| ----------------- | ---------------------------- | ----------------------------------------- |
| shardingColumns   | String                       | Sharding column name, separated by commas |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | Complex sharding algorithm                |

#### InlineShardingStrategyConfiguration

The implementation class of `ShardingStrategyConfiguration`, used in sharding strategy of inline expression.

| *Name*              | *DataType* | *Explanation*                                                |
| ------------------- | ---------- | ------------------------------------------------------------ |
| shardingColumn      | String     | Sharding column name                                         |
| algorithmExpression | String     | Inline expression of sharding strategies, should conform to groovy syntax; refer to [Inline expression](/en/features/sharding/other-features/inline-expression) for more details |

#### HintShardingStrategyConfiguration

The implementation class of `ShardingStrategyConfiguration`,  used to configure hint sharding strategies.

| *Name*            | *DataType*            | *Description*           |
| ----------------- | --------------------- | ----------------------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint sharding algorithm |

#### NoneShardingStrategyConfiguration

The implementation class of `ShardingStrategyConfiguration`, used to configure none-sharding strategies.

#### KeyGeneratorConfiguration

| *Name* | *DataType* | *Description*                                                |
| ------ | ---------- | ------------------------------------------------------------ |
| column | String     | Column name of key generator                                 |
| type   | String     | Type of key generator, use user-defined ones or built-in ones, e.g. SNOWFLAKE, UUID |
| props  | Properties | The Property configuration of key generators                 |

#### Properties

Property configuration that can include these properties of these key generators.

##### SNOWFLAKE

| *Name*                                              | *DataType* | *Explanation*                                                                                                                                                                                                                   |
| --------------------------------------------------- | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| worker.id (?)                                        |   long     | The unique id for working machine, the default value is `0`                                                                                                                                                                    |
| max.tolerate.time.difference.milliseconds (?)        |   long     | The max tolerate time for different server's time difference in milliseconds, the default value is `10`                                                                                                                         |
| max.vibration.offset (?)                             |    int     | The max upper limit value of vibrate number, range `[0, 4096)`, the default value is `1`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1` |

#### EncryptRuleConfiguration

| *Name*              | *DataType*                                  | *Explanation*                                                                  |
| ------------------- | ------------------------------------------- | ------------------------------------------------------------------------------ |
| encryptors          | Map<String, EncryptorRuleConfiguration>     | Encryptor names and encryptors                                                 |
| tables              | Map<String, EncryptTableRuleConfiguration>  | Encrypt table names and encrypt tables                                         |

#### EncryptorRuleConfiguration

| *Name*              | *DataType*                   | *Explanation*                                                                               |
| ------------------- | ---------------------------- | ------------------------------------------------------------------------------------------- |
| type                | String                       | Type of encryptor，use user-defined ones or built-in ones, e.g. MD5/AES                      |
| properties          | Properties                   | Properties, Notice: when use AES encryptor, `aes.key.value` for AES encryptor need to be set |

#### EncryptTableRuleConfiguration

| *Name*              | *DataType*                                   | *Explanation*                              |
| ------------------- | -------------------------------------------- | ------------------------------------------ |
| tables              | Map<String, EncryptColumnRuleConfiguration>  | Encrypt column names and encrypt column    |

#### EncryptColumnRuleConfiguration

| *Name*              | *DataType*                   | *Explanation*                                                                                         |
| ------------------- | ---------------------------- |  ---------------------------------------------------------------------------------------------------- |
| plainColumn         | String                       | Plain column name                                                                                     |
| cipherColumn        | String                       | Cipher column name                                                                                    |
| assistedQueryColumn | String                       | AssistedColumns for query，when use ShardingQueryAssistedEncryptor, it can help query encrypted data  |
| encryptor           | String                       | Encryptor name                                                                                        |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                             | *DataType* | *Explanation*                                                |
| ---------------------------------- | ---------- | ------------------------------------------------------------ |
| sql.show (?)                       | boolean    | Show SQL or not, default value: false                        |
| executor.size (?)                  | int        | Work thread number, default value: CPU core number           |
| max.connections.size.per.query (?) | int        | The maximum connection number allocated by each query of each physical database. default value: 1 |
| check.table.metadata.enabled (?)   | boolean    | Check meta-data consistency or not in initialization, default value: false                        |
| query.with.cipher.column (?)       | boolean    | When there is a plainColumn, use cipherColumn or not to query, default value: true                |
| allow.range.query.with.inline.sharding (?)    | boolean   | Allow or not execute range query with inline sharding strategy, default value: false        |

### Read-Write Split

#### MasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                       |
| --------------------- | ---------------------------- | ----------------------------------- |
| dataSourceMap         | Map\<String, DataSource\>    | Mapping of data source and its name |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Master slave rule configuration     |
| props (?)             | Properties                   | Property configurations             |

#### MasterSlaveRuleConfiguration

| *Name*                   | *DataType*                      | *Explanation*                     |
| ------------------------ | ------------------------------- | --------------------------------- |
| name                     | String                          | Read-write split data source name |
| masterDataSourceName     | String                          | Master database source name       |
| slaveDataSourceNames     | Collection\<String\>            | Slave database source name list   |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | Slave database load balance       |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                             | *Data Type* | *Explanation*                                                |
| ---------------------------------- | ----------- | ------------------------------------------------------------ |
| sql.show (?)                       | boolean     | Print SQL parse and rewrite log or not, default value: false |
| executor.size (?)                  | int         | Be used in work thread number implemented by SQL; no limits if it is 0. default value: 0 |
| max.connections.size.per.query (?) | int         | The maximum connection number allocated by each query of each physical database, default value: 1 |
| check.table.metadata.enabled (?)   | boolean     | Check meta-data consistency or not in initialization, default value: false |

### Data Masking

#### EncryptDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*      |
| --------------------- | ---------------------------- | ------------------ |
| dataSource            | DataSource                   | Data source        |
| encryptRuleConfig     | EncryptRuleConfiguration     | encrypt rule configuration |
| props (?)             | Properties                   | Property configurations |

#### EncryptRuleConfiguration

| *Name*              | *DataType*                                  | *Explanation*                                               |
| ------------------- | ------------------------------------------- | ----------------------------------------------------------- |
| encryptors          | Map<String, EncryptorRuleConfiguration>     | Encryptor names and encryptors                              |
| tables              | Map<String, EncryptTableRuleConfiguration>  | Encrypt table names and encrypt tables                      |

#### Properties

Property configuration items, can be of the following properties.

| *Name*                            | *DataType*| *Explanation*                                                                        |
| ----------------------------------| --------- | ------------------------------------------------------------------------------------ |
| sql.show (?)                      | boolean   | Print SQL parse and rewrite log or not, default value: false                         |
| query.with.cipher.column (?)      | boolean   | When there is a plainColumn, use cipherColumn or not to query, default value: true   |

### Orchestration

#### OrchestrationShardingDataSourceFactory

| *Name*              | *DataType*                 | *Explanation*                          |
| ------------------- | -------------------------- | -------------------------------------- |
| dataSourceMap       | Map\<String, DataSource\>  | Same as `ShardingDataSourceFactory`    |
| shardingRuleConfig  | ShardingRuleConfiguration  | Same as `ShardingDataSourceFactory`    |
| props (?)           | Properties                 | Same as `ShardingDataSourceFactory`    |
| orchestrationConfig | OrchestrationConfiguration | Orchestration rule configurations |

#### OrchestrationMasterSlaveDataSourceFactory

| *Name*                | *Data Type*                  | *Explanation*                          |
| --------------------- | ---------------------------- | -------------------------------------- |
| dataSourceMap         | Map<String, DataSource>      | Same as `MasterSlaveDataSourceFactory` |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Same as `MasterSlaveDataSourceFactory` |
| configMap (?)         | Map<String, Object>          | Same as `MasterSlaveDataSourceFactory` |
| props (?)             | Properties                   | Same as `ShardingDataSourceFactory`    |
| orchestrationConfig   | OrchestrationConfiguration   | Orchestration rule configurations |

#### OrchestrationEncryptDataSourceFactory

| *Name*                | *DataType*                   | *Explanation*                      |
| --------------------- | ---------------------------- | ---------------------------------- |
| dataSource            | DataSource                   | Same as `EncryptDataSourceFactory` |
| encryptRuleConfig     | EncryptRuleConfiguration     | Same as `EncryptDataSourceFactory` |
| props (?)             | Properties                   | Same as `EncryptDataSourceFactory` |
| orchestrationConfig   | OrchestrationConfiguration   | Orchestration rule configurations  |


#### OrchestrationConfiguration

| *Name*          | *Data Type*                 | *Explanation*                                                |
| --------------- | --------------------------- | ------------------------------------------------------------ |
| instanceConfigurationMap | Map\<String, CenterConfiguration\>  | config map of config-center&registry-center，the key is center's name，the value is the config-center/registry-center   |


#### CenterConfiguration

| *Name*                           | *Data Type* | *Explanation*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| instanceType                      | String     | The type of center instance(zookeeper/etcd/apollo/nacos)                                       |
| properties                        | String     | Properties for center instance config, such as options of zookeeper                        |
| orchestrationType                 | String     | The type of orchestration center: config_center or registry_center or metadata_center, if both, use "setOrchestrationType("registry_center,config_center,metadata_center");"                  |
| serverLists                       | String     | Connect to server lists in center, including IP address and port number; addresses are separated by commas, such as `host1:2181,host2:2181` |
| namespace (?)                     | String     | Namespace of center instance                                                                    |

Common configuration in properties as follow:

| *Name*                           | *Data Type* | *Explanation*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| overwrite                         | boolean    | Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations                          |

If type of center is `zookeeper` with config-center&registry-center&metadata-center, properties could be set with the follow options:

| *Name*                           | *Data Type* | *Explanation*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| digest (?)                       | String      | Connect to authority tokens in registry center; default indicates no need for authority |
| operationTimeoutMilliseconds (?) | int         | The operation timeout millisecond number, default to be 500 milliseconds |
| maxRetries (?)                   | int         | The maximum retry count, default to be 3 times               |
| retryIntervalMilliseconds (?)    | int         | The retry interval millisecond number, default to be 500 milliseconds |
| timeToLiveSeconds (?)            | int         | The living time for temporary nodes, default to be 60 seconds |

If type of center is `etcd` with config-center&registry-center&metadata-center, properties could be set with the follow options:

| *Name*                           | *Data Type* | *Explanation*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| timeToLiveSeconds (?)            | long        | The etcd TTL in seconds, default to be 30 seconds            |

If type of center is `apollo` with config-center, properties could be set with the follow options:

| *Name*                           | *Data Type* | *Explanation*                                                |
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

| *Name*                           | *Data Type* | *Explanation*                                                |
| -------------------------------- | ----------- | ------------------------------------------------------------ |
| group (?)          | String        | Nacos group, "SHARDING_SPHERE_DEFAULT_GROUP" in default                  |
| timeout (?)        | long          | Nacos timeout, default to be 3000 milliseconds                           |
