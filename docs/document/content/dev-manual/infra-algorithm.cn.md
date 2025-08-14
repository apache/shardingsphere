+++
pre = "<b>5.4. </b>"
title = "基础算法"
weight = 4
chapter = true
+++

## LoadBalanceAlgorithm

### 全限定类名

[`org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/load-balancer/spi/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/spi/LoadBalanceAlgorithm.java)

### 定义

负载均衡算法，可以使用在读写分离、JDBC 双路由等功能中。

### 已知实现

| *配置标识*      | *详细说明*        | *全限定类名*                                                                                                                                                                                                                                                                                                                              |
|-------------|---------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ROUND_ROBIN | 基于轮询的负载均衡算法 | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.round.robin.RoundRobinLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/load-balancer/type/round-robin/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/round/robin/RoundRobinLoadBalanceAlgorithm.java) |
| RANDOM      | 基于随机的负载均衡算法 | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.random.RandomLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/load-balancer/type/random/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/random/RandomLoadBalanceAlgorithm.java)         |
| WEIGHT      | 基于权重的负载均衡算法 | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.weight.WeightLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/load-balancer/type/weight/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/weight/WeightLoadBalanceAlgorithm.java)         |

## KeyGenerateAlgorithm

### 全限定类名

[`org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/key-generator/spi/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/spi/KeyGenerateAlgorithm.java)

### 定义

分布式主键生成算法，可以使用在数据分片功能中。

### 已知实现

| *配置标识*       | *详细说明*                      | *全限定类名*                                                                                                                                                                                                                                                                                               |
|-----------------|--------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SNOWFLAKE       | 基于雪花算法的分布式主键生成算法    | [`org.apache.shardingsphere.keygen.snowflake.algorithm.SnowflakeKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/key-generator/type/snowflake/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/snowflake/SnowflakeKeyGenerateAlgorithm.java)    |
| UUID            | 基于 UUID 的分布式主键生成算法     | [`org.apache.shardingsphere.keygen.uuid.algorithm.UUIDKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/key-generator/type/uuid/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/uuid/UUIDKeyGenerateAlgorithm.java)                             |

## MessageDigestAlgorithm

### 全限定类名

[`org.apache.shardingsphere.infra.algorithm.messagedigest.spi.MessageDigestAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/message-digest/spi/src/main/java/org/apache/shardingsphere/infra/algorithm/messagedigest/spi/MessageDigestAlgorithm.java)

### 定义

消息摘要算法，可以使用在数据脱敏、数据加密功能中。

### 已知实现

| *配置标识* | *详细说明*      | *全限定类名*                                                                                                                                                                                                                                                                                               |
|--------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MD5    | MD5 消息摘要算法 | [`org.apache.shardingsphere.infra.algorithm.messagedigest.md5.MD5MessageDigestAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/type/message-digest/type/md5/src/main/java/org/apache/shardingsphere/infra/algorithm/messagedigest/md5/MD5MessageDigestAlgorithm.java)    |
