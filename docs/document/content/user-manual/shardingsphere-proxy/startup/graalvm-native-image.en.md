+++
title = "Build GraalVM Native Image(Alpha)"
weight = 2
+++

## Background

This section mainly introduces how to build the `Native Image` of ShardingSphere-Proxy and the
corresponding `Docker Image` through the `native-image` component of `GraalVM`.

## Notice

- ShardingSphere Proxy is not yet ready to integrate with GraalVM Native Image. Proxy's Native Image artifacts are
  built nightly at https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-proxy-native .
  Assuming there is a `conf` folder containing `global.yaml` as `./custom/conf`, you can test it with the
  following `docker-compose.yml` file.

````yaml
version: "3.8"

services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:latest
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy-native/conf
    ports:
      - "3307:3307"
````

- This section assumes a Linux (amd64, aarch64), MacOS (amd64) or Windows (amd64) environment.
  If you are on MacOS (aarch64/M1) environment, you need to follow https://github.com/oracle/graal/issues/2666 which is
  not closed yet.

## Premise

1. Install and configure `GraalVM Community Edition` or a downstream distribution of `GraalVM Community Edition` for 
JDK 21 according to https://www.graalvm.org/downloads/. If `SDKMAN!` is used,

```shell
sdk install java 21.0.2-graalce
```

2. Install the local toolchain as required by https://www.graalvm.org/jdk17/reference-manual/native-image/#prerequisites.

3. If you need to build a Docker Image, make sure `docker-ce` is installed.

## Steps

1. Get Apache ShardingSphere Git Source

- Get it at the [download page](https://shardingsphere.apache.org/document/current/en/downloads/)
  or https://github.com/apache/shardingsphere/tree/master.

2. Build the product on the command line, in two cases.

- Scenario 1: No need to use JARs with SPI implementations or 3rd party dependencies

- Execute the following command in the same directory of Git Source to directly complete the construction of Native
  Image.

```bash
./mvnw -am -pl distribution/proxy-native -T1C -Prelease.native -DskipTests clean package
```

- Scenario 2: It is necessary to use a JAR that has an SPI implementation or a third-party dependent JAR of a LICENSE
  such as GPL V2.

- Add SPI implementation JARs or third-party dependent JARs to `dependencies`
  in `distribution/proxy-native/pom.xml`. Examples are as follows

```xml

<dependencies>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.1.0</version>
    </dependency>
</dependencies>
```

- Build GraalVM Native Image via command line.

```bash
./mvnw -am -pl distribution/proxy-native -T1C -Prelease.native -DskipTests clean package
```

3. To start Native Image through the command line, you need to bring 4 parameters. The first parameter is the `Port`
   used by ShardingSphere Proxy, the second parameter is the `/conf` folder containing `global.yaml` written by you, the
   third parameter is the `Address` of the bound port, and the fourth parameter is `Force Start`, if it is true, it will
   ensure that ShardingSphere Proxy Native can start normally no matter whether it is connected or not. Assuming the
   folder `./custom/conf` already exists, the example is

```bash
./apache-shardingsphere-proxy-native 3307 ./custom/conf "0.0.0.0" false
````

4. If you need to build a Docker Image, execute the following command on the command line after adding dependencies that
   exist for SPI implementation or third-party dependencies.

```shell
./mvnw -am -pl distribution/proxy-native -B -T1C -Prelease.native,docker.native -DskipTests clean package
```

- Assuming that there is a `conf` folder containing `global.yaml` as `./custom/conf`, you can start the Docker Image
  corresponding to GraalVM Native Image through the following `docker-compose.yml` file.

```yaml
version: "3.8"

services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:latest
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy-native/conf
    ports:
      - "3307:3307"
```

- If you don't make any changes to the Git Source, the commands mentioned above will use `oraclelinux:9-slim` as the
  Base Docker Image. But if you want to use a smaller Docker Image like `busybox:glic`, `gcr.io/distroless/base` or
  `scratch` as the Base Docker Image, you need according
  to https://www.graalvm.org/jdk17/reference-manual/native-image/guides/build-static-executables/,
  add operations such as `-H:+StaticExecutableWithDynamicLibC` to `jvmArgs` as the `native profile` of `pom.xml`.
  Also note that some 3rd-party dependencies will require more system libraries such as `libdl` to be installed in
  the `Dockerfile`. So make sure to tune `distribution/proxy-native` according to your usage `pom.xml` and `Dockerfile`
  below.

## Observability

ShardingSphere for GraalVM Native Image form Proxy, which provides observability capabilities
with https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-proxy/observability/
not consistent.

You can observe GraalVM Native Image using a series of command line tools or visualization tools available
at https://www.graalvm.org/jdk17/tools/, and use VSCode to debug it according to its requirements.
If you are using IntelliJ IDEA and want to debug the generated GraalVM Native Image, You can follow
https://blog.jetbrains.com/idea/2022/06/intellij-idea-2022-2-eap-5/#Experimental_GraalVM_Native_Debugger_for_Java
and its successors. If you are not using Linux, you cannot debug GraalVM Native Image, please pay attention
to https://github.com/oracle/graal/issues/5648 which has not been closed yet.

For the use of Java Agents such as `ShardingSphere Agent`, GraalVM's `native-image` component does not yet fully support building Native
when using javaagent with Image, you need to pay attention to https://github.com/oracle/graal/issues/1065 which has not yet been closed.

If users expect to use this type of Java Agent under ShardingSphere Proxy Native, they need to pay attention to the changes involved in https://github.com/oracle/graal/pull/8077 .
