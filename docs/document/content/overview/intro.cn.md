+++
pre = "<b>1.1 </b>"
title = "什么是 ShardingSphere"
weight = 1
chapter = true
+++

## 介绍

Apache ShardingSphere 是一款开源的分布式数据库生态项目，由 JDBC 和 Proxy 两款产品组成。
其核心采用可插拔架构，通过组件扩展功能。
对上以数据库协议及 SQL 方式提供诸多增强功能，包括数据分片、访问路由、数据安全等；对下原生支持 MySQL、PostgreSQL、SQL Server、Oracle 等多种数据存储引擎。 
Apache ShardingSphere 项目理念，是提供数据库增强计算服务平台，进而围绕其上构建生态。
充分利用现有数据库的计算与存储能力，通过插件化方式增强其核心能力，为企业解决在数字化转型中面临的诸多使用难点，为加速数字化应用赋能。

### ShardingSphere-JDBC

[![Maven Status](https://img.shields.io/maven-central/v/org.apache.shardingsphere/shardingsphere-jdbc.svg?color=green)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

ShardingSphere-JDBC 定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。

### ShardingSphere-Proxy

[![Nightly-Download](https://img.shields.io/badge/nightly--builds-download-orange.svg)](https://nightlies.apache.org/shardingsphere/)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](/cn/downloads/)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/shardingsphere-proxy.svg)](https://hub.docker.com/r/apache/shardingsphere-proxy)

ShardingSphere-Proxy 定位为透明化的数据库代理端，通过实现数据库二进制协议，对异构语言提供支持。

## 产品定位

* 构建异构数据库上层生态和标准

Apache ShardingSphere 产品定位为 Database Plus，旨在构建异构数据库上层的标准和生态。
它关注如何充分合理地利用数据库的计算和存储能力，而并非实现一个全新的数据库。
ShardingSphere 站在数据库的上层视角，关注他们之间的协作多于数据库自身。

* 在数据库基础上提供扩展和增强

Apache ShardingSphere 旨在充分合理地在分布式的场景下利用关系型数据库的计算和存储能力， 并非实现一个全新的关系型数据库。
关系型数据库当今依然占有巨大市场份额，是企业核心系统的基石，未来也难于撼动，我们更加注重在原有基础上提供增量，而非颠覆。

* 统一管控的多端接入

Apache ShardingSphere 是多接入端共同组成的生态圈。 
通过混合使用 ShardingSphere-JDBC 和 ShardingSphere-Proxy，并采用同一注册中心统一配置分片策略，能够灵活的搭建适用于各种场景的应用系统，使得架构师更加自由地调整适合于当前业务的最佳系统架构。

## 线路规划

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap_v2.png)

## 如何参与

ShardingSphere 已于 2020 年 4 月 16 日成为 [Apache 软件基金会](https://apache.org/index.html#projects-list)的顶级项目。
欢迎通过[邮件列表](mailto:dev@shardingsphere.apache.org)参与讨论。
