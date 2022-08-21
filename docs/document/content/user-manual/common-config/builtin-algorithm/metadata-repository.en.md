+++
title = "Metadata Repository"
weight = 1
+++

## Background

Apache ShardingSphere provides different metadata persistence methods for different running modes. Users can choose an appropriate way to store metadata while configuring the running mode.

## Parameters
### File Repository

Type: File

Mode: Standalone

Attributes:

| *Name*                       | *Type* | *Description*                     | *Default Value*                                                         |
| ---------------------------- | ------ | --------------------------------- | ----------------------------------------------------------------------- |
|path|	String|	Path for metadata persist	|.shardingsphere|

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

## Procedure

1. Configure running mode in server.yaml.
1. Configure metadata persistence warehouse type.

## Sample

- Standalone mode configuration method.

```yaml
mode:
  type: Standalone
  repository:
    type: File
    props:
       path: ~/user/.shardingsphere
  overwrite: false
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
  overwrite: false
```
