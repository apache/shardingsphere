# [ShardingSphere - 构建多模数据库上层的标准和生态](https://shardingsphere.apache.org/index_zh.html)

**官方网站: https://shardingsphere.apache.org/**

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)

[![Twitter](https://img.shields.io/twitter/url/https/twitter.com/ShardingSphere.svg?style=social&label=Follow%20%40ShardingSphere)](https://twitter.com/ShardingSphere)
[![Slack](https://img.shields.io/badge/%20Slack-ShardingSphere%20Channel-blueviolet)](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

**星评增长时间线**

[![Stargazers over time](https://starchart.cc/apache/shardingsphere.svg)](https://starchart.cc/apache/shardingsphere)

**贡献者增长时间线**

[![Contributor over time](https://contributor-graph-api.apiseven.com/contributors-svg?chart=contributorOverTime&repo=apache/shardingsphere)](https://www.apiseven.com/en/contributor-graph?chart=contributorOverTime&repo=apache/shardingsphere)

[![Build Status](https://api.travis-ci.org/apache/shardingsphere.svg?branch=master&status=created)](https://travis-ci.org/apache/shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)
[![snyk](https://snyk.io/test/github/apache/shardingsphere/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/apache/shardingsphere?targetFile=pom.xml)
[![Maintainability](https://cloud.quality-gate.com/dashboard/api/badge?projectName=apache_shardingsphere&branchName=master)](https://cloud.quality-gate.com/dashboard/branches/30#overview)

[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/apache/skywalking)

## 文档

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://shardingsphere.apache.org/document/current/cn/overview/)

## 概述

Apache ShardingSphere 产品定位为 `Database Plus`，旨在构建多模数据库上层的标准和生态。
它关注如何充分合理地利用数据库的计算和存储能力，而并非实现一个全新的数据库。ShardingSphere 站在数据库的上层视角，关注他们之间的协作多于数据库自身。

`连接`、`增量`和`可插拔`是 Apache ShardingSphere 的核心概念。

- `连接`：通过对数据库协议、SQL 方言以及数据库存储的灵活适配，快速的连接应用与多模式的异构数据库；
- `增量`：获取数据库的访问流量，并提供流量重定向（数据分片、读写分离、影子库）、流量变形（数据加密、数据脱敏）、流量鉴权（安全、审计、权限）、流量治理（熔断、限流）以及流量分析（服务质量分析、可观察性）等透明化增量功能；
- `可插拔`：项目采用微内核 + 3 层可插拔模型，使内核、功能组件以及生态对接完全能够灵活的方式进行插拔式扩展，开发者能够像使用积木一样定制属于自己的独特系统。

ShardingSphere 已于 2020 年 4 月 16 日成为 [Apache 软件基金会](https://apache.org/index.html#projects-list)的顶级项目。
欢迎通过[邮件列表](mailto:dev@shardingsphere.apache.org)参与讨论。

![Overview](https://shardingsphere.apache.org/document/current/img/overview.cn.png)

Apache ShardingSphere 由 JDBC、Proxy 和 Sidecar（规划中）这 3 款既能够独立部署，又支持混合部署配合使用的产品组成。
它们均提供标准化的基于数据库作为存储节点的增量功能，可适用于如 Java 同构、异构语言、云原生等各种多样化的应用场景。

关系型数据库当今依然占有巨大市场份额，是企业核心系统的基石，未来也难于撼动，我们更加注重在原有基础上提供增量，而非颠覆。

### ShardingSphere-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere/shardingsphere-jdbc/badge.svg)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。
它使用客户端直连数据库，以 jar 包形式提供服务，无需额外部署和依赖，可理解为增强版的 JDBC 驱动，完全兼容 JDBC 和各种 ORM 框架。

* 适用于任何基于 JDBC 的 ORM 框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template 或直接使用 JDBC。
* 支持任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP 等。
* 支持任意实现 JDBC 规范的数据库，目前支持 MySQL，Oracle，SQLServer，PostgreSQL 以及任何遵循 SQL92 标准的数据库。

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc_v3.png)

### ShardingSphere-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://apache.org/dyn/closer.cgi?path=shardingsphere/5.0.0-beta/apache-shardingsphere-5.0.0-beta-shardingsphere-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/sharding-proxy.svg)](https://store.docker.com/community/images/apache/sharding-proxy)

定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。
目前提供 MySQL 和 PostgreSQL 版本，它可以使用任何兼容 MySQL/PostgreSQL 协议的访问客户端（如：MySQL Command Client, MySQL Workbench, Navicat 等）操作数据，对 DBA 更加友好。

* 向应用程序完全透明，可直接当做 MySQL/PostgreSQL 服务端使用。
* 适用于任何兼容 MySQL/PostgreSQL 协议的的客户端。

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

|           | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* |
| --------- | --------------------- | ---------------------- |
| 数据库     | 任意                   | MySQL/PostgreSQL       |
| 连接消耗数  | 高                    | 低                     |
| 异构语言    | 仅 Java               | 任意                   |
| 性能       | 损耗低                 | 损耗略高                |
| 无中心化    | 是                    | 否                     |
| 静态入口    | 无                    | 有                     |

### 混合架构

ShardingSphere-JDBC 采用无中心化架构，与应用程序共享资源，适用于 Java 开发的高性能的轻量级 OLTP 应用；
ShardingSphere-Proxy 提供静态入口以及异构语言的支持，独立于应用程序部署，适用于 OLAP 应用以及对分片数据库进行管理和运维的场景。

Apache ShardingSphere 是多接入端共同组成的生态圈。
通过混合使用 ShardingSphere-JDBC 和 ShardingSphere-Proxy，并采用同一注册中心统一配置分片策略，能够灵活的搭建适用于各种场景的应用系统，使得架构师更加自由地调整适合与当前业务的最佳系统架构。

![ShardingSphere Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid-architecture_v2.png)

## 解决方案

### 分布式数据库

* 数据分片
* 读写分离
* 分布式事务
* 弹性伸缩
* 分布式高可用

### 数据安全

* 数据加密
* 行级权限（TODO）
* SQL 审计（TODO）
* SQL 防火墙（TODO）

### 数据库网关

* 异构数据库支持
* SQL 方言转换（TODO）

### 全链路压测

* 影子库
* 可观测性（分布式跟踪、指标度量）

## 如何构建

### 构建 Apache ShardingSphere

```bash
./mvnw clean install -Prelease
```

构建产物：

```
shardingsphere-distribution/shardingsphere-src-distribution/target/apache-shardingsphere-${latest.release.version}-src.zip  # Apache ShardingSphere 的源码包
shardingsphere-distribution/shardingsphere-jdbc-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-jdbc-bin.tar.gz  # ShardingSphere-JDBC 的二进制包
shardingsphere-distribution/shardingsphere-proxy-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz  # ShardingSphere-Proxy 的二进制包
shardingsphere-distribution/shardingsphere-scaling-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-scaling-bin.tar.gz  # ShardingSphere-Scaling 的二进制包
```

### 构建ShardingSphere UI

```bash
git clone https://github.com/apache/shardingsphere-ui
cd shardingsphere-ui
./mvnw clean install -Prelease
```

构建产物：

```
shardingsphere-ui/shardingsphere-ui-distribution/shardingsphere-ui-bin-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-ui-bin.tar.gz  # ShardingSphere-UI的二进制包
```

## 线路规划

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap.png)

## 全景图

<p align="center">
<br/><br/>
<img src="https://landscape.cncf.io/images/left-logo.svg" width="150"/>&nbsp;&nbsp;<img src="https://landscape.cncf.io/images/right-logo.svg" width="200"/>
<br/><br/>
ShardingSphere 进入了<a href="https://landscape.cncf.io/landscape=observability-and-analysis&license=apache-license-2-0">CNCF 云原生全景图</a>。
</p>
