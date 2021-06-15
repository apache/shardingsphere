+++
pre = "<b>3.5. </b>"
title = "弹性伸缩"
weight = 5
chapter = true
+++

## 背景

Apache ShardingSphere 提供了数据分片的能力，可以将数据分散到不同的数据库节点上，提升整体处理能力。
但对于使用单数据库运行的系统来说，如何安全简单地将数据迁移至水平分片的数据库上，一直以来都是一个迫切的需求；
同时，对于已经使用了 Apache ShardingSphere 的用户来说，随着业务规模的快速变化，也可能需要对现有的分片集群进行弹性扩容或缩容。

## 简介

ShardingSphere-Scaling 是一个提供给用户的通用数据接入迁移及弹性伸缩的解决方案。

从 **4.1.0** 开始向用户提供。

![结构总揽](https://shardingsphere.apache.org/document/current/img/scaling/scaling-overview.cn.png)

## 挑战

Apache ShardingSphere 在分片策略和算法上提供给用户极大的自由度，但却给弹性伸缩造成了极大的挑战。
如何找到一种方式，即能支持各类不同用户的分片策略和算法，又能高效地将数据节点进行伸缩，是弹性伸缩面临的第一个挑战；

同时，弹性伸缩过程中，不应该对正在运行的业务造成影响，尽可能减少伸缩时数据不可用的时间窗口，甚至做到用户完全无感知，是弹性伸缩的另一个挑战；

最后，弹性伸缩不应该对现有的数据造成影响，如何保证数据的可用性和正确性，是弹性伸缩的第三个挑战。

## 目标

支持各类用户自定义的分片策略，减少用户在数据伸缩及迁移时的重复工作及业务影响，提供一站式的通用弹性伸缩解决方案，是 Apache ShardingSphere 弹性伸缩的主要设计目标。

## 状态

当前处于 **alpha** 开发阶段。

![路线图](https://shardingsphere.apache.org/document/current/img/scaling/roadmap.cn.png)
