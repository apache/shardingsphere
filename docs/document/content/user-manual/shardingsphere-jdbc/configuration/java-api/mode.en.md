+++
title = "Mode"
weight = 5
+++

## Configuration Item Explanation

### Memory mode

*Configuration Entrance*

Class name: org.apache.shardingsphere.infra.config.mode.ModeConfiguration

Attributes:

| *Name*                      | *Data Type*                  | *Description*                                                                                                                             |
| --------------------------- | ---------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| type (?)                    | String                       | Memory                                                                                                                                    |

### Standalone mode

*Configuration Entrance*

Class name: org.apache.shardingsphere.infra.config.mode.ModeConfiguration

| *Name*      | *Data Type*                    | *Description*                                                                                                                       |
| ----------- | ------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------|
| type        | String                         | Standalone     　                                                                                                                    |
| repository  | PersistRepositoryConfiguration | Configuration StandalonePersistRepositoryConfiguration                                                                               |
| overwrite   | boolean                        | Local configurations overwrite file configurations or not; if they overwrite, each start takes reference of local configurations    |

*StandalonePersistRepositoryConfiguration Configuration*

Class name: org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration

Attributes:

| *Name*         | *Data Type* | *Description*                                        |
| ------------- | ---------- | ------------------------------------------------------|
| type          | String     | Standalone Configuration persist type, such as: File  |
| props (?)     | Properties | Configuration persist properties, such as: path      |

Standalone Properties Configuration:

| *Name*                            | *Data Type* | *Description*                           | *Default*                 |
| --------------------------------  | ---------   | --------------------------------------- | ------------------------  |
| path                             | String       | Configuration information persist path  | .shardingsphere directory |

### Cluster mode

*Configuration Entrance*

Class name: org.apache.shardingsphere.infra.config.mode.ModeConfiguration

Attributes:

| *Name*      | *Data Type*                    | *Description*                                                                                                                              |
| ----------- | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------|
| type        | String                         | Cluster    　                                                                                                                              |
| repository  | PersistRepositoryConfiguration | Configuration ClusterPersistRepositoryConfiguration                                                                                        |
| overwrite   | boolean                        | Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations  |

*ClusterPersistRepositoryConfiguration Configuration*

Class name: org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration

Attributes:

| *Name*         | *Data Type* | *Description*                                                                                                     |
| ------------- | ---------- | ------------------------------------------------------------------------------------------------------------------- |
| type          | String     | Cluster mode typ, such as: Zookeeper, Etcd                                                                          |
| namespace     | String     | Cluster mode instance namespace, such as: cluster-sharding-mode                                                     |
| server-lists  | String     | Zookeeper or Etcd server list，including IP and port number, use commas to separate, such as: host1:2181,host2:2181 |
| props         | Properties | Properties for center instance config, such as options of zookeeper                                                |

ZooKeeper Properties Configuration

| *Name*                           | *Data Type* | *Description*                                  | *Default Value*       |
| -------------------------------- | ----------- | ---------------------------------------------- | --------------------- |
| digest (?)                       | String      | Connect to authority tokens in registry center | No need for authority |
| operationTimeoutMilliseconds (?) | int         | The operation timeout milliseconds             | 500 milliseconds      |
| maxRetries (?)                   | int         | The maximum retry count                        | 3                     |
| retryIntervalMilliseconds (?)    | int         | The retry interval milliseconds                | 500 milliseconds      |
| timeToLiveSeconds (?)            | int         | Time to live seconds for ephemeral nodes       | 60 seconds            |


Etcd Properties Configuration

| *Name*                | *Data Type* | *Description*                         | *Default Value* |
| --------------------- | ----------- | ------------------------------------- | --------------- |
| timeToLiveSeconds (?) | long        | Time to live seconds for data persist | 30 seconds      |
