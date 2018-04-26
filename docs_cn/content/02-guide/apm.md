+++
toc = true
title = "应用性能监控"
weight = 12
prev = "/02-guide/test-framework/"
next = "/02-guide/sharding-jdbc-server/"

+++

## 简介

`APM`是应用性能监控的缩写。目前`APM`的主要功能着眼于分布式系统的性能诊断，其主要功能包括调用链展示，应用拓扑分析等。

[Sharding-JDBC](http://shardingjdbc.io)团队与[SkyWalking](http://skywalking.io)团队共同合作，推出了`Sharding-JDBC`自动探针，可以将`Sharding-JDBC`的性能数据发送到`SkyWalking`中。

## 使用方法

## 使用SkyWalking插件

请参考[SkyWalking部署手册](https://github.com/OpenSkywalking/skywalking/wiki/Quick-start-chn)。

## 使用OpenTracing插件

如果想使用其他的APM系统，且该系统支持[OpenTracing](http://opentracing.io)。可以使用[sharding-jdbc-opentracing](https://github.com/shardingjdbc/sharding-jdbc-opentracing/blob/master/README_ZH.md)插件来配合该
APM系统的使用。

*注意:使用SkyWalking的OpenTracing探针时，应将原ShardingJDBC探针插件禁用，以防止两种插件互相冲突*

## 效果展示

### 应用架构

该应用是一个`SpringBoot`应用，使用`Sharding-JDBC`访问两个数据库`ds_0`和`ds_1`，且每个数据库中有两个分表。

### 拓扑图展示

![拓扑图](http://ovfotjrsi.bkt.clouddn.com/apm-topology.png)

从图中看，虽然用户访问一次应用，但是每个数据库访问了两次。这是由于本次访问涉及到每个库中的两个分表，所以一共访问了四张表。

### 跟踪数据展示

![拓扑图](http://ovfotjrsi.bkt.clouddn.com/apm-trace.png)

从跟踪图中能够看到这四次访问。

`/SJDBC/TRUNK/*`: 表示本次SQL的总体执行性能。


![逻辑执行节点](http://ovfotjrsi.bkt.clouddn.com/apm-trunk-span.png)

`/SJSBC/BRANCH/*`: 表示具体执行的实际SQL的性能。

![实际访问节点](http://ovfotjrsi.bkt.clouddn.com/apm-branch-span.png)
