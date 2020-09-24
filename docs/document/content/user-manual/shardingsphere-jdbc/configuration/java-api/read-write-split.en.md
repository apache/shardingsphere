+++
title = "Read-write Split"
weight = 2
+++

## Root Configuration

Class name: PrimaryReplicaReplicationRuleConfiguration

Attributes:

| *Name*            | *DataType*                                                         | *Description*                                                        |
| ----------------- | ------------------------------------------------------------------ | -------------------------------------------------------------------- |
| dataSources (+)   | Collection\<PrimaryReplicaReplicationDataSourceRuleConfiguration\> | Data sources of primary and replicas                                 |
| loadBalancers (*) | Map\<String, ShardingSphereAlgorithmConfiguration\>                | Load balance algorithm name and configurations of replica data sources |

## Primary-Replica Replication Data Source Configuration

Class name: PrimaryReplicaReplicationDataSourceRuleConfiguration

Attributes:

| *Name*                     | *DataType*           | *Description*                                  | *Default Value*                    |
| -------------------------- | -------------------- | ---------------------------------------------- | ---------------------------------- |
| name                       | String               | Read-write split data source name              | -                                  |
| primaryDataSourceName      | String               | Primary sources source name                    | -                                  |
| replicaDataSourceNames (+) | Collection\<String\> | Replica sources source name list               | -                                  |
| loadBalancerName (?)       | String               | Load balance algorithm name of replica sources | Round robin load balance algorithm |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
