+++
icon = "<b>0. </b>"
date = "2017-04-12T16:06:17+08:00"
title = "概览"
weight = 0
prev = "/03-design/roadmap/"
next = "/00-overview/intro/"
chapter = true

+++

# 概述

[![GitHub release](https://img.shields.io/github/release/shardingjdbc/sharding-jdbc.svg?style=social&label=Release)](https://github.com/shardingjdbc/sharding-jdbc/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/shardingjdbc/sharding-jdbc.svg?style=social&label=Star)](https://github.com/shardingjdbc/sharding-jdbc/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/shardingjdbc/sharding-jdbc.svg?style=social&label=Fork)](https://github.com/shardingjdbc/sharding-jdbc/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/shardingjdbc/sharding-jdbc.svg?style=social&label=Watch)](https://github.com/shardingjdbc/sharding-jdbc/watchers)

Sharding-JDBC是一个开源的适用于微服务的分布式数据访问基础类库，它始终以云原生的基础开发套件为目标。

Sharding-JDBC定位为轻量级java框架，使用客户端直连数据库，以jar包形式提供服务，未使用中间层，无需额外部署，无其他依赖，DBA也无需改变原有的运维方式，可理解为增强版的JDBC驱动，旧代码迁移成本几乎为零。

Sharding-JDBC完整的实现了分库分表，读写分离和分布式主键功能，并初步实现了柔性事务。从2016年开源至今，在经历了整体架构的数次精炼以及稳定性打磨后，如今它已积累了足够的底蕴，相信可以成为开发者选择技术组件时的一个参考。

[![Build Status](https://secure.travis-ci.org/shardingjdbc/sharding-jdbc.svg?branch=master)](https://travis-ci.org/shardingjdbc/sharding-jdbc)
[![Maven Status](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.shardingjdbc/sharding-jdbc)
[![Coverage Status](https://coveralls.io/repos/shardingjdbc/sharding-jdbc/badge.svg?branch=master&service=github)](https://coveralls.io/github/shardingjdbc/sharding-jdbc?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# 功能列表

## 1. 分库分表
* SQL解析功能完善，支持聚合，分组，排序，LIMIT，TOP等查询，并且支持级联表以及笛卡尔积的表查询
* 支持内、外连接查询
* 分片策略灵活，可支持=，BETWEEN，IN等多维度分片，也可支持多分片键共用，以及自定义分片策略
* 基于Hint的强制分库分表路由

## 2. 读写分离
* 独立使用读写分离支持sql透传
* 一主多从的读写分离配置，可配合分库分表使用
* 基于Hint的强制主库路由

## 3. 柔性事务
* 最大努力送达型事务
* TCC型事务(TBD)

## 4. 分布式主键
* 统一的分布式基于时间序列的ID生成器

## 5. 兼容性
* 可适用于任何基于java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC
* 可基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid等
* 理论上可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL

## 6. 灵活多样的配置
* Java
* YAML
* Inline表达式
* Spring命名空间
* Spring boot starter

## 7. 分布式治理能力 (2.0新功能)

* 配置集中化与动态化，可支持数据源、表与分片策略的动态切换(2.0.0.M1)
* 客户端的数据库治理，数据源失效自动切换(2.0.0.M2)
* 基于Open Tracing协议的APM信息输出(2.0.0.M3)

# 交流与参与

 - **官方群(目前已满，请加官方2群)：** 532576663（仅限于讨论与Sharding-JDBC相关的话题。我们希望您在入群前仔细阅读文档。并在入群后阅读公告以及修改群名片。谢谢合作）
 - **官方2群：** 459894627
 - **源码交流群：** 659205143（仅限于讨论与Sharding-JDBC源码实现相关的话题。我们欢迎您在这里与我们交流Sharding-JDBC的架构设计、代码实现以及未来线路规划。此群需要对Sharding-JDBC有先期了解。入群资格：请发布一篇关于Sharding-JDBC的源码分析的文章并将链接通过官方交流群发送给我们。）
 - 报告确定的bug，提交增强功能建议和提交补丁等，请阅读[如何进行贡献](/00-overview/contribution)。
 
 **使用Sharding-JDBC的公司如果方便请留下公司+网址** https://github.com/shardingjdbc/sharding-jdbc/issues/234
