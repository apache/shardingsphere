+++
title = "混合规则"
weight = 6
+++

混合配置的规则项之间的叠加使用是通过数据源名称和表名称关联的。

如果前一个规则是面向数据源聚合的，下一个规则在配置数据源时，则需要使用前一个规则配置的聚合后的逻辑数据源名称；
同理，如果前一个规则是面向表聚合的，下一个规则在配置表时，则需要使用前一个规则配置的聚合后的逻辑表名称。

## 配置项说明

```java
/* 数据源配置 */
HikariDataSource primaryDataSource0 = new HikariDataSource();
primaryDataSource0.setDriverClassName("com.mysql.jdbc.Driver");
primaryDataSource0.setJdbcUrl("jdbc:mysql://localhost:3306/db0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8");
primaryDataSource0.setUsername("root");
primaryDataSource0.setPassword("");

HikariDataSource primaryDataSource1 = new HikariDataSource();
// ...忽略其他数据库配置项

HikariDataSource replica0OfPrimaryDataSource0 = new HikariDataSource();
// ...忽略其他数据库配置项

HikariDataSource replica1OfPrimaryDataSource0 = new HikariDataSource();
// ...忽略其他数据库配置项

HikariDataSource replica0OfPrimaryDataSource1 = new HikariDataSource();
// ...忽略其他数据库配置项

HikariDataSource replica1OfPrimaryDataSource1 = new HikariDataSource();
// ...忽略其他数据库配置项

Map<String, DataSource> datasourceMaps = new HashMap<>(6);

datasourceMaps.put("primary_ds0", primaryDataSource0);
datasourceMaps.put("primary_ds0_replica0", replica0OfPrimaryDataSource0);
datasourceMaps.put("primary_ds0_replica1", replica1OfPrimaryDataSource0);

datasourceMaps.put("primary_ds1", primaryDataSource1);
datasourceMaps.put("primary_ds1_replica0", replica0OfPrimaryDataSource1);
datasourceMaps.put("primary_ds1_replica1", replica1OfPrimaryDataSource1);

/* 分片规则配置 */
// 表达式 ds_${0..1} 枚举值表示的是主从配置的逻辑数据源名称列表
ShardingTableRuleConfiguration tOrderRuleConfiguration = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${[0, 1]}");
tOrderRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
tOrderRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "tOrderInlineShardingAlgorithm"));
Properties tOrderShardingInlineProps = new Properties();
tOrderShardingInlineProps.setProperty("algorithm-expression", "t_order_${order_id % 2}");
ruleConfiguration.getShardingAlgorithms().putIfAbsent("tOrderInlineShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE",tOrderShardingInlineProps));

ShardingTableRuleConfiguration tOrderItemRuleConfiguration = new ShardingTableRuleConfiguration("t_order_item", "ds_${0..1}.t_order_item_${[0, 1]}");
tOrderItemRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_item_id", "snowflake"));
tOrderRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_item_id", "tOrderItemInlineShardingAlgorithm"));
Properties tOrderItemShardingInlineProps = new Properties();
tOrderItemShardingInlineProps.setProperty("algorithm-expression", "t_order_item_${order_item_id % 2}");
ruleConfiguration.getShardingAlgorithms().putIfAbsent("tOrderItemInlineShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE",tOrderItemShardingInlineProps));

ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
shardingRuleConfiguration.getTables().add(tOrderRuleConfiguration);
shardingRuleConfiguration.getTables().add(tOrderItemRuleConfiguration);
shardingRuleConfiguration.getBindingTableGroups().add("t_order, t_order_item");
shardingRuleConfiguration.getBroadcastTables().add("t_bank");
// 默认分库策略
shardingRuleConfiguration.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "default_db_strategy_inline"));
Properties defaultDatabaseStrategyInlineProps = new Properties();
defaultDatabaseStrategyInlineProps.setProperty("algorithm-expression", "ds_${user_id % 2}");
shardingRuleConfiguration.getShardingAlgorithms().put("default_db_strategy_inline", new ShardingSphereAlgorithmConfiguration("INLINE", defaultDatabaseStrategyInlineProps));
// 分布式序列算法配置
Properties snowflakeProperties = new Properties();
snowflakeProperties.setProperty("worker-id", "123");
shardingRuleConfiguration.getKeyGenerators().put("snowflake", new ShardingSphereAlgorithmConfiguration("SNOWFLAKE", snowflakeProperties));

/* 数据加密规则配置 */
Properties encryptProperties = new Properties();
encryptProperties.setProperty("aes-key-value", "123456");
EncryptColumnRuleConfiguration columnConfigAes = new EncryptColumnRuleConfiguration("user_name", "user_name", "", "user_name_plain", "name_encryptor");
EncryptColumnRuleConfiguration columnConfigTest = new EncryptColumnRuleConfiguration("pwd", "pwd", "assisted_query_pwd", "", "pwd_encryptor");
EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_user", Arrays.asList(columnConfigAes, columnConfigTest));

Map<String, ShardingSphereAlgorithmConfiguration> encryptAlgorithmConfigs = new LinkedHashMap<>(2, 1);
encryptAlgorithmConfigs.put("name_encryptor", new ShardingSphereAlgorithmConfiguration("AES", encryptProperties));
encryptAlgorithmConfigs.put("pwd_encryptor", new ShardingSphereAlgorithmConfiguration("assistedTest", encryptProperties));
EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), encryptAlgorithmConfigs);

/* 读写分离规则配置 */
ReplicaQueryDataSourceRuleConfiguration dataSourceConfiguration1 = new ReplicaQueryDataSourceRuleConfiguration("ds_0", "primary_ds0", Arrays.asList("primary_ds0_replica0", "primary_ds0_replica1"), "roundRobin");
ReplicaQueryDataSourceRuleConfiguration dataSourceConfiguration2 = new ReplicaQueryDataSourceRuleConfiguration("ds_1", "primary_ds0", Arrays.asList("primary_ds1_replica0", "primary_ds1_replica0"), "roundRobin");

//负载均衡算法
Map<String, ShardingSphereAlgorithmConfiguration> loadBalanceMaps = new HashMap<>(1);
loadBalanceMaps.put("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));

ReplicaQueryRuleConfiguration replicaQueryRuleConfiguration = new ReplicaQueryRuleConfiguration(Arrays.asList(dataSourceConfiguration1, dataSourceConfiguration2), loadBalanceMaps);

/* 其他配置 */
Properties otherProperties = new Properties();
otherProperties.setProperty("sql-show", "true");
otherProperties.setProperty("query-with-cipher-column", "true");

/* shardingDataSource 就是最终被ORM框架或其他jdbc框架引用的数据源名称 */
DataSource shardingDataSource = ShardingSphereDataSourceFactory.createDataSource(datasourceMaps, Arrays.asList(shardingRuleConfiguration, replicaQueryRuleConfiguration, encryptRuleConfiguration), otherProperties);

```