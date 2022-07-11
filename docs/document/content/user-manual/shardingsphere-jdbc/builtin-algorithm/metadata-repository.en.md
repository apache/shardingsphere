+++
title = "Metadata Repository"
weight = 1
+++

## H2 Repository

Type: H2

Mode: Standalone

Attributes:

| *Name*                       | *Type* | *Description*                     | *Default Value*                                                         |
| ---------------------------- | ------ | --------------------------------- | ----------------------------------------------------------------------- |
| jdbcUrl                      | String | Database access URL               | jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL |
| user                         | String | Database access username          | sa                                                                      |
| password                     | String | Database access password          |                                                                         |

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
