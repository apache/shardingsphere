# [Sharding-Sphere - Distributed database middleware ecosphere](http://shardingsphere.io/)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)
[![GitHub release](https://img.shields.io/github/release/sharding-sphere/sharding-sphere.svg)](https://github.com/sharding-sphere/sharding-sphere/releases)

[![Build Status](https://api.travis-ci.org/sharding-sphere/sharding-sphere.png?branch=master)](https://travis-ci.org/sharding-sphere/sharding-sphere)
[![Coverage Status](https://codecov.io/github/sharding-sphere/sharding-sphere/coverage.svg?branch=master)](https://codecov.io/github/sharding-sphere/sharding-sphere?branch=master)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

## Document

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](http://shardingsphere.io/document/en/)
[![Roadmap](https://img.shields.io/badge/roadmap-English-blue.svg)](ROADMAP.md)

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](http://shardingsphere.io/document/cn/)

## Overview

Sharding-Sphere is an open-sourced distributed database middleware solution suite, which is composite by Sharding-JDBC, Sharding-Proxy and Sharding-Sidecar. Those 3 projects provide consistent features which include data sharding, read-write splitting, orchestration and B.A.S.E transaction. They can suitable for various scenario.

Sharding-Sphere is a Relational Database Middleware which reuse capacity of original databases (such as: compute, storage), but make best simplicity and efficiency on distribute environment. Sharding-Sphere do not want implement a new database.
It can cooperate with NoSQL and NewSQL. NoSQL and NewSQL are good exploration for technology, they are care about what’s going to change. There is another theory which is care about what’s not going to change. Relational Database still is the biggest percentage of market share, it is difficult to estimate trendy in future. So we are focus about how to enhance with Relational Database now.

![Sharding-Sphere Score](http://ovfotjrsi.bkt.clouddn.com/sphere_scope_en.png)

### Sharding-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)

Use JDBC connect databases without redirect cost for java application, best performance for production.

* ORM compatible. JPA, Hibernate, Mybatis, Spring JDBC Template or JDBC supported.
* Connection-pool compatible. DBCP, C3P0, BoneCP, Druid supported.
* Multi SQL-based databases compatible. Any Database supported theoretically. Support MySQL, Oracle, SQLServer and PostgreSQL right now.

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-jdbc-brief.png)

### Sharding-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/sharding-sphere/sharding-sphere-doc/raw/master/dist/sharding-proxy-3.0.0.M1.tar.gz)

It is a database proxy. Deploy as a stateless server, support MySQL protocol for now.

* Use standard MySQL protocol, application do not care about whether proxy or real MySQL.
* Any MySQL command line and UI workbench supported in theoretically. MySQL Workbench are fully compatible right now.

![Sharding-Proxy Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-proxy-brief_v2.png)

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
* Aggregation functions, GROUP BY, ORDER BY, LIMIT, OR, and JOIN DQL supported.
* DML, DDL, TCL and database administrator command supported.
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
