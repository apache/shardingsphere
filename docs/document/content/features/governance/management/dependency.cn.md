+++
title = "第三方组件依赖"
weight = 4
+++

Apache ShardingSphere 在数据库治理模块使用 SPI 方式载入数据到配置中心和注册中心，进行实例熔断和数据库禁用。
目前，Apache ShardingSphere 内部支持 ZooKeeper，Etcd，Apollo 和 Nacos 等常用的配置中心/注册中心。
此外，开发者可以使用其他第三方组件，并通过 SPI 的方式注入到 Apache ShardingSphere，从而使用该配置中心和注册中心，实现数据库治理功能。

|                                               | *实现驱动*                                            | *版本*  | *配置中心* | *注册中心* |
| --------------------------------------------- | ---------------------------------------------------- | ------ | ---------- | --------- |
| [Zookeeper](https://zookeeper.apache.org/)    | [Apache Curator](http://curator.apache.org/)         | 3.6.x  | 支持       | 支持       |
| [Etcd](https://etcd.io/)                      | [jetcd](https://github.com/etcd-io/jetcd)            | v3     | 支持       | 支持       |
| [Apollo](https://github.com/ctripcorp/apollo) | [Apollo Client](https://github.com/ctripcorp/apollo) | 1.5.0  | 支持       | 不支持     |
| [Nacos](https://nacos.io/zh-cn/docs/sdk.html) | [Nacos Client](https://nacos.io/zh-cn/docs/sdk.html) | 1.0.0  | 支持       | 不支持     |
