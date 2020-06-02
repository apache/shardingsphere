+++
title = "Governance"
weight = 6
+++

## Root Configuration

Class name: org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration

Attributes:

| *Name*                   | *Data Type*                         | *Description*                                                                                                         |
| ------------------------ | ----------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| instanceConfigurationMap | Map\<String, CenterConfiguration\>  | Config map of config-center&registry-center, the key is center's name, the value is the config-center/registry-center |

## Config / Registry Center Configuration

Class name: org.apache.shardingsphere.orchestration.center.config.CenterConfiguration

Attributes:

| *Name*            | *Data Type* | *Description*                                                                                                                               |
| ----------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| instanceType      | String      | The type of center instance(zookeeper/etcd/apollo/nacos)                                                                                    |
| properties        | String      | Properties for center instance config, such as options of zookeeper                                                                         |
| orchestrationType | String      | The type of orchestration center: config_center or registry_center or metadata_center, multiple types are separated by commas               |
| serverLists       | String      | Connect to server lists in center, including IP address and port number; addresses are separated by commas, such as `host1:2181,host2:2181` |
| namespace (?)     | String      | Namespace of center instance                                                                                                                |

### Common Properties Configuration

| *Name*          | *Data Type* | *Description*                                                                                                                             | *Default Value* |
| --------------- | ----------- | ----------------------------------------------------------------------------------------------------------------------------------------- | --------------- |
| overwrite       | boolean     | Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations | false           |

### ZooKeeper Properties Configuration

| *Name*                           | *Data Type* | *Description*                                  | *Default Value*       |
| -------------------------------- | ----------- | ---------------------------------------------- | --------------------- |
| digest (?)                       | String      | Connect to authority tokens in registry center | No need for authority |
| operationTimeoutMilliseconds (?) | int         | The operation timeout milliseconds             | 500 milliseconds      |
| maxRetries (?)                   | int         | The maximum retry count                        | 3                     |
| retryIntervalMilliseconds (?)    | int         | The retry interval milliseconds                | 500 milliseconds      |
| timeToLiveSeconds (?)            | int         | Time to live seconds for ephemeral nodes       | 60 seconds            |


### Etcd Properties Configuration

| *Name*                | *Data Type* | *Description*                         | *Default Value* |
| --------------------- | ----------- | ------------------------------------- | --------------- |
| timeToLiveSeconds (?) | long        | Time to live seconds for data persist | 30 seconds      |

### Apollo Properties Configuration

| *Name*             | *Data Type* | *Description*                | *Default Value*       |
| ------------------ | ----------- | ---------------------------- | --------------------- |
| appId (?)          | String      | Apollo appId                 | APOLLO_SHARDINGSPHERE |
| env (?)            | String      | Apollo env                   | DEV                   |
| clusterName (?)    | String      | Apollo clusterName           | default               |
| administrator (?)  | String      | Apollo administrator         | Empty                 |
| token (?)          | String      | Apollo token                 | Empty                 |
| portalUrl (?)      | String      | Apollo portalUrl             | Empty                 |
| connectTimeout (?) | int         | Connect timeout milliseconds | 1000 milliseconds     |
| readTimeout (?)    | int         | Read timeout milliseconds    | 5000 milliseconds     |

### Nacos Properties Configuration

| *Name*      | *Data Type* | *Description* | *Default Value*               |
| ----------- | ----------- | ------------- | ----------------------------- |
| group (?)   | String      | group         | SHARDING_SPHERE_DEFAULT_GROUP |
| timeout (?) | long        | timeout       | 3000 milliseconds             |
