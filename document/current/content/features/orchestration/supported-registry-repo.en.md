+++
pre = "<b>3.3.3. </b>"
toc = true
title = "Available Registry Center"
weight = 3
+++

## SPI

[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) is a kind of API that aims to be implemented or extended by the third party. 
It can be used to realize framework extension or component replacement.

ShardingSphere uses SPI to load data to the registry center and disable instances and databases. Currently, ShardingSphere supports two kinds of frequently used registry centers, Zookeeper and Etcd. In addition, by injecting them to ShardingSphere with SPI, users can use other third-party registry centers to enable database orchestration.

## Zookeeper

ShardingSphere adopts [Apache Curator](http://curator.apache.org/) to enable Zookeeper. Please use Zookeeper 3.4.6 and above, see the [official website](https://zookeeper.apache.org/) for details.

## Etcd

ShardingSphere adopts [native Etcd](https://coreos.com/etcd/) to enable Etcd. Please use Etcd V3 and above, see the [official website](https://coreos.com/etcd/docs/latest) for details.

## Others

Use SPI to realize relevant logic coding.
