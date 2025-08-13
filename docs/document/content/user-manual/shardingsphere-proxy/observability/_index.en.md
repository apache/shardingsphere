+++
title = "Observability"
weight = 5
+++

## Agent

### Compile source code

Download Apache ShardingSphere from GitHub,Then compile.

```shell
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -DskipITs -DskipTests -P-dev,release,all
```

Agent artifact is `distribution/agent/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz`

Proxy artifact is `distribution/proxy/target/apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz`

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

## Usage

Start ShardingSphere-Proxy

```shell
tar -zxvf apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin.tar.gz
cd apache-shardingsphere-${latest.release.version}-shardingsphere-proxy-bin
./bin/start.sh -g
```

After startup, you can find the plugin info in the log of ShardingSphere-Proxy, `Metric` and `Tracing` data can be viewed through the configured monitoring address.

## Metrics

| Name                         | Type      | Description                                                                                                                               |
|:-----------------------------|:----------|:------------------------------------------------------------------------------------------------------------------------------------------|
| build_info                   | GAUGE     | Build information                                                                                                                         |
| parsed_sql_total             | COUNTER   | Total count of parsed by type (INSERT, UPDATE, DELETE, SELECT, DDL, DCL, DAL, TCL, RQL, RDL, RAL, RUL)                                    |
| routed_sql_total             | COUNTER   | Total count of routed by type (INSERT, UPDATE, DELETE, SELECT)                                                                            |
| routed_result_total          | COUNTER   | Total count of routed result (data source routed, table routed)                                                                           |
| proxy_state                  | GAUGE     | Status information of ShardingSphere-Proxy. 0 is OK; 1 is CIRCUIT BREAK; 2 is LOCK                                                        |
| proxy_meta_data_info         | GAUGE     | Meta data information of ShardingSphere-Proxy. database_count is logic number of databases; storage_unit_count is number of storage units |
| proxy_current_connections    | GAUGE     | Current connections of ShardingSphere-Proxy                                                                                               |
| proxy_requests_total         | COUNTER   | Total requests of ShardingSphere-Proxy                                                                                                    |
| proxy_transactions_total     | COUNTER   | Total transactions of ShardingSphere-Proxy, classify by commit, rollback                                                                  |
| proxy_execute_latency_millis | HISTOGRAM | Execute latency millis histogram of ShardingSphere-Proxy                                                                                  |
| proxy_execute_errors_total   | COUNTER   | Total executor errors of ShardingSphere-Proxy                                                                                             |
