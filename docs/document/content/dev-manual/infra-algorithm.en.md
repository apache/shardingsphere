+++
pre = "<b>5.4. </b>"
title = "Infra algorithm"
weight = 4
chapter = true
+++

## LoadBalanceAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.infra.algorithm.loadbalancer.core.LoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/load-balancer/core/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/core/LoadBalanceAlgorithm.java)

### Definition

Load balance algorithms, they can be used in readwrite-splitting and traffic features.

### Implementation classes

| *Configuration Type* | *Description*                                          | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                         |
|----------------------|--------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ROUND_ROBIN          | load balancer algorithm based on polling | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.round.robin.RoundRobinLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/load-balancer/type/round-robin/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/round/robin/RoundRobinLoadBalanceAlgorithm.java) |
| RANDOM               | load balancer algorithm based on random | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.random.RandomLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/load-balancer/type/random/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/random/RandomLoadBalanceAlgorithm.java)         |
| WEIGHT               | load balancer algorithm based on weight | [`org.apache.shardingsphere.infra.algorithm.loadbalancer.weight.WeightLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/load-balancer/type/weight/src/main/java/org/apache/shardingsphere/infra/algorithm/loadbalancer/weight/WeightLoadBalanceAlgorithm.java)         |

## KeyGenerateAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/key-generator/core/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/core/KeyGenerateAlgorithm.java)

### Definition

Distributed key generated algorithms, they can be used in sharding feature.

### Implementation classes

| *Configuration Type* | *Description*                    | *Fully-qualified class name*                                                                                                                                                                                                                                                                          |
|----------------------|----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SNOWFLAKE            | Snowflake key generate algorithm | [`org.apache.shardingsphere.keygen.snowflake.algorithm.SnowflakeKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/key-generator/type/snowflake/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/snowflake/SnowflakeKeyGenerateAlgorithm.java) |
| UUID                 | UUID key generate algorithm      | [`org.apache.shardingsphere.keygen.uuid.algorithm.UUIDKeyGenerateAlgorithm`](https://github.com/apache/shardingsphere/blob/master/infra/algorithm/key-generator/type/uuid/src/main/java/org/apache/shardingsphere/infra/algorithm/keygen/uuid/UUIDKeyGenerateAlgorithm.java)                          |
