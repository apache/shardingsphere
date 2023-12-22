+++
title = "Build"
weight = 1
+++

The document is in Chinese and it's about the deployment and usage of ShardingSphere CDC (Change Data Capture). Here's a rough translation:

Title: "Deployment Operation"
Weight: 1

## Background Information

ShardingSphere CDC is divided into two parts, one is the CDC Server, and the other is the CDC Client. The CDC Server and ShardingSphere-Proxy are currently deployed together.

Users can introduce the CDC Client into their own projects to implement data consumption logic.

## Constraints

- Pure JAVA development, JDK recommended 1.8 or above.
- The CDC Server requires SharingSphere-Proxy to use cluster mode, currently supports ZooKeeper as the registry center.
- CDC only synchronizes data, does not synchronize table structure, and currently does not support DDL statement synchronization.
- The CDC incremental phase will output data according to the dimension of the transaction. If you want to enable XA transaction compatibility, both openGauss and ShardingSphere-Proxy need the GLT module.

## CDC Server Deployment Steps

Here, the openGauss database is used as an example to introduce the deployment steps of the CDC Server.

Since the CDC Server is built into the ShardingSphere-Proxy, you need to get the ShardingSphere-Proxy. For details, please refer to the [proxy startup manual](/cn/user-manual/shardingsphere-proxy/startup/bin/).

### Configure the GLT Module (Optional)

The official binary package does not include the GLT module by default and does not guarantee the integrity of cross-database transactions. If you are using the openGauss database with the GLT function, you can additionally introduce the GLT module to ensure the integrity of cross-database transactions.

There are currently two ways to introduce the GLT module, and you also need to make corresponding configurations in server.yaml.

#### 1. Source code compilation and installation

