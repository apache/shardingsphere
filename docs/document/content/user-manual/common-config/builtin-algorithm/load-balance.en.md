+++
title = "Load Balance Algorithm"
weight = 4
+++

## Background

ShardingSphere built-in provides a variety of load balancer algorithms, including polling algorithm, random access algorithm and weight access algorithm, which can meet users' needs in most business scenarios.

Moreover, considering the complexity of the business scenario, the built-in algorithm also provides an extension mode. Users can implement the load balancer algorithm they need based on SPI interface.

## Parameters

### Round-robin Load Balance Algorithm

Type: ROUND_ROBIN

Description: Within the transaction, read query are routed to the primary, and outside the transaction, the round-robin strategy is used to route to the replica.

Attributes: None

### Random Load Balance Algorithm

Type: RANDOM

Description: Within the transaction, read query are routed to the primary, and outside the transaction, the random strategy is used to route to the replica.

Attributes: None

### Weight Load Balance Algorithm

Type: WEIGHT

Description: Within the transaction, read query are routed to the primary, and outside the transaction, the weight strategy is used to route to the replica.

Attributes: 

| *Name*         | *DataType* | *Description*                                                                                                                                                                     |
| -------------- |------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ${replica-name} | double     | Attribute name uses the name of the replica, and the parameter fills in the weight value corresponding to the replica. Weight parameter range min > 0, total <= Double.MAX_VALUE. |

### Transaction Random Load Balance Algorithm

Type: TRANSACTION_RANDOM

Description: Display/non-display open transaction, read query are routed to multiple replicas using random strategy.

Attributes: None

### Transaction Round-robin Load Balance Algorithm

Type: TRANSACTION_ROUND_ROBIN

Description: Display/non-display open transaction, read query are routed to multiple replicas using round-robin strategy.

Attributes: None

### Transaction Weight Load Balance Algorithm

Type: TRANSACTION_WEIGHT

Description: Display/non-display open transaction, read query are routed to multiple replicas using weight strategy.

Attributes: 

| *Name*         | *DataType* | *Description*                                                                                                                                                                     |
| -------------- |------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ${replica-name} | double     | Attribute name uses the name of the replica, and the parameter fills in the weight value corresponding to the replica. Weight parameter range min > 0, total <= Double.MAX_VALUE. |

### Fixed Replica Random Load Balance Algorithm

Type: FIXED_REPLICA_RANDOM

Description: Open transaction displayed, and the read query is routed to a fixed replica using random strategy; otherwise, each read traffic is routed to a different replica using random strategy.

Attributes: None

### Fixed Replica Round-robin Load Balance Algorithm

Type: FIXED_REPLICA_ROUND_ROBIN

Description: Open transaction displayed, and the read query is routed to a fixed replica using round-robin strategy; otherwise, each read traffic is routed to a different replica using round-robin strategy.

Attributes: None

### Fixed Replica Weight Load Balance Algorithm

Type: FIXED_REPLICA_WEIGHT

Description: Open transaction displayed, and the read query is routed to a fixed replica using weight strategy; otherwise, each read traffic is routed to a different replica using weight strategy.

Attributes: 

| *Name*         | *DataType* | *Description*                                                                                                                                                                     |
| -------------- |------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ${replica-name} | double     | Attribute name uses the name of the replica, and the parameter fills in the weight value corresponding to the replica. Weight parameter range min > 0, total <= Double.MAX_VALUE. |

### Fixed Primary Load Balance Algorithm

Type: FIXED_PRIMARY

Description: All read query are routed to the primary.

Attributes: None

## Procedure

1. Configure a load balancer algorithm for the loadBalancers attribute to use read/write splitting.

## Sample

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    readwrite_ds:
      staticStrategy:
        writeDataSourceName: write_ds
        readDataSourceNames:
          - read_ds_0
          - read_ds_1
      loadBalancerName: random
  loadBalancers:
    random:
      type: RANDOM
```

## Related References

- [Core Feature: Read/Write Splitting](/en/features/readwrite-splitting/)
- [Developer Guide: Read/Write Splitting](/en/dev-manual/readwrite-splitting/)
