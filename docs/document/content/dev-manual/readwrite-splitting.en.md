+++
pre = "<b>6.8. </b>"
title = "Readwrite-splitting"
weight = 8
chapter = true
+++

## ReadwriteSplittingType

| *SPI 名称*                                 | *详细说明*                 |
| ----------------------------------------- | ------------------------- |
| ReadwriteSplittingType                    | Readwrite-splitting type  |

| *已知实现类*                               | *详细说明*                         |
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
