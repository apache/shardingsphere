+++
title = "Cache for Sharding Route"
weight = 11
+++

## Background

This feature is **experimental** and needs to be used with the data sharding rule.
The cache for sharding route will put the logical SQL, the parameter value of the shard key, and the routing result into the cache, exchange space for time, and reduce CPU usage of the routing logic.

We recommend enabling it only if the following conditions are met:
- Pure OLTP scenarios.
- The CPU of the machine which deployed the ShardingSphere process has reached the bottleneck.
- Most of the CPUs are used by ShardingSphere routing logic.
- All SQLs are optimized and each SQL execution could be routed to a single data node.

If the above conditions are not met, the execution delay of SQL may not be significantly improved, and the memory pressure will be increased.

## Parameters

Class：org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration

Attributes：

| *name*                                  | *DataType* | *Description*                                    | *Default Value* |
|-----------------------|------------------------------------------------------|----------------|-------|
| allowedMaxSqlLength            | int                                                  | 允许缓存的 SQL 长度限制 | -     |
| routeCache | org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration | 路由缓存           | -     |

Class：org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration

Attributes：

| *name*                                  | *DataType* | *Description*                                    | *Default Value* |
|-----------------------|------------------------------------------------------|----------------|-------|
| softValues            | boolean                                                  | 是否软引用缓存值 | -     |
| initialCapacity | int | 缓存初始容量           | -     |
| maximumSize | int | 缓存最大容量           | -     |

## Sample

```java
public final class ShardingDatabasesAndTablesConfigurationPrecise {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return ShardingSphereDataSourceFactory.createDataSource(createDataSourceMap(), Arrays.asList(createShardingRuleConfiguration(), createBroadcastRuleConfiguration())), new Properties());
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(getOrderTableRuleConfiguration());
        result.getTables().add(getOrderItemTableRuleConfiguration());
        // ...
        result.setShardingCache(new ShardingCacheConfiguration(512, new ShardingCacheConfiguration.RouteCacheConfiguration(65536, 262144, true)));
        return result;
    }
    
    private ShardingTableRuleConfiguration getOrderTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "demo_ds_${0..1}.t_order_${[0, 1]}");
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_id", "snowflake"));
        result.setAuditStrategy(new ShardingAuditStrategyConfiguration(Collections.singleton("sharding_key_required_auditor"), true));
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
    
    private BroadcastRuleConfiguration createBroadcastRuleConfiguration() {
        return new BroadcastRuleConfiguration(Collections.singletonList("t_address"));
    }
}
```

## Related References

- [Core Feature: Data Sharding](/en/features/sharding/)
