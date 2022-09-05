+++
title = "负载均衡算法"
weight = 4
+++

## 背景信息

ShardingSphere 内置提供了多种负载均衡算法，具体包括了轮询算法、随机访问算法和权重访问算法，能够满足用户绝大多数业务场景的需要。此外，考虑到业务场景的复杂性，内置算法也提供了扩展方式，用户可以基于 SPI 接口实现符合自己业务需要的负载均衡算法。

## 参数解释

### 轮询负载均衡算法

类型：ROUND_ROBIN

说明：事务内，读请求路由到 primary，事务外，采用轮询策略路由到 replica。

可配置属性：无

### 随机负载均衡算法

类型：RANDOM

说明：事务内，读请求路由到 primary，事务外，采用随机策略路由到 replica。

可配置属性：无

### 权重负载均衡算法

类型：WEIGHT

说明：事务内，读请求路由到 primary，事务外，采用权重策略路由到 replica。

可配置属性：

| *属性名称*      | *数据类型* | *说明*                                                         |
| -------------- | -------- |--------------------------------------------------------------|
| ${replica-name} |   double    | 属性名使用读库名称，参数填写读库对应的权重值。权重参数范围最小值 > 0，合计 <= Double.MAX_VALUE。 |

### 事务随机负载均衡算法

类型：TRANSACTION_RANDOM

说明：显示/非显示开启事务，读请求采用随机策略路由到多个 replica。

可配置属性：无

### 事务轮询负载均衡算法

类型：TRANSACTION_ROUND_ROBIN

说明：显示/非显示开启事务，读请求采用轮询策略路由到多个 replica。

可配置属性：无

### 事务权重负载均衡算法

类型：TRANSACTION_WEIGHT

说明：显示/非显示开启事务，读请求采用权重策略路由到多个 replica。

可配置属性：

| *属性名称*      | *数据类型* | *说明*                                                         |
| -------------- | -------- |--------------------------------------------------------------|
| ${replica-name} |   double    | 属性名使用读库名称，参数填写读库对应的权重值。权重参数范围最小值 > 0，合计 <= Double.MAX_VALUE。 |

### 固定副本随机负载均衡算法

类型：FIXED_REPLICA_RANDOM

说明：显示开启事务，读请求采用随机策略路由到一个固定 replica；不开事务，每次读流量使用随机策略路由到不同的 replica。

可配置属性：无

### 固定副本轮询负载均衡算法

类型：FIXED_REPLICA_ROUND_ROBIN

说明：显示开启事务，读请求采用轮询策略路由到一个固定 replica；不开事务，每次读流量使用轮询策略路由到不同的 replica。

可配置属性：无

### 固定副本权重负载均衡算法

类型：FIXED_REPLICA_WEIGHT

说明：显示开启事务，读请求采用权重策略路由到一个固定 replica；不开事务，每次读流量使用权重策略路由到不同的 replica。

可配置属性：

| *属性名称*      | *数据类型* | *说明*                                                         |
| -------------- | -------- |--------------------------------------------------------------|
| ${replica-name} |   double    | 属性名使用读库名称，参数填写读库对应的权重值。权重参数范围最小值 > 0，合计 <= Double.MAX_VALUE。 |

### 固定主库负载均衡算法

类型：FIXED_PRIMARY

说明：读请求全部路由到 primary

可配置属性：无

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
