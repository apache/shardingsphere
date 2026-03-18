+++
title = "读写分离"
weight = 3
+++

## 背景信息

Java API 形式配置的读写分离可以方便的适用于各种场景，不依赖额外的 jar 包，用户只需要通过 java 代码构造读写分离数据源便可以使用读写分离功能。

## 参数解释

### 配置入口

类名称：org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration

可配置属性：

| *名称*              | *数据类型*                                                      | *说明*       |
|-------------------|-------------------------------------------------------------|------------|
| dataSources (+)   | Collection\<ReadwriteSplittingDataSourceRuleConfiguration\> | 读写数据源配置    |
| loadBalancers (*) | Map\<String, AlgorithmConfiguration\>                       | 从库负载均衡算法配置 |

## 主从数据源配置

类名称：org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration

可配置属性：

| *名称*                               | *数据类型*                         | *说明*                                                                                                                                       | *默认值*    |
|------------------------------------|--------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|----------|
| name                               | String                         | 读写分离数据源名称                                                                                                                                  | -        |
| writeDataSourceName                | String                         | 写库数据源名称                                                                                                                                    | -        |
| readDataSourceNames                | List\<String\>                 | 读库数据源名称                                                                                                                                    | -        |
| transactionalReadQueryStrategy (?) | TransactionalReadQueryStrategy | 事务内读请求的路由策略，可选值：PRIMARY（路由至主库）、FIXED（同一事务内路由至固定数据源）、DYNAMIC（同一事务内路由至非固定数据源）。 **注意：`FIXED` 和 `DYNAMIC` 需要数据库支持主从强一致同步能力才能使用，例如：openGauss。** | PRIMARY  |
| loadBalancerName (?)               | String                         | 读库负载均衡算法名称                                                                                                                                 | 轮询负载均衡算法 |

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/common-config/builtin-algorithm/load-balance)。

## 操作步骤

1. 添加读写分离数据源
1. 设置负载均衡算法
1. 使用读写分离数据源
   
## 配置示例

```java
public DataSource getDataSource() throws SQLException {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadwriteSplittingDataSourceRuleConfiguration(
                "demo_read_query_ds", "demo_write_ds", Arrays.asList("demo_read_ds_0", "demo_read_ds_1"), "demo_weight_lb");
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

## 相关参考

- [核心特性：读写分离](/cn/features/readwrite-splitting/)
- [YAML 配置：读写分离](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/readwrite-splitting/)
