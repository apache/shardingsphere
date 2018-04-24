+++
pre = "<b>6. </b>"
title = "Sharding-JDBC"
weight = 6
chapter = true
+++

## 简介

Sharding-JDBC是Sharding-Sphere的第一个产品，也是Sharding-Sphere的前身。
Sharding-JDBC通过客户端分片的方式由应用程序直连数据库，减少二次转发成本，性能最高，适合线上程序使用。

主要包括以下特点：

1. 可适用于任何基于Java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC。
1. 可基于任何第三方的数据库连接池，如：DBCP, BoneCP, HikariCP, Druid等。
1. 可支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL。

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/jdbc_brief_cn.png)

## 对比

|         | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ------- | --------------- | ---------------- | ------------------ |
| 数据库   |  `任意`         |   MySQL          | MySQL               |
| 连接数   |   `高`          |   低             | 高                  |
| 异构语言 |   `仅Java`      |   任意            | 任意                |
| 性能     |  `损耗低`       |   损耗略高        | 损耗低               |
| 无中心化 |  `是`           |   否             | 是                   |
| 静态入口 |  `无`           |   有             | 无                   |

与Sharding-Proxy和Sharding-Sidecar相比，Sharding-JDBC的优势在于对Java应用的友好度。
