+++
title = "联邦查询"
weight = 13
+++

## 背景信息

该功能为**实验性功能，暂不适合核心系统生产环境使用**。
联邦查询 YAML 配置方式可读性高，当关联查询中的多个表分布在不同的数据库实例上时，通过开启联邦查询可以进行跨库关联查询，以及子查询。

## 参数解释

```yaml
sqlFederation:
  sqlFederationEnabled: # 是否开启联邦查询
  allQueryUseSQLFederation: # 是否全部查询 SQL 使用联邦查询
  executionPlanCache: # 执行计划缓存
    initialCapacity: 2000 # 执行计划缓存初始容量
    maximumSize: 65535 # 执行计划缓存最大容量
```

## 配置示例

```yaml
sqlFederation:
  sqlFederationEnabled: true
  allQueryUseSQLFederation: false
  executionPlanCache:
    initialCapacity: 2000
    maximumSize: 65535
```

## 相关参考

- [JAVA API：联邦查询](/cn/user-manual/shardingsphere-jdbc/java-api/rules/sql-federation/)
