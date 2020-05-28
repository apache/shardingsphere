+++
pre = "<b>3.3.4. </b>"
title = "Available Config/Registry/Metadata Center"
weight = 4
+++

## SPI

[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) is a kind of API that aims to be implemented or extended by the third party. 
It can be used to realize framework extension or component replacement.

ShardingSphere uses SPI to load data to the config center/registry/metadata center and disable instances and databases. Currently, ShardingSphere supports frequently used registry centers, Zookeeper and Etcd. In addition, by injecting them to ShardingSphere with SPI, users can use other third-party config/registry/metadata centers to enable databases orchestration.

## Zookeeper

ShardingSphere adopts [Apache Curator](http://curator.apache.org/) to enable Zookeeper(support config center&registry center&metadata center). Please use Zookeeper 3.4.6 and above, see the [official website](https://zookeeper.apache.org/) for details.

## Etcd

ShardingSphere adopts [io.etcd/jetcd](https://github.com/etcd-io/jetcd) to enable Etcd(support config center&registry center&metadata center). Please use Etcd v3 and above, see the [official website](https://etcd.io/) for details.

## Apollo

ShardingSphere adopts [Apollo Client](https://github.com/ctripcorp/apollo) to enable Apollo(support config center). Please use Apollo Client 1.5.0 and above, see the [official website](https://github.com/ctripcorp/apollo) for details.

## Nacos

ShardingSphere adopts [Nacos Client](https://nacos.io/en-us/docs/sdk.html) to enable Nacos(support config center). Please use Nacos Client 1.0.0 and above, see the [official website](https://nacos.io/en-us/docs/sdk.html) for details.

## Others

Use SPI to realize relevant logic coding.
