+++
toc = true
title = "Orchestration"
weight = 2
+++

# Operation guide

## The instance node

You can write DISABLED (case ignored) to the IP address @-@pid node to disable the instance or remove DISABLED to enable.

The commands in Zookeeper:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

The commands in Etcd:

```
etcdctl set /your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

## The data source node

In use of Read-write splitting or Sharding + Read-write splitting, you can write DISABLED to the child node in data source to disable slaves or remove DISABLED to enable (Expected in 2.0.0.M3 release).

The commands in Zookeeper:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/datasources/your_slave_datasource_name DISABLED
```

The commands in Etcd:

```
etcdctl set /your_app_name/state/datasources/your_slave_datasource_name DISABLED
```
