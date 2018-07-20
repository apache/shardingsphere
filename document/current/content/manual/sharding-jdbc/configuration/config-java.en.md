+++
toc = true
title = "Java"
weight = 1
+++

## Example

### Sharding 

```java
     DataSource getShardingDataSource() throws SQLException {
         ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
         shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
         shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
         shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
         shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds${user_id % 2}"));
         shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new ModuloShardingTableAlgorithm()));
         return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig);
     }
     
     TableRuleConfiguration getOrderTableRuleConfiguration() {
         TableRuleConfiguration result = new TableRuleConfiguration();
         result.setLogicTable("t_order");
         result.setActualDataNodes("ds${0..1}.t_order${0..1}");
         result.setKeyGeneratorColumnName("order_id");
         return result;
     }
     
     TableRuleConfiguration getOrderItemTableRuleConfiguration() {
         TableRuleConfiguration result = new TableRuleConfiguration();
         result.setLogicTable("t_order_item");
         result.setActualDataNodes("ds${0..1}.t_order_item${0..1}");
         return result;
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("ds0", DataSourceUtil.createDataSource("ds0"));
         result.put("ds1", DataSourceUtil.createDataSource("ds1"));
         return result;
     }
```

### Read-write splitting
 
```java
     DataSource getMasterSlaveDataSource() throws SQLException {
         MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
         masterSlaveRuleConfig.setName("ds_master_slave");
         masterSlaveRuleConfig.setMasterDataSourceName("ds_master");
         masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("ds_slave0", "ds_slave1"));
         return MasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig, new LinkedHashMap<String, Object>(), new Properties());
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("ds_master", DataSourceUtil.createDataSource("ds_master"));
         result.put("ds_slave0", DataSourceUtil.createDataSource("ds_slave0"));
         result.put("ds_slave1", DataSourceUtil.createDataSource("ds_slave1"));
         return result;
     }
```

### Sharding + Read-write splitting 

```java
    DataSource getShardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
        shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", ModuloShardingDatabaseAlgorithm.class.getName()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", ModuloShardingTableAlgorithm.class.getName()));
        shardingRuleConfig.setMasterSlaveRuleConfigs(getMasterSlaveRuleConfigurations());
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new HashMap<String, Object>(), new Properties());
    }
    
    TableRuleConfiguration getOrderTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order");
        result.setActualDataNodes("ds${0..1}.t_order${[0, 1]}");
        result.setKeyGeneratorColumnName("order_id");
        return result;
    }
    
    TableRuleConfiguration getOrderItemTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order_item");
        result.setActualDataNodes("ds${0..1}.t_order_item${[0, 1]}");
        return result;
    }
    
    List<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig1.setName("ds0");
        masterSlaveRuleConfig1.setMasterDataSourceName("ds_master0");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("ds_master0_slave0", "ds_master0_slave1"));
        
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig2.setName("ds1");
        masterSlaveRuleConfig2.setMasterDataSourceName("ds_master1");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("ds_master1_slave0", "ds_master1_slave1"));
        return Lists.newArrayList(masterSlaveRuleConfig1, masterSlaveRuleConfig2);
    }
    
    Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        
        result.put("ds_master0", DataSourceUtil.createDataSource("ds_master0"));
        result.put("ds_master0_slave0", DataSourceUtil.createDataSource("ds_master0_slave0"));
        result.put("ds_master0_slave1", DataSourceUtil.createDataSource("ds_master0_slave1"));
        result.put("ds_master1", DataSourceUtil.createDataSource("ds_master1"));
        result.put("ds_master1_slave0", DataSourceUtil.createDataSource("ds_master1_slave0"));
        result.put("ds_master1_slave1", DataSourceUtil.createDataSource("ds_master1_slave1"));
        
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig1.setName("ds0");
        masterSlaveRuleConfig1.setMasterDataSourceName("ds_master0");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("ds_master0_slave0", "ds_master0_slave1"));
        
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig2.setName("ds1");
        masterSlaveRuleConfig2.setMasterDataSourceName("ds_master1");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("ds_master1_slave0", "ds_master1_slave1"));
        
        return result;
    }
```

