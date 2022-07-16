+++
pre = "<b>6.8. </b>"
title = "Readwrite-splitting"
weight = 8
chapter = true
+++

## SPI Interface

### ReadQueryLoadBalanceAlgorithm

| *SPI Name*                                 | *Description*              |
| ----------------------------------------- | ----------------------- |
| ReadQueryLoadBalanceAlgorithm             | the read database load balancer algorithm           |

## Sample

### ReadQueryLoadBalanceAlgorithm

| *known implementation class*                        | *Description*                                                                                                                                                                                                        |
|-----------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| RoundRobinReadQueryLoadBalanceAlgorithm             | the read database load balancer algorithm based on polling                                                                                                                                                           |
| RandomReadQueryLoadBalanceAlgorithm                 | the read database load balancer algorithm based on random                                                                                                                                                            |
| WeightReadQueryLoadBalanceAlgorithm                 | the read database load balancer algorithm based on weight                                                                                                                                                            |
| TransactionRandomReadQueryLoadBalanceAlgorithm      | Whether in a transaction or not, read requests are routed to multiple replicas using a random strategy                                                                                                               |
| TransactionRoundRobinReadQueryLoadBalanceAlgorithm  | Whether in a transaction or not, read requests are routed to multiple replicas using a round-robin strategy                                                                                                          |
| TransactionWeightReadQueryLoadBalanceAlgorithm      | Whether in a transaction or not, read requests are routed to multiple replicas using a weight strategy                                                                                                               |
| FixedReplicaRandomReadQueryLoadBalanceAlgorithm     | Open transaction, and the read request is routed to a fixed replica using a random strategy; if the transaction is not opened, each read traffic is routed to a different replica using the specified algorithm      |
| FixedReplicaRoundRobinReadQueryLoadBalanceAlgorithm | Open transaction, and the read request is routed to a fixed replica using a round-robin strategy; if the transaction is not opened, each read traffic is routed to a different replica using the specified algorithm |
| FixedReplicaWeightReadQueryLoadBalanceAlgorithm     | Open transaction, and the read request is routed to a fixed replica using a weight strategy; if the transaction is not opened, each read traffic is routed to a different replica using the specified algorithm      |
| FixedPrimaryReadQueryLoadBalanceAlgorithm           | All read traffic is routed to the primary                                                                                                                                                                            |
