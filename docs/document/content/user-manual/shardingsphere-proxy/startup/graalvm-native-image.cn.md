+++
title = "构建 GraalVM Native Image(Alpha)"
weight = 2
+++

## 背景信息

本节主要介绍如何通过 `GraalVM` 的 `native-image` 组件构建 ShardingSphere-Proxy 的 `Native Image` 和对应的 `Docker Image`
。

## 注意事项

- ShardingSphere Proxy 尚未准备好与 GraalVM Native Image 集成。
  其在 https://github.com/apache/shardingsphere/actions/ 存在每日构建的任务用于测试构建。

- 若你发现构建过程存在缺失的 GraalVM Reachability Metadata,
  应当在 https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue ，
  并提交包含 ShardingSphere 自身或依赖的第三方库缺失的 GraalVM Reachability Metadata 的 PR。

- ShardingSphere 的 master 分支尚未准备好处理 Native Image 中的单元测试,
  需要等待 Junit 5 Platform 的集成，你总是需要在构建 GraalVM Native Image 的过程中，
  加上特定于 `GraalVM Native Build Tools` 的 `-DskipNativeTests` 或 `-DskipTests` 参数跳过 Native Image 中的单元测试。

- 本节假定处于 Linux（amd64，aarch64）， MacOS（ amd64 ）或 Windows（amd64）环境。
  如果你位于 MacOS(aarch64/M1) 环境, 你需要关注尚未关闭的 https://github.com/oracle/graal/issues/2666 。

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
./mvnw -am -pl shardingsphere-distribution/shardingsphere-proxy-distribution -B -Pnative -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
```

- 情形二：需要使用存在 SPI 实现的 JAR 或 GPL V2 等 LICENSE 的第三方依赖的 JAR。

- 在 `shardingsphere-distribution/shardingsphere-proxy-distribution/pom.xml` 的 `dependencies` 加入存在 SPI 实现的 JAR
  或第三方依赖的 JAR。示例如下

```xml

<dependencies>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.30</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-sql-translator-jooq-provider</artifactId>
        <version>5.2.0</version>
    </dependency>
</dependencies>
```

- 在 Git Source 同级目录下执行如下命令。

```bash
./mvnw -am -pl shardingsphere-distribution/shardingsphere-proxy-distribution -B -Pnative -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
```

- 通过命令行构建 GraalVM Native Image。

```bash
./mvnw org.graalvm.buildtools:native-maven-plugin:compile-no-fork -am -pl shardingsphere-distribution/shardingsphere-proxy-distribution -Pnative -DskipTests
```

3. 通过命令行启动 Native Image, 需要带上两个参数，
   第一个参数为 ShardingSphere Proxy 使用的端口，第二个参数为你编写的包含 `server.yaml` 的 `/conf` 文件夹，
   假设已存在文件夹`./custom/conf`，示例为

```bash
./apache-shardingsphere-proxy 3307 ./custom/conf
```

4. 如果需要构建 Docker Image, 在添加后存在 SPI 实现的依赖或第三方依赖后, 在命令行执行如下命令。

```shell
./mvnw -am -pl shardingsphere-distribution/shardingsphere-proxy-distribution -B -Pnative,docker.native -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
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

- 如果您使用默认构建配置， 你当然可以为 `shardingsphere-distribution/shardingsphere-proxy-distribution/Dockerfile-Native`
  使用 `scratch` 作为 base docker image。
  但如果您主动为`pom.xml`的`native profile`添加`jvmArgs`为`-H:+StaticExecutableWithDynamicLibC`，
  以静态链接除 `glic` 之外的所有内容，您应该切换 base image 到 `busybox:glic`。
  参考 https://www.graalvm.org/22.2/reference-manual/native-image/guides/build-static-executables/ 。
  另请注意，某些第三方依赖将需要更多系统库，例如 `libdl`。
  因此请确保根据您的使用情况调整 base docker image 和`shardingsphere-distribution/shardingsphere-proxy-distribution`
  下的 `pom.xml` 和 `Dockerfile-Native` 的内容。
