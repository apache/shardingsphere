+++
pre = "<b>5.1. </b>"
title = "运行模式"
weight = 1
chapter = true
+++

## StandalonePersistRepository

### 全限定类名

[`org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-standalone-mode/shardingsphere-standalone-mode-repository/shardingsphere-standalone-mode-repository-api/src/main/java/org/apache/shardingsphere/mode/repository/standalone/StandalonePersistRepository.java)

### 定义

单机模式配置信息持久化定义

### 已知实现

| *配置标识*             | *详细说明*             | *全限定类名*                   |
| -------------------- | -------------------- | ---------------------------- |
| H2                   | H2-based persistence | [`org.apache.shardingsphere.mode.repository.standalone.h2.H2Repository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-standalone-mode/shardingsphere-standalone-mode-repository/shardingsphere-standalone-mode-repository-provider/shardingsphere-standalone-mode-repository-jdbc-h2/src/main/java/org/apache/shardingsphere/mode/repository/standalone/h2/H2Repository.java) |

## ClusterPersistRepository

### 全限定类名

[`org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-api/src/main/java/org/apache/shardingsphere/mode/repository/cluster/ClusterPersistRepository.java)

### 定义

集群模式配置信息持久化定义

### 已知实现

| *配置标识*             | *详细说明*                                 | *全限定类名*                   |
| -------------------- | ----------------------------------------- | ---------------------------- |
| ZooKeeper            | 基于 ZooKeeper 的持久化                     | [`org.apache.shardingsphere.mode.repository.cluster.zookeeper.CuratorZookeeperRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-provider/shardingsphere-cluster-mode-repository-zookeeper-curator/src/main/java/org/apache/shardingsphere/mode/repository/cluster/zookeeper/CuratorZookeeperRepository.java)      |
| etcd                 | 基于 Etcd 的持久化                          | [`org.apache.shardingsphere.mode.repository.cluster.etcd.EtcdRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-provider/shardingsphere-cluster-mode-repository-etcd/src/main/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepository.java) |

## GovernanceWatcher

### 全限定类名

[`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/GovernanceWatcher.java)

### 定义

治理监听器定义

### 已知实现

| *配置标识*                                                               | *详细说明*                             | *全限定类名*                    |
| ----------------------------------------------------------------------- | --------------------------------------| ---------------------------- |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /nodes/compute_nodes      | 计算节点状态变化监听器                   | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.watcher.ComputeNodeStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/compute/watcher/ComputeNodeStateChangedWatcher.java) |
| Types: ADDED, DELETED;  WatchingKeys: /lock/database/locks              | 数据库锁状态变化监听器                   | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.watcher.DatabaseLockChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/lock/database/watcher/DatabaseLockChangedWatcher.java) |
| Types: ADDED, DELETED; WatchingKeys: /lock/distributed/locks            | 分布式锁变化监听器                       | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.watcher.DistributedLockChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/lock/distributed/watcher/DistributedLockChangedWatcher.java) |
| Types: UPDATED; WatchingKeys: /rules                                    | 全局规则配置变化监听器                   | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.GlobalRuleChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/config/watcher/GlobalRuleChangedWatcher.java) |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /metadata/${databaseName} | 元数据变化监听器                        | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.watcher.MetaDataChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/metadata/watcher/MetaDataChangedWatcher.java) |
| Types: ADDED, UPDATED; WatchingKeys: /props                             | 属性变化监听器                          | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.PropertiesChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/config/watcher/PropertiesChangedWatcher.java) |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /nodes/storage_nodes      | 存储节点状态变化监听器                    | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.watcher.StorageNodeStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/storage/watcher/StorageNodeStateChangedWatcher.java) |
