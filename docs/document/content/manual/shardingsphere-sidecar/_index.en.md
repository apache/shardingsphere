+++
pre = "<b>4.3. </b>"
title = "Sharding-Sidecar"
weight = 3
chapter = true
+++

## Introduction

Sharding-Sidecar (TODO) defines itself as a cloud native database agent of the Kubernetes environment, in charge of all the access to the database in the form of sidecar.

It provides a mesh layer interacting with the database, we call this as `Database Mesh`.

![Sharding-Sidecar Architecture](https://shardingsphere.apache.org/document/current/img/sharding-sidecar-brief_v2.png)


## Comparison

|                         | *Sharding-JDBC* | *Sharding-Proxy*     | *Sharding-Sidecar* |
| :---------------------- | :-------------- | :------------------- | :----------------- |
| Database                | Any             | MySQL                | `MySQL`            |
| Connections Count Cost  | High            | Low                  | `High`             |
| Supported Languages     | Java Only       | Any                  | `Any`              |
| Performance             | Low loss        | Relatively High loss | `Low loss`         |
| Decentralization        | Yes             | No                   | `Yes`              |
| Static Entry            | No              | Yes                  | `No`               |

The advantage of Sharding-Sidecar lies in its cloud native support for Kubernetes and Mesos.