+++
pre = "<b>5.9. </b>"
title = "Observability"
weight = 9
chapter = true
+++

## PluginLifecycleService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.PluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/api/src/main/java/org/apache/shardingsphere/agent/spi/PluginLifecycleService.java)

### Definition

Plug lifecycle management interface

### Implementation classes

| *Configuration Type* | *Description*                                        | *Fully-qualified class name*                                                                                                                                                                                                                                                                                                          |
|----------------------|------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| File                 | Logging plug lifecycle management class              | [`org.apache.shardingsphere.agent.plugin.logging.file.FileLoggingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/logging/type/file/src/main/java/org/apache/shardingsphere/agent/plugin/logging/file/FileLoggingPluginLifecycleService.java)                                              |
| Prometheus           | Prometheus plug lifecycle management class           | [`org.apache.shardingsphere.agent.plugin.metrics.prometheus.PrometheusPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/metrics/type/prometheus/src/main/java/org/apache/shardingsphere/agent/plugin/metrics/prometheus/PrometheusPluginLifecycleService.java)                              |
| OpenTelemetry        | OpenTelemetryTracing plug lifecycle management class | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.OpenTelemetryTracingPluginLifecycleService`](https://github.com/apache/shardingsphere/blob/master/agent/plugins/tracing/type/opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/OpenTelemetryTracingPluginLifecycleService.java) |
