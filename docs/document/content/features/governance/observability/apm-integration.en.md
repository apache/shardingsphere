+++
title = "APM Integration"
weight = 1
+++

## Background

APM is the abbreviation for application performance monitoring. 
Currently, main APM functions lie in the performance diagnosis of distributed systems, including chain demonstration, application topology analysis and so on.

Apache ShardingSphere is not responsible for gathering, storing and demonstrating APM data, but sends the core information of SQL parsing and enforcement to APM to process. 
In other words, Apache ShardingSphere is only responsible for generating valuable data and submitting it to relevant systems through standard protocol. 
It can connect to APM systems in two ways.

The first way is to send performance tracing data by OpenTracing API. 
APM products facing OpenTracing protocol can all automatically connect to Apache ShardingSphere, like SkyWalking, Zipkin and Jaeger. 
In this way, users only need to configure the implementation of OpenTracing protocol at the start. 
Its advantage is the compatibility of all the products compatible of OpenTracing protocol, such as the APM demonstration system. 
If companies intend to implement their own APM systems, they only need to implement the OpenTracing protocol, and they can automatically show the chain tracing information of Apache ShardingSphere. 
Its disadvantage is that OpenTracing protocol is not stable in its development, has only a few new versions, and is too neutral to support customized products as native ones do.

The second way is to use SkyWalking's automatic monitor agent. 
Cooperating with [Apache SkyWalking](https://skywalking.apache.org/) team, 
Apache ShardingSphere team has realized `ShardingSphere` automatic monitor agent to automatically send application performance data to `SkyWalking`.

## Usage

### Use OpenTracing

* Method 1: inject Tracer provided by APM system through reading system parameters

Add startup arguments

```
-Dorg.apache.shardingsphere.tracing.opentracing.tracer.class=org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer
```

Call initialization method

```java
ShardingTracer.init();
```

* Method 2: inject Tracer provided by APM through parameter

```java
ShardingTracer.init(new SkywalkingTracer());
```

*Notice: when using SkyWalking OpenTracing agent, you should disable the former ShardingSphere agent plug-in to avoid the conflict between them.*

### Use SkyWalking's Automatic Agent

Please refer to [SkyWalking Manual](https://github.com/apache/skywalking/blob/5.x/docs/en/Quick-start.md).

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
