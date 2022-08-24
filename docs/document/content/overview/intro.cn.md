+++
pre = "<b>1.1 </b>"
title = "什么是 ShardingSphere"
weight = 1
chapter = true
+++

## 介绍

Apache ShardingSphere 是一款开源的分布式数据库生态项目，由 JDBC 和 Proxy 两款产品组成。 其核心采用微内核+可插拔架构，通过插件开放扩展功能。它提供多源异构数据库增强平台，进而围绕其上层构建生态。

Apache ShardingSphere 设计哲学为 Database Plus，旨在构建异构数据库上层的标准和生态。它关注如何充分合理地利用数据库的计算和存储能力，而并非实现一个全新的数据库。它站在数据库的上层视角，关注它们之间的协作多于数据库自身。

### ShardingSphere-JDBC

[![Maven Status](https://img.shields.io/maven-central/v/org.apache.shardingsphere/shardingsphere-jdbc.svg?color=green)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

ShardingSphere-JDBC 定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。

### ShardingSphere-Proxy

[![Nightly-Download](https://img.shields.io/badge/nightly--builds-download-orange.svg)](https://nightlies.apache.org/shardingsphere/)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](/cn/downloads/)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/shardingsphere-proxy.svg)](https://hub.docker.com/r/apache/shardingsphere-proxy)

ShardingSphere-Proxy 定位为透明化的数据库代理端，通过实现数据库二进制协议，对异构语言提供支持。

## 产品功能

|特性|定义|
|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|数据分片  |数据分片是一种数据库分布式技术，ShardingSphere 可将一个大的数据库（或表）以某种维度切分成多个小的数据库（或表），构建出可有效的应对海量数据存储及更密集请求的分布式数据库解决方案。|
|分布式事务|ShardingSphere 对外提供本地事务接口，通过 LOCAL，XA，BASE 三种模式提供了分布式事务的能力。                                                                                |
|读写分离  |在读请求明显多于写请求的业务场景中，通过 ShardingSphere 读写分离技术可大幅度提升系统吞吐能力，即主库负责处理事务性的增删改请求，从库只负责处理查询请求。                        |
|高可用    |ShardingSphere 自身提供计算节点，并通过数据库作为存储节点。 它采用的高可用方案是利用数据库自身的高可用方案做存储节点高可用，并自动识别其变化。                                  |
|数据迁移  |ShardingSphere 为用户提供了基于数据全场景迁移能力，可应对业务数据量激增的场景。                                                                                           |
|联邦查询  |ShardingSphere 联邦查询适合于跨越数据库之间的关联查询和子查询。                                                                                                         |
|数据加密  |ShardingSphere 根据业界对加密的需求及业务改造痛点，提供了一套完整、安全、透明化、低改造成本的数据加密整合解决方案。                                                            |
|影子库    |在全链路压测场景中，ShardingSphere 影子库用于存储压测数据，为复杂的全链路压测工作提供数据隔离支持，获得的测试结果能够准确地反应系统真实容量和性能水平。                         |

## 产品优势

* 极致性能

驱动程序端历经多年打磨，效率接近原生 JDBC，性能极致。

* 生态兼容

代理端支持任何通过 MySQL/PostgreSQL 协议的应用访问，驱动程序端可对接任意实现 JDBC 规范的数据库。

* 业务零侵入

面对数据库替换场景，ShardingSphere 可满足业务无需改造，实现平滑业务迁移。

* 运维低成本

在保留原技术栈不变前提下，对 DBA 学习、管理成本低，交互友好。

* 安全稳定

基于成熟数据库底座之上提供增量能力，兼顾安全性及稳定性。

* 弹性扩展

具备计算、存储平滑在线扩展能力，可满足业务多变的需求。

* 开放生态

通过多层次（内核、功能、生态）插件化能力，为用户提供可定制满足自身特殊需求的独有系统。

## 线路规划

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap_v2_cn.png)

## 如何参与

ShardingSphere 已于 2020 年 4 月 16 日成为 [Apache 软件基金会](https://apache.org/index.html#projects-list)的顶级项目。
欢迎通过[邮件列表](mailto:dev@shardingsphere.apache.org)参与讨论。
