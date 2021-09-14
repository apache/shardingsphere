# [ShardingSphere - Build criterion and ecosystem above multi-model databases](https://shardingsphere.apache.org/)

**Official Website: https://shardingsphere.apache.org/**

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)

[![Twitter](https://img.shields.io/twitter/url/https/twitter.com/ShardingSphere.svg?style=social&label=Follow%20%40ShardingSphere)](https://twitter.com/ShardingSphere)
[![Slack](https://img.shields.io/badge/%20Slack-ShardingSphere%20Channel-blueviolet)](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

**Stargazers Over Time**

[![Stargazers Over Time](https://starchart.cc/apache/shardingsphere.svg)](https://starchart.cc/apache/shardingsphere)

**Contributors Over Time**

[![Contributors Over Time](https://contributor-graph-api.apiseven.com/contributors-svg?chart=contributorOverTime&repo=apache/shardingsphere)](https://www.apiseven.com/en/contributor-graph?chart=contributorOverTime&repo=apache/shardingsphere)

[![Build Status](https://api.travis-ci.org/apache/shardingsphere.svg?branch=master&status=created)](https://travis-ci.org/apache/shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)
[![snyk](https://snyk.io/test/github/apache/shardingsphere/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/apache/shardingsphere?targetFile=pom.xml)
[![Maintainability](https://cloud.quality-gate.com/dashboard/api/badge?projectName=apache_shardingsphere&branchName=master)](https://cloud.quality-gate.com/dashboard/branches/30#overview)

[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/apache/skywalking)

## Document

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://shardingsphere.apache.org/document/current/en/overview/)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://shardingsphere.apache.org/document/current/cn/overview/)

## Overview

Apache ShardingSphere is positioned as `Database Plus`, which aims to build criterion and ecosystem above multi-model databases.
It focuses on how to reuse existing database, rather than creating a new database.
ShardingSphere focus on the upper layer of databases, pays more attention on cooperation between databases rather than database itself.

`Link`, `Enhance` and `Pluggable` is the core concepts of Apache ShardingSphere.

- `Link`：Flexible adaptation of database protocol, SQL dialect and database storage, it can link applications and multi-mode heterogeneous databases quickly;
- `Enhance`：Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance（circuit breaker and access limitation and analyse (Qos and observability);
- `Pluggable`：Use micro kernel and 3 layers pluggable mode, to make features and database ecosystem can be embedded flexibility. Developers can customize their ShardingSphere just like building blocks.

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top-Level Project on April 16, 2020.

You are welcome to communicate with the community via the [mailing list](mailto:dev@shardingsphere.apache.org) and interact via the [ShardingSphere Slack](https://app.slack.com/client/T026JKU2DPF/C026MLH7F34).

![Overview](https://shardingsphere.apache.org/document/current/img/overview.en.png)

Apache ShardingSphere including 3 independent products: JDBC, Proxy & Sidecar (Planning).
They all provide functions of data scale-out, distributed transaction and distributed governance, 
applicable in a variety of situations such as Java isomorphism, heterogeneous language and Cloud-Native.

As the cornerstone of enterprises, the relational database has a huge market share. Therefore, we prefer to focus on its incrementation instead of a total overturn.

### ShardingSphere-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere/shardingsphere-jdbc/badge.svg)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

ShardingSphere-JDBC defines itself as a lightweight Java framework that provides extra services at the Java JDBC layer. With the client end connecting directly to the database, it provides services in the form of a jar and requires no extra deployment and dependence.
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
Supports any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
Supports any kind of JDBC standard database: MySQL, Oracle, SQLServer, PostgreSQL and any SQL92 followed databases.

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc_v2.png)

### ShardingSphere-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://www.apache.org/dyn/closer.cgi?path=shardingsphere/5.0.0-beta/apache-shardingsphere-5.0.0-beta-sharding-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/sharding-proxy.svg)](https://store.docker.com/community/images/apache/sharding-proxy)

ShardingSphere-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. Friendlier to DBAs, the MySQL version now provided can use any kind of terminal (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible of MySQL protocol to operate data.

Transparent towards applications, it can be used directly as MySQL and PostgreSQL servers.
Applicable to any kind of terminal that is compatible with MySQL and PostgreSQL protocol.

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

|                         | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* |
| ----------------------- | --------------------- | ---------------------- |
| Database                | Any                   | MySQL/PostgreSQL       |
| Connections Count Cost  | High                  | Low                    |
| Supported Languages     | Java Only             | Any                    |
| Performance             | Low loss              | Relatively High loss   |
| Decentralization        | Yes                   | No                     |
| Static Entry            | No                    | Yes                    |

### Hybrid Architecture

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP application developed with Java. ShardingSphere-Proxy provides static entry and all languages support, applicable for OLAP application and the sharding databases management and operation situation.

ShardingSphere is an ecosystem consisting of multiple endpoints together. Through a mixed use of ShardingSphere-JDBC and ShardingSphere-Proxy and a unified sharding strategy by the same registry center, ShardingSphere can build an application system that is applicable to all kinds of scenarios. Architects can adjust the system architecture to the most applicable one to their needs to conduct business more freely.

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

## How to Build

### Build Apache ShardingSphere

```bash
./mvnw clean install -Prelease
```

Artifact:

```
shardingsphere-distribution/shardingsphere-src-distribution/target/apache-shardingsphere-${latest.release.version}-src.zip  # Source code package of Apache ShardingSphere
shardingsphere-distribution/shardingsphere-jdbc-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-jdbc-bin.tar.gz  # Binary package of ShardingSphere-JDBC
shardingsphere-distribution/shardingsphere-proxy-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz  # Binary package of ShardingSphere-Proxy
shardingsphere-distribution/shardingsphere-scaling-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-scaling-bin.tar.gz  # Binary package of ShardingSphere-Scaling
```

### Build ShardingSphere-UI

```bash
git clone https://github.com/apache/shardingsphere-ui
cd shardingsphere-ui
./mvnw clean install -Prelease
```

Artifact:

```
shardingsphere-ui/shardingsphere-ui-distribution/shardingsphere-ui-bin-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-ui-bin.tar.gz  # Binary package of ShardingSphere-UI
```

## Roadmap

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap.png)

## Landscapes

<p align="center">
<br/><br/>
<img src="https://landscape.cncf.io/images/left-logo.svg" width="150"/>&nbsp;&nbsp;<img src="https://landscape.cncf.io/images/right-logo.svg" width="200"/>
<br/><br/>
Apache ShardingSphere enriches the <a href="https://landscape.cncf.io/landscape=observability-and-analysis&license=apache-license-2-0">CNCF CLOUD NATIVE Landscape</a>.
</p>
