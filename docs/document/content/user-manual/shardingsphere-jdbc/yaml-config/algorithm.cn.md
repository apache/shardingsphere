+++
title = "算法配置"
weight = 4
+++

## 分片算法

```yaml
shardingAlgorithms:
  # algorithmName 由用户指定，需要和分片策略中的 shardingAlgorithmName 属性一致
  <algorithmName>:
    # type 和 props，请参考分片内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/sharding/
    type: xxx
    props:
      xxx: xxx
```

## 加密算法

```yaml
encryptors:
  # encryptorName 由用户指定，需要和加密规则中的 encryptorName 属性一致
  <encryptorName>:
    # type 和 props，请参考加密内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/encrypt/
    type: xxx
    props:
      xxx: xxx
```

## 读写分离负载均衡算法

```yaml
loadBalancers:
  # loadBalancerName 由用户指定，需要和读写分离规则中的 loadBalancerName 属性一致
  <loadBalancerName>:
    # type 和 props，请参考读写分离负载均衡内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/load-balance/
    type: xxx
    props:
      xxx: xxx
```

## 影子算法

```yaml
loadBalancers:
  # shadowAlgorithmName 由用户指定，需要和影子库规则中的 shadowAlgorithmNames 属性一致
  <shadowAlgorithmName>:
    # type 和 props，请参考影子库内置算法：https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/shadow/
    type: xxx
    props:
      xxx: xxx
```

## 高可用

```yaml
discoveryTypes:
  # discoveryTypeName 由用户指定，需要和数据库发现规则中的 discoveryTypeName 属性一致
  <discoveryTypeName>:
    type: xxx
    props:
      xxx: xxx
```
