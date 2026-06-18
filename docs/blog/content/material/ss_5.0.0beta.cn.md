+++
title = "新版发布｜ShardingSphere 5.0.0-beta 来了！"
weight = 14
chapter = true
+++

Original 潘娟 SphereEx 6/22


>Apache ShardingSphere 5.0.0-beta 版在经过长达半年的筹备后，终于将在近期正式 Release! 本文将带领大家一同预览新版本即将带来哪些重大亮点功能。



## 作者介绍

![Juan](https://shardingsphere.apache.org/blog/img/ss_5.0.0beta1.jpg)

> **潘娟 | Trista**
>
>SphereEx 联合创始人
>
>SphereEx co-founder, Apache member, Apache ShardingSphere PMC, Apache brpc(Incubating) mentor, 本次 Release manager。
>
>前京东科技高级 DBA，曾负责京东数科数据库智能平台的设计与研发，现专注于分布式数据库 & 中间件生态及开源领域。被评为《2020 中国开源先锋人物》，多次受邀参加数据库 & 架构领域的相关会议分享。



作为 Apache 的顶级项目，ShardingSphere 的 Release 要经过社区验证、投票、发布等环节，保证推出的版本符合 License 及 Apache Release 规范，且功能及项目层面尽可能符合预期，这是对项目本身及使用者的保护。当前版本已完成基本构建，预计本周内正式发行。



## 本次 Release 将会带来以下重要特性：

### 1. 亮点功能

#### 全新定义的分布式数据库操作语言—DistSQL

SQL 是一种用于存取数据以及查询、更新和管理关系数据库系统的数据库查询和程序设计语言。1986 年 10 月，美国国家标准学会将 SQL 作为关系式数据库管理系统的标准语言。现有通用数据库系统在其实践过程中都对 SQL 规范作了部分改写和扩充，具有更高灵活性和更丰富的功能，使其适用于自身的数据库系统。

DistSQL（Distributed SQL）是 Apache ShardingSphere 提出的，特有的一种内置 SQL 语言，能够提供标准 SQL 之外的增量功能操作能力。DistSQL 让用户可以像操作数据库一样操作 ShardingSphere，使其从面向开发人员的框架和中间件转变为面向运维人员的基础设施产品。

在 ShardingSphere 中， DistSQL 目前主要划分为 RDL、RQL 和 RAL 三种具体类型：

* RDL（Resource & Rule Definition Language）：资源和规则的创建、修改和删除；
* RQL（Resource & Rule Query Language）：资源和规则的查询和展现； 
* RAL（Resource & Rule Administration Language）：Hint、事务类型切换、分片执行计划查询等增量功能操作。

ShardingSphere 推出 Database Plus 理念，为传统数据库赋能，构建具备分布式、高安全、可管控等的数据库增强生态，打造兼具数据库且贴合实际业务需求的开源分布式数据库体系。与该分布式数据库体系搭配使用的分布式 SQL（Distributed SQL）将传统通过配置文件驱动的分布式数据库代理端 ShardingSphere-Proxy，变成真正意义上通过 SQL 驱动的“分布式数据库”。

在 5.0.0-beta 版本中，用户可一键启动 ShardingSphere-Proxy，并通过 DistSQL 在线动态创建、修改、删除分布式数据库表，加密数据库表，动态注入数据库实例资源，创建主从轮询规则，展示全局配置信息，开启分布式事务，启动动态迁移分布式库表作业等功能。

DistSQL 这种数据库态产品，让用户用最规范、标准、熟悉的查询方式操纵及管理 ShardingSphere 分布式数据库生态所有数据库资源及元数据信息。未来我们将通过 DistSQL 打破中间件和数据库之间的界限，让开发者真正像使用数据库一样原生的使用 ShardingSphere。

#### 全面对接 PostgreSQL 生态

PostgreSQL 是世界领先的开源数据库，被业界称为最强大的企业级开源数据库。PostgreSQL 目前世界排名第四，且于 2017、2018 连续两年赢得了“年度数据库”冠军称号。

ShardingSphere-JDBC 和 ShardingSphere-Proxy 共同构成 ShardingSphere 的接入端体系。ShardingSphere-Proxy 又包括 MySQL 和 PostgreSQL 两个协议版本。在 MySQL 协议日益成熟及广泛采用的同时，ShardingSphere 团队开始将注意力放在 PostgreSQL 协议。本次发行的版本，特别针对 PostgreSQL 在其 SQL 解析层、SQL 兼容层、协议接入层、权限控制层进行大规模开发及提升。作为本次发布的主打产品，提升后的 ShardingSphere-Proxy PostgreSQL 版本将真正踏上联动 PostgreSQL 开源生态之路，并在未来将有更多持续提升。

PostgreSQL 作为开源界的明星数据库产品，ShardingSphere 与 PostgreSQL 的链接，将为考虑将 PostgreSQL 分布式化、水平拓展化、安全加密化、细粒度权限控制化的用户提供更为完善和持续维护的解决方案。

#### ShardingSphere 可插拔架构

可插拔架构追求各个模块的相互独立和互无感知，并且通过一个高灵活度，可插拔和可扩展内核，以叠加的方式将各种功能组合使用。

在 ShardingSphere 中，很多功能实现类的加载方式是通过 SPI（Service Provider Interface）注入的方式完成的。SPI 是一种为了被第三方实现或扩展的 API，它可以用于实现框架扩展或组件替换。

目前，数据分片、读写分离、数据加密、影子库、数据库发现等功能，以及对 MySQL、PostgreSQL、SQLServer、Oracle 等 SQL 与协议的支持，均可通过插件的方式植入 ShardingSphere 中。ShardingSphere 现已提供数十个 SPI 作为系统的扩展点，而且仍在不断增加中。可插拔架构的完善，使 ShardingSphere 从分库分表中间件蜕变成为分布式数据库的生态系统。

ShardingSphere 可插拔与可拓展架构理念，为用户提供如积木一样组合式的自定义数据库解决方案，例如让传统关系型数据库同时具有水平扩展和数据加密的功能，或单独打造分布式数据库解决方案。

### 2. 新功能

#### 全新开放式可观测能力

为了有效分离可观测性与主线功能，ShardingSphere 提供了自动化探针，用于方便用户定制化扩展调用链路追踪（tracing）、性能度量指标（metrics）和日志（log）的埋点。ShardingSphere 内置实现了基于 Opentracing、Jaeger 和 Zipkin 的 tracing 探针和基于 Prometheus 的 metrics 探针，并且提供了默认的日志实现。

### 3. 增强

#### 增强的分布式查询能力

跨数据库实例节点进行 Join 及子查询一直是令人头疼的问题。同时使用多数据库实例，使得业务层面的功能受到数据库限制，业务研发人员需时刻注意查询 SQL 的使用范畴。

本次发布的版本实现了增强的分布式查询的功能，在支持跨数据库实例进行 Join 及子查询的同时，通过在 SQL 解析、路由及执行层面的增强及 bug 修复，对 MySQL/PostgreSQL/Oracle/SQLServer 在分布式场景下的 SQL 兼容度实现了大幅度提升。这使得用户在原有的数据库实例集群基础上，通过引入 ShardingSphere，低风险、高效率、零改造地实现从传统数据库集群向分布式水平扩展的数据库集群进行平滑过渡。

目前增强分布式查询能力仍处于 PoC 阶段，性能方面还有较大提升空间，欢迎社区的朋友们一起参与开发。

#### 增强分布式用户及权限控制

用户安全及权限控制是数据库领域较为关注且非常重要的功能之一。在先前 5.0.0-alpha 版本中，ShardingSphere 提供了简单用户密码配置、库级别的粗粒度权限控制，而在此次即将发行的 beta 版则进一步将该特性进行升级改造。从只能通过配置文件进行用户和密码配置，到现在的标准化 SQL 方式进行分布式用户名、hostname、密码的在线修改和管理。此外，从原来粗粒度的库级别权限控制，升级到针对库、表级别的权限控制。

无论业务场景下使用的是 MySQL 还是 PostgreSQL（未来将支持 openGauss），都可使用原生的数据库 SQL 方言，在 ShardingSphere 的分布式体系下，进行用户名、hostname、密码、库、表等自由组合权限控制管理。ShardingSphere-Proxy 的代理端接入方式，让用户尽可能无缝迁移原有的数据库权限及用户体系。

在未来的版本中，ShardingSphere 将提供针对列、视图层面的权限控制，甚至针对每一行的数据都可进行权限的约束。而针对三方业务系统或用户特有的安全系统，ShardingSphere 提供与这些系统打通能力，使 ShardingSphere-Proxy 可以连通第三方安全管控体系的同时，提供最标准的数据库权限管理模式。

目前权限模块处于开发阶段，下个版本将会呈现更加完善的功能。

#### API 简化重构能力

ShardingSphere 的可插拔架构为用户提供丰富的扩展能力，同时为方便使用也内置常用功能。例如分库分表的分片策略，就预置了哈希分片、时间范围分片、取模分片等策略；数据安全领域的数据存储加密，则预置有 AES 、RC4、MD5 加密等策略。同时为进一步简化操作，通过新增的强大 DistSQL 能力，用户只需一条 SQL 即可在线动态创建一张分片或加密表。

除预置常用功能外，为满足更为复杂使用场景，ShardingSphere 也开放相关算法与策略接口，让用户可按照自己实际的业务场景，注入更为复杂功能实现。简单内置与自主开放的并存策略，一直是 ShardingSphere 的架构设计哲学。

### 4. 其他功能

#### 性能提升：元数据加载优化

由于 ShardingSphere 要帮助用户屏蔽及管理所有数据库实例及元数据信息，故在应用启动时，会出现长时间加载元数据信息的性能问题，特别是在上千台服务器实例情况下，元数据加载慢的问题更为明显。本次发行的版本，特别针对社区呼声较高的元数据加载问题进行多次性能调优和架构调整。取代原生的 JDBC 驱动加载方式，改为针对不同数据库方言，进行并行化的 SQL 查询方式一次性取出所有元数据信息，从而大幅度提升启动性能。

#### 使用便捷：新增内置性能测试体系

ShardingSphere 在功能不断完善、新功能不断开发的进程中，一直欠缺一套完善、全面的整合 & 性能测试体系，在保证每次提交都能正常编译的同时，不影响其他模块，并能观测到性能的上升及下降趋势。此外，针对数据分片、数据加密、读写分离、分布式管控、权限控制、SQL 支持度等功能进行整合测试保证；对不同数据库、不同分片或加密策略、不同版本之间的性能进行监控和调优提供基础保证。

配合本次 beta 版本发布，相关性能测试报表、曲线变化展示也将陆续为社区开发，方便用户了解 ShardingSphere 的性能变化。此外整套测试体系源码也将提供给社区，方便用户自行部署测试，感谢 SphereEx(sphere-ex.com) 公司为社区贡献整套的性能测试体系。

除了上述列举的功能外，本次发布还进行了其他方面的功能增强、性能优化、缺陷修复等处理。在后续的系列文章中，我们将会持续为大家带来 Apache ShardingSphere 5.0.0-beta 的正式发布报道、各个特性及功能的深度技术文章，欢迎锁定我们的系列更新！

**🔗 ShardingSphere GitHub 地址：**

<https://github.com/apache/shardingsphere>

**在使用 ShardingSphere 的过程中，如果您发现任何问题，有新的想法、建议，欢迎点击 [链接](https://shardingsphere.apache.org/community/cn/involved/subscribe/) 通过 Apache 邮件列表参与到 ShardingSphere 的社区建设中。**