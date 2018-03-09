+++
toc = true
title = "apm"
weight = 2
prev = "/04-orchestration/orchestration/"
next = "/05-transaction/"
+++

## The brief
` APM ` is the abbreviation of Application Performance Monitoring. Currently its core function is the performance diagnosis of distributed system, including call chain demonstration, application topology analysis, etc.

[Sharding-JDBC](http://shardingjdbc.io) Team work with [SkyWalking](http://skywalking.io) Team to introduce an automatic prober of `Sharding-JDBC` to send the performance data of `Sharding-JDBC` to `SkyWalking`.

## Usage

## Using the SkyWalking plugin

Please refer to [SkyWalking Manual](https://github.com/OpenSkywalking/skywalking/wiki/Quick-start-chn)。

## Using OpenTracing plugin

If you want to use other APM systems which support [OpenTracing] (http://opentracing.io), you can use the [sharding-jdbc-opentracing] (https://github.com/shardingjdbc/sharding-jdbc-opentracing/blob/master/README_ZH.md) plugin to work with
those APM systems.


*Notices: When using SkyWalking's OpenTracing probe, disabling the original ShardingJDBC probe plugin is necessary to avoid conflicting with each other.*

## Results display

### The application architecture

The application is a ` SpringBoot ` application, using ` Sharding - JDBC ` to access two databases of ` ds_0 ` and ` ds_1 `, and each owns two tables in the database.

### The topology diagram display

![The topology diagram](http://ovfotjrsi.bkt.clouddn.com/apm-topology.png)

Although the user accesses the application once, each database is accessed twice. This is because this visit involves two splitting tables in each database, four tables in total.

### Data tracking display

![The topology diagram](http://ovfotjrsi.bkt.clouddn.com/apm-trace.png)

Four visits in total in this figure.

`/SJDBC/TRUNK/*` : Represents the overall execution performance of this SQL.


![The logical execution node](http://ovfotjrsi.bkt.clouddn.com/apm-trunk-span.png)

`/SJSBC/BRANCH/*` : Represents the performance of the actual SQL.

![The actual access node](http://ovfotjrsi.bkt.clouddn.com/apm-branch-span.png)
