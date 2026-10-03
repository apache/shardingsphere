+++
title = "Agent"
weight = 4
pre = "<b>10.4. </b>"
+++

使用 JDK 21 或更高版本，在源码仓库根目录执行以下命令。

## 打包

```bash
./mvnw -B -pl distribution/agent -am \
  '-P<profiles>' \
  -Dmaven.test.skip=true package
```

## Profile 选择

按所需制品，将 `<profiles>` 替换为下表中的值。

| Profile          | 生成的制品                    |
|------------------|--------------------------|
| `release`        | 二进制发行包                   |
| `release,docker` | 发行包及 Docker 镜像，需要 Docker |

## 制品

`distribution/agent/target/apache-shardingsphere-<version>-shardingsphere-agent-bin.tar.gz`

`<version>` 为所构建源码的项目版本。

发行包包含 Agent JAR、配置和插件目录，内置 Prometheus、文件日志及 OpenTelemetry 插件。
