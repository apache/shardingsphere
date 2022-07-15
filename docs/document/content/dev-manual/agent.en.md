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
| PrometheusPluginBootService           | Prometheus plugin startup class           | TODO |
| BaseLoggingPluginBootService          | Logging plugin startup class              | TODO |
| JaegerTracingPluginBootService        | Jaeger plugin startup class               | TODO |
| OpenTelemetryTracingPluginBootService | OpenTelemetryTracing plugin startup class | TODO |
| OpenTracingPluginBootService          | OpenTracing plugin startup class          | TODO |
| ZipkinTracingPluginBootService        | Zipkin plugin startup class               | TODO |

## PluginDefinitionService

### Fully-qualified class name

[`org.apache.shardingsphere.agent.spi.definition.PluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-api/src/main/java/org/apache/shardingsphere/agent/spi/definition/PluginDefinitionService.java)

### Definition

Agent plugin definition

### Implementation classes

| *Implementation Classes*                    |        *Description*                   | *Fully-qualified class name* |
| ------------------------------------------- |--------------------------------------- | ---------------------------- |
| PrometheusPluginDefinitionService           | Prometheus plugin definition           | [`org.apache.shardingsphere.agent.metrics.prometheus.definition.PrometheusPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/definition/PrometheusPluginDefinitionService.java) |
| BaseLoggingPluginDefinitionService          | Logging plugin definition              | TODO |
| JaegerPluginDefinitionService               | Jaeger plugin definition               | TODO |
| OpenTelemetryTracingPluginDefinitionService | OpenTelemetryTracing plugin definition | TODO |
| OpenTracingPluginDefinitionService          | OpenTracing plugin definition          | TODO |
| ZipkinPluginDefinitionService               | Zipkin plugin definition               | TODO |
