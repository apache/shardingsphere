+++
pre = "<b>6.15. </b>"
title = "Observability"
weight = 15
chapter = true
+++

## PluginBootService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.boot.PluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-api/src/main/java/org/apache/shardingsphere/agent/spi/boot/PluginBootService.java)

### Definition

Plugin startup service definition

### Implementation classes

| *Implementation Class*                | *Description*                             | *Fully-qualified class name* |
| ------------------------------------- | ----------------------------------------- | ---------------------------- |
| PrometheusPluginBootService           | Prometheus plugin startup class           | [`org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/service/PrometheusPluginBootService.java) |
| BaseLoggingPluginBootService          | Logging plugin startup class              | [`org.apache.shardingsphere.agent.plugin.logging.base.service.BaseLoggingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-logging/shardingsphere-agent-logging-base/src/main/java/org/apache/shardingsphere/agent/plugin/logging/base/service/BaseLoggingPluginBootService.java) |
| JaegerTracingPluginBootService        | Jaeger plugin startup class               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.service.JaegerTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/service/JaegerTracingPluginBootService.java) |
| OpenTelemetryTracingPluginBootService | OpenTelemetryTracing plugin startup class | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.service.OpenTelemetryTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/service/OpenTelemetryTracingPluginBootService.java) |
| OpenTracingPluginBootService          | OpenTracing plugin startup class          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.service.OpenTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/service/OpenTracingPluginBootService.java) |
| ZipkinTracingPluginBootService        | Zipkin plugin startup class               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.service.ZipkinTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/service/ZipkinTracingPluginBootService.java) |

## PluginDefinitionService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.definition.PluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-api/src/main/java/org/apache/shardingsphere/agent/spi/definition/PluginDefinitionService.java)

### Definition

Agent plugin definition

### Implementation classes

| *Implementation Classes*                    |        *Description*                   | *Fully-qualified class name* |
| ------------------------------------------- |--------------------------------------- | ---------------------------- |
| PrometheusPluginDefinitionService           | Prometheus plugin definition           | [`org.apache.shardingsphere.agent.metrics.prometheus.definition.PrometheusPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/definition/PrometheusPluginDefinitionService.java) |
| BaseLoggingPluginDefinitionService          | Logging plugin definition              | [`org.apache.shardingsphere.agent.plugin.logging.base.definition.BaseLoggingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-logging/shardingsphere-agent-logging-base/src/main/java/org/apache/shardingsphere/agent/plugin/logging/base/definition/BaseLoggingPluginDefinitionService.java) |
| JaegerPluginDefinitionService               | Jaeger plugin definition               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.definition.JaegerPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/definition/JaegerPluginDefinitionService.java) |
| OpenTelemetryTracingPluginDefinitionService | OpenTelemetryTracing plugin definition | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.definition.OpenTelemetryTracingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/definition/OpenTelemetryTracingPluginDefinitionService.java) |
| OpenTracingPluginDefinitionService          | OpenTracing plugin definition          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.definition.OpenTracingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/definition/OpenTracingPluginDefinitionService.java) |
| ZipkinPluginDefinitionService               | Zipkin plugin definition               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.definition.ZipkinPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/definition/ZipkinPluginDefinitionService.java) |
