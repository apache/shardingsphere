+++
title = "影子库"
weight = 5
+++

## 配置入口

类名称：org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration

可配置属性：

| *名称*                      | *数据类型*                                           | *说明*               | *默认值* |
| -------------------------- | --------------------------------------------------- | ------------------- | ------- |
| enable                     | boolean                                             | 影子库开关            | false   |
| dataSources                | Map\<String, ShadowDataSourceConfiguration\>        | 影子数据源映射名称和配置 |        |
| tables                     | Map\<String, ShadowTableConfiguration\>             | 影子表名称和配置       |        |
| defaultShadowAlgorithmName | String                                              | 默认影子算法名称       |        |
| shadowAlgorithms           | Map\<String, ShardingSphereAlgorithmConfiguration\> | 影子算法名称和配置     | 无      |

## 影子数据源配置

类名称：org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration

可配置属性：

| *名称*                | *数据类型* | *说明*       |
| -------------------- | --------- | ----------- |
| sourceDataSourceName | String    | 生产数据源名称 |
| shadowDataSourceName | String    | 影子数据源名称 |

## 影子表配置

类名称：org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration

可配置属性：

| *名称*                | *数据类型*            | *说明*                   |
| -------------------- | -------------------- | ----------------------- |
| dataSourceNames      | Collection\<String\> | 影子表关联影子数据源名称列表 |
| shadowAlgorithmNames | Collection\<String\> | 影子表关联影子算法名称列表   |

## 影子算法配置

算法类型的详情，请参见[内置影子算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/shadow)。
