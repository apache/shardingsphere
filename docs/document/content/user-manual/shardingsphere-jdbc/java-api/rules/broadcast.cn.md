+++
title = "广播表"
weight = 2
+++

广播表 Java API 规则配置允许用户直接通过编写 Java 代码的方式，完成 ShardingSphereDataSource 对象的创建，Java API 的配置方式非常灵活，不需要依赖额外的 jar 包就能够集成各种类型的业务系统。

## 参数解释

类名称：org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration

可配置属性：

| *名称*                      | *数据类型*                                      | *说明*       | *默认值* |
|---------------------------|---------------------------------------------|------------|-------|
| tables (+)                | Collection\<String\> | 广播表规则配置    |       |

## 配置示例

广播表 Java API 配置示例如下：

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

## 相关参考

- [YAML 配置：广播表](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/broadcast/)
