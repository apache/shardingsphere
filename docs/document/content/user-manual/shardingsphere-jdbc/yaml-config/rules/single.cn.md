+++
title = "单表"
weight = 12
+++

## 背景信息

单表规则用于指定哪些单表需要被 ShardingSphere 管理，也可设置默认的单表数据源。

## 参数解释

```yaml
rules:
- !SINGLE
  tables:
    # MySQL 风格
    - ds_0.t_single # 加载指定单表
    - ds_1.* # 加载指定数据源中的全部单表
    - "*.*" # 加载全部单表
    # PostgreSQL 风格
    - ds_0.public.t_config
    - ds_1.public.*
    - ds_2.*.*
    - "*.*.*"
  defaultDataSource: ds_0 # 默认数据源，仅在执行 CREATE TABLE 创建单表时有效。缺失值为空，表示随机单播路由。
```

## 相关参考

- [单表](/cn/features/sharding/concept/#单表)
