+++
title = "Readwrite-splitting"
weight = 2
+++

## Root Configuration

Class name: ReadwriteSplittingRuleConfiguration

Attributes:

| *Name*            | *DataType*                                            | *Description*                                                          |
| ----------------- | ----------------------------------------------------- | ---------------------------------------------------------------------- |
| dataSources (+)   | Collection\<ReadwriteSplittingDataSourceRuleConfiguration\> | Data sources of write and reads                                  |
| loadBalancers (*) | Map\<String, ShardingSphereAlgorithmConfiguration\>   | Load balance algorithm name and configurations of replica data sources |

## Readwrite-splitting Data Source Configuration

Class name: ReadwriteSplittingDataSourceRuleConfiguration

Attributes:

| *Name*                     | *DataType*           | *Description*                                  | *Default Value*                    |
| -------------------------- | -------------------- | ---------------------------------------------- | ---------------------------------- |
| name                       | String               | Readwrite-splitting data source name           | -                                  |
| writeDataSourceName        | String               | Write sources source name                      | -                                  |
| readDataSourceNames (+)    | Collection\<String\> | Read sources source name list                  | -                                  |
| loadBalancerName (?)       | String               | Load balance algorithm name of replica sources | Round robin load balance algorithm |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
