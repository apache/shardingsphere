+++
title = "Build"
weight = 1
+++

## Build&Deployment

1. Execute the following command to compile and generate the ShardingSphere-Proxy binary package:

```
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```

The binary packages:
- /shardingsphere-distribution/shardingsphere-proxy-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz

Or get binary package from [download page]( https://shardingsphere.apache.org/document/current/en/downloads/ ).

> Scaling is an experimental feature, if scaling job fail, you could try nightly version, click here to [download nightly build]( https://github.com/apache/shardingsphere#nightly-builds ).

2. Unzip the proxy distribution package, modify the configuration file `conf/config-sharding.yaml`. Please refer to [proxy startup manual](/en/user-manual/shardingsphere-proxy/startup/bin/) for more details.

3. Modify the configuration file `conf/server.yaml`. Please refer to [Mode Configuration](/en/user-manual/shardingsphere-jdbc/yaml-config/mode/) for more details.
Type of `mode` must be `Cluster` for now, please start the registry center before running proxy.

Configuration Example:
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

4. Enable scaling

Way 1. Modify `scalingName` and `scaling` configuration in `conf/config-sharding.yaml`. 

Configuration Items Explanation:
```yaml
rules:
- !SHARDING
  # ignored configuration
  
  scalingName: # Enabled scaling action config name
  scaling:
    <scaling-action-config-name> (+):
      input: # Data read configuration. If it's not configured, then part of its configuration will take effect.
        workerThread: # Worker thread pool size for inventory data ingestion from source. If it's not configured, then use system default value.
        batchSize: # Maximum records count of a DML select operation. If it's not configured, then use system default value.
        rateLimiter: # Rate limit algorithm. If it's not configured, then system will skip rate limit.
          type: # Algorithm type. Options: QPS
          props: # Algorithm properties
            qps: # QPS property. Available for types: QPS
      output: # Data write configuration. If it's not configured, then part of its configuration will take effect.
        workerThread: # Worker thread pool size for data importing to target. If it's not configured, then use system default value.
        batchSize: # Maximum records count of a DML insert/delete/update operation. If it's not configured, then use system default value.
        rateLimiter: # Rate limit algorithm. If it's not configured, then system will skip rate limit.
          type: # Algorithm type. Options: TPS
          props: # Algorithm properties
            tps: # TPS property. Available for types: TPS
      streamChannel: # Algorithm of channel that connect producer and consumer, used for input and output. If it's not configured, then system will use MEMORY type
        type: # Algorithm type. Options: MEMORY
        props: # Algorithm properties
          block-queue-size: # Property: data channel block queue size. Available for types: MEMORY
      completionDetector: # Completion detect algorithm. If it's not configured, then system won't continue to do next steps automatically.
        type: # Algorithm type. Options: IDLE
        props: # Algorithm properties
          incremental-task-idle-seconds-threshold: # If incremental tasks is idle more than so much seconds, then it could be considered as almost completed. Available for types: IDLE
      dataConsistencyChecker: # Data consistency check algorithm. If it's not configured, then system will skip this step.
        type: # Algorithm type. Options: DATA_MATCH, CRC32_MATCH
        props: # Algorithm properties
          chunk-size: # Maximum records count of a query operation for check
```

`type` of `dataConsistencyChecker` could be got by executing DistSQL `SHOW SCALING CHECK ALGORITHMS`. Simple comparison:
- `DATA_MATCH` : Support all types of databases, but it's not the best performant one.
- `CRC32_MATCH` : Support `MySQL`, performance is better than `DATA_MATCH`.

Auto Mode Configuration Example:
```yaml
rules:
- !SHARDING
  # ignored configuration
  
  scalingName: scaling_auto
  scaling:
    scaling_auto:
      input:
        workerThread: 40
        batchSize: 1000
        rateLimiter:
          type: QPS
          props:
            qps: 50
      output:
        workerThread: 40
        batchSize: 1000
        rateLimiter:
          type: TPS
          props:
            tps: 2000
      streamChannel:
        type: MEMORY
        props:
          block-queue-size: 10000
      completionDetector:
        type: IDLE
        props:
          incremental-task-idle-seconds-threshold: 1800
      dataConsistencyChecker:
        type: DATA_MATCH
        props:
          chunk-size: 1000
```

Manual Mode Configuration Example:
```yaml
rules:
- !SHARDING
  # ignored configuration
  
  scalingName: scaling_manual
  scaling:
    scaling_manual:
      input:
        workerThread: 40
        batchSize: 1000
        rateLimiter:
          type: QPS
          props:
            qps: 50
      output:
        workerThread: 40
        batchSize: 1000
        rateLimiter:
          type: TPS
          props:
            tps: 2000
      streamChannel:
        type: MEMORY
        props:
          block-queue-size: 10000
      dataConsistencyChecker:
        type: DATA_MATCH
        props:
          chunk-size: 1000
```

Way 2: Configure scaling by DistSQL

Auto Mode Configuration Example:
```sql
CREATE SHARDING SCALING RULE scaling_auto (
INPUT(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  RATE_LIMITER(TYPE(NAME=QPS, PROPERTIES("qps"=50)))
),
OUTPUT(
  WORKER_THREAD=40,
  BATCH_SIZE=1000,
  RATE_LIMITER(TYPE(NAME=TPS, PROPERTIES("tps"=2000)))
),
STREAM_CHANNEL(TYPE(NAME="MEMORY", PROPERTIES("block-queue-size"="10000"))),
COMPLETION_DETECTOR(TYPE(NAME="IDLE", PROPERTIES("incremental-task-idle-seconds-threshold"="1800"))),
DATA_CONSISTENCY_CHECKER(TYPE(NAME="DATA_MATCH", PROPERTIES("chunk-size"="1000")))
);
```

Manual Mode Configuration Example:
```sql
CREATE SHARDING SCALING RULE scaling_manual (
INPUT(
  WORKER_THREAD=40,
  BATCH_SIZE=1000
),
OUTPUT(
  WORKER_THREAD=40,
  BATCH_SIZE=1000
),
STREAM_CHANNEL(TYPE(NAME="MEMORY", PROPERTIES("block-queue-size"="10000"))),
DATA_CONSISTENCY_CHECKER(TYPE(NAME="DATA_MATCH", PROPERTIES("chunk-size"="1000")))
);
```

Please refer to [RDL#Sharding](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/) for more details.

5. Import JDBC driver dependency

If the backend database is in following table, please download JDBC driver jar and put it into `${shardingsphere-proxy}/lib` directory.

| RDBMS                 | JDBC driver                          | Reference            |
| --------------------- | ------------------------------------ | -------------------- |
| MySQL                 | [mysql-connector-java-5.1.47.jar]( https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar ) | [Connector/J Versions]( https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-versions.html ) |

6. Start up ShardingSphere-Proxy:

```
sh bin/start.sh
```

7. Check proxy log `logs/stdout.log`:

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy start success
```

It means `proxy` start up successfully.

## Shutdown

```
sh bin/stop.sh
```
