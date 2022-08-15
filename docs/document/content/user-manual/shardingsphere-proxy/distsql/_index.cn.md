+++
title = "DistSQL"
weight = 3
chapter = true
+++

本章节将介绍 DistSQL 的详细语法。

## 定义

DistSQL（Distributed SQL）是 Apache ShardingSphere 特有的操作语言。 它与标准 SQL 的使用方式完全一致，用于提供增量功能的 SQL 级别操作能力。

灵活的规则配置和资源管控能力是 Apache ShardingSphere 的特点之一。

在使用 4.x 及其之前版本时，开发者虽然可以像使用原生数据库一样操作数据，但却需要通过本地文件或注册中心配置资源和规则。然而，操作习惯变更，对于运维工程师并不友好。

从 5.x 版本开始，DistSQL（Distributed SQL）让用户可以像操作数据库一样操作 Apache ShardingSphere，使其从面向开发人员的框架和中间件转变为面向运维人员的数据库产品。

## 相关概念

DistSQL 细分为 RDL、RQL、RAL 和 RUL 四种类型。

### RDL

Resource & Rule Definition Language，负责资源和规则的创建、修改和删除。

### RQL

Resource & Rule Query Language，负责资源和规则的查询和展现。

### RAL

Resource & Rule Administration Language，负责强制路由、熔断、配置导入导出、数据迁移控制等管理功能。

### RUL

Resource & Rule Utility Language，负责 SQL 解析、SQL 格式化、执行计划预览等功能。

## 对系统的影响

### 之前

在拥有 DistSQL 以前，用户一边使用 SQL 语句操作数据，一边使用 YAML 文件来管理 ShardingSphere 的配置，如下图：

![Before](https://shardingsphere.apache.org/document/current/img/distsql/before.png)

这时用户不得不面对以下几个问题：
- 需要通过不同类型的客户端来操作数据和管理 ShardingSphere 规则；
- 多个逻辑库需要多个 YAML 文件；
- 修改 YAML 需要文件的编辑权限；
- 修改 YAML 后需要重启 ShardingSphere。

### 之后

随着 DistSQL 的出现，对 ShardingSphere 的操作方式也得到了改变：

![After](https://shardingsphere.apache.org/document/current/img/distsql/after.png)

现在，用户的使用体验得到了巨大改善：
- 使用相同的客户端来管理数据和 ShardingSphere 配置；
- 不再额外创建 YAML 文件，通过 DistSQL 管理逻辑库；
- 不再需要文件的编辑权限，通过 DistSQL 来管理配置；
- 配置的变更实时生效，无需重启 ShardingSphere。

## 使用限制

DistSQL 只能用于 ShardingSphere-Proxy，ShardingSphere-JDBC 暂不提供。

## 原理介绍

与标准 SQL 一样，DistSQL 由 ShardingSphere 的解析引擎进行识别，将输入语句转换为抽象语法树，进而生成各个语法对应的 `Statement`，最后由合适的 `Handler` 进行业务处理。
整体流程如下图所示：

![Overview](https://shardingsphere.apache.org/document/current/img/distsql/overview.png)

## 相关参考

[用户手册：DistSQL](/cn/user-manual/shardingsphere-proxy/distsql/)
