+++
pre = "<b>4.1. </b>"
title = "Sharding-JDBC"
weight = 1
chapter = true
+++

## Introduction

Sharding-JDBC is first project of Sharding-Sphere, it is precursor of Sharding-Sphere.
It uses JDBC to connect databases without redirect cost for java application, best performance for production.

* ORM compatible. JPA, Hibernate, Mybatis, Spring JDBC Template or JDBC supported.
* Connection-pool compatible. DBCP, C3P0, BoneCP, Druid supported.
* Multi SQL-based databases compatible. Any Database supported theoretically. Support MySQL, Oracle, SQLServer and PostgreSQL right now.

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/sharding-jdbc-brief.png)

## Comparison

|                        | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ---------------------- | --------------- | ---------------- | ------------------ |
| Database               | `Any`           | MySQL            | MySQL              |
| Connections Cost       | `More`          | Less             | More               |
| Heterogeneous Language | `Java Only`     | Any              | Any                |
| Performance            | `Low loss`      | High loss        | Low loss           |
| Centre-less            | `Yes`           | No               | No                 |
| Static Entry           | `No`            | Yes              | No                 |

Sharding-JDBC is suitable for java only application.
