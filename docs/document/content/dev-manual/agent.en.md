+++
pre = "<b>6.15. </b>"
title = "Observability"
weight = 15
chapter = true
+++

## SPI Interface

| *SPI Name*       | *Description*   |
|---------------- |------------ |
| PluginDefinitionService | Agent plugin definition |
| PluginBootService | Plugin startup service definition |

## Sample
### PluginDefinitionService

| *Implementation Class*       |        *Description*       |
|-------------------------------- |----------------------- |
| PrometheusPluginDefinitionService | Prometheus plugin     |
| BaseLoggingPluginDefinitionService | Logging plugin  |
| JaegerPluginDefinitionService    | Jaeger plugin |
| OpenTelemetryTracingPluginDefinitionService    | OpenTelemetryTracing plugin |
| OpenTracingPluginDefinitionService    | OpenTracing plugin |
| ZipkinPluginDefinitionService    | Zipkin plugin |


### PluginBootService


| *Implementation Class*          | *Description*   |
|-------------------------------- |---------------- |
| PrometheusPluginBootService | Prometheus plugin startup class |
| BaseLoggingPluginBootService | Logging plugin startup class   |
| JaegerTracingPluginBootService | Jaeger plugin startup class  |
| OpenTelemetryTracingPluginBootService | OpenTelemetryTracing plugin startup class |
| OpenTracingPluginBootService | OpenTracing plugin startup class  |
| ZipkinTracingPluginBootService | Zipkin plugin startup class |