### Orchestration by Zookeeper

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new HashMap<String, Object>(), new Properties(), 
                 new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false, OrchestrationType.SHARDING));
    
    private RegistryCenterConfiguration getRegistryCenterConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        result.setServerLists("localhost:2181");
        result.setNamespace("orchestration-demo");
        return result;
    }
```

### Orchestration by Etcd

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new HashMap<String, Object>(), new Properties(), 
                 new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false, OrchestrationType.SHARDING));
    
    private RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists("http://localhost:2379");
        return result;
    }
```

## Configuration reference

### Sharding

#### ShardingDataSourceFactory

| *Name*             | *DataType*                | *Description*               |
| ------------------ |  ------------------------ | --------------------------- |
| dataSourceMap      | Map\<String, DataSource\> | Data sources configuration  |
| shardingRuleConfig | ShardingRuleConfiguration | Sharding rule configuration |
| configMap (?)      | Map\<String, Object\>     | Config map                  |
| props (?)          | Properties                | Properties                  |

#### ShardingRuleConfiguration

| *Name*                                    | *DataType*           | *Description*                                                                                                   |
| ----------------------------------------- | ------------------------------------------ | ----------------------------------------------------------------------------------------- |
| tableRuleConfigs                          | Collection\<TableRuleConfiguration\>       | Table rule configuration                                                                  |
| bindingTableGroups (?)                    | Collection\<String\>                       | Binding table groups                                                                      |
| defaultDataSourceName (?)                 | String                                     | If table not configure at table rule, will route to defaultDataSourceName                 |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration              | Default strategy for sharding databases                                                   |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration              | Default strategy for sharding tables                                                      |
| defaultKeyGenerator (?)                   | KeyGenerator                               | Default key generator, default value is `io.shardingsphere.core.keygen.DefaultKeyGenerator` |
| masterSlaveRuleConfigs (?)                | Collection\<MasterSlaveRuleConfiguration\> | Read-write splitting rule configuration                                                   |

#### TableRuleConfiguration

| *Name*                             | *DataType*                    | *Description*                                                                                                                                                                                         |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logicTable                         | String                        | Name of logic table                                                                                                                                                                                   |
| actualDataNodes (?)                | String                        | Describe data source names and actual tables, delimiter as point, multiple data nodes split by comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | Databases sharding strategy, use default databases sharding strategy if absent                                                                                                                        |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | Tables sharding strategy, use default databases sharding strategy if absent                                                                                                                           |
| logicIndex (?)                     | String                        | Name if logic index. If use *DROP INDEX XXX* SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables                                                                    |
| keyGeneratorColumnName (?)         | String                        | Key generator column name, do not use Key generator if absent                                                                                                                                         |
| keyGenerator (?)                   | KeyGenerator                  | Key generator, use default key generator if absent                                                                                                                                                    |

#### StandardShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*                     | *DataType*               | *Description*                                    |
| -------------------------- | ------------------------ | ------------------------------------------------ |
| shardingColumn             | String                   | Name of sharding column                          |
| preciseShardingAlgorithm   | PreciseShardingAlgorithm | Precise sharding algorithm used for `=` and `IN` |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm   | Range sharding algorithm used for `BETWEEN`      |

#### ComplexShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*            | *DataType*                   | *Description*                                                    |
| ----------------- | ---------------------------- | ---------------------------------------------------------------- |
| shardingColumns   | String                       | Names of sharding columns. Multiple columns separated with comma |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | Complex sharding algorithm                                       |

#### InlineShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*              | *DataType*  | *Description*                                                                                                                                    |
| ------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| shardingColumn      |  String     | Name of sharding column                                                                                                                          |
| algorithmExpression |  String     | Inline expression for sharding algorithm, more details please reference [Inline expression](/en/features/sharding/other-features/inline-expression) |

#### HintShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

| *Name*            | *DataType*            | *Description*           |
| ----------------- | --------------------- | ----------------------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint sharding algorithm |

#### NoneShardingStrategyConfiguration

Subclass of ShardingStrategyConfiguration.

#### ShardingPropertiesConstant

Enumeration of properties.

| *Name*            | *DataType* | *Description*                                           |
| ----------------- | ---------- | ------------------------------------------------------- |
| sql.show (?)      | boolean    | To show SQLS or not, default value: false               |
| executor.size (?) | int        | The number of working threads, default value: CPU count |

#### configMap

