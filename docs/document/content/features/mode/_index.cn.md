+++
pre = "<b>3.9. </b>"
title = "mode模式"
weight = 9
chapter = true
+++

## 背景

为满足用户不同的需求，比如快速测试、单机运行和分布式运行。mode提供了三种模式，它们分别是 Memory 模式、Standalone 模式、以及 Cluster 模式。

## Memory 模式

Memory 模式比较适用于做快速集成测试，方便测试、运维及开发人员做快速的整合功能测试。该模式也是 Apache ShardingSphere 的默认模式。

## Standalone 模式

Standalone 模式比较适用于单机环境下使用，通过该模式可将数据源、规则、元数据进行持久化。其中 Standalone 模式中的 File 属性会将配置信息写入您所指定的 Path，
如果没有设置 Path 属性，那么 ShardingSphere 会在根目录创建 .shardingsphere 文件用来存储配置信息。

## Cluster 模式

Cluster 模式比较适用于分布式场景下使用，Cluster 模式提供了多个实例之间共享元数据，节点状态的同步，以及通过 Dist SQL 动态的调整规则等功能。
关于分布式治理相关可[点击这里](https://shardingsphere.apache.org/document/current/cn/features/governance/)
