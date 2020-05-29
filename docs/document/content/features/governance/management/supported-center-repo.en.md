+++
pre = "<b>3.4.1.4. </b>"
title = "Third-party Components Dependency"
weight = 4
+++

## SPI

Apache ShardingSphere uses SPI to load data to the config center/registry/metadata center and disable instances and databases. 
Currently, Apache ShardingSphere supports frequently used registry centers, Zookeeper, Etcd, Apollo and Nacos. 
In addition, by injecting them to ShardingSphere with SPI, users can use other third-party config/registry/metadata centers to enable databases orchestration.

## Zookeeper

ShardingSphere adopts [Apache Curator](http://curator.apache.org/) to enable Zookeeper(support config center&registry center&metadata center). 
Please use Zookeeper 3.4.6 and above, see the [official website](https://zookeeper.apache.org/) for details.

## Etcd

ShardingSphere adopts [io.etcd/jetcd](https://github.com/etcd-io/jetcd) to enable Etcd(support config center&registry center&metadata center). 
Please use Etcd v3 and above, see the [official website](https://etcd.io/) for details.

## Apollo

ShardingSphere adopts [Apollo Client](https://github.com/ctripcorp/apollo) to enable Apollo(support config center). 
Please use Apollo Client 1.5.0 and above, see the [official website](https://github.com/ctripcorp/apollo) for details.

## Nacos

ShardingSphere adopts [Nacos Client](https://nacos.io/en-us/docs/sdk.html) to enable Nacos(support config center). 
Please use Nacos Client 1.0.0 and above, see the [official website](https://nacos.io/en-us/docs/sdk.html) for details.

## Others

Use SPI to realize relevant logic coding.
