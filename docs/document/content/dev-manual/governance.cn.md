+++
pre = "<b>5.9. </b>"
title = "分布式治理"
weight = 9
chapter = true
+++

## RegistryCenterRepository

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| RegistryCenterRepository         | 注册中心                 |

| *已知实现类*                      | *详细说明*               |
| -------------------------------- | ----------------------- |
| CuratorZookeeperRepository       | 基于 ZooKeeper 的注册中心 |
| EtcdRepository                   | 基于 etcd 的注册中心      |

## GovernanceListenerFactory

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| GovernanceListenerFactory        | 治理监听器工厂            |

| *已知实现类*                           | *详细说明*           |
| ------------------------------------- | ------------------- |
| TerminalStateChangedListenerFactory   | 终端节点状态变化监听器 |
| DataSourceStateChangedListenerFactory | 数据源状态变化监听器   |
| LockChangedListenerFactory            | 锁状态变化监听器      |
| PropertiesChangedListenerFactory      | 属性变化监听器        |
| UserChangedListenerFactory            | 用户变化监听器        |
| PrivilegeNodeChangedListenerFactory   | 权限变化监听器        |
