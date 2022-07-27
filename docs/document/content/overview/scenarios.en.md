+++
pre = "<b>1.5 </b>"
title = "Application Scenarios"
weight = 5
chapter = true
+++

Apache ShardingSphere includes two independent clients: ShardingSphere-JDBC & ShardingSphere-Proxy. They all provide functions of data scale-out, distributed transaction and distributed governance, applicable in a variety of scenarios such as Java isomorphism, heterogeneous languages, and a cloud-native environment.

## ShardingSphere-JDBC

[![Maven Status](https://img.shields.io/maven-central/v/org.apache.shardingsphere/shardingsphere-jdbc.svg?color=green)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

As the community's first product and the predecessor of Apache ShardingSphere, ShardingSphere-JDBC is a lightweight Java framework that provides additional services at Java's JDBC layer. With the client connecting directly to the database, it provides services in the form of jar and requires no extra deployment and dependence. It can be considered as an enhanced version of the JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.
- Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template, or direct use of JDBC;
- Support any third-party database connection pool, such as DBCP, C3P0, BoneCP, HikariCP;
- Support any kind of JDBC standard database: MySQL, PostgreSQL, Oracle, SQLServer and any JDBC adapted databases.

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc_v3.png)


||	ShardingSphere-JDBC|	ShardingSphere-Proxy|
|---|---|---|
|Database|	Any|	MySQL/PostgreSQL|
|Connections Count Cost|	More	|Less|
|Heterogeneous language	|Java Only	|Any|
|Performance|	Low loss	|Relatively High loss|
|Decentralization|	Yes|	No|
|Static entry|	No	| Yes |

ShardingSphere-JDBC is suitable for java applications.

Source Codes: [https://github.com/apache/shardingsphere/tree/master/shardingsphere-jdbc](https://github.com/apache/shardingsphere/tree/master/shardingsphere-jdbc)

## ShardingSphere-Proxy

[![Nightly-Release](https://img.shields.io/badge/nightly--builds-download-orange.svg)](https://nightlies.apache.org/shardingsphere/)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](/cn/downloads/)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/shardingsphere-proxy.svg)](https://hub.docker.com/r/apache/shardingsphere-proxy)

ShardingSphere-Proxy is Apache ShardingSphere's second product. It is a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 

Currently, MySQL and PostgreSQL (compatible with PostgreSQL-based databases, such as openGauss) versions are provided. It can use any kind of terminal (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible with MySQL or PostgreSQL protocol to operate data, which is more friendly to DBAs.

- Transparent to applications, it can be used directly as MySQL/PostgreSQL;
- Applicable to any kind of client that is compatible with MySQL/PostgreSQL protocol.

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

	
||	ShardingSphere-JDBC	|ShardingSphere-Proxy|
|---|---|---|
|Database	|Any	|MySQL/PostgreSQL|
|Connections Count Cost	|More|	Less|
|Heterogeneous language	|Java Only|	Any|
|Performance|	Low loss	|Relatively High loss|
|Decentralization|	Yes|	No|
|Static entry|	No	|Yes|

The advantages of ShardingSphere-Proxy lie in supporting heterogeneous languages and providing operational entries for DBA.

Source Codes: [https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy](https://github.com/apache/shardingsphere/tree/master/shardingsphere-proxy)

## Hybrid Architecture

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP applications developed with Java. ShardingSphere-Proxy provides static entry and supports all languages, applicable to OLAP applications and the sharding databases management and operation situation.

Apache ShardingSphere is an ecosystem composed of multiple access ports. By combining ShardingSphere-JDBC and ShardingSphere-Proxy, and using the same registry to configure sharding strategies, it can flexibly build application systems for various scenarios, allowing architects to freely adjust the system architecture according to the current businesses. 

![ShardingSphere Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid-architecture_v2.png)
