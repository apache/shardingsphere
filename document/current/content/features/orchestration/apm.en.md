+++
pre = "<b>3.3.4. </b>"
toc = true
title = "APM"
weight = 4
+++

## Background

`APM` is the abbreviation of Application Performance Monitoring. Currently its core function is the performance diagnosis of distributed system, including call chain demonstration, application topology analysis, etc.

[Sharding-Sphere](http://shardingsphere.io) Team work with [SkyWalking](http://skywalking.io) Team to introduce an automatic monitor agent of `Sharding-Sphere` to send tracing data of `Sharding-Sphere` to `SkyWalking`.

## Usage

### Using OpenTracing

* Inject the Tracer implementation class through System.properties
```
    System.Properties：-Dio.shardingsphere.opentracing.tracer.class=org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer
    mehtod：ShardingTracer.init()                          
```

* Inject the Tracer implementation class through method parameter
```
    shardingTracer.init(new SkywalkingTracer())   
```

*Notices: When using SkyWalking's OpenTracing monitor agent, disabling the original Sharding-Sphere monitor agent plugin is necessary to avoid conflicting with each other.*

### Using SkyWalking

Please refer to [SkyWalking Manual](https://github.com/apache/incubator-skywalking/blob/5.x/docs/en/Quick-start.md).

## UI

### Application schema

Using ` Sharding-Proxy ` to access two databases of `192.168.0.1:3306` and `192.168.0.2:3306`, each owns two tables in database.

### Topology diagram

![The topology diagram](http://ovfotjrsi.bkt.clouddn.com/apm/5x_topology.png)

User accesses the Sharding-Proxy 18 times, each database is accessed twice each time. This is because one access involves two splitting tables in each database, four tables in total.

### Tracking diagram

![The tracking diagram](http://ovfotjrsi.bkt.clouddn.com/apm/5x_trace.png)

You can see SQL parsing and execution in this figure.

`/Sharding-Sphere/parseSQL/`: Represents the parsing performance of this SQL.

![The parsing node](http://ovfotjrsi.bkt.clouddn.com/apm/5x_parse.png)

`/Sharding-Sphere/executeSQL/`: Represents the performance of the actual SQL.

![The actual access node](http://ovfotjrsi.bkt.clouddn.com/apm/5x_executeSQL.png)

### Exception diagram

![Exception tracking diagram](http://ovfotjrsi.bkt.clouddn.com/apm/5x_trace_err.png)

You can see Exceptions in this figure.

`/Sharding-Sphere/executeSQL/` : Represents the Exceptions of the actual SQL.

![Exception node](http://ovfotjrsi.bkt.clouddn.com/apm/5x_executeSQL_Tags_err.png)

`/Sharding-Sphere/executeSQL/` : Represents the Exception logs of the actual SQL.

![Exception log](http://ovfotjrsi.bkt.clouddn.com/apm/5x_executeSQL_Logs_err.png)
