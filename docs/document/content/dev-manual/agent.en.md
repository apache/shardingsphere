+++
pre = "<b>5.14. </b>"
title = "Observability"
weight = 14
chapter = true
+++

## PluginLifecycleService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.PluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/api/src/main/java/org/apache/shardingsphere/agent/spi/PluginLifecycleService.java)

### Definition

Plug lifecycle management interface

### Implementation classes

| *Configuration Type* | *Description*                                        | *Fully-qualified class name* |
| -------------------- | ---------------------------------------------------- | ---------------------------- |
| Prometheus           | Prometheus plug lifecycle management class           | [`org.apache.shardingsphere.agent.plugin.metrics.prometheus.PrometheusPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/metrics/type/prometheus/src/main/java/org/apache/shardingsphere/agent/plugin/metrics/prometheus/PrometheusPluginLifecycleService.java) |
| File                 | Logging plug lifecycle management class              | [`org.apache.shardingsphere.agent.plugin.logging.file.FileLoggingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/logging/type/file/src/main/java/org/apache/shardingsphere/agent/plugin/logging/file/FileLoggingPluginLifecycleService.java) |
| Jaeger               | Jaeger plug lifecycle management class               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.JaegerTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/JaegerTracingPluginLifecycleService.java) |
| OpenTelemetry        | OpenTelemetryTracing plug lifecycle management class | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.OpenTelemetryTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/OpenTelemetryTracingPluginLifecycleService.java) |
| OpenTracing          | OpenTracing plug lifecycle management class          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.OpenTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/OpenTracingPluginLifecycleService.java) |
| Zipkin               | Zipkin plug lifecycle management class               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.ZipkinTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/ZipkinTracingPluginLifecycleService.java) |
