+++
pre = "<b>3.8. </b>"
title = "Dist SQL"
weight = 8
chapter = true
+++

## 背景

DistSQL（Distributed SQL）是 Apache ShardingSphere 特有的内置 SQL 语言，提供了标准 SQL 之外的增量功能操作能力。

## 挑战

灵活的规则配置和资源管控能力是 Apache ShardingSphere 的特点之一，在使用 ShardingSphere-Proxy 时，开发者可以像使用数据库一样操作数据，却需要通过 YAML 文件（或注册中心）配置资源和规则。
然而，YAML 格式的展现形式，以及注册中心动态修改带来的操作习惯变更，对于运维人员来说并不友好。

DistSQL 让用户可以像操作数据库一样操作 Apache ShardingSphere，使其从面向开发人员的框架和中间件转变为面向运维人员的基础设施产品。

DistSQL 划分为 RDL、RQL 和 SCTL 这三种具体类型。

 - RDL（Resource & Rule Definition Language）负责资源和规则的创建、修改和删除；
 - RQL（Resource & Rule Query Language）负责资源和规则的查询和展现；
 - SCTL（ShardingSphere Control Language）负责Hint、事务类型切换、分片执行计划查询等增量功能的操作。

## 目标

**打破中间件和数据库之间的界限，让开发者像使用数据库一样使用 Apache ShardingSphere，是 DistSQL 的设计目标。**

## 注意事项

DistSQL 只能用于 ShardingSphere-Proxy，ShardingSphere-JDBC 暂不提供。
