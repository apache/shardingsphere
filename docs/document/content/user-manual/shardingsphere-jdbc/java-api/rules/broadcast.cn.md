+++
title = "广播表"
weight = 2
+++

广播表 YAML 配置方式具有非凡的可读性，通过 YAML 格式，能够快速地理解广播表配置，ShardingSphere 会根据 YAML 配置，自动完成 ShardingSphereDataSource 对象的创建，减少用户不必要的编码工作。

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
        return new BroadcastRuleConfiguration(Collections.singletonList("t_address"));;
    }
}
```

## 相关参考

- [YAML 配置：广播表](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/broadcast/)
