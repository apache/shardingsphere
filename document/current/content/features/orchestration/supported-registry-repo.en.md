+++
pre = "<b>3.3.3. </b>"
toc = true
title = "Supported Registry Centers"
weight = 3
+++

Config center and orchestration registry center can use one of below.

## Zookeeper

Please use Zookeeper 3.4.6 and above. Please reference [official website](https://zookeeper.apache.org/).

## Etcd

Please use Etcd V3 and above. Please reference [official website](https://coreos.com/etcd/docs/latest).


## SPI
[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) is an API intended to be implemented or extended by a third party. It can be used to enable framework extension and replaceable components.

Sharding-Sphere loads the registry by using SPI for database orchestration to do circuit breaking and disable databases. Currently, Sharding-Sphere supports two popular registries, Zookeeper and Etcd. In addition, you can use other third-party registries and inject them into Sharding-Sphere via SPI for database orchestration.

When you need to use the Sharding-Sphere built-in registry implementation solutions, Maven artifactId should be added to introduce the corresponding registry implementation solution for databases orchestration.

## Zookeeper

Sharding-Sphere官方使用[Apache Curator](http://curator.apache.org/)作为Zookeeper的实现方案。请使用Zookeeper 3.4.6及其以上版本，详情请参见[官方网站](https://zookeeper.apache.org/)。

### Maven坐标

```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-orchestration-reg-zookeeper-curator</artifactId>
</dependency>
```

## Etcd

Sharding-Sphere官方使用[原生的Etcd](https://coreos.com/etcd/)作为Etcd的实现方案。请使用Etcd V3及其以上版本，详情请参见[官方网站](https://coreos.com/etcd/docs/latest)。

### Maven坐标
```xml
<dependency>
    <groupId>io.shardingsphere</groupId>
    <artifactId>sharding-orchestration-reg-etcd</artifactId>
</dependency>
```
