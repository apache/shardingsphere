+++
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

### 读写分离

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

### 数据脱敏

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

### 数据分片 + 读写分离

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

### 数据分片 + 数据脱敏

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

### 治理

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

## 配置项说明

### 数据分片

#### ShardingDataSourceFactory

数据分片的数据源创建工厂。

| *名称*             | *数据类型*                 | *说明*          |
| ------------------ |  ------------------------ | -------------- |
| dataSourceMap      | Map\<String, DataSource\> | 数据源配置      |
| shardingRuleConfig | ShardingRuleConfiguration | 数据分片配置规则 |
| props (?)          | Properties                | 属性配置        |

#### ShardingRuleConfiguration

分片规则配置对象。

| *名称*                                     | *数据类型*                                  | *说明*                                                                                         
| ----------------------------------------- | ------------------------------------------ | ----------------------------------------------------------------------------------------------- |
| tableRuleConfigs                          | Collection\<TableRuleConfiguration\>       | 分片规则列表                                                                                      |
| bindingTableGroups (?)                    | Collection\<String\>                       | 绑定表规则列表                                                                                    |
| broadcastTables (?)                       | Collection\<String\>                       | 广播表规则列表                                                                                    |
| defaultDataSourceName (?)                 | String                                     | 未配置分片规则的表将通过默认数据源定位                                                                |
| defaultDatabaseShardingStrategyConfig (?) | ShardingStrategyConfiguration              | 默认分库策略                                                                                      |
| defaultTableShardingStrategyConfig (?)    | ShardingStrategyConfiguration              | 默认分表策略                                                                                      |
| defaultKeyGeneratorConfig (?)             | KeyGeneratorConfiguration                  | 默认自增列值生成器配置，缺省将使用org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator |
| masterSlaveRuleConfigs (?)                | Collection\<MasterSlaveRuleConfiguration\> | 读写分离规则，缺省表示不使用读写分离                                                                  |

#### TableRuleConfiguration

表分片规则配置对象。

| *名称*                              | *数据类型*                     | *说明*                                                                                                                                                                                                      |
| ---------------------------------- | ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logicTable                         | String                        | 逻辑表名称                                                                                                                                                                                                   |
| actualDataNodes (?)                | String                        | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况    |
| databaseShardingStrategyConfig (?) | ShardingStrategyConfiguration | 分库策略，缺省表示使用默认分库策略                                                                                                                                                                              |
| tableShardingStrategyConfig (?)    | ShardingStrategyConfiguration | 分表策略，缺省表示使用默认分表策略                                                                                                                                                                              |
| keyGeneratorConfig (?)             | KeyGeneratorConfiguration     | 自增列值生成器配置，缺省表示使用默认自增主键生成器                                                                                                                                                                |
| encryptorConfiguration (?)         | EncryptorConfiguration        | 加解密生成器配置                                                                                                                                                                                              |

#### StandardShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于单分片键的标准分片场景。

| *名称*                      | *数据类型*                | *说明*                  |
| -------------------------- | ------------------------ | ----------------------- |
| shardingColumn             | String                   | 分片列名称               |
| preciseShardingAlgorithm   | PreciseShardingAlgorithm | 精确分片算法，用于=和IN   |
| rangeShardingAlgorithm (?) | RangeShardingAlgorithm   | 范围分片算法，用于BETWEEN |

#### ComplexShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于多分片键的复合分片场景。

| *名称*             | *数据类型*                    | *说明*                   |
| ----------------- | ---------------------------- | ------------------------ |
| shardingColumns   | String                       | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithm | ComplexKeysShardingAlgorithm | 复合分片算法               |

#### InlineShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置行表达式分片策略。

| *名称*               | *数据类型*  | *说明*                                                                                                   |
| ------------------- | ----------- | ------------------------------------------------------------------------------------------------------- |
| shardingColumn      |  String     | 分片列名称                                                                                               |
| algorithmExpression |  String     | 分片算法行表达式，需符合groovy语法，详情请参考[行表达式](/cn/features/sharding/other-features/inline-expression) |

#### HintShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置Hint方式分片策略。

| *名称*             | *数据类型*             | *说明*      |
| ----------------- | --------------------- | ----------- |
| shardingAlgorithm | HintShardingAlgorithm | Hint分片算法 |

#### NoneShardingStrategyConfiguration

ShardingStrategyConfiguration的实现类，用于配置不分片的策略。

#### KeyGeneratorConfiguration

| *名称*             | *数据类型*                    | *说明*                                                                         |
| ----------------- | ---------------------------- | ------------------------------------------------------------------------------ |
| column            | String                       | 自增列名称                                                                      |
| type              | String                       | 自增列值生成器类型，可自定义或选择内置类型：SNOWFLAKE/UUID |
| props             | Properties                   | 自增列值生成器的相关属性配置                                                      |

#### Properties

属性配置项，可以为以下自增列值生成器的属性。

##### SNOWFLAKE

| *名称*                                              | *数据类型*  | *说明*                                                                                                   |
| --------------------------------------------------- | ---------- | ------------------------------------------------------------------------------------------------------- |
| worker.id (?)                                        | long       | 工作机器唯一id，默认为0                                                                                  |
| max.tolerate.time.difference.milliseconds (?)        | long       | 最大容忍时钟回退时间，单位：毫秒。默认为10毫秒                                                               |
| max.vibration.offset (?)                             | int        | 最大抖动上限值，范围[0, 4096)，默认为1。注：若使用此算法生成值作分片值，建议配置此属性。此算法在不同毫秒内所生成的key取模2^n (2^n一般为分库或分表数) 之后结果总为0或1。为防止上述分片问题，建议将此属性值配置为(2^n)-1 |

#### EncryptRuleConfiguration

| *名称*               |*数据类型*                                    | *说明*                                                                          |
| ------------------- | ------------------------------------------- | ------------------------------------------------------------------------------ |
| encryptors          | Map<String, EncryptorRuleConfiguration>     | 加解密器配置列表，可自定义或选择内置类型：MD5/AES                                    |
| tables              | Map<String, EncryptTableRuleConfiguration>  | 加密表配置列表                                                                   |

#### EncryptorRuleConfiguration

| *名称*               |*数据类型*                    | *说明*                                                                          |
| ------------------- | ---------------------------- | ------------------------------------------------------------------------------ |
| type                | String                       | 加解密器类型，可自定义或选择内置类型：MD5/AES                                       |
| properties          | Properties                   | 属性配置, 注意：使用AES加密器，需要配置AES加密器的KEY属性：aes.key.value              |

#### EncryptTableRuleConfiguration

| *名称*               |*数据类型*                                     | *说明*                            |
| ------------------- | -------------------------------------------- | --------------------------------- |
| tables              | Map<String, EncryptColumnRuleConfiguration>  | 加密列配置列表                      |

#### EncryptColumnRuleConfiguration

| *名称*               |*数据类型*                    | *说明*                                                                          |
| ------------------- | ---------------------------- | ------------------------------------------------------------------------------ |
| plainColumn        | String                       | 存储明文的字段                                                                   |
| cipherColumn       | String                       | 存储密文的字段                                                                   |
| assistedQueryColumn| String                       | 辅助查询字段，针对ShardingQueryAssistedEncryptor类型的加解密器进行辅助查询            |
| encryptor          | String                       | 加解密器名字                                                                      |

#### Properties

属性配置项，可以为以下属性。

| *名称*                             | *数据类型*  | *说明*                                          |
| ----------------------------------| --------- | -------------------------------------------------|
| sql.show (?)                      | boolean   | 是否开启SQL显示，默认值: false                      |
| executor.size (?)                 | int       | 工作线程数量，默认值: CPU核数                       |
| max.connections.size.per.query (?)| int       | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1   |
| check.table.metadata.enabled (?)  | boolean   | 是否在启动时检查分表元数据一致性，默认值: false        |
| query.with.cipher.column (?)      | boolean   | 当存在明文列时，是否使用密文列查询，默认值: true        |
| allow.range.query.with.inline.sharding (?)    | boolean   | 当使用inline分表策略时，是否允许范围查询，默认值: false        |

