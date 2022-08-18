+++
title = "Sharding"
weight = 1
+++

## Background

The Java API rule configuration for data sharding, which allows users to create ShardingSphereDataSource objects directly by writing Java code, is flexible enough to integrate various types of business systems without relying on additional jar packages.

## Parameters

### Root Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration

Attributes:

| *Name*                              | *DataType*                                       | *Description*                             | *Default Value* |
| ----------------------------------- | ------------------------------------------------ | ----------------------------------------- | --------------- |
| tables (+)                          | Collection\<ShardingTableRuleConfiguration\>     | Sharding table rules                      | -               |
| autoTables (+)                      | Collection\<ShardingAutoTableRuleConfiguration\> | Sharding auto table rules                 | -               |
| bindingTableGroups (*)              | Collection\<String\>                             | Binding table rules                       | Empty           |
| broadcastTables (*)                 | Collection\<String\>                             | Broadcast table rules                     | Empty           |
| defaultDatabaseShardingStrategy (?) | ShardingStrategyConfiguration                    | Default database sharding strategy        | Not sharding    |
| defaultTableShardingStrategy (?)    | ShardingStrategyConfiguration                    | Default table sharding strategy           | Not sharding    |
| defaultKeyGenerateStrategy (?)      | KeyGeneratorConfiguration                        | Default key generator                     | Snowflake       |
| defaultShardingColumn (?)           | String                                           | Default sharding column name              | None            |
| shardingAlgorithms (+)              | Map\<String, AlgorithmConfiguration\>            | Sharding algorithm name and configurations | None            |
| keyGenerators (?)                   | Map\<String, AlgorithmConfiguration\>            | Key generate algorithm name and configurations | None            |

### Sharding Table Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingTableRuleConfiguration

Attributes:

| *Name*                       | *DataType*                    | *Description*                                                                                                                         | *Default Value*                            |
| ---------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| logicTable                   | String                        | Name of sharding logic table                                                                                                          | -                                          |
| actualDataNodes (?)          | String                        | Describe data source names and actual tables, delimiter as point.<br /> Multiple data nodes split by comma, support inline expression | Broadcast table or databases sharding only |
| databaseShardingStrategy (?) | ShardingStrategyConfiguration | Databases sharding strategy                                                                                                           | Use default databases sharding strategy    |
| tableShardingStrategy (?)    | ShardingStrategyConfiguration | Tables sharding strategy                                                                                                              | Use default tables sharding strategy       |
| keyGenerateStrategy (?)      | KeyGeneratorConfiguration     | Key generator configuration                                                                                                           | Use default key generator                  |

### Sharding Auto Table Configuration

Class name: org.apache.shardingsphere.sharding.api.config.ShardingAutoTableRuleConfiguration

Attributes:

| *Name*                  | *DataType*                    | *Description*                                               | *Default Value*                 |
| ----------------------- | ----------------------------- | ----------------------------------------------------------- | ------------------------------- |
| logicTable              | String                        | Name of sharding logic table                                | -                               |
| actualDataSources (?)   | String                        | Data source names.<br /> Multiple data nodes split by comma | Use all configured data sources |
| shardingStrategy (?)    | ShardingStrategyConfiguration | Sharding strategy                                           | Use default sharding strategy   |
| keyGenerateStrategy (?) | KeyGeneratorConfiguration     | Key generator configuration                                 | Use default key generator       |

### Sharding Strategy Configuration

#### Standard Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*           |
| --------------------- | ---------- | ----------------------- |
| shardingColumn        | String     | Sharding column name    |
| shardingAlgorithmName | String     | Sharding algorithm name |

#### Complex Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*                             |
| --------------------- | ---------- | ----------------------------------------- |
| shardingColumns       | String     | Sharding column name, separated by commas |
| shardingAlgorithmName | String     | Sharding algorithm name                   |

#### Hint Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration

Attributes:

| *Name*                | *DataType* | *Description*           |
| --------------------- | ---------- | ----------------------- |
| shardingAlgorithmName | String     | Sharding algorithm name |

#### None Sharding Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration

Attributes: None

Please refer to [Built-in Sharding Algorithm List](/en/user-manual/common-config/builtin-algorithm/sharding) for more details about type of algorithm.

### Distributed Key Strategy Configuration

Class name: org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration

Attributes:

| *Name*           | *DataType* | *Description*               |
| ---------------- | ---------- | --------------------------- |
| column           | String     | Column name of key generate |
| keyGeneratorName | String     | key generate algorithm name |

Please refer to [Built-in Key Generate Algorithm List](/en/user-manual/common-config/builtin-algorithm/keygen) for more details about type of algorithm.

## Procedure

1. Create an authentic data source mapping relationship, with key as the logical name of the data source and value as the DataSource object.
1. Create the sharding rule object ShardingRuleConfiguration, and initialize the sharding table objects—ShardingTableRuleConfiguration, the set of bound tables, the set of broadcast tables, and parameters like library sharding strategy and the database sharding strategy, on which the data sharding depends.
1. Using the ShardingSphereDataSource method of calling the ShardingSphereDataSourceFactory subject to create the ShardingSphereDataSource.

## Sample

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

## Related References

- [Core Feature: Data Sharding](/en/features/sharding/)
- [Developer Guide: Data Sharding](/en/dev-manual/sharding/)
