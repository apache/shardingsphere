+++

title = "DistSQL：像数据库一样使用 Apache ShardingSphere"
weight = 16
chapter = true

+++

Apache ShardingSphere 5.0.0-beta 深度解析的第一篇文章和大家一起重温了 ShardingSphere 的内核原理，并详细阐述了此版本在内核层面，特别是 SQL 能力方面的优化和提升。强大稳定的内核是 ShardingSphere 持续发展的基础，与此同时，ShardingSphere 在研发分布式数据库生态特性上也在努力摸索。本次 5.0.0-beta 版本发布的 DistSQL，用于搭配整个 ShardingSphere 分布式数据库体系，在提供更标准化的分布式数据库管理方式的同时，兼具灵活、便捷和优雅的特性。

本文将带领大家全面认识 DistSQL，并结合实战案例展示如何使用 DistSQL 一键管理 ShardingSphere 分布式数据库服务。

**作者｜孟浩然**

SphereEx 高级 Java 工程师

Apache ShardingSphere Committer

曾就职于京东科技，负责数据库产品研发，热爱开源，关注数据库生态，目前专注于 ShardingSphere 数据库中间件开发以及开源社区建设。

## 初识 DistSQL
相信大家对 SQL（Structured Query Language）都不陌生，SQL 是一种数据查询和程序设计语言，同时作为关系数据库管理系统的标准语言，用于存取数据以及查询、更新和管理关系数据库系统。

和标准 SQL 类似，DistSQL（Distributed SQL），即分布式 SQL，是 ShardingSphere 特有的一种内置 SQL 语言，能够提供标准 SQL 之外的增量功能操作能力。借助于 ShardingSphere 强大的 SQL 解析引擎，DistSQL 提供了类似于标准 SQL 的语法结构和语法校验体系，在保证规范化的同时，也让 DistSQL 更加灵活。

ShardingSphere 提出的 Database Plus 理念，旨在打造兼具数据库且贴合实际业务需求的开源分布式数据库体系，而 DistSQL 正是在传统数据库上层构建，提供既贴合标准又拥有 ShardingSphere 功能特色的 SQL 能力，能更好的为传统数据库赋能。



## DistSQL 设计初衷
ShardingSphere 快速发展的几年来，随着内核的逐步稳定，以及对核心功能的持续打磨，在数据库中间件领域，已然独树一帜。作为国内开源的佼佼者， ShardingSphere 在分布式数据库生态的探索中并没有停止脚步，打破中间件和数据库之间的界限，让开发者像使用数据库一样使用 Apache ShardingSphere，是 DistSQL 的设计目标，也是 ShardingSphere 从面向开发人员的框架和中间件转变为面向运维人员的基础设施产品不可或缺的能力。

## DistSQL 语法体系
DistSQL 在设计之初，就以面向标准为目标，充分考虑数据库开发和运维人员的使用习惯，所以 DistSQL 的语法全面借鉴标准 SQL 语言，兼顾可读性和易用性的同时，最大程度保留 ShardingSphere 自身的特性，并为使用者提供最宽泛的自定义边界，以应对不同的业务场景。对于熟悉 SQL 和 ShardingSphere 的开发者，可以快速入手。

标准的 SQL 提供了如 DQL、DDL、DML、DCL 等不同类型的语法，用于定义不同功能的 SQL 语句，DistSQL 也定义了一套自己的语法体系。

在 ShardingSphere 中， DistSQL 的语法目前主要划分为 RDL、RQL 和 RAL 三种类型：

- RDL（Resource & Rule Definition Language）：资源规则定义语言，用于资源和规则的创建、修改和删除；

- RQL（Resource & Rule Query Language）：资源规则查询语言，用于资源和规则的查询和展现；

- RAL（Resource & Rule Administrate Language）：资源规则管理语言，用于 Hint、事务类型切换、分片执行计划查询等增量功能操作。

DistSQL 的语法体系为 ShardingSphere 迈向分布式数据库搭起了桥梁，目前还在持续完善中，随着更多的想法被实现，DistSQL 势必会越来越强大。也欢迎对此感兴趣的开发者加入 ShardingSphere，为 DistSQL 提供想法，贡献代码。

