+++
pre = "<b>5.1. </b>"
title = "Mode"
weight = 1
chapter = true
+++

## StandalonePersistRepository

### Fully-qualified class name

[`org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/standalone/repository/api/src/main/java/org/apache/shardingsphere/mode/repository/standalone/StandalonePersistRepository.java)

### Definition

Standalone mode configuration information persistence definition

### Implementation classes

| *Configuration Type* | *Description*          | *Fully-qualified class name*                                                                                                                                                                                                                                                 |
|----------------------|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| JDBC                 | JDBC-based persistence | [`org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/standalone/repository/provider/jdbc/src/main/java/org/apache/shardingsphere/mode/repository/standalone/jdbc/JDBCRepository.java) |

## ClusterPersistRepository

### Fully-qualified class name

[`org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/api/src/main/java/org/apache/shardingsphere/mode/repository/cluster/ClusterPersistRepository.java)

### Definition

Cluster mode configuration information persistence definition

### Implementation classes

| *Configuration Type* | *Description*               | *Fully-qualified class name*                                                                                                                                                                                                                                                                 |
|----------------------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ZooKeeper            | ZooKeeper based persistence | [`org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/provider/zookeeper/src/main/java/org/apache/shardingsphere/mode/repository/cluster/zookeeper/ZookeeperRepository.java) |
| etcd                 | Etcd based persistence      | [`org.apache.shardingsphere.mode.repository.cluster.etcd.EtcdRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/provider/etcd/src/main/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepository.java)                          |
| Nacos                | Nacos based persistence     | [`org.apache.shardingsphere.mode.repository.cluster.nacos.NacosRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/provider/nacos/src/main/java/org/apache/shardingsphere/mode/repository/cluster/nacos/NacosRepository.java)                     |
| Consul               | Consul based persistence    | [`org.apache.shardingsphere.mode.repository.cluster.consul.ConsulRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/provider/consul/src/main/java/org/apache/shardingsphere/mode/repository/cluster/consul/ConsulRepository.java)                |
