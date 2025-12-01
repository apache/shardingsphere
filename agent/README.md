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
Supports for OpenTelemetry.

## How To Build

```shell
git clone https://github.com/apache/shardingsphere.git
cd shardingsphere
./mvnw clean install -P-dev,release,all
```

Artifact is `distribution/agent/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz`

## Quick Start

* `shardingsphere-agent-{latest.release.version}.jar` is the agent startup jar.
* `conf/agent.yaml` is used to configure and enable plugins.

### Quick Start with Proxy

Add the agent to the Proxy directory, as follows:

```shell
apache-shardingsphere-{latest.release.version}-shardingsphere-proxy-bin/agent/shardingsphere-agent.jar
```

Start agent with `-g` option by bin/start.sh
```shell
sh start.sh -g

# following command also works
sh start.sh --agent
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

### Extend via SPI PluginLifecycleService

`PluginLifecycleService` is the plugin service definition interface, used to start the plugin service.
Custom plugins need to implement this interface.

### Configure advisors.yaml

`advisors.yaml` is used to define interception points. The format is as follows:

```yaml
advisors:
  - target: xxx.TargetClass # class that need interception enhancements
    advice: xxx.XXXAdvice # enhanced class
    pointcuts: # intercept methods
      - name: foo # method name
        type: method # intercept type. configuring "method" when intercepting the method, configuring "constructor" when intercepting the constructor
        params: # method parameters, just satisfy the unique identification method
          - index: 0 # parameter index
            type: java.lang.String # parameter type
```

The file location is as follows:

* Proxy: META-INF/conf/{plugin-type}-proxy-advisors.yaml
* JDBC:  META-INF/conf/{plugin-type}-jdbc-advisors.yaml

### Configure more metrics collector into metrics.yaml

`metrics.yaml` is the metrics definition file. The format is as follows.

```yaml
metrics:
  - id: xxx_id # metric id
    type: COUNTER # metric type, COUNTER、GAUGE、HISTOGRAM、SUMMARY
    name: xxx_name # metric name
    help: xxx help # metric help
```
