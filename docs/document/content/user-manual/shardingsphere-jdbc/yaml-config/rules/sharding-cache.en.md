+++
title = "Cache for Sharding Route"
weight = 10
+++

## Background

This feature is **experimental** and needs to be used with data sharding rule.
The cache for sharding route will put the logical SQL, the actual parameter value of the shard key, and the routing result into the cache, exchange space for time, and reduce the CPU usage of routing logic.

It is recommended to enable only if the following conditions are met:
- Pure OLTP scenarios
- The CPU of the machine which deployed the ShardingSphere process has reached the bottleneck
- Most of the CPUs are used by ShardingSphere routing logic
- All SQLs are optimized and each SQL execution could be routed to a single data node

If the above conditions are not met, the execution delay of SQL may not be significantly improved, and the memory pressure will be increased.

## Parameters

```yaml
rules:
  - !SHARDING_CACHE
    allowedMaxSqlLength: 512 # Allow cached SQL length limit
    routeCache:
      initialCapacity: 65536 # Initial capacity
      maximumSize: 262144 # Maximum capacity
      softValues: true # Whether to use soft references
```

## Related References

- [Core Feature: Data Sharding](/en/features/sharding/)
