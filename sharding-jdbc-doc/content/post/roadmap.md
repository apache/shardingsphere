+++
date = "2016-01-08T16:14:21+08:00"
title = "未来线路规划"
weight = 11
+++
# 未来线路规划

## 已完成

### transaction
1. sharding-jdbc-core发送SQL执行事件 `(完成)`
1. bed类型事务日志存储设计 `(完成)`
1. bed类型事务同步尝试 `(完成)`
1. 基于elastic-job的bed类型事务异步尝试 `(完成)`

### config 2.0
1. 动态表配置 `(完成)`
1. 简化只分库配置，无需配置逻辑表和真实表对应关系 `(完成)`
1. 简化包含不分片库表的配置，可指定默认数据源，不分片无需配置TableRule `(完成)`

### 读写分离
1. 一主多从的读写分离，可配合分库分表使用 `(完成)`
1. 同一线程且同一数据库连接内，写入操作后读操作均从主库读取，用于保证数据一致性 `(完成)`
1. Spring命名空间 `(完成)`
1. Hint强制主库路由 `(完成)`

## 进行中

## 计划中

### transaction 2.0
1. 基于tcc的柔性事务

### distribution id
1. JDBC接口改写
1. 分布式主键策略接口制定
1. 基于snowflake的分布式主键算法实现
1. 基于groupsequence的分布式主键算法实现
1. 基于数据库的分布式主键算法实现

### parser 2.0
1. 判断不支持SQL并直接报错
1. 支持DISTINCT
1. 支持GROUP BY聚合之后进行HAVING
1. 支持计算表达式，如：SUM(pv) / COUNT(uv)
1. 支持通过SQL注释指定SQL Hint
1. SQL重写优化，进一步提升性能

### merger 2.0
1. 管道化结果归并 `(完成)`
1. 支持OR语句根据row的唯一标识去重
1. 支持DISTINCT
1. 支持GROUP BY聚合之后进行HAVING
1. 支持计算表达式，如：SUM(pv) / COUNT(uv)

### router 2.0
1. 支持通过SQL注释指定SQL Hint
1. ThreadLocal Hints需重新考虑生命周期是否和connection以及statement绑定 `(完成)`
1. 释放资源时，自动清理ThreadLocal Hints `(完成)`

## 待定
1. 字典表复制广播
1. HA相关
1. 流量控制
1. 建表工具
1. 动态扩容
1. 配置中心
1. 其他数据库支持
