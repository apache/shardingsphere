+++
title = "Management"
weight = 4
+++

## Circuit Breaker

Write `DISABLED` (case insensitive) to `IP@PORT` to disable that instance; delete `DISABLED` to enable the instance.

Zookeeper command is as follows:

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/status/compute_nodes/circuit_breaker/${your_instance_ip_a}@${your_instance_port_x} DISABLED
```

## Disable Replica Database

Under readwrite-splitting scenarios, users can write `DISABLED` (case insensitive) to sub-nodes of data source name to disable replica database sources. Delete `DISABLED` or the node to enable it.

Zookeeper command is as follows:

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/status/storage_nodes/disable/${your_schema_name.your_replica_datasource_name} DISABLED
```

## Third-party Components

Apache ShardingSphere uses SPI to load data to the config center and registry center and disable instances and databases.
Currently, Apache ShardingSphere supports frequently used registry centers, Zookeeper and Etcd.
In addition, by injecting them to ShardingSphere with SPI, users can use other third-party config and registry centers to enable databases governance.

|                                            | *Driver*                                     | *Version* |
| ------------------------------------------ | -------------------------------------------- | --------- |
| [Zookeeper](https://zookeeper.apache.org/) | [Apache Curator](http://curator.apache.org/) | 3.6.x     |
| [Etcd](https://etcd.io/)                   | [jetcd](https://github.com/etcd-io/jetcd)    | v3        |
