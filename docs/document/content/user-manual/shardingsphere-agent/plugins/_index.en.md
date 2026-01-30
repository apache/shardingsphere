+++
title = "Plugins"
weight = 4
+++

## File

Currently, the File plugin only outputs the time-consuming log output of building metadata, and has no other log output for the time being.

## Prometheus

Used for exposure monitoring metrics.

* Parameter description

| Name                              | Description                                  |
|-----------------------------------|----------------------------------------------|
| host                              | host IP                                      |
| port                              | port                                         |
| jvm-information-collector-enabled | whether to collect JVM indicator information |

## OpenTelemetry

OpenTelemetry can export tracing data to Jaeger, Zipkin.

* Parameter description

| Name                               | Description     |
|------------------------------------|-----------------|
| otel.service.name                  | service name    |
| otel.traces.exporter               | traces exporter |
| otel.exporter.otlp.traces.endpoint | traces endpoint |
| otel.traces.sampler                | traces sampler  |

Parameter reference [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)
