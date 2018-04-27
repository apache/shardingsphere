+++
pre = "<b>6. </b>"
title = "Sharding-JDBC"
weight = 6
chapter = true
+++

## 简介

Sharding-JDBC是Sharding-Sphere的第一个产品，也是Sharding-Sphere的前身。
它定位为轻量级Java框架，在Java的JDBC层提供的额外服务。它使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。

* 适用于任何基于Java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC。
* 基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP等。
* 支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL。

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-jdbc-brief.png)

## 对比

|         | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ------- | --------------- | ---------------- | ------------------ |
| 数据库   | `任意`          | MySQL            | MySQL               |
| 连接数   | `高`            | 低               | 高                  |
| 异构语言 | `仅Java`        | 任意              | 任意                |
| 性能     | `损耗低`        | 损耗略高          | 损耗低               |
| 无中心化 | `是`            | 否               | 是                   |
| 静态入口 | `无`            | 有               | 无                   |

Sharding-JDBC的优势在于对Java应用的友好度。
