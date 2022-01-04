+++
pre = "<b>6.1. </b>"
title = "运行模式"
weight = 1
chapter = true
+++

## StandalonePersistRepository

| *SPI 名称*                     | *详细说明*                  |
| ----------------------------- | -----------------------    |
| StandalonePersistRepository   | Standalone 模式配置信息持久化 |

| *已知实现类*                    | *详细说明*                  |
| ----------------------------- | -------------------------- |
| FileRepository                | 基于 File 的持久化           |

## ClusterPersistRepository

| *SPI 名称*                     | *详细说明*                  |
| ----------------------------- | -------------------------- |
| ClusterPersistRepository      | Cluster 模式配置信息持久化    |

| *已知实现类*                    | *详细说明*                 |
| ----------------------------- | ------------------------- |
| CuratorZookeeperRepository    | 基于 ZooKeeper 的持久化     |
| EtcdRepository                | 基于 etcd 的持久化          |

## GovernanceWatcher

| *SPI 名称*                     | *详细说明*          |
| ----------------------------- | ------------------ |
| GovernanceWatcher             | 治理监听器           |

| *已知实现类*                     | *详细说明*          |
| -----------------------------  | ------------------ |
| StorageNodeStateChangedWatcher | 存储节点状态变化监听器 |
| ComputeNodeStateChangedWatcher | 计算节点状态变化监听器 |
| PropertiesChangedWatcher       | 属性变化监听器        |
| PrivilegeNodeChangedWatcher    | 权限变化监听器        |
| GlobalRuleChangedWatcher       | 全局规则配置变化监听器 |
| MetaDataChangedWatcher         | 元数据变化监听器      |
