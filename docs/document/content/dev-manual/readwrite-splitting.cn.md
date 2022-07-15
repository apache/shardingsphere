+++
pre = "<b>6.8. </b>"
title = "读写分离"
weight = 8
chapter = true
+++

## SPI 接口

### ReadQueryLoadBalanceAlgorithm

| *SPI 名称*                                 | *详细说明*              |
| ----------------------------------------- | ----------------------- |
| ReadQueryLoadBalanceAlgorithm             | 读库负载均衡算法           |

## 示例

### ReadQueryLoadBalanceAlgorithm

| *已知实现类*                                             | *详细说明*                                             |
|-----------------------------------------------------|----------------------------------------------------|
| RoundRobinReadQueryLoadBalanceAlgorithm             | 基于轮询的读库负载均衡算法                                      |
| RandomReadQueryLoadBalanceAlgorithm                 | 基于随机的读库负载均衡算法                                      |
| WeightReadQueryLoadBalanceAlgorithm                 | 基于权重的读库负载均衡算法                                      |
| TransactionRandomReadQueryLoadBalanceAlgorithm      | 无论是否在事务中，读请求采用随机策略路由到多个读库                          |
| TransactionRoundRobinReadQueryLoadBalanceAlgorithm  | 无论是否在事务中，读请求采用轮询策略路由到多个读库                          |
| TransactionWeightReadQueryLoadBalanceAlgorithm      | 无论是否在事务中，读请求采用权重策略路由到多个读库                          |
| FixedReplicaRandomReadQueryLoadBalanceAlgorithm     | 显示开启事务，读请求采用随机策略路由到一个固定读库；不开事务，每次读流量使用指定算法路由到不同的读库 |
| FixedReplicaRoundRobinReadQueryLoadBalanceAlgorithm | 显示开启事务，读请求采用轮询策略路由到一个固定读库；不开事务，每次读流量使用指定算法路由到不同的读库 |
| FixedReplicaWeightReadQueryLoadBalanceAlgorithm     | 显示开启事务，读请求采用权重策略路由到多个读库；不开事务，每次读流量使用指定算法路由到不同的读库   |
| FixedPrimaryReadQueryLoadBalanceAlgorithm           | 读请求全部路由到主库                                         |
