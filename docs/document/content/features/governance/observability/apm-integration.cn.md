+++
title = "应用性能监控集成"
weight = 1
+++

## 背景

APM 是应用性能监控的缩写。目前 APM 的主要功能着眼于分布式系统的性能诊断，其主要功能包括调用链展示，应用拓扑分析等。

Apache ShardingSphere 并不负责如何采集、存储以及展示应用性能监控的相关数据，而是将 SQL 解析与 SQL 执行这两块数据分片的最核心的相关信息发送至应用性能监控系统，并交由其处理。
换句话说，Apache ShardingSphere 仅负责产生具有价值的数据，并通过标准协议递交至相关系统。Apache ShardingSphere 可以通过两种方式对接应用性能监控系统。

第一种方式是使用 OpenTracing API 发送性能追踪数据。面向 OpenTracing 协议的 APM 产品都可以与 Apache ShardingSphere 自动对接，比如 SkyWalking，Zipkin 和 Jaeger。
使用这种方式只需要在启动时配置 OpenTracing 协议的实现者即可。
它的优点是可以兼容所有的与 OpenTracing 协议兼容的产品作为 APM 的展现系统，如果采用公司愿意实现自己的 APM 系统，也只需要实现 OpenTracing 协议，即可自动展示 Apache ShardingSphere 的链路追踪信息。
缺点是 OpenTracing 协议发展并不稳定，较新的版本实现者较少，且协议本身过于中立，对于个性化的相关产品的实现不如原生支持强大。

第二种方式是使用 SkyWalking 的自动探针。
[Apache ShardingSphere](https://shardingsphere.apache.org) 团队与[Apache SkyWalking](https://skywalking.apache.org) 团队共同合作，在 SkyWalking 中实现了 Apache ShardingSphere 自动探针，可以将相关的应用性能数据自动发送到 SkyWalking 中。

## 使用方法

### 使用 OpenTracing 协议

* 方法1：通过读取系统参数注入APM系统提供的 Tracer 实现类

启动时添加参数

```
-Dorg.apache.shardingsphere.opentracing.tracer.class=org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer
```

调用初始化方法

```java
ShardingTracer.init();
```

* 方法2：通过参数注入APM系统提供的 Tracer 实现类

```java
ShardingTracer.init(new SkywalkingTracer());
```

*注意:使用 SkyWalking 的 OpenTracing 探针时，应将原 Apache ShardingSphere 探针插件禁用，以防止两种插件互相冲突*

### 使用 SkyWalking 自动探针

请参考 [SkyWalking 部署手册](https://github.com/apache/skywalking/blob/5.x/docs/cn/Quick-start-CN.md)。

## 效果展示

无论使用哪种方式，都可以方便的将APM信息展示在对接的系统中，以下以 SkyWalking 为例。

### 应用架构

使用 ShardingSphere-Proxy 访问两个数据库`192.168.0.1:3306`和`192.168.0.2:3306`，且每个数据库中有两个分表。

### 拓扑图展示

![拓扑图](https://shardingsphere.apache.org/document/current/img/apm/5x_topology.png)

从图中看，用户访问 18 次 ShardingSphere-Proxy 应用，每次每个数据库访问了两次。这是由于每次访问涉及到每个库中的两个分表，所以每次访问了四张表。

### 跟踪数据展示

![跟踪图](https://shardingsphere.apache.org/document/current/img/apm/5x_trace.png)

从跟踪图中可以能够看到 SQL 解析和执行的情况。

`/Sharding-Sphere/parseSQL/` : 表示本次 SQL 的解析性能。

![解析节点](https://shardingsphere.apache.org/document/current/img/apm/5x_parse.png)

`/Sharding-Sphere/executeSQL/` : 表示具体执行的实际 SQL 的性能。

![实际访问节点](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL.png)

### 异常情况展示

![异常跟踪图](https://shardingsphere.apache.org/document/current/img/apm/5x_trace_err.png)

从跟踪图中可以能够看到发生异常的节点。

`/Sharding-Sphere/executeSQL/` : 表示执行 SQL 异常的结果。

![异常节点](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL_Tags_err.png)

`/Sharding-Sphere/executeSQL/` : 表示执行 SQL 异常的日志。

![异常节点日志](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL_Logs_err.png)
