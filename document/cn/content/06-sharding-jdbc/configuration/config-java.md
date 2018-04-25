+++
toc = true
title = "Java配置"
weight = 1
+++

## 配置示例

### 数据分片

```java
     DataSource getShardingDataSource() throws SQLException {
         ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
         shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
         shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
         shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
         shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
         shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new ModuloShardingTableAlgorithm()));
         return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig);
     }
     
     TableRuleConfiguration getOrderTableRuleConfiguration() {
         TableRuleConfiguration result = new TableRuleConfiguration();
         result.setLogicTable("t_order");
         result.setActualDataNodes("ds_${0..1}.t_order_${0..1}");
         result.setKeyGeneratorColumnName("order_id");
         return result;
     }
     
     TableRuleConfiguration getOrderItemTableRuleConfiguration() {
         TableRuleConfiguration result = new TableRuleConfiguration();
         result.setLogicTable("t_order_item");
         result.setActualDataNodes("ds_${0..1}.t_order_item_${0..1}");
         return result;
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("ds_0", DataSourceUtil.createDataSource("ds_0"));
         result.put("ds_1", DataSourceUtil.createDataSource("ds_1"));
         return result;
     }
```

### 读写分离

```java
     DataSource getMasterSlaveDataSource() throws SQLException {
         MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
         masterSlaveRuleConfig.setName("ds_master_slave");
         masterSlaveRuleConfig.setMasterDataSourceName("ds_master");
         masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("ds_slave_0", "ds_slave_1"));
         return MasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig);
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>();
         result.put("ds_master", DataSourceUtil.createDataSource("ds_master"));
         result.put("ds_slave_0", DataSourceUtil.createDataSource("ds_slave_0"));
         result.put("ds_slave_1", DataSourceUtil.createDataSource("ds_slave_1"));
         return result;
     }
```

### 数据分片 + 读写分离

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
        result.setActualDataNodes("ds_${0..1}.t_order_${[0, 1]}");
        result.setKeyGeneratorColumnName("order_id");
        return result;
    }
    
    TableRuleConfiguration getOrderItemTableRuleConfiguration() {
        TableRuleConfiguration result = new TableRuleConfiguration();
        result.setLogicTable("t_order_item");
        result.setActualDataNodes("ds_${0..1}.t_order_item_${[0, 1]}");
        return result;
    }
    
    List<MasterSlaveRuleConfiguration> getMasterSlaveRuleConfigurations() {
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig1.setName("ds_0");
        masterSlaveRuleConfig1.setMasterDataSourceName("ds_master_0");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("ds_master_0_slave_0", "ds_master_0_slave_1"));
        
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig2.setName("ds_1");
        masterSlaveRuleConfig2.setMasterDataSourceName("ds_master_1");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("ds_master_1_slave_0", "ds_master_1_slave_1"));
        return Lists.newArrayList(masterSlaveRuleConfig1, masterSlaveRuleConfig2);
    }
    
    Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        
        result.put("ds_master_0", DataSourceUtil.createDataSource("ds_master_0"));
        result.put("ds_master_0_slave_0", DataSourceUtil.createDataSource("ds_master_0_slave_0"));
        result.put("ds_master_0_slave_1", DataSourceUtil.createDataSource("ds_master_0_slave_1"));
        result.put("ds_master_1", DataSourceUtil.createDataSource("ds_master_1"));
        result.put("ds_master_1_slave_0", DataSourceUtil.createDataSource("ds_master_1_slave_0"));
        result.put("ds_master_1_slave_1", DataSourceUtil.createDataSource("ds_master_1_slave_1"));
        
        MasterSlaveRuleConfiguration masterSlaveRuleConfig1 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig1.setName("ds_0");
        masterSlaveRuleConfig1.setMasterDataSourceName("ds_master_0");
        masterSlaveRuleConfig1.setSlaveDataSourceNames(Arrays.asList("ds_master_0_slave_0", "ds_master_0_slave_1"));
        
        MasterSlaveRuleConfiguration masterSlaveRuleConfig2 = new MasterSlaveRuleConfiguration();
        masterSlaveRuleConfig2.setName("ds_1");
        masterSlaveRuleConfig2.setMasterDataSourceName("ds_master_1");
        masterSlaveRuleConfig2.setSlaveDataSourceNames(Arrays.asList("ds_master_1_slave_0", "ds_master_1_slave_1"));
        
        return result;
    }
