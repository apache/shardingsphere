+++
title = "Docker"
weight = 5
+++



##  本地构建

ShardingSphere Agent 存在可用的 `Dockerfile` 用于方便分发。可执行如下命令以构建 Docker Image，

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/agent -P-dev,release,all,docker -T1C -DskipTests clean package
```

此后若在自定义 `Dockerfile` 中添加以下语句，这会将 ShardingSphere Agent 的目录复制到 `/shardingsphere-agent/` 。

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:latest /usr/agent/ /shardingsphere-agent/
```

## 社区构建

自 ShardingSphere 5.5.2 开始，ShardingSphere Agent 在 https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-agent 发布社区构建。
此 Docker Image 不属于 ASF 分发产物之一，只是为了方便而提供。

若在自定义 `Dockerfile` 中添加以下语句，这会将 ShardingSphere Agent 的目录复制到 `/shardingsphere-agent/` 。

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:5.5.2 /usr/agent/ /shardingsphere-agent/
```

## 夜间构建

ShardingSphere Agent 在 https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-agent 存在夜间构建的 Docker Image。

若在自定义 `Dockerfile` 中添加以下语句，这会将 ShardingSphere Agent 的目录复制到 `/shardingsphere-agent/` 。

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:latest /usr/agent/ /shardingsphere-agent/
```

## 通过 Dockerfile 使用

引入一个典型场景，

1. 假设通过如下的 Bash 命令部署了 Jaeger All in One 的 Docker Container,

```shell
docker network create example-net
docker run --rm -d \
  --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  --network example-net \
  jaegertracing/all-in-one:1.62.0
```

2. 假设 `./custom-agent.yaml` 包含 ShardingSphere Agent 的配置，内容可能如下，

```yaml
plugins:
  tracing:
    OpenTelemetry:
      props:
        otel.service.name: "example"
        otel.exporter.otlp.traces.endpoint: "http://jaeger:4318"
```

3. 假设`./target/example.jar` 是一个即将使用 ShardingSphere Agent 的 Spring Boot 的 Uber JAR，
   可通过类似如下的 `Dockerfile` 来为类似 `example.jar` 的 JAR 使用夜间构建的 Docker Image 中的 ShardingSphere Agent。

```dockerfile
FROM ghcr.io/apache/shardingsphere-agent:latest
COPY ./target/example.jar /app.jar
COPY ./custom-agent.yaml /usr/agent/conf/agent.yaml
ENTRYPOINT ["java","-javaagent:/usr/agent/shardingsphere-agent.jar","-jar","/app.jar"]
```

如果是通过本地构建 `ghcr.io/apache/shardingsphere-agent:latest` 的 Docker Image，`Dockerfile` 可能如下，

```dockerfile
FROM ghcr.io/apache/shardingsphere-agent:latest
COPY ./target/example.jar /app.jar
COPY ./custom-agent.yaml /usr/agent/conf/agent.yaml
ENTRYPOINT ["java","-javaagent:/usr/agent/shardingsphere-agent.jar","-jar","/app.jar"]
```

4. 享受它，

```shell
docker build -t example/gs-spring-boot-docker:latest .
docker run --network example-net example/gs-spring-boot-docker:latest
```
