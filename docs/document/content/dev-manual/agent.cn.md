+++
pre = "<b>5.14. </b>"
title = "可观察性"
weight = 14
chapter = true
+++

## PluginBootService

### 全限定类名

[`org.apache.shardingsphere.agent.spi.boot.PluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/api/src/main/java/org/apache/shardingsphere/agent/spi/boot/PluginBootService.java)

### 定义

插件启动服务定义接口

### 已知实现

| *配置标识*      | *详细说明*                         | *全限定类名* |
| ------------- | --------------------------------- | ---------- |
| Prometheus    | Prometheus plugin 启动类           | [`org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/metrics/prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/service/PrometheusPluginBootService.java) |
| Logging       | Logging plugin 启动类              | [`org.apache.shardingsphere.agent.plugin.logging.base.service.BaseLoggingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/logging/base/src/main/java/org/apache/shardingsphere/agent/plugin/logging/base/service/BaseLoggingPluginBootService.java) |
| Jaeger        | Jaeger plugin 启动类               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.service.JaegerTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/service/JaegerTracingPluginBootService.java) |
| OpenTelemetry | OpenTelemetryTracing plugin 启动类 | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.service.OpenTelemetryTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/service/OpenTelemetryTracingPluginBootService.java) |
| OpenTracing   | OpenTracing plugin 启动类          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.service.OpenTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/service/OpenTracingPluginBootService.java) |
| Zipkin        | Zipkin plugin 启动类               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.service.ZipkinTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/service/ZipkinTracingPluginBootService.java) |
