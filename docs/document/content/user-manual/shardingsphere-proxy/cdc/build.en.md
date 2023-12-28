+++
title = "Build"
weight = 1
+++

## Background Information

ShardingSphere CDC is divided into two parts, one is the CDC Server, and the other is the CDC Client. The CDC Server and ShardingSphere-Proxy are currently deployed together.

Users can introduce the CDC Client into their own projects to implement data consumption logic.

## Constraints

- Pure JAVA development, JDK recommended 1.8 or above.
- CDC Server requires SharingSphere-Proxy to use cluster mode, currently supports ZooKeeper as the registry center.
- CDC only synchronizes data, does not synchronize table structure, and currently does not support DDL statement synchronization.
- CDC incremental stage will output data according to the dimension of the transaction. If you want to enable XA transaction compatibility, both openGauss and ShardingSphere-Proxy need the GLT module.

## CDC Server Deployment Steps

Here, the openGauss database is used as an example to introduce the deployment steps of the CDC Server.

Since the CDC Server is built into ShardingSphere-Proxy, you need to get ShardingSphere-Proxy. For details, please refer to the [proxy startup manual](/cn/user-manual/shardingsphere-proxy/startup/bin/).

### Configure GLT Module (Optional)

The official website's released binary package does not include the GLT module by default and does not guarantee the integrity of cross-library transactions. If you are using the openGauss database with GLT functionality, you can additionally introduce the GLT module to ensure the integrity of cross-library transactions.

There are currently two ways to introduce the GLT module, and corresponding configurations need to be made in server.yaml.

#### 1. Source code compilation and installation

1.1 Prepare the code environment, download in advance or use Git clone to download the [ShardingSphere](https://github.com/apache/shardingsphere.git) source code from Github.

1.2 Delete the `<scope>provided</scope>` tag of the shardingsphere-global-clock-tso-provider-redis dependency in kernel/global-clock/type/tso/core/pom.xml and the `<scope>provided</scope>` tag of jedis in kernel/global-clock/type/tso/provider/redis/pom.xml

1.3 Compile ShardingSphere-Proxy, for specific compilation steps, please refer to the [ShardingSphere Compilation Manual](https://github.com/apache/shardingsphere/wiki#build-apache-shardingsphere).

#### 2. Directly introduce GLT dependencies

Can be introduced from the maven repository

2.1. [shardingsphere-global-clock-tso-provider-redis](https://repo1.maven.org/maven2/org/apache/shardingsphere/shardingsphere-global-clock-tso-provider-redis), download the same version as ShardingSphere-Proxy

2.2. [jedis-4.3.1](https://repo1.maven.org/maven2/redis/clients/jedis/4.3.1/jedis-4.3.1.jar)

### CDC Server User Manual

1. Modify the configuration file `conf/server.yaml` and turn on the CDC function. Currently, `mode` must be `Cluster`, and the corresponding registry center needs to be started in advance. If the GLT provider uses Redis, Redis needs to be started in advance.

Configuration example:

1. Enable CDC function in `server.yaml`.

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

Proxy already includes PostgreSQL JDBC driver.

If the backend connects to the following databases, please download the corresponding JDBC driver jar package and put it in the `${shardingsphere-proxy}/ext-lib` directory.

| Database   | JDBC Driver                                                                                                                         |
|-----------|---------------------------------------------------------------------------------------------------------------------------------|
| MySQL     | [mysql-connector-java-8.0.31.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.31/)                            |
| openGauss | [opengauss-jdbc-3.1.1-og.jar](https://repo1.maven.org/maven2/org/opengauss/opengauss-jdbc/3.1.1-og/opengauss-jdbc-3.1.1-og.jar) |

4. Start ShardingSphere-Proxy:

```
sh bin/start.sh
```

5. View the proxy log `logs/stdout.log`, and see in the log:

```
[INFO ] [main] o.a.s.p.frontend.ShardingSphereProxy - ShardingSphere-Proxy Cluster mode started successfully
```

Confirm successful startup.

6. Configure CDC task synchronization configuration as needed

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

Because the streaming rule has a default value, no creation is required, only the ALTER statement is provided.

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
  WORKER_THREAD=20, -- Affects full and incremental tasks, the size of the thread pool for fetching data from the source end. If not configured, the default value will be used. It needs to ensure that this value is not lower than the number of sub-libraries
  BATCH_SIZE=1000, -- Affects full and incremental tasks, the maximum number of records returned by a query operation. If the amount of data in a transaction is greater than this value, the incremental situation may exceed the set value.
  SHARDING_SIZE=10000000, -- Affects full tasks, the size of stock data sharding. If not configured, the default value will be used.
  RATE_LIMITER ( -- Affects full and incremental tasks, rate limiting algorithm. If not configured, no rate limiting.
  TYPE( -- Algorithm type. Optional: QPS
  NAME='QPS',
  PROPERTIES( -- Algorithm properties
  'qps'='500'
  )))
),
WRITE( -- Data writing configuration. If not configured, some parameters will take effect by default.
  WORKER_THREAD=20, -- Affects full and incremental tasks, the size of the thread pool for writing data to the target end. If not configured, the default value will be used.
  BATCH_SIZE=1000, -- Affects full and incremental tasks, the maximum number of records for a batch write operation in a stock task. If not configured, the default value will be used. If the amount of data in a transaction is greater than this value, the incremental situation may exceed the set value.
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
'block-queue-size'='2000' -- Property: Blocking queue size
)))
);
```

## CDC Client Manual

The CDC Client does not need to be deployed separately, just need to introduce the dependency of the CDC Client through maven to use it in the project. Users can interact with the server through the CDC Client.

If necessary, users can also implement a CDC Client themselves to consume data and ACK.

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-data-pipeline-cdc-client</artifactId>
    <version>${version}</version>
</dependency>
```

### CDC Client Introduction

`org.apache.shardingsphere.data.pipeline.cdc.client.CDCClient` is the entry class of the CDC Client. Users can interact with the CDC Server through this class. The main new methods are as follows.

| Method Name                                                                                                                         | Return Value                                | Description                                                                                                      |
|-----------------------------------------------------------------------------------------------------------------------------|------------------------------------|---------------------------------------------------------------------------------------------------------|
| connect(Consumer<List<Record>> dataConsumer, ExceptionHandler exceptionHandler, ServerErrorResultHandler errorResultHandler | void                               | Connect with the server, when connecting, you need to specify <br/>1. Data consumption processing logic <br/>2. Exception handling logic during consumption <br/>3. Server error exception handling logic                           |
| login(CDCLoginParameter parameter)                                                                                          | void                               | CDC login, parameters <br/>username: username <br/>password: password                                                             |
| startStreaming(StartStreamingParameter parameter)                                                                           | String (CDC task unique identifier, used for subsequent operations) | Start CDC subscription, StartStreamingParameter parameters <br/> database: logical library name <br/> schemaTables: subscribed table name <br/> full: whether to subscribe to full data |
| restartStreaming(String streamingId)                                                                                        | void                               | Restart subscription                                                                                                    |
| stopStreaming(String streamingId)                                                                                           | void                               | Stop subscription                                                                                                    |
| dropStreaming(String streamingId)                                                                                           | void                               | Delete subscription                                                                                                    |
| await()                                                                                                                     | void                               | Block the CDC thread and wait for the channel to close                                                                                 |
| close()                                                                                                                     | void                               | Close the channel, the process ends.                                                                                        |

```