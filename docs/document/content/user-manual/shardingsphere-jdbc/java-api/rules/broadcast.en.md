+++
title = "Broadcast Table"
weight = 2
+++

## Background

The Java API rule configuration for broadcast, which allows users to create ShardingSphereDataSource objects directly by writing Java code, is flexible enough to integrate various types of business systems without relying on additional jar packages.

## Parameters

Class：org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration

Attributes：

| *name*                                  | *DataType* | *Description*                                    | *Default Value* |
|---------------------------|---------------------------------------------|------------|-------|
| tables (+)                | Collection\<String\> | Broadcast table rules    |       |

## Sample

The following is an example of the broadcast table Java API configuration:

```java
public final class ShardingDatabasesAndTablesConfigurationPrecise {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Arrays.asList(createBroadcastRuleConfiguration()), new Properties());
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
        result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
        return result;
    }
    
    private BroadcastRuleConfiguration createBroadcastRuleConfiguration() {
        return new BroadcastRuleConfiguration(Collections.singletonList("t_address"));
    }
}
```

## Related References
- [YAML Configuration: Broadcast](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/broadcast/)