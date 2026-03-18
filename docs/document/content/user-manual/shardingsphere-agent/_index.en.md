+++
pre = "<b>4.6. </b>"
title = "ShardingSphere-Agent"
weight = 6
chapter = true
+++

The agent helps collect observability metrics, provides support for logging, and integrates with popular tracing and monitoring solutions like JVM metrics, Prometheus, and OpenTelemetry.

## Compile source code

Download Apache ShardingSphere from GitHub, then compile.

```shell
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -DskipITs -DskipTests -P-dev,release,all
```

Agent artifact is `distribution/agent/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz`

## Directory structure

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
