+++
pre = "<b>1. </b>"
title = "概览"
weight = 1
chapter = true
+++

[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg?style=social&label=Release)](https://github.com/shardingjdbc/sharding-jdbc/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/shardingjdbc/sharding-jdbc.svg?style=social&label=Star)](https://github.com/shardingjdbc/sharding-jdbc/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/shardingjdbc/sharding-jdbc.svg?style=social&label=Fork)](https://github.com/shardingjdbc/sharding-jdbc/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/shardingjdbc/sharding-jdbc.svg?style=social&label=Watch)](https://github.com/shardingjdbc/sharding-jdbc/watchers)

Sharding-Sphere是一套开源的分布式数据库中间件解决方案组成的生态圈，它由Sharding-JDBC、Sharding-Proxy和Sharding-Sidecar这3款产品组成。3款产品提供标准化的数据分片、读写分离、柔性事务和数据治理功能，可以适用于各种多样化的应用场景。

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Gitter](https://badges.gitter.im/Sharding-JDBC/shardingjdbc.svg)](https://gitter.im/Sharding-JDBC/shardingjdbc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.png?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Coverage Status](https://codecov.io/github/shardingjdbc/sharding-jdbc/coverage.svg?branch=master)](https://codecov.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

![Sharding-Sphere Score](http://ovfotjrsi.bkt.clouddn.com/sphere_scope_cn.png)

## 简介

### Sharding-JDBC

定位为轻量级Java框架，于Java的JDBC层提供的额外服务。
它使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。

* 可适用于任何基于Java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC。
* 可基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP等。
* 可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL。

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-jdbc-brief.png)

### Sharding-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-proxy-2.1.0-SNAPSHOT-assembly-v1.tar.gz)

定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。
目前先提供MySQL版本，它可以使用任何兼容MySQL协议的访问客户端(如：MySQL Command Client, MySQL Workbench等)操作数据，对DBA更加友好。

* 向应用程序完全透明，可直接当做MySQL使用。
* 可适用于任何兼容MySQL协议的的客户端。

![Sharding-Proxy Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-proxy-brief.png)

### Sharding-Sidecar（TBD）

定位为Kubernetes或Mesos的云原生数据库代理，以DaemonSet的形式代理所有对数据库的访问。
通过无中心、零侵入的方案提供与数据库交互的的啮合层，即Database Mesh，又可称数据网格。

Database Mesh的关注重点在于如何将分布式的数据访问应用与数据库有机串联起来，它更加关注的是交互，是将杂乱无章的应用与数据库之间的交互有效的梳理。使用Database Mesh，访问数据库的应用和数据库终将形成一个巨大的网格体系，应用和数据库只需在网格体系中对号入座即可，它们都是被啮合层所治理的对象。

![Sharding-Sidecar Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-sidecar-brief.png)

|         | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ------- | --------------- | ---------------- | ------------------ |
| 数据库   | 任意            | MySQL            | MySQL               |
| 连接数   | 高              | 低               | 高                  |
| 异构语言 | 仅Java          | 任意              | 任意                |
| 性能     | 损耗低          | 损耗略高          | 损耗低               |
| 无中心化 | 是              | 否               | 是                   |
| 静态入口 | 无              | 有               | 无                   |

## 功能列表

### 数据分片

* 分库 + 分表
* 支持聚合，分组，排序，分页，关联查询等复杂查询语句
* 支持常见的DML，DDL，TCL以及数据库管理语句
* 支持=，BETWEEN，IN的分片操作符
* 自定义的灵活分片策略，支持多分片键共用，支持inline表达式
* 基于Hint的强制路由
* 分布式主键

### 读写分离

* 一主多从的读写分离
* 同一线程内的数据一致性
* 支持分库分表与读写分离共同使用
* 基于Hint的强制主库路由

### 柔性事务

* 最大努力送达型事务
* TCC型事务(TBD)

### 分布式治理

* 配置中心，配置动态化
* 客户端熔断
* 客户端失效转移
* 支持Open Tracing协议
