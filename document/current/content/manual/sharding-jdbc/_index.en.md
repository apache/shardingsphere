+++
pre = "<b>4.1. </b>"
title = "Sharding-JDBC"
weight = 1
chapter = true
+++

## Introduction

As the first product as well as the predecessor of ShardingSphere, Sharding-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. 
With client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

- Applicable in any ORM framework based on Java, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.

- Based on any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.

- Support any kind of database that conforms to JDBC standard: MySQL，Oracle，SQLServer and PostgreSQL for now.

![Sharding-JDBC Architecture](http://shardingsphere.jd.com/document/current/img/sharding-jdbc-brief.png)

## Comparison

|                        | *Sharding-JDBC* | *Sharding-Proxy*     | *Sharding-Sidecar* |
| ---------------------- | --------------- | -------------------- | ------------------ |
| Database               | `Any`           | MySQL                | MySQL              |
| Connections Cost       | `More`          | Less                 | More               |
| Heterogeneous Language | `Java Only`     | Any                  | Any                |
| Performance            | `Low loss`      | Relatively High loss | Low loss           |
| Decentralization       | `Yes`           | No                   | No                 |
| Static Entry           | `No`            | Yes                  | No                 |

Sharding-JDBC is suitable for java only application.
