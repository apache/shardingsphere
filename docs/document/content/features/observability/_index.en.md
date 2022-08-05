+++
pre = "<b>3.12. </b>"
title = "Observability"
weight = 12
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

## Related References

[Special API: Observability](/en/user-manual/shardingsphere-jdbc/special-api/observability/)
