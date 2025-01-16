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

All Docker images mentioned in this section are not distributed through ASF official channels such as https://downloads.apache.org and https://repository.apache.org .
Docker images are only provided in downstream channels such as `GitHub Packages` and `Docker Hub` for easy use.

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

Users must build the different GraalVM Native Image for each target operating system and target architecture that they need to run the GraalVM Native Image on.
Users can consider partially circumventing this limitation by using Docker Image.

This section is still limited by the documented content of the [GraalVM Native Image](/en/user-manual/shardingsphere-jdbc/graalvm-native-image) on the ShardingSphere JDBC side.

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

Only command line parameters can be set for binaries of built GraalVM Native Images. This means that:

(1) Users can only set JVM parameters during the process of building a GraalVM Native Image
(2) Users cannot set JVM parameters for binaries of built GraalVM Native Images

Assuming the folder `/customAbsolutePath/conf` already exists, the example is.

```bash
cd ./shardingsphere/
cd ./distribution/proxy-native/target/apache-shardingsphere-5.5.2-shardingsphere-proxy-native-bin/
./proxy-native "3307" "/customAbsolutePath/conf" "0.0.0.0" "false"
```

4. If you need to build a Docker Image, after adding the dependencies that have SPI implementation or third-party dependencies,
   execute the following command in the command line:

```shell
cd ./shardingsphere/
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

If the user does not make any changes to the Git Source,
the above mentioned command will use `container-registry.oracle.com/os/oraclelinux:9-slim` in https://yum.oracle.com/oracle-linux-downloads.html as the Base Docker Image.
But if the user wants to use a smaller Docker Image such as `scratch`, `alpine:3`, `gcr.io/distroless/base-debian12`,
`gcr.io/distroless/java-base-debian12` or `gcr.io/distroless/static-debian12` as the Base Docker Image,
the user may need to add `--static`,
`--libc=musl` or `--static-nolibc` to the `Maven Profile` of `pom.xml` as required by https://www.graalvm.org/jdk23/reference-manual/native-image/guides/build-static-executables/ and other operations such as `buildArgs`.

Building a statically linked GraalVM Native Image requires more system dependencies,
and currently does not support building statically linked GraalVM Native Images for environments such as Linux (aarch64).
Fully statically linked GraalVM Native Images use musl libc.
Most Linux systems come with outdated musl, such as Ubuntu 22.04.5 LTS which uses [musl (1.2.2-4) unstable](https://packages.ubuntu.com/jammy/musl).
Users always need to build and install a new version of musl from source.

Also note that some third-party Maven dependencies will require more system libraries to be installed in the `Dockerfile`,
so make sure to adjust the contents of `pom.xml` and `Dockerfile` under `distribution/proxy-native` according to your usage.

## Observability

The observability provided by ShardingSphere Proxy in the form of GraalVM Native Image is not consistent with [observability](/cn/user-manual/shardingsphere-proxy/observability).

Users can use a series of command-line tools or visualization tools provided by https://www.graalvm.org/jdk23/tools/ to observe the internal behavior of GraalVM Native Image,
and use VSCode under Linux to complete debugging work according to their requirements.
If the user is using IntelliJ IDEA and wants to debug the generated GraalVM Native Image,
the user can follow https://blog.jetbrains.com/idea/2022/06/intellij-idea-2022-2-eap-5/#Experimental_GraalVM_Native_Debugger_for_Java and its successors.

If the user is not using Linux, the GraalVM Native Image cannot be debugged.
Please follow https://github.com/oracle/graal/issues/5648 which has not been closed.

For Java Agents such as `ShardingSphere Agent`, the `native-image` component of GraalVM does not fully support the use of javaagent when building Native Image.
Users need to pay attention to https://github.com/oracle/graal/issues/8177 which has not been closed.

If users expect to use such Java Agents under ShardingSphere Proxy Native, they need to pay attention to the changes involved in https://github.com/oracle/graal/pull/8077 .

## Seata AT mode integration

For ShardingSphere Proxy Native in GraalVM Native Image,
Users always need to modify the ShardingSphere source code to add the Seata Client and Seata integrated Maven modules and compile them into GraalVM Native Image.
ShardingSphere Proxy Native in GraalVM Native Image cannot recognize the additional JAR files.

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
         <version>2.2.0</version>
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
