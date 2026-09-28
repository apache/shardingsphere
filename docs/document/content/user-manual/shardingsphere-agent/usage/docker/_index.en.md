+++
title = "Docker"
weight = 5
+++

## Local Build

ShardingSphere Agent has a `Dockerfile` available for easy distribution. You can execute the following command to build a Docker Image,

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/agent -P-dev,release,all,docker -T1C -DskipTests clean package
```

If you add the following statement in your custom `Dockerfile`, it will copy the ShardingSphere Agent directory to `/shardingsphere-agent/`.

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:latest /usr/agent/ /shardingsphere-agent/
```

## Community Build

Since ShardingSphere 5.5.2, ShardingSphere Agent has released community builds at https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-agent .
This Docker Image is not part of the ASF distribution, but is provided for convenience.

If you add the following statement in a custom `Dockerfile`, it will copy the ShardingSphere Agent directory to `/shardingsphere-agent/`.

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:5.5.2 /usr/agent/ /shardingsphere-agent/
```

## Nightly Build

ShardingSphere Agent has a nightly built Docker Image at https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-agent .

If you add the following statement in your custom `Dockerfile`, it will copy the ShardingSphere Agent directory to `/shardingsphere-agent/`.

```dockerfile
COPY --from=ghcr.io/apache/shardingsphere-agent:latest /usr/agent/ /shardingsphere-agent/
```

## Using Dockerfile

Introduce a typical scenario,

1. Assume that the tracing backend Docker Container is deployed through the following Bash command.
   Create the network first,

```shell
docker network create example-net
```

   Use Jaeger to receive OTLP over gRPC,

```shell
docker run --rm -d \
  --name jaeger \
  --network example-net \
  jaegertracing/all-in-one:1.62.0
```

   Or use Zipkin. The upstream `openzipkin/zipkin` image does not receive OTLP, so use `ghcr.io/openzipkin-contrib/zipkin-otel`, which adds an OTLP/HTTP receiver on the Zipkin server port 9411,

```shell
docker run --rm -d \
  --name zipkin \
  -p 9411:9411 \
  --network example-net \
  ghcr.io/openzipkin-contrib/zipkin-otel:0.3.0
```

   `GET http://localhost:9411/health` reports `zipkin.details.OpenTelemetryHttpCollector{}` as `UP` when Zipkin is ready.

2. Assume `./custom-agent.yaml` contains the configuration of ShardingSphere Agent, and the content may be as follows.
   For the Jaeger backend,

```yaml
plugins:
  tracing:
    OpenTelemetry:
      props:
        otel.service.name: "example"
        otel.exporter.otlp.traces.endpoint: "http://jaeger:4317"
```

   For the Zipkin backend. Zipkin serves OTLP over HTTP only, so the protocol must be set explicitly,

```yaml
plugins:
  tracing:
    OpenTelemetry:
      props:
        otel.service.name: "example"
        otel.exporter.otlp.protocol: "http/protobuf"
        otel.exporter.otlp.traces.endpoint: "http://zipkin:9411/v1/traces"
```

   `otel.exporter.otlp.traces.endpoint` is used exactly as written. Setting it to `http://zipkin:9411` makes the exporter post to `/` and Zipkin drops the spans. `otel.exporter.otlp.endpoint` is a base URL, to which the SDK appends `/v1/traces`.

3. Assuming `./target/example.jar` is an Uber JAR of Spring Boot that will use ShardingSphere Agent,
   you can use the ShardingSphere Agent in the nightly built Docker Image for a JAR like `example.jar` through a `Dockerfile` like the following.

```dockerfile
FROM ghcr.io/apache/shardingsphere-agent:latest
COPY ./target/example.jar /app.jar
COPY ./custom-agent.yaml /usr/agent/conf/agent.yaml
ENTRYPOINT ["java","-javaagent:/usr/agent/shardingsphere-agent.jar","-jar","/app.jar"]
```

If you build the Docker Image of `ghcr.io/apache/shardingsphere-agent:latest` locally, the `Dockerfile` may be as follows,

```dockerfile
FROM ghcr.io/apache/shardingsphere-agent:latest
COPY ./target/example.jar /app.jar
COPY ./custom-agent.yaml /usr/agent/conf/agent.yaml
ENTRYPOINT ["java","-javaagent:/usr/agent/shardingsphere-agent.jar","-jar","/app.jar"]
```

4. Enjoy it,

```shell
docker build -t example/gs-spring-boot-docker:latest .
docker run --network example-net example/gs-spring-boot-docker:latest
```
