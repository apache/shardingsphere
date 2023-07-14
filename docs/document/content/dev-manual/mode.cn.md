+++
pre = "<b>5.1. </b>"
title = "运行模式"
weight = 1
chapter = true
+++

## StandalonePersistRepository

### 全限定类名

[`org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/standalone/repository/api/src/main/java/org/apache/shardingsphere/mode/repository/standalone/StandalonePersistRepository.java)

### 定义

单机模式配置信息持久化定义

### 已知实现

| *配置标识* | *详细说明*       | *全限定类名*                                                                                                                                                                                                                                                                           |
|--------|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| JDBC   | 基于 JDBC 的持久化 | [`org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/standalone/repository/provider/jdbc/src/main/java/org/apache/shardingsphere/mode/repository/standalone/jdbc/JDBCRepository.java) |

## ClusterPersistRepository

### 全限定类名

[`org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/api/src/main/java/org/apache/shardingsphere/mode/repository/cluster/ClusterPersistRepository.java)

### 定义

集群模式配置信息持久化定义

### 已知实现

| *配置标识*    | *详细说明*            | *全限定类名*                                                                                                                                                                                                                                                                                      |
|-----------|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ZooKeeper | 基于 ZooKeeper 的持久化 | [`org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/provider/zookeeper/src/main/java/org/apache/shardingsphere/mode/repository/cluster/zookeeper/ZookeeperRepository.java) |
| etcd      | 基于 Etcd 的持久化      | [`org.apache.shardingsphere.mode.repository.cluster.etcd.EtcdRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/provider/etcd/src/main/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepository.java)                          |
| Nacos     | 基于 Nacos 的持久化     | [`org.apache.shardingsphere.mode.repository.cluster.nacos.NacosRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/provider/nacos/src/main/java/org/apache/shardingsphere/mode/repository/cluster/nacos/NacosRepository.java)                     |
| Consul    | 基于 Consul 的持久化    | [`org.apache.shardingsphere.mode.repository.cluster.consul.ConsulRepository`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/repository/provider/consul/src/main/java/org/apache/shardingsphere/mode/repository/cluster/consul/ConsulRepository.java)                |

## NewGovernanceWatcher

### 全限定类名

[`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.NewGovernanceWatcher`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/NewGovernanceWatcher.java)

### 定义

治理监听器定义

### 已知实现

| *配置标识*                                                                    | *详细说明*      | *全限定类名*                                                                                                                                                                                                                                                                                                                                                 |
|---------------------------------------------------------------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /nodes/compute_nodes/status | 集群状态变化监听器   | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.cluster.watcher.ClusterStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/cluster/watcher/ClusterStateChangedWatcher.java)         |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /nodes/compute_nodes        | 计算节点状态变化监听器 | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.watcher.ComputeNodeStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/compute/watcher/ComputeNodeStateChangedWatcher.java) |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /metadata/${databaseName}   | 元数据变化监听器    | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.watcher.NewMetaDataChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/metadata/watcher/NewMetaDataChangedWatcher.java)                             |
| Types: ADDED, UPDATED; WatchingKeys: /props                               | 属性变化监听器     | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.NewPropertiesChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/config/watcher/NewPropertiesChangedWatcher.java)                             |
| Types: ADDED, UPDATED; WatchingKeys: /rules                               | 全局规则变化监听器   | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.NewGlobalRuleChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/config/watcher/NewGlobalRuleChangedWatcher.java)                             |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /sys_data                   | 系统库变化监听器    | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.ShardingSphereDataChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/data/ShardingSphereDataChangedWatcher.java)                       |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /nodes/storage_nodes        | 存储节点状态变化监听器 | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.watcher.StorageNodeStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/mode/type/cluster/core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/storage/watcher/StorageNodeStateChangedWatcher.java) |
