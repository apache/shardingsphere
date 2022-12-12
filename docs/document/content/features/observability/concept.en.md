+++
title = "Core Concept"
weight = 1
+++

## Agent

Based on bytecode enhancement and plugin design to provide tracing, metrics and logging features.

Only after the plugin of the Agent is enabled, the monitoring indicator data can be output to the third-party APM for display.

## APM

APM is an acronym for Application Performance Monitoring.

Focusing on the performance diagnosis of distributed systems, its main functions include call chain display, application topology analysis, etc.

## Tracing

Tracing data between distributed services or internal processes will be collected by agent. It will then be sent to third-party APM systems.

## Metrics

System statistical indicators are collected through probes and written to the time series database for display by third-party applications.

## Logging

The log can be easily expanded through the agent to provide more information for analyzing the system running status.
