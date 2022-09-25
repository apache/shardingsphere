+++
title = "分片审计算法"
weight = 8
+++

## 背景信息

分片审计功能是针对数据库分片场景下对执行的 SQL 语句进行审计操作。分片审计既可以进行拦截操作，拦截系统配置的非法 SQL 语句，也可以是对 SQL 语句进行统计操作。

## 参数解释

### DML_SHARDING_CONDITIONS 算法

类型：DML_SHARDING_CONDITIONS

## 操作步骤

1. 配置数据分片规则时设置分配审计生成策略

## 配置示例

- DML_SHARDING_CONDITIONS

```yaml
auditors:
  sharding_key_required_auditor:
    type: DML_SHARDING_CONDITIONS
```
