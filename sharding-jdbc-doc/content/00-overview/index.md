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

[![GitHub release](https://img.shields.io/github/release/dangdangdotcom/sharding-jdbc.svg?style=social&label=Release)](https://github.com/dangdangdotcom/sharding-jdbc/releases)&nbsp;
[![GitHub stars](https://img.shields.io/github/stars/dangdangdotcom/sharding-jdbc.svg?style=social&label=Star)](https://github.com/dangdangdotcom/sharding-jdbc/stargazers)&nbsp;
[![GitHub forks](https://img.shields.io/github/forks/dangdangdotcom/sharding-jdbc.svg?style=social&label=Fork)](https://github.com/dangdangdotcom/sharding-jdbc/fork)&nbsp;
[![GitHub watchers](https://img.shields.io/github/watchers/dangdangdotcom/sharding-jdbc.svg?style=social&label=Watch)](https://github.com/dangdangdotcom/sharding-jdbc/watchers)

Sharding-JDBC是当当应用框架ddframe中，关系型数据库模块dd-rdb中分离出来的数据库水平扩展框架，即透明化数据库分库分表访问。Sharding-JDBC继dubbox和Elastic-Job之后，是ddframe系列开源的第三个产品。

Sharding-JDBC定位为轻量级java框架，使用客户端直连数据库，以jar包形式提供服务，未使用中间层，无需额外部署，无其他依赖，DBA也无需改变原有的运维方式，可理解为增强版的JDBC驱动，旧代码迁移成本几乎为零。

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dangdang/sharding-jdbc)
[![Build Status](https://secure.travis-ci.org/dangdangdotcom/sharding-jdbc.svg?branch=master)](https://travis-ci.org/dangdangdotcom/sharding-jdbc)
[![Coverage Status](https://coveralls.io/repos/dangdangdotcom/sharding-jdbc/badge.svg?branch=master&service=github)](https://coveralls.io/github/dangdangdotcom/sharding-jdbc?branch=master)
[![Hex.pm](http://dangdangdotcom.github.io/sharding-jdbc/img/license.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# 功能列表

## 1. 分库分表
* SQL解析功能完善，支持聚合，分组，排序，LIMIT，OR等查询，并且支持级联表以及笛卡尔积的表查询
* 支持内、外连接查询
* 分片策略灵活，可支持=，BETWEEN，IN等多维度分片，也可支持多分片键共用，以及自定义分片策略
* 基于Hint的强制分库分表路由

## 2. 读写分离
* 一主多从的读写分离配置，可配合分库分表使用
* 基于Hint的强制主库路由

## 3. 分布式事务
* 最大努力送达型事务
* TCC型事务(TBD)

## 4. 兼容性
* 可适用于任何基于java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC
* 可基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid等
* 理论上可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL

## 5. 灵活多样的配置
* Java
* Spring命名空间
* YAML
* Inline表达式

## 6. 分布式生成全局主键
* 统一的分布式基于时间序列的ID生成器

# 第三方文章
* 2017-07 [InfoQ新闻：分布式数据库中间件Sharding-JDBC 1.5.0发布——自研SQL解析引擎+全数据库支持里程碑版本](http://www.infoq.com/cn/news/2017/08/Sharding-JDBC-150)
* 2017-07 [芋艿V的博客：Sharding-JDBC 源码分析](http://www.yunai.me/categories/Sharding-JDBC/?sjdbc)
* 2017-07 [DBAPlus分享：轻量级数据库中间件利器Sharding-JDBC深度解析](http://mp.weixin.qq.com/s/W-pBQInQKu99NLn2rHMI-Q)
* 2017-03 [开源中国高手问答：高手问答第144期—轻量级数据库中间层 Sharding-JDBC 深度解析](https://www.oschina.net/question/2720166_2233456)
* 2016-06 [InfoQ文章：Sharding-JDBC 1.3.0发布——支持读写分离](http://www.infoq.com/cn/news/2016/06/sharding-jdbc-130)
* 2016-02 [CSDN文章：解读分库分表中间件Sharding-JDBC](http://geek.csdn.net/news/detail/55513)
* 2016-01 [InfoQ新闻：当当开源sharding-jdbc，轻量级数据库分库分表中间件](http://www.infoq.com/cn/news/2016/01/sharding-jdbc-dangdang)

# 交流与参与

 - **问题交流群：** 532576663（仅限于讨论与Sharding-JDBC相关的话题。我们希望您在入群前仔细阅读文档。并在入群后阅读公告以及修改群名片。谢谢合作。）
 - **源码交流群：** 659205143（仅限于讨论与Sharding-JDBC源码实现相关的话题。我们欢迎您在这里与我们交流Sharding-JDBC的架构设计、代码实现以及未来线路规划。此群需要对Sharding-JDBC有先期了解，如果我们发现您暂时还未达到入群资格，会先将您请出。谢谢合作。）
 - 报告确定的bug，提交增强功能建议和提交补丁等，请阅读[如何进行贡献](/00-overview/contribution)。
 
 **使用Sharding-JDBC的公司如果方便请留下公司+网址** https://github.com/dangdangdotcom/sharding-jdbc/issues/234
  
# 采用公司（统计中）
