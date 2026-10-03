+++
title = "Agent"
weight = 4
pre = "<b>10.4. </b>"
+++

Use JDK 21 or later and run the following command from the source repository root.

## Packaging

```bash
./mvnw -B -pl distribution/agent -am \
  '-P<profiles>' \
  -Dmaven.test.skip=true package
```

## Profile Selection

Replace `<profiles>` with a value from the table below for the required artifacts.

| Profile          | Generated artifact                             |
|------------------|------------------------------------------------|
| `release`        | Binary distribution archive                    |
| `release,docker` | Distribution and Docker image; requires Docker |

## Artifacts

`distribution/agent/target/apache-shardingsphere-<version>-shardingsphere-agent-bin.tar.gz`

`<version>` is the project version of the source being built.

The archive contains the Agent JAR, configuration, and a plugin directory with Prometheus, file logging, and OpenTelemetry plugins.
