+++
title = "Heartbeat Detection"
weight = 1
+++

## Background

The ShardingSphere governance module is designed to provide a more efficient and automated cluster management feature. This depends on the state of each node in the cluster, and the real-time connection state between each node is also essential for automated cluster management.

The heartbeat detection is responsible for collecting the real-time connection state between the application and the databases to provide support for subsequent automated management and scheduling.

## Program

![Program](https://shardingsphere.apache.org/document/current/img/control-panel/cluster/heartbeat.png)

- Initialize the heartbeat detection module according to the configuration when the application starts
- The heartbeat detection module starts the heartbeat detection task, periodically obtains the database connection associated with the instance and executes heartbeat detection `SQL`
- Process the heartbeat detection result and persist it to the registry center

## Data Structure

The heartbeat detection result is persist to the `instances` node of registry center:
```
state: ONLINE     # Application instance state
sharding_db.ds_0: # logicSchemaName.dataSourceName
	state: ONLINE # DataSource state
	lastConnect:  #Last connect timestamp
sharding_db.ds_1:
	state: DISABLED
	lastConnect:	
master_slave_db.master_ds:
	state: ONLINE
	lastConnect:	
master_slave_db.slave_ds_0:
	state: ONLINE
	lastConnect:	
master_slave_db.slave_ds_1:
	state: ONLINE
	lastConnect:	
```

## Use

### Sharding-Proxy

Add the following configuration to the `server.yaml` file of ShardingSphere-Proxy:
```
cluster:
   heartbeat:
     sql: select 1 # Heartbeat detection SQL
     threadCount: 1 # Thread pool size
     interval: 60 # Heartbeat detection task interval (s)
     retryEnable: false # Whether to enable retry, if set true and detect fails, then retry until the retryMaximum is reached
     retryMaximum: 3 # Maximum number of retry, effective when retryEnable is true
     retryInterval: 3 # Retry interval (s), effective when retryEnable is true
proxy.cluster.enabled: false # Set true to start heartbeat detection, false to disable heartbeat detection
```

Since the heartbeat detection results need to be stored in the registry center, the ShardingSphere [Governance](/cn/features/governance/management/) must also be enabled.
