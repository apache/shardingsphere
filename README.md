# [ShardingSphere - Distributed Database Middleware Ecosphere](http://shardingsphere.io/)

Official website: http://shardingsphere.io/

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)
[![GitHub release](https://img.shields.io/github/release/sharding-sphere/sharding-sphere.svg)](https://github.com/sharding-sphere/sharding-sphere/releases)
[![Stargazers over time](https://starcharts.herokuapp.com/sharding-sphere/sharding-sphere.svg)](https://starcharts.herokuapp.com/sharding-sphere/sharding-sphere)

[![Total Lines](https://tokei.rs/b1/github/sharding-sphere/sharding-sphere?category=lines)](https://github.com/sharding-sphere/sharding-sphere)
[![Build Status](https://api.travis-ci.org/sharding-sphere/sharding-sphere.png?branch=master)](https://travis-ci.org/sharding-sphere/sharding-sphere)
[![Coverage Status](https://coveralls.io/repos/github/sharding-sphere/sharding-sphere/badge.svg?branch=dev)](https://coveralls.io/github/sharding-sphere/sharding-sphere?branch=dev)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/278600ed40ad48e988ab485b439abbcd)](https://www.codacy.com/app/terrymanu/sharding-sphere?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sharding-sphere/sharding-sphere&amp;utm_campaign=Badge_Grade)
[![snyk](https://snyk.io/test/github/sharding-sphere/sharding-sphere/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/sharding-sphere/sharding-sphere?targetFile=pom.xml)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

## Document

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](http://shardingsphere.io/document/current/en/)
[![Roadmap](https://img.shields.io/badge/roadmap-English-blue.svg)](ROADMAP.md)

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](http://shardingsphere.io/document/current/cn/)

## Overview

ShardingSphere is an open-source ecosystem consisted of a set of distributed database middleware solutions, including 3 independent products, Sharding-JDBC, Sharding-Proxy & Sharding-Sidecar (todo). 
They all provide functions of data sharding, distributed transaction and database orchestration, applicable in a variety of situations such as Java isomorphism, heterogeneous language and cloud native. 

Aiming at reasonably making full use of the computation and storage capacity of database in distributed system, ShardingSphere defines itself as a middleware, rather than a totally new type of database. 
As the cornerstone of many enterprises, relational database still takes a huge market share. 
Therefore, at current stage, we prefer to focus on its increment instead of a total overturn.

![ShardingSphere Scope](http://shardingsphere.jd.com/document/current/img/shardingsphere-scope_en.png)

### Sharding-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingsphere/sharding-jdbc/badge.svg)](https://mvnrepository.com/artifact/io.shardingsphere/sharding-jdbc)

Sharding-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. 
With client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on Java, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Based on any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of database that conforms to JDBC standard: MySQL，Oracle，SQLServer and PostgreSQL for now.

![Sharding-JDBC Architecture](http://shardingsphere.jd.com/document/current/img/sharding-jdbc-brief.png)

### Sharding-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/sharding-sphere/sharding-sphere-doc/raw/master/dist/sharding-proxy-3.0.0.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/shardingsphere/sharding-proxy.svg)](https://store.docker.com/community/images/shardingsphere/sharding-proxy)

Sharding-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBA, the MySQL version provided now can use any kind of client access (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible of MySQL protocol to operate data.

* Totally transparent to applications, it can be used directly as MySQL.
* Applicable to any kind of compatible of client end that is compatible of MySQL protocol.

![Sharding-Proxy Architecture](http://shardingsphere.jd.com/document/current/img/sharding-proxy-brief_v2.png)

### Sharding-Sidecar(TBD)

Sharding-Sidecar (TBD) defines itself as a cloud native database agent of Kubernetes or Mesos environment, in charge of all the access to database in the form of DaemonSet. 
Through a decentralized and zero-cost solution, it provides a mesh layer interacting with database, i.e., Database Mesh, also referred to as database grid.

Database Mesh emphasizes on how to connect distributed database access application with the database. 
Focusing on interaction, it effectively organizes the interaction between messy applications and database. 
The application and database that use Database Mesh to visit database will form a large grid system, where they just need to be put into the right position accordingly. 
They are all governed by mesh layer.

![Sharding-Sidecar Architecture](http://shardingsphere.jd.com/document/current/img/sharding-sidecar-brief_v2.png)

|                         | *Sharding-JDBC* | *Sharding-Proxy*     | *Sharding-Sidecar* |
| ----------------------- | --------------- | -------------------- | ------------------ |
| Database                | Any             | MySQL                | MySQL              |
| Connections Cost Number | High            | Low                  | High               |
| Heterogeneous Language  | Java Only       | Any                  | Any                |
| Performance             | Low loss        | Relatively High loss | Low loss           |
| Decentralization        | Yes             | No                   | No                 |
| Static Entry            | No              | Yes                  | No                 |

### Hybrid Architecture

Sharding-JDBC adopts decentralized architecture, applicable to high-performance light-weight OLTP application developed with Java; 
Sharding-Proxy provides static entry and heterogeneous language support, applicable for OLAP application and the sharding databases management and operation situation.

ShardingSphere is an ecosphere consists of multiple endpoints together.
Through mixed use of Sharding-JDBC and Sharding-Proxy and unified sharding strategy by one registry center, ShardingSphere can build application system applicable to all kinds of situations. 
Architects can adjust the system architecture to the most applicable one to current business more freely.

![ShardingSphere Hybrid Architecture](http://shardingsphere.jd.com/document/current/img/shardingsphere-hybrid.png)

## Features

### Data Sharding

* Database sharding & Table sharding
* Read-write splitting
* Distributed primary key

### Distributed Transaction (Doing)

* XA transaction
* BASE transaction

### Database Orchestration

* Dynamic configuration
* Fusing & Disabling
* Tracing
* Elastic scaling out (Planing)

## Roadmap

![Roadmap](http://shardingsphere.jd.com/document/current/img/shardingsphere-roadmap_en.png)
