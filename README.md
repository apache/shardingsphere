## [Apache ShardingSphere - Enterprise Distributed Database Ecosystem](https://shardingsphere.apache.org/)

Building the standards and ecosystem on top of heterogeneous databases, empowering enterprise data architecture transformation

**Official Website:** [https://shardingsphere.apache.org/](https://shardingsphere.apache.org/)

[![GitHub Release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)

[![CI](https://github.com/apache/shardingsphere/actions/workflows/ci.yml/badge.svg)](https://github.com/apache/shardingsphere/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=apache_shardingsphere&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=apache_shardingsphere)
[![codecov](https://codecov.io/gh/apache/shardingsphere/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/shardingsphere)

[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/5394/badge)](https://bestpractices.coreinfrastructure.org/projects/5394)

[![Slack](https://img.shields.io/badge/%20Slack-ShardingSphere%20Channel-blueviolet)](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)
[![Gitter](https://badges.gitter.im/shardingsphere/shardingsphere.svg)](https://gitter.im/shardingsphere/Lobby)

[![X](https://img.shields.io/twitter/url/https/twitter.com/ShardingSphere.svg?style=social&label=Follow%20%40ShardingSphere)](https://x.com/ShardingSphere)

<table style="width:100%">
    <tr>
        <th>
            <a href="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map?activity=stars&repo_id=49876476" target="_blank" style="display: block" align="center">
                <picture>
                    <source media="(prefers-color-scheme: dark)" srcset="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=stars&repo_id=49876476&image_size=auto&color_scheme=dark" width="721" height="auto">
                    <img alt="Star Geographical Distribution of apache/shardingsphere" src="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=stars&repo_id=49876476&image_size=auto&color_scheme=light" width="721" height="auto">
                </picture>
            </a>
        </th>
        <th>
            <a href="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map?activity=pull-request-creators&repo_id=49876476" target="_blank" style="display: block" align="center">
                <picture>
                    <source media="(prefers-color-scheme: dark)" srcset="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=pull-request-creators&repo_id=49876476&image_size=auto&color_scheme=dark" width="721" height="auto">
                    <img alt="Pull Request Creator Geographical Distribution of apache/shardingsphere" src="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=pull-request-creators&repo_id=49876476&image_size=auto&color_scheme=light" width="721" height="auto">
                </picture>
            </a>
        </th>
        <th>
            <a href="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map?activity=issue-creators&repo_id=49876476" target="_blank" style="display: block" align="center">
                <picture>
                    <source media="(prefers-color-scheme: dark)" srcset="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=issue-creators&repo_id=49876476&image_size=auto&color_scheme=dark" width="721" height="auto">
                    <img alt="Issue Creator Geographical Distribution of apache/shardingsphere" src="https://next.ossinsight.io/widgets/official/analyze-repo-stars-map/thumbnail.png?activity=issue-creators&repo_id=49876476&image_size=auto&color_scheme=light" width="721" height="auto">
                </picture>
            </a>
        </th>
    </tr>
</table>

### OVERVIEW

<hr>

Apache ShardingSphere is positioned as **Database Plus**, a standard and ecosystem built on top of heterogeneous databases. As an operating system layer above databases, ShardingSphere does not create new databases but focuses on maximizing the computing capabilities of existing databases, providing unified data access and enhanced computing capabilities.

**Database Plus Core Concept**: By building a standardized and scalable enhancement layer above databases, it makes heterogeneous databases as simple to use as a single database, providing unified governance capabilities and distributed computing capabilities for enterprise data architectures.

**Connect, Enhance, and Pluggable** are the three core pillars of Apache ShardingSphere:

- **Connect:** Building database upper-layer standards, quickly connecting applications with multi-modal heterogeneous databases through flexible adaptation of database protocols, SQL dialects, and storage formats, providing unified data access experience;

- **Enhance:** As a database computing enhancement engine, transparently providing enterprise-grade capabilities including distributed computing (data sharding, readwrite-splitting, SQL federation), data security (encryption, masking, audit), traffic control (circuit breaker, rate limiting), and observability (monitoring, tracing, analysis);

- **Pluggable:** Adopting a micro-kernel + 3-layer pluggable architecture to achieve complete decoupling of kernel, functional components, and ecosystem integration. Developers can flexibly customize unique data architecture solutions that meet enterprise needs, just like building with LEGO blocks.

**Differentiation Advantages**:
- **vs Distributed Databases**: More lightweight, protecting existing investments, avoiding vendor lock-in
- **vs Traditional Middleware**: Richer features, more complete ecosystem, more flexible architecture
- **vs Cloud Vendor Solutions**: Support multi-cloud deployment, avoid technology binding, autonomous and controllable

ShardingSphere became an [Apache](https://apache.org/index.html#projects-list) Top-Level Project on April 16, 2020, and has been adopted by [19,000+ projects](https://github.com/search?l=Maven+POM&q=shardingsphere+language%3A%22Maven+POM%22&type=Code) worldwide.

### DUAL-ACCESS ARCHITECTURE DESIGN

<hr>

ShardingSphere adopts a unique dual-access architecture design, providing two access ends - JDBC and Proxy - that can be deployed independently or in hybrid deployment, meeting diverse requirements for different scenarios.

#### ShardingSphere-JDBC: Lightweight Access End

**Positioning**: Lightweight Java framework, enhanced JDBC driver

**Core Features**:
- **Client-side direct connection**: Shares resources with applications, decentralized architecture
- **High performance, low overhead**: Direct database connection with minimal performance loss
- **Complete compatibility**: Compatible with all ORM frameworks (MyBatis, JPA, Hibernate, etc.)
- **Zero additional deployment**: Provided as JAR package, no independent deployment and dependencies required

**Use Cases**: High-performance Java applications, integrated deployment with business applications, pursuing ultimate performance

#### ShardingSphere-Proxy: Enterprise Access End

**Positioning**: Transparent database proxy, independently deployed server-side

**Core Features**:
- **Static entry point**: Independent deployment from applications, providing stable database access entry
- **Heterogeneous language support**: Supports any MySQL/PostgreSQL protocol compatible client
- **DBA friendly**: Database operation and maintenance management interface, convenient for O&M personnel
- **Enterprise-grade features**: Supports cluster deployment, load balancing, failover

**Use Cases**: Heterogeneous language environments, database operation and maintenance management, enterprise applications requiring unified access entry

#### Hybrid Architecture Advantages

By hybridizing ShardingSphere-JDBC and ShardingSphere-Proxy with unified configuration through the same registry center, you can flexibly build application systems suitable for various scenarios:

- **Architectural flexibility**: Architects can freely adjust the optimal system architecture
- **Scenario adaptability**: Select the most suitable access method according to different business scenarios
- **Unified management**: Single configuration, multi-end collaboration, simplifying O&M complexity
- **Progressive evolution**: Support smooth evolution path from JDBC to Proxy

### AI ABSTRACTION

[![DeepWiki](https://img.shields.io/badge/DeepWiki-apache%2Fshardingsphere-blue.svg?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACwAAAAyCAYAAAAnWDnqAAAAAXNSR0IArs4c6QAAA05JREFUaEPtmUtyEzEQhtWTQyQLHNak2AB7ZnyXZMEjXMGeK/AIi+QuHrMnbChYY7MIh8g01fJoopFb0uhhEqqcbWTp06/uv1saEDv4O3n3dV60RfP947Mm9/SQc0ICFQgzfc4CYZoTPAswgSJCCUJUnAAoRHOAUOcATwbmVLWdGoH//PB8mnKqScAhsD0kYP3j/Yt5LPQe2KvcXmGvRHcDnpxfL2zOYJ1mFwrryWTz0advv1Ut4CJgf5uhDuDj5eUcAUoahrdY/56ebRWeraTjMt/00Sh3UDtjgHtQNHwcRGOC98BJEAEymycmYcWwOprTgcB6VZ5JK5TAJ+fXGLBm3FDAmn6oPPjR4rKCAoJCal2eAiQp2x0vxTPB3ALO2CRkwmDy5WohzBDwSEFKRwPbknEggCPB/imwrycgxX2NzoMCHhPkDwqYMr9tRcP5qNrMZHkVnOjRMWwLCcr8ohBVb1OMjxLwGCvjTikrsBOiA6fNyCrm8V1rP93iVPpwaE+gO0SsWmPiXB+jikdf6SizrT5qKasx5j8ABbHpFTx+vFXp9EnYQmLx02h1QTTrl6eDqxLnGjporxl3NL3agEvXdT0WmEost648sQOYAeJS9Q7bfUVoMGnjo4AZdUMQku50McDcMWcBPvr0SzbTAFDfvJqwLzgxwATnCgnp4wDl6Aa+Ax283gghmj+vj7feE2KBBRMW3FzOpLOADl0Isb5587h/U4gGvkt5v60Z1VLG8BhYjbzRwyQZemwAd6cCR5/XFWLYZRIMpX39AR0tjaGGiGzLVyhse5C9RKC6ai42ppWPKiBagOvaYk8lO7DajerabOZP46Lby5wKjw1HCRx7p9sVMOWGzb/vA1hwiWc6jm3MvQDTogQkiqIhJV0nBQBTU+3okKCFDy9WwferkHjtxib7t3xIUQtHxnIwtx4mpg26/HfwVNVDb4oI9RHmx5WGelRVlrtiw43zboCLaxv46AZeB3IlTkwouebTr1y2NjSpHz68WNFjHvupy3q8TFn3Hos2IAk4Ju5dCo8B3wP7VPr/FGaKiG+T+v+TQqIrOqMTL1VdWV1DdmcbO8KXBz6esmYWYKPwDL5b5FA1a0hwapHiom0r/cKaoqr+27/XcrS5UwSMbQAAAABJRU5ErkJggg==)](https://deepwiki.com/apache/shardingsphere)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/apache/shardingsphere)

### DOCUMENTATION📜

<hr>

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://shardingsphere.apache.org/document/current/en/overview/)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://shardingsphere.apache.org/document/current/cn/overview/)

For full documentation & more details, visit: [Docs](https://shardingsphere.apache.org/document/current/en/overview/)

### CONTRIBUTION🚀🧑💻

<hr>

For guides on how to get started and setup your environment, contributor & committer guides, visit: [Contribution Guidelines](https://shardingsphere.apache.org/community/en/involved/)

### Team

<hr>

We deeply appreciate [community contributors](https://shardingsphere.apache.org/community/en/team) for their dedication to Apache ShardingSphere.

##

### COMMUNITY & SUPPORT💝🖤

<hr>

:link: [Mailing List](https://shardingsphere.apache.org/community/en/involved/subscribe/). Best for: Apache community updates, releases, changes.

:link: [GitHub Issues](https://github.com/apache/shardingsphere/issues). Best for: design discussions, bug reports, or anything development related.

:link: [Slack channel](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg). Best for: instant communications and online meetings, sharing your applications.

:link: [X](https://x.com/ShardingSphere). Best for: keeping up to date on everything ShardingSphere.

:link: [LinkedIn](https://www.linkedin.com/showcase/apache-shardingsphere/e). Best for: professional networking and career development with other ShardingSphere contributors.

##

### PROJECT STATUS

<hr>

:white_check_mark: **Version 5.5.3-SNAPSHOT**: Actively under development :tada:

🔗 For the release notes, follow this link to the relevant [GitHub page](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md).

:soon: **Version 5.5.3**

We are currently developing version 5.5.3, which includes multiple security enhancements and performance optimizations.
Keep an eye on the [milestones page](https://github.com/apache/shardingsphere/milestones) of this repo for the latest development progress.

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

### TECHNICAL ARCHITECTURE EVOLUTION

<hr>

Apache ShardingSphere adopts a micro-kernel + 3-layer pluggable architecture, achieving complete decoupling of the kernel, functional components, and ecosystem integration, providing developers with ultimate flexibility and extensibility.

#### Micro-Kernel + 3-Layer Pluggable Model

**Core Layer**:
- Query optimizer: Intelligent SQL routing and execution plan optimization
- Distributed transaction: ACID transaction guarantees and consistency coordination
- Execution engine: Efficient distributed execution and result aggregation

**Feature Layer**:
- Data sharding, readwrite-splitting, federation query
- Data encryption, data masking, SQL audit
- Shadow database, observability, traffic control

**Ecosystem Layer**:
- Database protocol adaptation (MySQL, PostgreSQL, Oracle, etc.)
- Registry center integration (ZooKeeper, ETCD, etc.)
- Configuration management, service discovery, monitoring integration

#### Technical Innovation Highlights

**Complete Decoupling Architecture**:
- Database types completely decoupled, supporting rapid integration of new databases
- Functional modules completely decoupled, supporting on-demand feature combination

Apache ShardingSphere consists of two access ends - JDBC and Proxy - that can be deployed independently or in hybrid deployment, providing unified distributed database solutions for diverse application scenarios including Java isomorphism, heterogeneous languages, and cloud-native environments.

### ShardingSphere-JDBC

<hr>

[![Maven Status](https://img.shields.io/maven-central/v/org.apache.shardingsphere/shardingsphere-jdbc.svg?color=green)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

A lightweight Java framework providing extra services at the Java JDBC layer. 
With the client end connecting directly to the database, it provides services in the form of a jar and requires no extra deployment and dependence.

:link: For more details, follow this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-jdbc).

> **Note**: When using ShardingSphere-JDBC adapter, pay attention to your application's memory configuration. Antlr uses an internal cache to improve performance during SQL parsing. If your application has too many SQL templates, the cache will continue to grow, occupying a large amount of heap memory.
According to feedback from the ANTLR official [issue#4232](https://github.com/antlr/antlr4/issues/4232), this issue has not yet been optimized. When connecting your application to ShardingSphere-JDBC, it is recommended to set a reasonable heap memory size using the `-Xmx` parameter to avoid OOM errors caused by insufficient memory.

### ShardingSphere-Proxy

<hr>

[![Nightly-Download](https://img.shields.io/static/v1?label=nightly-builds&message=download&color=orange)](https://nightlies.apache.org/shardingsphere/)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://www.apache.org/dyn/closer.lua/shardingsphere/5.3.2/apache-shardingsphere-5.3.2-shardingsphere-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/shardingsphere-proxy.svg)](https://store.docker.com/community/images/apache/shardingsphere-proxy)

A transparent database proxy, providing a database server that encapsulates the database binary protocol to support heterogeneous languages. 
Friendlier to DBAs, the MariaDB, MySQL and PostgreSQL version now provided can use any kind of terminal.

:link: For more details, follow this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-proxy).

### Hybrid Architecture

<hr>

ShardingSphere-JDBC adopts a decentralized architecture, applicable to high-performance light-weight OLTP applications developed with Java. 
ShardingSphere-Proxy provides static entry and all languages support, suitable for an OLAP application and sharding databases management and operation.

Through the combination of ShardingSphere-JDBC & ShardingSphere-Proxy together with a unified sharding strategy by the same registry center, the ShardingSphere ecosystem can build an application system suitable to all kinds of scenarios.

:link: More details can be found following this [link to the official website](https://shardingsphere.apache.org/document/current/en/overview/#hybrid-architecture).

##

### CORE FEATURE MATRIX

<hr>

#### Distributed Database Core Capabilities
- **Data Sharding**: Horizontal sharding, vertical sharding, custom sharding strategies, automatic sharding routing
- **Read/Write Splitting**: Master-slave replication, load balancing, failover, read weight configuration
- **Distributed Transaction**: XA transactions, BASE transactions, transaction propagation

#### Data Security & Governance
- **Data Encryption**: Field-level encryption, transparent encryption, key management, encryption algorithm support
- **Data Masking**: Sensitive data protection, masking strategy customization, dynamic masking rules
- **Access Control**: Fine-grained permissions, access control, SQL firewall, security policies

#### Database Gateway Capabilities
- **Heterogeneous Databases**: MySQL, PostgreSQL, Oracle, SQL Server, Firebird, etc.
- **SQL Dialect Translation**: Cross-database SQL compatibility, dialect adaptation, syntax conversion
- **Protocol Adaptation**: Database protocol conversion, multi-protocol support, communication optimization

#### Full-link Stress Testing & Observability
- **Shadow Database**: Stress testing data isolation, environment separation, real data simulation
- **Observability**: Performance monitoring, distributed tracing, QoS analysis, metrics collection
- **Traffic Analysis**: SQL performance analysis, traffic statistics, bottleneck identification

#### Enterprise-grade Features
- **High Availability**: Cluster deployment, fault recovery, service discovery, health checks
- **Cloud Native**: Containerized deployment, Kubernetes integration, native image support
- **Monitoring & Alerting**: Real-time monitoring, alert notifications, performance metrics, O&M dashboard

##

### Roadmap

<hr>

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap_en.png)

##

### How to Build Apache ShardingSphere

<hr>

Check out [Wiki](https://github.com/apache/shardingsphere/wiki) section for details on how to build Apache ShardingSphere and a full guide on how to get started and setup your local dev environment.

##

### Landscapes

<hr>

<p align="center">
<br/><br/>
<img src="https://landscape.cncf.io/images/cncf-landscape-horizontal-color.svg" width="165"/>&nbsp;&nbsp;<img src="https://www.cncf.io/wp-content/uploads/2023/04/cncf-main-site-logo.svg" width="200"/>
<br/><br/>
Apache ShardingSphere enriches the <a href="https://landscape.cncf.io/?category=app-definition-and-development&grouping=category">CNCF CLOUD NATIVE Landscape</a>.
</p>

##
