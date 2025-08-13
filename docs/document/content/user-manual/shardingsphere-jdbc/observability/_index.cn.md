+++
title = "可观察性"
weight = 7
+++

## Agent

### 源码编译

从 Github 下载 Apache ShardingSphere 源码，对源码进行编译，操作命令如下。

```shell
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -DskipITs -DskipTests -P-dev,release,all
```
Agent 制品 `distribution/agent/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz`

### 目录说明

创建 agent 目录，解压 agent 二进制包到 agent 目录。

```shell
mkdir agent
tar -zxvf apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz -C agent
cd agent
tree
├── LICENSE
├── NOTICE
├── conf
│   └── agent.yaml
├── plugins
│   ├── lib
│   │   ├── shardingsphere-agent-metrics-core-${latest.release.version}.jar
│   │   └── shardingsphere-agent-plugin-core-${latest.release.version}.jar
│   ├── logging
│   │   └── shardingsphere-agent-logging-file-${latest.release.version}.jar
│   ├── metrics
│   │   └── shardingsphere-agent-metrics-prometheus-${latest.release.version}.jar
│   └── tracing
│       ├── shardingsphere-agent-tracing-opentelemetry-${latest.release.version}.jar
└── shardingsphere-agent-${latest.release.version}.jar
```

### 配置说明

`conf/agent.yaml` 用于管理 agent 配置。内置插件包括 File、Prometheus、OpenTelemetry。

```yaml
plugins:
#  logging:
#    File:
#      props:
#        level: "INFO"
#  metrics:
#    Prometheus:
#      host:  "localhost"
#      port: 9090
#      props:
#        jvm-information-collector-enabled: "true"
#  tracing:
#    OpenTelemetry:
#      props:
#        otel.service.name: "shardingsphere"
#        otel.traces.exporter: "jaeger"
#        otel.exporter.otlp.traces.endpoint: "http://localhost:14250"
#        otel.traces.sampler: "always_on"
```

### 插件说明

#### File

目前 File 插件只有构建元数据耗时日志输出，暂无其他日志输出。

#### Prometheus

用于暴露监控指标

* 参数说明

| 名称                               | 说明                |
|-----------------------------------|---------------------|
| host                              | 主机                 |
| port                              | 端口                 |
| jvm-information-collector-enabled | 是否采集 JVM 指标信息  |

#### OpenTelemetry

OpenTelemetry 可以导出 tracing 数据到 Jaeger，Zipkin。

* 参数说明

| 名称                                  | 说明                |
|-------------------------------------|----------------------|
| otel.service.name                   | 服务名称              |
| otel.traces.exporter                | traces exporter      |
| otel.exporter.otlp.traces.endpoint  | traces endpoint      |
| otel.traces.sampler                 | traces sampler       |

参数参考 [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)

## 使用方式

+ 1 准备好已集成 `ShardingSphere-JDBC` 的 `SpringBoot` 项目，test-project.jar
+ 2 启动项目
```shell
java -javaagent:/agent/shardingsphere-agent-${latest.release.version}.jar -jar test-project.jar
```
+ 3 访问启动的服务
+ 4 查看对应的插件是否生效

### Docker

####  本地构建

ShardingSphere Agent 存在可用的 `Dockerfile` 用于方便分发。可执行如下命令以构建 Docker Image，

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/agent -P-dev,release,all,docker -T1C -DskipTests clean package
```

此后若在自定义 `Dockerfile` 中添加以下语句，这会将 ShardingSphere Agent 的目录复制到 `/shardingsphere-agent/` 。

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:latest /usr/agent/ /shardingsphere-agent/
```

#### 社区构建

自 ShardingSphere 5.5.2 开始，ShardingSphere Agent 在 https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-agent 发布社区构建。
此 Docker Image 不属于 ASF 分发产物之一，只是为了方便而提供。

若在自定义 `Dockerfile` 中添加以下语句，这会将 ShardingSphere Agent 的目录复制到 `/shardingsphere-agent/` 。

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:5.5.2 /usr/agent/ /shardingsphere-agent/
```

#### 夜间构建

ShardingSphere Agent 在 https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-agent 存在夜间构建的 Docker Image。

若在自定义 `Dockerfile` 中添加以下语句，这会将 ShardingSphere Agent 的目录复制到 `/shardingsphere-agent/` 。

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:latest /usr/agent/ /shardingsphere-agent/
```

#### 通过 Dockerfile 使用

引入一个典型场景，

1. 假设通过如下的 Bash 命令部署了 Jaeger All in One 的 Docker Container,

```shell
docker network create example-net
docker run --rm -d \
  --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  --network example-net \
  jaegertracing/all-in-one:1.62.0
```

2. 假设 `./custom-agent.yaml` 包含 ShardingSphere Agent 的配置，内容可能如下，

```yaml
plugins:
  tracing:
    OpenTelemetry:
      props:
        otel.service.name: "example"
        otel.exporter.otlp.traces.endpoint: "http://jaeger:4318"
```

3. 假设`./target/example.jar` 是一个即将使用 ShardingSphere Agent 的 Spring Boot 的 Uber JAR，
可通过类似如下的 `Dockerfile` 来为类似 `example.jar` 的 JAR 使用夜间构建的 Docker Image 中的 ShardingSphere Agent。

```dockerfile
FROM ghcr.io/apache/shardingsphere-agent:latest
COPY ./target/example.jar /app.jar
COPY ./custom-agent.yaml /usr/agent/conf/agent.yaml
ENTRYPOINT ["java","-javaagent:/usr/agent/shardingsphere-agent.jar","-jar","/app.jar"]
```

如果是通过本地构建 `ghcr.io/apache/shardingsphere-agent:latest` 的 Docker Image，`Dockerfile` 可能如下，

```dockerfile
FROM ghcr.io/apache/shardingsphere-agent:latest
COPY ./target/example.jar /app.jar
COPY ./custom-agent.yaml /usr/agent/conf/agent.yaml
ENTRYPOINT ["java","-javaagent:/usr/agent/shardingsphere-agent.jar","-jar","/app.jar"]
```

4. 享受它，

```shell
docker build -t example/gs-spring-boot-docker:latest .
docker run --network example-net example/gs-spring-boot-docker:latest
```

## Metrics

| 指标名称                                 | 指标类型    | 指标描述                                                                                       |
|:----------------------------------------|:----------|:----------------------------------------------------------------------------------------------|
| build_info                              | GAUGE     | 构建信息                                                                                       |
| parsed_sql_total                        | COUNTER   | 按类型（INSERT、UPDATE、DELETE、SELECT、DDL、DCL、DAL、TCL、RQL、RDL、RAL、RUL）分类的解析总数        |
| routed_sql_total                        | COUNTER   | 按类型（INSERT、UPDATE、DELETE、SELECT）分类的路由总数                                             |
| routed_result_total                     | COUNTER   | 路由结果总数(数据源路由结果、表路由结果)                                                            |
| jdbc_state                              | GAUGE     | ShardingSphere-JDBC 状态信息。0 表示正常状态；1 表示熔断状态；2 锁定状态                              |
| jdbc_meta_data_info                     | GAUGE     | ShardingSphere-JDBC 元数据信息                                                                  |
| jdbc_statement_execute_total            | COUNTER   | 语句执行总数                                                                                    |
| jdbc_statement_execute_errors_total     | COUNTER   | 语句执行错误总数                                                                                 |
| jdbc_statement_execute_latency_millis   | HISTOGRAM | 语句执行耗时                                                                                    |
| jdbc_transactions_total                 | COUNTER   | 事务总数，按 commit，rollback 分类                                                                |
