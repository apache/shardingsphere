+++
title = "Metadata Repository"
weight = 1
+++

## File Repository

Type: File

Mode: Standalone

Attributes:

| *Name*                       | *Type* | *Description*                     | *Default Value* |
| ---------------------------- | ------ | --------------------------------- | --------------- |
| path                         | String | Path for metadata persist         | .shardingsphere |

## ZooKeeper Repository

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

## Etcd Repository

Type: Etcd

Mode: Cluster

Attributes:

| *Name*                       | *Type* | *Description*                     | *Default Value* |
| ---------------------------- | ------ | --------------------------------- | --------------- |
| timeToLiveSeconds            | long   | Seconds of ephemeral data live    | 30              |
| connectionTimeout            | long   | Seconds of connection timeout     | 30              |
