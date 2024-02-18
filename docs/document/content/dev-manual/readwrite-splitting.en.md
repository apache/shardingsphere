+++
pre = "<b>5.4. </b>"
title = "Readwrite-splitting"
weight = 4
chapter = true
+++

## ReadQueryLoadBalanceAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/api/src/main/java/org/apache/shardingsphere/readwritesplitting/spi/ReadQueryLoadBalanceAlgorithm.java)

### Definition

Read query load balance algorithm's definition

### Implementation classes

| *Configuration Type* | *Description*                                              | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                         |
|----------------------|------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ROUND_ROBIN          | the read database load balancer algorithm based on polling | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RoundRobinReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/RoundRobinReadQueryLoadBalanceAlgorithm.java) |
| RANDOM               | the read database load balancer algorithm based on random  | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/RandomReadQueryLoadBalanceAlgorithm.java)         |
| WEIGHT               | the read database load balancer algorithm based on weight  | [`org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.WeightReadQueryLoadBalanceAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/readwrite-splitting/core/src/main/java/org/apache/shardingsphere/readwritesplitting/algorithm/loadbalance/WeightReadQueryLoadBalanceAlgorithm.java)         |
