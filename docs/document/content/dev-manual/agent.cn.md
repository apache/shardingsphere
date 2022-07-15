+++
pre = "<b>6.15. </b>"
title = "可观察性"
weight = 15
chapter = true
+++

## PluginBootService

### 全限定类名

[`org.apache.shardingsphere.agent.spi.boot.PluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-api/src/main/java/org/apache/shardingsphere/agent/spi/boot/PluginBootService.java)

### 定义

插件启动服务定义接口

### 已知实现

| *已知实现类*                            | *详细说明*                         | *全限定类名* |
| ------------------------------------- | --------------------------------- | ---------- |
| PrometheusPluginBootService           | Prometheus plugin 启动类           | [`org.apache.shardingsphere.agent.metrics.prometheus.service.PrometheusPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/service/PrometheusPluginBootService.java) |
| BaseLoggingPluginBootService          | Logging plugin 启动类              | [`org.apache.shardingsphere.agent.plugin.logging.base.service.BaseLoggingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-logging/shardingsphere-agent-logging-base/src/main/java/org/apache/shardingsphere/agent/plugin/logging/base/service/BaseLoggingPluginBootService.java) |
| JaegerTracingPluginBootService        | Jaeger plugin 启动类               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.service.JaegerTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/service/JaegerTracingPluginBootService.java) |
| OpenTelemetryTracingPluginBootService | OpenTelemetryTracing plugin 启动类 | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.service.OpenTelemetryTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/service/OpenTelemetryTracingPluginBootService.java) |
| OpenTracingPluginBootService          | OpenTracing plugin 启动类          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.service.OpenTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/service/OpenTracingPluginBootService.java) |
| ZipkinTracingPluginBootService        | Zipkin plugin 启动类               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.service.ZipkinTracingPluginBootService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/service/ZipkinTracingPluginBootService.java) |

## PluginDefinitionService

### 全限定类名

[`org.apache.shardingsphere.agent.spi.definition.PluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-api/src/main/java/org/apache/shardingsphere/agent/spi/definition/PluginDefinitionService.java)

### 定义

探针插件定义服务接口

### 已知实现

| *已知实现类*                                  | *详细说明*                   | *全限定类名*                   |
| ------------------------------------------- | --------------------------- | --------------------------- |
| PrometheusPluginDefinitionService           | Prometheus 插件定义           | [`org.apache.shardingsphere.agent.metrics.prometheus.definition.PrometheusPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/definition/PrometheusPluginDefinitionService.java) |
| BaseLoggingPluginDefinitionService          | Logging 插件定义              | [`org.apache.shardingsphere.agent.plugin.logging.base.definition.BaseLoggingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-logging/shardingsphere-agent-logging-base/src/main/java/org/apache/shardingsphere/agent/plugin/logging/base/definition/BaseLoggingPluginDefinitionService.java) |
| JaegerPluginDefinitionService               | Jaeger 插件定义               | [`org.apache.shardingsphere.agent.plugin.tracing.jaeger.definition.JaegerPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-jaeger/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/jaeger/definition/JaegerPluginDefinitionService.java) |
| OpenTelemetryTracingPluginDefinitionService | OpenTelemetryTracing 插件定义 | [`org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.definition.OpenTelemetryTracingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentelemetry/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentelemetry/definition/OpenTelemetryTracingPluginDefinitionService.java) |
| OpenTracingPluginDefinitionService          | OpenTracing 插件定义          | [`org.apache.shardingsphere.agent.plugin.tracing.opentracing.definition.OpenTracingPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-opentracing/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/opentracing/definition/OpenTracingPluginDefinitionService.java) |
| ZipkinPluginDefinitionService               | Zipkin 插件定义               | [`org.apache.shardingsphere.agent.plugin.tracing.zipkin.definition.ZipkinPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-tracing/shardingsphere-agent-tracing-zipkin/src/main/java/org/apache/shardingsphere/agent/plugin/tracing/zipkin/definition/ZipkinPluginDefinitionService.java) |
