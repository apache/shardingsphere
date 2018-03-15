# [Sharding-JDBC - Distributed database middleware](http://shardingjdbc.io/)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-proxy-2.1.0-SNAPSHOT-assembly-v1.tar.gz)

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.png?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Coverage Status](https://codecov.io/github/shardingjdbc/sharding-jdbc/coverage.svg?branch=master)](https://codecov.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![Gitter](https://badges.gitter.im/Sharding-JDBC/shardingjdbc.svg)](https://gitter.im/Sharding-JDBC/shardingjdbc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

# Overview

Sharding-JDBC is a distributed database middleware, focus on data sharding, read-write splitting, BASE transaction and database orchestration. It provides maximum compatibilities for applications by JDBC driver or database protocols proxy.

# Document

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](http://shardingjdbc.io/docs_en/00-overview/)
[![Roadmap](https://img.shields.io/badge/roadmap-English-blue.svg)](ROADMAP.md)

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](http://shardingjdbc.io/docs_cn/00-overview/)

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
* Connection-pool compatible. DBCP, BoneCP, Druid supported.
* Multi SQL-based databases compatible. Any Database supported theoretically. Support MySQL, Oracle, SQLServer and PostgreSQL right now.

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/jdbc_brief_en.png)

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
