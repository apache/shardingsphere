+++
pre = "<b>4.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
chapter = true
+++

## 简介

ShardingSphere-JDBC 是 Apache ShardingSphere 的第一个产品，也是 Apache ShardingSphere 的前身。
定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。
它使用客户端直连数据库，以 jar 包形式提供服务，无需额外部署和依赖，可理解为增强版的 JDBC 驱动，完全兼容 JDBC 和各种 ORM 框架。

* 适用于任何基于 JDBC 的 ORM 框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template 或直接使用 JDBC。
* 支持任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP 等。
* 支持任意实现 JDBC 规范的数据库，目前支持 MySQL，Oracle，SQLServer，PostgreSQL 以及任何遵循 SQL92 标准的数据库。

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc-brief.png)

## 对比

|           | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* | *ShardingSphere-Sidecar* |
| --------- | --------------------- | ---------------------- | ------------------------ |
| 数据库     | `任意`                | MySQL/PostgreSQL       | MySQL/PostgreSQL          |
| 连接消耗数 | `高`                  | 低                      | 高                        |
| 异构语言   | `仅Java`              | 任意                    | 任意                      |
| 性能       | `损耗低`              | 损耗略高                | 损耗低                     |
| 无中心化   | `是`                  | 否                     | 是                         |
| 静态入口   | `无`                  | 有                     | 无                         |

ShardingSphere-JDBC 的优势在于对 Java 应用的友好度。
