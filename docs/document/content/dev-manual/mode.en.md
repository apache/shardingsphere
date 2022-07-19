+++
pre = "<b>6.1. </b>"
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

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| FileRepository                | File-based persistence           | [`..`]() |
| H2                  | H2-based persistence             | [`org.apache.shardingsphere.mode.repository.standalone.h2.H2Repository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-standalone-mode/shardingsphere-standalone-mode-repository/shardingsphere-standalone-mode-repository-provider/shardingsphere-standalone-mode-repository-h2/src/main/java/org/apache/shardingsphere/mode/repository/standalone/h2/H2Repository.java) |

## ClusterPersistRepository

### Fully-qualified class name

[`org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-api/src/main/java/org/apache/shardingsphere/mode/repository/cluster/ClusterPersistRepository.java)

### Definition

Cluster mode configuration information persistence definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| ZooKeeper    | ZooKeeper-based persistence     | [`org.apache.shardingsphere.mode.repository.cluster.zookeeper.CuratorZookeeperRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-provider/shardingsphere-cluster-mode-repository-zookeeper-curator/src/main/java/org/apache/shardingsphere/mode/repository/cluster/zookeeper/CuratorZookeeperRepository.java) |
| etcd         | Etcd-based persistence          | [`org.apache.shardingsphere.mode.repository.cluster.etcd.EtcdRepository`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-repository/shardingsphere-cluster-mode-repository-provider/shardingsphere-cluster-mode-repository-etcd/src/main/java/org/apache/shardingsphere/mode/repository/cluster/etcd/EtcdRepository.java) |

## GovernanceWatcher

### Fully-qualified class name

[`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.GovernanceWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/GovernanceWatcher.java)

### Definition

Governance listener definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED) , Collections.singleton(ComputeNode.getComputeNodePath()) | Compute node state change listener | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.watcher.ComputeNodeStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/compute/watcher/ComputeNodeStateChangedWatcher.java) |
| Arrays.asList(Type.ADDED, Type.DELETED) , Collections.singleton(lockNode.getLocksNodePath())     | Database lock state change listener | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.watcher.DatabaseLockChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/lock/database/watcher/DatabaseLockChangedWatcher.java) |
| Arrays.asList(Type.ADDED, Type.DELETED) , Collections.singleton(lockNode.getLocksNodePath())  | Distributed lock change listener     | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.watcher.DistributedLockChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/lock/distributed/watcher/DistributedLockChangedWatcher.java) |
| Collections.singleton(Type.UPDATED) , Collections.singleton(GlobalNode.getGlobalRuleNode())  | The global rule configuration change listener | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.GlobalRuleChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/config/watcher/GlobalRuleChangedWatcher.java) |
| Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED) , Collections.singleton(DatabaseMetaDataNode.getMetaDataNodePath())  | Metadata change listener      | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.watcher.MetaDataChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/metadata/watcher/MetaDataChangedWatcher.java) |
| Arrays.asList(Type.UPDATED, Type.ADDED) , Collections.singleton(GlobalNode.getPropsPath()) | Property change listener        | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.watcher.PropertiesChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/config/watcher/PropertiesChangedWatcher.java) |
| Arrays.asList(Type.ADDED, Type.UPDATED, Type.DELETED) , Collections.singletonList(StorageNode.getRootPath()) | Storage node state change listener | [`org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.watcher.StorageNodeStateChangedWatcher`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-mode/shardingsphere-mode-type/shardingsphere-cluster-mode/shardingsphere-cluster-mode-core/src/main/java/org/apache/shardingsphere/mode/manager/cluster/coordinator/registry/status/storage/watcher/StorageNodeStateChangedWatcher.java) |
