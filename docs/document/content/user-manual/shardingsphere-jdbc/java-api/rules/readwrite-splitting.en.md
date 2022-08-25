+++
title = "Readwrite-splitting"
weight = 2
+++

## Background

The read/write splitting configured in Java API form can be easily applied to various scenarios without relying on additional jar packages. Users only need to construct the read/write splitting data source through java code to be able to use the read/write splitting function.

## Parameters Explained

### Entry

Class name: org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration

Configurable Properties:

| *Name*            | *DataType*                                                  | *Description*                                                          |
| ----------------- | ----------------------------------------------------------- | ---------------------------------------------------------------------- |
| dataSources (+)   | Collection\<ReadwriteSplittingDataSourceRuleConfiguration\> | Data sources of write and reads                                        |
| loadBalancers (*) | Map\<String, AlgorithmConfiguration\>                       | Load balance algorithm name and configurations of replica data sources |

### Primary-secondary Data Source Configuration

Class name: org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration

Configurable Properties:

| *Name*               | *DataType* | *Description*                                  | *Default Value*                    |
| -------------------- | ---------- | ---------------------------------------------- | ---------------------------------- |
| name                 | String     | Readwrite-splitting data source name           | -                                  |
| staticStrategy       | String     | Static Readwrite-splitting configuration       | -                                  |
| dynamicStrategy      | Properties | Dynamic Readwrite-splitting configuration      | -                                  |
| loadBalancerName (?) | String     | Load balance algorithm name of replica sources | Round robin load balance algorithm |

Class name：org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration

Configurable Properties:

| *Name*              | *DataType*     | *Description*          |
| ------------------- | -------------- | ---------------------- |
| writeDataSourceName | String         | Write data source name |
| readDataSourceNames | List\<String\> | Read data sources list |

Class name：org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration

Configurable Properties:

| *Name*                          | *DataType* | *Description*                                                                                               | *Default Value*    |
| ------------------------------- | ---------- | ------------------------------------------------------------------------------------------------------------| -------------------|
| autoAwareDataSourceName         | String     | Database discovery logic data source name                                                                   | -                  |
| writeDataSourceQueryEnabled (?) | String     | All read data source are offline, write data source whether the data source is responsible for read traffic | true               |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/common-config/builtin-algorithm/load-balance) for details on algorithm types.
Please refer to [Read-write splitting-Core features](/en/features/readwrite-splitting/) for more details about query consistent routing.

## Operating Procedures

1. Add read-write splitting data source
1. Set load balancing algorithms
1. Use read-write splitting data source 
   
## Configuration Examples

```java
public DataSource getDataSource() throws SQLException {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadwriteSplittingDataSourceRuleConfiguration(
                "demo_read_query_ds", new StaticReadwriteSplittingStrategyConfiguration("demo_write_ds",
                Arrays.asList("demo_read_ds_0", "demo_read_ds_1")), null,"demo_weight_lb");
        Properties algorithmProps = new Properties();
        algorithmProps.setProperty("demo_read_ds_0", "2");
        algorithmProps.setProperty("demo_read_ds_1", "1");
        Map<String, AlgorithmConfiguration> algorithmConfigMap = new HashMap<>(1);
        algorithmConfigMap.put("demo_weight_lb", new AlgorithmConfiguration("WEIGHT", algorithmProps));
        ReadwriteSplittingRuleConfiguration ruleConfig = new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), algorithmConfigMap);
        Properties props = new Properties();
        props.setProperty("sql-show", Boolean.TRUE.toString());
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Collections.singleton(ruleConfig), props);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("demo_write_ds", DataSourceUtil.createDataSource("demo_write_ds"));
        result.put("demo_read_ds_0", DataSourceUtil.createDataSource("demo_read_ds_0"));
        result.put("demo_read_ds_1", DataSourceUtil.createDataSource("demo_read_ds_1"));
        return result;
    }
```

## References

- [Read-write splitting-Core features](/en/features/readwrite-splitting/)
- [YAML Configuration: read-write splitting](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/)
- [Spring Boot Starter: read-write splitting](/en/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/readwrite-splitting/)
- [Spring namespace: read-write splitting](/en/user-manual/shardingsphere-jdbc/spring-namespace/rules/readwrite-splitting/)
