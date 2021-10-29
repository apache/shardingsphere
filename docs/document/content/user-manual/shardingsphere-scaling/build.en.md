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

2. Unzip the proxy distribution package, modify the configuration file `conf/server.yaml`, enable `scaling` and `mode`:
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

Enable `clusterAutoSwitchAlgorithm` indicate system will detect when scaling job is finished and switch cluster configuration automatically. Currently, system supply `IDLE` type implementation.

Enable `dataConsistencyCheckAlgorithm` indicate system will use this defined algorithm to do data consistency check when it's emitted, if it's disabled, then data consistency check will be ignored. Currently, system supply `DEFAULT` type implementation, it supports following database types: `MySQL`, you could not enable it if you're running other database types for now, support of other database types is under development.

You could customize an auto switch algorithm by implementing `ScalingClusterAutoSwitchAlgorithm` SPI interface, and customize a check algorithm by implementing `ScalingDataConsistencyCheckAlgorithm` SPI interface. Please refer to [Dev Manual#Scaling](/en/dev-manual/scaling/) for more details.

3. Start up ShardingSphere-Proxy:

```
sh bin/start.sh
```

4. See the proxy log file `logs/stdout.log`，ensure startup successfully.

## Shutdown

```
sh bin/stop.sh
```

## Configuration

The existing configuration items are as follows, we can modify them in `conf/server.yaml`:

| Name           | Description                                                                               | Default value |
| -------------- | ----------------------------------------------------------------------------------------- | ------------- |
| blockQueueSize | Queue size of data transmission channel                                                   | 10000         |
| workerThread   | Worker thread pool size, the number of migration task threads allowed to run concurrently | 40            |
