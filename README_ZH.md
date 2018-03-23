# [Sharding-JDBC - 分布式数据库中间层](http://shardingjdbc.io/index_zh.html)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg)](https://github.com/shardingjdbc/sharding-jdbc/releases)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://github.com/shardingjdbc/sharding-jdbc-doc/raw/master/dist/sharding-proxy-2.1.0-SNAPSHOT-assembly-v1.tar.gz)

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.png?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Coverage Status](https://codecov.io/github/shardingjdbc/sharding-jdbc/coverage.svg?branch=master)](https://codecov.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![Gitter](https://badges.gitter.im/Sharding-JDBC/shardingjdbc.svg)](https://gitter.im/Sharding-JDBC/shardingjdbc?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/OpenSkywalking/skywalking)

# 概述

Sharding-JDBC是一个开源的分布式数据库中间件解决方案。它在Java的JDBC层以对业务应用零侵入的方式额外提供数据分片，读写分离，柔性事务和分布式治理能力。并在其基础上提供封装了数据库协议的代理端版本，用于完成对异构语言的支持。

基于JDBC的客户端版本定位为轻量级Java框架，使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。

封装了MySQL协议的服务端版本定位为透明化的MySQL代理端，可以使用任何兼容MySQL协议的访问客户端(如：MySQL Command Client, MySQL Workbench等)操作数据，对DBA更加友好。

# 文档

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](http://shardingjdbc.io/docs_cn/00-overview/)
[![Roadmap](https://img.shields.io/badge/roadmap-English-blue.svg)](ROADMAP.md)

# 功能列表

## 1. 数据分片
* 支持分库 + 分表
* 支持聚合，分组，排序，分页，关联查询等复杂查询语句
* 支持常见的DML，DDL，TCL以及数据库管理语句
* 支持=，BETWEEN，IN的分片操作符
* 自定义的灵活分片策略，支持多分片键共用，支持inline表达式
* 基于Hint的强制路由
* 支持分布式主键

## 2. 读写分离
* 支持一主多从的读写分离
* 支持同一线程内的数据一致性
* 支持分库分表与读写分离共同使用
* 支持基于Hint的强制主库路由

## 3. 柔性事务
* 最大努力送达型事务
* TCC型事务(TBD)

## 4. 分布式治理
* 支持配置中心，可动态修改配置
* 支持客户端熔断和失效转移
* 支持Open Tracing协议

# 部署架构

## Sharding-JDBC

通过客户端分片的方式由应用程序直连数据库，减少二次转发成本，性能最高，适合线上程序使用。

* 可适用于任何基于Java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC。
* 可基于任何第三方的数据库连接池，如：DBCP, BoneCP, Druid等。
* 可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL。

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/jdbc_brief_cn.png)

## Sharding-Proxy

通过代理服务器连接数据库(目前仅支持MySQL)，适合其他开发语言或MySQL客户端操作数据。

* 向应用程序完全透明，可直接当做MySQL使用。
* 可适用于任何兼容MySQL协议的的客户端。

![Sharding-Proxy Architecture](http://ovfotjrsi.bkt.clouddn.com/proxy_brief_cn.png)

## Sharding-Sidecar(TBD)

通过sidecar分片的方式，由IPC代替RPC，自动代理SQL分片，适合与Kubernetes或Mesos配合使用。

![Sharding-Sidecar Architecture](http://ovfotjrsi.bkt.clouddn.com/sidecar_brief_v2_cn.png)

# 快速入门

## Sharding-JDBC

### 引入maven依赖

```xml
<!-- 引入sharding-jdbc核心模块 -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 规则配置

Sharding-JDBC可以通过`Java`，`YAML`，`Spring命名空间`和`Spring Boot Starter`四种方式配置，开发者可根据场景选择适合的配置方式。

### 创建DataSource

通过ShardingDataSourceFactory工厂和规则配置对象获取ShardingDataSource，ShardingDataSource实现自JDBC的标准接口DataSource。然后即可通过DataSource选择使用原生JDBC开发，或者使用JPA, MyBatis等ORM工具。

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```

## Sharding-Proxy

### 规则配置

编辑`${sharding-proxy}\conf\sharding-config.yaml`。配置规则同Sharding-JDBC-Driver的`YAML`格式。 

### 启动服务

``` shell
${sharding-proxy}\bin\start.sh ${port}
```
