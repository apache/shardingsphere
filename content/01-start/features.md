+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "详细功能列表"
weight = 3
prev = "/01-start/faq/"
next = "/01-start/limitations/"

+++

## 功能性需求

### 分库分表
* SQL解析功能完善，支持聚合，分组，排序，LIMIT，TOP等查询，并且支持级联表以及笛卡尔积的表查询
* 支持内、外连接查询
* 分片策略灵活，可支持=，BETWEEN，IN等多维度分片，也可支持多分片键共用，以及自定义分片策略
* 基于Hint的强制分库分表路由

### 读写分离
* 一主多从的读写分离配置，可配合分库分表使用
* 基于Hint的强制主库路由

### 柔性事务
* 最大努力送达型事务
* TCC型事务(TBD)

### 分布式主键
* 统一的分布式基于时间序列的ID生成器

### 兼容性
* 可适用于任何基于java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC
* 可基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid等
* 理论上可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL

### 灵活多样的配置
* Java
* YAML
* Inline表达式
* Spring命名空间
* Spring boot starter

### 分布式治理能力 (2.0新功能)
* 配置集中化与动态化，可支持数据源、表与分片策略的动态切换(2.0.0.M1)
* 客户端的数据库治理，数据源失效自动切换(2.0.0.M2)
* 基于Open Tracing协议的APM信息输出(2.0.0.M3)

## 性能
1. 半理解"理念的SQL解析引擎
2. 多线程处理结果归并
3. 性能损失率约6%

## 稳定性
1. 完善的疲劳测试，普通查询无Full GC（GROUP BY除外）
2. 多数据库、分片策略和语句的完整单元测试
