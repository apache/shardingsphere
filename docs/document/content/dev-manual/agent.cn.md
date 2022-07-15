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

| *已知实现类*                            | *详细说明*                         | *全限定类名*                  |
| ------------------------------------- | --------------------------------- | --------------------------- |
| PrometheusPluginBootService           | Prometheus plugin 启动类           | TODO |
| BaseLoggingPluginBootService          | Logging plugin 启动类              | TODO |
| JaegerTracingPluginBootService        | Jaeger plugin 启动类               | TODO |
| OpenTelemetryTracingPluginBootService | OpenTelemetryTracing plugin 启动类 | TODO |
| OpenTracingPluginBootService          | OpenTracing plugin 启动类          | TODO |
| ZipkinTracingPluginBootService        | Zipkin plugin 启动类               | TODO |

## PluginDefinitionService

### 全限定类名

[`org.apache.shardingsphere.agent.spi.definition.PluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-api/src/main/java/org/apache/shardingsphere/agent/spi/definition/PluginDefinitionService.java)

### 定义

探针插件定义服务接口

### 已知实现

| *已知实现类*                                  | *详细说明*                   | *全限定类名*                   |
| ------------------------------------------- | --------------------------- | --------------------------- |
| PrometheusPluginDefinitionService           | Prometheus 插件定义           | [`org.apache.shardingsphere.agent.metrics.prometheus.definition.PrometheusPluginDefinitionService`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-agent/shardingsphere-agent-plugins/shardingsphere-agent-plugin-metrics/shardingsphere-agent-metrics-prometheus/src/main/java/org/apache/shardingsphere/agent/metrics/prometheus/definition/PrometheusPluginDefinitionService.java) |
| BaseLoggingPluginDefinitionService          | Logging 插件定义              | TODO |
| JaegerPluginDefinitionService               | Jaeger 插件定义               | TODO |
| OpenTelemetryTracingPluginDefinitionService | OpenTelemetryTracing 插件定义 | TODO |
| OpenTracingPluginDefinitionService          | OpenTracing 插件定义          | TODO |
| ZipkinPluginDefinitionService               | Zipkin 插件定义               | TODO |
