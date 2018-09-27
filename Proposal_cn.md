# Sharding-Sphere Proposal

## Abstract

Sharding-Sphere是一套透明化的分布式数据库中间件生态圈，专注于数据分片、分布式事务和数据库治理。
它通过实现JDBC接口的驱动程序（Sharding-JDBC）和通过实现数据库传输协议的代理层（Sharding-Proxy）为应用程序提供最大兼容性。

## Proposal

Sharding-Sphere在中国的社区较为成熟和活跃，拥有大量用户，很多公司和组织均采用Sharding-Sphere作为其处理海量数据的解决方案。

我们希望将 Sharding-Sphere 引入 Apache Software Foundation，以构建一个全球化、多样性、更强大的开源社区。

当当网提交此项提案，将Sharding-Sphere的源代码和所有相关文档捐赠给Apache Software Foundation。 这些代码已经在Apache License Version 2.0之下。

- Code base: <https://github.com/sharding-sphere/sharding-sphere>
- Web site: <http://shardingsphere.io/>
- Documentations: <http://shardingsphere.io/document/current/>
- Community: <http://shardingsphere.io/community/>

## Background

由于互联网公司的数据迅速增长，关系型数据库难以支撑如此庞大的数据，但由于对关系型数据库的长期技术累积，开发人员和DBA仍然希望使用其来存储核心数据。

2016年，Sharding-Sphere在GitHub上开源。Sharding-Sphere一开始仅仅是当当内部用于数据分片的JDBC驱动程序增强版（原名Sharding-JDBC），目前提供数据分片、分布式事务和数据库治理的功能。
除JDBC模式之外，也提供了实现MySQL数据库协议的Proxy模式。我们的蓝图中还包括PostgreSQL协议的Proxy、Sidecar模式、多数据副本以及弹性扩缩等功能。

由于对项目范围的扩展，我们在实现JDBC接口的同时，也另外提供了Proxy模式，并且即将提供Sidecar模式。
因此通过公投，我们将其重命名为Sharding-Sphere，意为分片生态圈，而Sharding-JDBC，Sharding-Proxy和Sharding-Sidecar将作为它的子项目存在。

Sharding-Sphere的前身——Sharding-JDBC在开源当年即入选“2016年度最受欢迎中国开源软件”前20名。

## Rationale

关系型数据库在当前的应用系统中仍然起着非常重要的作用。
产品和周围生态系统的成熟度，数据查询的友好性，开发人员和DBA对其掌握的程度，这些都不是新形态的数据库能够轻易取代的。但是传统的关系型数据库并不能很好地支持云原生，而且对分布式系统也很不友好。

Sharding-Sphere的最终方案是让用户像使用单数据库一样使用分布式数据库，并具备管理和协调散落在系统中的各个数据库的能力。
Sharding-Sphere有3个子项目，分别是Sharding-JDBC，Sharding-Proxy和Sharding-Sidecar（规划中）。

Sharding-JDBC使用JDBC连接数据库，由Java应用直接连接数据库，无额外消耗，性能最高。

