+++
pre = "<b>4.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
chapter = true
+++

## Introduction

ShardingSphere-Proxy is the second product of ShardingSphere. It defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. Friendlier to DBA, the MySQL/PostgreSQL version provided now can use any kind of terminal (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible with MySQL protocol to operate data.

- Totally transparent to applications, it can be used directly as MySQL/PostgreSQL.
- Applicable to any kind of client end that is compatible with MySQL/PostgreSQL protocol.

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy-brief.png)

## Comparison

|                          | *ShardingSphere-JDBC* | *ShardingSphere-Proxy*       | *ShardingSphere-Sidecar* |
| ------------------------ | --------------------- | ---------------------------- | ------------------------ |
| Database                 | Any                   | `MySQL`                      | MySQL                    |
| Connections Count Cost   | High                  | `Low`                        | High                     |
| Supported Languages      | Java Only             | `Any`                        | Any                      |
| Performance              | Low loss              | `Relatively high loss`       | Low loss                 |
| Decentralization         | Yes                   | `No`                         | Yes                      |
| Static Entry             | No                    | `Yes`                        | No                       |

The advantages of ShardingSphere-Proxy lie in supporting heterogeneous languages and providing operational entries for DBA.