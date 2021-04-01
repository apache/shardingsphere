+++
title = "Agent集成"
weight = 2
+++

## 背景

ShardingSphere-Agent 是独立自主设计，基于`Bytebuddy`字节码增加的项目，基于插件化的设计，可以无缝隙的与ShardingSphere集成，

目前有提供 Log, metrics, APM 等可观测性功能。

## 使用方法

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

启动时添加参数

```
-javaagent: \absolute path\shardingsphere-agent.jar
```

## Agent配置

在本地打包目录和解压缩目录下找到: `agent.yaml` 

```yaml
applicationName: shardingsphere-agent # 应用名称
ignoredPluginNames: #忽略的插件集合，表示集合里面的插件不生效
  - Opentracing
  - Jaeger
  - Zipkin
  - Prometheus
  - Logging

plugins:
  Prometheus:
    host:  "localhost" #prometheus暴露的host
    port: 9090 #prometheus暴露的端口
    props:
      JVM_INFORMATION_COLLECTOR_ENABLED : "true"
  Jaeger:
    host: "localhost" #jaeger服务的host
    port: 5775 #jaeger服务的端口
    props:
      SERVICE_NAME: "shardingsphere-agent"
      JAEGER_SAMPLER_TYPE: "const"
      JAEGER_SAMPLER_PARAM: "1"
      JAEGER_REPORTER_LOG_SPANS: "true"
      JAEGER_REPORTER_FLUSH_INTERVAL: "1"
  Zipkin:
    host: "localhost" #zipkin服务的host
    port: 9411 #zipkin服务的prot
    props:
      SERVICE_NAME: "shardingsphere-agent"
      URL_VERSION: "/api/v2/spans" #zipkin服务的抓取span的uri
  Logging:
    props:
      LEVEL: "INFO" #打印的日志级别

``

以上为agent的所有配置，注意：当配置 ignoredPluginNames时候，表示集合里面的插件会被忽略！