+++
pre = "<b>3.4. </b>"
title = "高可用"
weight = 4
chapter = true
+++

## 定义

高可用是现代系统的最基本诉求，作为系统基石的数据库，对于高可用的要求也是必不可少的。

在存算分离的分布式数据库体系中，存储节点和计算节点的高可用方案是不同的。
对于有状态的存储节点来说，需要其自身具备数据一致性同步、探活、主节点选举等能力；
对于无状态的计算节点来说，需要感知存储节点的变化的同时，还需要独立架设负载均衡器，并具备服务发现和请求分发的能力。

尽可能的保证 7X24 小时不间断的数据库服务，是 Apache ShardingSphere 高可用模块的主要设计目标。

## 原理介绍

Apache ShardingSphere 提供的高可用方案，允许用户进行二次定制开发及实现扩展，主要分为四个步骤 : 前置检查、动态发现主库、动态发现从库、同步配置。

![概述](https://shardingsphere.apache.org/document/current/img/discovery/overview.cn.png)

## 相关参考

[Java API](/cn/user-manual/shardingsphere-jdbc/java-api/rules/ha)\
[YAML 配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/ha)\
[Spring Boot Starter](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha)\
[Spring 命名空间](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha)
