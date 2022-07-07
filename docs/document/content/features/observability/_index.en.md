+++
pre = "<b>4.11. </b>"
title = "Observability"
weight = 11
+++

## Definition
Observing a cluster's operation status in order to quickly grasp the system's current status and efficiently be able to carry out maintenance work, represents a new challenge for distributed systems.

The point-to-point operation and maintenance method of logging into a specific server cannot be applied to scenarios facing a large number of distributed servers.

Telemetry of system-observable data is the recommended way of operating and maintaining distributed systems.

## Related Concepts

### Agent
Based on bytecode enhancement and plugin design to provide tracing, metrics and logging features.

Only after the plugin of the Agent is enabled, the monitoring indicator data can be output to the third-party APM for display.

### APM
APM is an acronym for Application Performance Monitoring.

Focusing on the performance diagnosis of distributed systems, its main functions include call chain display, application topology analysis, etc.

### Tracing
Tracing data between distributed services or internal processes will be collected by agent. It will then be sent to third-party APM systems.

### Metrics
System statistical indicators are collected through probes and written to the time series database for display by third-party applications.

### Logging
The log can be easily expanded through the agent to provide more information for analyzing the system running status.

## How it works
ShardingSphere-Agent module provides an observable framework for ShardingSphere, which is implemented based on Java Agent.

Metrics, tracing and logging functions are integrated into the agent through plugins, as shown in the following figure:

![Overview](https://shardingsphere.apache.org/document/current/img/apm/overview_v4.png)

- The Metrics plugin is used to collect and display statistical indicators for the entire cluster. Apache ShardingSphere supports Prometheus by default.
- The tracing plugin is used to obtain the link trace information of SQL parsing and SQL execution. Apache ShardingSphere provides support for Jaeger, OpenTelemetry, OpenTracing(SkyWalking) and Zipkin by default. It also supports users developing customized tracing components through plugin.
- The default logging plugin shows how to record additional logs in ShardingSphere. In practical applications, users need to explore according to their own needs.

## Related References
[Special API: Observability](/en/user-manual/shardingsphere-jdbc/special-api/observability/)
