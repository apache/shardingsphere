+++
title = "Build GraalVM Native Image(Alpha)"
weight = 2
+++

## Background

This section mainly introduces it how to build the `GraalVM Native Image` of ShardingSphere Proxy through the `native-image` command line tool of `GraalVM`,
and the `Docker Image` containing this `GraalVM Native Image`.

The `GraalVM Native Image` of ShardingSphere Proxy refers to ShardingSphere Proxy Native in this article.

For background information about GraalVM Native Image, please refer to https://www.graalvm.org .

## Notice

All Docker Images mentioned in this section are not distributed through ASF official channels such as https://downloads.apache.org and https://repository.apache.org/ .
Docker Images are only provided on downstream channels such as `ghcr.io` for convenience.

Native Image products of Proxy exist in nightly builds at https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-proxy-native .
Assuming that there is a `conf` folder containing `global.yaml` as `./custom/conf`, you can test it with the following `docker-compose.yml` file.

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:latest
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy-native/conf
    ports:
      - "3307:3307"
```

ShardingSphere Proxy Native can execute DistSQL, which means that no YAML file that defines the logical database is actually required.

By default, the GraalVM Native Image of ShardingSphere Proxy Native only contains,

1. GraalVM Reachability Metadata maintained by ShardingSphere and some third-party dependencies
2. JDBC Driver for H2database, OpenGauss and PostgreSQL
3. HikariCP database connection pool
4. Logback logging framework

If the user needs to use third-party JAR in ShardingSphere Proxy Native, 
the content of `distribution/proxy-native/pom.xml` needs to be modified to build any of the following outputs,

1. Customized GraalVM Native Image
2. Customized Docker Image containing customized GraalVM Native Image

This section assumes that you are in one of the following system environments:

1. Linux (amd64, aarch64)
2. MacOS (amd64, aarch64/M1)
3. Windows (amd64)

If you are in a system environment that Graal compiler does not support, such as Linux (riscv64),
please enable LLVM backend according to the content of https://medium.com/graalvm/graalvm-native-image-meets-risc-v-899be38eddd9 to use the LLVM compiler.

This section is still limited by the documented content of the [GraalVM Native Image](/us/user-manual/shardingsphere-jdbc/graalvm-native-image) on the ShardingSphere JDBC side.

## Premise

1. Install and configure `GraalVM Community Edition` or a downstream distribution of `GraalVM Community Edition` for JDK 22 according to https://www.graalvm.org/downloads/ .
   If using `SDKMAN!`,

```shell
sdk install java 22.0.2-graalce
sdk use java 22.0.2-graalce
```

2. Install the native toolchain according to https://www.graalvm.org/jdk23/reference-manual/native-image/#prerequisites .

3. If you need to build a Docker Image, make sure `Docker Engine` is installed.

## Steps

1. Get Apache ShardingSphere Git Source

Get it from [Download page](https://shardingsphere.apache.org/document/current/en/downloads/) or https://github.com/apache/shardingsphere/tree/master .

2. Build the product in the command line, divided into two cases.

Case 1: No need to use JAR with custom SPI implementation or third-party dependent JAR. Execute the following command in the same directory as Git Source to directly complete the construction of Native Image.

```bash
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -Prelease.native -DskipTests clean package
```

Case 2: Need to use JAR with custom SPI implementation or third-party dependent JAR. Add one of the following options to the `dependencies` of `distribution/proxy-native/pom.xml`:

(1) JARs with SPI implementations
(2) JARs with third-party dependencies

The examples are as follows. 
These JARs should be pre-placed in the local Maven repository or a remote Maven repository such as Maven Central.

```xml
<dependencies>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.0.0</version>
    </dependency>
</dependencies>
```

Then build the GraalVM Native Image through the command line.

```bash
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -Prelease.native -DskipTests clean package
```

3. To start Native Image through the command line, you need to bring 4 parameters.
   The first parameter is the port used by ShardingSphere Proxy Native.
   The second parameter is the folder containing the `global.yaml` configuration file written by the user.
   The third parameter is the host to listen to. If it is `0.0.0.0`, any database client is allowed to access ShardingSphere Proxy Native.
   The fourth parameter is Force Start. If it is `true`, it ensures that ShardingSphere Proxy Native can start normally regardless of whether it can be connected.

Assuming the folder `./custom/conf` already exists, the example is.

```bash
cd ./shardingsphere/
cd ./distribution/proxy-native/target/apache-shardingsphere-5.5.1-SNAPSHOT-shardingsphere-proxy-native-bin/
./proxy-native "3307" "./custom/conf" "0.0.0.0" "false"
```

4. If you need to build a Docker Image, after adding the dependencies that have SPI implementation or third-party dependencies, 
   execute the following command in the command line:

```shell
./mvnw -am -pl distribution/proxy-native -T1C -Prelease.native,docker.native -DskipTests clean package
```

Assuming that there is a conf folder called `./custom/conf` containing `global.yaml`, 
you can start the Docker Image containing the GraalVM Native Image using the following `docker-compose.yml` file,

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:latest
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy-native/conf
    ports:
      - "3307:3307"
```

If you do not make any changes to the Git Source, 
the commands mentioned above will use `oraclelinux:9-slim` as the Base Docker Image.
But if you want to use a smaller Docker Image such as `busybox:glic`, 
`gcr.io/distroless/base` or `scratch` as the Base Docker Image, 
you need to follow the requirements of https://www.graalvm.org/jdk23/reference-manual/native-image/guides/build-static-executables/ and add `-H:+StaticExecutableWithDynamicLibC` to the `jvmArgs` of the `native profile` in `pom.xml`.
Also note that some third-party dependencies will require more system libraries to be installed in the `Dockerfile`, such as `libdl`.
So make sure to adjust the contents of `pom.xml` and `Dockerfile` under `distribution/proxy-native` according to your usage.

## Observability

The observability provided by ShardingSphere Proxy in the form of GraalVM Native Image is not consistent with [observability](/cn/user-manual/shardingsphere-proxy/observability).

Users can use a series of command-line tools or visualization tools provided by https://www.graalvm.org/jdk23/tools/ to observe the internal behavior of GraalVM Native Image,
and use VSCode to complete debugging work according to their requirements. 
If users are using IntelliJ IDEA and want to debug the generated GraalVM Native Image,
users can follow https://blog.jetbrains.com/idea/2022/06/intellij-idea-2022-2-eap-5/#Experimental_GraalVM_Native_Debugger_for_Java and its successors.
If users are not using Linux, they cannot debug GraalVM Native Image. Please follow https://github.com/oracle/graal/issues/5648 which has not been closed.

For Java Agents such as `ShardingSphere Agent`, the `native-image` component of GraalVM does not fully support the use of javaagent when building Native Image.
Users need to pay attention to https://github.com/oracle/graal/issues/1065 which has not been closed.

If users expect to use such Java Agents under ShardingSphere Proxy Native, they need to pay attention to the changes involved in https://github.com/oracle/graal/pull/8077 .
