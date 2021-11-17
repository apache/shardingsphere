+++
pre = "<b>4.10. </b>"
title = "可观察性"
weight = 10
chapter = true
+++

## 背景

如何观测集群的运行状态，使运维人员可以快速掌握当前系统现状，并进行进一步的维护工作，是分布式系统的全新挑战。
登录到具体服务器的点对点运维方式，无法适用于面向大量分布式服务器的场景。
可观察性和遥测是分布式系统推荐的运维方式。
APM（应用性能监控）和 Metrics（统计指标监控）是系统运行状况和健康度的重要可观察性指标。

APM 是应用性能监控的缩写。
主要功能着眼于分布式系统的性能诊断，包括调用链展示，应用拓扑分析等。

Apache ShardingSphere 并不负责如何采集、存储以及展示应用性能监控的相关数据，而是将 SQL 解析与 SQL 执行这两块数据分片的最核心的相关信息发送至应用性能监控系统，并交由其处理。
换句话说，Apache ShardingSphere 仅负责产生具有价值的数据，并通过标准协议递交至相关系统，可通过三种方式对接应用性能监控系统。

- 使用 OpenTracing API 发送性能追踪数据

面向 OpenTracing 协议的 APM 产品都可以与 Apache ShardingSphere 自动对接，比如 SkyWalking，Zipkin 和 Jaeger。
使用这种方式只需要在启动时配置 OpenTracing 协议的实现者即可。
它的优点是可以兼容所有的与 OpenTracing 协议兼容的产品作为 APM 的展现系统，如果采用公司愿意实现自己的 APM 系统，也只需要实现 OpenTracing 协议，即可自动展示 Apache ShardingSphere 的链路追踪信息。
缺点是 OpenTracing 协议发展并不稳定，较新的版本实现者较少，且协议本身过于中立，对于个性化的相关产品的实现不如原生支持强大。

- 使用 OpenTelemetry 发送性能追踪数据
OpenTelemetry 在2019年由 OpenTracing 和 OpenCencus 合并而来。
使用这种方式，只需要在agent配置文件中，根据 [OpenTelemetry SDK自动配置说明](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure) ，填写合适的配置即可。

- 使用 SkyWalking 的自动探针

[Apache ShardingSphere](https://shardingsphere.apache.org) 团队与[Apache SkyWalking](https://skywalking.apache.org) 团队共同合作，在 SkyWalking 中实现了 Apache ShardingSphere 自动探针，可以将相关的应用性能数据自动发送到 SkyWalking 中。

Metrics 则用于收集和展示整个集群的统计指标。

## 挑战

APM 和 Metrics 需要通过埋点来收集系统信息。
大量的埋点使项目核心代码支离破碎，难于维护，且不易定制化统计指标。

## 目标

提供尽量多的性能和统计指标，并隔离核心代码和埋点代码，是 Apache ShardingSphere 可观察性模块的设计目标。
