+++
pre = "<b>6. </b>"
title = "Sharding-JDBC"
weight = 6
chapter = true
+++

## Introduction

Sharding-JDBC is first project of Sharding-Sphere, it is precursor of Sharding-Sphere.
Sharding-JDBC use JDBC connect databases without redirect cost for java application, best performance for production.

1. ORM compatible. JPA, Hibernate, Mybatis, Spring JDBC Template or JDBC supported.
1. Connection-pool compatible. DBCP, BoneCP, Druid supported.
1. Multi SQL-based databases compatible. Any Database supported theoretically. Support MySQL, Oracle, SQLServer and PostgreSQL right now.

![Sharding-JDBC Architecture](http://ovfotjrsi.bkt.clouddn.com/jdbc_brief_en.png)

## Comparison

|                        | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ---------------------- | --------------- | ---------------- | ------------------ |
| Database               | `Any`           | MySQL            | MySQL              |
| Connections            | `More`          | Less             | More               |
| Heterogeneous Language | `Java Only`     | Any              | Nay                |
| Performance            | `Low loss`      | High loss        | Low loss           |
| Centre-less            | `Yes`           | No               | No                 |
| Static Entry           | `No`            | Yes              | No                 |


Sharding-JDBC is suitable for java only application.