### 读写分离

#### MasterSlaveDataSourceFactory

读写分离的数据源创建工厂。

| *名称*                 | *数据类型*                    | *说明*             |
| --------------------- | ---------------------------- | ------------------ |
| dataSourceMap         | Map\<String, DataSource\>    | 数据源与其名称的映射  |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 读写分离规则         |
| props (?)             | Properties                   | 属性配置            |

#### MasterSlaveRuleConfiguration

读写分离规则配置对象。

| *名称*                    | *数据类型*                       | *说明*           |
| ------------------------ | ------------------------------- | ---------------- |
| name                     | String                          | 读写分离数据源名称 |
| masterDataSourceName     | String                          | 主库数据源名称    |
| slaveDataSourceNames     | Collection\<String\>            | 从库数据源名称列表 |
| loadBalanceAlgorithm (?) | MasterSlaveLoadBalanceAlgorithm | 从库负载均衡算法   |

#### Properties

属性配置项，可以为以下属性。

| *名称*                              | *数据类型* | *说明*                                            |
| ---------------------------------- | --------- | ------------------------------------------------- |
| sql.show (?)                       | boolean   | 是否打印SQL解析和改写日志，默认值: false              |
| executor.size (?)                  | int       | 用于SQL执行的工作线程数量，为零则表示无限制。默认值: 0   |
| max.connections.size.per.query (?) | int       | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1    |
| check.table.metadata.enabled (?)   | boolean   | 是否在启动时检查分表元数据一致性，默认值: false         |

### 数据脱敏

#### EncryptDataSourceFactory

| *名称*                 | *数据类型*                    | *说明*             |
| --------------------- | ---------------------------- | ------------------ |
| dataSource            | DataSource                   | 数据源，任意连接池    |
| encryptRuleConfig     | EncryptRuleConfiguration     | 数据脱敏规则         |
| props (?)             | Properties                   | 属性配置            |

#### EncryptRuleConfiguration

| *名称*               |*数据类型*                                    | *说明*                                                                          |
| ------------------- | ------------------------------------------- | ------------------------------------------------------------------------------ |
| encryptors          | Map<String, EncryptorRuleConfiguration>     | 加解密器配置列表，可自定义或选择内置类型：MD5/AES                                    |
| tables              | Map<String, EncryptTableRuleConfiguration>  | 加密表配置列表                      |

#### Properties

属性配置项，可以为以下属性。

| *名称*                             | *数据类型*  | *说明*                                          |
| ----------------------------------| --------- | -------------------------------------------------|
| sql.show (?)                      | boolean   | 是否开启SQL显示，默认值: false                      |
| query.with.cipher.column (?)      | boolean   | 当存在明文列时，是否使用密文列查询，默认值: true       |

### 治理

#### OrchestrationShardingDataSourceFactory

数据分片 + 治理的数据源工厂。

| *名称*               | *数据类型*                  | *说明*                      |
| ------------------- |  ------------------------- | --------------------------- |
| dataSourceMap       | Map\<String, DataSource\>  | 同ShardingDataSourceFactory |
| shardingRuleConfig  | ShardingRuleConfiguration  | 同ShardingDataSourceFactory |
| props (?)           | Properties                 | 同ShardingDataSourceFactory |
| orchestrationConfig | OrchestrationConfiguration | 治理规则配置              |

#### OrchestrationMasterSlaveDataSourceFactory

读写分离 + 治理的数据源工厂。

| *名称*                 | *数据类型*                    | *说明*                         |
| --------------------- | ---------------------------- | ------------------------------ |
| dataSourceMap         | Map\<String, DataSource\>    | 同MasterSlaveDataSourceFactory |
| masterSlaveRuleConfig | MasterSlaveRuleConfiguration | 同MasterSlaveDataSourceFactory |
| props (?)             | Properties                   | 同ShardingDataSourceFactory    |
| orchestrationConfig   | OrchestrationConfiguration   | 治理规则配置                 |

