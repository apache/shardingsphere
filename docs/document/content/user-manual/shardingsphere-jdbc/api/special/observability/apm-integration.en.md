+++
title = "APM Integration"
weight = 2
+++

## Usage

### Use OpenTracing

* Method 1: inject Tracer provided by APM system through reading system parameters

Add startup arguments

```
-Dorg.apache.shardingsphere.tracing.opentracing.tracer.class=org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer
```

Call initialization method.

```java
ShardingTracer.init();
```

* Method 2: inject Tracer provided by APM through parameter.

```java
ShardingTracer.init(new SkywalkingTracer());
```

*Notice: when using SkyWalking OpenTracing agent, you should disable the former ShardingSphere agent plug-in to avoid the conflict between them.*

### Use SkyWalking's Automatic Agent

Please refer to [SkyWalking Manual](https://github.com/apache/skywalking/blob/5.x/docs/en/Quick-start.md).

### Use OpenTelemetry

Just fill in the configuration in `agent.yaml`. For example, export Traces data to Zipkin.

```yaml
OpenTelemetry:
    props:
      otel.resource.attributes: "service.name=shardingsphere-agent"
      otel.traces.exporter: "zipkin"
      otel.exporter.zipkin.endpoint: "http://127.0.0.1:9411/api/v2/spans"
```

## Result Demonstration

No matter in which way, it is convenient to demonstrate APM information in the connected system. Take SkyWalking for example:

### Application Architecture

Use `ShardingSphere-Proxy` to visit two databases, `192.168.0.1:3306` and `192.168.0.2:3306`, and there are two tables in each one of them.

### Topology

![The topology diagram](https://shardingsphere.apache.org/document/current/img/apm/5x_topology.png)

It can be seen from the picture that the user has accessed ShardingSphere-Proxy 18 times, with each database twice each time. It is because two tables in each database are accessed each time, so there are totally four tables accessed each time.

### Tracking Data

![The tracking diagram](https://shardingsphere.apache.org/document/current/img/apm/5x_trace.png)

SQL parsing and implementation can be seen from the tracing diagram.

`/Sharding-Sphere/parseSQL/` indicates the SQL parsing performance this time.

![The parsing node](https://shardingsphere.apache.org/document/current/img/apm/5x_parse.png)

`/Sharding-Sphere/executeSQL/` indicates the SQL parsing performance in actual execution.

![The actual access node](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL.png)

### Exception

![Exception tracking diagram](https://shardingsphere.apache.org/document/current/img/apm/5x_trace_err.png)

Exception nodes can be seen from the tracing diagram.

`/Sharding-Sphere/executeSQL/` indicates the exception results of SQL.

![Exception node](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL_Tags_err.png)

`/Sharding-Sphere/executeSQL/` indicates the exception log of SQL execution.

![Exception log](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL_Logs_err.png)
