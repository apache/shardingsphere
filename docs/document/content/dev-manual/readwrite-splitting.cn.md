+++
pre = "<b>6.8. </b>"
title = "读写分离"
weight = 8
chapter = true
+++

## SPI 接口

## ReadQueryLoadBalanceAlgorithm

| *SPI 名称*                                 | *详细说明*              |
| ----------------------------------------- | ----------------------- |
| ReadQueryLoadBalanceAlgorithm             | 读库负载均衡算法           |

## 示例

## ReadQueryLoadBalanceAlgorithm

| *已知实现类*                               | *详细说明*               |
| ----------------------------------------- | ----------------------- |
| RoundRobinReplicaLoadBalanceAlgorithm     | 基于轮询的读库负载均衡算法 |
| RandomReplicaLoadBalanceAlgorithm         | 基于随机的读库负载均衡算法 |
| WeightReplicaLoadBalanceAlgorithm         | 基于权重的读库负载均衡算法 |
