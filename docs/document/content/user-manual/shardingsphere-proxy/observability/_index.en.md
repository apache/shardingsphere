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
Built-in plugins include Jaeger, OpenTracing, Zipkin, OpenTelemetry, BaseLogging and Prometheus.
No plugin is enabled by default.

```yaml
plugins:
#  logging:
#    BaseLogging:
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
#      port: 5775
#      props:
#        service-name: "shardingsphere"
#        jaeger-sampler-type: "const"
#        jaeger-sampler-param: "1"
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

| Name                              | Description                         |  Value range    |  Default value     |
|:----------------------------------|:------------------------------------|:--------- | :-------- |
| jvm-information-collector-enabled | Start JVM collector                 |  true, false  |  true  |
| service-name                      | Tracking service name               | Custom | shardingsphere-agent |
| jaeger-sampler-type               | Jaeger sample rate type             | const, probabilistic, ratelimiting, remote | const |
| jaeger-sampler-param              | Jaeger sample rate parameter        |const:0, 1, probabilistic:0.0 - 1.0, ratelimiting: > 0, Customize the number of acquisitions per second, remote：need to customize the remote service addres,JAEGER_SAMPLER_MANAGER_HOST_PORT | 1 (const type) |
| url-version                       | Zipkin url address                  | Custom | /api/v2/spans                    |
| sampler-type                      | Zipkin sample rate type             | const, counting, ratelimiting, boundary | const |
| sampler-param                     | Zipkin sampling rate parameter      |const:0, 1, counting:0.01 - 1.0, ratelimiting: > 0, boundary:0.0001 - 1.0 | 1 (const type) |
| otel-resource-attributes          | opentelemetry properties            | String key value pair (, split) | service.name=shardingsphere-agent |
| otel-traces-exporter              | Tracing expoter                     | zipkin, jaeger | zipkin |
| otel-traces-sampler               | Opentelemetry sample rate type      | always_on, always_off, traceidratio | always_on |
| otel-traces-sampler-arg           | Opentelemetry sample rate parameter | traceidratio：0.0 - 1.0 | 1.0 |

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


## Metrics
| name                             | type       | description                                                                                              |
|:---------------------------------|:-----------|:---------------------------------------------------------------------------------------------------------|
| proxy_request_total              | COUNTER    | proxy request total                                                                                      |
| proxy_connection_total           | GAUGE      | proxy connection total                                                                                   |
| proxy_execute_latency_millis     | HISTOGRAM  | proxy executor latency millis                                                                            |
| proxy_execute_error_total        | COUNTER    | proxy executor error total                                                                               |
| route_sql_select_total           | COUNTER    | proxy executor route select sql total                                                                    |
| route_sql_insert_total           | COUNTER    | proxy executor route insert sql total                                                                    |
| route_sql_update_total           | COUNTER    | proxy executor route update sql total                                                                    |
| route_sql_delete_total           | COUNTER    | proxy executor route delete sql total                                                                    |
| route_datasource_total           | COUNTER    | number of datasource routed                                                                              |
| route_table_total                | COUNTER    | number of table routed                                                                                   |
| proxy_transaction_commit_total   | COUNTER    | transaction commit count total                                                                           |
| proxy_transaction_rollback_total | COUNTER    | transaction rollback count total                                                                         |
| parse_sql_dml_insert_total       | COUNTER    | proxy executor parse insert sql total                                                                    |
| parse_sql_dml_delete_total       | COUNTER    | proxy executor parse delete sql total                                                                    |
| parse_sql_dml_update_total       | COUNTER    | proxy executor parse update sql total                                                                    |
| parse_sql_dml_select_total       | COUNTER    | proxy executor parse select sql total                                                                    |
| parse_sql_ddl_total              | COUNTER    | proxy executor parse ddl sql total                                                                       |
| parse_sql_dcl_total              | COUNTER    | proxy executor parse dcl sql total                                                                       |
| parse_sql_dal_total              | COUNTER    | proxy executor parse dal sql total                                                                       |
| parse_sql_tcl_total              | COUNTER    | proxy executor parse tcl sql total                                                                       |
| parse_dist_sql_rql_total         | COUNTER    | proxy executor parse rql sql total                                                                       |
| parse_dist_sql_rdl_total         | COUNTER    | proxy executor parse rdl sql total                                                                       |
| parse_dist_sql_ral_total         | COUNTER    | proxy executor parse ral sql total                                                                       |
| build_info                       | GAUGE      | build information                                                                                        |
| proxy_info                       | GAUGE      | proxy information， state:1 OK， state:2 CIRCUIT BREAK                                                     |
| meta_data_info                   | GAUGE      | meta data information， schema_count:logic number of databases， database_count:actual number of databases |