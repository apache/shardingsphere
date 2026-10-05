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

1. 假设通过如下的 Bash 命令部署了 tracing 后端的 Docker Container。
   首先创建网络，

```shell
docker network create example-net
```

   使用 Jaeger 通过 gRPC 接收 OTLP，

```shell
docker run --rm -d \
  --name jaeger \
  --network example-net \
  jaegertracing/all-in-one:1.62.0
```

   或使用 Zipkin。上游的 `openzipkin/zipkin` 镜像不接收 OTLP，需要改用 `ghcr.io/openzipkin-contrib/zipkin-otel`，它在 Zipkin 服务端口 9411 上提供了 OTLP/HTTP 接收端，

```shell
docker run --rm -d \
  --name zipkin \
  -p 9411:9411 \
  --network example-net \
  ghcr.io/openzipkin-contrib/zipkin-otel:0.3.0
```

   当 Zipkin 就绪时，`GET http://localhost:9411/health` 会返回 `zipkin.details.OpenTelemetryHttpCollector{}` 为 `UP`。

2. 假设 `./custom-agent.yaml` 包含 ShardingSphere Agent 的配置，内容可能如下。
   使用 Jaeger 后端时，

```yaml
plugins:
  tracing:
    OpenTelemetry:
      props:
        otel.service.name: "example"
        otel.exporter.otlp.traces.endpoint: "http://jaeger:4317"
```

   使用 Zipkin 后端时。Zipkin 仅通过 HTTP 提供 OTLP，因此需要显式设置协议，

```yaml
plugins:
  tracing:
    OpenTelemetry:
      props:
        otel.service.name: "example"
        otel.exporter.otlp.protocol: "http/protobuf"
        otel.exporter.otlp.traces.endpoint: "http://zipkin:9411/v1/traces"
```

   `otel.exporter.otlp.traces.endpoint` 会按字面值原样使用。如果将其设置为 `http://zipkin:9411`，导出器会向 `/` 发送请求，Zipkin 会丢弃这些 span。`otel.exporter.otlp.endpoint` 是基础 URL，SDK 会在其后追加 `/v1/traces`。

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
