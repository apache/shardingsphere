+++
pre = "<b>4.3. </b>"
title = "Sharding-Sidecar"
weight = 3
chapter = true
+++

## Introduction

Sharding-Sidecar is third project of ShardingSphere, we just plan to do it in future.
It can mesh interactions between applications and databases, must be running in Kubernetes or Mesos environment.
It is a centre-less solution, and can support any languages, we call it as `Database Mesh`.

Database Mesh is focused on how to connect the distributed data-access-layer and databases together. It pays more attention on interaction, which means the messy interaction among the applications and databases will be effectively orchestrate. By using Database Mesh, applications and databases will form a large grid system, and they just need to be put into the right position on grid system accordingly.

![Sharding-Sidecar Architecture](https://shardingsphere.apache.org/document/current/img/sharding-sidecar-brief_v2.png)

## Comparison

|                        | *Sharding-JDBC* | *Sharding-Proxy* | *Sharding-Sidecar* |
| ---------------------- | --------------- | ---------------- | ------------------ |
| Database               | Any             | MySQL            | `MySQL`            |
| Connections Cost       | More            | Less             | `More`             |
| Heterogeneous Language | Java Only       | Any              | `Any`              |
| Performance            | Low loss        | High loss        | `Low loss`         |
| Centre-less            | Yes             | No               | `No`               |
| Static Entry           | No              | Yes              | `No`               |

Sharding-Proxy can support heterogeneous languages and provide an operation entry for DBA.
