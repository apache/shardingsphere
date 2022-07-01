+++
title = "Readwrite-splitting"
weight = 2
+++

## Root Configuration

Class name: org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration

Attributes:

| *Name*            | *DataType*                                                  | *Description*                                                          |
| ----------------- | ----------------------------------------------------------- | ---------------------------------------------------------------------- |
| dataSources (+)   | Collection\<ReadwriteSplittingDataSourceRuleConfiguration\> | Data sources of write and reads                                        |
| loadBalancers (*) | Map\<String, ShardingSphereAlgorithmConfiguration\>         | Load balance algorithm name and configurations of replica data sources |

## Readwrite-splitting Data Source Configuration

Class name: org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration

Attributes:

| *Name*                     | *DataType*           | *Description*                                     | *Default Value*                   |
| -------------------------- | -------------------- | ------------------------------------------------- | ----------------------------------|
| name                       | String               | Readwrite-splitting data source name              | -                                 |
| staticStrategy             | String               | Static Readwrite-splitting configuration          | -                                 |
| dynamicStrategy            | Properties           | Dynamic Readwrite-splitting configuration         | -                                  |
| loadBalancerName (?)       | String               | Load balance algorithm name of replica sources    | Round robin load balance algorithm |


Class name：org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration

Attributes：

| *Name*                     | *DataType*           | *Description*               | *Default Value* |
| -------------------------- | ---------------------| ----------------------------| ----------------|
| writeDataSourceName        | String               | Write data source name      | -               |
| readDataSourceNames        | List\<String\>       | Read data sources list      | -               |

Class name：org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration

Attributes：

| *Name*                          | *DataType*        | *Description*                                                                                               | *Default Value*    |
| ------------------------------- | -------------------| ------------------------------------------------------------------------------------------------------------| -------------------|
| autoAwareDataSourceName         | String             | Database discovery logic data source name                                                                   | -                  |
| writeDataSourceQueryEnabled (?) | String             | All read data source are offline, write data source whether the data source is responsible for read traffic | true               |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance) for more details about type of algorithm.
Please refer to [Use Norms](/en/features/readwrite-splitting/use-norms) for more details about query consistent routing.
