+++
pre = "<b>1. </b>"
title = "Overview"
weight = 1
chapter = true
+++

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg?style=social&label=Release)](https://github.com/apache/shardingsphere/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/apache/shardingsphere.svg?style=social&label=Star)](https://github.com/apache/shardingsphere/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/apache/shardingsphere.svg?style=social&label=Fork)](https://github.com/apache/shardingsphere/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/apache/shardingsphere.svg?style=social&label=Watch)](https://github.com/apache/shardingsphere/watchers)

**Stargazers Over Time**

[![Stargazers Over Time](https://starchart.cc/apache/shardingsphere.svg)](https://starchart.cc/apache/shardingsphere)

**Contributors Over Time**

[![Contributors Over Time](https://contributor-graph-api.apiseven.com/contributors-svg?chart=contributorOverTime&repo=apache/shardingsphere)](https://www.apiseven.com/en/contributor-graph?chart=contributorOverTime&repo=apache/shardingsphere)

Apache ShardingSphere is positioned as `Database Plus`, which aims to build criterion and ecosystem above multi-model databases.
It focuses on how to reuse existing database, rather than creating a new database.
ShardingSphere focus on the upper layer of databases, pays more attention on cooperation between databases rather than database itself.

`Link`, `Enhance` and `Pluggable` is the core concepts of Apache ShardingSphere.

- `Link`：Flexible adaptation of database protocol, SQL dialect and database storage, it can link applications and multi-mode heterogeneous databases quickly;
- `Enhance`：Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance（circuit breaker and access limitation and analyse (Qos and observability);
- `Pluggable`：Use micro kernel and 3 layers pluggable mode, to make features and database ecosystem can be embedded flexibility. Developers can customize their ShardingSphere just like building blocks.

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top-Level Project on April 16, 2020.

Welcome to interact with community via the official [mail list](mailto:dev@shardingsphere.apache.org) and the [ShardingSphere Slack](https://app.slack.com/client/T026JKU2DPF/C026MLH7F34).

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)

[![Twitter](https://img.shields.io/twitter/url/https/twitter.com/ShardingSphere.svg?style=social&label=Follow%20%40ShardingSphere)](https://twitter.com/ShardingSphere)
[![Slack](https://img.shields.io/badge/%20Slack-ShardingSphere%20Channel-blueviolet)](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

[![Build Status](https://api.travis-ci.org/apache/shardingsphere.svg?branch=master&status=created)](https://travis-ci.org/apache/shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)
[![snyk](https://snyk.io/test/github/apache/shardingsphere/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/apache/shardingsphere?targetFile=pom.xml)
[![Maintainability](https://cloud.quality-gate.com/dashboard/api/badge?projectName=apache_shardingsphere&branchName=master)](https://cloud.quality-gate.com/dashboard/branches/30#overview)

[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/apache/skywalking)

![Overview](https://shardingsphere.apache.org/document/current/img/overview.en.png)

## Introduction

Apache ShardingSphere including 3 independent products: JDBC, Proxy & Sidecar (Planning).
They all provide functions of data scale-out, distributed transaction and distributed governance,
applicable in a variety of situations such as Java isomorphism, heterogeneous language and Cloud-Native.

As the cornerstone of enterprises, the relational database has a huge market share. Therefore, we prefer to focus on its incrementation instead of a total overturn.

### ShardingSphere-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere/shardingsphere-jdbc/badge.svg)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

ShardingSphere-JDBC defines itself as a lightweight Java framework that provides extra services at the Java JDBC layer.
With the client end connecting directly to the database, it provides services in the form of a jar and requires no extra deployment and dependence.  
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Supports any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Supports any kind of JDBC standard database: MySQL, Oracle, SQLServer, PostgreSQL and any SQL92 followed databases.

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc_v2.png)

### ShardingSphere-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/sharding-proxy.svg)](https://store.docker.com/community/images/apache/sharding-proxy)

ShardingSphere-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBAs, the MySQL version now provided can use any kind of terminal (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible of MySQL protocol to operate data.

* Transparent towards applications, it can be used directly as MySQL and PostgreSQL servers.
* Applicable to any kind of terminal that is compatible with MySQL and PostgreSQL protocol.

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

### ShardingSphere-Sidecar(TODO)

ShardingSphere-Sidecar (TODO) defines itself as a cloud-native database agent of the Kubernetes environment, in charge of all database access in the form of a sidecar.
It provides a mesh layer interacting with the database, we call this `Database Mesh`.

Database Mesh emphasizes how to connect distributed data-access applications with the databases.
Focusing on interaction, it effectively organizes the interaction between messy applications and databases.
The applications and databases that use Database Mesh to visit databases will form a large grid system, where they just need to be put into the right positions accordingly. 
They are all governed by the mesh layer.

![ShardingSphere-Sidecar Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-sidecar-brief.png)

|                         | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* | *ShardingSphere-Sidecar* |
| ----------------------- | --------------------- | ---------------------- | ------------------------ |
| Database                | Any                   | MySQL/PostgreSQL       | MySQL/PostgreSQL         |
| Connections Count Cost  | High                  | Low                    | High                     |
| Supported Languages     | Java Only             | Any                    | Any                      |
| Performance             | Low loss              | Relatively High loss   | Low loss                 |
| Decentralization        | Yes                   | No                     | No                       |
| Static Entry            | No                    | Yes                    | No                       |

### Hybrid Architecture

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP application developed with Java. 
ShardingSphere-Proxy provides static entry and all languages support, applicable for OLAP application and the sharding databases management and operation situation.

ShardingSphere is an ecosystem consisting of multiple endpoints together.
Through a mixed use of ShardingSphere-JDBC and ShardingSphere-Proxy and a unified sharding strategy by the same registry center, ShardingSphere can build an application system that is applicable to all kinds of scenarios. 
Architects can adjust the system architecture to the most applicable one to their needs to conduct business more freely.

![ShardingSphere Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid-architecture_v2.png)

## Solution

### Distributed Database

* Data Sharding
* Readwrite-splitting
* Distributed Transaction
* Elastic Scale-out
* Distributed Highly Available

### Data Security

* Data Encrypt
* Row Authority (TODO)
* SQL Audit (TODO)
* SQL Firewall (TODO)

### Database Gateway

* Multi-model Databases supported
* SQL Dialect Translate（TODO）

### Stress Testing

* Shadow Database
* Observability (Tracing and Metrics)

## Roadmap

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap.png)
