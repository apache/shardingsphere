+++
pre = "<b>1. </b>"
title = "Overview"
weight = 1
chapter = true
+++

## What is ShardingSphere

### Introduction

Apache ShardingSphere is an open source ecosystem that allows you to transform any database into a distributed database system. 
The project includes a JDBC and a Proxy, and its core adopts a micro-kernel and pluggable architecture.
Thanks to its plugin-oriented architecture, features can be flexibly expanded at will.

The project is committed to providing a multi-source heterogeneous, enhanced database platform and further building an ecosystem around the upper layer of the platform.
Database Plus, the design philosophy of Apache ShardingSphere, aims at building the standard and ecosystem on the upper layer of the heterogeneous database.
It focuses on how to make full and reasonable use of the computing and storage capabilities of existing databases rather than creating a brand new database.
It attaches greater importance to the collaboration between multiple databases instead of the database itself.

#### ShardingSphere-JDBC

[![Maven Status](https://img.shields.io/maven-central/v/org.apache.shardingsphere/shardingsphere-jdbc.svg?color=green)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

ShardingSphere-JDBC is a lightweight Java framework that provides additional services at Java's JDBC layer.

#### ShardingSphere-Proxy

[![Nightly-Download](https://img.shields.io/badge/nightly--builds-download-orange.svg)](https://nightlies.apache.org/shardingsphere/)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](/cn/downloads/)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/shardingsphere-proxy.svg)](https://hub.docker.com/r/apache/shardingsphere-proxy)

ShardingSphere-Proxy is a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages.

### Product Features

| Feature                 | Definition |
| ----------------------- | ---------- |
| Data Sharding           | Data sharding is an effective way to deal with massive data storage and computing. ShardingSphere provides distributed database solutions that can scale out computing and storage levels on top of the underlying database. |
| Distributed Transaction | Transactional capability is key to ensuring database integrity and security and is also one of the databases' core technologies. ShardingSphere provides distributed transaction capability on top of a single database, which can achieve data security across underlying data sources. |
| Read/write Splitting    | Read/write splitting can be used to cope with business access with high stress. Based on its understanding of SQL semantics and the topological awareness of the underlying database, ShardingSphere provides flexible and secure read/write splitting capabilities and can achieve load balancing for read access. |
| High Availability       | High availability is a basic requirement for a data storage and computing platform. ShardingSphere provides access to high-availability computing services based on stateless services. At the same time, it can sense and use the underlying database's HA solution to achieve its overall high availability. |
| Data Migration          | Data migration is the key to connecting data ecosystems. ShardingSphere provides full-scenario data migration capability for users, which can cope with the surge of business data volume. |
| Federated Query         | Federated queries are effective in utilizing data in a complex data environment. ShardingSphere is capable of querying and analyzing complex data across data sources, simplifying and improving the data usage experience. |
| Data Encryption         | Data Encryption is a basic way to ensure data security. ShardingSphere provides a set of data encryption solutions that are complete, secure, transparent, and with low transformation costs. |
| Shadow Database         | In the full-link stress testing scenario, ShardingSphere shadow DB is used for providing data isolation support for complex testing work. The obtained testing result can accurately reflect the system's true capacity and performance. |

### Advantages

- Ultimate Performance 

Having been polished for years, the driver is close to a native JDBC in terms of efficiency, with ultimate performance.

- Ecosystem Compatibility

The proxy can be accessed by any application using MySQL/PostgreSQL protocol, and the driver can connect to any database that implements JDBC specifications.

- Zero Business Intrusion

In response to database switchover scenarios, ShardingSphere can achieve smooth business migration without business intrusion.

- Low Ops & Maintenance Cost

ShardingSphere offers a flat learning curve to DBAs and is interaction-friendly while allowing the original technology stack to remain unchanged.

- Security & Stability

It can provide enhancement capability based on mature databases while ensuring security and stability.

- Elastic Extention

It supports computing, storage, and smooth online expansion, which can meet diverse business needs.

- Open Ecosystem

It can provide users with flexibility thanks to custom systems based on multi-level (kernel, feature, and ecosystem) plugin capabilities.

### Roadmap

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap_en.png)

### How to Contribute

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top-Level Project on April 16, 2020.
You are welcome to check out the mailing list and discuss via [mail](mailto:dev@shardingsphere.apache.org).

## Design Philosophy

ShardingSphere adopts the database plus design philosophy, which is committed to building the standards and ecology of the upper layer of the database and supplementing the missing capabilities of the database in the ecology.

![Design](https://shardingsphere.apache.org/document/current/img/design_en.png)

### Connect: Create database upper level standard

 Through flexible adaptation of database protocols, SQL dialects, and database storage, it can quickly build standards on top of multi-modal heterogeneous databases, while providing standardized connection mode for applications through built-in DistSQL.

### Enhance: Database computing enhancement engine

It can further provide distributed capabilities and traffic enhancement functions based on native database capabilities. The former can break through the bottleneck of the underlying database in computing and storage, while the latter provides more diversified data application enhancement capabilities through traffic deformation, redirection, governance, authentication, and analysis.

### Pluggable: Building database function ecology

![Overview](https://shardingsphere.apache.org/document/current/img/overview_en.png)

The pluggable architecture of Apache ShardingSphere is composed of three layers - L1 Kernel Layer, L2 Feature Layer and L3 Ecosystem Layer.

#### L1 Kernel Layer

An abstraction of databases' basic capabilities.
All the components are required and the specific implementation method can be replaced thanks to plugins.
It includes a query optimizer, distributed transaction engine, distributed execution engine, permission engine and scheduling engine.

#### L2 Feature Layer

Used to provide enhancement capabilities.
All components are optional, allowing you to choose whether to include zero or multiple components.
Components are isolated from each other, and multiple components can be used together by overlaying.
It includes data sharding, read/write splitting, database high availability, data encryption and shadow database and so on.
The user-defined feature can be fully customized and extended for the top-level interface defined by Apache ShardingSphere without changing kernel codes.

#### L3 Ecosystem Layer

It is used to integrate and merge the current database ecosystems.
The ecosystem layer includes database protocol, SQL parser and storage adapter, corresponding to the way in which Apache ShardingSphere provides services by database protocol, the way in which SQL dialect operates data, and the database type that interacts with storage nodes.

## Deployment

### Deployment

Apache ShardingSphere includes two independent clients: ShardingSphere-JDBC & ShardingSphere-Proxy. They all provide functions of data scale-out, distributed transaction and distributed governance, applicable in a variety of scenarios such as Java isomorphism, heterogeneous languages, and a cloud-native environment.

#### Independent ShardingSphere-JDBC

ShardingSphere-JDBC is a lightweight Java framework that provides additional services at Java's JDBC layer.
With the client connecting directly to the database, it provides services in the form of jar and requires no extra deployment and dependence.
It can be considered as an enhanced version of the JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

- Applicable in any ORM framework based on JDBC, such as JPA, Hibernate, Mybatis, Spring JDBC Template, or direct use of JDBC;
- Support any third-party database connection pool, such as DBCP, C3P0, BoneCP, HikariCP;
- Support any kind of JDBC standard database: MySQL, PostgreSQL, Oracle, SQLServer and any JDBC adapted databases.

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc_v3.png)

|                        | ShardingSphere-JDBC | ShardingSphere-Proxy |
| ---------------------- | ------------------- | -------------------- |
| Database               | `Any`               | MySQL/PostgreSQL     |
| Connections Count Cost | `More`              | Less                 |
| Heterogeneous language | `Java Only`         | Any                  |
| Performance            | `Low loss`          | Relatively High loss |
| Decentralization       | `Yes`               | No                   |
| Static entry           | `No`                | Yes                  |

#### Independent ShardingSphere-Proxy

ShardingSphere-Proxy is a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages.
Currently, MySQL and PostgreSQL protocols are provided.
It can use any kind of terminal that is compatible with MySQL or PostgreSQL protocol to operate data, which is more friendly to DBAs.

- Transparent to applications, it can be used directly as MySQL/PostgreSQL;
- Compatible with MySQL-based databases, such as MariaDB, and PostgreSQL-based databases, such as openGauss;
- Applicable to any kind of client that is compatible with MySQL/PostgreSQL protocol, such as MySQL Command Client, MySQL Workbench, etc.

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

|                        | ShardingSphere-JDBC | ShardingSphere-Proxy   |
| ---------------------- | ------------------- | ---------------------- |
| Database               | Any                 | `MySQL/PostgreSQL`     |
| Connections Count Cost | More                | `Less`                 |
| Heterogeneous language | Java Only           | `Any`                  |
| Performance            | Low loss            | `Relatively High loss` |
| Decentralization       | Yes                 | `No`                   |
| Static entry           | No                  | `Yes`                  |

#### Hybrid Architecture

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP applications developed with Java.
ShardingSphere-Proxy provides static entry and supports all languages, applicable to OLAP applications and the sharding databases management and operation situation.

Apache ShardingSphere is an ecosystem composed of multiple access ports.
By combining ShardingSphere-JDBC and ShardingSphere-Proxy, and using the same registry to configure sharding strategies, it can flexibly build application systems for various scenarios, allowing architects to freely adjust the system architecture according to the current businesses. 

![ShardingSphere Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid-architecture_v2.png)

### Running Modes

Apache ShardingSphere provides two running modes: standalone mode and cluster mode.

#### Standalone mode

It can achieve data persistence in terms of metadata information such as data sources and rules,
but it is not able to synchronize metadata to multiple Apache ShardingSphere instances or be aware of each other in a cluster environment.
Updating metadata through one instance causes inconsistencies in other instances because they cannot get the latest metadata.

It is ideal for engineers to build a ShardingSphere environment locally.

#### Cluster mode

It provides metadata sharing between multiple Apache ShardingSphere instances and the capability to coordinate states in distributed scenarios.

It provides the capabilities necessary for distributed systems, such as horizontal scaling of computing capability and high availability.
Clustered environments need to store metadata and coordinate nodes' status through a separately deployed registry center.

We suggest using cluster mode in production environment.
