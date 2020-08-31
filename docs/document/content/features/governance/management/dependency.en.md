+++
title = "Third-party Components"
weight = 4
+++

Apache ShardingSphere uses SPI to load data to the config center/registry/metadata center and disable instances and databases. 
Currently, Apache ShardingSphere supports frequently used registry centers, Zookeeper, Etcd, Apollo and Nacos. 
In addition, by injecting them to ShardingSphere with SPI, users can use other third-party config/registry/metadata centers to enable databases governance.

|                                               | *Driver*                                             | *Version* | *Config Center* | *Registry Center* | *Metadata Center* |
| --------------------------------------------- | ---------------------------------------------------- | --------- | --------------- | ----------------- | ----------------- |
| [Zookeeper](https://zookeeper.apache.org/)    | [Apache Curator](http://curator.apache.org/)         | 3.6.x     | Support         | Support           | Support           |
| [Etcd](https://etcd.io/)                      | [jetcd](https://github.com/etcd-io/jetcd)            | v3        | Support         | Support           | Support           |
| [Apollo](https://github.com/ctripcorp/apollo) | [Apollo Client](https://github.com/ctripcorp/apollo) | 1.5.0     | Support         | Not Support       | Not Support       |
| [Nacos](https://nacos.io/zh-cn/docs/sdk.html) | [Nacos Client](https://nacos.io/zh-cn/docs/sdk.html) | 1.0.0     | Support         | Not Support       | Not Support       |
