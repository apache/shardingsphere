# [Sharding-Sphere - Distributed Database Middleware Ecosphere](http://shardingsphere.io/)

Official website: http://shardingsphere.io/

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)
[![GitHub release](https://img.shields.io/github/release/sharding-sphere/sharding-sphere.svg)](https://github.com/sharding-sphere/sharding-sphere/releases)

[![Build Status](https://api.travis-ci.org/sharding-sphere/sharding-sphere.png?branch=master)](https://travis-ci.org/sharding-sphere/sharding-sphere)
[![Coverage Status](https://coveralls.io/repos/github/sharding-sphere/sharding-sphere/badge.svg?branch=dev)](https://coveralls.io/github/sharding-sphere/sharding-sphere?branch=dev)
[![snyk](https://snyk.io/test/github/sharding-sphere/sharding-sphere/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/sharding-sphere/sharding-sphere?targetFile=pom.xml)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

## Document

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](http://shardingsphere.io/document/current/en/)
[![Roadmap](https://img.shields.io/badge/roadmap-English-blue.svg)](ROADMAP.md)

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](http://shardingsphere.io/document/current/cn/)

## Overview

Sharding-Sphere is an open-source ecosystem consisted of a set of distributed database middleware solution, including 3 independent products, Sharding-JDBC, Sharding-Proxy & Sharding-Sidecar (todo). 
They all provide functions of data sharding, distributed transaction and database orchestration, applicable in a variety of situations such as Java isomorphism, heterogeneous language and cloud native. 

Aiming at reasonably making full use of the computation and storage capacity of database in distributed system, Sharding-Sphere defines itself as a middleware, rather than a totally new type of database. 
As the cornerstone of many enterprises, relational database still takes a huge market share. 
Therefore, at current stage, we prefer to focus on its increment instead of a total overturn.

![Sharding-Sphere Scope](http://ovfotjrsi.bkt.clouddn.com/sharding-sphere-scope_en.png)

### Sharding-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingsphere/sharding-jdbc/badge.svg)](https://mvnrepository.com/artifact/io.shardingsphere/sharding-jdbc)

Sharding-JDBC is positioned as a lightweight Java framework that provides Sharding service in the JDBC layer. 
It directly connects to databases to provide services in the form of jar, with no additional deployment and dependencies, and it can be understood as an enhanced version of the JDBC driver, fully compliant with JDBC and various ORM frameworks.

* It is compliant with any Java-based ORM framework, such as JPA, Hibernate, Mybatis and Spring JDBC Template; or you can use JDBC directly.
* It is suitable for connection-pool based on any third party, such as DBCP, C3P0, BoneCP, Druid, HikariCP and so on.
* It supports databases implementing JDBC specification. Supporting MySQL, Oracle, SQLServer and PostgreSQL right now.

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-jdbc-brief.png)

### Sharding-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/sharding-sphere/sharding-sphere-doc/raw/master/dist/sharding-proxy-3.0.0.M4.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/shardingsphere/sharding-proxy.svg)](https://store.docker.com/community/images/shardingsphere/sharding-proxy)

Sharding-Proxy is positioned as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
At present, we provide Sharding-Proxy for MySQL, and you can use any clients compatible with MySQL protocol (such as MySQL Command Client, MySQL Workbench, etc.) to manipulate data.

* It is transparent to application, which can be used as MySQL.
* It is suitable for any clients compatible with MySQL protocol.

![Sharding-Proxy Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-proxy-brief_v2.png)

### Sharding-Sidecar(TBD)

It is positioned as cloud native database agent of Kubernetes or Mesos environment, in charge of all database access in the form of DaemonSet. 
Through a centre-less and zero-cost solution, it provides mesh layer interacting with database. We call it `Database Mesh`.

Database Mesh emphasizes how to connect distributed data-access-layer with databases. It pays more attention to interaction, which means the messy interaction between applications and databases will be effectively orchestrated. 
By using Database Mesh, applications and databases will form a large grid system, where they just need to be put into the right position accordingly. They are all governed by mesh layer.

![Sharding-Sidecar Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-sidecar-brief_v2.png)

|                        | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ---------------------- | --------------- | ---------------- | ------------------ |
| Database               | Any             | MySQL            | MySQL              |
| Connections Cost       | More            | Less             | More               |
| Heterogeneous Language | Java Only       | Any              | Any                |
| Performance            | Low loss        | High loss        | Low loss           |
| Centre-less            | Yes             | No               | No                 |
| Static Entry           | No              | Yes              | No                 |

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
* Open tracing
* Multiple data replication (Planing)
* Elastic scaling out (Planing)

## Roadmap

![Roadmap](http://ovfotjrsi.bkt.clouddn.com/roadmap_en_v2.png)
