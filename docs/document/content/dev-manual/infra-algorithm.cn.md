+++
pre = "<b>5.4. </b>"
title = "基础算法"
weight = 4
chapter = true
+++

## LoadBalanceAlgorithm

### 全限定类名

[`org.apache.shardingsphere.infra.algorithm.loadbalancer.core.LoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/load-balancer/core/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/core/LoadBalanceAlgorithm.java)

### 定义

负载均衡算法，可以使用在读写分离、JDBC 双路由等功能中。

### 已知实现

| *配置标识*      | *详细说明*        | *全限定类名*                                                                                                                                                                                                                                                                                                                              |
|-------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ROUND_ROBIN | 基于轮询的负载均衡算法 | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.round.robin.RoundRobinLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/load-balancer/type/round-robin/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/round/robin/RoundRobinLoadBalanceAlgorithm.java) |
| RANDOM      | 基于随机的负载均衡算法 | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.random.RandomLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/load-balancer/type/random/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/random/RandomLoadBalanceAlgorithm.java)         |
| WEIGHT      | 基于权重的负载均衡算法 | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.weight.WeightLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/load-balancer/type/weight/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/weight/WeightLoadBalanceAlgorithm.java)         |

## KeyGenerateAlgorithm

### 全限定类名

[`org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/key-generator/core/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/core/KeyGenerateAlgorithm.java)

### 定义

分布式主键生成算法，可以使用在数据分片功能中。

### 已知实现

| *配置标识*       | *详细说明*                      | *全限定类名*                                                                                                                                                                                                                                                                                               |
|-----------------|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SNOWFLAKE       | 基于雪花算法的分布式主键生成算法    | [`org.apache.shardingsphere.keygen.snowflake.algorithm.SnowflakeKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/key-generator/type/snowflake/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/snowflake/SnowflakeKeyGenerateAlgorithm.java)    |
| UUID            | 基于 UUID 的分布式主键生成算法     | [`org.apache.shardingsphere.keygen.uuid.algorithm.UUIDKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/key-generator/type/uuid/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/uuid/UUIDKeyGenerateAlgorithm.java)                             |
