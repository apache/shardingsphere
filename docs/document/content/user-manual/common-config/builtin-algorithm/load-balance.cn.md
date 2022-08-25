+++
title = "负载均衡算法"
weight = 4
+++

## 背景信息

ShardingSphere 内置提供了多种负载均衡算法，具体包括了轮询算法、随机访问算法和权重访问算法，能够满足用户绝大多数业务场景的需要。此外，考虑到业务场景的复杂性，内置算法也提供了扩展方式，用户可以基于 SPI 接口实现符合自己业务需要的负载均衡算法。

## 参数解释

|    *Type*   | *Describe* | *Limitations* |
| -------- | ------------- | ------------ |
| ROUND_ROBIN  | 事务内，读请求路由到 primary，事务外，采用轮询策略路由到 replica | |
| RANDOM    |事务内，读请求路由到 primary，事务外，采用随机策略路由到 replica| |
| WEIGHT    | 事务内，读请求路由到 primary，事务外，采用权重策略路由到 replica| 需配置属性，属性名：${replica-name}，数据类型：double, 属性名字使用读库名字，参数填写读库对应的权重值。权重参数范围最小值 > 0，合计 <= Double.MAX_VALUE。|
| TRANSACTION_RANDOM  |显示/非显示开启事务，读请求采用随机策略路由到多个 replica| |
| TRANSACTION_ROUND_ROBIN  |显示/非显示开启事务，读请求采用轮询策略路由到多个 replica| |
| TRANSACTION_WEIGHT  |显示/非显示开启事务，读请求采用权重策略路由到多个 replica| 需配置属性，属性名：${replica-name}，数据类型：double, 属性名字使用读库名字，参数填写读库对应的权重值。权重参数范围最小值 > 0，合计 <= Double.MAX_VALUE。 |
| FIXED_REPLICA_RANDOM  |显示开启事务，读请求采用随机策略路由到一个固定 replica；不开事务，每次读流量使用随机策略路由到不同的 replica| |
| FIXED_REPLICA_ROUND_ROBIN  |显示开启事务，读请求采用轮询策略路由到一个固定 replica；不开事务，每次读流量使用轮询策略路由到不同的 replica| |
| FIXED_REPLICA_WEIGHT  |显示开启事务，读请求采用权重策略路由到一个固定 replica；不开事务，每次读流量使用权重策略路由到不同的 replica| 需配置属性，属性名：${replica-name}，数据类型：double, 属性名字使用读库名字，参数填写读库对应的权重值。权重参数范围最小值 > 0，合计 <= Double.MAX_VALUE。 |
| FIXED_PRIMARY  |读请求全部路由到 primary|

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
```

## 相关参考

- [核心特性：读写分离](/cn/features/readwrite-splitting/)
- [开发者指南：读写分离](/cn/dev-manual/readwrite-splitting/)
