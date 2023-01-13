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

Artifact is agent/distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz

## Agent configuration

* Directory structure

  Create agent directory, and unzip agent distribution package to the directory.

```shell
mkdir agent
tar -zxvf apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz -C agent
cd agent
tree 
.
├── LICENSE
├── NOTICE
├── conf
│   └── agent.yaml
├── lib
│   ├── shardingsphere-agent-metrics-core-${latest.release.version}.jar
│   └── shardingsphere-agent-plugin-core-${latest.release.version}.jar
├── plugins
│   ├── shardingsphere-agent-logging-file-${latest.release.version}.jar
│   ├── shardingsphere-agent-metrics-prometheus-${latest.release.version}.jar
│   ├── shardingsphere-agent-tracing-jaeger-${latest.release.version}.jar
│   ├── shardingsphere-agent-tracing-opentelemetry-${latest.release.version}.jar
│   ├── shardingsphere-agent-tracing-opentracing-${latest.release.version}.jar
│   └── shardingsphere-agent-tracing-zipkin-${latest.release.version}.jar
└── shardingsphere-agent-${latest.release.version}.jar
```

* Configuration file

`conf/agent.yaml` is used to manage agent configuration.
Built-in plugins include Jaeger, OpenTracing, Zipkin, OpenTelemetry, Log and Prometheus.
No plugin is enabled by default.

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
#    Jaeger:
#      host: "localhost"
#      port: 6831
#      props:
#        service-name: "shardingsphere"
#        jaeger-sampler-type: "const"
#        jaeger-sampler-param: "1"
#        jaeger-reporter-flush-interval: "1000"
#        jaeger-reporter-max-queue-size: "100"
#    Zipkin:
#      host: "localhost"
#      port: 9411
#      props:
#        service-name: "shardingsphere"
#        url-version: "/api/v2/spans"
#        sampler-type: "const"
#        sampler-param: "1"
#    OpenTracing:
#      props:
#        opentracing-tracer-class-name: "org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer"
#    OpenTelemetry:
#      props:
#        otel-resource-attributes: "service.name=shardingsphere"
#        otel-traces-exporter: "zipkin"
```

* Parameter description:

| Name                              | Description                                                           | Value range                                                                                                                                                                                  | Default value                     |
|:----------------------------------|:----------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------|
| jvm-information-collector-enabled | Start JVM collector                                                   | true, false                                                                                                                                                                                  | true                              |
| service-name                      | Tracking service name                                                 | Custom                                                                                                                                                                                       | shardingsphere-agent              |
| jaeger-sampler-type               | Jaeger sample rate type                                               | const, probabilistic, ratelimiting, remote                                                                                                                                                   | const                             |
| jaeger-sampler-param              | Jaeger sample rate parameter                                          | const:0, 1, probabilistic:0.0 - 1.0, ratelimiting: > 0, Customize the number of acquisitions per second, remote：need to customize the remote service addres,JAEGER_SAMPLER_MANAGER_HOST_PORT| 1 (const type)                    |
| jaeger-reporter-flush-interval    | Jaeger the flush interval when reporting spans remotely (millisecond) | Custom                                                                                                                                                                                       | 1000                              |
| jaeger-reporter-max-queue-size    | Jaeger the maximum queue size for use when reporting spans remotely   | Custom                                                                                                                                                                                       | 100                               |
| url-version                       | Zipkin url address                                                    | Custom                                                                                                                                                                                       | /api/v2/spans                     |
| sampler-type                      | Zipkin sample rate type                                               | const, counting, ratelimiting, boundary                                                                                                                                                      | const                             |
| sampler-param                     | Zipkin sampling rate parameter                                        | const:0, 1, counting:0.01 - 1.0, ratelimiting: > 0, boundary:0.0001 - 1.0                                                                                                                    | 1 (const type)                    |
| otel-resource-attributes          | opentelemetry properties                                              | String key value pair (, split)                                                                                                                                                              | service.name=shardingsphere-agent |
| otel-traces-exporter              | Tracing expoter                                                       | zipkin, jaeger                                                                                                                                                                               | zipkin                            |
| otel-traces-sampler               | Opentelemetry sample rate type                                        | always_on, always_off, traceidratio                                                                                                                                                          | always_on                         |
| otel-traces-sampler-arg           | Opentelemetry sample rate parameter                                   | traceidratio：0.0 - 1.0                                                                                                                                                                       | 1.0                               |

## Usage in ShardingSphere-Proxy

### Using via a non-container environment

* Edit the startup script

Configure the absolute path of shardingsphere-agent-${latest.release.version}.jar to the start.sh startup script of shardingsphere proxy. 

```shell
nohup java ${JAVA_OPTS} ${JAVA_MEM_OPTS} \
-javaagent:/xxxxx/agent/shardingsphere-agent-${latest.release.version}.jar \
-classpath ${CLASS_PATH} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
```

* Start ShardingSphere-Proxy

```shell
bin/start.sh
```

After startup, you can find the plugin info in the log of ShardingSphere-Proxy, `Metric` and `Tracing` data can be viewed through the configured monitoring address.

### Use via container environment

- Assume that the following corresponding configurations have been completed locally.
  - Folder `./custom/agent/` that contains all files after unpacking ShardingSphere-Agent binary package
  - The folder containing the configuration files of ShardingSphere-Proxy such as `server.yaml` is `./custom/conf/`

- At this point, the use of ShardingSphere-Agent can be configured through the environment variable `JVM_OPT`.
  Taking starting in the Docker Compose environment as an example, a reasonable `docker-compose.yml` example is as
  follows.

```yaml
version: "3.8"

