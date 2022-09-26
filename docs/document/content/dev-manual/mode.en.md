+++
pre = "<b>5.1. </b>"
title = "Mode"
weight = 1
chapter = true
+++

## StandalonePersistRepository

### Fully-qualified class name

[`org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-standalone-mode/shardingsphere-standalone-mode-repository/shardingsphere-standalone-mode-repository-api/src/main/java/org/apache/shardingsphere/mode/repository/standalone/StandalonePersistRepository.java)

### Definition

Standalone mode configuration information persistence definition

### Implementation classes

| *Configuration Type* | *Description*        | *Fully-qualified class name* |
| -------------------- | -------------------- | ---------------------------- |
| H2                   | H2-based persistence | [`org.apache.shardingsphere.mode.repository.standalone.h2.H2Repository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-standalone-mode/shardingsphere-standalone-mode-repository/shardingsphere-standalone-mode-repository-provider/shardingsphere-standalone-mode-repository-jdbc-h2/src/main/java/org/apache/shardingsphere/mode/repository/standalone/h2/H2Repository.java) |

## ClusterPersistRepository

### Fully-qualified class name

[`org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-api/src/main/java/org/apache/shardingsphere/mode/repository/cluster/ClusterPersistRepository.java)

### Definition

Cluster mode configuration information persistence definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| ZooKeeper            | ZooKeeper-based persistence               | [`org.apache.shardingsphere.mode.repository.cluster.zookeeper.CuratorZookeeperRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-provider/shardingsphere-cluster-mode-repository-zookeeper-curator/src/main/java/org/apache/shardingsphere/mode/repository/cluster/zookeeper/CuratorZookeeperRepository.java) |
| etcd                 | Etcd-based persistence                    | [`org.apache.shardingsphere.mode.repository.cluster.etcd.EtcdRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-provider/shardingsphere-cluster-mode-repository-etcd/src/main/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepository.java) |
| Nacos                | Nacos-based persistence                   | [`org.apache.shardingsphere.mode.repository.cluster.nacos.NacosRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-provider/shardingsphere-cluster-mode-repository-nacos/src/main/java/org/apache/shardingsphere/mode/repository/cluster/nacos/NacosRepository.java) |

## GovernanceWatcher

### Fully-qualified class name

[`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/GovernanceWatcher.java)

### Definition

Governance listener definition

### Implementation classes

| *Configuration Type*                                                    | *Description*                                 | *Fully-qualified class name* |
| ----------------------------------------------------------------------- | --------------------------------------------- | ---------------------------- |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /nodes/compute_nodes      | Compute node state change listener            | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.watcher.ComputeNodeStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/compute/watcher/ComputeNodeStateChangedWatcher.java) |
| Types: ADDED, DELETED;  WatchingKeys: /lock/database/locks              | Database lock state change listener           | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.watcher.DatabaseLockChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/lock/database/watcher/DatabaseLockChangedWatcher.java) |
| Types: ADDED, DELETED; WatchingKeys: /lock/distributed/locks            | Distributed lock change listener              | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.watcher.DistributedLockChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/lock/distributed/watcher/DistributedLockChangedWatcher.java) |
| Types: UPDATED; WatchingKeys: /rules                                    | The global rule configuration change listener | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.GlobalRuleChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/config/watcher/GlobalRuleChangedWatcher.java) |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /metadata/${databaseName} | Metadata change listener                      | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.watcher.MetaDataChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/metadata/watcher/MetaDataChangedWatcher.java) |
| Types: ADDED, UPDATED; WatchingKeys: /props                             | Property change listener                      | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.PropertiesChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/config/watcher/PropertiesChangedWatcher.java) |
| Types: ADDED, UPDATED, DELETED; WatchingKeys: /nodes/storage_nodes      | Storage node state change listener            | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.watcher.StorageNodeStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/storage/watcher/StorageNodeStateChangedWatcher.java) |
