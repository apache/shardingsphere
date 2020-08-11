+++
title = "停滞数年后，ElasticJob 携首个 Apache 版本 3.0.0-alpha 回归"
weight = 1
chapter = true
+++
## 停滞数年后，ElasticJob 携首个 Apache 版本 3.0.0-alpha 回归！

在成为 Apache ShardingSphere 的子项目的几个月时间里，ElasticJob 社区在修复与合并了535个 issue 和 pull request 之后，发布了加入 Apache 软件基金会后的第一个正式版本：3.0.0-alpha。

### 背景
ElasticJob（ https://github.com/apache/shardingsphere-elasticjob ）是面向互联网生态和海量任务的分布式调度解决方案，由两个相互独立的子项目 ElasticJob-Lite 和 ElasticJob-Cloud 组成。它诞生于 2015年，当时业界虽然有 QuartZ 等出类拔萃的定时任务框架，但缺乏分布式方面的探索。分布式调度云平台产品的缺失，使得 ElasticJob 从出现伊始便备受关注。它有效地弥补了作业在分布式领域的短板，并且提供了一站式的自动化运维管控端，各个产品使用统一的作业 API，开发者仅需一次开发，即可随意部署。

ElasticJob 在技术选型时，选择站在了巨人的肩膀上而不是重复制造轮子的理念，将定时任务事实标准的 QuartZ 与 分布式协调的利器 ZooKeeper 完美结合，快速而稳定的搭建了全新概念的分布式调度框架。

### ElasticJob调度模型
ElasticJob 的调度模型划分为支持线程级别调度的进程内调度 ElasticJob-Lite，和进程级别调度的ElasticJob-Cloud。

**进程内调度**

ElasticJob-Lite 是面向进程内的线程级调度框架。它能够与 Spring 、Dubbo等 Java 框架配合使用，在作业中可自由使用 Spring 注入的 Bean，如数据源连接池、Dubbo 远程服务等，更加方便地贴合业务开发。

ElasticJob-Lite与业务应用部署在一起，其生命周期与业务应用保持一致，是典型的嵌入式轻量级架构。ElasticJob-Lite 非常适合于资源使用稳定、部署架构简单的普通 Java 应用，可以理解为 Java 开发框架。

ElasticJob-Lite 本身是无中心化架构，无需独立的中心化调度节点，分布式下的每个任务节点均是以自调度的方式适时的调度作业。任务之间只需要一个注册中心来对分布式场景下的任务状态进行协调即可，目前支持 ZooKeeper 作为注册中心。

架构图如下：

