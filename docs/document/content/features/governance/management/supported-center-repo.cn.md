+++
pre = "<b>3.4.1.4. </b>"
title = "第三方组件依赖"
weight = 4
+++

Apache ShardingSphere 在数据库治理模块使用 SPI 方式载入数据到配置中心/注册/元数据中心，进行实例熔断和数据库禁用。
目前，Apache ShardingSphere 内部支持 ZooKeeper，Etcd，Apollo 和 Nacos 等常用的配置中心/注册中心。
此外，您可以使用其他第三方配置中心/注册/元数据中心，并通过 SPI 的方式注入到 Apache ShardingSphere，从而使用该配置中心/注册/元数据中心，实现数据库治理功能。

## ZooKeeper

Apache ShardingSphere 官方使用 [Apache Curator](http://curator.apache.org/) 作为 ZooKeeper 的实现方案（支持配置中心、注册中心和元数据中心）。
请使用 ZooKeeper 3.4.6 及其以上版本，详情请参见[官方网站](https://zookeeper.apache.org/)。

## Etcd

ShardingSphere官方使用 [io.etcd/jetcd](https://github.com/etcd-io/jetcd) 作为 Etcd 的实现方案（支持配置中心、注册中心和元数据中心）。
请使用 Etcd v3 以上版本，详情请参见[官方网站](https://etcd.io/)。

## Apollo

ShardingSphere官方使用 [Apollo Client](https://github.com/ctripcorp/apollo) 作为 Apollo 的实现方案（支持配置中心）。
请使用 Apollo Client 1.5.0 及其以上版本，详情请参见[官方网站](https://github.com/ctripcorp/apollo)。

## Nacos

ShardingSphere官方使用 [Nacos Client](https://nacos.io/zh-cn/docs/sdk.html) 作为 Nacos 的实现方案（支持配置中心）。
请使用 Nacos Client 1.0.0 及其以上版本，详情请参见[官方网站](https://nacos.io/zh-cn/docs/sdk.html)。

## 其他

使用SPI方式自行实现相关逻辑编码。
