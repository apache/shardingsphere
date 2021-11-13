+++
title = "Mode Configuration"
weight = 1
chapter = true
+++

## Root Configuration

Class name: org.apache.shardingsphere.infra.config.mode.ModeConfiguration

Attributes:

| *Name*     | *DataType*                     | *Description*                                                                                                                                                                                                          | *Default Value* |
| ---------- | ------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- |
| type       | String                         | Type of mode configuration<br />Values could be: Memory, Standalone, Cluster                                                                                                                                           | Memory          |
| repository | PersistRepositoryConfiguration | Persist repository configuration<br />Memory type does not need persist, could be null<br />Standalone type uses StandalonePersistRepositoryConfiguration<br />Cluster type uses ClusterPersistRepositoryConfiguration |                 |
| overwrite  | boolean                        | Whether overwrite persistent configuration with local configuration                                                                                                                                                    | false           |

## Standalone Persist Configuration

Class name: org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration

Attributes:

| *Name* | *DataType* | *Description*                    |
| ------ | ---------- | -------------------------------- |
| type   | String     | Type of persist repository       |
| props  | Properties | Properties of persist repository |

## Cluster Persist Configuration

Class name: org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration

Attributes:

| *Name*      | *DataType* | *Description*                    |
| ----------- | ---------- | -------------------------------- |
| type        | String     | Type of persist repository       |
| namespace   | String     | Namespace of registry center     |
| serverLists | String     | Server lists of registry center  |
| props       | Properties | Properties of persist repository |
