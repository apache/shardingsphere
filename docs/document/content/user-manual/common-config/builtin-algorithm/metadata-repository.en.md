+++
title = "Metadata Repository"
weight = 1
+++

## Background

Apache ShardingSphere provides different metadata persistence methods for different running modes. Users can choose an appropriate way to store metadata while configuring the running mode.

## Parameters

### Database Repository

Type: JDBC

Mode: Standalone

Attributes:

| *Name*                       | *Type* | *Description*                     | *Default Value*                                                         |
| ---------------------------- | ------ | --------------------------------- | ----------------------------------------------------------------------- |
| provider                    | String      | Type for metadata persist     | H2              |
| jdbc_url                    | String      | JDBC URL              | jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL              |
| username                    | String      | username              | sa              |
| password                    | String      | password              |                 |


### ZooKeeper Repository

Type: ZooKeeper

Mode: Cluster

Attributes:

| *Name*                       | *Type* | *Description*                     | *Default Value* |
| ---------------------------- | ------ | --------------------------------- | --------------- |
| retryIntervalMilliseconds    | int    | Milliseconds of retry interval    | 500             |
| maxRetries                   | int    | Max retries of client connection  | 3               |
| timeToLiveSeconds            | int    | Seconds of ephemeral data live    | 60              |
| operationTimeoutMilliseconds | int    | Milliseconds of operation timeout | 500             |
| digest                       | String | Password of login                 |                 |

### Etcd Repository

Type: Etcd

Mode: Cluster

Attributes:

| *Name*                       | *Type* | *Description*                     | *Default Value* |
| ---------------------------- | ------ | --------------------------------- | --------------- |
| timeToLiveSeconds            | long   | Seconds of ephemeral data live    | 30              |
| connectionTimeout            | long   | Seconds of connection timeout     | 30              |

### Nacos Repository

Type: Nacos

Mode: Cluster

Attributes:

| *Name*                       | *Type* | *Description*                                     | *Default Value* |
| ---------------------------- | ------ | ------------------------------------------------- | --------------- |
| clusterIp                    | String | Unique identifier in cluster                      | Host IP         |
| retryIntervalMilliseconds    | long   | Milliseconds of retry interval                    | 500             |
| maxRetries                   | int    | Max retries for client to check data availability | 3               |
| timeToLiveSeconds            | int    | Seconds of ephemeral instance live                | 30              |

### Consul Repository

Type: Consul

Mode: Cluster

Attributes:

| *Name*                       | *Type*  | *Description*                                     | *Default Value* |
| ---------------------------- | ------- | ------------------------------------------------- | --------------- |
| timeToLiveSeconds            | String  | Seconds of ephemeral instance live                | 30s             |
| blockQueryTimeToSeconds      | long    | Seconds of query timeout                          | 60              |

## Procedure

1. Configure running mode in server.yaml.
1. Configure metadata persistence warehouse type.

## Sample

- Standalone mode configuration method.

```yaml
mode:
  type: Standalone
  repository:
    type: JDBC
    props:
      provider: H2
      jdbc_url: jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
      username: test
      password: Test@123
```

- Cluster mode.

```yaml
mode:
  type: Cluster
  repository:
    type: zookeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500
```
