+++
pre = "<b>6.9. </b>"
title = "Mode"
weight = 9
chapter = true
+++

## StandalonePersistRepository

| *SPI Name*                     | *Description*                            |
| ----------------------------- | ----------------------------------------  |
| StandalonePersistRepository   | Standalone mode Configuration persistence |

| *Implementation Class*        | *Description*                             |
| ----------------------------- | ----------------------------------------- |
| FileRepository                | File persistence                          |

## ClusterPersistRepository

| *SPI Name*                       | *Description*                        |
| -------------------------------- | ------------------------------------ |
| ClusterPersistRepository         | Registry center repository           |

| *Implementation Class*           | *Description*                        |
| -------------------------------- | ------------------------------------ |
| CuratorZookeeperRepository       | ZooKeeper registry center repository |
| EtcdRepository                   | Etcd registry center repository      |

## GovernanceWatcher

| *SPI Name*                       | *Description*                 |
| -------------------------------- | ----------------------------- |
| GovernanceWatcher                | Governance watcher            |

| *Implementation Class*           | *Description*                     |
| -------------------------------- | --------------------------------- |
| StorageNodeStateChangedWatcher   | Storage node changed watcher      |
| ComputeNodeStateChangedWatcher   | Compute node changed watcher      |
| PropertiesChangedWatcher         | Properties changed watcher        |
| PrivilegeNodeChangedWatcher      | Privilege changed watcher         |
| GlobalRuleChangedWatcher         | Global rule changed watcher       |
| MetaDataChangedWatcher           | Meta data changed watcher         |
