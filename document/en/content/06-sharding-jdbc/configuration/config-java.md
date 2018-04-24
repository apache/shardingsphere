+++
toc = true
title = "Java"
weight = 1
+++

## JAVA configuration

### Import the dependency of maven

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configuration Example

#### Sharding 

```java
     DataSource getShardingDataSource() throws SQLException {
         ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
         shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
         shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
         shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
         shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "demo_ds_${user_id % 2}"));
         shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", ModuloShardingTableAlgorithm.class.getName()));
         return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig);
     }
     
     TableRuleConfiguration getOrderTableRuleConfiguration() {
         TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
         orderTableRuleConfig.setLogicTable("t_order");
         orderTableRuleConfig.setActualDataNodes("demo_ds_${0..1}.t_order_${0..1}");
         orderTableRuleConfig.setKeyGeneratorColumnName("order_id");
         return orderTableRuleConfig;
     }
     
     TableRuleConfiguration getOrderItemTableRuleConfiguration() {
         TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
         orderItemTableRuleConfig.setLogicTable("t_order_item");
         orderItemTableRuleConfig.setActualDataNodes("demo_ds_${0..1}.t_order_item_${0..1}");
         return orderItemTableRuleConfig;
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
         result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
         return result;
     }
```

#### Read-write splitting 
```java
     DataSource getMasterSlaveDataSource() throws SQLException {
         MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
         masterSlaveRuleConfig.setName("demo_ds_master_slave");
         masterSlaveRuleConfig.setMasterDataSourceName("demo_ds_master");
         masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("demo_ds_slave_0", "demo_ds_slave_1"));
         return MasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig);
     }
     
     Map<String, DataSource> createDataSourceMap() {
         final Map<String, DataSource> result = new HashMap<>();
         result.put("demo_ds_master", DataSourceUtil.createDataSource("demo_ds_master"));
         result.put("demo_ds_slave_0", DataSourceUtil.createDataSource("demo_ds_slave_0"));
         result.put("demo_ds_slave_1", DataSourceUtil.createDataSource("demo_ds_slave_1"));
         return result;
     }
```

#### Sharding + Read-write splitting 

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
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${[0, 1]}");
        orderTableRuleConfig.setKeyGeneratorColumnName("order_id");
        return orderTableRuleConfig;
    }
    
    TableRuleConfiguration getOrderItemTableRuleConfiguration() {
        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_item_${[0, 1]}");
        return orderItemTableRuleConfig;
    }
    
    List<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig1.setName("ds_0");
        masterSlaveRuleConfig1.setMasterDataSourceName("demo_ds_master_0");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("demo_ds_master_0_slave_0", "demo_ds_master_0_slave_1"));
    
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig2.setName("ds_1");
        masterSlaveRuleConfig2.setMasterDataSourceName("demo_ds_master_1");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("demo_ds_master_1_slave_0", "demo_ds_master_1_slave_1"));
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
    
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig1.setName("ds_0");
        masterSlaveRuleConfig1.setMasterDataSourceName("demo_ds_master_0");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("demo_ds_master_0_slave_0", "demo_ds_master_0_slave_1"));
    
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig2.setName("ds_1");
        masterSlaveRuleConfig2.setMasterDataSourceName("demo_ds_master_1");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("demo_ds_master_1_slave_0", "demo_ds_master_1_slave_1"));
    
        return result;
    }
