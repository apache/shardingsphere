+++
pre = "<b>1. </b>"
title = "Overview"
weight = 1
chapter = true
+++

[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg?style=social&label=Release)](https://github.com/shardingjdbc/sharding-jdbc/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/shardingjdbc/sharding-jdbc.svg?style=social&label=Star)](https://github.com/shardingjdbc/sharding-jdbc/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/shardingjdbc/sharding-jdbc.svg?style=social&label=Fork)](https://github.com/shardingjdbc/sharding-jdbc/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/shardingjdbc/sharding-jdbc.svg?style=social&label=Watch)](https://github.com/shardingjdbc/sharding-jdbc/watchers)

Sharding-Sphere is an open-sourced distributed database middleware solution suite, which is composite by Sharding-JDBC, Sharding-Proxy and Sharding-Sidecar. Those 3 projects provide consistent features which include data sharding, read-write splitting, orchestration and B.A.S.E transaction. They can suitable for various scenario.

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://badges.gitter.im/Sharding-JDBC/shardingjdbc.svg)](https://gitter.im/Sharding-JDBC/shardingjdbc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.png?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Coverage Status](https://codecov.io/github/shardingjdbc/sharding-jdbc/coverage.svg?branch=master)](https://codecov.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

![Sharding-Sphere Score](http://ovfotjrsi.bkt.clouddn.com/sphere_scope_en.png)

## Introduction

### Sharding-JDBC

Use JDBC connect databases without redirect cost for java application, best performance for production.

* ORM compatible. JPA, Hibernate, Mybatis, Spring JDBC Template or JDBC supported.
* Connection-pool compatible. DBCP, C3P0, BoneCP, Druid supported.
* Multi SQL-based databases compatible. Any Database supported theoretically. Support MySQL, Oracle, SQLServer and PostgreSQL right now.

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-jdbc-brief.png)

### Sharding-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-proxy-2.1.0-SNAPSHOT-assembly-v1.tar.gz)

It is a database proxy. Deploy as a stateless server, support MySQL protocol for now.

* Use standard MySQL protocol, application do not care about whether proxy or real MySQL.
* Any MySQL command line and UI workbench supported in theoretically. MySQL Workbench are fully compatible right now.

![Sharding-Proxy Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-proxy-brief.png)

### Sharding-Sidecar(TBD)

It can mesh interactions between applications and databases, must run in Kubernetes or Mesos environment.
It is a centre-less solution, can support any languages, we call it as `Database Mesh`.

Database Mesh is focused on how to connect the distributed data-access-layer and databases together. It pays more attention on interaction, which means the messy interaction among the applications and databases will be effectively orchestrate. By using Database Mesh, applications and databases will form a large grid system, and they just need to be put into the right position on grid system accordingly.

![Sharding-Sidecar Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-sidecar-brief.png)

|                        | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ---------------------- | --------------- | ---------------- | ------------------ |
| Database               | Any             | MySQL            | MySQL              |
| Connections Cost       | More            | Less             | More               |
| Heterogeneous Language | Java Only       | Any              | Any                |
| Performance            | Low loss        | High loss        | Low loss           |
| Centre-less            | Yes             | No               | No                 |
| Static Entry           | No              | Yes              | No                 |

## Features

### Data sharding
* Both databases and tables sharding.
* Standard aggregation functions, GROUP BY, ORDER BY, LIMIT and JOIN DQL supported.
* Standard DML, DDL, TCL and database administrator command supported.
* Sharding operator `=`, `BETWEEN` and `IN` supported.
* Sharding algorithm customization and inline expression supported.
* Force route by hint.
* Distributed sequence.

### Read-write splitting
* Multiple slaves replica. 
* Data consistency guarantee in same thread.
* Mix read-write splitting and data sharding.
* Force route by hint.

### BASE Transaction
* Best efforts delivery transaction.
* Try confirm cancel transaction (TBD).

### Orchestration
* Configuration center, can refresh dynamically.
* Circuit breaker.
* Open tracing supported.
