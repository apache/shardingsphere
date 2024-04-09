+++
pre = "<b>5.9. </b>"
title = "可观察性"
weight = 9
chapter = true
+++

## PluginLifecycleService

### 全限定类名

[`org.apache.shardingsphere.agent.spi.PluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/api/src/main/java/org/apache/shardingsphere/agent/spi/PluginLifecycleService.java)

### 定义

插件生命周期管理接口

### 已知实现

| *配置标识*        | *详细说明*                  | *全限定类名*                                                                                                                                                                                                                                                                                                                               |
|---------------|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| File          | File 插件生命周期管理类          | [`org.apache.shardingsphere.agent.plugin.logging.file.FileLoggingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/logging/type/file/src/main/java/org/apache/shardingsphere/agent/plugin/logging/file/FileLoggingPluginLifecycleService.java)                                              |
| Prometheus    | Prometheus 插件生命周期管理类    | [`org.apache.shardingsphere.agent.plugin.metrics.prometheus.PrometheusPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/metrics/type/prometheus/src/main/java/org/apache/shardingsphere/agent/plugin/metrics/prometheus/PrometheusPluginLifecycleService.java)                              |
| OpenTelemetry | OpenTelemetry 插件生命周期管理类 | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.OpenTelemetryTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/OpenTelemetryTracingPluginLifecycleService.java) |
