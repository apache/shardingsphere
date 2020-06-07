+++
title = "Read-write Split"
weight = 2
+++

## Root Configuration

Class name: org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration

Attributes:

| *Name*          | *DataType*                                           | *Description*                     |
| --------------- | ---------------------------------------------------- | --------------------------------- |
| dataSources (+) | Collection\<MasterSlaveDataSourceRuleConfiguration\> | Data sources of master and slaves |

## Master Slave Data Source Configuration

Class name: org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration

Attributes:

| *Name*                  | *DataType*                       | *Description*                         | *Default Value*                    |
| ----------------------- | -------------------------------- | ------------------------------------- | ---------------------------------- |
| name                    | String                           | Read-write split data source name     | -                                  |
| masterDataSourceName    | String                           | Master database source name           | -                                  |
| slaveDataSourceNames    | Collection\<String\>             | Slave database source name list       | -                                  |
| loadBalanceStrategy (?) | LoadBalanceStrategyConfiguration | Slave database load balance algorithm | Round robin load balance algorithm |

## Slave Data Sources Load Balance Strategy Configuration

| *Name*         | *DataType* | *Description*                                        | *Default Value* |
| -------------- | ---------- | ---------------------------------------------------- | --------------- |
| type           | String     | Slave data sources load balance algorithm type       | -               |
| properties (?) | Properties | Slave data sources load balance algorithm Properties | Empty           |

Apache ShardingSphere built-in implemented classes of MasterSlaveLoadBalanceAlgorithm are:

### Round Robin Algorithm

Class name: org.apache.shardingsphere.masterslave.strategy.RoundRobinMasterSlaveLoadBalanceAlgorithm

Attributes: None

### Random Algorithm

Class name: org.apache.shardingsphere.masterslave.strategy.RandomMasterSlaveLoadBalanceAlgorithm

Attributes: None
