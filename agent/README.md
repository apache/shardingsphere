# ShardingSphere Agent

ShardingSphere-Agent module provides an observable framework for ShardingSphere, which is implemented based on Java Agent.

## Features

### Logging

The logging plugin uses to record logs of ShardingSphere.
Supports for File.

### Metrics

The metrics plugin uses to collect and expose monitoring metrics.
Supports for prometheus.

### Tracing

The tracing plugin uses to obtain the link trace information of SQL parsing and SQL execution.
Supports for Jaeger, OpenTelemetry, OpenTracing and Zipkin.

## How To Build

```shell
git clone https://github.com/apache/shardingsphere.git
cd shardingsphere
./mvnw clean install -Prelease
```

Artifact is `agent/distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz`

## Quick Start

* `shardingsphere-agent-{latest.release.version}.jar` is the agent startup jar.
* `conf/agent.yaml` is used to configure and enable plugins.

### Quick Start with Proxy

Add the javaagent configuration in the Proxy startup script, as follows:

```shell
nohup $JAVA ${JAVA_OPTS} ${JAVA_MEM_OPTS} -javaagent:/xx/xx/shardingsphere-agent-{latest.release.version}.jar -classpath ${CLASS_PATH} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
```

### Quick Start with JDBC

When using ShardingSphere-JDBC, you need to add javaagent configuration in the startup command, as follows:

```shell
java -javaagent:/xx/xx/shardingsphere-agent-{latest.release.version}.jar -jar project-using-shardingsphere-jdbc.jar
```

## Install Customized Plugins

### Usage of lib and plugins folders

* `lib` contains dependencies common to plugins.
* `plugins` contains all plugins.

### Extend via SPI PluginBootService

`PluginBootService` is the plugin service definition interface, used to start the plugin service.
Custom plugins need to implement this interface.

### Configure advisors.yaml

`advisors.yaml` is used to define interception points. The format is as follows:

```yaml
advisors:
  - target: org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory # class that need interception enhancements
    advice: org.apache.shardingsphere.agent.plugin.metrics.core.advice.MetaDataContextsFactoryAdvice # enhanced class
    pointcuts: # intercept methods
      - name: create # method name
        type: method # intercept type. configuring "method" when intercepting the method, configuring "constructor" when intercepting the constructor
        params: # method parameters, just satisfy the unique identification method
          - index: 3 # parameter index
            type: java.util.Map # parameter type
```

The file location is as follows:

* Proxy: META-INF/conf/{plugin-type}-proxy-advisors.yaml
* JDBC:  META-INF/conf/{plugin-type}-jdbc-advisors.yaml

### Configure more metrics collector into metrics.yaml

`metrics.yaml` is the metrics definition file. The format is as follows.

```yaml
metrics:
  - id: proxy_request_total # metric id
    type: COUNTER # metric type, COUNTER、GAUGE、HISTOGRAM、SUMMARY
    name: proxy_request_total # metric name
    help: the shardingsphere proxy request total # metric help
```
