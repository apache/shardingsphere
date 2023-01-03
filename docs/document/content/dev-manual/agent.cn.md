+++
pre = "<b>5.14. </b>"
title = "可观察性"
weight = 14
chapter = true
+++

## PluginBootService

### 全限定类名

[`org.apache.shardingsphere.agent.spi.PluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/api/src/main/java/org/apache/shardingsphere/agent/spi/PluginBootService.java)

### 定义

插件启动服务定义接口

### 已知实现

| *配置标识*      | *详细说明*                         | *全限定类名* |
| ------------- | --------------------------------- | ---------- |
| Prometheus    | Prometheus plugin 启动类           | [`org.apache.shardingsphere.agent.plugin.metrics.prometheus.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/metrics/type/prometheus/src/main/java/org/apache/shardingsphere/agent/plugin/metrics/prometheus/PrometheusPluginBootService.java) |
| Logging       | Logging plugin 启动类              | [`org.apache.shardingsphere.agent.plugin.logging.file.FileLoggingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/logging/type/file/src/main/java/org/apache/shardingsphere/agent/plugin/logging/file/FileLoggingPluginBootService.java) |
| Jaeger        | Jaeger plugin 启动类               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.JaegerTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/JaegerTracingPluginBootService.java) |
| OpenTelemetry | OpenTelemetryTracing plugin 启动类 | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.OpenTelemetryTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/OpenTelemetryTracingPluginBootService.java) |
| OpenTracing   | OpenTracing plugin 启动类          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.OpenTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/OpenTracingPluginBootService.java) |
| Zipkin        | Zipkin plugin 启动类               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.ZipkinTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/ZipkinTracingPluginBootService.java) |