User-defined arguments.

### Read-write splitting

#### MasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Description*                       |
| --------------------- | ---------------------------- | ----------------------------------- |
| dataSourceMap         | Map\<String, DataSource\>    | Map of data sources and their names |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Master slave rule configuration     |
| configMap (?)         | Map\<String, Object\>        | Config map                          |

#### MasterSlaveRuleConfiguration

| *Name*                   | *DataType*                      | *Description*                    |
| ------------------------ | ------------------------------- | -------------------------------- |
| name                     | String                          | Name of master slave data source |
| masterDataSourceName     | String                          | Name of master data source       |
| slaveDataSourceNames     | Collection\<String\>            | Names of Slave data sources      |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | Load balance algorithm           |

#### configMap

User-defined arguments.

### Orchestration

#### OrchestrationShardingDataSourceFactory

| *Name*              | *DataType*                 | *Description*                       |
| ------------------- |  ------------------------- | ----------------------------------- |
| dataSourceMap       | Map\<String, DataSource\>  | Same with ShardingDataSourceFactory |
| shardingRuleConfig  | ShardingRuleConfiguration  | Same with ShardingDataSourceFactory |
| configMap (?)       | Map\<String, Object\>      | Same with ShardingDataSourceFactory |
| props (?)           | Properties                 | Same with ShardingDataSourceFactory |
| orchestrationConfig | OrchestrationConfiguration | Orchestration configuration         |

#### OrchestrationMasterSlaveDataSourceFactory

| *Name*                | *DataType*                   | *Description*                          |
| --------------------- | ---------------------------- | -------------------------------------- |
| dataSourceMap         | Map\<String, DataSource\>    | Same with MasterSlaveDataSourceFactory |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | Same with MasterSlaveDataSourceFactory |
| configMap (?)         | Map\<String, Object\>        | Same with MasterSlaveDataSourceFactory |
| orchestrationConfig   | OrchestrationConfiguration   | Orchestration configuration            |
 
#### OrchestrationConfiguration

| *Name*          | *DataType*                  | *Description*                                                       |
| --------------- | --------------------------- | ------------------------------------------------------------------- |
| name            | String                      | Name of orchestration instance                                      |
| overwrite       | boolean                     | Use local configuration to overwrite registry center or not         |
| type            | OrchestrationType           | Data source type, values should be: `SHARDING` or `MASTER_SLAVE`    |
| regCenterConfig | RegistryCenterConfiguration | Registry center configuration                                       |

#### ZookeeperConfiguration

Subclass of RegistryCenterConfiguration.

| *Name*                            | *DataType* | *Description*                                                                   |
| --------------------------------- | ---------- | ------------------------------------------------------------------------------- |
| serverLists                       | String     | Zookeeper servers list, multiple split as comma. Example: host1:2181,host2:2181 |
| namespace                         | String     | Namespace of zookeeper                                                          |
| baseSleepTimeMilliseconds (?)     | int        | Initial milliseconds of waiting for retry, default value is 1000 milliseconds   |
| maxSleepTimeMilliseconds (?)      | int        | Maximum milliseconds of waiting for retry, default value is 3000 milliseconds   |
| maxRetries (?)                    | int        | Max retries times if connect failure, default value is 3                        |
| sessionTimeoutMilliseconds (?)    | int        | Session timeout milliseconds, default value is 60000 milliseconds               |
| connectionTimeoutMilliseconds (?) | int        | Connection timeout milliseconds, default value is 15000 milliseconds            |
| digest (?)                        | String     | Connection digest                                                               |

#### EtcdConfiguration

Subclass of RegistryCenterConfiguration.

| *Name*                        | *DataType* | *Description*                                                                            |
| ----------------------------- | ---------- | ---------------------------------------------------------------------------------------- |
| serverLists                   | String     | Etcd servers list, multiple split as comma. Example: http://host1:2379,http://host2:2379 |
| timeToLiveSeconds (?)         | int        | Time to live of data, default is 60 seconds                                              |
| timeoutMilliseconds (?)       | int        | Timeout milliseconds, default is 500 milliseconds                                        |
| retryIntervalMilliseconds (?) | int        | Milliseconds of retry interval, default is w00 milliseconds                              |
| maxRetries (?)                | int        | Max retries times if request failure, default value is 3                                 |
