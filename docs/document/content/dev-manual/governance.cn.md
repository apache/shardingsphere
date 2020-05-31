+++
pre = "<b>5.8. </b>"
title = "分布式治理"
weight = 8
chapter = true
+++

## ConfigCenterRepository

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| ConfigCenterRepository           | 配置中心                 |

| *已知实现类*                      | *详细说明*               |
| -------------------------------- | ----------------------- |
| CuratorZookeeperCenterRepository | 基于 ZooKeeper 的配置中心 |
| EtcdCenterRepository             | 基于 Etcd 的配置中心      |
| NacosCenterRepository            | 基于 Nacos 的配置中心     |
| ApolloCenterRepository           | 基于 Apollo 的配置中心    |

## RegistryCenterRepository

| *SPI 名称*                       | *详细说明*               |
| -------------------------------- | ----------------------- |
| RegistryCenterRepository         | 注册中心                 |

| *已知实现类*                      | *详细说明*               |
| -------------------------------- | ----------------------- |
| CuratorZookeeperCenterRepository | 基于 ZooKeeper 的注册中心 |
| EtcdCenterRepository             | 基于 Etcd 的注册中心      |

## RootInvokeHook

| *SPI 名称*                 | *详细说明*                           |
| ------------------------- | ------------------------------------ |
| RootInvokeHook            | 请求调用入口追踪                       |

| *已知实现类*               | *详细说明*                            |
| ------------------------- | ------------------------------------ |
| OpenTracingRootInvokeHook | 基于 OpenTracing 协议的请求调用入口追踪 |

## MetricsTrackerManager

| *SPI 名称*                      | *详细说明*                    |
| ------------------------------- | --------------------------- |
| MetricsTrackerManager           | 度量指标追踪                  |

| *已知实现类*                     | *详细说明*                    |
| ------------------------------- | ---------------------------- |
| PrometheusMetricsTrackerManager | 基于 Prometheus 的度量指标追踪 |
