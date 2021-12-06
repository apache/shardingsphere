+++
pre = "<b>3.3. </b>"
title = "DistSQL"
weight = 3
chapter = true
+++

## 背景

DistSQL（Distributed SQL）是 Apache ShardingSphere 特有的操作语言。
它与标准 SQL 的使用方式完全一致，用于提供增量功能的 SQL 级别操作能力。

## 挑战

灵活的规则配置和资源管控能力是 Apache ShardingSphere 的特点之一。
在使用 4.x 及其之前版本时，开发者虽然可以像使用原生数据库一样操作数据，但却需要通过本地文件或注册中心配置资源和规则。
然而，操作习惯变更，对于运维工程师并不友好。

DistSQL 让用户可以像操作数据库一样操作 Apache ShardingSphere，使其从面向开发人员的框架和中间件转变为面向运维人员的数据库产品。

DistSQL 细分为 RDL、RQL 和 RAL 三种类型。

 - RDL（Resource & Rule Definition Language）负责资源和规则的创建、修改和删除；
 - RQL（Resource & Rule Query Language）负责资源和规则的查询和展现；
 - RAL（Resource & Rule Administration Language）负责 Hint、事务类型切换、分片执行计划查询等管理功能。

## 目标

**打破中间件和数据库之间的界限，让开发者像使用数据库一样使用 Apache ShardingSphere，是 DistSQL 的设计目标。**

## 注意事项

DistSQL 只能用于 ShardingSphere-Proxy，ShardingSphere-JDBC 暂不提供。
