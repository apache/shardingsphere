+++
pre = "<b>7.9. </b>"
title = "可观察性"
weight = 9
+++

## 原理说明

ShardingSphere-Agent 模块为 ShardingSphere 提供了可观察性的框架，它是基于 Java Agent 技术实现的。

Metrics、Tracing 和 Logging 等功能均通过插件的方式集成在 Agent 中，如图：

![Overview](https://shardingsphere.apache.org/document/current/img/apm/overview_v3.png)

- Metrics 插件用于收集和展示整个集群的统计指标。Apache ShardingSphere 默认提供了对 Prometheus 的支持。
- Tracing 插件用于获取 SQL 解析与 SQL 执行的链路跟踪信息。Apache ShardingSphere 默认提供了对 Jaeger、OpenTelemetry、OpenTracing（SkyWalking）和 Zipkin 的支持，也支持用户通过插件化的方式开发自定义的 Tracing 组件。
- 默认的 Logging 插件展示了如何在 ShardingSphere 中记录额外的日志，实际应用中需要用户根据自己的需求进行探索。
