+++
pre = "<b>3.3.3. </b>"
toc = true
title = "支持的注册中心"
weight = 3
+++

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
