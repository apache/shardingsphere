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
[![Stargazers over time](https://starchart.cc/apache/shardingsphere.svg)](https://starchart.cc/apache/shardingsphere)

ShardingSphere is an open-source ecosphere consists of a set of distributed database middleware solutions, including 3 independent products, Sharding-JDBC, Sharding-Proxy & Sharding-Sidecar (todo).
They all provide functions of data sharding, distributed transaction and database orchestration, applicable in a variety of situations such as Java isomorphism, heterogeneous language and cloud native. 
Aiming at reasonably making full use of the computation and storage capacity of the database in a distributed system, ShardingSphere defines itself as a middleware, rather than a totally new type of database. 
As the cornerstone of many enterprises, relational database still takes a huge market share. 
Therefore, at the current stage, we prefer to focus on its increment instead of a total overturn.

ShardingSphere had graduated from [Apache Incubator](http://incubator.apache.org/projects/shardingsphere.html) on April 16 2020, is now officially an Apache Project!
Welcome discuss with community via [shardingsphere dev mail list](mailto:dev@shardingsphere.apache.org).

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)

[![Total Lines](https://tokei.rs/b1/github/apache/shardingsphere?category=lines)](https://github.com/apache/shardingsphere)
[![Build Status](https://api.travis-ci.org/apache/shardingsphere.svg?branch=master&status=created)](https://travis-ci.org/apache/shardingsphere)
[![Coverage Status](https://coveralls.io/repos/github/apache/shardingsphere/badge.svg?branch=dev)](https://coveralls.io/github/apache/shardingsphere?branch=dev)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/278600ed40ad48e988ab485b439abbcd)](https://www.codacy.com/app/terrymanu/sharding-sphere?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sharding-sphere/sharding-sphere&amp;utm_campaign=Badge_Grade)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/apache/skywalking)

![ShardingSphere Scope](https://shardingsphere.apache.org/document/current/img/shardingsphere-scope_en.png)

## Introduction

### Sharding-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere/sharding-jdbc/badge.svg)](https://mvnrepository.com/artifact/org.apache.shardingsphere/sharding-jdbc)

Sharding-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. 
With the client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Support any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of JDBC standard database: MySQL, Oracle, SQLServer, PostgreSQL and any SQL92 followed databases.

![Sharding-JDBC Architecture](https://shardingsphere.apache.org/document/current/img//sharding-jdbc-brief.png)

### Sharding-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/shardingsphere/sharding-proxy.svg)](https://store.docker.com/community/images/shardingsphere/sharding-proxy)

Sharding-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBA, the MySQL version provided now can use any kind of terminal (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible of MySQL protocol to operate data.

* Totally transparent to applications, it can be used directly as MySQL.
* Applicable to any kind of terminal that is compatible with MySQL and PostgreSQL protocol.

![Sharding-Proxy Architecture](https://shardingsphere.apache.org/document/current/img//sharding-proxy-brief_v2.png)

### Sharding-Sidecar(TODO)

Sharding-Sidecar (TODO) defines itself as a cloud native database agent of the Kubernetes environment, in charge of all the access to the database in the form of sidecar. 
It provides a mesh layer interacting with the database, we call this as `Database Mesh`.

Database Mesh emphasizes on how to connect distributed database access application with the database. 
Focusing on interaction, it effectively organizes the interaction between messy applications and the database. 
The application and database that use Database Mesh to visit database will form a large grid system, where they just need to be put into the right position accordingly. 
They are all governed by the mesh layer.

![Sharding-Sidecar Architecture](https://shardingsphere.apache.org/document/current/img/sharding-sidecar-brief_v2.png)

|                         | *Sharding-JDBC* | *Sharding-Proxy*     | *Sharding-Sidecar* |
| ----------------------- | --------------- | -------------------- | ------------------ |
| Database                | Any             | MySQL                | MySQL              |
| Connections Count Cost  | High            | Low                  | High               |
| Supported Languages     | Java Only       | Any                  | Any                |
| Performance             | Low loss        | Relatively High loss | Low loss           |
| Decentralization        | Yes             | No                   | No                 |
| Static Entry            | No              | Yes                  | No                 |

### Hybrid Architecture

Sharding-JDBC adopts decentralized architecture, applicable to high-performance light-weight OLTP application developed with Java; 
Sharding-Proxy provides static entry and all languages support, applicable for OLAP application and the sharding databases management and operation situation.

ShardingSphere is an ecosphere consists of multiple endpoints together.
Through a mixed use of Sharding-JDBC and Sharding-Proxy and unified sharding strategy by the same registry center, ShardingSphere can build an application system applicable to all kinds of scenarios. 
Architects can adjust the system architecture to the most applicable one to current business more freely.

![ShardingSphere Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid.png)

## Features

### Data Sharding

* Database sharding & Table sharding
* Read-write splitting
* Sharding strategy customization
* Centre-less Distributed primary key

### Distributed Transaction

* Unified Transaction API
* XA transaction
* BASE transaction

### Database Orchestration

* Dynamic Configuration
* Orchestration & Governance
* Data Encryption
* Tracing & Observability
* Elastic scaling out (Planing)

## Project Status

![Status](https://shardingsphere.apache.org/document/current/img/shardingsphere-status_en.png)
