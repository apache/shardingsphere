+++
title = "Single Table"
weight = 12
+++

## Background

Single rule is used to specify which single tables need to be managed by ShardingSphere, or to set the default single table data source.

## Parameters

Class：org.apache.shardingsphere.single.api.config.SingleRuleConfiguration

Attributes：

| *name*                                  | *DataType* | *Description*                    | *Default Value* |
|-----------------------|----------------------|----------------------------------|-----------------|
| tables (+)            | Collection\<String\> | single tables                    | -               |
| defaultDataSource (?) | String | single table default data source | -               |

## Procedure

1. Initialize SingleRuleConfiguration;
2. Add a single table to be loaded and configure the default data source.

## Sample

```java
SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Arrays.asList(ruleConfig), new Properties());
```

## Related References

- [Single Table](/en/features/sharding/concept/#single-table)
