+++
pre = "<b>5.9. </b>"
title = "分布式治理"
weight = 9
chapter = true
+++

## RegistryCenterRepository

| *SPI 名称*                     | *详细说明*               |
| ----------------------------- | ----------------------- |
| RegistryCenterRepository      | 注册中心                 |

| *已知实现类*                    | *详细说明*               |
| ----------------------------- | ----------------------- |
| CuratorZookeeperRepository    | 基于 ZooKeeper 的注册中心 |
| EtcdRepository                | 基于 etcd 的注册中心      |

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