services:
  apache-shardingsphere-proxy:
    image: apache/shardingsphere-proxy:latest
    environment:
      JVM_OPTS: "-javaagent:/agent/shardingsphere-agent-${latest.release.version}.jar"
      PORT: 3308
    volumes:
      - ./custom/agent:/agent/
      - ./custom/conf:/opt/shardingsphere-proxy/conf/
    ports:
      - "13308:3308"
```

## Metrics

| Name                              | Type      | Description                                                                                                    |
| :-------------------------------- | :-------- | :------------------------------------------------------------------------------------------------------------- |
| build_info                        | GAUGE     | Build information                                                                                              |
| meta_data_info                    | GAUGE     | Meta data information. schema_count is logic number of databases; database_count is actual number of databases |
| parse_sql_dml_insert_total        | COUNTER   | Total count of parsed INSERT                                                                                   |
| parse_sql_dml_update_total        | COUNTER   | Total count of parsed UPDATE                                                                                   |
| parse_sql_dml_delete_total        | COUNTER   | Total count of parsed DELETE                                                                                   |
| parse_sql_dml_select_total        | COUNTER   | Total count of parsed SELECT                                                                                   |
| parse_sql_ddl_total               | COUNTER   | Total count of parsed DDL                                                                                      |
| parse_sql_dcl_total               | COUNTER   | Total count of parsed DCL                                                                                      |
| parse_sql_dal_total               | COUNTER   | Total count of parsed DAL                                                                                      |
| parse_sql_tcl_total               | COUNTER   | Total count of parsed TCL                                                                                      |
| parse_dist_sql_rql_total          | COUNTER   | Total count of parsed RQL                                                                                      |
| parse_dist_sql_rdl_total          | COUNTER   | Total count of parsed RDL                                                                                      |
| parse_dist_sql_ral_total          | COUNTER   | Total count of parsed RAL                                                                                      |
| route_sql_insert_total            | COUNTER   | Total count of routed INSERT                                                                                   |
| route_sql_update_total            | COUNTER   | Total count of routed UPDATE                                                                                   |
| route_sql_delete_total            | COUNTER   | Total count of routed DELETE                                                                                   |
| route_sql_select_total            | COUNTER   | Total count of routed SELECT                                                                                   |
| route_datasource_total            | COUNTER   | Total count of data source routed                                                                              |
| route_table_total                 | COUNTER   | Total count of table routed                                                                                    |
| proxy_info                        | GAUGE     | Status information of ShardingSphere-Proxy. 1 is OK; 2 is CIRCUIT BREAK                                        |
| proxy_current_connections         | GAUGE     | Current connections of ShardingSphere-Proxy                                                                    |
| proxy_requests_total              | COUNTER   | Total requests of ShardingSphere-Proxy                                                                         |
| proxy_commit_transactions_total   | COUNTER   | Total commit transactions of ShardingSphere-Proxy                                                              |
| proxy_rollback_transactions_total | COUNTER   | Total rollback transactions of ShardingSphere-Proxy                                                            |
| proxy_execute_latency_millis      | HISTOGRAM | Execute latency millis histogram of ShardingSphere-Proxy                                                       |
| proxy_execute_errors_total        | COUNTER   | Total executor errors of ShardingSphere-Proxy                                                                  |
