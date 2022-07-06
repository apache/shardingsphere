+++
pre = "<b>4.11. </b>"
title = "可观察性"
weight = 11
+++

## 定义
如何观测集群的运行状态，使运维人员可以快速掌握当前系统现状，并进行进一步的维护工作，是分布式系统的全新挑战。
登录到具体服务器的点对点运维方式，无法适用于面向大量分布式服务器的场景。
通过对可系统观察性数据的遥测是分布式系统推荐的运维方式。

## 相关概念

### Agent
基于字节码增强和插件化设计，以提供 Tracing 和 Metrics 埋点，以及日志输出功能。
需要开启 Agent 的插件功能后，才能将监控指标数据输出至第三方 APM 中展示。

### APM
APM 是应用性能监控的缩写。
着眼于分布式系统的性能诊断，其主要功能包括调用链展示，应用拓扑分析等。

### Tracing
链路跟踪，通过探针收集调用链数据，并发送到第三方 APM 系统。

### Metrics
系统统计指标，通过探针收集，并且写入到时序数据库，供第三方应用展示。

### Logging
日志，通过 Agent 能够方便的扩展日志内容，为分析系统运行状态提供更多信息。

 ## 原理介绍
ShardingSphere-Agent 模块为 ShardingSphere 提供了可观察性的框架，它是基于 Java Agent 技术实现的。
Metrics、Tracing 和 Logging 等功能均通过插件的方式集成在 Agent 中，如图：

![Overview](https://shardingsphere.apache.org/document/current/img/apm/overview_v4.png)

- Metrics 插件用于收集和展示整个集群的统计指标。Apache ShardingSphere 默认提供了对 Prometheus 的支持。
- Tracing 插件用于获取 SQL 解析与 SQL 执行的链路跟踪信息。Apache ShardingSphere 默认提供了对 Jaeger、OpenTelemetry、OpenTracing（SkyWalking）和 Zipkin 的支持，也支持用户通过插件化的方式开发自定义的 Tracing 组件。
- 默认的 Logging 插件展示了如何在 ShardingSphere 中记录额外的日志，实际应用中需要用户根据自己的需求进行探索。

## 相关参考
[特殊 API：可观察性](/cn/user-manual/shardingsphere-jdbc/special-api/observability/)
