+++
pre = "<b>6.9. </b>"
title = "Mode"
weight = 9
chapter = true
+++

## StandalonePersistRepository

| *SPI 名称*                     | *详细说明*                  |
| ----------------------------- | -----------------------    |
| StandalonePersistRepository   | Standalone 模式配置信息持久化 |

| *已知实现类*                    | *详细说明*                  |
| ----------------------------- | -----------------------     |
| FileRepository                | 基于 File 的持久化            |

## ClusterPersistRepository

| *SPI 名称*                     | *详细说明*                  |
| ----------------------------- | -------------------------- |
| ClusterPersistRepository      | Cluster 模式配置信息持久化   |

| *已知实现类*                    | *详细说明*                 |
| ----------------------------- | ------------------------- |
| CuratorZookeeperRepository    | 基于 ZooKeeper 的持久化     |
| EtcdRepository                | 基于 etcd 的持久化         |

## GovernanceWatcher

| *SPI 名称*                     | *详细说明*          |
| ----------------------------- | ------------------ |
| GovernanceWatcher             | 治理监听器           |

| *已知实现类*                    | *详细说明*          |
| ----------------------------- | ------------------ |
| TerminalStateChangedWatcher   | 终端节点状态变化监听器 |
| DataSourceStateChangedWatcher | 数据源状态变化监听器   |
| LockChangedWatcher            | 锁状态变化监听器      |
| PropertiesChangedWatcher      | 属性变化监听器        |
| PrivilegeNodeChangedWatcher   | 权限变化监听器        |
| GlobalRuleChangedWatcher      | 全局规则配置变化监听器 |
| MetaDataChangedWatcher        | 元数据变化监听器      |
