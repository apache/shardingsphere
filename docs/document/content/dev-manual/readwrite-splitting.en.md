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
| TransactionRandomReplicaLoadBalanceAlgorithm     | Whether in a transaction or not, read requests are routed to multiple replicas using a random strategy |
| TransactionRoundRobinReplicaLoadBalanceAlgorithm | Whether in a transaction or not, read requests are routed to multiple replicas using a round-robin strategy |
| TransactionWeightReplicaLoadBalanceAlgorithm     | Whether in a transaction or not, read requests are routed to multiple replicas using a weight strategy |
| FixedReplicaRandomLoadBalanceAlgorithm           | Open transaction, and the read request is routed to a fixed replica using a random strategy; if the transaction is not opened, each read traffic is routed to a different replica using the specified algorithm |
| FixedReplicaRoundRobinLoadBalanceAlgorithm       | Open transaction, and the read request is routed to a fixed replica using a round-robin strategy; if the transaction is not opened, each read traffic is routed to a different replica using the specified algorithm |
| FixedReplicaWeightLoadBalanceAlgorithm           | Open transaction, and the read request is routed to a fixed replica using a weight strategy; if the transaction is not opened, each read traffic is routed to a different replica using the specified algorithm |
| FixedPrimaryLoadBalanceAlgorithm                 | All read traffic is routed to the primary |
