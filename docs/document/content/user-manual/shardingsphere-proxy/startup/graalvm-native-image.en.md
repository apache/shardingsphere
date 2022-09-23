+++
title = "Build GraalVM Native Image(Alpha)"
weight = 2
+++

## Background

This section mainly introduces how to build the `Native Image` of ShardingSphere-Proxy and the
corresponding `Docker Image` through the `native-image` component of `GraalVM`.

## Notice

- ShardingSphere Proxy is not yet ready to integrate with GraalVM Native Image.
  It has daily build tasks at https://github.com/apache/shardingsphere/actions/ for testing builds.

- If you find that the build process has missing GraalVM Reachability Metadata,
  A new issue should be opened at https://github.com/oracle/graalvm-reachability-metadata,
  And submit a PR containing GraalVM Reachability Metadata missing from ShardingSphere itself or dependent third-party
  libraries.

- The master branch of ShardingSphere is not yet ready to handle unit tests in Native Image,
  Need to wait for the integration of Junit 5 Platform, you always need to build GraalVM Native Image in the process,
  Plus `-DskipNativeTests` or `-DskipTests` parameter specific to `GraalVM Native Build Tools` to skip unit tests in
  Native Image.

- This section assumes a Linux (amd64, aarch64), MacOS (amd64) or Windows (amd64) environment.
  If you are on MacOS(aarch64/M1) environment, you need to follow https://github.com/oracle/graal/issues/2666 which is
  not closed yet.

## Premise

1. Install and configure `GraalVM CE` or `GraalVM EE` for JDK 17 according to https://www.graalvm.org/downloads/.
   `GraalVM CE` for JDK 17 can also be installed via `SDKMAN!`.

2. Install the `native-image` component via the `GraalVM Updater` tool.

3. Install the local toolchain as required by https://www.graalvm.org/22.2/reference-manual/native-image/#prerequisites.

4. If you need to build a Docker Image, make sure `docker-cli` is in the system environment variables.

## Steps

1. Get Apache ShardingSphere Git Source

- Get it at the [download page](https://shardingsphere.apache.org/document/current/en/downloads/)
  or https://github.com/apache/shardingsphere/tree/master.

2. Build the product on the command line, in two cases.

- Scenario 1: No need to use JARs with SPI implementations or 3rd party dependencies

- Execute the following command in the same directory of Git Source to directly complete the construction of Native
  Image.

```bash
./mvnw -am -pl shardingsphere-distribution/shardingsphere-proxy-distribution -B -Pnative -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
```

- Scenario 2: It is necessary to use a JAR that has an SPI implementation or a third-party dependent JAR of a LICENSE
  such as GPL V2.

- Add SPI implementation JARs or third-party dependent JARs to `dependencies`
  in `shardingsphere-distribution/shardingsphere-proxy-distribution/pom.xml`. Examples are as follows

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

- Execute the following commands in the same directory as Git Source.

```bash
./mvnw -am -pl shardingsphere-distribution/shardingsphere-proxy-distribution -B -Pnative -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat.skip=true clean package
```

- Build GraalVM Native Image via command line.

```bash
./mvnw org.graalvm.buildtools:native-maven-plugin:compile-no-fork -am -pl shardingsphere-distribution/shardingsphere-proxy-distribution -Pnative -DskipTests
```

3. Start Native Image through the command line, you need to bring two parameters,
   The first parameter is the port used by ShardingSphere Proxy, and the second parameter is the `/conf` folder that
   contains `server.yaml` written by you,
   Assuming the folder `./custom/conf` already exists, the example is

```bash
./apache-shardingsphere-proxy 3307 ./custom/conf
````

4. If you need to build a Docker Image, after adding the dependencies of the SPI implementation or third-party
   dependencies, execute the following commands on the command line.

```shell
./mvnw -am -pl shardingsphere-distribution/shardingsphere-proxy-distribution -B -Pnative,docker.native -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotless.apply.skip=true -Drat .skip=true clean package
```

- Assuming that there is a `conf` folder containing `server.yaml` as `./custom/conf`, you can start the Docker Image
  corresponding to GraalVM Native Image through the following `docker-compose.yml` file.

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

- If you use the default build configuration, you can of course use `scratch` as the base docker image
  for `shardingsphere-distribution/shardingsphere-proxy-distribution/Dockerfile-Native`.
  But if you actively add `jvmArgs` to `-H:+StaticExecutableWithDynamicLibC` for the `native profile` of `pom.xml`,
  To statically link everything except `glic`, you should switch the base image to `busybox:glic`. Refer
  to https://www.graalvm.org/22.2/reference-manual/native-image/guides/build-static-executables/.
  Also note that some third-party dependencies will require more system libraries, such as `libdl`.
  So make sure to adjust the base docker image and the content of `pom.xml` and `Dockerfile-Native`
  under `shardingsphere-distribution/shardingsphere-proxy-distribution` according to your usage.
