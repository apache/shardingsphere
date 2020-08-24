+++
title = "Registry Center"
weight = 2
+++

## Motivation

- As config center manage configuration data, registry center hold all ephemeral status data dynamically generated in runtime(such as available proxy instances, disabled datasource instances etc).

- Registry center can disable the access to slave database and the access of application. Orchestration still has many functions(such as flow control) to be developed.

## Data Structure in Registry Center

The registry center can create running node of database access object under `state` in defined namespace, to distinguish different database access instances, including `instances` and `datasources` nodes.

```
instances
    ├──your_instance_ip_a@-@your_instance_pid_x
    ├──your_instance_ip_b@-@your_instance_pid_y
    ├──....
datasources
    ├──ds0
    ├──ds1
    ├──....
```

ShardingSphere-Proxy can support multiple logical data sources, so `datasources` sub-nodes are named `schema_name.data_source_name`.

```
instances
    ├──your_instance_ip_a@-@your_instance_pid_x
    ├──your_instance_ip_b@-@your_instance_pid_y
    ├──....
datasources
    ├──sharding_db.ds0
    ├──sharding_db.ds1
    ├──....
```

### state/instances

It includes running instance information of database access object, with sub-nodes as the identifiers of currently running instance, which consist of IP and PID. Those identifiers are temporary nodes, which are registered when instances are on-line and cleared when instances are off-line. The registry center monitors the change of those nodes to govern the database access of running instances and other things.

### state/datasources

It is able to orchestrate read-write split slave database, delete or disable data dynamically.

## Operation Guide

### Circuit Breaker

Write `DISABLED` (case insensitive) to `IP@-@PID` to disable that instance; delete `DISABLED` to enable the instance.

Zookeeper command is as follows:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

### Disable Slave Database

Under read-write split scenarios, users can write `DISABLED` (case insensitive) to sub-nodes of data source name to disable slave database sources. Delete `DISABLED` or the node to enable it.

Zookeeper command is as follows:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/datasources/your_slave_datasource_name DISABLED
```