#### OrchestrationEncryptDataSourceFactory

数据脱敏 + 治理的数据源工厂。

| *名称*                 | *数据类型*                    | *说明*                         |
| --------------------- | ---------------------------- | ------------------------------ |
| dataSource            | DataSource                   | 同EncryptDataSourceFactory     |
| encryptRuleConfig     | EncryptRuleConfiguration     | 同EncryptDataSourceFactory     |
| props (?)             | Properties                   | 同ShardingDataSourceFactory    |
| orchestrationConfig   | OrchestrationConfiguration   | 治理规则配置                 |

#### OrchestrationConfiguration

治理规则配置对象。

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| instanceConfigurationMap | Map\<String, CenterConfiguration\>  | 配置/注册/元数据中心的配置map，key为名称，value为配置/注册/元数据中心   |

#### CenterConfiguration

用于配置配置/注册/元数据中心。

| *名称*                             | *数据类型* | *说明*                                                                               |
| --------------------------------- | ---------- | ----------------------------------------------------------------------------------- |
| instanceType                      | String     | 配置/注册/元数据中心的实例类型，例如zookeeper或etcd、apollo、nacos                                       |
| properties                        | String     | 配置本实例需要的其他参数，例如zookeeper的连接参数等，具体参考properties配置                         |
| orchestrationType                 | String     | 治理类型，例如config_center/registry_center/metadata_center，如果都是，可以"setOrchestrationType("registry_center,config_center,metadata_center");"              |
| serverLists                       | String     | 连接配置/注册/元数据中心服务器的列表，包括IP地址和端口号，多个地址用逗号分隔。如: host1:2181,host2:2181 |
| namespace (?)                     | String     | 配置/注册/元数据中心的命名空间                                                                     |

其中properties的通用配置如下：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| overwrite                         | boolean    | 本地配置是否覆盖配置中心配置，如果可覆盖，每次启动都以本地配置为准                         |

如果采用了zookeeper作为配置中心或（和）注册中心或 (和) 元数据中心，那么properties还可以配置：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| digest (?)                        | String     | 连接注册中心的权限令牌。缺省为不需要权限验证                                             |
| operationTimeoutMilliseconds (?)  | int        | 操作超时的毫秒数，默认500毫秒                                                          |
| maxRetries (?)                    | int        | 连接失败后的最大重试次数，默认3次                                                       |
| retryIntervalMilliseconds (?)     | int        | 重试间隔毫秒数，默认500毫秒                                                            |
| timeToLiveSeconds (?)             | int        | 临时节点存活秒数，默认60秒                                                             |

如果采用了etcd作为配置中心或（和）注册中心或 (和) 元数据中心，那么properties还可以配置：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| timeToLiveSeconds (?)             | long        | TTL时间，单位为秒，默认30秒                                     |

如果采用了apollo作为配置中心，那么properties还可以配置：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| appId (?)          | String        | apollo appId，默认值为"APOLLO_SHARDINGSPHERE"                               |
| env (?)            | String        | apollo env，默认值为"DEV"                                                   |
| clusterName (?)    | String        | apollo clusterName，默认值为"default"                                       |
| administrator (?)  | String        | apollo administrator，默认值为""                                            |
| token (?)          | String        | apollo token，默认值为""                                                    |
| portalUrl (?)      | String        | apollo portalUrl，默认值为""                                                |
| connectTimeout (?) | int           | apollo connectTimeout，默认值为1000毫秒                                      |
| readTimeout (?)    | int           | apollo readTimeout，默认值为5000毫秒                                         |

如果采用了nacos作为配置中心或 (和) 注册中心，那么properties还可以配置：

| *名称*           | *数据类型*                   | *说明*                                                     |
| --------------- | --------------------------- | ---------------------------------------------------------- |
| group (?)          | String        | nacos group配置，默认值为"SHARDING_SPHERE_DEFAULT_GROUP"                     |
| timeout (?)        | long          | nacos 获取数据超时时间，单位为毫秒，默认值为3000毫秒                            |
