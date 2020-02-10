+++
pre = "<b>3.3.3. </b>"
toc = true
title = "支持的注册中心"
weight = 3
+++

## SPI

[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)是一种为了被第三方实现或扩展的API。它可以用于实现框架扩展或组件替换。

ShardingSphere在数据库治理模块使用SPI方式载入注册中心，进行实例熔断和数据库禁用。
目前，ShardingSphere内部支持Zookeeper这种常用的注册中心。
此外，您可以使用其他第三方注册中心，并通过SPI的方式注入到ShardingSphere，从而使用该注册中心，实现数据库治理功能。

## Zookeeper

ShardingSphere官方使用[Apache Curator](http://curator.apache.org/)作为Zookeeper的实现方案。
请使用Zookeeper 3.4.6及其以上版本，详情请参见[官方网站](https://zookeeper.apache.org/)。

## Nacos

ShardingSphere官方使用[Nacos Client](https://nacos.io/zh-cn/docs/sdk.html)作为Nacos的实现方案。
请使用Nacos Client 1.0.0及其以上版本，详情请参见[官方网站](https://nacos.io/zh-cn/docs/sdk.html)。

## 其他

使用SPI方式自行实现相关逻辑编码。
