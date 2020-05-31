+++
pre = "<b>5.8. </b>"
title = "Distributed Governance"
weight = 8
chapter = true
+++

## ConfigCenterRepository

| *SPI Name*                       | *Description*           |
| -------------------------------- | ----------------------- |
| ConfigCenterRepository           | Config center           |

| *Implementation Class*           | *Description*           |
| -------------------------------- | ----------------------- |
| CuratorZookeeperCenterRepository | ZooKeeper config center |
| EtcdCenterRepository             | Etcd config center      |
| NacosCenterRepository            | Nacos config center     |
| ApolloCenterRepository           | Apollo config center    |

## RegistryCenterRepository

| *SPI Name*                       | *Description*             |
| -------------------------------- | ------------------------- |
| RegistryCenterRepository         | Registry center           |

| *Implementation Class*           | *Description*             |
| -------------------------------- | ------------------------- |
| CuratorZookeeperCenterRepository | ZooKeeper registry center |
| EtcdCenterRepository             | Etcd registry center      |

## RootInvokeHook

| *SPI Name*                | *Description*                                  |
| ------------------------- | ---------------------------------------------- |
| RootInvokeHook            | Used to trace request root                     |

| *Implementation Class*    | *Description*                                  |
| ------------------------- | ---------------------------------------------- |
| OpenTracingRootInvokeHook | Use OpenTracing protocol to trace request root |

## MetricsTrackerManager

| *SPI Name*                      | *Description*                   |
| ------------------------------- | ------------------------------- |
| MetricsTrackerManager           | Metrics track manager           |

| *Implementation Class*          | *Description*                   |
| ------------------------------- | ------------------------------- |
| PrometheusMetricsTrackerManager | Use Prometheus to track metrics |
