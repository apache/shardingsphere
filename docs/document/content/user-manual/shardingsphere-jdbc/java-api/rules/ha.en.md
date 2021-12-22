+++
title = "HA"
weight = 3
+++

## Root Configuration

Class name：org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration

Attributes：

| *Name*                      | *DataType*                                                   | *Description*                        |
| -------------------------  | ------------------------------------------------------------- | ------------------------------------ |
| dataSources (+)            | Collection\<DatabaseDiscoveryDataSourceRuleConfiguration\>    | Data source configuration            |
| discoveryHeartbeats (+)    | Map\<String, DatabaseDiscoveryHeartBeatConfiguration\>        | Detect heartbeat configuration       |
| discoveryTypes (+)         | Map\<String, ShardingSphereAlgorithmConfiguration\>           | Database discovery type configuration  |

## Data Source Configuration

Class name：org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration

Attributes：

| *Name*                     | *DataType*             | *Description*                                                                          | *Default Value* |
| -------------------------- | -------------------- | ---------------------------------------------------------------------------------------- | ------------- |
| name (+)                   | String               | Data source name                                                                         | -             |
| dataSourceNames (+)        | Collection\<String\> | Data source names, multiple data source names separated with comma. Such as: ds_0, ds_1  | -             |
| discoveryHeartbeatName (+) | String               | Detect heartbeat name                                                                    | -             |
| discoveryTypeName (+)      | String               | Database discovery type name                                                             | -             |

## Detect Heartbeat Configuration

Class name：org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration

Attributes：

| *Name*                     | *DataType*             | *Description*                                                                                                      | *Default Value*  |
| -------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------------ | ------------- |
| props (+)                  | Properties             | Detect heartbeat attribute configuration, keep-alive-cron configuration, cron expression. Such as: '0/5 * * * * ?'  | -             |

## Database Discovery Type Configuration

Class name：org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration

Attributes：

| *Name*                     | *DataType*             | *Description*                                                                   | *Default Value*       |
| -------------------------- | ---------------------- | ------------------------------------------------------------------------------- | ------------- |
| type (+)                   | String                 | Database discovery type, such as: MGR、openGauss                                  | -             |
| props (?)                  | Properties             | Required parameters for high-availability types, such as MGR's group-name       | -             |
