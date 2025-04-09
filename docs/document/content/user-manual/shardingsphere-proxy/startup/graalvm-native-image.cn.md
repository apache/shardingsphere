+++
title = "构建 GraalVM Native Image(Alpha)"
weight = 2
+++

## 背景信息

本节主要介绍如何通过 `GraalVM` 的 `native-image` 命令行工具构建 ShardingSphere Proxy 的 `GraalVM Native Image`，
以及包含此 `GraalVM Native Image` 的 `Docker Image`。

ShardingSphere Proxy 的 `GraalVM Native Image` 在本文即指代 ShardingSphere Proxy Native。

GraalVM Native Image 的背景信息可参考 https://www.graalvm.org 。

## 注意事项

本节涉及的所有 Docker Image 均不通过 https://downloads.apache.org ，https://repository.apache.org 等 ASF 官方渠道进行分发。
Docker Image 仅在 `GitHub Packages`，`Docker Hub` 等下游渠道提供以方便使用。

Proxy 的 Native Image 产物在 https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-proxy-native 存在每夜构建。
假设存在包含 `global.yaml` 的 `conf` 文件夹为 `./custom/conf`，你可通过如下的 `docker-compose.yml` 文件进行测试。

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:latest
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy-native/conf
    ports:
      - "3307:3307"
```

ShardingSphere Proxy Native 可执行 DistSQL，这意味着实际上不需要任何定义逻辑数据库的 YAML 文件。

默认情况下，ShardingSphere Proxy Native 的 GraalVM Native Image 中仅包含，

1. ShardingSphere 维护的自有及部分第三方依赖的 GraalVM Reachability Metadata
2. H2database, OpenGauss 和 PostgreSQL 的 JDBC Driver
3. HikariCP 的数据库连接池
4. Logback 的日志框架

如果用户需要在 ShardingSphere Proxy Native 中使用第三方 JAR，则需要修改 `distribution/proxy-native/pom.xml` 的内容，以构建以下的任意输出，

1. 自定义的 GraalVM Native Image
2. 包含自定义的 GraalVM Native Image 的自定义 Docker Image

本节假定处于以下的系统环境之一，

1. Linux（amd64，aarch64）
2. MacOS（amd64，aarch64/M1）
3. Windows（amd64）

若处于 Linux（riscv64）等 Graal compiler 不支持的系统环境，
请根据 https://medium.com/graalvm/graalvm-native-image-meets-risc-v-899be38eddd9 的内容启用 LLVM backend 来使用 LLVM compiler。

用户必须对需要运行 GraalVM Native Image 的每个目标操作系统和目标体系结构，来独立构建不同的 GraalVM Native Image。
用户可以考虑通过 Docker Image 来部分绕开此限制。

本节依然受到 ShardingSphere JDBC 一侧的 [GraalVM Native Image](/cn/user-manual/shardingsphere-jdbc/graalvm-native-image) 的已记录内容的限制。

## 前提条件

1. 根据 https://www.graalvm.org/downloads/ 要求安装和配置 JDK 22 对应的 `GraalVM Community Edition` 或 `GraalVM Community Edition` 的下游发行版。
若使用 `SDKMAN!`，

```shell
sdk install java 22.0.2-graalce
sdk use java 22.0.2-graalce
```

2. 根据 https://www.graalvm.org/jdk23/reference-manual/native-image/#prerequisites 的要求安装本地工具链。

3. 如果需要构建 Docker Image， 确保 `Docker Engine` 已安装。

## 操作步骤

1. 获取 Apache ShardingSphere Git Source

在[下载页面](https://shardingsphere.apache.org/document/current/en/downloads/)或 https://github.com/apache/shardingsphere/tree/master 获取。

2. 在命令行构建产物, 分两种情形。

情形一：不需要使用存在自定义 SPI 实现的 JAR 或第三方依赖的 JAR 。在 Git Source 同级目录下执行如下命令, 直接完成 Native Image 的构建。

```bash
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -Prelease.native -DskipTests clean package
```

情形二：需要使用存在自定义 SPI 实现的 JAR 或第三方依赖的 JAR。在 `distribution/proxy-native/pom.xml` 的 `dependencies` 加入如下选项之一，

(1) 存在 SPI 实现的 JAR
(2) 第三方依赖的 JAR

示例如下，这些 JAR 应预先置入本地 Maven 仓库或 Maven Central 等远程 Maven 仓库。

```xml
<dependencies>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.0.0</version>
    </dependency>
