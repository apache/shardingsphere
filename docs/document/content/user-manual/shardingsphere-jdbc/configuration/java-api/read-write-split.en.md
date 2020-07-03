+++
title = "Read-write Split"
weight = 2
+++

## Root Configuration

Class name: org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration

Attributes:

| *Name*            | *DataType*                                           | *Description*                                                        |
| ----------------- | ---------------------------------------------------- | -------------------------------------------------------------------- |
| dataSources (+)   | Collection\<MasterSlaveDataSourceRuleConfiguration\> | Data sources of master and slaves                                    |
| loadBalancers (*) | Map\<String, ShardingSphereAlgorithmConfiguration\>  | Load balance algorithm name and configurations of slave data sources |

## Master Slave Data Source Configuration

Class name: org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration

Attributes:

| *Name*                   | *DataType*           | *Description*                                | *Default Value*                    |
| ------------------------ | -------------------- | -------------------------------------------- | ---------------------------------- |
| name                     | String               | Read-write split data source name            | -                                  |
| masterDataSourceName     | String               | Master sources source name                   | -                                  |
| slaveDataSourceNames (+) | Collection\<String\> | Slave sources source name list               | -                                  |
| loadBalancerName (?)     | String               | Load balance algorithm name of slave sources | Round robin load balance algorithm |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
