+++
pre = "<b>5.14. </b>"
title = "Observability"
weight = 14
chapter = true
+++

## PluginBootService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.boot.PluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-api/src/main/java/org/apache/shardingsphere/agent/spi/boot/PluginBootService.java)

### Definition

Plugin startup service definition

### Implementation classes

| *Configuration Type* | *Description*                             | *Fully-qualified class name* |
| -------------------- | ----------------------------------------- | ---------------------------- |
| Prometheus           | Prometheus plugin startup class           | [`org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/service/PrometheusPluginBootService.java) |
| Logging              | Logging plugin startup class              | [`org.apache.shardingsphere.agent.plugin.logging.base.service.BaseLoggingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-logging/shardingsphere-agent-logging-base/src/main/java/org/apache/shardingsphere/agent/plugin/logging/base/service/BaseLoggingPluginBootService.java) |
| Jaeger               | Jaeger plugin startup class               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.service.JaegerTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/service/JaegerTracingPluginBootService.java) |
| OpenTelemetry        | OpenTelemetryTracing plugin startup class | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.service.OpenTelemetryTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/service/OpenTelemetryTracingPluginBootService.java) |
| OpenTracing          | OpenTracing plugin startup class          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.service.OpenTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/service/OpenTracingPluginBootService.java) |
| Zipkin               | Zipkin plugin startup class               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.service.ZipkinTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/service/ZipkinTracingPluginBootService.java) |

## PluginDefinitionService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.definition.PluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-api/src/main/java/org/apache/shardingsphere/agent/spi/definition/PluginDefinitionService.java)

### Definition

Agent plugin definition

### Implementation classes

| *Configuration Type* | *Description*                          | *Fully-qualified class name* |
| -------------------- |--------------------------------------- | ---------------------------- |
| Prometheus           | Prometheus plugin definition           | [`org.apache.shardingsphere.agent.metrics.prometheus.definition.PrometheusPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/definition/PrometheusPluginDefinitionService.java) |
| Logging              | Logging plugin definition              | [`org.apache.shardingsphere.agent.plugin.logging.base.definition.BaseLoggingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-logging/shardingsphere-agent-logging-base/src/main/java/org/apache/shardingsphere/agent/plugin/logging/base/definition/BaseLoggingPluginDefinitionService.java) |
| Jaeger               | Jaeger plugin definition               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.definition.JaegerPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/definition/JaegerPluginDefinitionService.java) |
| OpenTelemetry        | OpenTelemetryTracing plugin definition | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.definition.OpenTelemetryTracingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/definition/OpenTelemetryTracingPluginDefinitionService.java) |
| OpenTracing          | OpenTracing plugin definition          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.definition.OpenTracingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/definition/OpenTracingPluginDefinitionService.java) |
| Zipkin               | Zipkin plugin definition               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.definition.ZipkinPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/definition/ZipkinPluginDefinitionService.java) |
