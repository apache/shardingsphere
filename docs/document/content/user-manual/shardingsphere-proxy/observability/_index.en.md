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