```

### 使用Zookeeper的数据治理

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new HashMap<String, Object>(), new Properties(), 
                     new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
    
    private RegistryCenterConfiguration getRegistryCenterConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        result.setServerLists("localhost:2181");
        result.setNamespace("orchestration-demo");
        return result;
    }
```

### 使用Etcd的数据治理

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new HashMap<String, Object>(), new Properties(), 
                 new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
    
    private RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists("http://localhost:2379");
        return result;
    }
```

## 配置项说明

### 数据分片

#### ShardingDataSourceFactory

数据分片的数据源创建工厂。

| *名称*             | *数据类型*                 | *说明*              |
| ------------------ |  ------------------------ | ------------------ |
| dataSourceMap      | Map\<String, DataSource\> | 数据源与其名称的映射 |
| shardingRuleConfig | ShardingRuleConfiguration | 分库分表配置规则     |
| configMap (?)      | Map\<String, Object\>     | 配置映射关系        |
| props (?)          | Properties                | 相关属性配置        |

#### ShardingRuleConfiguration

分片规则配置对象。

| *名称*                                     | *数据类型*                                  | *说明*                                          |
| ----------------------------------------- | ------------------------------------------ | ----------------------------------------------- |
| defaultDataSourceName (?)                 | String                                     | 默认数据源名称，未配置分片规则的表将通过默认数据源定位 |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration              | 默认分库策略                                      |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration              | 默认分表策略                                      |
| defaultKeyGenerator (?)                   | KeyGenerator                               | 默认自增列值生成器                                 |
| tableRuleConfigs                          | Collection\<TableRuleConfiguration\>       | 分片规则列表                                      |
| bindingTableGroups (?)                    | Collection\<String\>                       | 绑定表规则列表                                    |
| masterSlaveRuleConfigs (?)                | Collection\<MasterSlaveRuleConfiguration\> | 读写分离规则，不填写表示不使用读写分离               |

#### TableRuleConfiguration

表分片规则配置对象。

| *名称*                              | *数据类型*                     | *说明*                                                                                                            |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| logicTable                         | String                        | 逻辑表名称                                                                                                         |
| actualDataNodes (?)                | String                        | 描述数据源名称 + 真实表名称，用点间隔，多个数据节点间用逗号分隔，可支持行表达式。不填写表示不分表。例如：ds${0..7}.tbl_${0..7} |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | 分库策略，不填写表示使用默认分库策略                                                                                  |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | 分表策略，不填写表示使用默认分表策略                                                                                  |
| logicIndex (?)                     | String                        | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表        |
| keyGeneratorColumnName (?)         | String                        | 自增列名称，不填写表示不使用自增主键生成器                                                                             |
| keyGenerator (?)                   | KeyGenerator                  | 自增列值生成器，如果填写了keyGeneratorColumnName，不填写keyGenerator，表示使用默认自增主键生成器                          |

#### StandardShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置标准分片策略。

| *名称*                      | *数据类型*                | *说明*                         |
| -------------------------- | ------------------------ | ------------------------------ |
| shardingColumn             | String                   | 分片列名                        |
| preciseShardingAlgorithm   | PreciseShardingAlgorithm | 精确的分片算法类名称，用于=和IN   |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm   | 范围的分片算法类名称，用于BETWEEN |

#### ComplexShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置复合分片策略。

| *名称*             | *数据类型*                    | *说明*                  |
| ----------------- | ---------------------------- | ----------------------- |
| shardingColumns   | String                       | 分片列名，多个列以逗号分隔 |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | 配置复合分片算法          |

#### InlineShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置行表达式分片策略。

| *名称*               | *数据类型*  | *说明*                                                                           |
| ------------------- | ----------- | ------------------------------------------------------------------------------- |
| shardingColumn      |  String     | 分片列名                                                                         |
| algorithmExpression |  String     | 分片算法表达式，详情请参考[行表达式](/02-sharding/other-features/inline-expression) |

#### HintShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置Hint方式分片策略。

| *名称*             | *数据类型*             | *说明*      |
| ----------------- | --------------------- | ----------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint分片算法 |

#### NoneShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置不分片的策略。

#### ShardingPropertiesConstant

属性配置项，可以为以下属性。

| *名称*             | *数据类型* | *说明*                          |
| ----------------- | --------- | ------------------------------- |
| sql.show (?)      | boolean   | 是否开启SQL显示，默认为false不开启 |
| executor.size (?) | int       | 最大工作线程数量                  |

#### configMap

为用户提供的自定义透传的配置项。

### 读写分离

#### MasterSlaveDataSourceFactory

读写分离的数据源创建工厂。

| *名称*                 | *数据类型*                    | *说明*             |
| --------------------- | ---------------------------- | ------------------ |
| dataSourceMap         | Map\<String, DataSource\>    | 数据源与其名称的映射 |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 读写分离规则        |
| configMap (?)         | Map\<String, Object\>        | 配置映射关系        |

#### MasterSlaveRuleConfiguration

读写分离规则配置对象。

| *名称*                    | *数据类型*                       | *说明*         |
| ------------------------ | ------------------------------- | -------------- |
| name                     | String                          | 读写分离配置名称 |
| masterDataSourceName     | String                          | 主库数据源      |
| slaveDataSourceNames     | Collection\<String\>            | 从库数据源列表   |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | 主从库访问策略   |

#### configMap

为用户提供的自定义透传的配置项。

### 数据治理

#### OrchestrationShardingDataSourceFactory

数据分片 + 数据治理的数据源创建工厂。

| *名称*               | *数据类型*                  | *说明*                      |
| ------------------- |  ------------------------- | --------------------------- |
| dataSourceMap       | Map\<String, DataSource\>  | 同ShardingDataSourceFactory |
| shardingRuleConfig  | ShardingRuleConfiguration  | 同ShardingDataSourceFactory |
| configMap (?)       | Map\<String, Object\>      | 同ShardingDataSourceFactory |
| props (?)           | Properties                 | 同ShardingDataSourceFactory |
| orchestrationConfig | OrchestrationConfiguration | 数据治理规则配置              |

#### OrchestrationMasterSlaveDataSourceFactory

读写分离 + 数据治理的数据源创建工厂。

| *名称*                 | *数据类型*                    | *说明*                         |
| --------------------- | ---------------------------- | ------------------------------ |
| dataSourceMap         | Map\<String, DataSource\>    | 同MasterSlaveDataSourceFactory |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 同MasterSlaveDataSourceFactory |
| configMap (?)         | Map\<String, Object\>        | 同MasterSlaveDataSourceFactory |
| orchestrationConfig   | OrchestrationConfiguration   | 数据治理规则配置                 |
 
#### OrchestrationConfiguration

数据治理规则配置对象。

| *名称*           | *数据类型*                   | *说明*                                 |
| --------------- | --------------------------- | -------------------------------------- |
| name            | String                      | 数据治理实例名称                         |
| regCenterConfig | RegistryCenterConfiguration | 注册中心配置                             |
| overwrite       | boolean                     | 本地配置是否覆盖注册中心配置               |
| type            | String                      | 数据源类型，可选值：sharding，masterslave |

#### ZookeeperConfiguration

RegistryCenterConfiguration的实现类，用于配置Zookeeper注册中心。

| *名称*                         | *数据类型* | *说明*                                                                     |
| ----------------------------- | ---------- | ------------------------------------------------------------------------- |
| serverLists                   | String     | Zookeeper连接地址，多个Zookeeper用逗号分隔，如：localhost:2181,localhost:3181 |
| namespace                     | String     | Zookeeper的命名空间                                                        |
| baseSleepTimeMilliseconds (?) | int        | 连接失败的初始等待毫秒数，默认1000毫秒                                        |
| maxSleepTimeMilliseconds (?)  | int        | 连接失败的最大等待毫秒数，默认3000毫秒                                        |
| maxRetries (?)                | int        | 连接失败后的最大重试次数，默认3次                                             |
| sessionTimeoutMilliseconds    | int        | 会话超时毫秒数                                                              |
| connectionTimeoutMilliseconds | int        | 连接超时毫秒数                                                              |
| digest (?)                    | String     | 连接凭证                                                                   |

#### EtcdConfiguration

RegistryCenterConfiguration的实现类，用于配置Etcd注册中心。

| *名称*                         | *数据类型* | *说明*                                                                         |
| ----------------------------- | ---------- | ----------------------------------------------------------------------------- |
| serverLists                   | String     | Etcd连接地址，多个Etcd用逗号分隔，如：http://localhost:2379,http://localhost:3379 |
| timeToLiveSeconds (?)         | int        | 数据存活秒数，默认60秒                                                           |
| timeoutMilliseconds (?)       | int        | 请求超时毫秒数，默认500毫秒                                                      |
| retryIntervalMilliseconds (?) | int        | 重试间隔毫秒数，默认200毫秒                                                      |
| maxRetries (?)                | int        | 请求失败后的最大重试次数，默认3次                                                 |
