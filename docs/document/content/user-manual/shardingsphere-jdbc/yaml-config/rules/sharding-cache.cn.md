+++
title = "数据分片路由缓存"
weight = 10
+++

## 背景信息

该项功能为**实验性功能**，需要与数据分片功能同时使用。
数据分片路由缓存会将逻辑 SQL、分片键实际参数值、路由结果放入缓存中，以空间换时间，减少路由逻辑对 CPU 的使用。

建议仅在满足以下条件的情况下启用：
- 纯 OLTP 场景
- ShardingSphere 进程所在机器 CPU 已达到瓶颈
- CPU 开销主要在于 ShardingSphere 路由逻辑
- 所有 SQL 已经最优且每次 SQL 执行都能命中单一分片

在不满足以上条件的情况下使用，可能对 SQL 的执行延时不会有明显改善，同时会增加内存的压力。

## 参数解释

```yaml
rules:
  - !SHARDING_CACHE
    allowedMaxSqlLength: 512 # 允许缓存的 SQL 长度限制
    routeCache:
      initialCapacity: 65536 # 缓存初始容量
      maximumSize: 262144 # 缓存最大容量
      softValues: true # 是否软引用缓存值
```

## 相关参考

- [核心特性：数据分片](/cn/features/sharding/)
