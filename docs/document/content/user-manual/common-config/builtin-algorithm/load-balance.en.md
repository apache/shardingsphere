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

Description: Within the transaction, read query are routed according to the configuration of the `transaction-read-query-strategy` property, and outside the transaction, the round-robin strategy is used to route to the replica.

Attributes:

| *Name*         | *DataType* | *Description*                                                                                                                                                                                                                                                                                     |
| -------------- |------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| transaction-read-query-strategy | String | Routing strategy for read query within a transaction, optional values: FIXED_PRIMARY (route to primary), FIXED_REPLICA (select a fixed replica according to the round-robin strategy), DYNAMIC_REPLICA (route to different replicas according to the round-robin strategy), default value: FIXED_PRIMARY. |

### Random Load Balance Algorithm

Type: RANDOM

Description: Within the transaction, read query are routed according to the configuration of the `transaction-read-query-strategy` property, and outside the transaction, the random strategy is used to route to the replica.

Attributes:

| *Name*         | *DataType* | *Description*                                                                                                                                                                                                                                                                                     |
| -------------- |------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| transaction-read-query-strategy | String | Routing strategy for read query within a transaction, optional values: FIXED_PRIMARY (route to primary), FIXED_REPLICA (select a fixed replica according to the random strategy), DYNAMIC_REPLICA (route to different replicas according to the random strategy), default value: FIXED_PRIMARY. |

### Weight Load Balance Algorithm

Type: WEIGHT

Description: Within the transaction, read query are routed according to the configuration of the `transaction-read-query-strategy` property, and outside the transaction, the weight strategy is used to route to the replica.

Attributes: 

| *Name*         | *DataType* | *Description*                                                                                                                                                                                                                                                                                     |
| -------------- |------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ${replica-name} | double     | Attribute name uses the name of the replica, and the parameter fills in the weight value corresponding to the replica. Weight parameter range min > 0, total <= Double.MAX_VALUE.                                                                                                                 |
| transaction-read-query-strategy | String | Routing strategy for read query within a transaction, optional values: FIXED_PRIMARY (route to primary), FIXED_REPLICA (select a fixed replica according to the weight strategy), DYNAMIC_REPLICA (route to different replicas according to the weight strategy), default value: FIXED_PRIMARY. |

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
      props:
        transaction-read-query-strategy: FIXED_PRIMARY
```

## Related References

- [Core Feature: Read/Write Splitting](/en/features/readwrite-splitting/)
- [Developer Guide: Read/Write Splitting](/en/dev-manual/readwrite-splitting/)
