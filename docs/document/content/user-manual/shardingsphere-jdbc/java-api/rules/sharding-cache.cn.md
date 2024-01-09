+++
title = "数据分片路由缓存"
weight = 11
+++

## 背景信息

该项功能为**实验性功能**，需要与数据分片功能同时使用。
数据分片路由缓存会将逻辑 SQL、分片键实际参数值、路由结果放入缓存中，以空间换时间，减少路由逻辑对 CPU 的使用。

建议仅在满足以下条件的情况下启用：
- 纯 OLTP 场景
- ShardingSphere 进程所在机器 CPU 已达到瓶颈
- CPU 开销主要在于 ShardingSphere 路由逻辑
- 所有 SQL 已经最优且每次 SQL 执行都能命中单一分片

在不满足以上条件的情况下使用，可能对 SQL 的执行延时不会有明显改善，同时会增加内存的压力。

## 参数解释

类名称：org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheConfiguration

可配置属性：

| *名称*                  | *数据类型*                                               | *说明*           | *默认值* |
|-----------------------|------------------------------------------------------|----------------|-------|
| allowedMaxSqlLength            | int                                                  | 允许缓存的 SQL 长度限制 | -     |
| routeCache | org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration | 路由缓存           | -     |

类名称：org.apache.shardingsphere.sharding.api.config.cache.ShardingCacheOptionsConfiguration

可配置属性：

| *名称*                  | *数据类型*                                               | *说明*           | *默认值* |
|-----------------------|------------------------------------------------------|----------------|-------|
| softValues            | boolean                                                  | 是否软引用缓存值 | -     |
| initialCapacity | int | 缓存初始容量           | -     |
| maximumSize | int | 缓存最大容量           | -     |

## 配置示例

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

## 相关参考

- [核心特性：数据分片](/cn/features/sharding/)
