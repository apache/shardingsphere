## [Apache ShardingSphere - 企业级分布式数据库生态系统](https://shardingsphere.apache.org/index_zh.html)

构建异构数据库上层的标准与生态，赋能企业数据架构数字化转型

**官方网站: https://shardingsphere.apache.org/**

[![GitHub release](https://img.shields.io/github/release/apache/shardingsphere.svg)](https://github.com/apache/shardingsphere/releases)
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

### 概述

<hr>

Apache ShardingSphere 定位为 **Database Plus**，是构建在异构数据库上层的标准与生态系统。作为数据库上层的操作系统，ShardingSphere 并非创造全新的数据库，而是专注于最大化现有数据库的计算能力，提供统一的数据访问入口和增强计算能力。

**Database Plus 核心理念**：通过在数据库上层构建标准化、可扩展的增强层，让异构数据库像单一数据库一样简单使用，为企业数据架构提供统一的治理能力和分布式计算能力。

**连接、增强、可插拔** 是 Apache ShardingSphere 的三大核心支柱：

- **连接：** 构建数据库上层标准，通过灵活适配数据库协议、SQL 方言和存储格式，快速连接应用与多模式异构数据库，提供统一的数据访问体验；

- **增强：** 作为数据库计算增强引擎，透明化提供分布式计算（数据分片、读写分离、联邦查询）、数据安全（加密、脱敏、审计）、流量控制（熔断、限流）以及可观测性（监控、追踪、分析）等企业级能力；

- **可插拔：** 采用微内核 + 三层可插拔架构，实现内核、功能组件与生态对接的完全解耦，开发者可以像搭建积木一样，灵活定制符合企业需求的独特数据架构解决方案。

**差异化优势**：
- **vs 分布式数据库**：更轻量级、保护现有投资、避免厂商锁定
- **vs 传统中间件**：功能更丰富、生态更完善、架构更灵活
- **vs 云厂商方案**：支持多云部署、避免技术绑定、自主可控

ShardingSphere 于 2020 年 4 月 16 日成为 [Apache 软件基金会](https://apache.org/index.html#projects-list) 顶级项目，已在全球范围内被 [19,000+ 项目](https://github.com/search?l=Maven+POM&q=shardingsphere+language%3A%22Maven+POM%22&type=Code)采用。

### 双接入端架构设计

<hr>

ShardingSphere 采用独特的双接入端架构设计，提供 JDBC 和 Proxy 两种既可独立部署又可混合部署的接入端，满足不同场景下的多样化需求。

#### ShardingSphere-JDBC：轻量级接入端

**定位**：轻量级 Java 框架，增强版 JDBC 驱动

**核心特性**：
- **客户端直连**：与应用程序共享资源，无中心化架构
- **高性能低损耗**：直接数据库连接，性能损耗最小
- **完全兼容**：兼容所有 ORM 框架（MyBatis、JPA、Hibernate 等）
- **零额外部署**：以 JAR 包形式提供，无需独立部署和依赖

**适用场景**：Java 高性能应用，与业务应用集成部署，追求极致性能

#### ShardingSphere-Proxy：企业级接入端

**定位**：透明化数据库代理，独立部署的服务端

**核心特性**：
- **静态入口**：独立于应用程序部署，提供稳定的数据库访问入口
- **异构语言支持**：支持任意兼容 MySQL/PostgreSQL 协议的客户端
- **DBA 友好**：数据库运维管理界面，便于运维人员操作
- **企业级特性**：支持集群部署、负载均衡、故障转移

**适用场景**：异构语言环境、数据库运维管理、需要统一访问入口的企业级应用

#### 混合架构优势

通过混合使用 ShardingSphere-JDBC 和 ShardingSphere-Proxy，并采用同一注册中心统一配置分片策略，能够灵活搭建适用于各种场景的应用系统：

- **架构灵活性**：架构师可自由调整最佳系统架构
- **场景适配性**：根据不同业务场景选择最适合的接入方式
- **统一管理**：单一配置，多端协同，简化运维复杂度
- **渐进式演进**：支持从 JDBC 到 Proxy 的平滑演进路径

### AI 总结

[![DeepWiki](https://img.shields.io/badge/DeepWiki-apache%2Fshardingsphere-blue.svg?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACwAAAAyCAYAAAAnWDnqAAAAAXNSR0IArs4c6QAAA05JREFUaEPtmUtyEzEQhtWTQyQLHNak2AB7ZnyXZMEjXMGeK/AIi+QuHrMnbChYY7MIh8g01fJoopFb0uhhEqqcbWTp06/uv1saEDv4O3n3dV60RfP947Mm9/SQc0ICFQgzfc4CYZoTPAswgSJCCUJUnAAoRHOAUOcATwbmVLWdGoH//PB8mnKqScAhsD0kYP3j/Yt5LPQe2KvcXmGvRHcDnpxfL2zOYJ1mFwrryWTz0advv1Ut4CJgf5uhDuDj5eUcAUoahrdY/56ebRWeraTjMt/00Sh3UDtjgHtQNHwcRGOC98BJEAEymycmYcWwOprTgcB6VZ5JK5TAJ+fXGLBm3FDAmn6oPPjR4rKCAoJCal2eAiQp2x0vxTPB3ALO2CRkwmDy5WohzBDwSEFKRwPbknEggCPB/imwrycgxX2NzoMCHhPkDwqYMr9tRcP5qNrMZHkVnOjRMWwLCcr8ohBVb1OMjxLwGCvjTikrsBOiA6fNyCrm8V1rP93iVPpwaE+gO0SsWmPiXB+jikdf6SizrT5qKasx5j8ABbHpFTx+vFXp9EnYQmLx02h1QTTrl6eDqxLnGjporxl3NL3agEvXdT0WmEost648sQOYAeJS9Q7bfUVoMGnjo4AZdUMQku50McDcMWcBPvr0SzbTAFDfvJqwLzgxwATnCgnp4wDl6Aa+Ax283gghmj+vj7feE2KBBRMW3FzOpLOADl0Isb5587h/U4gGvkt5v60Z1VLG8BhYjbzRwyQZemwAd6cCR5/XFWLYZRIMpX39AR0tjaGGiGzLVyhse5C9RKC6ai42ppWPKiBagOvaYk8lO7DajerabOZP46Lby5wKjw1HCRx7p9sVMOWGzb/vA1hwiWc6jm3MvQDTogQkiqIhJV0nBQBTU+3okKCFDy9WwferkHjtxib7t3xIUQtHxnIwtx4mpg26/HfwVNVDb4oI9RHmx5WGelRVlrtiw43zboCLaxv46AZeB3IlTkwouebTr1y2NjSpHz68WNFjHvupy3q8TFn3Hos2IAk4Ju5dCo8B3wP7VPr/FGaKiG+T+v+TQqIrOqMTL1VdWV1DdmcbO8KXBz6esmYWYKPwDL5b5FA1a0hwapHiom0r/cKaoqr+27/XcrS5UwSMbQAAAABJRU5ErkJggg==)](https://deepwiki.com/apache/shardingsphere)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/apache/shardingsphere)

### 文档📜

<hr>

[![EN d](https://img.shields.io/badge/document-English-blue.svg)](https://shardingsphere.apache.org/document/current/en/overview/)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://shardingsphere.apache.org/document/current/cn/overview/)

更多信息请参考：[https://shardingsphere.apache.org/document/current/cn/overview/](https://shardingsphere.apache.org/document/current/cn/overview/)

### 参与贡献🚀🧑💻

<hr>

搭建开发环境和贡献者指南，请参考：[https://shardingsphere.apache.org/community/cn/involved/](https://shardingsphere.apache.org/community/cn/involved/)

### 团队成员

<hr>

我们真挚感谢[社区贡献者](https://shardingsphere.apache.org/community/cn/team)对 Apache ShardingSphere 的奉献。

##

### 社区和支持💝🖤

<hr>

:link: [Mailing List](https://shardingsphere.apache.org/community/cn/involved/subscribe/). 适合于 Apache 社区相关讨论和版本发布；

:link: [GitHub Issues](https://github.com/apache/shardingsphere/issues). 适合于设计讨论、缺陷报告或者开发相关；

:link: [Slack channel](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg). 适合于在线交流和线上会议；

:link: [X](https://x.com/ShardingSphere). 随时了解 Apache ShardingSphere 信息。

##

### 项目状态

<hr>

:white_check_mark: **Version 5.5.4-SNAPSHOT**: 正在积极开发中 :tada:

🔗 请访问 [发布说明](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md) 获得更详细的信息。

:soon: **Version 5.5.4**

我们目前正在开发 5.5.4 版本，包含多项安全增强和性能优化。
请访问[里程碑](https://github.com/apache/shardingsphere/milestones) 获取最新开发进展。

##

### 技术架构演进

<hr>

Apache ShardingSphere 采用微内核 + 三层可插拔架构，实现了内核、功能组件与生态对接的完全解耦，为开发者提供了极致的灵活性和扩展能力。

#### 微内核 + 三层可插拔模型

**核心层**：
- 查询优化器：智能 SQL 路由和执行计划优化
- 分布式事务：ACID 事务保证和一致性协调
- 执行引擎：高效的分布式执行和结果聚合

**功能层**：
- 数据分片、读写分离、联邦查询
- 数据加密、数据脱敏、SQL 审计
- 影子库、可观测性、流量控制

**生态层**：
- 数据库协议适配（MySQL、PostgreSQL、Firebird 等）
- 注册中心集成（ZooKeeper、ETCD 等）
- 配置管理、服务发现、监控集成

#### 技术创新亮点

**完全解耦架构**：
- 数据库类型完全解耦，支持新数据库快速接入
- 功能模块完全解耦，支持功能按需组合

Apache ShardingSphere 由 JDBC、Proxy 两个既可独立部署又可混合部署的接入端组成，为 Java 同构、异构语言、云原生等多样化应用场景提供统一的分布式数据库解决方案。

### ShardingSphere-JDBC

<hr>

[![Maven Status](https://img.shields.io/maven-central/v/org.apache.shardingsphere/shardingsphere-jdbc.svg?color=green)](https://mvnrepository.com/artifact/org.apache.shardingsphere/shardingsphere-jdbc)

定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。
它使用客户端直连数据库，以 jar 包形式提供服务，无需额外部署和依赖，可理解为增强版的 JDBC 驱动，完全兼容 JDBC 和各种 ORM 框架。

:link: 更多信息请参考[官方网站](https://shardingsphere.apache.org/document/current/cn/overview/#shardingsphere-jdbc)。

> **注意**：使用 ShardingSphere-JDBC 接入端时，需特别关注应用的内存配置。由于 Antlr 在 SQL 解析过程中，会使用内部缓存来提升性能，如果应用的 SQL 模板数量过多，则会导致缓存不断增长，占用大量堆内存。
根据 ANTLR 官方 [issue#4232](https://github.com/antlr/antlr4/issues/4232) 的反馈，目前该问题尚未得到优化，应用接入 ShardingSphere-JDBC 时，建议通过 `-Xmx` 参数设置合理的堆内存大小，避免因内存不足导致的 OOM。

### ShardingSphere-Proxy

<hr>

[![Nightly-Download](https://img.shields.io/static/v1?label=nightly-builds&message=download&color=orange)](https://nightlies.apache.org/shardingsphere/)
[![Download](https://img.shields.io/badge/release-download-orange.svg)](https://www.apache.org/dyn/closer.lua/shardingsphere/5.3.1/apache-shardingsphere-5.3.1-shardingsphere-proxy-bin.tar.gz)
[![Docker Pulls](https://img.shields.io/docker/pulls/apache/shardingsphere-proxy.svg)](https://store.docker.com/community/images/apache/shardingsphere-proxy)

定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。
目前提供 MySQL 和 PostgreSQL 版本，它可以使用任何兼容 MySQL/PostgreSQL 协议的访问客户端操作数据，对 DBA 更加友好。

:link: 更多信息请参考[官方网站](https://shardingsphere.apache.org/document/current/en/overview/#shardingsphere-proxy)。

|       | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* |
|-------|-----------------------|------------------------|
| 数据库   | 任意                    | MySQL/PostgreSQL       |
| 连接消耗数 | 高                     | 低                      |
| 异构语言  | 仅 Java                | 任意                     |
| 性能    | 损耗低                   | 损耗略高                   |
| 无中心化  | 是                     | 否                      |
| 静态入口  | 无                     | 有                      |

### 混合架构

<hr>

ShardingSphere-JDBC 采用无中心化架构，与应用程序共享资源，适用于 Java 开发的高性能的轻量级 OLTP 应用；
ShardingSphere-Proxy 提供静态入口以及异构语言的支持，独立于应用程序部署，适用于 OLAP 应用以及对分片数据库进行管理和运维的场景。

Apache ShardingSphere 是多接入端共同组成的生态圈。
通过混合使用 ShardingSphere-JDBC 和 ShardingSphere-Proxy，并采用同一注册中心统一配置分片策略，能够灵活的搭建适用于各种场景的应用系统，使得架构师更加自由地调整适合于当前业务的最佳系统架构。

:link: 更多信息请参考[官方网站](https://shardingsphere.apache.org/document/current/en/overview/#hybrid-architecture)。

##

### 核心功能矩阵

<hr>

#### 分布式数据库核心能力
- **数据分片**：水平分片、垂直分片、自定义分片策略、自动分片路由
- **读写分离**：主从复制、负载均衡、故障转移、读权重配置
- **分布式事务**：XA 事务、柔性事务、事务传播

#### 数据安全与治理
- **数据加密**：字段级加密、透明加密、密钥管理、加密算法支持
- **数据脱敏**：敏感数据保护、脱敏策略定制、动态脱敏规则
- **权限控制**：细粒度权限、访问控制、安全策略

#### 数据库网关能力
- **异构数据库**：MySQL、PostgreSQL、Oracle、SQL Server、Firebird 等
- **SQL 方言转换**：跨数据库 SQL 兼容、方言适配、语法转换
- **协议适配**：数据库协议转换、多协议支持、通信优化

#### 全链路压测与可观测性
- **影子库**：压测数据隔离、环境分离、真实数据模拟
- **可观测性**：性能监控、链路追踪、服务质量分析、指标采集
- **流量分析**：SQL 性能分析、流量统计、瓶颈识别

#### 企业级特性
- **高可用**：集群部署、故障恢复、服务发现、健康检查
- **云原生**：容器化部署、Kubernetes 集成、原生镜像支持
- **监控告警**：实时监控、告警通知、性能指标、运维仪表盘

##

### 线路规划

<hr>

![Roadmap](https://shardingsphere.apache.org/document/current/img/roadmap_cn.png)

##

### 如何构建 Apache ShardingSphere

<hr>

查看 [Wiki](https://github.com/apache/shardingsphere/wiki) 详细了解如何构建 Apache ShardingSphere。

##

### 全景图

<hr>

<p align="center">
<br/><br/>
<img src="https://landscape.cncf.io/images/cncf-landscape-horizontal-color.svg" width="165"/>&nbsp;&nbsp;<img src="https://www.cncf.io/wp-content/uploads/2023/04/cncf-main-site-logo.svg" width="200"/>
<br/><br/>
ShardingSphere 进入了<a href="https://landscape.cncf.io/?category=app-definition-and-development&grouping=category">CNCF 云原生全景图</a>。
</p>

##
