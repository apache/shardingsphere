+++
pre = "<b>6.14. </b>"
title = "可观察性"
weight = 14
chapter = true
+++

## SPI 接口

| *SPI 名称*       | *详细说明*   |
|---------------- |------------ |
| PluginDefinitionService | Agent 插件定义 |
| PluginBootService | 插件启动服务定义 |

## 示例
### PluginDefinitionService

| *已知实现类*                      | *详细说明*              |
|-------------------------------- |----------------------- |
| PrometheusPluginDefinitionService | Prometheus plugin     |
| BaseLoggingPluginDefinitionService | Logging plugin  |
| JaegerPluginDefinitionService    | Jaeger plugin |
| OpenTelemetryTracingPluginDefinitionService    | OpenTelemetryTracing plugin |
| OpenTracingPluginDefinitionService    | OpenTracing plugin |
| ZipkinPluginDefinitionService    | Zipkin plugin |


### PluginBootService

| *已知实现类*                      | *详细说明*              |
|-------------------------------- |----------------------- |
| PrometheusPluginBootService | Prometheus plugin 启动类 |
| BaseLoggingPluginBootService | Logging plugin 启动类   |
| JaegerTracingPluginBootService | Jaeger plugin 启动类  |
| OpenTelemetryTracingPluginBootService | OpenTelemetryTracing plugin 启动类 |
| OpenTracingPluginBootService | OpenTracing plugin 启动类  |
| ZipkinTracingPluginBootService | Zipkin plugin 启动类 |
