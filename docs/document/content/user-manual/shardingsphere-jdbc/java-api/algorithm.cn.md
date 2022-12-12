+++
title = "算法配置"
weight = 4
chapter = true
+++

## 分片算法

```java
ShardingRuleConfiguration ruleConfiguration = new ShardingRuleConfiguration();
// algorithmName 由用户指定，需要和分片策略中的分片算法一致
// type 和 props，请参考分片内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/sharding/
ruleConfiguration.getShardingAlgorithms().put("algorithmName", new AlgorithmConfiguration("xxx", new Properties()));
```

## 加密算法

```java
// encryptorName 由用户指定，需要和加密规则中的 encryptorName 属性一致
// type 和 props，请参考加密内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/encrypt/
Map<String, AlgorithmConfiguration> algorithmConfigs = new LinkedHashMap<>(1, 1);
algorithmConfigs.put("encryptorName", new AlgorithmConfiguration("xxx", new Properties()));
```

## 读写分离负载均衡算法

```java
// loadBalancerName 由用户指定，需要和读写分离规则中的 loadBalancerName 属性一致
// type 和 props，请参考读写分离负载均衡内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/load-balance/      
Map<String, AlgorithmConfiguration> algorithmConfigs = new HashMap<>(1, 1);
algorithmConfigs.put("loadBalancerName", new AlgorithmConfiguration("xxx", new Properties()));
```

## 影子算法

```java
// shadowAlgorithmName 由用户指定，需要和影子库规则中的 shadowAlgorithmNames 属性一致
// type 和 props，请参考影子库内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/shadow/      
Map<String, AlgorithmConfiguration> algorithmConfigs = new HashMap<>(1, 1);
algorithmConfigs.put("shadowAlgorithmName", new AlgorithmConfiguration("xxx", new Properties()));
```

## 高可用

```java
// discoveryTypeName 由用户指定，需要和数据库发现规则中的 discoveryTypeName 属性一致
Map<String, AlgorithmConfiguration> algorithmConfigs = new HashMap<>(1, 1);
algorithmConfigs.put("discoveryTypeName", new AlgorithmConfiguration("xxx", new Properties()));
```