```

##### Introduction for config items

##### Sharding

##### ShardingDataSourceFactory

| *Name*                         | *DataType*  |  *Required* | *Info*         |
| ----------------------------- |  --------- | ------ | -------------- |
| dataSourceMap                 |  Map\<String, DataSource\>     |   Y   | The map of datasource and its name.|
| shardingRuleConfig               |   ShardingRuleConfiguration        |   Y   | Sharding Rule.        |
| configMap?                  |   Map\<String, Object\>        |   N   |         config map. |
| props?                        |   Properties         |   N   | Property Config.     |

##### ShardingRuleConfiguration

| *Name*                         | *DataType*  |  *Required* | *Info*                                                                |
| ------------------------------- | ---------- | ------ | --------------------------------------------------------------------- |
| defaultDataSourceName?     | String      |   N   | The default data source.                        |
| defaultDatabaseShardingStrategyConfig? | ShardingStrategyConfiguration      |   N   | The default strategy for sharding databases.   |
| defaultTableShardingStrategyConfig?    | ShardingStrategyConfiguration      |   N   | The default strategy for sharding tables.  |
| defaultKeyGenerator? | KeyGenerator |N|The class name of key generator.
| tableRuleConfigs                    |   Collection\<TableRuleConfiguration\>         |   Y   | The list of table rules.                                                            |
| bindingTableGroups?            | Collection\<String\>      | N| Blinding Rule.|
| masterSlaveRuleConfigs? | Collection\<MasterSlaveRuleConfiguration\>|N|The read-write-splitting configs.|


##### TableRuleConfiguration

| *Name*                         | *DataType*  |  *Required* | *Info*  |
| --------------------         | ---------- | ------ | ------- |
| logicTable                 |  String     |   Y   | LogicTables. |
| actualDataNodes?             |  String     |   N   | Actual data nodes configured in the format of *datasource_name.table_name*, multiple configs separated with commas.|
| databaseShardingStrategyConfig?      |  ShardingStrategyConfiguration     |   N   | The strategy for sharding databases.  |
| tableShardingStrategyConfig?            |  ShardingStrategyConfiguration     |   N   | The strategy for sharding tables.       |
| logicIndex?                   |  String     |   N   | The Logic index name. If you want to use *DROP INDEX XXX* SQL in Oracle/PostgreSQL，This property needs to be set for finding the actual tables.        |
| keyGeneratorColumnName? | String | N | The generate column.|
| keyGenerator?  | KeyGenerator | N| The class name of key generator.|


##### StandardShardingStrategyConfiguration

The standard sharding strategy for single sharding column.

| *Name*                        | *DataType*  |  *Required* | *Info*                                                                |
| ------------------------------ | ---------- | ------ | --------------------------------------------------------------------- |
| shardingColumn             |  String     |   Y   | The name of sharding column.                                                                |
| preciseShardingAlgorithm      |  PreciseShardingAlgorithm     |   Y   | The  precise sharding algorithm used for = and IN. The default constructor or on-parametric constructor is needed.    |
| rangeShardingAlgorithm?      |  RangeShardingAlgorithm     |   N   | The class name for range sharding algorithm used for BETWEEN. The default constructor or on-parametric constructor is needed. |


##### ComplexShardingStrategyConfiguration

The complex sharding strategy for multiple sharding columns.

| *Name*                        | *DataType*  |  *Required* | *Info*                                              |
| ------------------------------ | ---------- | ------ | --------------------------------------------------- |
| shardingColumns             |  String     |   Y  |  The name of sharding column. Multiple names separated with commas.                               |
| shardingAlgorithm             |  ComplexKeysShardingAlgorithm     |   Y  | The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed. |

## InlineShardingStrategyConfiguration

The inline-expression sharding strategy.

| *Name*                        | *DataType*  |  *Required* | *Info*       |
| ------------------------------- | ---------- | ------ | ------------ |
| shardingColumn              |  String     |   Y   |The name of sharding column.      |
| algorithmExpression    |  String     |   Y   | The expression for sharding algorithm.|

##### HintShardingStrategyConfiguration

The Hint-method sharding strategy.

| *Name*                        | *DataType*  |  *Required* | *Info*                                              |
| ------------------------------- | ---------- | ------ | --------------------------------------------------- |
| shardingAlgorithm            |  HintShardingAlgorithm     |   Y  | The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed.  |

##### NoneShardingStrategyConfiguration

The none sharding strategy.

##### ShardingPropertiesConstant

| *Name*                        | *DataType*  |  *Required* | *Info*                              |
| ------------------------------------- | ---------- | ----- | ----------------------------------- |
| sql.show                            |  boolean   |   Y   | To show SQLS or not, the default is false.  |
| executor.size?                       |  int       |   N   | The number of running threads.                      |

##### configMap

##### Read-write splitting

##### MasterSlaveDataSourceFactory

| *Name*                        | *DataType*  |  *Required* | *Info*                                      |
| ------------------------------ |  --------- | ------ | ---------------------------------------- |
| dataSourceMap                 |  Map\<String, DataSource\>     |   Y   | The map of datasource and its name. |
| shardingRuleConfig               |   ShardingRuleConfiguration        |   Y   | The sharding rule config.      |
| configMap?                  |   Map\<String, Object\>        |   N   |         Config map.|


##### MasterSlaveRuleConfiguration

| *Name*                        | *DataType*  |  *Required* | *Info*                                     |
| ------------------------------ |  --------- | ------ | ---------------------------------------- |
| name                        |  String     |   Y   | The name of rule configuration.                          |
| masterDataSourceName      |   String        |   Y   | The master datasource.                       |
| slaveDataSourceNames      |   Collection\<String\>       |   Y   |  The list of Slave databases, multiple items are separated by commas.        |
| loadBalanceAlgorithm?               |  MasterSlaveLoadBalanceAlgorithm     |   N   | The load balance algorithm of master and slaves. |

##### configMap


#### Orchestration

##### Zookeeper

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new ConcurrentHashMap<String, Object>(), new Properties(), 
                     new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
     
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        result.setServerLists("localhost:2181");
        result.setNamespace("orchestration-demo");
        return result;
    }
```

##### Etcd

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new ConcurrentHashMap<String, Object>(), new Properties(), 
                 new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists("http://localhost:2379");
        return result;
    }
```

