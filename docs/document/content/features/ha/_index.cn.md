+++
pre = "<b>3.4. </b>"
title = "高可用"
weight = 4
chapter = true
+++

## 背景

高可用是现代系统的最基本诉求，作为系统基石的数据库，对于高可用的要求也是必不可少的。

在存算分离的分布式数据库体系中，存储节点和计算节点的高可用方案是不同的。 对于有状态的存储节点来说，需要其自身具备数据一致性同步、探活、主节点选举等能力； 对于无状态的计算节点来说，需要感知存储节点的变化的同时，还需要独立架设负载均衡器，并具备服务发现和请求分发的能力。

Apache ShardingSphere 自身提供计算节点，并通过数据库作为存储节点。 因此，它采用的高可用方案是利用数据库自身的高可用方案做存储节点高可用，并自动识别其变化。

## 挑战

Apache ShardingSphere 需要自动感知多样化的存储节点高可用方案的同时，也能够动态集成对读写分离方案，是实现的主要挑战。

![概述](https://shardingsphere.apache.org/document/current/img/discovery/overview.cn.png)

## 目标

尽可能的保证 7*24 小时不间断的数据库服务，是 Apache ShardingSphere 高可用模块的主要设计目标。

## 应用场景

在大多数情况下，高可用搭配读写分离功能一起使用。当用户写库或读库关系发生变化时，ShardingSphere 可动态的感知并纠正内部的主从关系，进而保证读流量和写流量的正确路由。同时当从库宕机时，ShardingSphere 也可动态纠正存储节点的状态，保证读流量分发正确。

## 相关参考

[Java API](/cn/user-manual/shardingsphere-jdbc/java-api/rules/ha)\
[YAML 配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/ha)\
[Spring Boot Starter](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha)\
[Spring 命名空间](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha)
