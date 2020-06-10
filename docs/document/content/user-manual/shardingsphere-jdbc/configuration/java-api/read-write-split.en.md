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
| loadBalancers (*) | Map\<String, LoadBalanceAlgorithmConfiguration\>     | Load balance algorithm name and configurations of slave data sources |

## Master Slave Data Source Configuration

Class name: org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration

Attributes:

| *Name*               | *DataType*           | *Description*                                | *Default Value*                    |
| -------------------- | -------------------- | -------------------------------------------- | ---------------------------------- |
| name                 | String               | Read-write split data source name            | -                                  |
| masterDataSourceName | String               | Master sources source name                   | -                                  |
| slaveDataSourceNames | Collection\<String\> | Slave sources source name list               | -                                  |
| loadBalancerName (?) | String               | Load balance algorithm name of slave sources | Round robin load balance algorithm |

## Slave Data Sources Load Balance Algorithm Configuration

| *Name*         | *DataType* | *Description*                                        | *Default Value* |
| -------------- | ---------- | ---------------------------------------------------- | --------------- |
| type           | String     | Slave data sources load balance algorithm type       | -               |
| properties (?) | Properties | Slave data sources load balance algorithm Properties | Empty           |

Apache ShardingSphere built-in implemented classes of MasterSlaveLoadBalanceAlgorithm are:

### Round Robin Algorithm

Class name: org.apache.shardingsphere.masterslave.algorithm.RoundRobinMasterSlaveLoadBalanceAlgorithm

Attributes: None

### Random Algorithm

Class name: org.apache.shardingsphere.masterslave.algorithm.RandomMasterSlaveLoadBalanceAlgorithm

Attributes: None
