+++
pre = "<b>4.3. </b>"
title = "Sharding-Sidecar"
weight = 3
chapter = true
+++

## Introduction

Sharding-Sidecar defines itself as a cloud native database agent of Kubernetes or Mesos, in charge of all the access to database inDaemonSet form.

Through a decentralized and zero-cost solution, it provides a mesh layer interacting with database, i.e., Database Mesh, also referred to as database grid.Database Mesh emphasizes on how to connect distributed database access application with the database. Focusing on interaction, it effectively organizes the interaction between messy applications and database. The application and database that use Database Mesh to visit database will form a large grid system, where they just need to be put into the right position accordingly. They are all governed by mesh layer.

![Sharding-Sidecar Architecture](https://shardingsphere.apache.org/document/current/img/sharding-sidecar-brief_v2.png)



## Comparison

|                         | *Sharding-JDBC* | *Sharding-Proxy*     | *Sharding-Sidecar* |
| :---------------------- | :-------------- | :------------------- | :----------------- |
| Database                | Any             | MySQL                | `MySQL`            |
| Connections Cost Number | High            | Low                  | `High`             |
| Heterogeneous Language  | Java Only       | Any                  | `Any`              |
| Performance             | Low loss        | Relatively High loss | `Low loss`         |
| Decentralization        | Yes             | No                   | `Yes`              |
| Static Entry            | No              | Yes                  | `No`               |

The advantage of Sharding-Sidecar lies in its cloud native support for Kubernetes and Mesos.