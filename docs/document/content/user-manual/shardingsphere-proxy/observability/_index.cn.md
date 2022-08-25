+++
title = "可观察性"
weight = 5
+++

## 源码编译
从 Github 下载 Apache ShardingSphere 源码，对源码进行编译，操作命令如下。
```shell
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```
agent 包输出目录为 shardingsphere-agent/shardingsphere-agent-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz

## agent 配置
* 目录说明

创建 agent 目录，解压 agent 二进制包到 agent 目录。
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
* 配置说明

`conf/agent.yaml` 用于管理 agent 配置。
内置插件包括 Jaeger、OpenTracing、Zipkin、OpenTelemetry、Logging 及 Prometheus。
当需要开启插件时，只需要移除 `ignoredPluginNames` 中对应的插件名称即可。

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
* 参数说明；

| 名称       | 说明     |取值范围    |默认值     |
| :--------- | :-------- |:--------- | :-------- |
| JVM_INFORMATION_COLLECTOR_ENABLED      | 是否开启 JVM 采集器 |true、false|true|
| SERVICE_NAME      | 链路跟踪的服务名称 | 自定义 | shardingsphere-agent |
| JAEGER_SAMPLER_TYPE | Jaeger 采样率类型 | const、probabilistic、ratelimiting、remote | const |
| JAEGER_SAMPLER_PARAM  | Jaeger 采样率参数 |const：0、1，probabilistic：0.0 - 1.0，ratelimiting：> 0，自定义每秒采集数量，remote：需要自定义配置远程采样率管理服务地址，JAEGER_SAMPLER_MANAGER_HOST_PORT |1（const 类型）|
| SAMPLER_TYPE  | Zipkin 采样率类型 |const、counting、ratelimiting、boundary | const |
| SAMPLER_PARAM | Zipkin 采样率参数 |const： 0、1，counting：0.01 - 1.0，ratelimiting：> 0，自定义每秒采集数量，boundary: 0.0001 - 1.0 | 1（const 类型）|
| otel.resource.attributes|opentelemetry 资源属性 | 字符串键值对（,分割） | service.name=shardingsphere-agent |
| otel.traces.exporter | Tracing expoter | zipkin、jaeger | zipkin |
| otel.traces.sampler | opentelemetry 采样率类型 | always_on、always_off、traceidratio | always_on |
| otel.traces.sampler.arg | opentelemetry 采样率参数 | traceidratio：0.0 - 1.0 | 1.0 |

## ShardingSphere-Proxy 中使用

* 编辑启动脚本

配置 shardingsphere-agent.jar 的绝对路径到 ShardingSphere-Proxy 的 start.sh 启动脚本中，请注意配置自己对应的绝对路径。
```shell
nohup java ${JAVA_OPTS} ${JAVA_MEM_OPTS} \
-javaagent:/xxxxx/agent/shardingsphere-agent.jar \
-classpath ${CLASS_PATH} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
```

* 启动 ShardingSphere-Proxy
```shell
bin/start.sh
```
正常启动后，可以在 ShardingSphere-Proxy 日志中找到 plugin 的加载信息，访问 Proxy 后，可以通过配置的监控地址查看到 `Metric` 和 `Tracing` 的数据。
