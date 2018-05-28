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

### Using SkyWalking

Please refer to [SkyWalking Manual](https://github.com/OpenSkywalking/skywalking/wiki/Quick-start-chn)。

### Using OpenTracing

If user want to use other APM systems which support [OpenTracing] (http://opentracing.io), just use sharding-sphere API to work with those APM systems.

*Notices: When using SkyWalking's OpenTracing monitor agent, disabling the original Sharding-Sphere monitor agent plugin is necessary to avoid conflicting with each other.*

## UI

### Application schema

The application is a ` SpringBoot ` application, using ` Sharding-Sphere ` to access two databases of `ds_0` and `ds_1`, each owns two tables in database.

### Topology diagram

![The topology diagram](http://ovfotjrsi.bkt.clouddn.com/apm/apm-topology.png)

Although the user accesses the application once, each database is accessed twice. This is because this visit involves two splitting tables in each database, four tables in total.

### Tracking diagram

![The topology diagram](http://ovfotjrsi.bkt.clouddn.com/apm/apm-trace.png)

Four visits in total in this figure.

`/SJDBC/TRUNK/*`: Represents the overall execution performance of this SQL.


![The logical execution node](http://ovfotjrsi.bkt.clouddn.com/apm/apm-trunk-span.png)

`/SJDBC/BRANCH/*`: Represents the performance of the actual SQL.

![The actual access node](http://ovfotjrsi.bkt.clouddn.com/apm/apm-branch-span.png)
