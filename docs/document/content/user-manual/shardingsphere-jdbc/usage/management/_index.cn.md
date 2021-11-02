+++
title = "管控"
weight = 4
+++

## 熔断实例

可在 `IP地址@PORT` 节点写入 `DISABLED`（忽略大小写）表示禁用该实例，删除 `DISABLED` 表示启用。

Zookeeper 命令如下：

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/status/compute_nodes/circuit_breaker/${your_instance_ip_a}@${your_instance_port_x} DISABLED
```

## 禁用从库

在读写分离场景下，可在数据源名称子节点中写入 `DISABLED`（忽略大小写）表示禁用从库数据源，删除 `DISABLED` 或节点表示启用。

Zookeeper 命令如下：

```
[zk: localhost:2181(CONNECTED) 0] set /${your_zk_namespace}/status/storage_nodes/disable/${your_schema_name.your_replica_datasource_name} DISABLED
```

## 第三方组件依赖

Apache ShardingSphere 在数据库治理模块使用 SPI 方式载入数据到配置中心和注册中心，进行实例熔断和数据库禁用。
目前，Apache ShardingSphere 内部支持 ZooKeeper，Etcd 等常用的配置中心/注册中心。
此外，开发者可以使用其他第三方组件，并通过 SPI 的方式注入到 Apache ShardingSphere，从而使用该配置中心和注册中心，实现数据库治理功能。

|                                            | *实现驱动*                                     | *版本* |
| ------------------------------------------ | -------------------------------------------- | ------ |
| [Zookeeper](https://zookeeper.apache.org/) | [Apache Curator](http://curator.apache.org/) | 3.6.x  |
| [Etcd](https://etcd.io/)                   | [jetcd](https://github.com/etcd-io/jetcd)    | v3     |
