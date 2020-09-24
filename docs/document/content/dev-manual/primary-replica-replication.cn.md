+++
pre = "<b>5.5. </b>"
title = "读写分离"
weight = 5
chapter = true
+++

## MasterSlaveLoadBalanceAlgorithm

| *SPI 名称*                                 | *详细说明*              |
| ----------------------------------------- | ----------------------- |
| MasterSlaveLoadBalanceAlgorithm           | 读库负载均衡算法          |

| *已知实现类*                               | *详细说明*               |
| ----------------------------------------- | ----------------------- |
| RoundRobinMasterSlaveLoadBalanceAlgorithm | 基于轮询的读库负载均衡算法 |
| RandomMasterSlaveLoadBalanceAlgorithm     | 基于随机的读库负载均衡算法 |
