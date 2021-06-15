+++
title = "Governance"
weight = 5
+++

## Configuration Item Explanation

### Management

*Configuration Entrance*

Class name: org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration

Attributes:

| *Name*                      | *Data Type*                  | *Description*             |
| --------------------------- | ---------------------------- | ------------------------- |
| name                        | String                       | Governance instance name  |
| registryCenterConfiguration | RegistryCenterConfiguration  | Config of registry-center |

The type of registryCenter could be Zookeeper or Etcd.

*Governance Instance Configuration*

Class name: org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration

Attributes:

| *Name*      | *Data Type* | *Description*                                                                                                                                 |
| ----------- | ----------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| type        | String      | Governance instance type, such as: Zookeeper, etcd                                                                                            |
| serverLists | String      | The list of servers that connect to governance instance, including IP and port number, use commas to separate, such as: host1:2181,host2:2181 |
| props       | Properties  | Properties for center instance config, such as options of zookeeper                                                                           |
| overwrite   | boolean     | Local configurations overwrite config center configurations or not; if they overwrite, each start takes reference of local configurations     | 

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
