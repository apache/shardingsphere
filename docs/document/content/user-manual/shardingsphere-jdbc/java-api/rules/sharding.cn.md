+++
title = "数据分片"
weight = 1
+++

## 背景信息

数据分片 Java API 规则配置允许用户直接通过编写 Java 代码的方式，完成 ShardingSphereDataSource 对象的创建，Java API 的配置方式非常灵活，不需要依赖额外的 jar 包就能够集成各种类型的业务系统。

## 参数解释

### 配置入口

类名称：org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

可配置属性：

| *名称*                               | *数据类型*                                        | *说明*               | *默认值* |
| ----------------------------------- | ------------------------------------------------ | ------------------- | ------- |
| tables (+)                          | Collection\<ShardingTableRuleConfiguration\>     | 分片表规则列表        | -       |
| autoTables (+)                      | Collection\<ShardingAutoTableRuleConfiguration\> | 自动分片表规则列表    | -        |
| bindingTableGroups (*)              | Collection\<String\>                             | 绑定表规则列表        | 无       |
| broadcastTables (*)                 | Collection\<String\>                             | 广播表规则列表        | 无       |
| defaultDatabaseShardingStrategy (?) | ShardingStrategyConfiguration                    | 默认分库策略          | 不分片   |
| defaultTableShardingStrategy (?)    | ShardingStrategyConfiguration                    | 默认分表策略          | 不分片   |
| defaultKeyGenerateStrategy (?)      | KeyGeneratorConfiguration                        | 默认自增列生成器配置   | 雪花算法 |
| defaultShardingColumn (?)           | String                                           | 默认分片列名称        | 无      |
| shardingAlgorithms (+)              | Map\<String, AlgorithmConfiguration\>            | 分片算法名称和配置     | 无      |
| keyGenerators (?)                   | Map\<String, AlgorithmConfiguration\>            | 自增列生成算法名称和配置 | 无      |

### 分片表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

可配置属性：

| *名称*                        | *数据类型*                     | *说明*                                                         | *默认值*                                                                      |
| ---------------------------- | ----------------------------- | ------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| logicTable                   | String                        | 分片逻辑表名称                                                   | -                                                                            |
| actualDataNodes (?)          | String                        | 由数据源名 + 表名组成，以小数点分隔。<br />多个表以逗号分隔，支持行表达式 | 使用已知数据源与逻辑表名称生成数据节点，用于广播表或只分库不分表且所有库的表结构完全一致的情况 |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | 分库策略                                                        | 使用默认分库策略                                                                |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | 分表策略                                                        | 使用默认分表策略                                                                |
| keyGenerateStrategy (?)      | KeyGeneratorConfiguration     | 自增列生成器                                                     | 使用默认自增主键生成器                                                           |

### 自动分片表配置

类名称：org.apache.shardingsphere.sharding.api.config.ShardingAutoTableRuleConfiguration

可配置属性：

| *名称*                   | *数据类型*                     | *说明*                      | *默认值*            |
| ----------------------- | ----------------------------- | -------------------------- | ------------------ |
| logicTable              | String                        | 分片逻辑表名称                | -                  |
| actualDataSources (?)   | String                        | 数据源名称，多个数据源以逗号分隔 | 使用全部配置的数据源   |
| shardingStrategy (?)    | ShardingStrategyConfiguration | 分片策略                     | 使用默认分片策略      |
| keyGenerateStrategy (?) | KeyGeneratorConfiguration     | 自增列生成器                  | 使用默认自增主键生成器 |

### 分片策略配置

#### 标准分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型* | *说明*     |
| --------------------- | -------- | ---------- |
| shardingColumn        | String   | 分片列名称   |
| shardingAlgorithmName | String   | 分片算法名称 |

#### 复合分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型* | *说明*                    |
| --------------------- | ---------- | ----------------------- |
| shardingColumns       | String     | 分片列名称，多个列以逗号分隔 |
| shardingAlgorithmName | String     | 分片算法名称              |

#### Hint 分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration

可配置属性：

| *名称*                 | *数据类型* | *说明*      |
| --------------------- | --------- | ---------- |
| shardingAlgorithmName | String    | 分片算法名称 |

#### 不分片策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration

可配置属性：无

算法类型的详情，请参见[内置分片算法列表](/cn/user-manual/common-config/builtin-algorithm/sharding)。

### 分布式序列策略配置

类名称：org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration

可配置属性：

| *名称*           | *数据类型* | *说明*           |
| ---------------- | -------- | --------------- |
| column           | String   | 分布式序列列名称   |
| keyGeneratorName | String   | 分布式序列算法名称 |

算法类型的详情，请参见[内置分布式序列算法列表](/cn/user-manual/common-config/builtin-algorithm/keygen)。

## 操作步骤

1. 创建真实数据源映射关系，key 为数据源逻辑名称，value 为 DataSource 对象；
1. 创建分片规则对象 ShardingRuleConfiguration，并初始化对象中的分片表对象 ShardingTableRuleConfiguration、绑定表集合、广播表集合，以及数据分片所依赖的分库策略和分表策略等参数；
1. 调用 ShardingSphereDataSourceFactory 对象的 createDataSource 方法，创建 ShardingSphereDataSource。

## 配置示例

```java
public final class ShardingDatabasesAndTablesConfigurationPrecise implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Collections.singleton(createShardingRuleConfiguration()), new Properties());
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(getOrderTableRuleConfiguration());
        result.getTables().add(getOrderItemTableRuleConfiguration());
        result.getBindingTableGroups().add("t_order, t_order_item");
        result.getBroadcastTables().add("t_address");
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "inline"));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "standard_test_tbl"));
        Properties props = new Properties();
        props.setProperty("algorithm-expression", "demo_ds_${user_id % 2}");
        result.getShardingAlgorithms().put("inline", new AlgorithmConfiguration("INLINE", props));
        result.getShardingAlgorithms().put("standard_test_tbl", new AlgorithmConfiguration("STANDARD_TEST_TBL", new Properties()));
        result.getKeyGenerators().put("snowflake", new AlgorithmConfiguration("SNOWFLAKE", new Properties()));
        return result;
    }
    
    private ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "demo_ds_${0..1}.t_order_${[0, 1]}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        return result;
    }
    
    private ShardingTableRuleConfiguration getOrderItemTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order_item", "demo_ds_${0..1}.t_order_item_${[0, 1]}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_item_id", "snowflake"));
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
        result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
        return result;
    }
}
```

## 相关参考

- [核心特性：数据分片](/cn/features/sharding/)
- [开发者指南：数据分片](/cn/dev-manual/sharding/)
