+++
pre = "<b>4.3. </b>"
title = "ShardingSphere-Sidecar"
weight = 3
chapter = true
+++

## 简介

ShardingSphere-Sidecar 是 ShardingSphere 的第三个产品，目前仍然在`规划中`。
定位为 Kubernetes 或 Mesos 的云原生数据库代理，以 DaemonSet 的形式代理所有对数据库的访问。

通过无中心、零侵入的方案提供与数据库交互的的啮合层，即 Database Mesh，又可称数据网格。
Database Mesh 的关注重点在于如何将分布式的数据访问应用与数据库有机串联起来，它更加关注的是交互，是将杂乱无章的应用与数据库之间的交互进行有效地梳理。使用 Database Mesh，访问数据库的应用和数据库终将形成一个巨大的网格体系，应用和数据库只需在网格体系中对号入座即可，它们都是被啮合层所治理的对象。

![ShardingSphere-Sidecar Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-sidecar-brief.png)

## 对比

|          | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* | *ShardingSphere-Sidecar* |
| -------- | --------------------- | ---------------------- | ------------------------ |
| 数据库    | 任意                  | MySQL/PostgreSQL       | `MySQL/PostgreSQL`        |
| 连接消耗数 | 高                   | 低                     | `高`                       |
| 异构语言   | 仅Java               | 任意                    | `任意`                    |
| 性能      | 损耗低                | 损耗略高                | `损耗低`                   |
| 无中心化  | 是                    | 否                     | `是`                       |
| 静态入口  | 无                    | 有                     | `无`                       |

ShardingSphere-Sidecar 的优势在于对 Kubernetes 和 Mesos 的云原生支持。
