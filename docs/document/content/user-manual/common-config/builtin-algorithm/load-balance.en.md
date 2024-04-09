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

### Random Load Balance Algorithm

Type: RANDOM

### Weight Load Balance Algorithm

Type: WEIGHT

Attributes:

| *Name*          | *DataType* | *Description*                                                                                                                                                                     |
|-----------------|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ${replica-name} | double     | Attribute name uses the name of the replica, and the parameter fills in the weight value corresponding to the replica. Weight parameter range min > 0, total <= Double.MAX_VALUE. |

## Procedure

1. Configure a load balancer algorithm for the loadBalancers attribute to use read/write splitting.

## Sample

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    readwrite_ds:
      writeDataSourceName: write_ds
      readDataSourceNames:
        - read_ds_0
        - read_ds_1
      transactionalReadQueryStrategy: PRIMARY
      loadBalancerName: random
  loadBalancers:
    random:
      type: RANDOM
```

## Related References

- [Core Feature: Read/Write Splitting](/en/features/readwrite-splitting/)
- [Developer Guide: Read/Write Splitting](/en/dev-manual/infra-algorithm/)
