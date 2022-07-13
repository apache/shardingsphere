+++
pre = "<b>6.8. </b>"
title = "Readwrite-splitting"
weight = 8
chapter = true
+++

## SPI Interface

## ReadQueryLoadBalanceAlgorithm

| *SPI Name*                                 | *Description*              |
| ----------------------------------------- | ----------------------- |
| ReadQueryLoadBalanceAlgorithm             | the read database load balancer algorithm           |

## Sample

## ReadQueryLoadBalanceAlgorithm

| *known implementation class*              | *Description*               |
| ----------------------------------------- | ----------------------- |
| RoundRobinReplicaLoadBalanceAlgorithm     | the read database load balancer algorithm based on polling |
| RandomReplicaLoadBalanceAlgorithm         | the read database load balancer algorithm based on random |
| WeightReplicaLoadBalanceAlgorithm         | the read database load balancer algorithm based on weight |
