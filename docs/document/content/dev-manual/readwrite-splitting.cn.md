+++
pre = "<b>6.8. </b>"
title = "读写分离"
weight = 8
chapter = true
+++

## ReplicaLoadBalanceAlgorithm

| *SPI 名称*                                 | *详细说明*              |
| ----------------------------------------- | ----------------------- |
| ReplicaLoadBalanceAlgorithm               | 读库负载均衡算法          |

| *已知实现类*                               | *详细说明*               |
| ----------------------------------------- | ----------------------- |
| RoundRobinReplicaLoadBalanceAlgorithm     | 基于轮询的读库负载均衡算法 |
| RandomReplicaLoadBalanceAlgorithm         | 基于随机的读库负载均衡算法 |
| WeightReplicaLoadBalanceAlgorithm         | 基于权重的读库负载均衡算法 |