![](https://shardingsphere.apache.org/blog/img/alpha1.jpg)

通过图中可看出，ElasticJob-Lite 的分布式作业节点通过选举获取主节点，并通过主节点进行分片。分片完毕后，主节点与从节点并无二致，均以自我调度的方式执行任务。

**进程级调度**

ElasticJob-Cloud 拥有进程内调度和进程级别调度两种方式。由于 ElasticJob-Cloud 能够对作业服务器的资源进行控制，因此其作业类型可划分为常驻任务和瞬时任务。常驻任务类似于ElasticJob-Lite，是进程内调度；瞬时任务则完全不同，它充分的利用了资源分配的削峰填谷能力，是进程级的调度，每次任务的会启动全新的进程处理。

ElasticJob-Cloud 需要通过 Mesos 对资源进行控制，并且通过部署在 Mesos Master的调度器进行任务和资源的分配。Cloud采用中心化架构，将调度中心的高可用交由 Mesos管理。

它的架构图如下：

 ![](https://shardingsphere.apache.org/blog/img/alpha2.jpg)

通过图中可看出，ElasticJob-Cloud 除了拥有 Lite 的全部能力之外，还拥有资源分配和任务分发的能力。它将作业的开发、打包、分发、调度、治理、分片等一些列的生命周期完全托管，是真正的作业云调度系统。

相比于 ElasticJob-Lite 的简单易用，ElasticJob-Cloud 对 Mesos 的强依赖增加了系统部署的复杂度，因此更加适合大规模的作业系统。

### 功能列表

ElasticJob 功能主要有弹性调度、资源分配、作业治理和可视化管控。

**弹性调度**

弹性调度是 ElasticJob 最重要的功能，也是这款产品名称的由来。它是一款能够让任务通过分片进行水平扩展的任务处理系统。

ElasticJob 中任务分片项的概念，使得任务可以在分布式的环境下运行，每台任务服务器只运行分配给该服务器的分片。随着服务器的增加或宕机，ElasticJob 会近乎实时的感知服务器数量的变更，从而重新为分布式的任务服务器分配更加合理的任务分片项，使得任务可以随着资源的增加而提升效率。

**资源分配**

调度是指在适合的时间将适合的资源分配给任务，并使其生效。ElasticJob 具备资源分配的能力，它能够像分布式的操作系统一样调度任务。资源分配是借由 Mesos 实现的，由 Mesos 负责分配任务声明的所需资源（CPU 和内存），并将分配出去的资源进行隔离。ElasticJob 在获取到资源之后才会执行任务。

考虑到 Mesos 系统部署相对复杂，因此 ElasticJob 将这部分拆分至 ElasticJob cloud 部分，供高级用户使用。随着 Kubernetes 的强劲发展，ElasticJob 未来也会完成 Cloud 部分与它的对接。

**作业治理**

作业在分布式场景下的高可用、失效转移、错过作业重新执行等行为的治理和协调。

**可视化管控端**

主要包括作业声明周期管控、执行历史记录查询、配置中心管理等。

### 3.0.0-alpha 功能先睹为快

**构建 & 依赖**

1. 升级至 Java 8

2. 升级最低支持的  ZooKeeper 版本 至 3.6.x

**API 变更**

1. 将 Maven 坐标的 groupId 变更为org.apache.shardingsphere.elasticjob

2. 将包名称变更为org.apache.shardingsphere.elasticjob

3. 将 Spring 命名空间名称变更为 http://shardingsphere.apache.org/schema/elasticjob

4. 全新的作业 API，可使用 SPI 自定制作业类型

5. 使用 SPI 引用配置策略，如任务分片、线程池使用和错误处理等策略

6. 将控制台代码从作业核心模块中分离

**新功能**

1. 调度器多元化，增加一次性任务调度器

2. 提供ElasticJob-Lite 项目的 官方 Spring Boot Starter

3. 支持使用多种数据库类型存储作业历史轨迹数据

4. 允许用户通过环境变量指定适合的 IP 地址

5. 全新的控制台界面

### 3.x版本设计解读

通过Release Notes能够看出，ElasticJob 3.x 并非 2.x 的修补版本，而是通过革新的设计理念践行的一套新产品。

ElasticJob 3.x 最直观的变化是将原有的个位数的模块数量拆分为数十个职责清理的微模块。

新版本的关键词是微内核、可扩展和生态对接。

**微内核**

ElasticJob 3.x 抽象了 API 和基础设施模块，并且将注册中心、历史执行轨迹、控制台、作业执行器、Lite和Cloud等模块全数分离。

内核模块高度可扩展，但不依赖于可扩展模块本身的实现。它继承了 ElasticJob 之前的能力，在继续为开发者提供分布式服务的工具包的同时，向开发者开放可自由定制化扩展的脚手架。

**可扩展**

ElasticJob 3.x在微内核的基础上定义了丰富的可扩展接口，包括作业类型、配置策略、历史执行轨迹存储端以及将要做的注册中心存储端等可扩展接口。

开发者可以在不修改 ElasticJob 源码的情况下织入定制化功能，真正做到对修改关闭，对扩展开放。

**生态对接**

ElasticJob 3.x 提供了官方的Spring Boot Starter，并已经着手开发基于 Apache SkyWalking 的自动探针，使其能更加便捷的融入现有的技术体系。

另外，从Release Notes中可以解读到的是，ElasticJob 3.x 并未对 Cloud进行大幅更新，其主要改动均集中在内核以及Lite模块。

对于部署复杂且逐渐不再流行的 Mesos，ElasticJob 的 3.x 将渐渐弱化对它的依赖，并计划在未来提供更加泛化资源隔离 API，使 Cloud 产品线可对接Mesos，Kubernetes 甚至无依赖的独立部署使用。

### 3.0.0-beta 功能预告

在调整完项目和包结构之后，ElasticJob 3.0.0-beta版本将工作重点放在新功能开发和操作 API 标准化这两个方面。

**新功能预告**

1. 作业依赖

支持基于有向无环图（DAG）的作业依赖。依赖包含基于作业整体维度的依赖，以及基于作业分片项的依赖，打造更加灵活的作业治理解决方案。

2. HTTP 作业类型

支持HTTP作业类型，在Script 之外提供另外的跨语言作业类型。

**操作 API 标准化**

1. 统一提供基于 RESTful 操作API接口

2. 简化基于 SDK  操作API接口

### 未来规划

未来，ElasticJob 将大刀阔斧的向前迈进，主要的规划如下：

**调度执行分离**

将调度器和执行器完全分离。调度器可以与执行器一起部署，即为 ElasticJob lite 的无中心化轻量级版本；调度器可以与执行器分离部署，即为 ElasticJob cloud 的资源管控的一站式分布式调度系统。

**更加易用的云管产品**

将目前仅支持 Mesos 的 ElasticJob cloud 打造为支持 Mesos 和 Kubernetes 的作业云管平台，并提供无 Mesos 和 Kubernetes 也能够独立使用的不包含资源管控的纯作业管控平台。

**可插拔生态**

与 Apache ShardingSphere 一脉相承，ElasticJob 也将提供更加可插拔和模块化架构，为开发者提供基础设施。方便开发者基于 ElasticJob 二次开发，添加各种定制化功能，包括但不限于作业类型（如：大数据作业、HTTP作业等）、注册中心类型（如：Eureka等）、执行轨迹存储介质（如其他数据库类型）等。


ElasticJob 最终会将 Lite 和 Cloud 以更贴近的方式供开发工程师和运维工程师使用，共享其调度、执行和作业库。整体规划如下：

![](https://shardingsphere.apache.org/blog/img/alpha3.jpg)

### 关于 ElasticJob 社区

ElasticJob 社区在之前的几年处于停滞状况，主要原因是作者精力有限，分身乏术。在接收到了作为 Apache ShardingSphere 弹性迁移的调度基础设施的需求之后，本就一脉相承的 ElasticJob 社区决定重启，并且作为 Apache ShardingSphere 的子项目继续发光发热。目前的 ElasticJob 已正式将项目源码迁入 Apache 的 GitHub 仓库，并且在重启的几个月来十分活跃，在GitHub 周和月度趋势排名中榜上有名。

ElasticJob 是Apache ShardingSphere（ https://github.com/apache/shardingsphere ）的子项目，目标是成为独立的 Apache 顶级项目，以及为 Apache ShardingSphere 的弹性迁移提供数据调度的基石。

### 作者简介

张亮，京东数科数字技术中心架构专家，Apache ShardingSphere PMC Chair。

热爱开源，擅长以 Java 为主分布式架构，推崇优雅代码。

目前主要精力投入在将分布式数据库中间件 Apache ShardingSphere 打造为业界一流的金融级数据解决方案之上。

Apache ShardingSphere( https://github.com/apache/shardingsphere )是京东主导的首个 Apache 软件基金会顶级项目，也是 Apache 软件基金会首个分布式数据库中间件。

曾出版书籍《未来架构——从服务化到云原生》。

GitHub: https://github.com/terrymanu ，随时欢迎技术交流和指正。
