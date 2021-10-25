+++
pre = "<b>3.1. </b>"
title = "Adaptor"
weight = 1
chapter = true
+++

Apache ShardingSphere including 2 independent products: ShardingSphere-JDBC & ShardingSphere-Proxy.
They all provide functions of data scale-out, distributed transaction and distributed governance,
applicable in a variety of situations such as Java isomorphism, heterogeneous language and Cloud-Native.

## ShardingSphere-JDBC

As the first product and the predecessor of Apache ShardingSphere,
ShardingSphere-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer.
With the client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence.
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Support any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of JDBC standard database: MySQL, Oracle, SQLServer, PostgreSQL and any SQL92 followed databases.

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc_v3.png)

|                        | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* |
| ---------------------- | --------------------- | ---------------------- |
| Database               | `Any`                 | MySQL/PostgreSQL       |
| Connections Count Cost | `More`                | Less                   |
| Supported Languages    | `Java Only`           | Any                    |
| Performance            | `Low loss`            | Relatively High loss   |
| Decentralization       | `Yes`                 | No                     |
| Static Entry           | `No`                  | Yes                    |

ShardingSphere-JDBC is suitable for java application.

## ShardingSphere-Proxy

ShardingSphere-Proxy is the second product of Apache ShardingSphere.
It defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages.
Currently, MySQL and PostgreSQL (compatible with PostgreSQL-based databases, such as openGauss) versions are provided. It can use any kind of terminal (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible of MySQL or PostgreSQL protocol to operate data, which is friendlier to DBAs

* Totally transparent to applications, it can be used directly as MySQL/PostgreSQL.
* Applicable to any kind of client end that is compatible with MySQL/PostgreSQL protocol.

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

|                          | *ShardingSphere-JDBC* | *ShardingSphere-Proxy*       |
| ------------------------ | --------------------- | ---------------------------- |
| Database                 | Any                   | `MySQL/PostgreSQL`           |
| Connections Count Cost   | High                  | `Low`                        |
| Supported Languages      | Java Only             | `Any`                        |
| Performance              | Low loss              | `Relatively high loss`       |
| Decentralization         | Yes                   | `No`                         |
| Static Entry             | No                    | `Yes`                        |

The advantages of ShardingSphere-Proxy lie in supporting heterogeneous languages and providing operational entries for DBA.

## Hybrid Adaptors

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP application developed with Java.
ShardingSphere-Proxy provides static entry and all languages support, applicable for OLAP application and the sharding databases management and operation situation.

ShardingSphere is an ecosystem consisting of multiple endpoints together.
Through a mixed use of ShardingSphere-JDBC and ShardingSphere-Proxy and a unified sharding strategy by the same registry center, ShardingSphere can build an application system that is applicable to all kinds of scenarios.
Architects can adjust the system architecture to the most applicable one to their needs to conduct business more freely.

![Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid-architecture_v2.png)
