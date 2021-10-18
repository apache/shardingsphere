+++
pre = "<b>4.4.1. </b>"
title = "Build"
weight = 1
+++

## Build&Deployment

1. Execute the following command to compile and generate the ShardingSphere-Scaling and ShardingSphere-Proxy binary package:

```
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```

The binary packages:
- /shardingsphere-distribution/shardingsphere-scaling-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-scaling-bin.tar.gz
- /shardingsphere-distribution/shardingsphere-proxy-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz

Or get binary package from [download page]( https://shardingsphere.apache.org/document/current/en/downloads/ ).

2. Unzip the scaling distribution package, modify the configuration file `conf/server.yaml`, we should ensure the port does not conflict with others, and modify the resume from break-point(optional) server lists:

```yaml
scaling:
  port: 8888
  blockQueueSize: 10000
  workerThread: 30

mode:
  type: Cluster
  repository:
    type: ZooKeeper
    props:
      namespace: governance_ds
      server-lists: localhost:2181
```

3. Start up ShardingSphere-Scaling:

```
sh bin/server_start.sh
```

4. See the scaling log file `logs/stdout.log`，ensure startup successfully.

5. Ensure scaling startup successfully by `curl`.

```
curl -X GET http://localhost:8888/scaling/job/list
```

response:

```
{"success":true,"errorCode":0,"errorMsg":null,"model":[]}
```

6. Unzip the proxy distribution package, modify the configuration file `conf/server.yaml`, enable `scaling` and `mode`:
```yaml
scaling:
  blockQueueSize: 10000
  workerThread: 40
  clusterAutoSwitchAlgorithm:
    type: IDLE
    props:
      incremental-task-idle-minute-threshold: 30
  dataConsistencyCheckAlgorithm:
    type: DEFAULT

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

7. Start up ShardingSphere-Proxy:

```
sh bin/start.sh
```

8. See the proxy log file `logs/stdout.log`，ensure startup successfully.

## Shutdown

### ShardingSphere-Scaling

```
sh bin/server_stop.sh
```

### ShardingSphere-Proxy

```
sh bin/stop.sh
```

## Configuration

### ShardingSphere-Scaling

The existing configuration items are as follows, we can modify them in `conf/server.yaml`:

| Name           | Description                                                                               | Default value |
| -------------- | ----------------------------------------------------------------------------------------- | ------------- |
| port           | Listening port of HTTP server                                                             | 8888          |
| blockQueueSize | Queue size of data transmission channel                                                   | 10000         |
| workerThread   | Worker thread pool size, the number of migration task threads allowed to run concurrently | 30            |
