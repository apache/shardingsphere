+++
pre = "<b>5.9. </b>"
title = "Distributed Governance"
weight = 9
chapter = true
+++

## ConfigurationRepository

| *SPI Name*                       | *Description*               |
| -------------------------------- | --------------------------- |
| ConfigurationRepository          | Config repository           |

| *Implementation Class*           | *Description*               |
| -------------------------------- | --------------------------- |
| CuratorZookeeperRepository       | ZooKeeper config repository |
| EtcdRepository                   | etcd config repository      |

## RegistryRepository

| *SPI Name*                       | *Description*                 |
| -------------------------------- | ----------------------------- |
| RegistryRepository               | Registry repository           |

| *Implementation Class*           | *Description*                 |
| -------------------------------- | ----------------------------- |
| CuratorZookeeperRepository       | ZooKeeper registry repository |
| EtcdRepository                   | etcd registry repository      |

## GovernanceListenerFactory

| *SPI Name*                       | *Description*                 |
| -------------------------------- | ----------------------------- |
| GovernanceListenerFactory        | Governance listener factory   |

| *Implementation Class*                | *Description*                              |
| ------------------------------------- | ------------------------------------------ |
| TerminalStateChangedListenerFactory   | Terminal state changed listener factory    |
| DataSourceStateChangedListenerFactory | Data source state changed listener factory |
| LockChangedListenerFactory            | Lock changed listener factory              |
| PropertiesChangedListenerFactory      | Properties changed listener factory        |
| UserChangedListenerFactory            | User changed listener factory              |
| PrivilegeNodeChangedListenerFactory   | Privilege changed listener factory         |
