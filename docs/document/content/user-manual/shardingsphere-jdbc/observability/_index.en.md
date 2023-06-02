+++
title = "Observability"
weight = 7
+++

## Agent

### Compile source code

Download Apache ShardingSphere from GitHub,Then compile.

```shell
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```

Artifact is distribution/agent/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz

### Directory structure

Create agent directory, and unzip agent distribution package to the directory.

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
Agent log output location is `agent/logs/stdout.log`.

### Configuration

`conf/agent.yaml` is used to manage agent configuration.
Built-in plugins include File, Prometheus, OpenTelemetry.

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

### Plugin description

#### File

Currently, the File plugin only outputs the time-consuming log output of building metadata, and has no other log output for the time being.

#### Prometheus

Used for exposure monitoring metrics.

* Parameter description

| Name                              | Description                                  |
|-----------------------------------|----------------------------------------------|
| host                              | host IP                                      |
| port                              | port                                         |
| jvm-information-collector-enabled | whether to collect JVM indicator information |

#### OpenTelemetry

OpenTelemetry can export tracing data to Jaeger, Zipkin.

* Parameter description

| Name                               | Description     |
|------------------------------------|-----------------|
| otel.service.name                  | service name    |
| otel.traces.exporter               | traces exporter |
| otel.exporter.otlp.traces.endpoint | traces endpoint |
| otel.traces.sampler                | traces sampler  |

Parameter reference [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure)

## Usage in ShardingSphere-JDBC

+ 1 The `SpringBoot` project ready to integrate `ShardingSphere-JDBC`, test-project.jar
+ 2 Startup project
```shell
java -javaagent:/agent/shardingsphere-agent-${latest.release.version}.jar -jar test-project.jar
```
+ 3 Access to started service
+ 4 Check whether the corresponding plug-in is effective

## Metrics

| Name                                  | Type      | Description                                                                                            |
|:--------------------------------------|:----------|:-------------------------------------------------------------------------------------------------------|
| build_info                            | GAUGE     | Build information                                                                                      |
| parsed_sql_total                      | COUNTER   | Total count of parsed by type (INSERT, UPDATE, DELETE, SELECT, DDL, DCL, DAL, TCL, RQL, RDL, RAL, RUL) |
| routed_sql_total                      | COUNTER   | Total count of routed by type (INSERT, UPDATE, DELETE, SELECT)                                         |
| routed_result_total                   | COUNTER   | Total count of routed result (data source routed, table routed)                                        |
| jdbc_state                            | GAUGE     | Status information of ShardingSphere-JDBC. 0 is OK; 1 is CIRCUIT BREAK; 2 is LOCK                      |
| jdbc_meta_data_info                   | GAUGE     | Meta data information of ShardingSphere-JDBC                                                           |
| jdbc_statement_execute_total          | GAUGE     | Total number of statements executed                                                                    |
| jdbc_statement_execute_errors_total   | GAUGE     | Total number of statement execution errors                                                             |
| jdbc_statement_execute_latency_millis | HISTOGRAM | Statement execution latency                                                                            |
| jdbc_transactions_total               | GAUGE     | Total number of transactions, classify by commit and rollback                                          |
