+++
pre = "<b>4.9. </b>"
title = "mode模式"
weight = 9
chapter = true
+++

## 背景

为满足用户不同的需求，比如快速测试、单机运行和分布式运行。mode 提供了三种模式，它们分别是 Memory 模式、Standalone 模式、以及 Cluster 模式。

## Memory 模式

Memory 模式适用于做快速集成测试，方便开发人员在整合功能测试中集成 ShardingSphere。该模式也是 Apache ShardingSphere 的默认模式。

## Standalone 模式

Standalone 模式适用于单机环境，通过该模式可将数据源、规则等元数据进行持久化。其中 Standalone 模式中的 File 类型会将配置信息写入您所指定的 Path，
如果没有设置 Path 属性，那么 ShardingSphere 会在根目录创建 .shardingsphere 文件用来存储配置信息。

## Cluster 模式

Cluster 模式适用于分布式场景，它提供了多个计算节点之间的元数据共享和状态协调。
