## [Ecosystem to Transform Any Database into a Distributed Database System, and Enhance it with Sharding, Elastic Scaling, Encryption Features & More](https://shardingsphere.apache.org/)

**Official Website:** [https://shardingsphere.apache.org/](https://shardingsphere.apache.org/)

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)
[![Build Status](https://api.travis-ci.org/apache/shardingsphere.svg?branch=master&status=created)](https://travis-ci.org/apache/shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)
[![snyk](https://snyk.io/test/github/apache/shardingsphere/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/apache/shardingsphere?targetFile=pom.xml)
[![Maintainability](https://cloud.quality-gate.com/dashboard/api/badge?projectName=apache_shardingsphere&branchName=master)](https://cloud.quality-gate.com/dashboard/branches/30#overview)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/5394/badge)](https://bestpractices.coreinfrastructure.org/projects/5394)
[![OpenTracing-1.0 Badge](https://img.shields.io/badge/OpenTracing--1.0-enabled-blue.svg)](http://opentracing.io)
[![Skywalking Tracing](https://img.shields.io/badge/Skywalking%20Tracing-enable-brightgreen.svg)](https://github.com/apache/skywalking)

[![Twitter](https://img.shields.io/twitter/url/https/twitter.com/ShardingSphere.svg?style=social&label=Follow%20%40ShardingSphere)](https://twitter.com/ShardingSphere)
[![Slack](https://img.shields.io/badge/%20Slack-ShardingSphere%20Channel-blueviolet)](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

| **Stargazers Over Time**                                                                                              | **Contributors Over Time**                                                                                                                                                                                                                       |
|:---------------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| [![Stargazers over time](https://starchart.cc/apache/shardingsphere.svg)](https://starchart.cc/apache/shardingsphere) | [![Contributor over time](https://contributor-graph-api.apiseven.com/contributors-svg?chart=contributorOverTime&repo=apache/shardingsphere)](https://www.apiseven.com/en/contributor-graph?chart=contributorOverTime&repo=apache/shardingsphere) |

### OVERVIEW

<hr>

Apache ShardingSphere follows Database Plus - our community's guiding development concept for creating a complete ecosystem that allows you to transform any database into a distributed database system, and easily enhance it with sharding, elastic scaling, data encryption features & more. 

It focuses on repurposing existing databases, by placing a standardized upper layer above existing and fragmented databases, rather than creating a new database. 

The goal is to provide unified database services, and minimize or eliminate the challenges caused by underlying databases' fragmentation. 
This results in applications only needing to communicate with a single standardized service.

The concepts at the core of the project are `Connect`, `Enhance` and `Pluggable`.

- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage. It can quickly connect applications and heterogeneous databases.
- `Enhance:` Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance (circuit breaker and access limitation and analyze, QoS and observability).
- `Pluggable:` Leveraging the micro kernel and 3 layers pluggable mode, features and database ecosystem can be embedded flexibly. Developers can customize their ShardingSphere just like building with LEGO blocks.

Virtually all databases are [supported](https://shardingsphere.apache.org/document/current/en/dev-manual/data-source/) including [MySQL](https://www.mysql.com), [PostgreSQL](https://www.postgresql.org), [SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads), [Oracle Database](https://www.oracle.com/database/), [MariaDB](https://mariadb.org) or any other SQL-92 database.

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top-Level Project on April 16, 2020.
 
### DOCUMENTATION📜

<hr>

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://shardingsphere.apache.org/document/current/en/overview/)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://shardingsphere.apache.org/document/current/cn/overview/)

For full documentation & more details, visit: [Docs](https://shardingsphere.apache.org/document/current/en/overview/)

### CONTRIBUTION🚀🧑‍💻

<hr>

For guides on how to get started and setup your environment, contributor & committer guides, visit: [Contribution Guidelines](https://shardingsphere.apache.org/community/en/involved/)

### Team

<hr>

We deeply appreciate [community contributors](https://shardingsphere.apache.org/community/en/team) for their dedication to Apache ShardingSphere.

##

### COMMUNITY & SUPPORT💝🖤

<hr>

:link: [Mailing List](https://shardingsphere.apache.org/community/en/involved/subscribe/). Best for: Apache community updates, releases, changes.

:link: [GitHub Issues](https://github.com/apache/shardingsphere/issues). Best for: larger systemic questions/bug reports or anything development related.

:link: [GitHub Discussions](https://github.com/apache/shardingsphere/discussions). Best for: technical questions & support, requesting new features, proposing new features.

:link: [Slack channel](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg). Best for: instant communications and online meetings, sharing your applications.

:link: [Twitter](https://twitter.com/ShardingSphere). Best for: keeping up to date on everything ShardingSphere.

##

### STATUS👀

<hr>

:white_check_mark: Version 5.2.0: released :tada:

🔗 For the release notes, follow this link to the relevant [GitHub page](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md).

:soon: Version 5.2.1

We are currently working towards our 5.2.1 milestone. 
Keep an eye on the [milestones page](https://github.com/apache/shardingsphere/milestones) of this repo to stay up to date.

[comment]: <> (##)

[comment]: <> (### NIGHTLY BUILDS:)
 
[comment]: <> (<hr>)

[comment]: <> (A nightly build of ShardingSphere from the latest master branch is available. )

[comment]: <> (The package is updated daily and is available [here]&#40;http://117.48.121.24:8080&#41;.)
 
[comment]: <> (##)

[comment]: <> (**‼️ Notice:**)

[comment]: <> (<hr>)

[comment]: <> (Use this nightly build at your own risk! )

[comment]: <> (The branch is not always fully tested. )

[comment]: <> (The nightly build may contain bugs, and there may be new features added which may cause problems with your environment. )
 
##

### How it Works

<hr>

Apache ShardingSphere includes 2 independent products: JDBC & Proxy.
They all provide functions of data scale-out, distributed transaction and distributed governance, applicable in a variety of situations such as Java isomorphism, heterogeneous language and Cloud-Native.

### ShardingSphere-JDBC

<hr>

[![Maven Status](https://img.shields.io/maven-central/v/org.apache.shardingsphere/shardingsphere-jdbc.svg?color=green)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

A lightweight Java framework providing extra services at the Java JDBC layer. 
With the client end connecting directly to the database, it provides services in the form of a jar and requires no extra deployment and dependence.

:link: For more details, follow this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc).

### ShardingSphere-Proxy

<hr>

[![Nightly-Download](https://img.shields.io/static/v1?label=nightly-builds&message=download&color=orange)](https://nightlies.apache.org/shardingsphere/)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://www.apache.org/dyn/closer.lua/shardingsphere/5.2.0/apache-shardingsphere-5.2.0-shardingsphere-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/shardingsphere-proxy.svg)](https://store.docker.com/community/images/apache/shardingsphere-proxy)

A transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBAs, the MySQL and PostgreSQL version now provided can use any kind of terminal.

:link: For more details, follow this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-proxy).

### Hybrid Architecture

<hr>

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP applications developed with Java. 
ShardingSphere-Proxy provides static entry and all languages support, suitable for OLAP application and sharding databases management and operation.

Through the mixed use of ShardingSphere-JDBC & ShardingSphere-Proxy together with a unified sharding strategy by the same registry center, the ShardingSphere ecosystem can build an application system suitable to all kinds of scenarios.

:link: More details can be found following this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#hybrid-architecture).

##

### Solution

<hr>

| *Solutions/Features* |  *Distributed Database* | *Data Security*      | *Database Gateway*                | *Stress Testing* |
| -------------------- | ----------------------- | ---------------------| --------------------------------- | ---------------- |
|                      | Data Sharding           | Data Encrypt         | Heterogeneous Databases Supported | Shadow Database  |
|                      | Readwrite-splitting     | Row Authority (TODO) | SQL Dialect Translate (TODO)      | Observability    |
|                      | Distributed Transaction | SQL Audit (TODO)     |                                   |                  |
|                      | Elastic Scale-out       | SQL Firewall (TODO)  |                                   |                  |
|                      | Highly Available        |                      |                                   |                  |

##

### Roadmap

<hr>

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap_v2.png)

##

### How to Build Apache ShardingSphere

<hr>

Check out [Wiki](https://github.com/apache/shardingsphere/wiki) section for details on how to build Apache ShardingSphere and a full guide on how to get started and setup your local dev environment.

##

### Landscapes

<hr>

<p align="center">
<br/><br/>
<img src="https://landscape.cncf.io/images/left-logo.svg" width="150"/>&nbsp;&nbsp;<img src="https://landscape.cncf.io/images/right-logo.svg" width="200"/>
<br/><br/>
Apache ShardingSphere enriches the <a href="https://landscape.cncf.io/?category=app-definition-and-development&grouping=category">CNCF CLOUD NATIVE Landscape</a>.
</p>

##
