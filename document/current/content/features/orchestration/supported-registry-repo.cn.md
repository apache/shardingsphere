+++
pre = "<b>3.3.3. </b>"
toc = true
title = "支持的注册中心"
weight = 3
+++

## SPI
[Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)是一种为了被第三方实现或扩展的API。它可以用于实现框架扩展或组件替换。

Sharding-Sphere在数据库治理模块使用SPI方式载入注册中心，进行实例熔断和数据库禁用。目前，Sharding-Sphere内部支持Zookeeper和Etcd两种常用的注册中心。此外，您可以使用其他第三方注册中心，并通过SPI的方式注入到Sharding-Sphere，从而使用该注册中心，实现数据库治理功能。

当您需要使用Sharding-Sphere内置的注册中心实现方案时，需要加入对应的Maven坐标，以引入对应的注册中心实现方案，达到数据库治理的目的。

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
