+++
pre = "<b>3.3.4. </b>"
title = "支持的配置/注册/元数据中心"
weight = 4
+++

## SPI

[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)是一种为了被第三方实现或扩展的API。它可以用于实现框架扩展或组件替换。

ShardingSphere在数据库治理模块使用SPI方式载入数据到配置中心/注册/元数据中心，进行实例熔断和数据库禁用。
目前，ShardingSphere内部支持Zookeeper和etcd这种常用的配置中心/注册中心。
此外，您可以使用其他第三方配置中心/注册/元数据中心，并通过SPI的方式注入到ShardingSphere，从而使用该配置中心/注册/元数据中心，实现数据库治理功能。

## Zookeeper

ShardingSphere官方使用[Apache Curator](http://curator.apache.org/)作为Zookeeper的实现方案（支持配置中心、注册中心和元数据中心）。
请使用Zookeeper 3.4.6及其以上版本，详情请参见[官方网站](https://zookeeper.apache.org/)。

## Etcd

ShardingSphere官方使用[io.etcd/jetcd](https://github.com/etcd-io/jetcd)作为Etcd的实现方案（支持配置中心、注册中心和元数据中心）。
请使用Etcd v3以上版本，详情请参见[官方网站](https://etcd.io/)。

## Apollo

ShardingSphere官方使用[Apollo Client](https://github.com/ctripcorp/apollo)作为Apollo的实现方案（支持配置中心）。
请使用Apollo Client 1.5.0及其以上版本，详情请参见[官方网站](https://github.com/ctripcorp/apollo)。

## Nacos

ShardingSphere官方使用[Nacos Client](https://nacos.io/zh-cn/docs/sdk.html)作为Nacos的实现方案（支持配置中心）。
请使用Nacos Client 1.0.0及其以上版本，详情请参见[官方网站](https://nacos.io/zh-cn/docs/sdk.html)。

## 其他

使用SPI方式自行实现相关逻辑编码。
