+++
title = "Algorithm"
weight = 4
chapter = true
+++

## Sharding

```java
ShardingRuleConfiguration ruleConfiguration = new ShardingRuleConfiguration();
// algorithmName is specified by users and should be consistent with the sharding algorithm in the sharding strategy.
// type and props, please refer to the built-in sharding algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/sharding/
ruleConfiguration.getShardingAlgorithms().put("algorithmName", new AlgorithmConfiguration("xxx", new Properties()));
```

## Encryption

```java
// encryptorName is specified by users, and its property should be consistent with that of encryptorName in encryption rules.
// type and props, please refer to the built-in encryption algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/encrypt/
Map<String, AlgorithmConfiguration> algorithmConfigs = new LinkedHashMap<>(1, 1);
algorithmConfigs.put("encryptorName", new AlgorithmConfiguration("xxx", new Properties()));
```

## Read/Write Splitting Load Balancer

```java
// loadBalancerName is specified by users, and its property has to be consistent with that of loadBalancerName in read/write splitting rules.
// type and props, please refer to the built-in read/write splitting algorithm load balancer: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/load-balance/      
Map<String, AlgorithmConfiguration> algorithmConfigs = new HashMap<>(1, 1);
algorithmConfigs.put("loadBalancerName", new AlgorithmConfiguration("xxx", new Properties()));
```

## Shadow DB

```java
// shadowAlgorithmName is specified by users, and its property has to be consistent with that of shadowAlgorithmNames in shadow DB rules.
// type and props, please refer to the built-in shadow DB algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/shadow/      
Map<String, AlgorithmConfiguration> algorithmConfigs = new HashMap<>(1, 1);
algorithmConfigs.put("shadowAlgorithmName", new AlgorithmConfiguration("xxx", new Properties()));
```

## High Availability

```
// discoveryTypeName is specified by users, and its property has to be consistent with that of discoveryTypeName in database discovery rules.
Map<String, AlgorithmConfiguration> algorithmConfigs = new HashMap<>(1, 1);
algorithmConfigs.put("discoveryTypeName", new AlgorithmConfiguration("xxx", new Properties()));
```
