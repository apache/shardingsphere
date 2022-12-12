+++
title = "Algorithm"
weight = 4
+++

## Sharding

```yaml
shardingAlgorithms:
  # algorithmName is specified by users, and its property has to be consistent with that of shardingAlgorithmName in the sharding strategy.
  <algorithmName>:
    # type and props, please refer to the built-in sharding algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/sharding/
    type: xxx
    props:
      xxx: xxx
```

## Encryption

```yaml
encryptors:
  # encryptorName is specified by users, and its property should be consistent with that of encryptorName in encryption rules.
  <encryptorName>:
    # type and props, please refer to the built-in encryption algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/encrypt/
    type: xxx
    props:
      xxx: xxx
```

## Read/Write Splitting Load Balancer

```yaml
loadBalancers:
  # loadBalancerName is specified by users, and its property has to be consistent with that of loadBalancerName in read/write splitting rules.
    # type and props, please refer to the built-in read/write splitting algorithm load balancer: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/load-balance/
    type: xxx
    props:
      xxx: xxx
``` 

## Shadow DB

```yaml
loadBalancers:
  # shadowAlgorithmName is specified by users, and its property has to be consistent with that of shadowAlgorithmNames in shadow DB rules.
  <shadowAlgorithmName>:
    # type and props, please refer to the built-in shadow DB algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/shadow/
    type: xxx
    props:
      xxx: xxx
```

## High Availability

```yaml
discoveryTypes:
  # discoveryTypeName is specified by users, and its property has to be consistent with that of discoveryTypeName in the database discovery rules.
    type: xxx
    props:
      xxx: xxx
```
