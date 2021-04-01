+++
title = "Agent Integration"
weight = 2
+++

## Background

ShardingSphere-Agent is an independent and independently designed project based on ByteBuddy bytecode increase. Based on plugin design, it can integrate seamlessly with ShardingSphere.
There are currently Log, metrics, APM and other observability capabilities available.

## Usage

### Local build

```
 > cd  shardingsphere/shardingsphere-agent
 > mvn clean install

```

### Remote download（No release）

```
 > weget http://xxxxx/shardingsphere-agent.tar.gz
 > tar -zxvcf shardingsphere-agent.tar.gz

```

Add startup arguments

```
-javaagent:\absolute path\shardingsphere-agent.jar
```

## Agent Configuration

It is found under the local package directory and unzip directory : `agent.yaml` 

```yaml
applicationName: shardingsphere-agent # application name 
ignoredPluginNames: # A collection of ignored plugins, indicating that the plugins in the collection are not active
  - Opentracing
  - Jaeger
  - Zipkin
  - Prometheus
  - Logging

plugins:
  Prometheus:
    host:  "localhost" #prometheus host
    port: 9090 #prometheus port 
    props:
      JVM_INFORMATION_COLLECTOR_ENABLED : "true"
  Jaeger:
    host: "localhost" #jaeger host
    port: 5775 #jaeger prot
    props:
      SERVICE_NAME: "shardingsphere-agent"
      JAEGER_SAMPLER_TYPE: "const"
      JAEGER_SAMPLER_PARAM: "1"
      JAEGER_REPORTER_LOG_SPANS: "true"
      JAEGER_REPORTER_FLUSH_INTERVAL: "1"
  Zipkin:
    host: "localhost" #zipkin host
    port: 9411 #zipkin prot
    props:
      SERVICE_NAME: "shardingsphere-agent"
      URL_VERSION: "/api/v2/spans" #zipkin uri
  Logging:
    props:
      LEVEL: "INFO" #log level

``

 When ignoredPluginNames is configured, plugins in the collection are ignored!