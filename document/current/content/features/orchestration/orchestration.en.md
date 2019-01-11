+++
pre = "<b>3.3.2. </b>"
toc = true
title = "Orchestration"
weight = 2
+++

## Motivation

Registry center provides the ability to disable the access of application to database and the access to slave database. 
Data orchestration still has many functions to be developed.

## Data Structure in Registry Center

Registry center creates running node of database access object under `state` in defined name space, to distinguish different database access instances, including instances and datasources nodes.

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

Sharding-Proxy can support multiple logical data sources, so datasources sub-nodes are named in the form of `schema_name.data_source_name`.

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

It includes running instance information of database access object, with sub-node as identifiers of currently running instance, which consists of IP address and PID. 
Those identifiers are temporary nodes registered when instances are on-line and cleared when instances are off-line. 
Registry center monitors the change of those nodes to govern the database access of running instances and other things.

### state/datasource

Able to govern read-write split slave database; able to add, delete or disable data dynamically.

## Operation guide

### Circuit breaker

Write `DISABLED` (case insensitive) to @-@PID node in IP address to indicate disabling that instance; delete `DISABLED` to indicate enabling the instance.

Zookeeper command is as follow:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

Etcd command is as follow:

```
etcdctl set /your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

### Disable Slave Database

Under read-write split scenarios, users can write `DISABLED` (case insensitive) to sub-nodes of data source name to indicate disabling slave database resource. 
Delete `DISABLED` or the node to indicate enabling the instance.

Zookeeper command is as follow:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/datasources/your_slave_datasource_name DISABLED
```

Etcd command is as follow:

```
etcdctl set /your_app_name/state/datasources/your_slave_datasource_name DISABLED
```
