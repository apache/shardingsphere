+++
title = "Load Balance Algorithm"
weight = 4
+++

## Background

ShardingSphere built-in provides a variety of load balancer algorithms, including polling algorithm, random access algorithm and weight access algorithm, which can meet users' needs in most business scenarios.

Moreover, considering the complexity of the business scenario, the built-in algorithm also provides an extension mode. Users can implement the load balancer algorithm they need based on SPI interface.

## Parameters

### Round Robin Algorithm

Type: ROUND_ROBIN

Attributes: None

### Random Algorithm

Type: RANDOM

Attributes: None

### Weight Algorithm

Type: WEIGHT

Attributes: 

> All read data in use must be configured with weights

| *Name*                 | *DataType* | *Description*                              |
| ---------------------------------- | ---------- | ---------------------------------------------- |
| \- <read-data_source-name> (+) | double     | The attribute name uses the read database name, and the parameter fills in the weight value corresponding to the read database.The minimum value of the weight parameter range>0,the total <=Double.MAX_VALUE. |

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
