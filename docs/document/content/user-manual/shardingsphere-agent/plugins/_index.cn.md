+++
title = "插件"
weight = 4
+++



## File

目前 File 插件只有构建元数据耗时日志输出，暂无其他日志输出。

## Prometheus

用于暴露监控指标

* 参数说明

| 名称                               | 说明                |
|-----------------------------------|---------------------|
| host                              | 主机                 |
| port                              | 端口                 |
| jvm-information-collector-enabled | 是否采集 JVM 指标信息  |

## OpenTelemetry

OpenTelemetry 可以导出 tracing 数据到 Jaeger，Zipkin。

* 参数说明

| 名称                                  | 说明                |
|-------------------------------------|----------------------|
| otel.service.name                   | 服务名称              |
| otel.traces.exporter                | traces exporter      |
| otel.exporter.otlp.traces.endpoint  | traces endpoint      |
| otel.traces.sampler                 | traces sampler       |

参数参考 [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)
