+++
icon = "<b>0. </b>"
title = "Overview"
weight = 0
prev = "/03-design/roadmap/"
next = "/00-overview/intro/"
chapter = true

+++

# Overview

[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg?style=social&label=Release)](https://github.com/shardingjdbc/sharding-jdbc/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/shardingjdbc/sharding-jdbc.svg?style=social&label=Star)](https://github.com/shardingjdbc/sharding-jdbc/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/shardingjdbc/sharding-jdbc.svg?style=social&label=Fork)](https://github.com/shardingjdbc/sharding-jdbc/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/shardingjdbc/sharding-jdbc.svg?style=social&label=Watch)](https://github.com/shardingjdbc/sharding-jdbc/watchers)

Sharding-JDBC: A data sharding, read-write splitting, BASE transaction and database orchestration middleware. It provides maximum compatibilities for applications by JDBC and MySQL protocol.

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-proxy-2.1.0-SNAPSHOT-assembly-v1.tar.gz)

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.png?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Coverage Status](https://codecov.io/github/shardingjdbc/sharding-jdbc/coverage.svg?branch=master)](https://codecov.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![Gitter](https://badges.gitter.im/Sharding-JDBC/shardingjdbc.svg)](https://gitter.im/Sharding-JDBC/shardingjdbc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

# Features

## 1. Data sharding
* Both databases and tables sharding supported.
* Standard aggregation functions, GROUP BY, ORDER BY, LIMIT and JOIN DQL supported.
* Standard DML, DDL, TCL and database administrator command supported.
* Sharding operator `=`, `BETWEEN` and `IN` supported.
* Sharding algorithm customization and inline expression supported.
* Route by hint supported.
* Distributed sequence supported.

## 2. Read-write splitting
* Multiple slaves replica supported. 
* Data consistency guarantee in same thread supported.
* Mix read-write splitting and data sharding supported.
* Route by hint supported.

## 3. BASE Transaction
* Best efforts delivery transaction supported.
* Try confirm cancel transaction (TBD).

## 4. Orchestration
* Configuration center supported, can refresh dynamically.
* Circuit breaker supported.
* Open tracing supported.

# Architecture

## Sharding-JDBC

Use JDBC connect databases without redirect cost for java application, best performance for production.

* ORM compatible. JPA, Hibernate, Mybatis, Spring JDBC Template or JDBC supported.
* Connection-pool compatible. DBCP, C3P0, BoneCP, Druid supported.
* Multi SQL-based databases compatible. Any Database supported theoretically. Support MySQL, Oracle, SQLServer and PostgreSQL right now.

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/jbc_brief_en.png)

## Sharding-Proxy

Database router. Deploy as a stateless server, support MySQL protocol for now.

* Use standard MySQL protocol, application do not care about whether proxy or real MySQL.
* Any MySQL command line and UI workbench supported in theoretically. MySQL Workbench are fully compatible right now.

![Sharding-Proxy Architecture](http://ovfotjrsi.bkt.clouddn.com/proxy_brief_en.png)

## Sharding-Sidecar(TBD)

Use sidecar to connect databases, best for Kubernetes or Mesos together.

![Sharding-Sidecar Architecture](http://ovfotjrsi.bkt.clouddn.com/sidecar_brief_v2_en.png)

# Quick Start

## Sharding-JDBC

### Add maven dependency

```xml
<!-- import sharding-jdbc core -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configure sharding rule

Sharding-JDBC support 4 types for sharding rule configuration, they are `Java`, `YAML`, `Spring namespace` and `Spring boot starter`. Developers can choose any one for best suitable situation.

### Create DataSource

Use ShardingDataSourceFactory to create ShardingDataSource, which is a standard JDBC DataSource. Then developers can use it for raw JDBC, JPA, MyBatis or Other JDBC based ORM frameworks.

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

## Sharding-Proxy

### Configure sharding rule

Edit `${sharding-proxy}\conf\sharding-config.yaml`. Same format with Sharding-JDBC-Driver's `YAML` configuration. 

### Start server

``` shell
${sharding-proxy}\bin\start.sh ${port}
```
