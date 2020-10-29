# [ShardingSphere - Distributed Database Middleware Ecosphere](https://shardingsphere.apache.org/)

**Official website: https://shardingsphere.apache.org/**

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)
[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)
[![Stargazers over time](https://starchart.cc/apache/shardingsphere.svg)](https://starchart.cc/apache/shardingsphere)

[![Build Status](https://api.travis-ci.org/apache/shardingsphere.svg?branch=master&status=created)](https://travis-ci.org/apache/shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/278600ed40ad48e988ab485b439abbcd)](https://www.codacy.com/app/terrymanu/sharding-sphere?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sharding-sphere/sharding-sphere&amp;utm_campaign=Badge_Grade)
[![snyk](https://snyk.io/test/github/apache/shardingsphere/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/apache/shardingsphere?targetFile=pom.xml)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/apache/skywalking)

## Document

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://shardingsphere.apache.org/document/current/en/overview/)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://shardingsphere.apache.org/document/current/cn/overview/)

## Overview

Apache ShardingSphere is an open-source ecosystem consisted of a set of distributed database middleware solutions, including 3 independent products, JDBC, Proxy & Sidecar (Planning). 
They all provide functions of data sharding, distributed transaction and database governance, applicable in a variety of situations such as Java isomorphism, heterogeneous language and cloud native.

Aiming at reasonably making full use of the computation and storage capacity of database in distributed system, ShardingSphere defines itself as a middleware, rather than a totally new type of database.
As the cornerstone of many enterprises, relational database still takes a huge market share. Therefore, at current stage, we prefer to focus on its increment instead of a total overturn.

Apache ShardingSphere begins to focus on pluggable architecture from version 5.x, features can be embedded into project flexibility.
Currently, the features such as data sharding, replica query, consensus replication, data encrypt, shadow test, and SQL dialects / database protocols such as MySQL, PostgreSQL, SQLServer, Oracle supported are all weaved by plugins.
Developers can customize their own ShardingSphere systems just like building lego blocks. There are lots of SPI extensions for Apache ShardingSphere now and increasing continuously.

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top Level Project on April 16, 2020.

Welcome communicate with community via [mail list](mailto:dev@shardingsphere.apache.org).

![ShardingSphere Scope](https://shardingsphere.apache.org/document/current/img/shardingsphere-scope_en.png)

### ShardingSphere-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere/sharding-jdbc/badge.svg)](https://mvnrepository.com/artifact/org.apache.shardingsphere/sharding-jdbc)

ShardingSphere-JDBC defines itself as a lightweight Java framework that provides extra services at Java JDBC layer. 
With the client end connecting directly to the database, it provides services in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Support any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of JDBC standard database: MySQL, Oracle, SQLServer, PostgreSQL and any SQL92 followed database.

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc-brief.png)

### ShardingSphere-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://www.apache.org/dyn/closer.cgi?path=shardingsphere/4.1.1/apache-shardingsphere-4.1.1-sharding-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/shardingsphere/sharding-proxy.svg)](https://store.docker.com/community/images/shardingsphere/sharding-proxy)

ShardingSphere-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBA, the proxy of MySQL/PostgreSQL version provided now can use any kind of client access (such as MySQL Command Client, MySQL Workbench, Navicat etc.) that is compatible of MySQL/PostgreSQL protocol to operate data.

* Totally transparent to applications, it can be used directly as MySQL and PostgreSQL server.
* Applicable to any kind of compatible client end that is compatible with MySQL and PostgreSQL protocol.

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy-brief.png)

### ShardingSphere-Sidecar(TODO)

ShardingSphere-Sidecar (TODO) defines itself as a cloud native database agent of the Kubernetes environment, in charge of all the access to the database in the form of sidecar. 
It provides a mesh layer interacting with the database, we call this as `Database Mesh`.

Database Mesh emphasizes on how to connect distributed data-access applications with the databases. 
Focusing on interaction, it effectively organizes the interaction between messy applications and the databases. 
The applications and databases those use Database Mesh to visit databases will form a large grid system, where they just need to be put into the right positions accordingly. 
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

ShardingSphere-JDBC adopts decentralized architecture, applicable to high-performance light-weight OLTP applications developed with Java; 
ShardingSphere-Proxy provides static entry and all languages support, applicable for OLAP applications and the sharding databases management and operation situation.

ShardingSphere is an ecosphere consisted of multiple endpoints together.
Through a mixed use of ShardingSphere-JDBC and ShardingSphere-Proxy and unified sharding strategy by the same registry center, ShardingSphere can build an application system applicable to all kinds of scenarios. 
Architects can adjust the system architecture to the most applicable one to current business more freely.

![ShardingSphere Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid.png)

## Features

### Data Sharding

* Database sharding & Table sharding
* Replica query
* Sharding strategy customization
* Centre-less Distributed primary key

### Distributed Transaction

* Unified Transaction API
* XA transaction
* BASE transaction

### Database Governance

* Distributed Governance
* Data migration & Scale out
* Observability(Tracing/Metrics) Supported
* Data Encryption&Decryption
* Shadow Table for Performance Testing

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

## Landscapes

<p align="center">
<br/><br/>
<img src="https://landscape.cncf.io/images/left-logo.svg" width="150"/>&nbsp;&nbsp;<img src="https://landscape.cncf.io/images/right-logo.svg" width="200"/>
<br/><br/>
Apache ShardingSphere enriches the <a href="https://landscape.cncf.io/landscape=observability-and-analysis&license=apache-license-2-0">CNCF CLOUD NATIVE Landscape</a>.
</p>