了解更详细的语法规则，可参考官方文档：[https://shardingsphere.apache.org/document/current/cn/features/dist-sql/syntax/
](https://shardingsphere.apache.org/document/current/cn/features/dist-sql/syntax/)
## DistSQL 实战
在了解了 DistSQL 的设计理念和语法体系后，接下来本文以数据分片为例，实战演示如何通过 DistSQL 来搭建一套基于 ShardingSphere 的数据分片服务。

### 环境准备

- 启动 MySQL 服务

- 创建用于分片的 MySQL 数据库

- 启动 Zookeeper 服务

打开分布式治理配置并启动 ShardingSphere-Proxy ([https://shardingsphere.apache.org/document/current/cn/quick-start/shardingsphere-proxy-quick-start/](https://shardingsphere.apache.org/document/current/cn/quick-start/shardingsphere-proxy-quick-start/))

### 实战演示

- 使用 MySQL 命令行连接到启动的 ShardingSphere-Proxy

- 创建并查询分布式数据库 sharding_db

![](https://mmbiz.qpic.cn/mmbiz_png/fEJnpu1WIkBgs4DXhA9mkibbUOX7Hm0AnhOjBlWtZltFnQLiaLb7xqBPNmDNtXR75Bt0V5afo8ib6x2icU6sTH4Nzw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 使用新创建的数据库

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1kOw8DibEWlUWExm51MyicHe2MibZ7NflDTpiceQbE76E17E6HqDOXFzqGQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 执行 RDL 配置 2 个用于分片的数据源资源 ds_1 和 ds_2

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1VEGFny6NTTFvJwupZgScic32CWU5R7FSYcJ2Xxa9DQL0QGbkkenHkrw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 执行 RQL 查询新增加的数据源资源


![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1aFLiaEibVjTsp7sRNAtt1iafiaLno2NCgPIvK0wQUjrJ2ncG6sHKib94fjw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 执行 RDL 创建 t_order 表的分片规则

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb10UicJL0Y31TGOPHYPn9OicAyGdEODsU1NCFic2EOJJ4nDZ8uvBpia7mUEw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 执行 RQL 查询分片规则

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1XuOarBG79VdVsRGz5BicvD6CgnzxCzGR7UjkkcG3yKbqTRnjYGO8CCQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

RQL 除了支持查询当前数据库下的所有分片规则，也可以通过下面的语句查询单个表的分片规则

SHOW SHARDING TABLE RULE t_order FROM sharding_db

- 创建并查询 t_order 分片表

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1RcBmlArC4e8CdpD9WTAePONjibUu4RGapEeDLP4LDwTyIHBLOTZEAgg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 在 ShardingSphere-Proxy 端成功创建分片表 t_order 后，通过客户端连接到底层数据库 ds_1 和 ds_2，ShardingSphere 已经根据  t_order 表的分片规则自动创建了分片表

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb17UGyVbiaeHRZFf7njF2WqMPjjkBsXwToxJOmCCftqeBaSpEwv0W3djQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1cWejp3FF3RM6Q5hEjzoHQPb8cPErqxM7V0qWjRVOl6Ag4oYsc1JtBg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 分片表创建完成后，继续在 ShardingSphere-Proxy 端执行 SQL 语句插入数据

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1LGUpTbl43ScONHXic9Z4Vc1fzzjufeI9x7iaoDjicbOwty9PjHPnmDFfQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 通过 RAL 查询执行计划

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1Ruib2QEMSkuAtDOPGpdMkAHicetjEEYaTriaW3b4nl5s2KwueXvKJJX8w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

至此，使用 DistSQL 搭建 ShardingSphere 数据分片服务已经完成，对比之前版本以配置文件驱动的 ShardingSphere 代理端相比，DistSQL 对开发者更友好，对资源和规则的管理更加灵活，以 SQL 驱动的方式，更是实现了 DistSQL 和标准 SQL 的无缝对接。

![](https://mmbiz.qpic.cn/mmbiz_png/0UoCt9tgpnlSYkKFnzb9oOCQyL3WVEb1jmhjJrAEvpyYS8q0KZgicB1bJJByDBFKdibCcuL25Qk0AEosibTqd1f4Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

以上的示例中，仅演示了小部分 DistSQL 语法的使用，除了通过 CREATE 和 SHOW 语句创建和查询资源、规则，DistSQL 同样提供了 ALTRE DROP 等更多的操作，且同时支持对数据分片、读写分离、数据加密、数据库发现核心功能的配置管控。

## 结语

作为 5.0.0-beta 版本发布的新特性之一，DistSQL 也将从此版本开始持续发力，构建更完善的语法体系，提供更强大的功能。

DistSQL 为 ShardingSphere 在分布式数据库领域的探索提供了无限可能，在后续的规划中，DistSQL 将作为纽带串联起更多的功能，提供一键式操作。如一键分析数据库整体状态，和弹性迁移对接，提供一键数据扩容、缩容；和管控对接，实现一键切换主从、改变数据库状态等等。

最后，欢迎大家多多关注社区，了解 ShardingSphere 项目最新进展。