1. Prepare the code environment, download in advance or use Git clone to download the [ShardingSphere](https://github.com/apache/shardingsphere.git) source code from Github.
2. Delete the `<scope>provided</scope>` tag of the shardingsphere-global-clock-tso-provider-redis dependency in kernel/global-clock/type/tso/core/pom.xml and the `<scope>provided</scope>` tag of jedis in kernel/global-clock/type/tso/provider/redis/pom.xml
3. Compile ShardingSphere-Proxy, for specific compilation steps, please refer to the [ShardingSphere Compilation Manual](https://github.com/apache/shardingsphere/wiki#build-apache-shardingsphere).

#### 2. Directly introduce GLT dependencies

Can be introduced from the maven repository

1. [shardingsphere-global-clock-tso-provider-redis](https://repo1.maven.org/maven2/org/apache/shardingsphere/shardingsphere-global-clock-tso-provider-redis), download the same version as ShardingSphere-Proxy
2. [jedis-4.3.1](https://repo1.maven.org/maven2/redis/clients/jedis/4.3.1/jedis-4.3.1.jar)

### CDC Server User Manual

1. Modify the configuration file `conf/server.yaml`, turn on the CDC function. Currently, `mode` must be `Cluster`, and the corresponding registry center needs to be started in advance. If the GLT provider uses Redis, Redis needs to be started in advance.

Configuration example:

1. Enable the CDC function in `server.yaml`.

```yaml
mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: cdc_demo
      server-lists: localhost:2181
      retryIntervalMilliseconds: 500
      timeToLiveSeconds: 60
      maxRetries: 3
      operationTimeoutMilliseconds: 500

authority:
  users:
    - user: root@%
      password: root
  privilege:
    type: ALL_PERMITTED

# When using GLT, you also need to enable distributed transactions
#transaction:
#  defaultType: XA
#  providerType: Atomikos
#
#globalClock:
#  enabled: true
#  type: TSO
#  provider: redis
#  props:
#    host: 127.0.0.1
#    port: 6379

props:
  system-log-level: INFO
  check-table-metadata-enabled: false
  proxy-default-port: 3307 # Proxy default port.
  cdc-server-port: 33071 # CDC Server port, must be configured
  #proxy-frontend-database-protocol-type: openGauss # Consistent with the type of backend database
```

2. Introduce JDBC driver.

The proxy already includes the PostgreSQL JDBC driver.

If the backend connects to the following databases, please download the corresponding JDBC driver jar package and put it in the `${shardingsphere-proxy}/ext-lib` directory.

| Database | JDBC Driver |
|-----------|---------------------------------------------------------------------------------------------------------------------------------|
| MySQL | [mysql-connector-java-8.0.31.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.31/) |
| openGauss | [opengauss-jdbc-3.1.1-og.jar](https://repo1.maven.org/maven2/org/opengauss/opengauss-jdbc/3.1.1-og/opengauss-jdbc-3.1.1-og.jar) |

4. Start ShardingSphere-Proxy:

```
sh bin/start.sh
```

5. View the proxy log `logs/stdout.log`, see in the log:

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy Cluster mode started successfully
```

Confirm successful startup.

6. Configure migration as needed

6.1. Query configuration.

```sql
SHOW STREAMING RULE;
```

The default configuration is as follows:

```
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
| read                                                         | write                                | stream_channel                                        |
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
| {"workerThread":20,"batchSize":1000,"shardingSize":10000000} | {"workerThread":20,"batchSize":1000} | {"type":"MEMORY","props":{"block-queue-size":"2000"}} |
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
```

6.2. Modify configuration (optional).

Because the streaming rule has default values, no creation is required, only the ALTER statement is provided.

Complete configuration DistSQL example:

```sql
ALTER STREAMING RULE (
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
ALTER STREAMING RULE (
READ( -- Data reading configuration. If not configured, some parameters will take effect by default.
  WORKER_THREAD=20, -- The size of the thread pool for fetching full data from the source end. If not configured, the default value will be used. It needs to ensure that this value is not lower than the number of sub-libraries
  BATCH_SIZE=1000, -- The maximum number of records returned by a query operation. If not configured, the default value will be used.
  SHARDING_SIZE=10000000, -- The size of the stock data partition. If not configured, the default value will be used.
  RATE_LIMITER ( -- Rate limiting algorithm. If not configured, no rate limiting.
  TYPE( -- Algorithm type. Optional: QPS
  NAME='QPS',
  PROPERTIES( -- Algorithm properties
  'qps'='500'
  )))
),
WRITE( -- Data writing configuration. If not configured, some parameters will take effect by default.
  WORKER_THREAD=20, -- The size of the thread pool for writing data to the target end. If not configured, the default value will be used.
  BATCH_SIZE=1000, -- The maximum number of records for a batch write operation of a stock task. If not configured, the default value will be used.
  RATE_LIMITER ( -- Rate limiting algorithm. If not configured, no rate limiting.
  TYPE( -- Algorithm type. Optional: TPS
  NAME='TPS',
  PROPERTIES( -- Algorithm properties
  'tps'='2000'
  )))
),
STREAM_CHANNEL ( -- Data channel, connecting producers and consumers, used for read and write links. If not configured, the MEMORY type is used by default.
TYPE( -- Algorithm type. Optional: MEMORY
NAME='MEMORY',
PROPERTIES( -- Algorithm properties
'block-queue-size'='2000' -- Property: Blocking queue size, when the heap memory is relatively small, this value needs to be reduced.
)))
);
```

## CDC Client Manual

The CDC Client does not need to be deployed separately, just introduce the CDC Client's dependency through maven to use it in the project. Users can interact with the server through the CDC Client.

If necessary, users can also implement a CDC Client themselves to consume data and ACK.

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-data-pipeline-cdc-client</artifactId>
    <version>${version}</version>
</dependency>
```

### CDC Client Introduction

`org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient` is the entry class of the CDC Client, users can interact with the CDC Server through this class. The main new methods are as follows.

| Method Name                                                                                                                 | Return Value                                                                  | Description                                                                                                                                                                              |
|-----------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| connect(Consumer<List<Record>> dataConsumer, ExceptionHandler exceptionHandler, ServerErrorResultHandler errorResultHandler | void                                                                          | Connect to the server, when connecting, you need to specify 1. Data consumption processing logic 2. Exception handling logic during consumption 3. Server error exception handling logic |
| login(CDCLoginParameter parameter)                                                                                          | void                                                                          | CDC login CDCLoginParameter parameter - username: username - password: password                                                                                                          |
| startStreaming(StartStreamingParameter parameter)                                                                           | java.lang.String (CDC task unique identifier, used for subsequent operations) | Start CDC subscription StartStreamingParameter parameter - database: logical library name - schemaTables: subscribed table name - full: whether to subscribe to full data                |
| restartStreaming(String streamingId)                                                                                        | void                                                                          | Restart subscription                                                                                                                                                                     |
| stopStreaming(String streamingId)                                                                                           | void                                                                          | Stop subscription                                                                                                                                                                        |
| dropStreaming(String streamingId)                                                                                           | void                                                                          | Delete subscription                                                                                                                                                                      |
| await()                                                                                                                     | void                                                                          | Block the CDC thread, waiting for the channel to close                                                                                                                                   |
| close()                                                                                                                     | void                                                                          | Close the channel, the process ends.                                                                                                                                                     |
