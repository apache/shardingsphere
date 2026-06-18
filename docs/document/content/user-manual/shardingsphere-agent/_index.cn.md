+++
pre = "<b>4.6. </b>"
title = "ShardingSphere-Agent"
weight = 6
chapter = true
+++

该代理程序有助于收集可观测性指标，提供日志记录支持，并与流行的跟踪和监控解决方案集成，例如 JVM 指标、Prometheus 和 OpenTelemetry。

## 源码编译

从 Github 下载 Apache ShardingSphere 源码，对源码进行编译，操作命令如下。

```shell
git clone --depth 1 https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -DskipITs -DskipTests -P-dev,release,all
```
Agent 制品 `distribution/agent/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz`

## 目录说明

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