</dependencies>
```

随后通过命令行构建 GraalVM Native Image。

```bash
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -Prelease.native -DskipTests clean package
```

3. 通过命令行启动 Native Image, 需要带上 4 个参数，
   第 1 个参数为 ShardingSphere Proxy Native 使用的端口，
   第 2 个参数为用户编写的包含 `global.yaml` 配置文件的文件夹，
   第 3 个参数为要侦听的主机，如果为 `0.0.0.0` 则允许任意数据库客户端均可访问 ShardingSphere Proxy Native
   第 4 个参数为 Force Start，如果为 `true` 则保证 ShardingSphere Proxy Native 无论能否连接都能正常启动。

已完成构建的 GraalVM Native Image 的二进制文件仅可设置命令行参数。这意味着，

(1) 用户仅可在构建 GraalVM Native Image 的过程中设置 JVM 参数
(2) 用户无法针对已完成构建的 GraalVM Native Image 的二进制文件设置 JVM 参数

假设已存在文件夹`/customAbsolutePath/conf`，示例为，

```bash
cd ./shardingsphere/
cd ./distribution/proxy-native/target/apache-shardingsphere-5.5.2-shardingsphere-proxy-native-bin/
./proxy-native "3307" "/customAbsolutePath/conf" "0.0.0.0" "false"
```

4. 如果需要构建 Docker Image, 在添加存在 SPI 实现的依赖或第三方依赖后, 在命令行执行如下命令，

```shell
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -Prelease.native,docker.native -DskipTests clean package
```

假设存在包含 `global.yaml` 的 `conf` 文件夹为 `./custom/conf`，可通过如下的 `docker-compose.yml` 文件启动包含 GraalVM Native Image 的 Docker Image。

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:latest
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy-native/conf
    ports:
      - "3307:3307"
```

如果用户不对 Git Source 做任何更改，
上文提及的命令将使用 https://yum.oracle.com/oracle-linux-downloads.html 中的 `container-registry.oracle.com/os/oraclelinux:9-slim` 作为 Base Docker Image。
但如果用户希望使用 `scratch`，`alpine:3`，`gcr.io/distroless/base-debian12`，
`gcr.io/distroless/java-base-debian12` 或 `gcr.io/distroless/static-debian12` 等更小体积的 Docker Image 作为 Base Docker Image，
用户可能需要根据 https://www.graalvm.org/jdk23/reference-manual/native-image/guides/build-static-executables/ 的要求，
做为 `pom.xml`的 `Maven Profile` 添加 `--static`，`--libc=musl` 或 `--static-nolibc` 的 `buildArgs` 等操作。

构建静态链接的 GraalVM Native Image 需要更多系统依赖，且目前不支持为 Linux（aarch64）等环境构建静态链接的 GraalVM Native Image。
完全静态链接的 GraalVM Native Image 采用的是 musl libc。
大多数 Linux 系统内置的 musl 已过时，比如 Ubuntu 22.04.5 LTS 使用的 [musl (1.2.2-4) unstable](https://packages.ubuntu.com/jammy/musl)。
用户总是需要从源代码构建和安装新版本的 musl。

另请注意，某些第三方 Maven 依赖将需要在 `Dockerfile` 安装更多系统库，
因此请确保根据使用情况调整 `distribution/proxy-native` 下的 `pom.xml` 和 `Dockerfile` 的内容。

## 可观察性

针对 GraalVM Native Image 形态的 ShardingSphere Proxy，其提供的可观察性的能力与[可观察性](/cn/user-manual/shardingsphere-proxy/observability)并不一致。

用户可以使用 https://www.graalvm.org/jdk23/tools/ 提供的一系列命令行工具或可视化工具观察 GraalVM Native Image 的内部行为，
并根据其要求在 Linux 下使用 VSCode 完成 Debug 工作。如果用户正在使用 IntelliJ IDEA 并且希望调试生成的 GraalVM Native Image，
用户可以关注 https://blog.jetbrains.com/idea/2022/06/intellij-idea-2022-2-eap-5/#Experimental_GraalVM_Native_Debugger_for_Java 及其后继。

如果用户使用的不是 Linux，则无法对 GraalVM Native Image 进行 Debug，请关注尚未关闭的 https://github.com/oracle/graal/issues/5648 。

对于使用 `ShardingSphere Agent` 等 Java Agent 的情形， GraalVM 的 `native-image` 组件尚未完全支持在构建 Native Image 时使用 javaagent，
用户需要关注尚未关闭的 https://github.com/oracle/graal/issues/8177 。

若用户期望在 ShardingSphere Proxy Native 下使用这类 Java Agent，则需要关注 https://github.com/oracle/graal/pull/8077 涉及的变动。

## Seata AT 模式集成

对于 GraalVM Native Image 形态的 ShardingSphere Proxy Native，
用户始终需要修改 ShardingSphere 源代码以添加 Seata Client 和 Seata 集成的 Maven 模块，并编译为 GraalVM Native Image。
GraalVM Native Image 形态的 ShardingSphere Proxy Native 无法识别额外添加的 JAR 文件。

```xml
<project>
    <dependencies>
      <dependency>
         <groupId>org.apache.shardingsphere</groupId>
         <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
         <version>${shardingsphere.version}</version>
      </dependency>
      <dependency>
         <groupId>org.apache.seata</groupId>
         <artifactId>seata-all</artifactId>
         <version>2.3.0</version>
         <exclusions>
            <exclusion>
               <groupId>org.antlr</groupId>
               <artifactId>antlr4-runtime</artifactId>
            </exclusion>
         </exclusions>
      </dependency>
    </dependencies>
</project>
```
