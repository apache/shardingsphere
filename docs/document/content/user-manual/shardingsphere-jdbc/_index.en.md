+++
pre = "<b>4.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
chapter = true
+++

## Introduction

As the first product and the predecessor of Apache ShardingSphere, 
ShardingSphere-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. 
With the client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Support any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of JDBC standard database: MySQL, Oracle, SQLServer, PostgreSQL and any SQL92 followed databases.

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc-brief.png)

## Comparison

|                        | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* | *ShardingSphere-Sidecar* |
| ---------------------- | --------------------- | ---------------------- | ------------------------ |
| Database               | `Any`                 | MySQL/PostgreSQL       | MySQL/PostgreSQL         |
| Connections Count Cost | `More`                | Less                   | More                     |
| Supported Languages    | `Java Only`           | Any                    | Any                      |
| Performance            | `Low loss`            | Relatively High loss   | Low loss                 |
| Decentralization       | `Yes`                 | No                     | No                       |
| Static Entry           | `No`                  | Yes                    | No                       |

ShardingSphere-JDBC is suitable for java application.
