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

1. Assume that the Jaeger All in One Docker Container is deployed through the following Bash command,

```shell
docker network create example-net
docker run --rm -d \
  --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  --network example-net \
  jaegertracing/all-in-one:1.62.0
```

2. Assume `./custom-agent.yaml` contains the configuration of ShardingSphere Agent, and the content may be as follows,

```yaml
plugins:
  tracing:
    OpenTelemetry:
      props:
        otel.service.name: "example"
        otel.exporter.otlp.traces.endpoint: "http://jaeger:4318"
```

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
