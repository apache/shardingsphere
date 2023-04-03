+++
title = "可观察性"
weight = 5
+++

## Agent

### 源码编译

从 Github 下载 Apache ShardingSphere 源码，对源码进行编译，操作命令如下。

```shell
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```
agent 包输出目录为 distribution/agent/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz

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
Agent 日志输出位置在 `agent/logs/stdout.log`。

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

| 名称                                | 说明            |
|-----------------------------------|---------------|
| host                              | 主机            |
| port                              | 端口            |
| jvm-information-collector-enabled | 是否采集 JVM 指标信息 |

#### OpenTelemetry

OpenTelemetry 可以导出 tracing 数据到 Jaeger，Zipkin。

* 参数说明

| 名称                                 | 说明              |
|------------------------------------|-----------------|
| otel.service.name                  | 服务名称            |
| otel.traces.exporter               | traces exporter |
| otel.exporter.otlp.traces.endpoint | traces endpoint |
| otel.traces.sampler                | traces sampler  |

参数参考 [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)


## ShardingSphere-Proxy 中使用

### 通过非容器环境使用

* 编辑启动脚本

配置 shardingsphere-agent-${latest.release.version}.jar 的绝对路径到 ShardingSphere-Proxy 的 start.sh 启动脚本中，请注意配置自己对应的绝对路径。

```shell
nohup java ${JAVA_OPTS} ${JAVA_MEM_OPTS} \
-javaagent:/xxxxx/agent/shardingsphere-agent-${latest.release.version}.jar \
-classpath ${CLASS_PATH} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
```

* 启动 ShardingSphere-Proxy

```shell
bin/start.sh
```
正常启动后，可以在 ShardingSphere-Proxy 日志中找到 plugin 的加载信息，访问 Proxy 后，可以通过配置的监控地址查看到 `Metric` 和 `Tracing` 的数据。

### 通过容器环境使用

- 假设本地已完成如下的对应配置。
  - 包含 ShardingSphere-Agent 二进制包解压后的所有文件的文件夹 `./custom/agent/`
  - 包含 `server.yaml` 等 ShardingSphere-Proxy 的配置文件的文件夹为 `./custom/conf/`

- 此时可通过环境变量 `JVM_OPT` 来配置 ShardingSphere-Agent 的使用。
  以在 Docker Compose 环境下启动为例，合理的 `docker-compose.yml` 示例如下。

```yaml
version: "3.8"

services:
  apache-shardingsphere-proxy:
    image: apache/shardingsphere-proxy:latest
    environment:
      JVM_OPTS: "-javaagent:/agent/shardingsphere-agent-${latest.release.version}.jar"
      PORT: 3308
    volumes:
      - ./custom/agent/:/agent/
      - ./custom/conf/:/opt/shardingsphere-proxy/conf/
    ports:
      - "13308:3308"
```

## Metrics

| 指标名称                         | 指标类型      | 指标描述                                                                      |
|:-----------------------------|:----------|:--------------------------------------------------------------------------|
| build_info                   | GAUGE     | 构建信息                                                                      |
| parsed_sql_total             | COUNTER   | 按类型（INSERT、UPDATE、DELETE、SELECT、DDL、DCL、DAL、TCL、RQL、RDL、RAL、RUL）分类的解析总数   |
| routed_sql_total             | COUNTER   | 按类型（INSERT、UPDATE、DELETE、SELECT）分类的路由总数                                   |
| routed_result_total          | COUNTER   | 路由结果总数(数据源路由结果、表路由结果)                                                     |
| proxy_state                  | GAUGE     | ShardingSphere-Proxy 状态信息。0 表示正常状态；1 表示熔断状态；2 锁定状态                        |
| proxy_meta_data_info         | GAUGE     | ShardingSphere-Proxy 元数据信息，database_count：逻辑库数量，storage_unit_count：存储节点数量 |
| proxy_current_connections    | GAUGE     | ShardingSphere-Proxy 的当前连接数                                               |
| proxy_requests_total         | COUNTER   | ShardingSphere-Proxy 的接受请求总数                                              |
| proxy_transactions_total     | COUNTER   | ShardingSphere-Proxy 的事务总数，按 commit，rollback 分类                           |
| proxy_execute_latency_millis | HISTOGRAM | ShardingSphere-Proxy 的执行耗时毫秒直方图                                           |
| proxy_execute_errors_total   | COUNTER   | ShardingSphere-Proxy 的执行异常总数                                              |
