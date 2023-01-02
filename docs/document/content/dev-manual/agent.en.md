+++
pre = "<b>5.14. </b>"
title = "Observability"
weight = 14
chapter = true
+++

## PluginBootService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.boot.PluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/api/src/main/java/org/apache/shardingsphere/agent/spi/boot/PluginBootService.java)

### Definition

Plugin startup service definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| Prometheus           | Prometheus plugin startup class           | [`org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/metrics/prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/service/PrometheusPluginBootService.java) |
| Logging              | Logging plugin startup class              | [`org.apache.shardingsphere.agent.plugin.logging.base.service.FileLoggingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/logging/base/src/main/java/org/apache/shardingsphere/agent/plugin/logging/file/service/FileLoggingPluginBootService.java) |
| Jaeger               | Jaeger plugin startup class               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.service.JaegerTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/service/JaegerTracingPluginBootService.java) |
| OpenTelemetry        | OpenTelemetryTracing plugin startup class | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.service.OpenTelemetryTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/service/OpenTelemetryTracingPluginBootService.java) |
| OpenTracing          | OpenTracing plugin startup class          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.service.OpenTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/service/OpenTracingPluginBootService.java) |
| Zipkin               | Zipkin plugin startup class               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.service.ZipkinTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/service/ZipkinTracingPluginBootService.java) |
