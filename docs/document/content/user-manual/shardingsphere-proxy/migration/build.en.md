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

1. Run the following command to compile the ShardingSphere-Proxy binary package: 

```
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```

Release package：
- /shardingsphere-distribution/shardingsphere-proxy-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz

Or you can get the installation package through the [Download Page](https://shardingsphere.apache.org/document/current/en/downloads/)

2. Decompress the proxy release package and modify the configuration file `conf/config-sharding.yaml`. Please refer to [proxy startup guide](/en/user-manual/shardingsphere-proxy/startup/bin/) for details.

3. Modify the configuration file `conf/server.yaml`. Please refer to [mode configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/mode/) for details.

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
  overwrite: false
```

4. Introduce JDBC driver.

If the backend is connected to the following databases, download the corresponding JDBC driver jar package and put it into the `${shardingsphere-proxy}/lib` directory.

| Database              | JDBC Driver                                                                                                                                                        | Reference                                                                                        |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------ |
| MySQL                 | [mysql-connector-java-5.1.47.jar]( https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar )                              | [Connector/J Versions]( https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-versions.html ) |
| openGauss             | [opengauss-jdbc-2.0.1-compatibility.jar]( https://repo1.maven.org/maven2/org/opengauss/opengauss-jdbc/2.0.1-compatibility/opengauss-jdbc-2.0.1-compatibility.jar ) |                                                                                                  |

5. Start ShardingSphere-Proxy:

```
sh bin/start.sh
```

6. View the proxy log `logs/stdout.log`. If you see the following statements:

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy start success
```

The startup will have been successful.

7. Configure and migrate on demand.

7.1. Query configuration.

```sql
SHOW MIGRATION PROCESS CONFIGURATION;
```

The default configuration is as follows.

```sql
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
| read                                                         | write                                | stream_channel                                       |
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
| {"workerThread":40,"batchSize":1000,"shardingSize":10000000} | {"workerThread":40,"batchSize":1000} | {"type":"MEMORY","props":{"block-queue-size":10000}} |
+--------------------------------------------------------------+--------------------------------------+------------------------------------------------------+
```

7.2. New configuration (Optional).

A default value is available if there is no configuration.

A completely configured DistSQL is as follows.

```sql
CREATE MIGRATION PROCESS CONFIGURATION (
READ(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  SHARDING_SIZE=10000000,
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))
),
WRITE(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='2000')))
),
STREAM_CHANNEL (TYPE(NAME='MEMORY',PROPERTIES('block-queue-size'='10000')))
);
```

Configuration item description:

```sql
CREATE MIGRATION PROCESS CONFIGURATION (
READ( -- Data reading configuration. If it is not configured, part of the parameters will take effect by default.
  WORKER_THREAD=40, -- Obtain the thread pool size of all the data from the source side. If it is not configured, the default value is used.
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
  WORKER_THREAD=40, -- The size of the thread pool on which data is written into the target side. If it is not configured, the default value is used.
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
'block-queue-size'='10000' -- Property: blocking queue size.
)))
);
```

DistSQL sample: configure `READ` for traffic limit.

```sql
CREATE MIGRATION PROCESS CONFIGURATION (
READ(
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))
)
);
```

Configure data reading for traffic limit. Other configurations use default values.

7.3. Modify configuration.

`ALTER MIGRATION PROCESS CONFIGURATION`, and its internal structure is the same as that of `CREATE MIGRATION PROCESS CONFIGURATION`.

DistSQL sample: modify traffic limit parameter

```sql
ALTER MIGRATION PROCESS CONFIGURATION (
READ(
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='1000')))
)
);
---
ALTER MIGRATION PROCESS CONFIGURATION (
READ(
  RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='1000')))
), WRITE(
  RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='1000')))
)
);
```

7.4. Clear configuration.

DistSQL sample: clear the configuration of `READ` and restore it to the default value.

```sql
DROP MIGRATION PROCESS CONFIGURATION '/READ';
```

DistSQL sample: clear the configuration of `READ/RATE_LIMITER`.

```sql
DROP MIGRATION PROCESS CONFIGURATION '/READ/RATE_LIMITER';
```
