+++
pre = "<b>3.2. </b>"
title = "运行模式"
weight = 2
chapter = true
+++

## 背景

为满足用户快速测试启动、单机运行以及集群运行等不同的需求，Apache ShardingSphere 提供了内存模式、单机模式和集群模式。

## 内存模式

适用于做快速集成测试的环境启动，方便开发人员在整合功能测试中集成 ShardingSphere。
该模式也是 Apache ShardingSphere 的默认模式。

## 单机模式

适用于单机启动 ShardingSphere，通过该模式可将数据源和规则等元数据信息持久化。
默认在根目录创建 `.shardingsphere` 文件用于存储配置信息。

## 集群模式

适用于分布式场景，它提供了多个计算节点之间的元数据共享和状态协调。
需要提供用于分布式协调的注册中心组件，如：ZooKeeper、Etcd 等。
