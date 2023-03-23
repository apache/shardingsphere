+++
pre = "<b>5.8. </b>"
title = "读写分离"
weight = 8
chapter = true
+++

## ReadQueryLoadBalanceAlgorithm

### 全限定类名

[`org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/api/src/main/java/org/apache/shardingsphere/readwritesplitting/spi/ReadQueryLoadBalanceAlgorithm.java)

### 定义

读库负载均衡算法

### 已知实现

| *配置标识*                 | *详细说明*                                                                         | *全限定类名* |
| ------------------------- | -------------------------------------------------------------------------------- | ---------- |
| ROUND_ROBIN               | 基于轮询的读库负载均衡算法                                                            | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RoundRobinReplicaLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/RoundRobinReadQueryLoadBalanceAlgorithm.java) |
| RANDOM                    | 基于随机的读库负载均衡算法                                                            | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReplicaLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/RandomReadQueryLoadBalanceAlgorithm.java) |
| WEIGHT                    | 基于权重的读库负载均衡算法                                                            | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.WeightReplicaLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/WeightReadQueryLoadBalanceAlgorithm.java) |
