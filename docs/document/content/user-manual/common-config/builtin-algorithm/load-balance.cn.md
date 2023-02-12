+++
title = "负载均衡算法"
weight = 4
+++

## 背景信息

ShardingSphere 内置提供了多种负载均衡算法，具体包括了轮询算法、随机访问算法和权重访问算法，能够满足用户绝大多数业务场景的需要。此外，考虑到业务场景的复杂性，内置算法也提供了扩展方式，用户可以基于 SPI 接口实现符合自己业务需要的负载均衡算法。

## 参数解释

### 轮询负载均衡算法

类型：ROUND_ROBIN

说明：事务内，读请求根据 `transaction-read-query-strategy` 属性的配置进行路由。事务外，采用轮询策略路由到 replica。

可配置属性：

| *属性名称*      | *数据类型* | *说明*                                                                                                                                      |
| -------------- |--------|-------------------------------------------------------------------------------------------------------------------------------------------|
| transaction-read-query-strategy | String | 事务内读请求路由策略，可选值：FIXED_PRIMARY(路由到 primary)、FIXED_REPLICA(根据轮询策略选择一个固定的 replica)、DYNAMIC_REPLICA(根据轮询策略路由到不同的 replica)，默认值：FIXED_PRIMARY。 |

### 随机负载均衡算法

类型：RANDOM

说明：事务内，读请求根据 `transaction-read-query-strategy` 属性的配置进行路由。事务外，采用随机策略路由到 replica。

可配置属性：

| *属性名称*      | *数据类型* | *说明*                                                                                                                                    |
| -------------- |--------|-----------------------------------------------------------------------------------------------------------------------------------------|
| transaction-read-query-strategy | String | 事务内读请求路由策略，可选值：FIXED_PRIMARY(路由到 primary)、FIXED_REPLICA(根据随机策略选择一个固定的 replica)、DYNAMIC_REPLICA(根据随机策略路由到不同的 replica)，默认值：FIXED_PRIMARY。 |

### 权重负载均衡算法

类型：WEIGHT

说明：事务内，读请求根据 `transaction-read-query-strategy` 属性的配置进行路由。事务外，采用权重策略路由到 replica。

可配置属性：

| *属性名称*      | *数据类型* | *说明*                                                                                                                                    |
| -------------- |--------|-----------------------------------------------------------------------------------------------------------------------------------------|
| ${replica-name} | double | 属性名使用读库名称，参数填写读库对应的权重值。权重参数范围最小值 > 0，合计 <= Double.MAX_VALUE。                                                                            |
| transaction-read-query-strategy | String | 事务内读请求路由策略，可选值：FIXED_PRIMARY(路由到 primary)、FIXED_REPLICA(根据权重策略选择一个固定的 replica)、DYNAMIC_REPLICA(根据权重策略路由到不同的 replica)，默认值：FIXED_PRIMARY。 |

## 操作步骤

1. 使用读写分离时，在 loadBalancers 属性下配置对应的负载均衡算法即可；

## 配置示例

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

## 相关参考

- [核心特性：读写分离](/cn/features/readwrite-splitting/)
- [开发者指南：读写分离](/cn/dev-manual/readwrite-splitting/)
