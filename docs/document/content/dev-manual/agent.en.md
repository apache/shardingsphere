+++
pre = "<b>6.15. </b>"
title = "agent"
weight = 15
chapter = true
+++

## PluginDefinitionService

| *SPI Name*       | *Description*   |
|---------------- |------------ |
| PluginDefinitionService | Agent plugin definition |

| *Implementation Class*       |        *Description*       |
|-------------------------------- |----------------------- |
| PrometheusPluginDefinitionService | Prometheus plugin     |
| BaseLoggingPluginDefinitionService | Logging plugin  |
| JaegerPluginDefinitionService    | Jaeger plugin |
| OpenTelemetryTracingPluginDefinitionService    | OpenTelemetryTracing plugin |
| OpenTracingPluginDefinitionService    | OpenTracing plugin |
| ZipkinPluginDefinitionService    | Zipkin plugin |


## PluginBootService

| *SPI Name*       | *Description*   |
|---------------- |------------ |
| PluginBootService | Plugin startup service definition |

| *Implementation Class*          | *Description*   |
|-------------------------------- |---------------- |
| PrometheusPluginBootService | Prometheus plugin startup class |
| BaseLoggingPluginBootService | Logging plugin startup class   |
| JaegerTracingPluginBootService | Jaeger plugin startup class  |
| OpenTelemetryTracingPluginBootService | OpenTelemetryTracing plugin startup class |
| OpenTracingPluginBootService | OpenTracing plugin startup class  |
| ZipkinTracingPluginBootService | Zipkin plugin startup class |