Sharding-Proxy部署为无状态服务器，目前支持MySQL协议。
在文章“ [What’s Really New with NewSQL?](https://db.cs.cmu.edu/papers/2016/pavlo-newsql-sigmodrec2016.pdf)” 中，有3种类型的NewSQL，Sharding-Proxy是其中的透明化分片中间件。

Sharding-Sidecar可以理解为Service Mesh中的数据面板。在应用程序和数据库之间的交互提供一个啮合层。Database Mesh的概念与Service Mesh类似，其关注点在于如何将数据访问应用与数据库连接在一起。
Database Mesh将应用与数据库会组成一个巨大的网格体系，应用和数据库只需在网格体系中对号入座即可，它们都是被啮合层所治理的对象。

## Current Status

### Meritocracy

该项目于2015年在当当网孵化，2016年在GitHub开源。2017年，京东认可其价值，并决定支持此项目。我们成立了PMC团队和Committer团队。
该项目由来自许多公司的贡献者和用户。现有PMC成员指导并审查新的贡献者，在合适的时机，PMC会进行投票决定新的贡献者是否可以成为PMC和Committer团队中的一员。
点击[这里](http://shardingsphere.io/community/en/organization/)查看详细信息。我们欢迎并高度重视新的贡献。

### Community

目前我们在京东、当当和翼支付分别为Sharding-Sphere设立了开发团队。我们希望通过走Apache这条路，从而邀请所有贡献代码的优秀人才，扩大贡献者的圈子。现在，我们使用github作为代码托管，利用gitter进行社区通信。

### **Core** Developers

核心开发人员组成了一个多样化开发小组，包括经验丰富的开源软件开发人员及团队负责人。

#### PMC members

- 张亮, Liang Zhang, Java and architect expert, Jingdong
- 曹昊, Hao Cao, Senior Architect, Dangdang
- 吴晟, Sheng Wu, APM and tracing expert, Apache SkyWalking(incubator) creator & PMC member
- 高洪涛, Hongtao Gao, Database and APM expert, Apache SkyWalking(incubator) PMC member
- 史海峰, Haifeng Shi, @PegasusS Ex-Director, ele.me
- 张小虎, Haifeng Shi, @pottieva Ex-Director, CHINA TELECOM Bestpay

#### Committer members

- 张永伦, Yonglun Zhang, @tuohai666 Senior engineer, Jingdong
- 潘娟, Juan Pan, @tristaZero Senior DBA, Jingdong
- 赵俊, Jun Zhao, @cherrylzhao Senior engineer, Jingdong
- 岳令, Ling Yue, @ling.yue QA Engineer, Dangdang
- 马晓光, Xiaoguang Ma, Senior engineer, huimai365
- 陈清阳, QingYang Chen, @beckhampu Senior engineer, CHINA TELECOM Bestpay

## Known Risks

### Orphaned products

未来，京东、当当和翼支付三个开发团队连同不断壮大的社区贡献者将继续全力以赴更新维护Sharding-Sphere，并且Sharding-Sphere在中国已被许多公司和组织广泛应用。因此，Sharding-Sphere成为“孤儿产品”的风险很低。

### Inexperience with Open Source

Sharding-Sphere目前的核心开发人员所在公司开发或贡献过许多开源项目，包括Apache SkyWalking（孵化中），Apache Dubbo（孵化器），CNCF OpenTracing，Elastic-Job等。
因此，缺乏开源软件经验和开源流程经验的风险较低。

### Homogenous Developers

目前的核心开发人员遍布各大公司，包括京东、当当和翼支付，也包括一些个人开发人员。鉴于搜狐和数人云也对Sharding-Sphere感兴趣，我们准备鼓励大家为Sharding-Sphere贡献代码，邀请他们一起开发。

### Reliance on Salaried Developers

核心开发人员中有三位需雇主支付费用来为Sharding-Sphere项目贡献代码。我们预计Sharding-Sphere项目的开发将继续由有偿开发人员主导，同时我们会努力壮大社区，吸引更多无偿开发人员。

### Relationships with Other Apache Products

Sharding-Sphere团队与Apache SkyWalking团队合作，将Sharding-Sphere的自动探针引入到SkyWalking，为其发送APM数据。SkyWalking也将Sharding-Sphere作为其数据的存储引擎之一。

Sharding-Sphere团队与Apache ServiceComb团队合作，将ServiceComb提供的Saga作为分布式事务处理引擎之一。

Sharding-Sphere采用Apache Zookeeper作为注册中心。

### A Excessive Fascination with the Apache Brand

Sharding-Sphere社区承认Apache品牌会使Sharding-Sphere项目的价值和认可度大大提升。但是，我们主要关注Apache Software Foundation为其项目所搭建的社区以及社区发展的长期稳定性。

## Documentation

点击shardingsphere.io，查看整套Sharding-Sphere文档。（包括英文和中文简体）

- [English](http://shardingsphere.io/document/current/en/)
- [Chinese](http://shardingsphere.io/document/current/cn/)

## Initial Source

本项目包含三个不同的代码库，分别是：核心代码，使用示例和文档。下面是现有的三个git存储库。

- <https://github.com/sharding-sphere/sharding-sphere>
- <https://github.com/sharding-sphere/sharding-sphere-example>
- <https://github.com/sharding-sphere/sharding-sphere-doc>

## Source and Intellectual Property Submission Plan

该项目代码目前拥有Apache 2.0许可证，并且2016年当当网在项目开源之前宣布该项目不存在知识产权或许可证问题。项目进入Apache Incubator之后，当当网会提供SGA，所有committers会签署ICLA。

## External Dependencies

使用Apache Maven管理所有依赖项，不需要将任何外部库打包成源发布。大多数依赖项都具有Apache兼容许可证。但是MySQL使用GPL-2.0。

未来我们会删除MySQL依赖项。
Sharding-Proxy目前使用MySQL JDBC驱动连接MySQL，之后我们会通过SPI的方式，由使用方提供数据库的JDBC驱动。

| *Dependency*                        | *License*       |
| ----------------------------------- | --------------- |
| guava                               | Apache-2.0      |
| commons-pool                        | Apache-2.0      |
| commons-dbcp                        | Apache-2.0      |
| netty                               | Apache-2.0      |
| curator                             | Apache-2.0      |
| grpc                                | Apache-2.0      |
| lombok                              | MIT             |
| groovy                              | Apache-2.0      |
| snakeyaml                           | Apache-2.0      |
| spring-context-support              | Apache-2.0      |
| spring-context-test                 | Apache-2.0      |
| spring-boot-starter                 | Apache-2.0      |
| spring-boot-configuration-processor | Apache-2.0      |
| spring-boot-starter-test            | Apache-2.0      |
| slf4j                               | MIT             |
| logback                             | EPL-1.0         |
| junit                               | EPL-1.0         |
| hamcrest                            | BSD 3-clause    |
| mockito                             | MIT             |
| h2                                  | MPL-2.0/EPL-1.0 |
| mysql                               | GPL-2.0         |
| postgresql                          | BSD             |
| mssql-jdbc                          | MIT             |
| HikariCP                            | Apache-2.0      |

## Required Resources

### Git Repositories

- <https://github.com/sharding-sphere/sharding-sphere.git>
- <https://github.com/sharding-sphere/sharding-sphere-example.git>
- <https://github.com/sharding-sphere/sharding-sphere-doc.git>

### Issue Tracking

<https://github.com/sharding-sphere/sharding-sphere/issues>

### Continuous Integration tool

Travis

### Mailing Lists

[sharding@googlegroups.com](mailto:sharding+subscribe@googlegroups.com)

### Communication

Gitter <https://gitter.im/shardingsphere/Lobby>

Slack [https://sharding.slack.com](https://sharding.slack.com/)

## Initial Committers

- 张亮, Liang Zhang, [zhangliang@apache.org](mailto:zhangliang@apache.org)
- 曹昊, Hao Cao,
- 吴晟, Sheng Wu, [wusheng@apache.org](mailto:wusheng@apache.org)
- 高洪涛, Hongtao Gao, [hanahmily@apache.org](mailto:hanahmily@apache.org)
- 张永伦, Yonglun Zhang
- 潘娟, Juan Pan
- 赵俊, Jun Zhao
- 岳令, Ling Yue
- 马晓光, Xiaoguang Ma
- 陈清阳, QingYang Chen

## Affiliations

- 京东: Liang Zhang, Yonglun Zhang, Juan Pan, Jun Zhao
- 当当: Hao Cao, Ling Yue
- 翼支付: QingYang Chen
- 个人开发者: Sheng Wu, Hongtao Gao, Xiaoguang Ma

## Sponsors

### Champion

### Mentors

### Sponsoring Entity

期待Apache Incubator可以赞助此项目。
