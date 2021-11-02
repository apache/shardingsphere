+++
title = "使用探针"
weight = 1
+++

## 如何获取

### 本地构建

```
 > cd shardingsphere/shardingsphere-agent
 > mvn clean install
```

### 远程下载(暂未发布)

```
 > weget http://xxxxx/shardingsphere-agent.tar.gz
 > tar -zxvcf shardingsphere-agent.tar.gz
```

## 配置

找到 `agent.yaml` 文件： 

```yaml
applicationName: shardingsphere-agent
ignoredPluginNames: # 忽略的插件集合
  - Opentracing
  - Jaeger
  - Zipkin
  - Prometheus
  - OpenTelemetry
  - Logging

plugins:
  Prometheus:
    host:  "localhost" 
    port: 9090
    props:
      JVM_INFORMATION_COLLECTOR_ENABLED : "true"
  Jaeger:
    host: "localhost"
    port: 5775
    props:
      SERVICE_NAME: "shardingsphere-agent"
      JAEGER_SAMPLER_TYPE: "const"
      JAEGER_SAMPLER_PARAM: "1"
      JAEGER_REPORTER_LOG_SPANS: "true"
      JAEGER_REPORTER_FLUSH_INTERVAL: "1"
  Zipkin:
    host: "localhost"
    port: 9411
    props:
      SERVICE_NAME: "shardingsphere-agent"
      URL_VERSION: "/api/v2/spans"
  Opentracing:
    props:
      OPENTRACING_TRACER_CLASS_NAME: "org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer"
  OpenTelemetry:
    props:
      otel.resource.attributes: "service.name=shardingsphere-agent" # 多个配置用','分隔
      otel.traces.exporter: "zipkin"
  Logging:
    props:
      LEVEL: "INFO"
```

## 启动

在启动脚本中添加参数：

```
-javaagent:\absolute path\shardingsphere-agent.jar
```
