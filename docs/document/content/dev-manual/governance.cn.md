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
| CuratorZookeeperRepository | 基于 ZooKeeper 的配置中心 |
| EtcdRepository             | 基于 etcd 的配置中心      |
| NacosRepository            | 基于 Nacos 的配置中心     |
| ApolloRepository           | 基于 Apollo 的配置中心    |

## RegistryRepository

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| RegistryRepository               | 注册中心                 |

| *已知实现类*                      | *详细说明*               |
| -------------------------------- | ----------------------- |
| CuratorZookeeperRepository | 基于 ZooKeeper 的注册中心 |
| EtcdRepository             | 基于 etcd 的注册中心      |

## RootInvokeHook

| *SPI 名称*                 | *详细说明*                           |
| ------------------------- | ------------------------------------ |
| RootInvokeHook            | 请求调用入口追踪                       |

| *已知实现类*               | *详细说明*                            |
| ------------------------- | ------------------------------------ |
| OpenTracingRootInvokeHook | 基于 OpenTracing 协议的请求调用入口追踪 |
