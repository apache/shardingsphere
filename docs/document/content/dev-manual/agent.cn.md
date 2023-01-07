+++
pre = "<b>5.14. </b>"
title = "可观察性"
weight = 14
chapter = true
+++

## PluginLifecycleService

### 全限定类名

[`org.apache.shardingsphere.agent.spi.PluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/api/src/main/java/org/apache/shardingsphere/agent/spi/PluginLifecycleService.java)

### 定义

插件生命周期管理接口

### 已知实现

| *配置标识*      | *详细说明*                    | *全限定类名* |
| ------------- | ---------------------------- | ---------- |
| Prometheus    | Prometheus 插件生命周期管理类    | [`org.apache.shardingsphere.agent.plugin.metrics.prometheus.PrometheusPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/metrics/type/prometheus/src/main/java/org/apache/shardingsphere/agent/plugin/metrics/prometheus/PrometheusPluginLifecycleService.java) |
| File          | File 插件生命周期管理类          | [`org.apache.shardingsphere.agent.plugin.logging.file.FileLoggingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/logging/type/file/src/main/java/org/apache/shardingsphere/agent/plugin/logging/file/FileLoggingPluginLifecycleService.java) |
| Jaeger        | Jaeger 插件生命周期管理类        | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.JaegerTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/JaegerTracingPluginLifecycleService.java) |
| OpenTelemetry | OpenTelemetry 插件生命周期管理类 | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.OpenTelemetryTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/OpenTelemetryTracingPluginLifecycleService.java) |
| OpenTracing   | OpenTracing 插件生命周期管理类   | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.OpenTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/OpenTracingPluginLifecycleService.java) |
| Zipkin        | Zipkin 插件生命周期管理类        | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.ZipkinTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/ZipkinTracingPluginLifecycleService.java) |
