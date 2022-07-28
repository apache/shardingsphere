+++
title = "Shadow DB"
weight = 6
+++

## Background

In the distributed application architecture based on microservices, businesses require multiple services to be completed through a series of services and middleware, so the stress test of a single service can no longer meet the needs of real scenarios. If we reconstruct a stress test environment similar to the production environment, it is too expensive and often fails to simulate the complexity and traffic of the online environment. For this reason, the industry often chooses the full link stress test, which is performed in the production environment, so that the test results can accurately reflect the true capacity and performance of the system. 

## Parameters

### Root Configuration

Class name: org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

Attributes:

| *Name*   | *Data Type*  | *Description*  | *Default* |
| -------- | ----------- | ------- | ------- |
| dataSources  | Map\<String, ShadowDataSourceConfiguration\> | shadow data source mapping name and configuration | no  |
| tables | Map\<String, ShadowTableConfiguration\> | shadow table name and configuration | no |
| shadowAlgorithms | Map\<String, ShardingSphereAlgorithmConfiguration\> | shadow algorithm name and configuration | no  |
| defaultShadowAlgorithmName | String | default shadow algorithm name  | no |

### Shadow Data Source Configuration

Class name: org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration

Attributes:

| *Name*               | *DataType* | *Description*               |
| -------------------- | ---------- | --------------------------- |
| sourceDataSourceName | String     | Production data source name |
| shadowDataSourceName | String     | Shadow data source name     |

### Shadow Table Configuration

Class name: org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration

Attributes:

| *Name*  | *Data Type* | *Description* |
| ------- | ---------- | ------- |
| dataSourceNames | Collection\<String\> | shadow table associates shadow data source mapping name list |
| shadowAlgorithmNames | Collection\<String\> | shadow table associates shadow algorithm name list |

### Shadow Algorithm Configuration

Class name：org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

Attributes:

| *Name*  | *Data Type* | *Description* |
| ------- | ---------- | ------- |
| type | String | shadow algorithm type |
| props | Properties | shadow algorithm configuration |

Please refer to [Built-in Shadow Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/shadow).

## Procedure

1. Create production and shadow data source.
1. Configure shadow rule.
- Configure shadow data source
- Configure shadow table
- Configure shadow algorithm

## Sample

```java
public final class ShadowConfiguration {

    @Override
    public DataSource getDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, createRuleConfigurations(), createShardingSphereProps());
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>();
        result.put("ds", DataSourceUtil.createDataSource("demo_ds"));
        result.put("ds_shadow", DataSourceUtil.createDataSource("shadow_demo_ds"));
        return result;
    }
    
    private Collection<RuleConfiguration> createRuleConfigurations() {
        Collection<RuleConfiguration> result = new LinkedList<>();
        ShadowRuleConfiguration shadowRule = new ShadowRuleConfiguration();
        shadowRule.setDataSources(createShadowDataSources());
        shadowRule.setTables(createShadowTables());
        shadowRule.setShadowAlgorithms(createShadowAlgorithmConfigurations());
        result.add(shadowRule);
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createShadowDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        return result;
    }
    
    private Map<String, ShadowTableConfiguration> createShadowTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_user", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source"), createShadowAlgorithmNames()));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        result.add("user-id-insert-match-algorithm");
        result.add("simple-hint-algorithm");
        return result;
    }
    
    private Map<String, ShardingSphereAlgorithmConfiguration> createShadowAlgorithmConfigurations() {
        Map<String, ShardingSphereAlgorithmConfiguration> result = new LinkedHashMap<>();
        Properties userIdInsertProps = new Properties();
        userIdInsertProps.setProperty("operation", "insert");
        userIdInsertProps.setProperty("column", "user_type");
        userIdInsertProps.setProperty("value", "1");
        result.put("user-id-insert-match-algorithm", new ShardingSphereAlgorithmConfiguration("VALUE_MATCH", userIdInsertProps));
        return result;
    }
}
```

## Related References

[Features Description of Shadow DB](/en/features/shadow/)
