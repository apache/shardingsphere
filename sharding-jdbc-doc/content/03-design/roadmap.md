+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "未来线路规划"
weight = 4
prev = "/03-design/module"
next = "/00-overview"
+++

## 已完成

### 数据库
1. MySQL
1. Oracle
1. SQLServer
1. PostgreSQL

### DQL
1. JOIN
1. BETWEEN
1. IN
1. ORDER BY
1. GROUP BY
1. COUNT, SUM, MAX, MIN, AVG
1. LIMIT, rownum, TOP
1. 非功能型子查询

### DML
1. INSERT INTO
1. INSERT SET
1. UPDATE
1. DELETE

### 配置
1. Java API
1. Spring命名空间
1. Yaml (仅读写分离未完成)

### SQL解析
1. 词法分析
1. 语法分析

### SQL改写
1. 正确性改写
1. 优化改写

### 访问路由
1. Hint路由
1. 简单路由
1. 笛卡尔积路由

### 结果归并
1. 流式归并
1. 内存归并
1. 装饰者归并

### 分库分表
1. 分库
1. 分表
1. 动态表
1. 默认数据源

### 读写分离
1. 读写分离
1. 同一线程且同一数据库连接内一致性保证
1. Hint强制主库路由

### 分布式主键
1. JDBC集成
1. 分布式主键策略接口
1. 基于snowflake的分布式主键算法实现

### 柔性事务
1. 最大努力送达型

## 计划中

### 配置
1. 读写分离独立化
1. Binding策略提升
1. 集中化
1. 动态化

### 治理
1. 数据源健康检测及发现
1. 数据源动态切换
1. 流量控制

## 待定

### DQL
1. DISTINCT
1. HAVING
1. OR
1. UNION, UNION ALL
1. 全子查询
1. 计算表达式，如：SUM(pv) / COUNT(uv)
1. 通过SQL注释指定SQL Hint

### DML
1. INSERT INTO VALUES (xxx), (xxx)
1. 多表UPDATE
1. 多表DELETE

### DDL
1. CREATE
1. ALTER
1. DROP
1. TRUNCATE

### SQL解析提升
1. 批量解析
1. 冗余括号
1. 通过SQL注释指定SQL Hint

### 柔性事务提升
1. TCC

### 运维工具
1. 字典表复制广播
1. 建表工具
1. 动态扩容
