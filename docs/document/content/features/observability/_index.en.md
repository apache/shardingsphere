+++
pre = "<b>4.10. </b>"
title = "Observability"
weight = 10
chapter = true
+++

## Background

In order to grasp the distributed system status, observe running state of the cluster is a new challenge.
The point-to-point operation mode of logging in to a specific server cannot suite to large number of distributed servers.
Observability and telemetry are the recommended operation way for them.
APM (application performance monitoring) and metrics (statistical indicator monitoring) are important system health indicators.

APM is the abbreviation for application performance monitoring.
It works for performance diagnosis of distributed systems, including chain demonstration, application topology analysis and so on.

Apache ShardingSphere is not responsible for gathering, storing and demonstrating APM data, but sends the core information of SQL parsing and enforcement to APM to process.
In other words, Apache ShardingSphere is only responsible for generating valuable data and submitting it to relevant systems through standard protocol.
It can connect to APM systems in 3 ways.

- Use OpenTracing API

APM products facing OpenTracing protocol can all automatically connect to Apache ShardingSphere, like SkyWalking, Zipkin and Jaeger.
In this way, users only need to configure the implementation of OpenTracing protocol at the start.
Its advantage is the compatibility of all the products compatible of OpenTracing protocol, such as the APM demonstration system.
If companies intend to implement their own APM systems, they only need to implement the OpenTracing protocol, and they can automatically show the chain tracing information of Apache ShardingSphere.
Its disadvantage is that OpenTracing protocol is not stable in its development, has only a few new versions, and is too neutral to support customized products as native ones do.

- Use OpenTelemetry API 

OpenTelemetry was merged by OpenTracing and OpenCencus in 2019.
In this way, you only need to fill in the appropriate configuration in the agent configuration file according to [OpenTelemetry SDK Autoconfigure Guide](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure).

- Use SkyWalking's automatic monitor agent

Cooperating with [Apache SkyWalking](https://skywalking.apache.org/) team,
Apache ShardingSphere team has realized `ShardingSphere` automatic monitor agent to automatically send application performance data to `SkyWalking`.

Metrics used to collect and display statistical indicator of cluster. 

## Challenges

APM and metrics need to collect system information through event tracking.
Lots of events tracking make kernel code mess, difficult to maintain, and difficult to customize extend.

## Goal

The goal of Apache ShardingSphere observability module is providing as many performance and statistical indicators as possible and isolating kernel code and embedded code.
