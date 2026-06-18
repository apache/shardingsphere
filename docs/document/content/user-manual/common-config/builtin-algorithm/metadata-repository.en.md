+++
title = "Metadata Repository"
weight = 1
+++

## Background

Apache ShardingSphere provides different metadata persistence methods for different running modes. Users can freely choose the most appropriate way to store metadata while configuring the running mode.

## Parameters

### Database Repository

The optional values of `provider` are H2, MySQL and HSQLDB.
Since third-party Vulnerability Reports often misreport H2 Database, avoiding the use of H2 Database in ShardingSphere Standalone Mode may be an option.
Discuss the case where `provider` is not the default value `H2`.

1. If `provider` is set to `MySQL`, a ready MySQL Server is required. The classpath should contain the Maven dependency of `com.mysql:mysql-connector-j:9.0.0`.

2. If `provider` is set to `HSQLDB`, a ready HyperSQL using Server Modes is required, or a database is created as an in-process database.
   The classpath should contain the Maven dependency of `org.hsqldb:hsqldb:2.7.3` with `classifier` as `jdk8`.
   There is no available Docker Image for HyperSQL using Server Modes, and users may need to manually start HyperSQL using Server Modes.
   If HyperSQL using mem: protocol is used, the possible configuration is as follows,

```yaml
mode:
   type: Standalone
   repository:
      type: JDBC
      props:
         provider: HSQLDB
         jdbc_url: jdbc:hsqldb:mem:config
         username: SA
```

Type: JDBC

Mode: Standalone

Attributes:

| *Name*   | *Type* | *Description*             | *Default Value*                                                         |
|----------|--------|---------------------------|-------------------------------------------------------------------------|
| provider | String | Type for metadata persist | H2                                                                      |
| jdbc_url | String | JDBC URL                  | jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL |
| username | String | username                  | sa                                                                      |
| password | String | password                  |                                                                         |

### ZooKeeper Repository

Type: ZooKeeper

Mode: Cluster

Attributes:

| *Name*                       | *Type* | *Description*                     | *Default Value* |
|------------------------------|--------|-----------------------------------|-----------------|
| retryIntervalMilliseconds    | int    | Milliseconds of retry interval    | 500             |
| maxRetries                   | int    | Max retries of client connection  | 3               |
| timeToLiveSeconds            | int    | Seconds of ephemeral data live    | 60              |
| operationTimeoutMilliseconds | int    | Milliseconds of operation timeout | 500             |
| digest                       | String | Password of login                 |                 |

### Etcd Repository

Type: Etcd

Mode: Cluster

Attributes:

| *Name*            | *Type* | *Description*                  | *Default Value* |
|-------------------|--------|--------------------------------|-----------------|
| timeToLiveSeconds | long   | Seconds of ephemeral data live | 30              |
| connectionTimeout | long   | Seconds of connection timeout  | 30              |

## Procedure

1. Configure running mode in global.yaml.
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
      password: Test@9876
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
