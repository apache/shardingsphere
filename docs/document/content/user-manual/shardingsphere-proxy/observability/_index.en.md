+++
title = "Observability"
weight = 5
+++

## Compile source code
Download Apache ShardingSphere from GitHub,Then compile.
```shell
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```
Output directory: shardingsphere-agent/shardingsphere-agent-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz

## Agent configuration
* Directory structure

  Create agent directory, and unzip agent distribution package to the directory.
```shell
mkdir agent
tar -zxvf apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz -C agent
cd agent
tree 
.
└── apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin
    ├── LICENSE
    ├── NOTICE
    ├── conf
    │   ├── agent.yaml
    │   └── logback.xml
    ├── plugins
    │   ├── shardingsphere-agent-logging-base-${latest.release.version}.jar
    │   ├── shardingsphere-agent-metrics-prometheus-${latest.release.version}.jar
    │   ├── shardingsphere-agent-tracing-jaeger-${latest.release.version}.jar
    │   ├── shardingsphere-agent-tracing-opentelemetry-${latest.release.version}.jar
    │   ├── shardingsphere-agent-tracing-opentracing-${latest.release.version}.jar
    │   └── shardingsphere-agent-tracing-zipkin-${latest.release.version}.jar
    └── shardingsphere-agent.jar
```
* Configuration file

`conf/agent.yaml` is used to manage agent configuration.
Built-in plugins include Jaeger, OpenTracing, Zipkin, OpenTelemetry, Logging and Prometheus.
When a plugin needs to be enabled, just remove the corresponding name in `ignoredPluginNames`.

```yaml
applicationName: shardingsphere-agent
ignoredPluginNames:
  - Jaeger
  - OpenTracing
  - Zipkin
  - OpenTelemetry
  - Logging
  - Prometheus

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
  Zipkin:
    host: "localhost"
    port: 9411
    props:
      SERVICE_NAME: "shardingsphere-agent"
      URL_VERSION: "/api/v2/spans"
      SAMPLER_TYPE: "const"
      SAMPLER_PARAM: "1"
  OpenTracing:
    props:
      OPENTRACING_TRACER_CLASS_NAME: "org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer"
  OpenTelemetry:
    props:
      otel.resource.attributes: "service.name=shardingsphere-agent"
      otel.traces.exporter: "zipkin"
  Logging:
    props:
      LEVEL: "INFO"

```
* Parameter description:

| Name       |  Description     |  Value range    |  Default value     |
| :--------- | :-------- |:--------- | :-------- |
| JVM_INFORMATION_COLLECTOR_ENABLED      | Start JVM collector |  true, false  |  true  |
| SERVICE_NAME      | Tracking service name | Custom | shardingsphere-agent |
| JAEGER_SAMPLER_TYPE | Jaeger sample rate type | const, probabilistic, ratelimiting, remote | const |
| JAEGER_SAMPLER_PARAM  | Jaeger sample rate parameter |const:0, 1, probabilistic:0.0 - 1.0, ratelimiting: > 0, Customize the number of acquisitions per second, remote：need to customize the remote service addres,JAEGER_SAMPLER_MANAGER_HOST_PORT | 1 (const type) |
| SAMPLER_TYPE  | Zipkin sample rate type | const, counting, ratelimiting, boundary | const |
| SAMPLER_PARAM | Zipkin sampling rate parameter |const:0, 1, counting:0.01 - 1.0, ratelimiting: > 0, boundary:0.0001 - 1.0 | 1 (const type) |
| otel.resource.attributes | opentelemetry properties | String key value pair (, split) | service.name=shardingsphere-agent |
| otel.traces.exporter | Tracing expoter | zipkin, jaeger | zipkin |
| otel.traces.sampler | Opentelemetry sample rate type | always_on, always_off, traceidratio | always_on |
| otel.traces.sampler.arg | Opentelemetry sample rate parameter | traceidratio：0.0 - 1.0 | 1.0 |

## Usage in ShardingSphere-Proxy

* Edit the startup script

Configure the absolute path of shardingsphere-agent.jar to the start.sh startup script of shardingsphere proxy. 
```shell
nohup java ${JAVA_OPTS} ${JAVA_MEM_OPTS} \
-javaagent:/xxxxx/agent/shardingsphere-agent.jar \
-classpath ${CLASS_PATH} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
```

* Start ShardingSphere-Proxy
```shell
bin/start.sh
```
After startup, you can find the plugin info in the log of ShardingSphere-Proxy, `Metric` and `Tracing` data can be viewed through the configured monitoring address.
