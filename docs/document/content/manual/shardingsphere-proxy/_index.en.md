+++
pre = "<b>4.2. </b>"
title = "Sharding-Proxy"
weight = 2
chapter = true
+++

## Introduction

Sharding-Proxy is the second product of ShardingSphere. It defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. Friendlier to DBA, the MySQL/PostgreSQL version provided now can use any kind of terminal (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible with MySQL protocol to operate data.

- Totally transparent to applications, it can be used directly as MySQL/PostgreSQL.
- Applicable to any kind of client end that is compatible with MySQL/PostgreSQL protocol.

![Sharding-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/sharding-proxy-brief_v2.png)

## Comparison

|                          | *Sharding-JDBC* | *Sharding-Proxy*       | *Sharding-Sidecar* |
| ------------------------ | --------------- | ---------------------- | ------------------ |
| Database                 | Any             | `MySQL`                | MySQL              |
| Connections Count Cost   | High            | `Low`                  | High               |
| Supported Languages      | Java Only       | `Any`                  | Any                |
| Performance              | Low loss        | `Relatively high loss` | Low loss           |
| Decentralization         | Yes             | `No`                   | Yes                |
| Static Entry             | No              | `Yes`                  | No                 |

The advantages of Sharding-Proxy lie in supporting heterogeneous languages and providing operational entries for DBA.