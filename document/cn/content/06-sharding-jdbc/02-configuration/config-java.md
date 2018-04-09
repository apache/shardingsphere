+++
toc = true
title = "Java配置"
weight = 2
+++

## JAVA配置

### 配置示例

#### 分库分表
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
         Map<String, DataSource> result = new HashMap<>(2, 1);
         result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
         result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
         return result;
     }
```

#### 读写分离
```java
     DataSource getMasterSlaveDataSource() throws SQLException {
         MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
         masterSlaveRuleConfig.setName("demo_ds_master_slave");
         masterSlaveRuleConfig.setMasterDataSourceName("demo_ds_master");
         masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("demo_ds_slave_0", "demo_ds_slave_1"));
         return MasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig);
     }
     
     Map<String, DataSource> createDataSourceMap() {
         final Map<String, DataSource> result = new HashMap<>(3, 1);
         result.put("demo_ds_master", DataSourceUtil.createDataSource("demo_ds_master"));
         result.put("demo_ds_slave_0", DataSourceUtil.createDataSource("demo_ds_slave_0"));
         result.put("demo_ds_slave_1", DataSourceUtil.createDataSource("demo_ds_slave_1"));
         return result;
     }
```

#### 分库分表 + 读写分离

```java
// 构建读写分离数据源, 读写分离数据源实现了DataSource接口, 可直接当做数据源处理. masterDataSource0, slaveDataSource00, slaveDataSource01等为使用DBCP等连接池配置的真实数据源
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

// 通过ShardingSlaveDataSourceFactory继续创建ShardingDataSource
ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig0);
shardingRuleConfig.getMasterSlaveRuleConfigs().add(masterSlaveRuleConfig1);

DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

#### 编排治理配置

##### Zookeeper配置示例

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

##### Etcd配置示例

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

