+++
pre = "<b>1.1 </b>"
title = "What is ShardingSphere"
weight = 1
chapter = true
+++

## Introduction

Apache ShardingSphere is an open source ecosystem that allows you to transform any database into a distributed database system. 

The project includes a JDBC and a Proxy, and its core adopts a micro-kernel and pluggable architecture. Thanks to its plugin-oriented architecture, features can be flexibly expanded at will. 

The project is committed to providing a multi-source heterogeneous, enhanced database platform and further building an ecosystem around the upper layer of the platform.

Database Plus, the design philosophy of Apache ShardingSphere, aims at building the standard and ecosystem on the upper layer of the heterogeneous database. It focuses on how to make full and reasonable use of the computing and storage capabilities of existing databases rather than creating a brand new database. It attaches greater importance to the collaboration between multiple databases instead of the database itself.

### ShardingSphere-JDBC

[![Maven Status](https://img.shields.io/maven-central/v/org.apache.shardingsphere/shardingsphere-jdbc.svg?color=green)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

ShardingSphere-JDBC is a lightweight Java framework that provides additional services at Java's JDBC layer.

### ShardingSphere-Proxy

[![Nightly-Download](https://img.shields.io/badge/nightly--builds-download-orange.svg)](https://nightlies.apache.org/shardingsphere/)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](/cn/downloads/)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/shardingsphere-proxy.svg)](https://hub.docker.com/r/apache/shardingsphere-proxy)

ShardingSphere-Proxy is a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages.

## Product Features

|Feature                |Definition                                                                                                                                                                                                                                                                                                                     |
|-----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|Data Sharding          |Data sharding is a distributed database technology. ShardingSphere can split a large database (or table) into multiple small databases (or tables), and create a distributed database solution that can effectively cope with massive data storage and intensive requests.                                                     |
|Distributed Transaction|ShardingSphere provides local transaction interfaces and supports distributed transactions through LOCAL, XA, and BASE modes.                                                                                                                                                                                                  |
|Read/write Splitting   |In a scenario where the read requests greatly outnumber write requests, ShardingSphere's read/write splitting feature can significantly improve the system throughput. The primary database deals with transactional addition, deletion, and modification requests while the secondary database only deals with query requests.|
|High Availability (HA) |ShardingSphere itself provides compute nodes and serves as the storage node through databases. It leverages the database's HA solutions to achieve high availability of the storage node, and automatically identifies the changes.                                                                                            |
|Data Migration         |ShardingSphere provides full-scenario data migration capability for users, which can cope with the surge of business data volume.                                                                                                                                                                                              |
|Federated Query        |ShardingSphere federated query applies to associated queries and sub-queries across databases.                                                                                                                                                                                                                                 |
|Data Encryption        |Considering the industry's needs for encryption and the pain points of business transformation, ShardingSphere provides a set of integrated data encryption solutions that are complete, secure, transparent, and with low transformation cost.                                                                                |
|Shadow DB              |In the full-link stress testing scenario, ShardingSphere shadow DB is used for storing stress testing data and providing data isolation support for complex testing work. The obtained testing result can accurately reflect the system's true capacity and performance.                                                       |

## Advantages

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

## Roadmap

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap_v2.png)

## How to Contribute

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top-Level Project on April 16, 2020.
You are welcome to check out the mailing list and discuss via [mail](mailto:dev@shardingsphere.apache.org).
