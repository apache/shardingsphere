+++
pre = "<b>6.1. </b>"
title = "Mode"
weight = 1
chapter = true
+++

## SPI Interface

| *SPI Name*                     | *Description*                  |
| ----------------------------- | -----------------------    |
| StandalonePersistRepository   | Standalone mode configuration information persistence |
| ClusterPersistRepository      | Cluster mode configuration information persistence    |
| GovernanceWatcher             | Governance listener                   |

## Sample

### StandalonePersistRepository

| *Implementation Class*                    | *Description*                  |
| ----------------------------- | -------------------------- |
| FileRepository                | File-based persistence           |
| H2Repository                  | H2-based persistence             |

### ClusterPersistRepository

| *Implementation Class*                    | *Description*                 |
| ----------------------------- | ------------------------- |
| CuratorZookeeperRepository    | ZooKeeper-based persistence     |
| EtcdRepository                | Etcd-based persistence          |

### GovernanceWatcher

| *Implementation Class*                     | *Description*          |
| -----------------------------  | ------------------ |
| ComputeNodeStateChangedWatcher | Compute node state change listener |
| DatabaseLockChangedWatcher     | Database lock state change listener |
| DistributedLockChangedWatcher  | Distributed lock change listener     |
| GlobalRuleChangedWatcher       | The global rule configuration change listener |
| MetaDataChangedWatcher         | Metadata change listener      |
| PropertiesChangedWatcher       | Property change listener        |
| StorageNodeStateChangedWatcher | Storage node state change listener |
