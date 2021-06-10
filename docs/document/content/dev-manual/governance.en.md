+++
pre = "<b>5.9. </b>"
title = "Distributed Governance"
weight = 9
chapter = true
+++

## RegistryCenterRepository

| *SPI Name*                       | *Description*                        |
| -------------------------------- | ------------------------------------ |
| RegistryCenterRepository         | Registry center repository           |

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
| TerminalStateChangedWatcher      | Terminal state changed watcher    |
| DataSourceStateChangedWatcher    | Data source state changed watcher |
| LockChangedWatcher               | Lock changed watcher              |
| PropertiesChangedWatcher         | Properties changed watcher        |
| PrivilegeNodeChangedWatcher      | Privilege changed watcher         |
| GlobalRuleChangedWatcher         | Global rule changed watcher       |