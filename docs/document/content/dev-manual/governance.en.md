+++
pre = "<b>5.9. </b>"
title = "Distributed Governance"
weight = 9
chapter = true
+++

## ConfigurationRepository

| *SPI Name*                       | *Description*           |
| -------------------------------- | ----------------------- |
| ConfigurationRepository          | Config repository           |

| *Implementation Class*           | *Description*           |
| -------------------------------- | ----------------------- |
| CuratorZookeeperRepository       | ZooKeeper config repository |
| EtcdRepository                   | etcd config repository      |
| NacosRepository                  | Nacos config repository     |
| ApolloRepository                 | Apollo config repository    |

## RegistryRepository

| *SPI Name*                       | *Description*             |
| -------------------------------- | ------------------------- |
| RegistryRepository               | Registry repository           |

| *Implementation Class*           | *Description*             |
| -------------------------------- | ------------------------- |
| CuratorZookeeperRepository | ZooKeeper registry repository |
| EtcdRepository             | etcd registry repository      |

## RootInvokeHook

| *SPI Name*                | *Description*                                  |
| ------------------------- | ---------------------------------------------- |
| RootInvokeHook            | Used to trace request root                     |

| *Implementation Class*    | *Description*                                  |
| ------------------------- | ---------------------------------------------- |
| OpenTracingRootInvokeHook | Use OpenTracing protocol to trace request root |

