# [ShardingSphere - Building a Criterion and Ecosystem Above Multi-Model Databases](https://shardingsphere.apache.org/)

**Official Website:** [https://shardingsphere.apache.org/](https://shardingsphere.apache.org/)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)
[![Build Status](https://api.travis-ci.org/apache/shardingsphere.svg?branch=master&status=created)](https://travis-ci.org/apache/shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)
[![snyk](https://snyk.io/test/github/apache/shardingsphere/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/apache/shardingsphere?targetFile=pom.xml)
[![Maintainability](https://cloud.quality-gate.com/dashboard/api/badge?projectName=apache_shardingsphere&branchName=master)](https://cloud.quality-gate.com/dashboard/branches/30#overview)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/apache/skywalking)

[![Twitter](https://img.shields.io/twitter/url/https/twitter.com/ShardingSphere.svg?style=social&label=Follow%20%40ShardingSphere)](https://twitter.com/ShardingSphere)
[![Slack](https://img.shields.io/badge/%20Slack-ShardingSphere%20Channel-blueviolet)](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

**Stargazers Over Time**           |  **Contributors Over Time**
:---------------------------------:|:------------------------------------:
[![Stargazers over time](https://starchart.cc/apache/shardingsphere.svg)](https://starchart.cc/apache/shardingsphere)  |  [![Contributor over time](https://contributor-graph-api.apiseven.com/contributors-svg?chart=contributorOverTime&repo=apache/shardingsphere)](https://www.apiseven.com/en/contributor-graph?chart=contributorOverTime&repo=apache/shardingsphere)




# ShardingSphere Overview


Apache ShardingSphere is positioned as a Database Plus, and aims at building a new criterion and ecosystem above multi-model databases. It focuses on how to reuse existing databases and their respective upper layer, rather than creating a new database. 

The concepts at the core of the project are Link, Enhance and Pluggable.

•	`Link`：Flexible adaptation of database protocol, SQL dialect and database storage. It can quickly link applications and multi-mode heterogeneous databases quickly.

•	`Enhance`：Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance (circuit breaker and access limitation and analyze, QoS and observability).

•	`Pluggable`：Leveraging the micro kernel and 3 layers pluggable mode, features and database ecosystem can be embedded flexibily. Developers can customize their ShardingSphere just like building with LEGO blocks.

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top-Level Project on April 16, 2020.
 
## Documentation: 

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://shardingsphere.apache.org/document/current/en/overview/)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://shardingsphere.apache.org/document/current/cn/overview/)

For full documentation & more details, visit: [https://shardingsphere.apache.org/document/current/en/overview/](https://shardingsphere.apache.org/document/current/en/)

## Contribution: 

For guides on how to get started and setup your environment, contributor & committer guides, visit: [https://shardingsphere.apache.org/community/en/contribute/](https://shardingsphere.apache.org/document/current/en/)

## Community & Support:

:link: [Mailing List](dev@shardingsphere.apache.org). Best for: Apache community updates, releases, changes.

:link: [GitHub Issues](https://github.com/apache/shardingsphere/issues). Best for: larger systemic questions/bug reports or anything development related.

:link: [GitHub Discussions](https://github.com/apache/shardingsphere/discussions). Best for: technical questions & support, requesting new features, proposing new features.

:link:	[Slack channel](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg). Best for: instant communications and online meetings, sharing your applications.

:link:	[Twitter](https://twitter.com/ShardingSphere). Best for: keeping up to date on everything ShardingSphere.

## Status:

:white_check_mark: Version 5.0.0-beta: released, stable enough & anyone can sign up :tada:

:soon:	Version 5.0.0-RC1

We are currently working towards our 5.0.0-RC1 milestone. Keep an eye on the [milestones page](https://github.com/apache/shardingsphere/milestones) of this repo to stay up to date.

## How it Works: 

Apache ShardingSphere includes 3 independent products: JDBC, Proxy & Sidecar (Planning). They all provide functions of data scale-out, distributed transaction and distributed governance, applicable in a variety of situations such as Java isomorphism, heterogeneous language and Cloud-Native.

### ShardingSphere-JDBC

[![Maven Status](https://maven-badges.herokuapp.com/maven-central/org.apache.shardingsphere/shardingsphere-jdbc/badge.svg)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

A lightweight Java framework providing extra services at the Java JDBC layer. With the client end connecting directly to the database, it provides services in the form of a jar and requires no extra deployment and dependence. Supports any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP. Supports any kind of JDBC standard database: MySQL, Oracle, SQLServer, PostgreSQL and any SQL92 followed databases.

:link: For more details, follow this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc).
 
### ShardingSphere-Proxy

[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://apache.org/dyn/closer.cgi?path=shardingsphere/5.0.0-beta/apache-shardingsphere-5.0.0-beta-shardingsphere-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/sharding-proxy.svg)](https://store.docker.com/community/images/apache/sharding-proxy)

A transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. Friendlier to DBAs, the MySQL version now provided can use any kind of terminal (such as MySQL Command Client, MySQL Workbench, etc.) that is compatible with the MySQL protocol to operate data.
It can be used directly as MySQL and PostgreSQL servers, and is applicable to any kind of terminal that is compatible with MySQL and PostgreSQL protocol.

:link: For more details, follow this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-proxy).


### Hybrid Architecture

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP applications developed with Java. ShardingSphere-Proxy provides static entry and all languages support, suitable for OLAP application and sharding databases management and operation.

Through the mixed use of ShardingSphere-JDBC & ShardingSphere-Proxy together with a unified sharding strategy by the same registry center, the ShardingSphere ecosystem can build an application system suitable to all kinds of scenarios.

:link: More details can be found following this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#hybrid-architecture).


### Solution

|  *Distributed Database* |    *Data Security*   |    *Database Gateway* | *Stress Testing* |        
| ----------------------- | ---------------------| ---------------------- | ---------------|
| Data Sharding           | Data Encrypt         | Multi-model Databases supported | Shadow Database |
| Readwrite-splitting     | Row Authority (TODO) | SQL Dialect Translate (TODO) | Observability (Tracing & Metrics)|
| Distributed Transaction | SQL Audit (TODO)     | Any                    
| Elastic Scale-out       | SQL Firewall (TODO)  | Relatively High loss   |
| Distributed Highly Available |                |                |

### Roadmap

![](./docs/document/content/Read-Me-Pics/Read-me-roadmap.png)

## How to Build and Setup Your Local Dev Environment:

### Build Apache ShardingSphere

Check out our Wiki section for details on how to build ShardingSphere, the ShardingSphere-UI, and a full guide on how to get started and setup your local dev environment [at this link](https://github.com/apache/shardingsphere/wiki).

## Landscapes

<p align="center">
<br/><br/>
<img src="https://landscape.cncf.io/images/left-logo.svg" width="150"/>&nbsp;&nbsp;<img src="https://landscape.cncf.io/images/right-logo.svg" width="200"/>
<br/><br/>
Apache ShardingSphere enriches the <a href="https://landscape.cncf.io/landscape=observability-and-analysis&license=apache-license-2-0">CNCF CLOUD NATIVE Landscape</a>.
</p>

