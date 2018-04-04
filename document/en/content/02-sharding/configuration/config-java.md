+++
toc = true
title = "Java configuration"
weight = 2
prev = "/02-sharding/configuration/config-overview/"
next = "/02-sharding/configuration/config-yaml/"
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

#### Sharding Configuration
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

#### Read-write splitting Configuration
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
