+++
pre = "<b>6.8. </b>"
title = "Readwrite-splitting"
weight = 8
chapter = true
+++

## ReadwriteSplittingType

| *SPI Name*                                 | *Description*                 |
| ----------------------------------------- | ------------------------- |
| ReadwriteSplittingType                    | Readwrite-splitting type  |

| *Implementation Class*                               | *Description*                         |
| ----------------------------------------- | -------------------------------- |
| StaticReadwriteSplittingType              | Static readwrite-splitting type  |
| DynamicReadwriteSplittingType             | Dynamic readwrite-splitting type |

## ReplicaLoadBalanceAlgorithm

| *SPI Name*                            | *Description*                                           |
| ------------------------------------- | ------------------------------------------------------- |
| ReplicaLoadBalanceAlgorithm           | Load balance algorithm of replica databases             |

| *Implementation Class*                | *Description*                                           |
| ------------------------------------- | ------------------------------------------------------- |
| RoundRobinReplicaLoadBalanceAlgorithm | Round robin load balance algorithm of replica databases |
| RandomReplicaLoadBalanceAlgorithm     | Random load balance algorithm of replica databases      |
| WeightReplicaLoadBalanceAlgorithm     | Weight load balance algorithm of replica databases      |
