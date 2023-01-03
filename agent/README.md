
##  Features Introduction

ShardingSphere-Agent module provides an observable framework for ShardingSphere, which is implemented based on Java Agent.

### Usage of logging
The default logging plugin shows how to record additional logs in ShardingSphere.

### Usage of metrics
The metrics plugin is used to collect and display statistical indicators for the entire cluster. Supports Prometheus by default.

### Usage of tracing
The tracing plugin is used to obtain the link trace information of SQL parsing and SQL execution. Supports for Jaeger, OpenTelemetry, OpenTracing and Zipkin.

## How To Build
```shell
git clone https://github.com/apache/shardingsphere.git

cd shardingsphere

./mvnw clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Drat.skip=true -Djacoco.skip=true -DskipITs -DskipTests -Prelease
```
Artifact:
```shell
agent/distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-agent-bin.tar.gz # Binary package of ShardingSphere-Agent
```


## Quick Start

* `shardingsphere-agent-{latest.release.version}.jar` is the agent startup jar.
* `conf/agent.yaml` is used to configure and enable plugins.

### Quick Start with Proxy

Add the javaagent configuration in the Proxy startup script, as follows.

```shell
nohup $JAVA ${JAVA_OPTS} ${JAVA_MEM_OPTS} -javaagent:/xx/xx/shardingsphere-agent-{latest.release.version}.jar -classpath ${CLASS_PATH} ${MAIN_CLASS} >> ${STDOUT_FILE} 2>&1 &
```

### Quick Start with JDBC
When using ShardingSphere-JDBC, you need to add javaagent configuration in the startup command, as follows.
```shell
java -javaagent:/xx/xx/shardingsphere-agent-{latest.release.version}.jar -jar project-using-shardingsphere-jdbc.jar
```

## Install Customized Plugins

### Usage of lib and plugins folders
* `lib` contains dependencies common to plugins.
* `plugins` contains all plugins.

### Extend via SPI PluginBootService
`PluginBootService` is the plugin service definition interface, used to start the plugin service. Custom plugins need to implement this interface.

### Configure advisors.yaml
`advisors.yaml` is used to define interception points. The file location is as follows.
* Proxy: META-INF/conf/{plugin-type}-proxy-advisors.yaml
* JDBC:  META-INF/conf/{plugin-type}-jdbc-advisors.yaml

### Configure more metrics collector into metrics.yaml
`metrics.yaml` is the metrics definition file.


