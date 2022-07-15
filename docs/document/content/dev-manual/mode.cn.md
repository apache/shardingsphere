+++
pre = "<b>6.1. </b>"
title = "运行模式"
weight = 1
chapter = true
+++

## SPI 接口

| *SPI 名称*                     | *详细说明*                  |
| ----------------------------- | -----------------------    |
| StandalonePersistRepository   | Standalone 模式配置信息持久化 |
| ClusterPersistRepository      | Cluster 模式配置信息持久化    |
| GovernanceWatcher             | 治理监听器                   |

## 示例

### StandalonePersistRepository

| *已知实现类*                    | *详细说明*                  |
| ----------------------------- | -------------------------- |
| FileRepository                | 基于 File 的持久化           |
| H2Repository                  | 基于 H2 的持久化             |

### ClusterPersistRepository

| *已知实现类*                    | *详细说明*                 |
| ----------------------------- | ------------------------- |
| CuratorZookeeperRepository    | 基于 ZooKeeper 的持久化     |
| EtcdRepository                | 基于 Etcd 的持久化          |

### GovernanceWatcher 

| *已知实现类*                     | *详细说明*          |
| -----------------------------  | ------------------ |
| ComputeNodeStateChangedWatcher | 计算节点状态变化监听器 |
| DatabaseLockChangedWatcher     | 数据库锁状态变化监听器 |
| DistributedLockChangedWatcher  | 分布式锁变化监听器     |
| GlobalRuleChangedWatcher       | 全局规则配置变化监听器 |
| MetaDataChangedWatcher         | 元数据变化监听器      |
| PropertiesChangedWatcher       | 属性变化监听器        |
| StorageNodeStateChangedWatcher | 存储节点状态变化监听器 |
