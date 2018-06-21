+++
pre = "<b>3.3.2. </b>"
toc = true
title = "Orchestration"
weight = 2
+++

## Motivation

Use registry center to provide circuit breaker and disable slave databases. Data orchestration still have lots of feature need to do.

## Data structure

Registry center is defined in the namespace under `state` node, and user can create the object running node to access the database, by which you can distinguish different accessing instances. There are instances and datasources nodes.

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

### state/instances

It includes the running-instance information of database-accessing object, and its child node is the identity of the current running instance. This identify is composed of IP and PID in the running server and always a temporary node. It is registered when the instance is online, and automatically cleaned when the instance is offline. The registry manages the access to the database by monitoring changes in these nodes.

### state/datasource

It is used to manage Read-write splitting and dynamically add, remove or disable data sources.

## Operation guide

### Circuit breaker

You can write DISABLED (case ignored) to the IP address @-@pid node to disable the instance or remove DISABLED to enable.

The commands in Zookeeper:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

The commands in Etcd:

```
etcdctl set /your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

### Disable slave

In use of Read-write splitting or Sharding + Read-write splitting, you can write DISABLED to the child node in data source to disable slaves or remove DISABLED to enable (Expected in 2.0.0.M3 release).

The commands in Zookeeper:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/datasources/your_slave_datasource_name DISABLED
```

The commands in Etcd:

```
etcdctl set /your_app_name/state/datasources/your_slave_datasource_name DISABLED
```
