+++
pre = "<b>5.9. </b>"
title = "分布式治理"
weight = 9
chapter = true
+++

## ConfigurationRepository

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| ConfigurationRepository          | 配置中心                 |

| *已知实现类*                      | *详细说明*               |
| -------------------------------- | ----------------------- |
| CuratorZookeeperRepository       | 基于 ZooKeeper 的配置中心 |
| EtcdRepository                   | 基于 etcd 的配置中心      |

## RegistryRepository

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| RegistryRepository               | 注册中心                 |

| *已知实现类*                      | *详细说明*               |
| -------------------------------- | ----------------------- |
| CuratorZookeeperRepository       | 基于 ZooKeeper 的注册中心 |
| EtcdRepository                   | 基于 etcd 的注册中心      |
