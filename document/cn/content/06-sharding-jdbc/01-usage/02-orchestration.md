+++
toc = true
title = "数据库治理"
weight = 2
+++

# 操作指南

## instances节点 

可在IP地址@-@PID节点写入DISABLED（忽略大小写）表示禁用该实例，删除DISABLED表示启用。

Zookeeper命令如下：

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

Etcd命令如下：

```
etcdctl set /your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

## datasources节点 

在读写分离（或分库分表+读写分离）场景下，可在数据源名称子节点中写入DISABLED表示禁用从库数据源，删除DISABLED或节点表示启用。（2.0.0.M3及以上版本支持）。

Zookeeper命令如下：

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/datasources/your_slave_datasource_name DISABLED
```

Etcd命令如下：

```
etcdctl set /your_app_name/state/datasources/your_slave_datasource_name DISABLED
```
