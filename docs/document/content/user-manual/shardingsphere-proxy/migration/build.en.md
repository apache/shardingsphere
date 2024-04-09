+++
title = "Build"
weight = 1
+++

## Background

For systems running on a single database that urgently need to securely and simply migrate data to a horizontally sharded database.

## Prerequisites

-  Proxy is developed in JAVA, and JDK version 1.8 or later is recommended. 
- Data migration adopts the cluster mode, and ZooKeeper is currently supported as the registry.

## Procedure

1. Get ShardingSphere-Proxy. Please refer to [proxy startup guide](/en/user-manual/shardingsphere-proxy/startup/bin/) for details.

2. Modify the configuration file `conf/global.yaml`. Please refer to [mode configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/mode/) for details.

Currently, `mode` must be `Cluster`, and the corresponding registry must be started in advance.

Configuration sample:
```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500
```

3. Introduce JDBC driver.

Proxy has included JDBC driver of PostgreSQL and openGauss.

If the backend is connected to the following databases, download the corresponding JDBC driver jar package and put it into the `${shardingsphere-proxy}/ext-lib` directory.

| Database | JDBC Driver                                                                                          |
|----------|------------------------------------------------------------------------------------------------------|
| MySQL    | [mysql-connector-java-8.0.31.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.31/) |

If you are migrating to a heterogeneous database, then you could use more types of database. Introduce JDBC driver as above too.

4. Start ShardingSphere-Proxy:

```
sh bin/start.sh
```

5. View the proxy log `logs/stdout.log`. If you see the following statements:

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy start success
```

The startup will have been successful.

6. Configure and migrate on demand.

6.1. Query configuration.

```sql
SHOW MIGRATION RULE;
```

The default configuration is as follows.

```
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
| read                                                         | write                                | stream_channel                                        |
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
| {"workerThread":20,"batchSize":1000,"shardingSize":10000000} | {"workerThread":20,"batchSize":1000} | {"type":"MEMORY","props":{"block-queue-size":"2000"}} |
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
```

6.2. Alter configuration (Optional).

Since the migration rule has default values, there is no need to create it, only the `ALTER` statement is provided.

A completely configured DistSQL is as follows.

```sql
ALTER MIGRATION RULE (
READ(
  WORKER_THREAD=20,
  BATCH_SIZE=1000,
  SHARDING_SIZE=10000000,
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))
),
WRITE(
  WORKER_THREAD=20,
  BATCH_SIZE=1000,
  RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='2000')))
),
STREAM_CHANNEL (TYPE(NAME='MEMORY',PROPERTIES('block-queue-size'='2000')))
);
```

Configuration item description:

```sql
ALTER MIGRATION RULE (
READ( -- Data reading configuration. If it is not configured, part of the parameters will take effect by default.
  WORKER_THREAD=20, -- Obtain the thread pool size of all the data from the source side. If it is not configured, the default value is used.
  BATCH_SIZE=1000, -- The maximum number of records returned by a query operation. If it is not configured, the default value is used.
  SHARDING_SIZE=10000000, -- Sharding size of all the data. If it is not configured, the default value is used.
  RATE_LIMITER ( -- Traffic limit algorithm. If it is not configured, traffic is not limited.
  TYPE( -- Algorithm type. Option: QPS
  NAME='QPS',
  PROPERTIES( -- Algorithm property
  'qps'='500'
  )))
),
WRITE( -- Data writing configuration. If it is not configured, part of the parameters will take effect by default.
  WORKER_THREAD=20, -- The size of the thread pool on which data is written into the target side. If it is not configured, the default value is used.
  BATCH_SIZE=1000, -- The maximum number of records for a batch write operation. If it is not configured, the default value is used.
  RATE_LIMITER ( -- Traffic limit algorithm. If it is not configured, traffic is not limited.
  TYPE( -- Algorithm type. Option: TPS
  NAME='TPS',
  PROPERTIES( -- Algorithm property.
  'tps'='2000'
  )))
),
STREAM_CHANNEL ( -- Data channel. It connects producers and consumers, used for reading and writing procedures. If it is not configured, the MEMORY type is used by default.
TYPE( -- Algorithm type. Option: MEMORY
NAME='MEMORY',
PROPERTIES( -- Algorithm property
'block-queue-size'='2000' -- Property: blocking queue size.
)))
);
```
