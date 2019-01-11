+++
pre = "<b>3.3.3. </b>"
toc = true
title = "Available Registry Center"
weight = 3
+++

## SPI

[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) is a kind of API that aims to be realized or extended by the third party. 
It can be used to realize framework extension or component replacement.

ShardingSphere uses SPI to load data to the registry center and disable instances and databases. 
Currently, ShardingSphere supports two kinds of common registry centers in it, Zookeeper and Etcd. 
In addition, by injecting them to ShardingSphere with SPI, users can use other third-party registry centers to realize database orchestration function.

## Zookeeper

ShardingSphere adopts [Apache Curator](http://curator.apache.org/) as the official realization solution of Zookeeper. 
Please use Zookeeper 3.4.6 and above, see the [official website](https://zookeeper.apache.org/) for details.

## Etcd

ShardingSphere uses natived [Etcd](https://coreos.com/etcd/) as the official realization solution of Etcd. 
Please use Etcd V3 and above, see the [official website](https://coreos.com/etcd/docs/latest) for details.

## Others

Use SPI to realize relevant logic coding in its own.
