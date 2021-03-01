+++
title = "Governance"
weight = 5
+++

## Configuration Item Explanation

### Management

*Configuration Entrance*

Class name: org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration

Attributes:

| *Name*                              | *Data Type*                         | *Description*                                                                                                       |
| ----------------------------------- | ----------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| name         | String  | Governance instance name |
| registryCenterConfiguration         | GovernanceCenterConfiguration  | Config of registry-center |
| additionalConfigCenterConfiguration | GovernanceCenterConfiguration  | Config additional of config-center |

The type of registryCenter could be Zookeeper or etcd.
The type of additional ConfigCenter could be Zookeeper or etcd, Apollo, Nacos.

*Governance Instance Configuration*

Class name: org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration

Attributes:

| *Name*        | *Data Type* | *Description*                                                                                                                                    |
| ------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| type          | String      | Governance instance type, such as: Zookeeper, etcd, Apollo, Nacos                                                                             |
| serverLists   | String      | The list of servers that connect to governance instance, including IP and port number, use commas to separate, such as: host1:2181,host2:2181 |                                                                                                                    |
| props         | Properties  | Properties for center instance config, such as options of zookeeper                                                                              |
| overwrite       | boolean     | Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations | 

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

Apollo Properties Configuration

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

Nacos Properties Configuration

| *Name*      | *Data Type* | *Description* | *Default Value*               |
| ----------- | ----------- | ------------- | ----------------------------- |
| group (?)   | String      | group         | SHARDING_SPHERE_DEFAULT_GROUP |
| timeout (?) | long        | timeout       | 3000 milliseconds             |
