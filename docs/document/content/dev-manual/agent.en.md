+++
pre = "<b>5.14. </b>"
title = "Observability"
weight = 14
chapter = true
+++

## PluginBootService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.PluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/api/src/main/java/org/apache/shardingsphere/agent/spi/PluginBootService.java)

### Definition

Plugin startup service definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| Prometheus           | Prometheus plugin startup class           | [`org.apache.shardingsphere.agent.plugin.metrics.prometheus.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/metrics/type/prometheus/src/main/java/org/apache/shardingsphere/agent/plugin/metrics/prometheus/PrometheusPluginBootService.java) |
| Logging              | Logging plugin startup class              | [`org.apache.shardingsphere.agent.plugin.logging.file.FileLoggingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/logging/type/file/src/main/java/org/apache/shardingsphere/agent/plugin/logging/file/FileLoggingPluginBootService.java) |
| Jaeger               | Jaeger plugin startup class               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.JaegerTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/JaegerTracingPluginBootService.java) |
| OpenTelemetry        | OpenTelemetryTracing plugin startup class | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.OpenTelemetryTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/OpenTelemetryTracingPluginBootService.java) |
| OpenTracing          | OpenTracing plugin startup class          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.OpenTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/OpenTracingPluginBootService.java) |
| Zipkin               | Zipkin plugin startup class               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.ZipkinTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/ZipkinTracingPluginBootService.java) |
