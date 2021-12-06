+++
title = "应用性能监控集成"
weight = 2
+++

## 使用方法

### 使用 OpenTracing 协议

* 方法1：通过读取系统参数注入APM系统提供的 Tracer 实现类

启动时添加参数

```
-Dorg.apache.shardingsphere.tracing.opentracing.tracer.class=org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer
```

调用初始化方法

```java
ShardingTracer.init();
```

* 方法2：通过参数注入 APM 系统提供的 Tracer 实现类。

```java
ShardingTracer.init(new SkywalkingTracer());
```

*注意：使用 SkyWalking 的 OpenTracing 探针时，应将原 Apache ShardingSphere 探针插件禁用，以防止两种插件互相冲突。*

### 使用 SkyWalking 自动探针

请参考 [SkyWalking 部署手册](https://github.com/apache/skywalking/blob/5.x/docs/cn/Quick-start-CN.md)。

### 使用 OpenTelemetry

在agent.yaml中填写好配置即可，例如将 Traces 数据导出到 Zipkin。

```yaml
OpenTelemetry:
    props:
      otel.resource.attributes: "service.name=shardingsphere-agent"
      otel.traces.exporter: "zipkin"
      otel.exporter.zipkin.endpoint: "http://127.0.0.1:9411/api/v2/spans"
```

## 效果展示

无论使用哪种方式，都可以方便的将APM信息展示在对接的系统中，以下以 SkyWalking 为例。

### 应用架构

使用 ShardingSphere-Proxy 访问两个数据库 `192.168.0.1:3306` 和 `192.168.0.2:3306`，且每个数据库中有两个分表。

### 拓扑图展示

![拓扑图](https://shardingsphere.apache.org/document/current/img/apm/5x_topology.png)

从图中看，用户访问 18 次 ShardingSphere-Proxy 应用，每次每个数据库访问了两次。这是由于每次访问涉及到每个库中的两个分表，所以每次访问了四张表。

### 跟踪数据展示

![跟踪图](https://shardingsphere.apache.org/document/current/img/apm/5x_trace.png)

从跟踪图中可以能够看到 SQL 解析和执行的情况。

`/Sharding-Sphere/parseSQL/`: 表示本次 SQL 的解析性能。

![解析节点](https://shardingsphere.apache.org/document/current/img/apm/5x_parse.png)

`/Sharding-Sphere/executeSQL/`: 表示具体执行的实际 SQL 的性能。

![实际访问节点](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL.png)

### 异常情况展示

![异常跟踪图](https://shardingsphere.apache.org/document/current/img/apm/5x_trace_err.png)

从跟踪图中可以能够看到发生异常的节点。

`/Sharding-Sphere/executeSQL/`: 表示执行 SQL 异常的结果。

![异常节点](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL_Tags_err.png)

`/Sharding-Sphere/executeSQL/`: 表示执行 SQL 异常的日志。

![异常节点日志](https://shardingsphere.apache.org/document/current/img/apm/5x_executeSQL_Logs_err.png)
