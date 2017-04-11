+++
icon = "<b>1. </b>"
date = "2016-12-02T16:06:17+08:00"
title = "起航"
weight = 0
next = "/01-start/faq"
chapter = true

+++

# 本章导航

 - 如果想快速体验`Sharding-JDBC`的强大功能，请阅读[快速上手](/01-start/quick-start)。
 - 如果在使用中遇到什么问题，请首先在[FAQ](/01-start/faq)中寻找解决问题的答案。
 - 想要在自己的项目中集成`Sharding-JDBC`,您一定很关心它[能做什么](/01-start/features)和[不能做什么](/01-start/limitations)。
 - 数据库中间件一般都不会支持完全的SQL语法,在这里你能了解到[支持的SQL语法](/01-start/sql-supported)。
 - 最后，你会关心中间件的性能情况。这里将为你我们的[性能测试结果](/01-start/stress-test)。

# 简介

* `Sharding-JDBC`直接封装`JDBC API`，可以理解为增强版的`JDBC`驱动，旧代码迁移成本几乎为零
 * 可适用于任何基于`java`的`ORM`框架，如：`JPA`, `Hibernate`, `Mybatis`, `Spring JDBC Template`或直接使用`JDBC`。
 * 可基于任何第三方的数据库连接池，如：`DBCP`, `C3P0`, `BoneCP`, `Druid`等。
 * 理论上可支持任意实现`JDBC`规范的数据库。虽然目前仅支持`MySQL`，但已有支持`Oracle`，`SQLServer`，`DB2`等数据库的计划。
* `Sharding-JDBC`功能灵活且全面
 * 分片策略灵活，可支持`=`，`BETWEEN`，`IN`等多维度分片，也可支持多分片键共用。
 * `SQL`解析功能完善，支持聚合，分组，排序，`Limit`，`OR`等查询，并且支持`Binding Table`以及笛卡尔积的表查询。
 * 支持柔性事务(目前仅最大努力送达型)。
 * 支持读写分离。
 * 支持生成唯一有顺序主键
* `Sharding-JDBC`配置多样
 * 可支持YAML和Spring命名空间配置
 * 灵活多样的`inline`方式


# 整体架构图

![整体架构图1](/img/architecture.png)