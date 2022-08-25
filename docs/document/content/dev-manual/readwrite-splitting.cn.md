+++
pre = "<b>5.8. </b>"
title = "读写分离"
weight = 8
chapter = true
+++

## ReadQueryLoadBalanceAlgorithm

### 全限定类名

[`org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-api/src/main/java/org/apache/shardingsphere/readwritesplitting/spi/ReadQueryLoadBalanceAlgorithm.java)

### 定义

读库负载均衡算法

### 已知实现

| *配置标识*                     | *详细说明*                                                                           | *全限定类名*                                               |
| ----------------------------- | ---------------------------------------------------------------------------------- | ---------------------------------------------------------- |
| ROUND_ROBIN                   | 基于轮询的读库负载均衡算法                                                              | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RoundRobinReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/RoundRobinReadQueryLoadBalanceAlgorithm.java) |
| RANDOM                        | 基于随机的读库负载均衡算法                                                              | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/RandomReadQueryLoadBalanceAlgorithm.java) |
| WEIGHT                        | 基于权重的读库负载均衡算法                                                              | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.WeightReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/WeightReadQueryLoadBalanceAlgorithm.java) |                                 |
| TRANSACTION_RANDOM            | 无论是否在事务中，读请求采用随机策略路由到多个读库                                           | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.TransactionRandomReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/TransactionRandomReadQueryLoadBalanceAlgorithm.java) |
| TRANSACTION_ROUND_ROBIN       | 无论是否在事务中，读请求采用轮询策略路由到多个读库                                           | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.TransactionRoundRobinReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/TransactionRoundRobinReadQueryLoadBalanceAlgorithm.java) |
| TRANSACTION_WEIGHT            | 无论是否在事务中，读请求采用权重策略路由到多个读库                                           | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.TransactionWeightReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/TransactionWeightReadQueryLoadBalanceAlgorithm.java) |
| FIXED_REPLICA_RANDOM          | 显示开启事务，读请求采用随机策略路由到一个固定读库；不开事务，每次读流量使用指定算法路由到不同的读库   | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.FixedReplicaRandomReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/FixedReplicaRandomReadQueryLoadBalanceAlgorithm.java) |
| FIXED_REPLICA_ROUND_ROBIN     | 显示开启事务，读请求采用轮询策略路由到一个固定读库；不开事务，每次读流量使用指定算法路由到不同的读库   | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.FixedReplicaRoundRobinReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/FixedReplicaRoundRobinReadQueryLoadBalanceAlgorithm.java) |
| FIXED_REPLICA_WEIGHT          | 显示开启事务，读请求采用权重策略路由到多个读库；不开事务，每次读流量使用指定算法路由到不同的读库      | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.FixedReplicaWeightReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/FixedReplicaWeightReadQueryLoadBalanceAlgorithm.java) |
| FIXED_PRIMARY                 | 读请求全部路由到主库                                                                    | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.FixedPrimaryReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-readwrite-splitting/shardingsphere-readwrite-splitting-core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/FixedPrimaryReadQueryLoadBalanceAlgorithm.java) |
