+++
title = "影子库"
weight = 6
+++

## 背景信息

如果您只想使用 Java API 方式配置使用 ShardingSphere 影子库功能请参考以下配置。

## 参数解释
### 配置入口

类名称：org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

可配置属性：

| *名称*                      | *数据类型*                                    | *说明*               |
| -------------------------- | -------------------------------------------- | ------------------- |
| dataSources                | Map\<String, ShadowDataSourceConfiguration\> | 影子数据源映射名称和配置 |
| tables                     | Map\<String, ShadowTableConfiguration\>      | 影子表名称和配置        |
| shadowAlgorithms           | Map\<String, AlgorithmConfiguration\>        | 影子算法名称和配置      |
| defaultShadowAlgorithmName | String                                       | 默认影子算法名称        |

### 影子数据源配置

类名称：org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration

可配置属性：

| *名称*                    | *数据类型* | *说明*       |
| ------------------------ | --------- | ----------- |
| productionDataSourceName | String    | 生产数据源名称 |
| shadowDataSourceName     | String    | 影子数据源名称 |

### 影子表配置

类名称：org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration

可配置属性：

| *名称*                | *数据类型*            | *说明*                      |
| -------------------- | -------------------- | -------------------------- |
| dataSourceNames      | Collection\<String\> | 影子表关联影子数据源映射名称列表 |
| shadowAlgorithmNames | Collection\<String\> | 影子表关联影子算法名称列表      |

### 影子算法配置

类名称：org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration

可配置属性：

| *名称*  | *数据类型* | *说明* |
| ------- | ---------- | ------- |
| type | String | 影子算法类型 |
| props | Properties | 影子算法配置 |

算法类型的详情，请参见[内置影子算法列表](/cn/user-manual/common-config/builtin-algorithm/shadow)。

## 操作步骤

1. 创建生产和影子数据源。
1. 配置影子规则
- 配置影子数据源
- 配置影子表
- 配置影子算法

## 配置示例

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
    
    private Map<String, AlgorithmConfiguration> createShadowAlgorithmConfigurations() {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>();
        Properties userIdInsertProps = new Properties();
        userIdInsertProps.setProperty("operation", "insert");
        userIdInsertProps.setProperty("column", "user_type");
        userIdInsertProps.setProperty("value", "1");
        result.put("user-id-insert-match-algorithm", new AlgorithmConfiguration("VALUE_MATCH", userIdInsertProps));
        return result;
    }
}
```

## 相关参考

[影子库的特性描述](/cn/features/shadow/)
