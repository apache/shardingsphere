+++
title = "构建 GraalVM Native Image(Alpha)"
weight = 2
+++

## 背景信息

本节主要介绍如何通过 `GraalVM` 的 `native-image` 组件构建 ShardingSphere-Proxy 的 `Native Image` 和对应的 `Docker Image`
。

## 注意事项

- ShardingSphere Proxy 尚未准备好与 GraalVM Native Image 集成。
  其在 https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-proxy-native 存在每夜构建。
  假设存在包含`server.yaml` 的 `conf` 文件夹为 `./custom/conf`，你可通过如下的 `docker-compose.yml` 文件进行测试。

```yaml
version: "3.8"

services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:latest
    volumes:
      - ./custom/conf:/conf
    ports:
      - "3307:3307"
```

- 若你发现构建过程存在缺失的 GraalVM Reachability Metadata,
  应当在 https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue ，
  并提交包含 ShardingSphere 自身或依赖的第三方库缺失的 GraalVM Reachability Metadata 的 PR。

- ShardingSphere 的 master 分支尚未准备好处理 Native Image 中的单元测试,
  需要等待 Junit 5 Platform 的集成，你总是需要在构建 GraalVM Native Image 的过程中，
  加上特定于 `GraalVM Native Build Tools` 的 `-DskipNativeTests` 或 `-DskipTests` 参数跳过 Native Image 中的单元测试。

- 本节假定处于 Linux（amd64，aarch64）， MacOS（amd64）或 Windows（amd64）环境。
  如果你位于 MacOS（aarch64/M1） 环境，你需要关注尚未关闭的 https://github.com/oracle/graal/issues/2666。

## 前提条件

1. 根据 https://www.graalvm.org/downloads/ 要求安装和配置 JDK 17 对应的 `GraalVM CE` 或 `GraalVM EE`。
   同时可以通过 `SDKMAN!` 安装 JDK 17 对应的 `GraalVM CE`。

2. 通过 `GraalVM Updater` 工具安装 `native-image` 组件。

3. 根据 https://www.graalvm.org/22.2/reference-manual/native-image/#prerequisites 的要求安装本地工具链。

4. 如果需要构建 Docker Image， 确保 `docker-cli` 在系统环境变量内。

## 操作步骤

1. 获取 Apache ShardingSphere Git Source

- 在[下载页面](https://shardingsphere.apache.org/document/current/en/downloads/)
  或 https://github.com/apache/shardingsphere/tree/master 获取。

2. 在命令行构建产物, 分两种情形。

- 情形一：不需要使用存在 SPI 实现的 JAR 或第三方依赖的 JAR

- 在 Git Source 同级目录下执行如下命令, 直接完成 Native Image 的构建。

```bash
./mvnw -am -pl distribution/proxy-native -B -Pnative -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
```

- 情形二：需要使用存在 SPI 实现的 JAR 或 GPL V2 等 LICENSE 的第三方依赖的 JAR。

- 在 `distribution/proxy-native/pom.xml` 的 `dependencies` 加入存在 SPI 实现的 JAR
  或第三方依赖的 JAR。示例如下

```xml

<dependencies>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.0.31</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-sql-translator-jooq-provider</artifactId>
        <version>5.2.0</version>
    </dependency>
</dependencies>
```

- 通过命令行构建 GraalVM Native Image。

```bash
./mvnw -am -pl distribution/proxy-native -B -Pnative -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
```

3. 通过命令行启动 Native Image, 需要带上两个参数，
   第一个参数为 ShardingSphere Proxy 使用的端口，第二个参数为你编写的包含 `server.yaml` 的 `/conf` 文件夹，
   假设已存在文件夹`./custom/conf`，示例为

```bash
./apache-shardingsphere-proxy-native 3307 ./custom/conf
```

4. 如果需要构建 Docker Image, 在添加后存在 SPI 实现的依赖或第三方依赖后, 在命令行执行如下命令。

```shell
./mvnw -am -pl distribution/proxy-native -B -Pnative,docker.native -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
```

- 假设存在包含`server.yaml` 的 `conf` 文件夹为 `./custom/conf`，可通过如下的 `docker-compose.yml` 文件启动 GraalVM Native
  Image 对应的 Docker Image。

```yaml
version: "3.8"

services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:latest
    volumes:
      - ./custom/conf:/conf
    ports:
      - "3307:3307"
```

- 如果你不对 Git Source 做任何更改， 上文提及的命令将使用 `oraclelinux:9-slim` 作为 Base Docker Image。
  但如果你希望使用 `busybox:glic`，`gcr.io/distroless/base` 或 `scratch` 等更小体积的 Docker Image 作为 Base Docker
  Image，你需要根据 https://www.graalvm.org/22.2/reference-manual/native-image/guides/build-static-executables/ 的要求，
  做为 `pom.xml`的 `native profile` 添加 `-H:+StaticExecutableWithDynamicLibC` 的 `jvmArgs` 等操作。
  另请注意，某些第三方依赖将需要在 `Dockerfile` 安装更多系统库，例如 `libdl`。
  因此请确保根据你的使用情况调整 `distribution/proxy-native`
  下的 `pom.xml` 和 `Dockerfile` 的内容。

# 可观察性

- 针对 GraalVM Native Image 形态的 ShardingSphere
  Proxy，其提供的可观察性的能力与 https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-proxy/observability/
  并不一致。

- 你可以使用 https://www.graalvm.org/22.2/tools/ 提供的一系列命令行工具或可视化工具观察 GraalVM Native Image
  的内部行为，并根据其要求使用 VSCode 完成调试工作。
  如果你正在使用 IntelliJ IDEA 并且希望调试生成的 GraalVM Native Image，
  你可以关注 https://blog.jetbrains.com/idea/2022/06/intellij-idea-2022-2-eap-5/#Experimental_GraalVM_Native_Debugger_for_Java
  及其后继。

- 对于使用 `ShardingSphere Agent` 等 APM Java Agent 的情形，
  GraalVM 的 `native-image` 组件尚未完全支持在构建 Native Image 时使用
  javaagent，你需要关注尚未关闭的 https://github.com/oracle/graal/issues/1065。

- 以下部分采用 `Apache SkyWalking Java Agent` 作为示例，可用于跟踪 GraalVM 社区的对应 issue。

1. 下载 https://dlcdn.apache.org/skywalking/java-agent/8.12.0/apache-skywalking-java-agent-8.12.0.tgz ，
   并解压到 ShardingSphere Git Source 的 `distribution/proxy-native`。

2. 修改 `distribution/proxy-native/pom.xml` 的 `native profile`，
   为 `org.graalvm.buildtools:native-maven-plugin` 的 `configuration` 添加如下的 `jvmArgs`。

```xml

<jvmArgs>
    <arg>-Dskywalking.agent.service_name="your service name"</arg>
    <arg>-Dskywalking.collector.backend_service="your skywalking oap ip and port"</arg>
    <arg>-javaagent:./skywalking-agent/skywalking-agent.jar</arg>
</jvmArgs>
```

3. 通过命令行构建 GraalVM Native Image。

```bash
./mvnw -am -pl distribution/proxy-native -B -Pnative -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
```