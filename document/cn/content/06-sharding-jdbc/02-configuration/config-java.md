+++
toc = true
title = "Java配置"
weight = 2
+++

## JAVA配置

### 引入maven依赖

```xml
<!-- 引入sharding-jdbc核心模块 -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

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
         Map<String, DataSource> result = new HashMap<>();
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
         final Map<String, DataSource> result = new HashMap<>();
         result.put("demo_ds_master", DataSourceUtil.createDataSource("demo_ds_master"));
         result.put("demo_ds_slave_0", DataSourceUtil.createDataSource("demo_ds_slave_0"));
         result.put("demo_ds_slave_1", DataSourceUtil.createDataSource("demo_ds_slave_1"));
         return result;
     }
```

#### 分库分表 + 读写分离

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

##### 配置项说明

##### 分库分表

##### ShardingDataSourceFactory

| *名称*                         | *数据类型*  |  *必填* | *说明*         |
| ----------------------------- |  --------- | ------ | -------------- |
| dataSourceMap                 |  Map\<String, DataSource\>     |   是   | 数据源与其名称的映射 |
| shardingRuleConfig               |   ShardingRuleConfiguration        |   是   | 分库分表配置规则        |
| configMap?                  |   Map\<String, Object\>        |   否   |         配置映射关系|
| props?                        |   Properties         |   否   | 相关属性配置     |

##### ShardingRuleConfiguration

| *名称*                         | *数据类型*  |  *必填* | *说明*                                                                |
| ------------------------------- | ---------- | ------ | --------------------------------------------------------------------- |
| defaultDataSourceName?     | String      |   否   | 默认数据源名称，未配置分片规则的表将通过默认数据源定位                        |
| defaultDatabaseShardingStrategyConfig? | ShardingStrategyConfiguration      |   否   | 默认分库策略  |
| defaultTableShardingStrategyConfig?    | ShardingStrategyConfiguration      |   否   | 默认分表策略  |
| defaultKeyGenerator? | KeyGenerator |否|自增列值生成类名
| tableRuleConfigs                    |   Collection\<TableRuleConfiguration\>         |   是   | 分片规则列表                                                            |
| bindingTableGroups?            | Collection\<String\>      | 否| 绑定表规则|
| masterSlaveRuleConfigs? | Collection\<MasterSlaveRuleConfiguration\>|否|读写分离配置|


##### TableRuleConfiguration

| *名称*                         | *数据类型*  |  *必填* | *说明*  |
| --------------------         | ---------- | ------ | ------- |
| logicTable                 |  String     |   是   | 逻辑表名 |
| actualDataNodes?             |  String     |   否   | 真实数据节点|
| databaseShardingStrategyConfig?      |  ShardingStrategyConfiguration     |   否   | 分库策略  |
| tableShardingStrategyConfig?            |  ShardingStrategyConfiguration     |   否   | 分表策略       |
| logicIndex?                   |  String     |   否   | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表        |
| keyGeneratorColumnName? | String | 否 | 自增列名|
| keyGenerator?  | KeyGenerator | 否| 自增列值生成类|


##### StandardShardingStrategyConfiguration

标准分片策略，用于单分片键的场景

| *名称*                        | *数据类型*  |  *必填* | *说明*                                                                |
| ------------------------------ | ---------- | ------ | --------------------------------------------------------------------- |
| shardingColumn             |  String     |   是   | 分片列名                                                               |
| preciseShardingAlgorithm      |  PreciseShardingAlgorithm     |   是   | 精确的分片算法类名称，用于=和IN。该类需使用默认的构造器或者提供无参数的构造器   |
| rangeShardingAlgorithm?      |  RangeShardingAlgorithm     |   否   | 范围的分片算法类名称，用于BETWEEN。该类需使用默认的构造器或者提供无参数的构造器 |


##### ComplexShardingStrategyConfiguration

复合分片策略，用于多分片键的场景

| *名称*                        | *数据类型*  |  *必填* | *说明*                                              |
| ------------------------------ | ---------- | ------ | --------------------------------------------------- |
| shardingColumns             |  String     |   是  | 分片列名，多个列以逗号分隔                              |
| shardingAlgorithm             |  ComplexKeysShardingAlgorithm     |   是  | 分片算法全类名，该类需使用默认的构造器或者提供无参数的构造器 |

##### InlineShardingStrategyConfiguration

inline表达式分片策略

| *名称*                         | *数据类型*  |  *必填* | *说明*       |
| ------------------------------- | ---------- | ------ | ------------ |
| shardingColumn              |  String     |   是   | 分片列名      |
| algorithmExpression    |  String     |   是   | 分片算法表达式 |

##### HintShardingStrategyConfiguration

Hint方式分片策略

| *名称*                         | *数据类型*  |  *必填* | *说明*                                              |
| ------------------------------- | ---------- | ------ | --------------------------------------------------- |
| shardingAlgorithm            |  HintShardingAlgorithm     |   是  | 分片算法全类名，该类需使用默认的构造器或者提供无参数的构造器 |

##### NoneShardingStrategyConfiguration

不分片的策略

##### ShardingPropertiesConstant

| *名称*                               | *数据类型*  | *必填* | *说明*                              |
| ------------------------------------- | ---------- | ----- | ----------------------------------- |
| sql.show                            |  boolean   |   是   | 是否开启SQL显示，默认为false不开启     |
| executor.size?                       |  int       |   否   | 最大工作线程数量                      |

##### configMap

##### 读写分离

##### MasterSlaveDataSourceFactory

| *名称*                        | *数据类型*  |  *必填* | *说明*                                   |
| ------------------------------ |  --------- | ------ | ---------------------------------------- |
| dataSourceMap                 |  Map\<String, DataSource\>     |   是   | 数据源与其名称的映射 |
| shardingRuleConfig               |   ShardingRuleConfiguration        |   是   | 分库分表规则配置       |
| configMap?                  |   Map\<String, Object\>        |   否   |         配置映射关系|


##### MasterSlaveRuleConfiguration

| *名称*                        | *数据类型*  |  *必填* | *说明*                                   |
| ------------------------------ |  --------- | ------ | ---------------------------------------- |
| name                        |  String     |   是   | 读写分离配置名称                          |
| masterDataSourceName      |   String        |   是   | 主库数据源                       |
| slaveDataSourceNames      |   Collection\<String\>       |   是   | 从库数据源列表       |
| loadBalanceAlgorithm?               |  MasterSlaveLoadBalanceAlgorithm     |   否   | 主从库访问策略 |

##### configMap


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

